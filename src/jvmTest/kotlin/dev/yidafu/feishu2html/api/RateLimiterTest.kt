package dev.yidafu.feishu2html.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger

class RateLimiterTest : FunSpec({

    test("应该限制每秒最大请求数") {
        val rateLimiter = RateLimiter(maxRequestsPerSecond = 3)
        val counter = AtomicInteger(0)

        // 快速发送5个请求
        val startTime = System.currentTimeMillis()
        repeat(5) {
            rateLimiter.execute {
                counter.incrementAndGet()
            }
        }
        val endTime = System.currentTimeMillis()

        // 应该花费至少1秒（因为前3个在1秒内，后2个需要等待）
        val duration = endTime - startTime
        counter.get() shouldBe 5
        (duration >= 1000L && duration <= 2500L) shouldBe true // 允许一些时间误差
    }

    test("并发请求应该被正确限流") {
        val rateLimiter = RateLimiter(maxRequestsPerSecond = 5)
        val counter = AtomicInteger(0)

        // 同时发起10个请求
        val jobs =
            (1..10).map {
                async {
                    rateLimiter.execute {
                        counter.incrementAndGet()
                        delay(10) // 模拟一些处理时间
                    }
                }
            }

        jobs.awaitAll()

        counter.get() shouldBe 10
    }

    test("应该在限频错误后重试") {
        val rateLimiter =
            RateLimiter(
                maxRequestsPerSecond = 5,
                maxRetries = 3,
                initialBackoffMs = 100,
            )
        val attemptCount = AtomicInteger(0)

        // 前两次模拟失败，第三次成功
        val result =
            rateLimiter.execute {
                val count = attemptCount.incrementAndGet()
                if (count < 3) {
                    throw FeishuApiException("API限频", code = 99991400)
                }
                "success"
            }

        result shouldBe "success"
        attemptCount.get() shouldBe 3
    }

    test("应该在达到最大重试次数后抛出异常") {
        val rateLimiter =
            RateLimiter(
                maxRequestsPerSecond = 5,
                maxRetries = 3,
                initialBackoffMs = 50,
            )

        // 总是抛出限频异常
        val exception =
            shouldThrow<FeishuApiException> {
                rateLimiter.execute {
                    throw FeishuApiException("API限频", code = 99991400)
                }
            }

        exception.code shouldBe 99991400
    }

    test("非限频异常应该立即抛出") {
        val rateLimiter = RateLimiter(maxRequestsPerSecond = 5)

        val exception =
            shouldThrow<FeishuApiException> {
                rateLimiter.execute {
                    throw FeishuApiException("其他错误", code = 12345)
                }
            }

        exception.code shouldBe 12345
    }

    test("应该正确获取当前请求计数") {
        val rateLimiter = RateLimiter(maxRequestsPerSecond = 5)

        // 发送3个请求
        repeat(3) {
            rateLimiter.execute { }
        }

        val count = rateLimiter.getCurrentRequestCount()
        count shouldBe 3

        // 等待1秒后，计数应该清零
        delay(1100)
        val countAfterDelay = rateLimiter.getCurrentRequestCount()
        countAfterDelay shouldBe 0
    }

    test("应该清理超过1秒的旧请求记录") {
        val rateLimiter = RateLimiter(maxRequestsPerSecond = 5)

        // 发送2个请求
        repeat(2) {
            rateLimiter.execute { }
        }

        rateLimiter.getCurrentRequestCount() shouldBe 2

        // 等待1.1秒
        delay(1100)

        // 再发送1个请求
        rateLimiter.execute { }

        // 应该只有1个请求（旧的已被清理）
        rateLimiter.getCurrentRequestCount() shouldBe 1
    }

    // === 新增边界测试 ===

    test("边界测试: permitsPerSecond = 1 应该每秒只允许1个请求") {
        val rateLimiter = RateLimiter(maxRequestsPerSecond = 1)
        val counter = AtomicInteger(0)

        val startTime = System.currentTimeMillis()

        // 发送3个请求
        repeat(3) {
            rateLimiter.execute {
                counter.incrementAndGet()
            }
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        counter.get() shouldBe 3
        // 至少需要2秒（第1个立即，第2个等1秒，第3个再等1秒）
        (duration >= 2000L) shouldBe true
    }

    test("边界测试: 高并发下的正确性 (100个并发请求)") {
        val rateLimiter = RateLimiter(maxRequestsPerSecond = 10)
        val counter = AtomicInteger(0)

        val jobs = (1..100).map {
            async {
                rateLimiter.execute {
                    counter.incrementAndGet()
                }
            }
        }

        jobs.awaitAll()
        counter.get() shouldBe 100
    }

    test("边界测试: permitsPerSecond = 1000 高速率限制") {
        val rateLimiter = RateLimiter(maxRequestsPerSecond = 1000)
        val counter = AtomicInteger(0)

        // 发送100个请求，应该几乎立即完成
        val startTime = System.currentTimeMillis()
        repeat(100) {
            rateLimiter.execute {
                counter.incrementAndGet()
            }
        }
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        counter.get() shouldBe 100
        // 应该在1秒内完成
        (duration < 1000L) shouldBe true
    }

    test("边界测试: 零延迟重试策略") {
        val rateLimiter = RateLimiter(
            maxRequestsPerSecond = 5,
            maxRetries = 2,
            initialBackoffMs = 0 // 零延迟
        )
        val attemptCount = AtomicInteger(0)

        val result = rateLimiter.execute {
            val count = attemptCount.incrementAndGet()
            if (count < 2) {
                throw FeishuApiException("API限频", code = 99991400)
            }
            "success"
        }

        result shouldBe "success"
        attemptCount.get() shouldBe 2
    }

    test("并发安全性: 多线程同时访问") {
        val rateLimiter = RateLimiter(maxRequestsPerSecond = 10)
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)

        val jobs = (1..50).map {
            async {
                try {
                    rateLimiter.execute {
                        delay(10) // 模拟处理
                        successCount.incrementAndGet()
                    }
                } catch (e: Exception) {
                    failureCount.incrementAndGet()
                }
            }
        }

        jobs.awaitAll()

        successCount.get() shouldBe 50
        failureCount.get() shouldBe 0
    }

    test("指数退避测试: 重试延迟应该递增") {
        val rateLimiter = RateLimiter(
            maxRequestsPerSecond = 5,
            maxRetries = 4,
            initialBackoffMs = 100
        )
        val attemptTimes = mutableListOf<Long>()

        val startTime = System.currentTimeMillis()
        val result = rateLimiter.execute {
            attemptTimes.add(System.currentTimeMillis() - startTime)
            if (attemptTimes.size < 3) {
                throw FeishuApiException("API限频", code = 99991400)
            }
            "success"
        }

        result shouldBe "success"
        attemptTimes.size shouldBe 3

        // 验证延迟递增：第1次立即，第2次约100ms后，第3次约200-300ms后
        if (attemptTimes.size >= 2) {
            (attemptTimes[1] >= 80L) shouldBe true // 允许一些误差
        }
        if (attemptTimes.size >= 3) {
            (attemptTimes[2] >= attemptTimes[1] + 80L) shouldBe true
        }
    }

    test("长时间等待场景: 连续多个请求应该被正确限流") {
        val rateLimiter = RateLimiter(maxRequestsPerSecond = 2)
        val counter = AtomicInteger(0)

        val startTime = System.currentTimeMillis()

        // 发送5个请求，每秒限制2个
        repeat(5) {
            rateLimiter.execute {
                counter.incrementAndGet()
            }
        }

        val duration = System.currentTimeMillis() - startTime

        // 应该完成所有5个请求
        counter.get() shouldBe 5
        // 至少需要2秒（前2个立即，第3-4个等1秒，第5个再等1秒）
        (duration >= 2000L) shouldBe true
    }

    test("错误传播: 业务异常应该正确传播") {
        val rateLimiter = RateLimiter(maxRequestsPerSecond = 5)

        val exception = shouldThrow<IllegalStateException> {
            rateLimiter.execute {
                throw IllegalStateException("Business error")
            }
        }

        exception.message shouldBe "Business error"
    }

    test("返回值测试: 复杂对象应该正确返回") {
        val rateLimiter = RateLimiter(maxRequestsPerSecond = 5)

        data class ComplexResult(val id: Int, val name: String, val items: List<String>)

        val result = rateLimiter.execute {
            ComplexResult(
                id = 123,
                name = "Test",
                items = listOf("item1", "item2", "item3")
            )
        }

        result.id shouldBe 123
        result.name shouldBe "Test"
        result.items.size shouldBe 3
    }
})

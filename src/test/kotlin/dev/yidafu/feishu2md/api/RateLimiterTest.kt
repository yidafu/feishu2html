package dev.yidafu.feishu2md.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeInRange
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
        val jobs = (1..10).map {
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
        val rateLimiter = RateLimiter(
            maxRequestsPerSecond = 5,
            maxRetries = 3,
            initialBackoffMs = 100
        )
        val attemptCount = AtomicInteger(0)

        // 前两次模拟失败，第三次成功
        val result = rateLimiter.execute {
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
        val rateLimiter = RateLimiter(
            maxRequestsPerSecond = 5,
            maxRetries = 3,
            initialBackoffMs = 50
        )

        // 总是抛出限频异常
        val exception = shouldThrow<FeishuApiException> {
            rateLimiter.execute {
                throw FeishuApiException("API限频", code = 99991400)
            }
        }

        exception.code shouldBe 99991400
    }

    test("非限频异常应该立即抛出") {
        val rateLimiter = RateLimiter(maxRequestsPerSecond = 5)

        val exception = shouldThrow<FeishuApiException> {
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
})


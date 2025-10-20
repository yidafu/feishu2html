package dev.yidafu.feishu2html.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

class RetryUtilsTest : FunSpec({

    test("withRetry should succeed on first attempt") {
        val result = runBlocking {
            withRetry {
                "success"
            }
        }

        result shouldBe "success"
    }

    test("withRetry should retry and eventually succeed") {
        val attemptCount = AtomicInteger(0)

        val result = runBlocking {
            withRetry(maxRetries = 3, initialDelay = 10) {
                val count = attemptCount.incrementAndGet()
                if (count < 3) {
                    throw Exception("Attempt $count failed")
                }
                "success after ${count} attempts"
            }
        }

        result shouldBe "success after 3 attempts"
        attemptCount.get() shouldBe 3
    }

    test("withRetry should throw after max retries exhausted") {
        val attemptCount = AtomicInteger(0)

        val exception = shouldThrow<Exception> {
            runBlocking {
                withRetry(maxRetries = 3, initialDelay = 10) {
                    attemptCount.incrementAndGet()
                    throw Exception("Always fails")
                }
            }
        }

        exception.message shouldBe "Always fails"
        attemptCount.get() shouldBe 3
    }

    test("withRetry should respect retryOn predicate") {
        val attemptCount = AtomicInteger(0)

        val exception = shouldThrow<IllegalArgumentException> {
            runBlocking {
                withRetry(
                    maxRetries = 5,
                    initialDelay = 10,
                    retryOn = { it !is IllegalArgumentException }
                ) {
                    attemptCount.incrementAndGet()
                    throw IllegalArgumentException("Not retriable")
                }
            }
        }

        exception.message shouldBe "Not retriable"
        attemptCount.get() shouldBe 1  // Should fail immediately
    }

    test("withRetry should use exponential backoff") {
        val attemptCount = AtomicInteger(0)

        val duration = measureTimeMillis {
            shouldThrow<Exception> {
                runBlocking {
                    withRetry(
                        maxRetries = 3,
                        initialDelay = 100,
                        factor = 2.0
                    ) {
                        attemptCount.incrementAndGet()
                        throw Exception("Fail")
                    }
                }
            }
        }

        // Total delay should be approximately: 100ms + 200ms = 300ms
        // (3rd attempt throws immediately)
        duration shouldBeGreaterThanOrEqual 250
        duration shouldBeLessThan 500
        attemptCount.get() shouldBe 3
    }

    test("withRetry should respect maxDelay") {
        val delays = mutableListOf<Long>()

        shouldThrow<Exception> {
            runBlocking {
                withRetry(
                    maxRetries = 5,
                    initialDelay = 100,
                    maxDelay = 250,
                    factor = 3.0
                ) {
                    val startTime = System.currentTimeMillis()
                    if (delays.isNotEmpty()) {
                        delays.add(System.currentTimeMillis() - startTime)
                    }
                    throw Exception("Fail")
                }
            }
        }

        // Delays should not exceed maxDelay
        // Expected: 100, 250, 250, 250
    }

    test("withRetry should validate parameters") {
        shouldThrow<IllegalArgumentException> {
            runBlocking {
                withRetry(maxRetries = 0) {
                    "test"
                }
            }
        }

        shouldThrow<IllegalArgumentException> {
            runBlocking {
                withRetry(initialDelay = 0) {
                    "test"
                }
            }
        }

        shouldThrow<IllegalArgumentException> {
            runBlocking {
                withRetry(factor = 1.0) {
                    "test"
                }
            }
        }
    }

    test("isRetriableException should return true for NetworkError") {
        val exception = FeishuApiException.NetworkError("Connection failed", RuntimeException())
        isRetriableException(exception) shouldBe true
    }

    test("isRetriableException should return true for RateLimitError") {
        val exception = FeishuApiException.RateLimitError(60)
        isRetriableException(exception) shouldBe true
    }

    test("isRetriableException should return false for AuthenticationError") {
        val exception = FeishuApiException.AuthenticationError("Invalid credentials", 401)
        isRetriableException(exception) shouldBe false
    }

    test("isRetriableException should return false for InsufficientPermission") {
        val exception = FeishuApiException.InsufficientPermission("doc123")
        isRetriableException(exception) shouldBe false
    }

    test("isRetriableException should return false for DocumentNotFound") {
        val exception = FeishuApiException.DocumentNotFound("doc123")
        isRetriableException(exception) shouldBe false
    }

    test("isRetriableException should return true for server errors (5xx)") {
        FeishuApiException.ApiError("Internal Server Error", 500).let {
            isRetriableException(it) shouldBe true
        }

        FeishuApiException.ApiError("Bad Gateway", 502).let {
            isRetriableException(it) shouldBe true
        }

        FeishuApiException.ApiError("Service Unavailable", 503).let {
            isRetriableException(it) shouldBe true
        }

        FeishuApiException.ApiError("Gateway Timeout", 504).let {
            isRetriableException(it) shouldBe true
        }
    }

    test("isRetriableException should return false for client errors (4xx)") {
        FeishuApiException.ApiError("Bad Request", 400).let {
            isRetriableException(it) shouldBe false
        }

        FeishuApiException.ApiError("Not Found", 404).let {
            isRetriableException(it) shouldBe false
        }
    }

    test("isRetriableException should return false for unknown exceptions") {
        isRetriableException(RuntimeException("Unknown")) shouldBe false
        isRetriableException(IllegalStateException("Unknown")) shouldBe false
    }

    test("withRetry with custom retryOn predicate") {
        val attemptCount = AtomicInteger(0)

        val result = runBlocking {
            withRetry(
                maxRetries = 5,
                initialDelay = 10,
                retryOn = { it is IllegalStateException }
            ) {
                val count = attemptCount.incrementAndGet()
                if (count < 3) {
                    throw IllegalStateException("Retry me")
                }
                "success"
            }
        }

        result shouldBe "success"
        attemptCount.get() shouldBe 3
    }
})


package dev.yidafu.feishu2html.api

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

/**
 * Request rate limiter
 *
 * Feishu API limit: Maximum 5 requests per second per application
 * Exceeding this limit will result in HTTP 400 with error code 99991400
 */
internal class RateLimiter(
    private val maxRequestsPerSecond: Int = 5,
    private val maxRetries: Int = 3,
    private val initialBackoffMs: Long = 1000,
) {
    private val logger = LoggerFactory.getLogger(RateLimiter::class.java)
    private val mutex = Mutex()
    private val requestTimes = mutableListOf<Long>()

    /**
     * Execute a request with rate limiting control
     *
     * @param block The request operation to execute
     * @return Request result
     * @throws Exception If all retries fail
     */
    suspend fun <T> execute(block: suspend () -> T): T {
        var lastException: Exception? = null

        for (attempt in 0 until maxRetries) {
            try {
                // Wait until a slot is available
                waitForAvailableSlot()

                // Execute request
                val result = block()

                // Request succeeded, record the time
                recordRequest()

                return result
            } catch (e: FeishuApiException) {
                // Check if it's a rate limit error
                if (e.code == 99991400) {
                    logger.warn(
                        "Rate limit error encountered (99991400), retry attempt {}/{}",
                        attempt + 1,
                        maxRetries,
                    )
                    lastException = e

                    // Use exponential backoff algorithm
                    val backoffTime = calculateBackoff(attempt)
                    logger.info("Waiting {}ms before retry...", backoffTime)
                    delay(backoffTime)

                    // Clear old request records and start fresh
                    clearOldRequests()
                } else {
                    // Not a rate limit error, throw immediately
                    throw e
                }
            } catch (e: Exception) {
                // Other exceptions, throw immediately
                throw e
            }
        }

        // All retries failed
        throw lastException ?: Exception("Request failed after maximum retries")
    }

    /**
     * Wait until a request slot is available
     */
    private suspend fun waitForAvailableSlot() {
        mutex.withLock {
            val now = System.currentTimeMillis()

            // Remove records older than 1 second
            requestTimes.removeIf { it < now - 1000 }

            // If request limit reached within current second, wait
            if (requestTimes.size >= maxRequestsPerSecond) {
                val oldestRequest = requestTimes.first()
                val waitTime = 1000 - (now - oldestRequest)

                if (waitTime > 0) {
                    logger.debug(
                        "Rate limit reached ({}/{}), waiting {}ms",
                        requestTimes.size,
                        maxRequestsPerSecond,
                        waitTime,
                    )
                    delay(waitTime)

                    // Clean old records again
                    val newNow = System.currentTimeMillis()
                    requestTimes.removeIf { it < newNow - 1000 }
                }
            }
        }
    }

    /**
     * Record request time
     */
    private suspend fun recordRequest() {
        mutex.withLock {
            val now = System.currentTimeMillis()
            requestTimes.add(now)
            logger.trace(
                "Request recorded, current window requests: {}/{}",
                requestTimes.size,
                maxRequestsPerSecond,
            )
        }
    }

    /**
     * Clear old request records
     */
    private suspend fun clearOldRequests() {
        mutex.withLock {
            val now = System.currentTimeMillis()
            requestTimes.removeIf { it < now - 1000 }
            logger.debug("Cleared old request records, current window requests: {}", requestTimes.size)
        }
    }

    /**
     * Calculate exponential backoff time
     *
     * @param attempt Current retry attempt (starting from 0)
     * @return Milliseconds to wait
     */
    private fun calculateBackoff(attempt: Int): Long {
        // Exponential backoff: initialBackoff * 2^attempt + random jitter
        val exponentialBackoff = initialBackoffMs * (1 shl attempt)
        val jitter = (Math.random() * 500).toLong() // 0-500ms random jitter
        return exponentialBackoff + jitter
    }

    /**
     * Get current request count in the window (for monitoring)
     */
    suspend fun getCurrentRequestCount(): Int {
        mutex.withLock {
            val now = System.currentTimeMillis()
            requestTimes.removeIf { it < now - 1000 }
            return requestTimes.size
        }
    }
}

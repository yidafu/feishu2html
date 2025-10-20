package dev.yidafu.feishu2html.api

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay

private val logger = KotlinLogging.logger {}

/**
 * Execute a suspend block with exponential backoff retry logic
 *
 * @param maxRetries Maximum number of retry attempts (including the first attempt)
 * @param initialDelay Initial delay in milliseconds before first retry
 * @param maxDelay Maximum delay in milliseconds between retries
 * @param factor Multiplier for exponential backoff (default 2.0)
 * @param retryOn Predicate to determine if an exception should trigger a retry
 * @param block The suspend function to execute
 * @return Result of the block execution
 * @throws The last exception if all retries are exhausted
 */
suspend fun <T> withRetry(
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10000,
    factor: Double = 2.0,
    retryOn: (Throwable) -> Boolean = { true },
    block: suspend () -> T,
): T {
    require(maxRetries > 0) { "maxRetries must be positive" }
    require(initialDelay > 0) { "initialDelay must be positive" }
    require(factor > 1.0) { "factor must be greater than 1.0" }

    var currentDelay = initialDelay
    var lastException: Throwable? = null

    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: Throwable) {
            lastException = e

            // Don't retry if predicate says no
            if (!retryOn(e)) {
                logger.warn { "Exception not retriable, throwing immediately: ${e.message}" }
                throw e
            }

            // Don't retry on last attempt
            if (attempt == maxRetries - 1) {
                logger.error { "Max retries ($maxRetries) exhausted, giving up" }
                throw e
            }

            logger.warn { "Attempt ${attempt + 1}/$maxRetries failed, retrying in ${currentDelay}ms: ${e.message}" }
            delay(currentDelay)

            // Calculate next delay with exponential backoff
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
    }

    // This should never be reached due to the throw in the last iteration
    throw lastException ?: IllegalStateException("Unexpected state in retry logic")
}

/**
 * Determines if an exception is retriable based on its type
 */
fun isRetriableException(exception: Throwable): Boolean {
    return when (exception) {
        is FeishuApiException.NetworkError -> true
        is FeishuApiException.RateLimitError -> true
        is FeishuApiException.AuthenticationError -> false  // Auth errors won't fix themselves
        is FeishuApiException.InsufficientPermission -> false  // Permission errors are permanent
        is FeishuApiException.DocumentNotFound -> false  // Document won't appear on retry
        is FeishuApiException.ApiError -> exception.code in listOf(500, 502, 503, 504)  // Server errors
        else -> false  // Unknown exceptions are not retriable by default
    }
}


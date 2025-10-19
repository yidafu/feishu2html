package dev.yidafu.feishu2html.api

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

/**
 * 请求速率限制器
 *
 * 飞书API限制：单个应用调用频率上限为每秒 5 次
 * 超过该频率限制，接口将返回 HTTP 状态码 400 及错误码 99991400
 */
class RateLimiter(
    private val maxRequestsPerSecond: Int = 5,
    private val maxRetries: Int = 3,
    private val initialBackoffMs: Long = 1000,
) {
    private val logger = LoggerFactory.getLogger(RateLimiter::class.java)
    private val mutex = Mutex()
    private val requestTimes = mutableListOf<Long>()

    /**
     * 执行带限流控制的请求
     *
     * @param block 要执行的请求操作
     * @return 请求结果
     * @throws Exception 如果所有重试都失败
     */
    suspend fun <T> execute(block: suspend () -> T): T {
        var lastException: Exception? = null

        for (attempt in 0 until maxRetries) {
            try {
                // 等待直到可以发送请求
                waitForAvailableSlot()

                // 执行请求
                val result = block()

                // 请求成功，记录时间
                recordRequest()

                return result
            } catch (e: FeishuApiException) {
                // 检查是否是限频错误
                if (e.code == 99991400) {
                    logger.warn("遇到限频错误 (99991400)，尝试第 ${attempt + 1}/$maxRetries 次重试")
                    lastException = e

                    // 使用指数退避算法
                    val backoffTime = calculateBackoff(attempt)
                    logger.info("等待 ${backoffTime}ms 后重试...")
                    delay(backoffTime)

                    // 清理旧的请求记录，重新开始
                    clearOldRequests()
                } else {
                    // 不是限频错误，直接抛出
                    throw e
                }
            } catch (e: Exception) {
                // 其他异常，直接抛出
                throw e
            }
        }

        // 所有重试都失败了
        throw lastException ?: Exception("请求失败，已达到最大重试次数")
    }

    /**
     * 等待直到有可用的请求槽位
     */
    private suspend fun waitForAvailableSlot() {
        mutex.withLock {
            val now = System.currentTimeMillis()

            // 移除1秒之前的请求记录
            requestTimes.removeIf { it < now - 1000 }

            // 如果当前1秒内的请求数已达上限，等待
            if (requestTimes.size >= maxRequestsPerSecond) {
                val oldestRequest = requestTimes.first()
                val waitTime = 1000 - (now - oldestRequest)

                if (waitTime > 0) {
                    logger.debug("已达到速率限制 (${requestTimes.size}/$maxRequestsPerSecond)，等待 ${waitTime}ms")
                    delay(waitTime)

                    // 重新清理旧记录
                    val newNow = System.currentTimeMillis()
                    requestTimes.removeIf { it < newNow - 1000 }
                }
            }
        }
    }

    /**
     * 记录请求时间
     */
    private suspend fun recordRequest() {
        mutex.withLock {
            val now = System.currentTimeMillis()
            requestTimes.add(now)
            logger.trace("记录请求，当前窗口内请求数: ${requestTimes.size}/$maxRequestsPerSecond")
        }
    }

    /**
     * 清理旧的请求记录
     */
    private suspend fun clearOldRequests() {
        mutex.withLock {
            val now = System.currentTimeMillis()
            requestTimes.removeIf { it < now - 1000 }
            logger.debug("清理旧请求记录，当前窗口内请求数: ${requestTimes.size}")
        }
    }

    /**
     * 计算指数退避时间
     *
     * @param attempt 当前重试次数（从0开始）
     * @return 需要等待的毫秒数
     */
    private fun calculateBackoff(attempt: Int): Long {
        // 指数退避：initialBackoff * 2^attempt + 随机抖动
        val exponentialBackoff = initialBackoffMs * (1 shl attempt)
        val jitter = (Math.random() * 500).toLong() // 0-500ms 的随机抖动
        return exponentialBackoff + jitter
    }

    /**
     * 获取当前窗口内的请求数（用于监控）
     */
    suspend fun getCurrentRequestCount(): Int {
        mutex.withLock {
            val now = System.currentTimeMillis()
            requestTimes.removeIf { it < now - 1000 }
            return requestTimes.size
        }
    }
}

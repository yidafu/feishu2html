package dev.yidafu.feishu2html.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.time.Instant

/**
 * Feishu authentication service for managing tenant_access_token acquisition and refresh
 */
internal class FeishuAuthService(
    private val appId: String,
    private val appSecret: String,
    private val httpClient: HttpClient,
) {
    private val logger = LoggerFactory.getLogger(FeishuAuthService::class.java)
    private val mutex = Mutex()

    @Volatile
    private var cachedToken: TokenCache? = null

    /**
     * Get a valid tenant_access_token
     */
    suspend fun getAccessToken(): String =
        mutex.withLock {
            logger.debug("Checking access token validity")
            val cached = cachedToken
            if (cached != null && cached.isValid()) {
                logger.debug("Using cached access token (expires at: {})", cached.expiresAt)
                return@withLock cached.token
            }

            logger.info("Requesting new tenant_access_token")
            logger.debug("App ID: {}", appId)

            try {
                val response =
                    httpClient.post("https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal") {
                        contentType(ContentType.Application.Json)
                        setBody(TenantAccessTokenRequest(appId, appSecret))
                    }

                val result: TenantAccessTokenResponse = response.body()
                logger.debug("Token request response - code: {}, msg: {}", result.code, result.msg)

                if (result.code != 0) {
                    logger.error("Failed to get access token - code: {}, msg: {}", result.code, result.msg)
                    throw FeishuApiException("Failed to get token: ${result.msg}")
                }

                val token =
                    result.tenantAccessToken
                        ?: throw FeishuApiException("Returned token is empty")

                val expiresAt = Instant.now().plusSeconds(result.expire.toLong() - 60)
                cachedToken =
                    TokenCache(
                        token = token,
                        expiresAt = expiresAt, // Expire 60 seconds early
                    )

                logger.info("Successfully obtained new access token (expires at: {}, valid for {} seconds)",
                    expiresAt, result.expire - 60)
                token
            } catch (e: Exception) {
                logger.error("Failed to request access token: {}", e.message, e)
                throw e
            }
        }

    private data class TokenCache(
        val token: String,
        val expiresAt: Instant,
    ) {
        fun isValid(): Boolean = Instant.now().isBefore(expiresAt)
    }
}

@Serializable
private data class TenantAccessTokenRequest(
    @SerialName("app_id") val appId: String,
    @SerialName("app_secret") val appSecret: String,
)

@Serializable
private data class TenantAccessTokenResponse(
    val code: Int,
    val msg: String,
    @SerialName("tenant_access_token") val tenantAccessToken: String? = null,
    val expire: Int = 7200,
)

class FeishuApiException(
    message: String,
    val code: Int? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

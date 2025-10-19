package dev.yidafu.feishu2html.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.serialization.json.Json

class FeishuAuthServiceTest : FunSpec({

    test("应该成功获取access token") {
        val mockEngine =
            MockEngine { request ->
                when (request.url.toString()) {
                    "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal" -> {
                        respond(
                            content =
                                ByteReadChannel(
                                    """{"code":0,"msg":"success","tenant_access_token":"test_token_123","expire":7200}""",
                                ),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                    else -> error("Unhandled ${request.url}")
                }
            }

        val httpClient =
            HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        },
                    )
                }
            }

        val authService = FeishuAuthService("test_app_id", "test_app_secret", httpClient)
        val token = authService.getAccessToken()

        token shouldBe "test_token_123"

        httpClient.close()
    }

    test("应该缓存access token") {
        var requestCount = 0
        val mockEngine =
            MockEngine { request ->
                requestCount++
                respond(
                    content = ByteReadChannel("""{"code":0,"msg":"success","tenant_access_token":"cached_token","expire":7200}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

        val httpClient =
            HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        },
                    )
                }
            }

        val authService = FeishuAuthService("test_app_id", "test_app_secret", httpClient)

        // 第一次请求
        val token1 = authService.getAccessToken()
        // 第二次请求（应该使用缓存）
        val token2 = authService.getAccessToken()

        token1 shouldBe token2
        requestCount shouldBe 1 // 只应该发送一次HTTP请求

        httpClient.close()
    }

    test("应该在token过期后刷新") {
        var requestCount = 0
        val mockEngine =
            MockEngine { request ->
                requestCount++
                val tokenSuffix = if (requestCount == 1) "_first" else "_second"
                respond(
                    content = ByteReadChannel("""{"code":0,"msg":"success","tenant_access_token":"token$tokenSuffix","expire":1}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

        val httpClient =
            HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        },
                    )
                }
            }

        val authService = FeishuAuthService("test_app_id", "test_app_secret", httpClient)

        // 第一次请求
        val token1 = authService.getAccessToken()
        token1 shouldBe "token_first"

        // 等待token过期（1秒 - 60秒提前过期时间已经是负数，立即过期）
        delay(100)

        // 第二次请求（应该刷新token）
        val token2 = authService.getAccessToken()
        token2 shouldBe "token_second"

        requestCount shouldBe 2

        httpClient.close()
    }

    test("应该在获取token失败时抛出异常") {
        val mockEngine =
            MockEngine { request ->
                respond(
                    content = ByteReadChannel("""{"code":10013,"msg":"invalid app_id or app_secret"}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

        val httpClient =
            HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        },
                    )
                }
            }

        val authService = FeishuAuthService("bad_app_id", "bad_secret", httpClient)

        val exception =
            shouldThrow<FeishuApiException> {
                authService.getAccessToken()
            }

        exception.message shouldContain "获取token失败"

        httpClient.close()
    }

    test("应该在返回的token为空时抛出异常") {
        val mockEngine =
            MockEngine { request ->
                respond(
                    content = ByteReadChannel("""{"code":0,"msg":"success","tenant_access_token":null}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

        val httpClient =
            HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        },
                    )
                }
            }

        val authService = FeishuAuthService("test_app_id", "test_app_secret", httpClient)

        val exception =
            shouldThrow<FeishuApiException> {
                authService.getAccessToken()
            }

        exception.message shouldContain "token为空"

        httpClient.close()
    }

    test("并发请求应该共享同一个token") {
        var requestCount = 0
        val mockEngine =
            MockEngine { request ->
                requestCount++
                respond(
                    content = ByteReadChannel("""{"code":0,"msg":"success","tenant_access_token":"shared_token","expire":7200}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

        val httpClient =
            HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        },
                    )
                }
            }

        val authService = FeishuAuthService("test_app_id", "test_app_secret", httpClient)

        // 并发请求10次
        val tokens =
            List(10) {
                async {
                    authService.getAccessToken()
                }
            }.awaitAll()

        // 所有token应该相同
        tokens.forEach { it shouldBe "shared_token" }
        // 只应该发送一次HTTP请求（mutex保护）
        requestCount shouldBe 1

        httpClient.close()
    }
})

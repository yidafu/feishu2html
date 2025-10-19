package dev.yidafu.feishu2html.platform

import io.ktor.client.*
import io.ktor.client.engine.curl.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Desktop Native (Linux/Windows) implementation using Curl engine
 */
actual fun createHttpClient(): HttpClient =
    HttpClient(Curl) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                },
            )
        }
    }


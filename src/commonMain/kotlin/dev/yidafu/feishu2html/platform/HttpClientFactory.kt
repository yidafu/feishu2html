package dev.yidafu.feishu2html.platform

import io.ktor.client.*

/**
 * Platform-specific HTTP client factory
 */
expect fun createHttpClient(): HttpClient


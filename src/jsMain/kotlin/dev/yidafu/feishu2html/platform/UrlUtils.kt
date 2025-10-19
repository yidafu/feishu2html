package dev.yidafu.feishu2html.platform

/**
 * JS implementation of URL decoding
 */
actual fun decodeUrl(url: String): String {
    return try {
        js("decodeURIComponent")(url) as String
    } catch (e: Exception) {
        url
    }
}


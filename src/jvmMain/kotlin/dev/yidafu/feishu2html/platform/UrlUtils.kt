package dev.yidafu.feishu2html.platform

import java.net.URLDecoder

/**
 * JVM implementation of URL decoding
 */
actual fun decodeUrl(url: String): String {
    return URLDecoder.decode(url, "UTF-8")
}

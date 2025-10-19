package dev.yidafu.feishu2html.platform

/**
 * Native implementation of URL decoding
 * Simple implementation - proper URL decoding would require more work
 */
actual fun decodeUrl(url: String): String {
    // Basic URL decoding for Native platforms
    return url.replace("+", " ")
        .replace("%20", " ")
        .replace("%2F", "/")
        .replace("%3A", ":")
        .replace("%3F", "?")
        .replace("%3D", "=")
        .replace("%26", "&")
        // Add more as needed
}


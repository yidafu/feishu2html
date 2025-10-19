package dev.yidafu.feishu2html.platform

import java.io.File
import java.util.Base64

/**
 * JVM implementation of ImageEncoder using java.util.Base64
 */
actual object ImageEncoder {
    actual fun encodeToBase64DataUrl(filePath: String): String {
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("Image file not found: $filePath")
        }

        val bytes = file.readBytes()
        val base64 = Base64.getEncoder().encodeToString(bytes)
        val mimeType = getMimeType(filePath)

        return "data:$mimeType;base64,$base64"
    }

    actual fun getMimeType(filePath: String): String {
        return when (filePath.substringAfterLast('.', "").lowercase()) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "svg" -> "image/svg+xml"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "ico" -> "image/x-icon"
            else -> "image/png" // default to PNG
        }
    }
}


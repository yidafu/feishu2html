package dev.yidafu.feishu2html.platform

import kotlinx.cinterop.*
import platform.posix.*

/**
 * Windows (MinGW) implementation of ImageEncoder using POSIX APIs
 */
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual object ImageEncoder {
    actual fun encodeToBase64DataUrl(filePath: String): String {
        val file = fopen(filePath, "rb")
            ?: throw IllegalArgumentException("Image file not found: $filePath")

        try {
            // Get file size
            fseek(file, 0, SEEK_END)
            val fileSize = ftell(file)
            fseek(file, 0, SEEK_SET)

            // Read file content
            val buffer = ByteArray(fileSize.toInt())
            buffer.usePinned { pinned ->
                fread(pinned.addressOf(0), 1.toULong(), fileSize.toULong(), file)
            }

            // Encode to base64
            val base64 = buffer.encodeBase64()
            val mimeType = getMimeType(filePath)

            return "data:$mimeType;base64,$base64"
        } finally {
            fclose(file)
        }
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

    /**
     * Simple base64 encoding implementation
     */
    private fun ByteArray.encodeBase64(): String {
        val base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val output = StringBuilder()

        var i = 0
        while (i < size) {
            val b1 = this[i++].toInt() and 0xFF
            val b2 = if (i < size) this[i++].toInt() and 0xFF else 0
            val b3 = if (i < size) this[i++].toInt() and 0xFF else 0

            val n = (b1 shl 16) or (b2 shl 8) or b3

            output.append(base64Chars[(n shr 18) and 0x3F])
            output.append(base64Chars[(n shr 12) and 0x3F])
            output.append(if (i - 2 < size) base64Chars[(n shr 6) and 0x3F] else '=')
            output.append(if (i - 1 < size) base64Chars[n and 0x3F] else '=')
        }

        return output.toString()
    }
}


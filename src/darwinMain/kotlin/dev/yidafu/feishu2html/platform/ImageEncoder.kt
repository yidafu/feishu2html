package dev.yidafu.feishu2html.platform

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.posix.memcpy

/**
 * Darwin (macOS/iOS) implementation of ImageEncoder using Foundation framework
 */
@OptIn(ExperimentalForeignApi::class)
actual object ImageEncoder {
    actual fun encodeToBase64DataUrl(filePath: String): String {
        val fileUrl = NSURL.fileURLWithPath(filePath)
        val data = NSData.dataWithContentsOfURL(fileUrl)
            ?: throw IllegalArgumentException("Image file not found or cannot be read: $filePath")

        val base64 = data.base64EncodedStringWithOptions(0u)
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


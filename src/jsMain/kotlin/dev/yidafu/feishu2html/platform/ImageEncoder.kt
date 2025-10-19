package dev.yidafu.feishu2html.platform

/**
 * External declarations for Node.js fs module
 */
@JsModule("fs")
@JsNonModule
external object fs {
    fun readFileSync(path: String): Buffer
    fun existsSync(path: String): Boolean
}

/**
 * External declaration for Node.js Buffer
 */
external class Buffer {
    fun toString(encoding: String): String

    companion object {
        fun from(data: dynamic): Buffer
    }
}

/**
 * JS/Node.js implementation of ImageEncoder using Buffer
 */
actual object ImageEncoder {
    actual fun encodeToBase64DataUrl(filePath: String): String {
        if (!fs.existsSync(filePath)) {
            throw IllegalArgumentException("Image file not found: $filePath")
        }

        val buffer = fs.readFileSync(filePath)
        val base64 = buffer.toString("base64")
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


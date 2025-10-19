package dev.yidafu.feishu2html.platform

import java.io.File

/**
 * JVM implementation of file system operations
 */
actual class PlatformFileSystem {
    actual fun createDirectories(path: String) {
        File(path).mkdirs()
    }

    actual fun exists(path: String): Boolean {
        return File(path).exists()
    }

    actual fun writeText(path: String, content: String) {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    actual fun writeBytes(path: String, content: ByteArray) {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeBytes(content)
    }
}

actual fun getPlatformFileSystem(): PlatformFileSystem = PlatformFileSystem()


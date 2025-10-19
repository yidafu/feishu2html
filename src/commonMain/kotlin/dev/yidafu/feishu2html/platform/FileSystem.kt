package dev.yidafu.feishu2html.platform

/**
 * Platform-specific file system operations
 */
expect class PlatformFileSystem() {
    fun createDirectories(path: String)
    fun exists(path: String): Boolean
    fun writeText(path: String, content: String)
    fun writeBytes(path: String, content: ByteArray)
}

expect fun getPlatformFileSystem(): PlatformFileSystem


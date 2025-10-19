package dev.yidafu.feishu2html.platform

import kotlinx.cinterop.*
import platform.posix.*

/**
 * Native implementation of file system operations
 */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class PlatformFileSystem {
    actual fun createDirectories(path: String) {
        // TODO: Implement Native file system operations
        platform.posix.mkdir(path, 0x1FFu) // 0777 permissions
    }

    actual fun exists(path: String): Boolean {
        // TODO: Implement Native file system operations
        return platform.posix.access(path, platform.posix.F_OK) == 0
    }

    actual fun writeText(path: String, content: String) {
        writeBytes(path, content.encodeToByteArray())
    }

    actual fun writeBytes(path: String, content: ByteArray) {
        val file = platform.posix.fopen(path, "wb")
        if (file != null) {
            try {
                content.usePinned { pinned ->
                    platform.posix.fwrite(pinned.addressOf(0), 1.toULong(), content.size.toULong(), file)
                }
            } finally {
                platform.posix.fclose(file)
            }
        }
    }
}

actual fun getPlatformFileSystem(): PlatformFileSystem = PlatformFileSystem()


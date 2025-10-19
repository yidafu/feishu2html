package dev.yidafu.feishu2html.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.F_OK
import platform.posix.access
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import platform.posix.mkdir

/**
 * Linux implementation of file system operations using POSIX APIs
 */
@OptIn(ExperimentalForeignApi::class)
actual class PlatformFileSystem {
    actual fun createDirectories(path: String) {
        // POSIX mkdir with mode parameter
        mkdir(path, 0x1FFu) // 0777 permissions (octal)
    }

    actual fun exists(path: String): Boolean {
        return access(path, F_OK) == 0
    }

    actual fun writeText(
        path: String,
        content: String,
    ) {
        writeBytes(path, content.encodeToByteArray())
    }

    actual fun writeBytes(
        path: String,
        content: ByteArray,
    ) {
        val file = fopen(path, "wb")
        if (file != null) {
            try {
                content.usePinned { pinned ->
                    fwrite(pinned.addressOf(0), 1.toULong(), content.size.toULong(), file)
                }
            } finally {
                fclose(file)
            }
        }
    }
}

actual fun getPlatformFileSystem(): PlatformFileSystem = PlatformFileSystem()

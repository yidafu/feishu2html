package dev.yidafu.feishu2html.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import platform.windows.CreateDirectoryA
import platform.windows.GetFileAttributesA
import platform.windows.INVALID_FILE_ATTRIBUTES

/**
 * Windows (MinGW) implementation of file system operations using Windows API
 */
@OptIn(ExperimentalForeignApi::class)
actual class PlatformFileSystem {
    actual fun createDirectories(path: String) {
        // Windows CreateDirectory API (does not create parent directories)
        CreateDirectoryA(path, null)
    }

    actual fun exists(path: String): Boolean {
        val attrs = GetFileAttributesA(path)
        return attrs != INVALID_FILE_ATTRIBUTES
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

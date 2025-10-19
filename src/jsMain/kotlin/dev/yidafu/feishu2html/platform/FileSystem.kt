package dev.yidafu.feishu2html.platform

/**
 * JS (Node.js) implementation of file system operations
 * Uses Node.js fs module
 */
actual class PlatformFileSystem {
    actual fun createDirectories(path: String) {
        val fs = js("require('fs')")
        val pathModule = js("require('path')")

        // Create directories recursively
        try {
            fs.mkdirSync(path, js("{ recursive: true }"))
        } catch (e: dynamic) {
            // Directory might already exist, ignore
        }
    }

    actual fun exists(path: String): Boolean {
        val fs = js("require('fs')")
        return try {
            fs.existsSync(path) as Boolean
        } catch (e: dynamic) {
            false
        }
    }

    actual fun writeText(path: String, content: String) {
        val fs = js("require('fs')")
        val pathModule = js("require('path')")

        // Ensure parent directory exists
        val dirname = pathModule.dirname(path)
        createDirectories(dirname as String)

        // Write file
        fs.writeFileSync(path, content, "utf8")
    }

    actual fun writeBytes(path: String, content: ByteArray) {
        val fs = js("require('fs')")
        val pathModule = js("require('path')")

        // Ensure parent directory exists
        val dirname = pathModule.dirname(path)
        createDirectories(dirname as String)

        // Convert ByteArray to Buffer
        val buffer = js("Buffer").from(content.toTypedArray())

        // Write file
        fs.writeFileSync(path, buffer)
    }
}

actual fun getPlatformFileSystem(): PlatformFileSystem = PlatformFileSystem()

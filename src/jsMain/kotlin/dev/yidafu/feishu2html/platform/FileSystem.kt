package dev.yidafu.feishu2html.platform

/**
 * Node.js fs module for ES Module
 */
@JsModule("fs")
@JsNonModule
external object NodeFs {
    fun mkdirSync(
        path: String,
        options: dynamic,
    ): dynamic

    fun existsSync(path: String): Boolean

    fun writeFileSync(
        path: String,
        data: dynamic,
        encoding: String? = definedExternally,
    ): Unit
}

/**
 * Node.js path module for ES Module
 */
@JsModule("path")
@JsNonModule
external object NodePath {
    fun dirname(path: String): String
}

/**
 * Node.js Buffer for ES Module
 */
external class NodeBuffer {
    companion object {
        fun from(array: Array<Byte>): dynamic
    }
}

/**
 * JS (Node.js) implementation of file system operations
 * Uses Node.js fs and path modules via ES Module imports
 */
actual class PlatformFileSystem {
    actual fun createDirectories(path: String) {
        // Create directories recursively
        try {
            NodeFs.mkdirSync(path, js("{ recursive: true }"))
        } catch (e: dynamic) {
            // Directory might already exist, ignore
        }
    }

    actual fun exists(path: String): Boolean {
        return try {
            NodeFs.existsSync(path)
        } catch (e: dynamic) {
            false
        }
    }

    actual fun writeText(
        path: String,
        content: String,
    ) {
        // Ensure parent directory exists
        val dirname = NodePath.dirname(path)
        createDirectories(dirname)

        // Write file
        NodeFs.writeFileSync(path, content, "utf8")
    }

    actual fun writeBytes(
        path: String,
        content: ByteArray,
    ) {
        // Ensure parent directory exists
        val dirname = NodePath.dirname(path)
        createDirectories(dirname)

        // Convert ByteArray to Buffer
        val buffer = NodeBuffer.from(content.toTypedArray())

        // Write file
        NodeFs.writeFileSync(path, buffer)
    }
}

actual fun getPlatformFileSystem(): PlatformFileSystem = PlatformFileSystem()

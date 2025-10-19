package dev.yidafu.feishu2html.platform

/**
 * Platform-specific image encoding utilities
 *
 * Provides base64 encoding for images to enable embedding them as data URLs in HTML.
 */
expect object ImageEncoder {
    /**
     * Read an image file and convert it to a base64 data URL
     *
     * @param filePath Path to the image file
     * @return base64 data URL (e.g., "data:image/png;base64,iVBORw0KGg...")
     */
    fun encodeToBase64DataUrl(filePath: String): String

    /**
     * Detect MIME type from file extension
     *
     * @param filePath Path to the image file
     * @return MIME type (e.g., "image/png", "image/jpeg")
     */
    fun getMimeType(filePath: String): String
}


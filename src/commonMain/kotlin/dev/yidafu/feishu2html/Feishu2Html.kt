package dev.yidafu.feishu2html

import dev.yidafu.feishu2html.api.FeishuApiClient
import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.HtmlBuilder
import dev.yidafu.feishu2html.converter.CssMode
import dev.yidafu.feishu2html.converter.HtmlTemplate
import dev.yidafu.feishu2html.platform.getPlatformFileSystem
import dev.yidafu.feishu2html.platform.ImageEncoder
import dev.yidafu.feishu2html.converter.EmbeddedResources
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.html.*

private val logger = KotlinLogging.logger {}

/**
 * Progress callback interface for monitoring export operations
 */
interface ExportProgressCallback {
    /**
     * Called when export starts for a document
     */
    fun onStart(documentId: String) {}

    /**
     * Called when document metadata has been fetched
     */
    fun onMetadataFetched(documentId: String, title: String, blocksCount: Int) {}

    /**
     * Called when content blocks have been fetched
     */
    fun onContentFetched(documentId: String, blocksCount: Int) {}

    /**
     * Called during asset downloads with progress information
     */
    fun onAssetDownloading(documentId: String, current: Int, total: Int) {}

    /**
     * Called when export completes successfully
     */
    fun onComplete(documentId: String, outputPath: String) {}

    /**
     * Called when an error occurs during export
     */
    fun onError(documentId: String, error: Throwable) {}
}

/**
 * Create HtmlTemplate based on TemplateMode
 *
 * This function creates predefined templates for CLI usage:
 * - DEFAULT: Uses the standard Feishu template
 * - FRAGMENT: Uses a minimal fragment template with simple wrapper
 * - FULL: Uses a minimal full template with basic HTML structure
 */
private fun createHtmlTemplate(mode: TemplateMode): HtmlTemplate {
    return when (mode) {
        TemplateMode.DEFAULT -> HtmlTemplate.Default

        TemplateMode.FRAGMENT -> HtmlTemplate.Fragment { content ->
            // Simple fragment template: just a wrapper div
            div(classes = "feishu-document") {
                content()
            }
        }

        TemplateMode.FULL -> HtmlTemplate.Full { content ->
            // Minimal full template with basic structure
            lang = "zh-CN"
            head {
                meta(charset = "UTF-8")
                meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                title("Feishu Document")
                style {
                    unsafe {
                        raw("""
                            body {
                                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
                                line-height: 1.6;
                                max-width: 900px;
                                margin: 0 auto;
                                padding: 20px;
                            }
                        """.trimIndent())
                    }
                }
            }
            body {
                content()
            }
        }
    }
}

/**
 * Main class for converting Feishu documents to HTML
 *
 * Provides functionality to export Feishu documents to standalone HTML files with automatic
 * download of images and attachments. Implements AutoCloseable for automatic resource management.
 *
 * ## Usage Example
 *
 * ```kotlin
 * val options = Feishu2HtmlOptions(
 *     appId = "your_app_id",
 *     appSecret = "your_app_secret",
 *     outputDir = "./output"
 * )
 *
 * Feishu2Html(options).use { converter ->
 *     converter.export("document_id")
 * }
 * ```
 *
 * @property options Configuration options including app credentials and output paths
 * @see Feishu2HtmlOptions
 */
class Feishu2Html
@PublishedApi
internal constructor(
    private val options: Feishu2HtmlOptions,
    @PublishedApi internal val apiClient: FeishuApiClient,
    @PublishedApi internal val fileSystem: dev.yidafu.feishu2html.platform.PlatformFileSystem,
) : AutoCloseable {
    // Cache for base64 encoded images (token -> base64 data URL)
    private val imageBase64Cache = mutableMapOf<String, String>()

    /**
     * Public constructor for creating Feishu2Html with default dependencies
     */
    constructor(options: Feishu2HtmlOptions) : this(
        options = options,
        apiClient = FeishuApiClient(options.appId, options.appSecret),
        fileSystem = getPlatformFileSystem()
    )

    /**
     * Export a single Feishu document to HTML file
     *
     * This method will:
     * 1. Fetch document metadata (title, version, etc.)
     * 2. Fetch all document blocks
     * 3. Download images and attachments
     * 4. Generate and save HTML file
     *
     * @param documentId Feishu document ID, can be extracted from document URL
     * @param outputFileName Optional output filename, defaults to document title
     * @param progressCallback Optional callback to monitor export progress
     * @throws FeishuApiException When API call fails (e.g., insufficient permissions, document not found)
     * @throws kotlinx.io.IOException When file write operation fails
     *
     * @see exportBatch Batch export multiple documents
     */
    suspend fun export(
        documentId: String,
        outputFileName: String? = null,
        progressCallback: ExportProgressCallback? = null,
    ) {
        progressCallback?.onStart(documentId)
        logger.info { "Starting export for document: $documentId" }
        logger.debug {
            "Export options - outputDir: ${options.outputDir}, imageDir: ${options.imageDir}, fileDir: ${options.fileDir}"
        }

        try {
            // Fetch document metadata (title, cover, etc.)
            logger.debug { "Fetching document info for: $documentId" }
            val documentInfo = apiClient.getDocumentInfo(documentId)
            logger.info {
                "Document info retrieved - Title: ${documentInfo.title}, Version: ${documentInfo.revisionId}"
            }

            // Fetch document content
            logger.debug { "Fetching document content for: $documentId" }
            val content = apiClient.getDocumentRawContent(documentId)
            val document = content.document
            val blocks = content.blocks

            logger.info { "Document content loaded - Total blocks: ${blocks.size}" }
            progressCallback?.onContentFetched(documentId, blocks.size)

            // Get ordered block list
            val orderedBlocks = apiClient.getOrderedBlocks(content)
            logger.debug { "Ordered blocks count: ${orderedBlocks.size}" }

            // Download images and files
            logger.info { "Starting asset download for document: $documentId" }
            downloadAssets(orderedBlocks)

            // Copy CSS file if external mode enabled
            if (options.externalCss) {
                val cssContent = EmbeddedResources.FEISHU_STYLE_CSS

                val cssPath = "${options.outputDir}/${options.cssFileName}"
                fileSystem.writeText(cssPath, cssContent)
                logger.info { "CSS file written: $cssPath (${cssContent.length} bytes)" }
            }

            // Generate HTML
            val fileName = outputFileName ?: "${document.title}.html"
            val htmlPath = "${options.outputDir}/$fileName"
            logger.debug { "Output file path: $htmlPath" }

            logger.info { "Building HTML for document: ${document.title}" }
            val template = createHtmlTemplate(options.templateMode)
            val htmlBuilder =
                HtmlBuilder(
                    title = document.title,
                    cssMode = if (options.externalCss) CssMode.EXTERNAL else CssMode.INLINE,
                    cssFileName = options.cssFileName,
                    customCss = options.customCss,
                    template = template,
                    imageBase64Cache = imageBase64Cache,
                    showUnsupportedBlocks = options.showUnsupportedBlocks,
                )
            val html = htmlBuilder.build(orderedBlocks, blocks)

            fileSystem.writeText(htmlPath, html)
            logger.info { "Document export completed successfully - File: $htmlPath" }
            progressCallback?.onComplete(documentId, htmlPath)
        } catch (e: Exception) {
            logger.error(e) { "Failed to export document $documentId: ${e.message}" }
            progressCallback?.onError(documentId, e)
            throw e
        }
    }

    /**
     * Batch export multiple Feishu documents
     *
     * Exports each document in the list sequentially. If a document fails to export,
     * the error is logged but processing continues with subsequent documents.
     *
     * @param documentIds List of document IDs to export
     * @param progressCallback Optional callback to monitor batch export progress
     *
     * @see export Export a single document
     */
    suspend fun exportBatch(
        documentIds: List<String>,
        progressCallback: ExportProgressCallback? = null,
    ) =
        coroutineScope {
            logger.info { "Starting batch export for ${documentIds.size} documents" }
            logger.debug { "Document IDs: ${documentIds.joinToString(", ")}" }

            var successCount = 0
            var failureCount = 0

            documentIds.forEachIndexed { index, documentId ->
                logger.info { "Processing document ${index + 1}/${documentIds.size}: $documentId" }
                try {
                    export(documentId, progressCallback = progressCallback)
                    successCount++
                    logger.debug { "Document ${index + 1}/${documentIds.size} exported successfully" }
                } catch (e: Exception) {
                    failureCount++
                    logger.error(e) {
                        "Failed to export document ${index + 1}/${documentIds.size} ($documentId): ${e.message}"
                    }
                }
            }

            logger.info {
                "Batch export completed - Success: $successCount, Failed: $failureCount, Total: ${documentIds.size}"
            }
        }

    private suspend fun downloadAssets(blocks: List<Block>) =
        coroutineScope {
            logger.debug { "Scanning ${blocks.size} blocks for downloadable assets" }
            val imageJobs = mutableListOf<Deferred<Unit>>()
            val fileJobs = mutableListOf<Deferred<Unit>>()

            for (block in blocks) {
                when (block) {
                    is ImageBlock -> {
                        val token = block.image?.token
                        if (token != null) {
                            val job =
                                async {
                                    try {
                                        val imagePath = "${options.imageDir}/$token.png"
                                        if (!fileSystem.exists(imagePath)) {
                                            apiClient.downloadFile(token, imagePath)
                                            logger.info { "Image downloaded: $token" }
                                        } else {
                                            logger.debug { "Image already exists, skipping: $token" }
                                        }

                                        // Convert to base64 if inline images enabled
                                        if (options.inlineImages) {
                                            try {
                                                val base64 = ImageEncoder.encodeToBase64DataUrl(imagePath)
                                                imageBase64Cache[token] = base64
                                                logger.debug { "Image encoded to base64: $token" }
                                            } catch (e: Exception) {
                                                logger.error(e) { "Failed to encode image to base64: $token" }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        logger.error(e) { "Failed to download image: $token" }
                                    }
                                }
                            imageJobs.add(job)
                        }
                    }
                    is FileBlock -> {
                        val token = block.file?.token
                        val name = block.file?.name
                        if (token != null) {
                            val job =
                                async {
                                    try {
                                        val fileName = name ?: token
                                        val filePath = "${options.fileDir}/$fileName"
                                        if (!fileSystem.exists(filePath)) {
                                            apiClient.downloadFile(token, filePath)
                                            logger.info { "File downloaded: $fileName" }
                                        } else {
                                            logger.debug { "File already exists, skipping: $fileName" }
                                        }
                                    } catch (e: Exception) {
                                        logger.error(e) { "Failed to download file: $token" }
                                    }
                                }
                            fileJobs.add(job)
                        }
                    }
                    is BoardBlock -> {
                        // Board block
                        val token = block.board?.token
                        if (token != null) {
                            val job =
                                async {
                                    try {
                                        val imagePath = "${options.imageDir}/$token.png"
                                        if (!fileSystem.exists(imagePath)) {
                                            apiClient.exportBoard(token, imagePath)
                                            logger.info { "Board exported as image: $token" }
                                        } else {
                                            logger.debug { "Board image already exists, skipping: $token" }
                                        }

                                        // Convert to base64 if inline images enabled
                                        if (options.inlineImages) {
                                            try {
                                                val base64 = ImageEncoder.encodeToBase64DataUrl(imagePath)
                                                imageBase64Cache[token] = base64
                                                logger.debug { "Board image encoded to base64: $token" }
                                            } catch (e: Exception) {
                                                logger.error(e) { "Failed to encode board image to base64: $token" }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        logger.error(e) { "Failed to export board: $token" }
                                    }
                                }
                            imageJobs.add(job)
                        }
                    }
                    else -> {}
                }
            }

            // Wait for all downloads to complete
            logger.debug {
                "Waiting for ${imageJobs.size} image downloads and ${fileJobs.size} file downloads"
            }
            imageJobs.awaitAll()
            fileJobs.awaitAll()

            logger.info {
                "Asset download completed - Images: ${imageJobs.size}, Files: ${fileJobs.size}"
            }
        }

    /**
     * Close HTTP client and release resources
     *
     * Implements AutoCloseable interface and is automatically called at the end of use {} block.
     * Can also be called manually to immediately release resources.
     *
     * ## Example (recommended with use {})
     * ```kotlin
     * Feishu2Html(options).use { converter ->
     *     converter.export("doc_id")
     * }
     * ```
     *
     * ## Or manual management
     * ```kotlin
     * val converter = Feishu2Html(options)
     * try {
     *     converter.export("doc_id")
     * } finally {
     *     converter.close()
     * }
     * ```
     */
    override fun close() {
        logger.debug { "Closing Feishu2Html and releasing resources" }
        apiClient.close()
        logger.debug { "Feishu2Html closed successfully" }
    }
}

/**
 * HTML template mode for command-line interface
 */
enum class TemplateMode {
    /** Use the default built-in template */
    DEFAULT,

    /** Use a minimal fragment template (custom body structure) */
    FRAGMENT,

    /** Use a minimal full template (custom HTML structure) */
    FULL
}

/**
 * Configuration options for Feishu document to HTML conversion
 *
 * @property appId Feishu app ID, obtained from Feishu Open Platform
 * @property appSecret Feishu app secret, obtained from Feishu Open Platform
 * @property outputDir HTML file output directory, defaults to "./output"
 * @property imageDir Image save directory, defaults to "./output/images"
 * @property fileDir Attachment save directory, defaults to "./output/files"
 * @property imagePath Relative path for images in HTML, defaults to "images"
 * @property filePath Relative path for attachments in HTML, defaults to "files"
 * @property customCss Custom CSS styles, overrides default styles if provided
 * @property externalCss true = external CSS file, false = inline styles
 * @property cssFileName CSS filename when externalCss is true
 * @property templateMode HTML template mode (for CLI usage), defaults to DEFAULT
 * @property inlineImages true = embed images as base64 data URLs
 * @property showUnsupportedBlocks true = show unsupported block warnings (for debugging)
 * @property enableDebugLogging true = enable verbose debug logging
 * @property quietMode true = suppress all non-error logs
 *
 * @see Feishu2Html
 */
data class Feishu2HtmlOptions(
    val appId: String,
    val appSecret: String,
    val outputDir: String = "./output",
    val imageDir: String = "./output/images",
    val fileDir: String = "./output/files",
    val imagePath: String = "images",
    val filePath: String = "files",
    val customCss: String? = null,
    val externalCss: Boolean = true, // true = external file, false = inline
    val cssFileName: String = "feishu-style-optimized.css", // Use optimized CSS with official Feishu rules
    val templateMode: TemplateMode = TemplateMode.DEFAULT,
    val inlineImages: Boolean = false, // true = embed images as base64 data URLs
    val showUnsupportedBlocks: Boolean = true, // true = show unsupported block warnings (for debugging)
    val enableDebugLogging: Boolean = false, // true = enable verbose debug logging
    val quietMode: Boolean = false, // true = suppress all non-error logs
) {
    init {
        // Validate required parameters
        require(appId.isNotBlank()) { "appId cannot be blank" }
        require(appSecret.isNotBlank()) { "appSecret cannot be blank" }
        require(outputDir.isNotBlank()) { "outputDir cannot be blank" }
        require(imageDir.isNotBlank()) { "imageDir cannot be blank" }
        require(fileDir.isNotBlank()) { "fileDir cannot be blank" }
        require(imagePath.isNotBlank()) { "imagePath cannot be blank" }
        require(filePath.isNotBlank()) { "filePath cannot be blank" }
        require(cssFileName.isNotBlank()) { "cssFileName cannot be blank" }

        // Create output directories
        val fileSystem = getPlatformFileSystem()
        fileSystem.createDirectories(outputDir)
        fileSystem.createDirectories(imageDir)
        fileSystem.createDirectories(fileDir)
    }
}

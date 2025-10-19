package dev.yidafu.feishu2html

import dev.yidafu.feishu2html.api.FeishuApiClient
import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.HtmlBuilder
import dev.yidafu.feishu2html.converter.CssMode
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths

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
class Feishu2Html(
    private val options: Feishu2HtmlOptions,
) : AutoCloseable {
    private val logger = LoggerFactory.getLogger(Feishu2Html::class.java)
    private val apiClient = FeishuApiClient(options.appId, options.appSecret)

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
     * @throws FeishuApiException When API call fails (e.g., insufficient permissions, document not found)
     * @throws java.io.IOException When file write operation fails
     *
     * @see exportBatch Batch export multiple documents
     */
    suspend fun export(
        documentId: String,
        outputFileName: String? = null,
    ) {
        logger.info("Starting export for document: {}", documentId)
        logger.debug(
            "Export options - outputDir: {}, imageDir: {}, fileDir: {}",
            options.outputDir,
            options.imageDir,
            options.fileDir,
        )

        try {
            // Fetch document metadata (title, cover, etc.)
            logger.debug("Fetching document info for: {}", documentId)
            val documentInfo = apiClient.getDocumentInfo(documentId)
            logger.info(
                "Document info retrieved - Title: {}, Version: {}",
                documentInfo.title,
                documentInfo.revisionId,
            )

            // Fetch document content
            logger.debug("Fetching document content for: {}", documentId)
            val content = apiClient.getDocumentRawContent(documentId)
            val document = content.document
            val blocks = content.blocks

            logger.info("Document content loaded - Total blocks: {}", blocks.size)

            // Get ordered block list
            val orderedBlocks = apiClient.getOrderedBlocks(content)
            logger.debug("Ordered blocks count: {}", orderedBlocks.size)

            // Download images and files
            logger.info("Starting asset download for document: {}", documentId)
            downloadAssets(orderedBlocks)

            // Copy CSS file if external mode enabled
            if (options.externalCss) {
                val cssContent =
                    this::class.java.getResourceAsStream("/${options.cssFileName}")
                        ?.bufferedReader()?.readText()
                        ?: throw java.io.IOException("${options.cssFileName} not found in resources")

                val cssFile = File(options.outputDir, options.cssFileName)
                cssFile.writeText(cssContent)
                logger.info("CSS file written: {} ({} bytes)", cssFile.absolutePath, cssContent.length)
            }

            // Generate HTML
            val fileName = outputFileName ?: "${document.title}.html"
            val htmlFile = File(options.outputDir, fileName)
            htmlFile.parentFile?.mkdirs()
            logger.debug("Output file path: {}", htmlFile.absolutePath)

            logger.info("Building HTML for document: {}", document.title)
            val htmlBuilder =
                HtmlBuilder(
                    title = document.title,
                    cssMode = if (options.externalCss) CssMode.EXTERNAL else CssMode.INLINE,
                    cssFileName = options.cssFileName,
                    customCss = options.customCss,
                )
            val html = htmlBuilder.build(orderedBlocks, blocks)

            htmlFile.writeText(html)
            logger.info("Document export completed successfully - File: {}", htmlFile.absolutePath)
        } catch (e: Exception) {
            logger.error("Failed to export document {}: {}", documentId, e.message, e)
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
     *
     * @see export Export a single document
     */
    suspend fun exportBatch(documentIds: List<String>) =
        coroutineScope {
            logger.info("Starting batch export for {} documents", documentIds.size)
            logger.debug("Document IDs: {}", documentIds.joinToString(", "))

            var successCount = 0
            var failureCount = 0

            documentIds.forEachIndexed { index, documentId ->
                logger.info("Processing document {}/{}: {}", index + 1, documentIds.size, documentId)
                try {
                    export(documentId)
                    successCount++
                    logger.debug("Document {}/{} exported successfully", index + 1, documentIds.size)
                } catch (e: Exception) {
                    failureCount++
                    logger.error(
                        "Failed to export document {}/{} ({}): {}",
                        index + 1,
                        documentIds.size,
                        documentId,
                        e.message,
                        e,
                    )
                }
            }

            logger.info(
                "Batch export completed - Success: {}, Failed: {}, Total: {}",
                successCount,
                failureCount,
                documentIds.size,
            )
        }

    private suspend fun downloadAssets(blocks: List<Block>) =
        coroutineScope {
            logger.debug("Scanning {} blocks for downloadable assets", blocks.size)
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
                                        val imagePath = Paths.get(options.imageDir, "$token.png")
                                        if (!imagePath.toFile().exists()) {
                                            apiClient.downloadFile(token, imagePath)
                                            logger.info("Image downloaded: {}", token)
                                        } else {
                                            logger.debug("Image already exists, skipping: {}", token)
                                        }
                                    } catch (e: Exception) {
                                        logger.error("Failed to download image: {}", token, e)
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
                                        val filePath = Paths.get(options.fileDir, fileName)
                                        if (!filePath.toFile().exists()) {
                                            apiClient.downloadFile(token, filePath)
                                            logger.info("File downloaded: {}", fileName)
                                        } else {
                                            logger.debug("File already exists, skipping: {}", fileName)
                                        }
                                    } catch (e: Exception) {
                                        logger.error("Failed to download file: {}", token, e)
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
                                        val imagePath = Paths.get(options.imageDir, "$token.png")
                                        if (!imagePath.toFile().exists()) {
                                            apiClient.exportBoard(token, imagePath)
                                            logger.info("Board exported as image: {}", token)
                                        } else {
                                            logger.debug("Board image already exists, skipping: {}", token)
                                        }
                                    } catch (e: Exception) {
                                        logger.error("Failed to export board: {}", token, e)
                                    }
                                }
                            imageJobs.add(job)
                        }
                    }
                    else -> {}
                }
            }

            // Wait for all downloads to complete
            logger.debug(
                "Waiting for {} image downloads and {} file downloads",
                imageJobs.size,
                fileJobs.size,
            )
            imageJobs.awaitAll()
            fileJobs.awaitAll()

            logger.info(
                "Asset download completed - Images: {}, Files: {}",
                imageJobs.size,
                fileJobs.size,
            )
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
        logger.debug("Closing Feishu2Html and releasing resources")
        apiClient.close()
        logger.debug("Feishu2Html closed successfully")
    }
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
) {
    init {
        // Create output directories
        File(outputDir).mkdirs()
        File(imageDir).mkdirs()
        File(fileDir).mkdirs()
    }
}

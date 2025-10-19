package dev.yidafu.feishu2html

import dev.yidafu.feishu2html.api.FeishuApiClient
import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.HtmlBuilder
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths

/**
 * 飞书文档转HTML主类
 *
 * 提供将飞书云文档导出为HTML文件的功能，支持自动下载图片和附件。
 * 实现 AutoCloseable 接口，支持使用 use 函数自动管理资源。
 *
 * ## 使用示例
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
 * @property options 配置选项，包含应用凭证、输出路径等
 * @see Feishu2HtmlOptions
 */
class Feishu2Html(
    private val options: Feishu2HtmlOptions,
) : AutoCloseable {
    private val logger = LoggerFactory.getLogger(Feishu2Html::class.java)
    private val apiClient = FeishuApiClient(options.appId, options.appSecret)

    /**
     * 导出单个飞书文档为HTML文件
     *
     * 该方法会：
     * 1. 获取文档基本信息（标题、版本等）
     * 2. 获取文档所有Block内容
     * 3. 下载文档中的图片和附件
     * 4. 生成HTML文件并保存
     *
     * @param documentId 飞书文档ID，可从文档URL中提取
     * @param outputFileName 可选的输出文件名，默认使用文档标题
     * @throws FeishuApiException 当API调用失败时抛出（如权限不足、文档不存在等）
     * @throws java.io.IOException 当文件写入失败时抛出
     *
     * @see exportBatch 批量导出多个文档
     */
    suspend fun export(
        documentId: String,
        outputFileName: String? = null,
    ) {
        logger.info("Starting export for document: {}", documentId)
        logger.debug("Export options - outputDir: {}, imageDir: {}, fileDir: {}",
            options.outputDir, options.imageDir, options.fileDir)

        try {
            // 先获取文档基本信息（包括标题、封面等）
            logger.debug("Fetching document info for: {}", documentId)
            val documentInfo = apiClient.getDocumentInfo(documentId)
            logger.info("Document info retrieved - Title: {}, Version: {}",
                documentInfo.title, documentInfo.revisionId)

            // 获取文档内容
            logger.debug("Fetching document content for: {}", documentId)
            val content = apiClient.getDocumentRawContent(documentId)
            val document = content.document
            val blocks = content.blocks

            logger.info("Document content loaded - Total blocks: {}", blocks.size)
            logger.debug("Document has {} top-level children", document.body?.children?.size ?: 0)

            // 获取有序的文档块列表
            val orderedBlocks = apiClient.getOrderedBlocks(content)
            logger.debug("Ordered blocks count: {}", orderedBlocks.size)

            // 下载图片和文件
            logger.info("Starting asset download for document: {}", documentId)
            downloadAssets(orderedBlocks)

            // 生成HTML
            val fileName = outputFileName ?: "${document.title}.html"
            val htmlFile = File(options.outputDir, fileName)
            htmlFile.parentFile?.mkdirs()
            logger.debug("Output file path: {}", htmlFile.absolutePath)

            logger.info("Building HTML for document: {}", document.title)
            val htmlBuilder = HtmlBuilder(document.title, options.customCss)
            val html = htmlBuilder.build(orderedBlocks, blocks)

            htmlFile.writeText(html)
            logger.info("Document export completed successfully - File: {}", htmlFile.absolutePath)
        } catch (e: Exception) {
            logger.error("Failed to export document {}: {}", documentId, e.message, e)
            throw e
        }
    }

    /**
     * 批量导出多个飞书文档
     *
     * 按顺序依次导出列表中的每个文档。如果某个文档导出失败，
     * 会记录错误日志但继续处理后续文档。
     *
     * @param documentIds 文档ID列表
     *
     * @see export 导出单个文档
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
                    logger.error("Failed to export document {}/{} ({}): {}",
                        index + 1, documentIds.size, documentId, e.message, e)
                }
            }

            logger.info("Batch export completed - Success: {}, Failed: {}, Total: {}",
                successCount, failureCount, documentIds.size)
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
                                            logger.info("图片已下载: $token")
                                        } else {
                                            logger.debug("图片已存在，跳过下载: $token")
                                        }
                                    } catch (e: Exception) {
                                        logger.error("下载图片失败: $token", e)
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
                                            logger.info("文件已下载: $fileName")
                                        } else {
                                            logger.debug("文件已存在，跳过下载: $fileName")
                                        }
                                    } catch (e: Exception) {
                                        logger.error("下载文件失败: $token", e)
                                    }
                                }
                            fileJobs.add(job)
                        }
                    }
                    is BoardBlock -> {
                        // 电子画板块
                        val token = block.board?.token
                        if (token != null) {
                            val job =
                                async {
                                    try {
                                        val imagePath = Paths.get(options.imageDir, "$token.png")
                                        if (!imagePath.toFile().exists()) {
                                            apiClient.exportBoard(token, imagePath)
                                            logger.info("电子画板图片已下载: $token")
                                        } else {
                                            logger.debug("电子画板图片已存在，跳过下载: $token")
                                        }
                                    } catch (e: Exception) {
                                        logger.error("下载电子画板图片失败: $token", e)
                                    }
                                }
                            imageJobs.add(job)
                        }
                    }
                    else -> {}
                }
            }

            // 等待所有下载完成
            logger.debug("Waiting for {} image downloads and {} file downloads",
                imageJobs.size, fileJobs.size)
            imageJobs.awaitAll()
            fileJobs.awaitAll()

            logger.info("Asset download completed - Images: {}, Files: {}",
                imageJobs.size, fileJobs.size)
        }

    /**
     * 关闭HTTP客户端并释放资源
     *
     * 实现 AutoCloseable 接口，会自动在 use 块结束时调用。
     * 也可以手动调用以立即释放资源。
     *
     * ## 示例（推荐使用 use）
     * ```kotlin
     * Feishu2Html(options).use { converter ->
     *     converter.export("doc_id")
     * }
     * ```
     *
     * ## 或手动管理
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
 * 飞书文档转HTML的配置选项
 *
 * @property appId 飞书应用ID，从飞书开放平台获取
 * @property appSecret 飞书应用密钥，从飞书开放平台获取
 * @property outputDir HTML文件输出目录，默认为"./output"
 * @property imageDir 图片保存目录，默认为"./output/images"
 * @property fileDir 附件保存目录，默认为"./output/files"
 * @property imagePath HTML中引用图片的相对路径，默认为"images"
 * @property filePath HTML中引用附件的相对路径，默认为"files"
 * @property customCss 自定义CSS样式，如果提供则覆盖默认样式
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
) {
    init {
        // 创建输出目录
        File(outputDir).mkdirs()
        File(imageDir).mkdirs()
        File(fileDir).mkdirs()
    }
}

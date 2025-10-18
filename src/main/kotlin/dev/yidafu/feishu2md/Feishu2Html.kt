package dev.yidafu.feishu2md

import dev.yidafu.feishu2md.api.FeishuApiClient
import dev.yidafu.feishu2md.api.model.*
import dev.yidafu.feishu2md.converter.HtmlBuilder
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths

/**
 * 飞书文档转HTML主类
 *
 * 提供将飞书云文档导出为HTML文件的功能，支持自动下载图片和附件。
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
 * val converter = Feishu2Html(options)
 * try {
 *     converter.export("document_id")
 * } finally {
 *     converter.close()
 * }
 * ```
 *
 * @property options 配置选项，包含应用凭证、输出路径等
 * @see Feishu2HtmlOptions
 */
class Feishu2Html(
    private val options: Feishu2HtmlOptions,
) {
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
        logger.info("开始导出文档: $documentId")

        // 先获取文档基本信息（包括标题、封面等）
        val documentInfo = apiClient.getDocumentInfo(documentId)
        logger.info("文档标题: ${documentInfo.title}")
        logger.info("文档版本: ${documentInfo.revisionId}")

        // 获取文档内容
        val content = apiClient.getDocumentRawContent(documentId)
        val document = content.document
        val blocks = content.blocks

        logger.info("文档块数量: ${blocks.size}")

        // 获取有序的文档块列表
        val orderedBlocks = apiClient.getOrderedBlocks(content)

        // 下载图片和文件
        downloadAssets(orderedBlocks)

        // 生成HTML
        val fileName = outputFileName ?: "${document.title}.html"
        val htmlFile = File(options.outputDir, fileName)
        htmlFile.parentFile?.mkdirs()

        val htmlBuilder = HtmlBuilder(document.title, options.customCss)
        val html = htmlBuilder.build(orderedBlocks, blocks)

        htmlFile.writeText(html)
        logger.info("HTML文件已保存: ${htmlFile.absolutePath}")
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
            logger.info("开始批量导出 ${documentIds.size} 个文档")

            documentIds.forEach { documentId ->
                try {
                    export(documentId)
                } catch (e: Exception) {
                    logger.error("导出文档失败: $documentId", e)
                }
            }

            logger.info("批量导出完成")
        }

    private suspend fun downloadAssets(blocks: List<Block>) =
        coroutineScope {
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
            imageJobs.awaitAll()
            fileJobs.awaitAll()

            logger.info("资源下载完成: ${imageJobs.size} 张图片, ${fileJobs.size} 个文件")
        }

    /**
     * 关闭HTTP客户端并释放资源
     *
     * 应在所有导出操作完成后调用此方法以释放网络连接资源。
     * 建议使用 try-finally 块确保资源被正确释放。
     *
     * ## 示例
     * ```kotlin
     * val converter = Feishu2Html(options)
     * try {
     *     converter.export("doc_id")
     * } finally {
     *     converter.close()
     * }
     * ```
     */
    fun close() {
        apiClient.close()
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

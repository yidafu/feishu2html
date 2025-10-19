package dev.yidafu.feishu2html.api

import dev.yidafu.feishu2html.api.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

/**
 * 飞书开放平台API客户端
 *
 * 封装了与飞书文档API的所有交互，包括：
 * - 文档信息获取
 * - 文档内容读取
 * - 图片和文件下载
 * - 电子画板导出
 *
 * 内置限流机制（QPS=5）以避免触发API限流。
 *
 * @param appId 飞书应用ID
 * @param appSecret 飞书应用密钥
 *
 * @see FeishuAuthService
 * @see RateLimiter
 */
class FeishuApiClient(
    appId: String,
    appSecret: String,
) {
    private val logger = LoggerFactory.getLogger(FeishuApiClient::class.java)
    private val rateLimiter = RateLimiter(maxRequestsPerSecond = 5) // 飞书API限制：每秒5次

    private val httpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = true
                    },
                )
            }
        }

    private val authService = FeishuAuthService(appId, appSecret, httpClient)

    /**
     * 获取飞书文档的基本信息
     *
     * 调用飞书开放平台API获取文档的元数据，包括标题、创建者、修改时间等。
     *
     * @param documentId 文档ID
     * @return [DocumentInfo] 文档基本信息对象
     * @throws FeishuApiException 当API调用失败时抛出，可能的原因包括：
     *   - 权限不足（code: 1770032）
     *   - 文档不存在
     *   - API限流（code: 99991400）
     *
     * @see <a href="https://open.feishu.cn/document/server-docs/docs/docs/docx-v1/document/get">官方API文档</a>
     */
    suspend fun getDocumentInfo(documentId: String): DocumentInfo =
        rateLimiter.execute {
            logger.info("获取文档基本信息: $documentId")

            val token = authService.getAccessToken()

            val response =
                httpClient.get("https://open.feishu.cn/open-apis/docx/v1/documents/$documentId") {
                    header("Authorization", "Bearer $token")
                }

            // 检查 HTTP 状态码（限频会返回 400）
            if (response.status == HttpStatusCode.BadRequest) {
                val responseText = response.bodyAsText()
                val json =
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                try {
                    val errorResult: DocumentInfoResponse = json.decodeFromString(responseText)
                    if (errorResult.code == 99991400) {
                        throw FeishuApiException("API限频", code = 99991400)
                    }
                } catch (e: Exception) {
                    // 解析失败，继续正常流程
                }
            }

            // 获取原始响应文本用于调试
            val responseText = response.bodyAsText()
            logger.debug("文档基本信息 API 原始响应: $responseText")

            // 手动解析 JSON 以获取更好的错误处理
            val json =
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                }

            val result: DocumentInfoResponse =
                try {
                    json.decodeFromString(responseText)
                } catch (e: Exception) {
                    logger.error("解析文档基本信息响应失败: ${e.message}")
                    logger.error("响应内容: $responseText")
                    throw FeishuApiException("解析API响应失败: ${e.message}\n响应内容: ${responseText.take(500)}")
                }

            if (result.code != 0) {
                val errorMsg = StringBuilder()
                errorMsg.append("获取文档基本信息失败\n")
                errorMsg.append("  错误代码: ${result.code}\n")
                errorMsg.append("  错误信息: ${result.msg}\n")
                errorMsg.append("  文档ID: $documentId\n")
                errorMsg.append("\n常见原因:\n")
                when (result.code) {
                    99991663 -> {
                        errorMsg.append("  - 应用没有访问该文档的权限\n")
                        errorMsg.append("  - 请确认文档已分享给应用，或将文档移至应用可访问的空间\n")
                    }
                    99991668, 1770032 -> {
                        errorMsg.append("  - 应用缺少必要的权限\n")
                        errorMsg.append("  - 请在开放平台添加 'docx:document' 权限并发布应用\n")
                        errorMsg.append("  - 请确认文档已分享给应用，应用需要有文档的访问权限\n")
                    }
                    else -> {
                        errorMsg.append("  - 检查应用是否已添加 'docx:document' 权限\n")
                        errorMsg.append("  - 检查应用是否已发布/启用\n")
                        errorMsg.append("  - 检查文档ID是否正确\n")
                        errorMsg.append("  - 检查应用是否有权限访问该文档\n")
                    }
                }
                logger.error(errorMsg.toString())
                throw FeishuApiException(errorMsg.toString(), code = result.code)
            }

            val documentInfo =
                result.data?.document
                    ?: throw FeishuApiException(
                        "文档基本信息为空。响应: code=${result.code}, msg=${result.msg}",
                        code = result.code,
                    )

            logger.info(
                "成功获取文档基本信息: ${documentInfo.title} (ID: ${documentInfo.documentId}, Revision: ${documentInfo.revisionId})",
            )
            documentInfo
        }

    /**
     * 获取文档所有块
     * 参考: https://open.feishu.cn/document/server-docs/docs/docs/docx-v1/document/list
     */
    suspend fun getDocumentRawContent(documentId: String): DocumentRawContent {
        logger.info("获取文档块列表: $documentId")

        val token = authService.getAccessToken()
        val allBlocks = mutableListOf<Block>()
        var pageToken: String? = null
        var pageCount = 0
        var hasMore = true

        // JSON 解析器
        val json =
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            }

        // 分页获取所有块
        while (hasMore) {
            pageCount++
            logger.debug("获取第 $pageCount 页块数据" + if (pageToken != null) ", page_token=$pageToken" else "")

            // 使用限流器包装请求
            val currentPageToken = pageToken
            val response =
                rateLimiter.execute {
                    httpClient.get("https://open.feishu.cn/open-apis/docx/v1/documents/$documentId/blocks") {
                        header("Authorization", "Bearer $token")
                        parameter("page_size", 500) // 每页最多500个块
                        if (currentPageToken != null) {
                            parameter("page_token", currentPageToken)
                        }
                    }
                }

            // 检查 HTTP 状态码（限频会返回 400）
            if (response.status == HttpStatusCode.BadRequest) {
                val responseText = response.bodyAsText()
                val errorJson =
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                try {
                    val errorResult: DocumentBlocksResponse = errorJson.decodeFromString(responseText)
                    if (errorResult.code == 99991400) {
                        throw FeishuApiException("API限频", code = 99991400)
                    }
                } catch (e: FeishuApiException) {
                    throw e
                } catch (e: Exception) {
                    // 解析失败，继续正常流程
                }
            }

            // 获取原始响应文本用于调试
            val responseText = response.bodyAsText()
            if (pageCount == 1) {
                logger.debug("块列表 API 原始响应: $responseText")
            }

            val result: DocumentBlocksResponse =
                try {
                    json.decodeFromString(responseText)
                } catch (e: Exception) {
                    logger.error("解析响应失败: ${e.message}")
                    logger.error("响应内容: $responseText")
                    throw FeishuApiException("解析API响应失败: ${e.message}\n响应内容: ${responseText.take(500)}")
                }

            if (result.code != 0) {
                val errorMsg = StringBuilder()
                errorMsg.append("获取文档块列表失败\n")
                errorMsg.append("  错误代码: ${result.code}\n")
                errorMsg.append("  错误信息: ${result.msg}\n")
                errorMsg.append("  文档ID: $documentId\n")
                errorMsg.append("\n常见原因:\n")
                when (result.code) {
                    99991663 -> {
                        errorMsg.append("  - 应用没有访问该文档的权限\n")
                        errorMsg.append("  - 请确认文档已分享给应用，或将文档移至应用可访问的空间\n")
                    }
                    99991668, 1770032 -> {
                        errorMsg.append("  - 应用缺少必要的权限\n")
                        errorMsg.append("  - 请在开放平台添加 'docx:document' 权限并发布应用\n")
                        errorMsg.append("  - 请确认文档已分享给应用\n")
                    }
                    else -> {
                        errorMsg.append("  - 检查应用是否已添加 'docx:document' 权限\n")
                        errorMsg.append("  - 检查应用是否已发布/启用\n")
                        errorMsg.append("  - 检查文档ID是否正确\n")
                        errorMsg.append("  - 检查应用是否有权限访问该文档\n")
                    }
                }
                logger.error(errorMsg.toString())
                throw FeishuApiException(errorMsg.toString(), code = result.code)
            }

            val data =
                result.data
                    ?: throw FeishuApiException(
                        "文档块数据为空。响应: code=${result.code}, msg=${result.msg}",
                        code = result.code,
                    )

            allBlocks.addAll(data.items)
            pageToken = data.pageToken
            hasMore = data.hasMore

            logger.debug("获取到 ${data.items.size} 个块, has_more=$hasMore")
        }

        logger.info("成功获取文档所有块: 共 ${allBlocks.size} 个块, 分 $pageCount 页获取")

        // 将块列表转换为 Map 格式
        val blocksMap = allBlocks.associateBy { it.blockId }

        // 构造 Document 对象（从第一个 page 块中获取信息，或使用文档ID）
        val pageBlock = allBlocks.firstOrNull { it is PageBlock } as? PageBlock
        val document =
            Document(
                documentId = documentId,
                revisionId = 0, // 从块列表API无法获取 revision_id
                title = pageBlock?.page?.elements?.firstOrNull()?.textRun?.content?.trim() ?: "未知标题",
            )

        return DocumentRawContent(
            document = document,
            blocks = blocksMap,
        )
    }

    /**
     * 下载文件（图片或附件）
     *
     * 使用文件token从飞书云盘下载文件并保存到指定路径。
     * 支持图片、文档附件等各类文件类型。
     *
     * @param fileToken 文件token，从Block数据中获取
     * @param outputPath 文件保存路径
     * @return [File] 下载后的文件对象
     * @throws FeishuApiException 当下载失败时抛出（如token无效、权限不足等）
     * @throws java.io.IOException 当文件写入失败时抛出
     */
    suspend fun downloadFile(
        fileToken: String,
        outputPath: Path,
    ): File =
        withContext(Dispatchers.IO) {
            rateLimiter.execute {
                logger.info("下载文件: $fileToken")

                val token = authService.getAccessToken()

                val response: HttpResponse =
                    httpClient.get("https://open.feishu.cn/open-apis/drive/v1/medias/$fileToken/download") {
                        header("Authorization", "Bearer $token")
                    }

                // 检查限频
                if (response.status == HttpStatusCode.BadRequest) {
                    // 下载API可能返回不同的错误格式，直接抛出限频异常
                    throw FeishuApiException("API限频", code = 99991400)
                }

                if (!response.status.isSuccess()) {
                    throw FeishuApiException("下载文件失败: ${response.status}")
                }

                val file = outputPath.toFile()
                file.parentFile?.mkdirs()

                val bytes = response.body<ByteArray>()
                file.writeBytes(bytes)

                logger.info("文件已保存: ${file.absolutePath}, 大小: ${bytes.size} bytes")
                file
            }
        }

    /**
     * 导出电子画板为PNG图片
     *
     * 调用飞书画板导出API将电子画板导出为PNG图片。
     * 注意：需要应用具有画板访问权限。
     *
     * @param boardToken 画板token，从BoardBlock中获取
     * @param outputPath 图片保存路径
     * @return [File] 导出的PNG图片文件
     * @throws FeishuApiException 当导出失败时抛出（如权限不足、画板不存在等）
     *
     * @see <a href="https://open.feishu.cn/document/docs/board-v1/whiteboard/download_as_image">官方API文档</a>
     */
    suspend fun exportBoard(
        boardToken: String,
        outputPath: Path,
    ): File =
        withContext(Dispatchers.IO) {
            rateLimiter.execute {
                logger.info("导出电子画板: $boardToken")

                val token = authService.getAccessToken()

                val apiUrl = "https://open.feishu.cn/open-apis/board/v1/whiteboards/$boardToken/download_as_image"
                logger.debug("画板下载API URL: $apiUrl")

                // 使用官方的画板下载图片API (GET方法)
                val response: HttpResponse =
                    httpClient.get(apiUrl) {
                        header("Authorization", "Bearer $token")
                        parameter("file_type", "png") // 指定导出格式为 PNG
                    }

                logger.debug("画板下载API响应状态: ${response.status}")

                // 检查限频
                if (response.status == HttpStatusCode.BadRequest) {
                    throw FeishuApiException("API限频", code = 99991400)
                }

                if (!response.status.isSuccess()) {
                    val responseText = response.bodyAsText()
                    logger.debug("画板下载API响应内容: $responseText")
                    throw FeishuApiException(
                        "画板下载失败: ${response.status}, 响应: $responseText",
                        code = response.status.value,
                    )
                }

                val file = outputPath.toFile()
                file.parentFile?.mkdirs()

                val bytes = response.body<ByteArray>()
                file.writeBytes(bytes)

                logger.info("电子画板已保存: ${file.absolutePath}, 大小: ${bytes.size} bytes")
                file
            }
        }

    /**
     * 获取文档块的有序列表（按文档结构排序）
     */
    fun getOrderedBlocks(content: DocumentRawContent): List<Block> {
        val blocks = content.blocks
        val result = mutableListOf<Block>()
        val visited = mutableSetOf<String>()

        fun traverse(blockId: String) {
            if (blockId in visited) return
            visited.add(blockId)

            val block = blocks[blockId] ?: return
            result.add(block)

            block.children?.forEach { childId ->
                traverse(childId)
            }
        }

        // 从文档根节点（PAGE 块）开始遍历
        // 新的块列表 API 中，第一个块就是 PAGE 块（文档根节点）
        val pageBlock = blocks.values.firstOrNull { it is PageBlock } as? PageBlock
        if (pageBlock != null) {
            logger.debug("找到 PAGE 块，ID: ${pageBlock.blockId}, 子块数量: ${pageBlock.children?.size ?: 0}")

            // 遍历 PAGE 块的所有子块（不包括 PAGE 块本身）
            pageBlock.children?.forEach { childId ->
                traverse(childId)
            }
        } else {
            // 如果没有找到 PAGE 块，尝试使用旧的方式（兼容旧 API）
            logger.warn("未找到 PAGE 块，使用所有块")
            blocks.values.forEach { block ->
                if (block !is PageBlock) {
                    result.add(block)
                }
            }
        }

        logger.debug("有序块列表大小: ${result.size}")
        return result
    }

    /**
     * 关闭HTTP客户端并释放资源
     *
     * 关闭底层的Ktor HttpClient，释放网络连接等资源。
     */
    fun close() {
        httpClient.close()
    }
}

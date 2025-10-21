package dev.yidafu.feishu2html.api

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.platform.createHttpClient
import dev.yidafu.feishu2html.platform.getPlatformFileSystem
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

/**
 * Feishu Open Platform API client
 *
 * Encapsulates all interactions with Feishu Document API, including:
 * - Document info retrieval
 * - Document content reading
 * - Image and file downloads
 * - Board export
 *
 * Built-in rate limiting (QPS=5) to avoid triggering API limits.
 *
 * @param appId Feishu application ID
 * @param appSecret Feishu application secret
 *
 * @see FeishuAuthService
 * @see RateLimiter
 */
internal class FeishuApiClient(
    appId: String,
    appSecret: String,
) {
    private val rateLimiter = RateLimiter(maxRequestsPerSecond = 5) // Feishu API limit: 5 requests per second
    private val httpClient = createHttpClient()
    private val authService = FeishuAuthService(appId, appSecret, httpClient)
    private val fileSystem = getPlatformFileSystem()

    /**
     * Get basic information of a Feishu document
     *
     * Calls Feishu Open Platform API to retrieve document metadata including title, creator, modification time, etc.
     *
     * @param documentId Document ID
     * @return [DocumentInfo] Document metadata object
     * @throws FeishuApiException When API call fails, possible reasons include:
     *   - Insufficient permissions (code: 1770032)
     *   - Document not found
     *   - API rate limit (code: 99991400)
     *
     * @see <a href="https://open.feishu.cn/document/server-docs/docs/docs/docx-v1/document/get">Official API Documentation</a>
     */
    suspend fun getDocumentInfo(documentId: String): DocumentInfo =
        withRetry(retryOn = ::isRetriableException) {
            rateLimiter.execute {
            logger.info { "Fetching document info: $documentId" }

            val token = authService.getAccessToken()

            val response =
                httpClient.get("https://open.feishu.cn/open-apis/docx/v1/documents/$documentId") {
                    header("Authorization", "Bearer $token")
                }

            // Check HTTP status code (rate limit returns 400)
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
                        throw FeishuApiException("API rate limit", code = 99991400)
                    }
                } catch (e: Exception) {
                    // Parsing failed, continue with normal flow
                }
            }

            // Get raw response text for debugging
            val responseText = response.bodyAsText()
            logger.debug { "Document info API raw response: $responseText" }

            // Manually parse JSON for better error handling
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
                    logger.error(e) { "Failed to parse document info response: ${e.message}" }
                    logger.error { "Response content: $responseText" }
                    throw FeishuApiException("Failed to parse API response: ${e.message}\nResponse: ${responseText.take(500)}")
                }

            if (result.code != 0) {
                val errorMsg =
                    buildString {
                        append("Failed to get document info\n")
                        append("  Error code: ${result.code}\n")
                        append("  Error message: ${result.msg}\n")
                        append("  Document ID: $documentId\n")
                        append("\nCommon causes:\n")
                        when (result.code) {
                            99991663 -> {
                                append("  - App does not have permission to access this document\n")
                                append("  - Please share the document with the app or move it to an accessible space\n")
                            }
                            99991668, 1770032 -> {
                                append("  - App is missing required permissions\n")
                                append("  - Please add 'docx:document' permission in Open Platform and publish the app\n")
                                append("  - Please ensure the document is shared with the app\n")
                            }
                            else -> {
                                append("  - Check if app has 'docx:document' permission\n")
                                append("  - Check if app is published/enabled\n")
                                append("  - Check if document ID is correct\n")
                                append("  - Check if app has permission to access the document\n")
                            }
                        }
                    }
                logger.error { errorMsg }
                throw FeishuApiException(errorMsg, code = result.code)
            }

            val documentInfo =
                result.data?.document
                    ?: throw FeishuApiException(
                        "Document info is empty. Response: code=${result.code}, msg=${result.msg}",
                        code = result.code,
                    )

            logger.info {
                "Successfully fetched document info: ${documentInfo.title} " +
                    "(ID: ${documentInfo.documentId}, Revision: ${documentInfo.revisionId})"
            }
            documentInfo
            }
        }

    /**
     * Get all blocks of a document
     * Reference: https://open.feishu.cn/document/server-docs/docs/docs/docx-v1/document/list
     */
    suspend fun getDocumentRawContent(documentId: String): DocumentRawContent {
        logger.info { "Starting to fetch document blocks for: $documentId" }
        logger.debug { "Will fetch blocks with page_size=500" }

        val token = authService.getAccessToken()
        val allBlocks = mutableListOf<Block>()
        var pageToken: String? = null
        var pageCount = 0
        var hasMore = true

        // JSON parser
        val json =
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            }

        // Fetch all blocks with pagination
        while (hasMore) {
            pageCount++
            logger.debug {
                "Fetching page $pageCount blocks${if (pageToken != null) " with page_token=$pageToken" else ""}"
            }

            // Execute request with rate limiter
            val currentPageToken = pageToken
            val response =
                rateLimiter.execute {
                    httpClient.get("https://open.feishu.cn/open-apis/docx/v1/documents/$documentId/blocks") {
                        header("Authorization", "Bearer $token")
                        parameter("page_size", 500) // Max 500 blocks per page
                        if (currentPageToken != null) {
                            parameter("page_token", currentPageToken)
                        }
                    }
                }

            // Check HTTP status code (rate limit returns 400)
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
                        throw FeishuApiException("API rate limit", code = 99991400)
                    }
                } catch (e: FeishuApiException) {
                    throw e
                } catch (e: Exception) {
                    // Parsing failed, continue with normal flow
                }
            }

            // Get raw response text for debugging
            val responseText = response.bodyAsText()
            if (pageCount == 1) {
                logger.debug { "Blocks list API raw response: $responseText" }
            }

            val result: DocumentBlocksResponse =
                try {
                    json.decodeFromString(responseText)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to parse response: ${e.message}" }
                    logger.error { "Response content: $responseText" }
                    throw FeishuApiException("Failed to parse API response: ${e.message}\nResponse: ${responseText.take(500)}")
                }

            if (result.code != 0) {
                val errorMsg =
                    buildString {
                        append("Failed to get document blocks\n")
                        append("  Error code: ${result.code}\n")
                        append("  Error message: ${result.msg}\n")
                        append("  Document ID: $documentId\n")
                        append("\nCommon causes:\n")
                        when (result.code) {
                            99991663 -> {
                                append("  - App does not have permission to access this document\n")
                                append("  - Please share the document with the app or move it to an accessible space\n")
                            }
                            99991668, 1770032 -> {
                                append("  - App is missing required permissions\n")
                                append("  - Please add 'docx:document' permission in Open Platform and publish the app\n")
                                append("  - Please ensure the document is shared with the app\n")
                            }
                            else -> {
                                append("  - Check if app has 'docx:document' permission\n")
                                append("  - Check if app is published/enabled\n")
                                append("  - Check if document ID is correct\n")
                                append("  - Check if app has permission to access the document\n")
                            }
                        }
                    }
                logger.error { errorMsg }
                throw FeishuApiException(errorMsg, code = result.code)
            }

            val data =
                result.data
                    ?: throw FeishuApiException(
                        "Document blocks data is empty. Response: code=${result.code}, msg=${result.msg}",
                        code = result.code,
                    )

            allBlocks.addAll(data.items)
            pageToken = data.pageToken
            hasMore = data.hasMore

            logger.debug { "Fetched ${data.items.size} blocks, has_more=$hasMore" }
        }

        logger.info {
            "Successfully fetched all document blocks: ${allBlocks.size} blocks across $pageCount pages"
        }

        // Convert block list to Map format
        val blocksMap = allBlocks.associateBy { it.blockId }

        // Construct Document object (get info from first page block, or use document ID)
        val pageBlock = allBlocks.firstOrNull { it is PageBlock } as? PageBlock
        val document =
            Document(
                documentId = documentId,
                revisionId = 0, // Cannot get revision_id from blocks list API
                title = pageBlock?.page?.elements?.firstOrNull()?.textRun?.content?.trim() ?: "Unknown Title",
            )

        return DocumentRawContent(
            document = document,
            blocks = blocksMap,
        )
    }

    /**
     * Download file (image or attachment)
     *
     * Downloads a file from Feishu Drive using file token and saves it to specified path.
     * Supports images, document attachments, and other file types.
     *
     * @param fileToken File token, obtained from Block data
     * @param outputPath File save path
     * @throws FeishuApiException When download fails (e.g., invalid token, insufficient permissions)
     */
    suspend fun downloadFile(
        fileToken: String,
        outputPath: String,
    ) = withRetry(retryOn = ::isRetriableException) {
        rateLimiter.execute {
            logger.info { "Downloading file: $fileToken" }
            logger.debug { "Output path: $outputPath" }

            try {
                val token = authService.getAccessToken()

                val response: HttpResponse =
                    httpClient.get("https://open.feishu.cn/open-apis/drive/v1/medias/$fileToken/download") {
                        header("Authorization", "Bearer $token")
                    }

                // Check for rate limiting
                if (response.status == HttpStatusCode.BadRequest) {
                    logger.warn { "API rate limit hit while downloading file: $fileToken" }
                    throw FeishuApiException("API rate limit", code = 99991400)
                }

                if (!response.status.isSuccess()) {
                    logger.error {
                        "File download failed with status: ${response.status} for token: $fileToken"
                    }
                    throw FeishuApiException("File download failed: ${response.status}")
                }

                val bytes = response.body<ByteArray>()
                fileSystem.writeBytes(outputPath, bytes)

                logger.info {
                    "File downloaded successfully: $outputPath (${bytes.size} bytes)"
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to download file $fileToken: ${e.message}" }
                throw e
            }
        }
    }

    /**
     * Export board as PNG image
     *
     * Calls Feishu board export API to export an electronic board as a PNG image.
     * Note: Requires app to have board access permission.
     *
     * @param boardToken Board token, obtained from BoardBlock
     * @param outputPath Image save path
     * @throws FeishuApiException When export fails (e.g., insufficient permissions, board not found)
     *
     * @see <a href="https://open.feishu.cn/document/docs/board-v1/whiteboard/download_as_image">Official API Documentation</a>
     */
    suspend fun exportBoard(
        boardToken: String,
        outputPath: String,
    ) {
        rateLimiter.execute {
            logger.info { "Exporting board as image: $boardToken" }
            logger.debug { "Output path: $outputPath" }

            val token = authService.getAccessToken()

            val apiUrl = "https://open.feishu.cn/open-apis/board/v1/whiteboards/$boardToken/download_as_image"
            logger.debug { "Board download API URL: $apiUrl" }

            // Use official board image download API (GET method)
            val response: HttpResponse =
                httpClient.get(apiUrl) {
                    header("Authorization", "Bearer $token")
                    parameter("file_type", "png") // Specify PNG export format
                }

            logger.debug { "Board download API response status: ${response.status}" }

            // Check for rate limiting
            if (response.status == HttpStatusCode.BadRequest) {
                throw FeishuApiException("API rate limit", code = 99991400)
            }

            if (!response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                logger.debug { "Board download API response content: $responseText" }
                throw FeishuApiException(
                    "Board download failed: ${response.status}, response: $responseText",
                    code = response.status.value,
                )
            }

            val bytes = response.body<ByteArray>()
            fileSystem.writeBytes(outputPath, bytes)

            logger.info {
                "Board saved successfully: $outputPath (${bytes.size} bytes)"
            }
        }
    }

    /**
     * Get ordered list of document blocks as a tree structure
     *
     * Converts the flat block list from Feishu API into a tree of BlockNodes,
     * where each node contains its Block data, children, and parent reference.
     *
     * @param content Document raw content containing blocks map
     * @return List of top-level BlockNodes (children of PAGE block)
     */
    fun getOrderedBlocks(content: DocumentRawContent): List<BlockNode<out Block>> {
        val blocks = content.blocks
        val nodeCache = mutableMapOf<String, BlockNode<out Block>>()

        /**
         * Recursively build BlockNode tree
         *
         * @param blockId ID of the block to build node for
         * @param parent Parent BlockNode (null for root)
         * @return Built BlockNode or null if block not found
         */
        fun buildNode(
            blockId: String,
            parent: BlockNode<out Block>? = null,
        ): BlockNode<out Block>? {
            // Check cache to avoid rebuilding
            if (blockId in nodeCache) {
                return nodeCache[blockId]
            }

            val block = blocks[blockId] ?: return null

            // Create node without children first (will be set later)
            // This avoids infinite recursion for circular references
            val node =
                BlockNode(
                    data = block,
                    children = emptyList(),
                    parent = parent,
                )

            // Cache the node before building children to handle circular refs
            nodeCache[blockId] = node

            // Build children recursively
            val children =
                block.children?.mapNotNull { childId ->
                    buildNode(childId, node)
                } ?: emptyList()

            // Update node with built children
            // Create new instance with children (can't use copy due to parent circular ref)
            val completeNode = BlockNode(
                data = node.data,
                children = children,
                parent = node.parent
            )
            nodeCache[blockId] = completeNode

            return completeNode
        }

        // Build tree from PAGE block
        val pageBlock = blocks.values.firstOrNull { it is PageBlock } as? PageBlock

        return if (pageBlock != null) {
            logger.debug {
                "Found PAGE block - ID: ${pageBlock.blockId}, children count: ${pageBlock.children?.size ?: 0}"
            }

            // Build tree for all direct children of PAGE block
            val rootNodes =
                pageBlock.children?.mapNotNull { childId ->
                    buildNode(childId, parent = null) // Top-level nodes have no parent
                } ?: emptyList()

            logger.debug { "Built tree with ${rootNodes.size} root nodes" }
            rootNodes
        } else {
            logger.warn { "PAGE block not found, building flat list as fallback" }

            // Fallback: treat all non-PAGE blocks as root nodes
            blocks.values
                .filter { it !is PageBlock }
                .map { block ->
                    BlockNode(
                        data = block,
                        children = emptyList(),
                        parent = null,
                    )
                }
        }
    }

    /**
     * Close HTTP client and release resources
     *
     * Closes the underlying Ktor HttpClient and releases network connection resources.
     */
    fun close() {
        httpClient.close()
    }
}

package dev.yidafu.feishu2html.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 飞书文档基本信息
 */
@Serializable
internal data class DocumentInfo(
    @SerialName("document_id") val documentId: String,
    @SerialName("revision_id") val revisionId: Int,
    val title: String,
)

/**
 * 获取文档基本信息的响应
 */
@Serializable
internal data class DocumentInfoResponse(
    val code: Int,
    val msg: String,
    val data: DocumentInfoData? = null,
)

@Serializable
internal data class DocumentInfoData(
    val document: DocumentInfo,
)

/**
 * 飞书文档（用于 raw_content）
 */
@Serializable
internal data class Document(
    @SerialName("document_id") val documentId: String,
    @SerialName("revision_id") val revisionId: Int,
    val title: String,
)

/**
 * 获取文档块列表的响应
 * 参考: https://open.feishu.cn/document/server-docs/docs/docs/docx-v1/document/list
 */
@Serializable
internal data class DocumentBlocksResponse(
    val code: Int,
    val msg: String,
    val data: DocumentBlocksData? = null,
)

@Serializable
internal data class DocumentBlocksData(
    val items: List<Block>,
    @SerialName("page_token") val pageToken: String? = null,
    @SerialName("has_more") val hasMore: Boolean = false,
)

@Serializable
internal data class DocumentRawContentResponse(
    val code: Int,
    val msg: String,
    val data: DocumentRawContent? = null,
)

@Serializable
internal data class DocumentRawContent(
    val document: Document,
    val blocks: Map<String, Block>,
)

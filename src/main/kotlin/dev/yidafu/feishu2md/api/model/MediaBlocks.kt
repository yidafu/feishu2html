package dev.yidafu.feishu2md.api.model

import kotlinx.serialization.*

/**
 * MediaBlocks
 */

@Serializable
data class ImageBlock(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("image") val image: ImageBlockData? = null,
) : Block()

@Serializable
data class FileBlock(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("file") val file: FileBlockData? = null,
) : Block()

@Serializable
data class BoardBlock(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("board") val board: BoardBlockData? = null,
) : Block()

@Serializable
data class DiagramBlock(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("diagram") val diagram: DiagramBlockData? = null,
) : Block()

@Serializable
data class IframeBlock(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("iframe") val iframe: IframeBlockData? = null,
) : Block()

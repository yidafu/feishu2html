package dev.yidafu.feishu2md.api.model

import kotlinx.serialization.*

/**
 * HeadingBlocks
 */

@Serializable
data class Heading1Block(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("heading1") val heading1: HeadingBlockData? = null,
) : Block()

@Serializable
data class Heading2Block(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("heading2") val heading2: HeadingBlockData? = null,
) : Block()

@Serializable
data class Heading3Block(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("heading3") val heading3: HeadingBlockData? = null,
) : Block()

@Serializable
data class Heading4Block(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("heading4") val heading4: HeadingBlockData? = null,
) : Block()

@Serializable
data class Heading5Block(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("heading5") val heading5: HeadingBlockData? = null,
) : Block()

@Serializable
data class Heading6Block(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("heading6") val heading6: HeadingBlockData? = null,
) : Block()

@Serializable
data class Heading7Block(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("heading7") val heading7: HeadingBlockData? = null,
) : Block()

@Serializable
data class Heading8Block(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("heading8") val heading8: HeadingBlockData? = null,
) : Block()

@Serializable
data class Heading9Block(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("heading9") val heading9: HeadingBlockData? = null,
) : Block()

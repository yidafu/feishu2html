package dev.yidafu.feishu2html.api.model

import kotlinx.serialization.*

/**
 * OtherBlocks
 */

@Serializable
internal data class BitableBlock(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("bitable") val bitable: BitableBlockData? = null,
) : Block()

@Serializable
internal data class ChatCardBlock(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("chat_card") val chatCard: ChatCardBlockData? = null,
) : Block()

@Serializable
internal data class UnknownBlock(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType = BlockType.UNDEFINED,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    val quote: QuoteBlockData? = null,
    val undefined: kotlinx.serialization.json.JsonElement? = null,
) : Block()

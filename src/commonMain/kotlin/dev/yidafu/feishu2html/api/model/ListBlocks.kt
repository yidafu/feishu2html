package dev.yidafu.feishu2html.api.model

import kotlinx.serialization.*

/**
 * ListBlocks
 */

@Serializable
internal data class BulletBlock(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("bullet") val bullet: BulletBlockData? = null,
) : Block()

@Serializable
internal data class OrderedBlock(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("ordered") val ordered: OrderedBlockData? = null,
) : Block()

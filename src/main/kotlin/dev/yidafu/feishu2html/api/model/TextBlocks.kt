package dev.yidafu.feishu2html.api.model

import kotlinx.serialization.*

/**
 * TextBlocks
 */

@Serializable
data class PageBlock(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("page") val page: PageBlockData? = null,
) : Block()

@Serializable
data class TextBlock(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("text") val text: TextBlockData? = null,
) : Block()

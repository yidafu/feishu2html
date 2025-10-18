package dev.yidafu.feishu2md.api.model

import kotlinx.serialization.*

/**
 * Block数据类定义 - 各类Block的具体数据结构
 */

@Serializable
data class PageBlockData(
    val elements: List<TextElement>? = null,
    val style: TextStyle? = null,
)

@Serializable
data class TextBlockData(
    val elements: List<TextElement>,
    val style: TextStyle? = null,
)

@Serializable
data class HeadingBlockData(
    val elements: List<TextElement>,
    val style: TextStyle? = null,
)

@Serializable
data class BulletBlockData(
    val elements: List<TextElement>,
    val style: TextStyle? = null,
)

@Serializable
data class OrderedBlockData(
    val elements: List<TextElement>,
    val style: TextStyle? = null,
)

@Serializable
data class CodeBlockData(
    val language: Int? = null,
    val elements: List<TextElement>,
)

@Serializable
data class QuoteBlockData(
    val elements: List<TextElement>,
    val style: TextStyle? = null,
)

@Serializable
data class EquationBlockData(
    val content: String,
)

@Serializable
data class TodoBlockData(
    val elements: List<TextElement>,
    val style: TodoStyle? = null,
)

@Serializable
data class TodoStyle(
    val align: Int? = null,
    val done: Boolean? = null,
    val folded: Boolean? = null,
)

@Serializable
data class BitableBlockData(
    val token: String,
    @SerialName("view_type") val viewType: String? = null,
)

@Serializable
data class CalloutBlockData(
    @SerialName("background_color") val backgroundColor: Int? = null,
    @SerialName("border_color") val borderColor: Int? = null,
    @SerialName("text_color") val textColor: Int? = null,
    @SerialName("emoji_id") val emojiId: String? = null,
    val elements: List<TextElement>? = null,
)

@Serializable
data class ChatCardBlockData(
    @SerialName("chat_id") val chatId: String,
    @SerialName("align") val align: Int? = null,
)

@Serializable
data class DiagramBlockData(
    @SerialName("diagram_type") val diagramType: Int? = null,
    val content: String,
)

@Serializable
data class FileBlockData(
    val name: String? = null,
    val token: String,
    @SerialName("tmp_url") val tmpUrl: String? = null,
)

@Serializable
data class GridBlockData(
    @SerialName("column_size") val columnSize: Int,
)

@Serializable
data class GridColumnBlockData(
    @SerialName("width_ratio") val widthRatio: Int,
)

@Serializable
data class IframeBlockData(
    val url: String? = null, // 旧格式兼容
    val component: IframeComponent? = null, // 新格式
)

@Serializable
data class IframeComponent(
    @SerialName("iframe_type") val iframeType: Int,
    val url: String,
)

@Serializable
data class ImageBlockData(
    val token: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val align: Int? = null,
)

@Serializable
data class TableBlockData(
    val cells: List<String>? = null,
    @SerialName("property") val property: TableProperty? = null,
)

@Serializable
data class TableProperty(
    @SerialName("row_size") val rowSize: Int,
    @SerialName("column_size") val columnSize: Int,
    @SerialName("column_width") val columnWidth: List<Int>? = null,
    @SerialName("merge_info") val mergeInfo: List<MergeInfo>? = null,
)

@Serializable
data class MergeInfo(
    @SerialName("row_span") val rowSpan: Int,
    @SerialName("col_span") val colSpan: Int,
)

@Serializable
data class TableCellBlockData(
    val elements: List<TextElement>? = null,
)

@Serializable
data class QuoteContainerBlockData(
    val elements: List<TextElement>? = null,
)

@Serializable
data class BoardBlockData(
    val token: String,
    val width: Int? = null,
    val height: Int? = null,
)

@Serializable
data class AddOnsBlockData(
    @SerialName("component_id") val componentId: String? = null,
    @SerialName("component_type_id") val componentTypeId: String? = null,
    val record: String? = null,
)

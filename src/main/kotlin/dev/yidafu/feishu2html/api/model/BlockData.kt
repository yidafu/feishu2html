package dev.yidafu.feishu2html.api.model

import kotlinx.serialization.*

/**
 * Block数据类定义 - 各类Block的具体数据结构
 */

@Serializable
internal data class PageBlockData(
    val elements: List<TextElement>? = null,
    val style: TextStyle? = null,
)

@Serializable
internal data class TextBlockData(
    val elements: List<TextElement>,
    val style: TextStyle? = null,
)

@Serializable
internal data class HeadingBlockData(
    val elements: List<TextElement>,
    val style: TextStyle? = null,
)

@Serializable
internal data class BulletBlockData(
    val elements: List<TextElement>,
    val style: TextStyle? = null,
)

@Serializable
internal data class OrderedBlockData(
    val elements: List<TextElement>,
    val style: TextStyle? = null,
)

@Serializable
internal data class CodeBlockData(
    val language: Int? = null,
    val elements: List<TextElement>,
)

@Serializable
internal data class QuoteBlockData(
    val elements: List<TextElement>,
    val style: TextStyle? = null,
)

@Serializable
internal data class EquationBlockData(
    val content: String,
)

@Serializable
internal data class TodoBlockData(
    val elements: List<TextElement>,
    val style: TodoStyle? = null,
)

@Serializable
internal data class TodoStyle(
    val align: Int? = null,
    val done: Boolean? = null,
    val folded: Boolean? = null,
)

@Serializable
internal data class BitableBlockData(
    val token: String,
    @SerialName("view_type") val viewType: String? = null,
)

@Serializable
internal data class CalloutBlockData(
    @SerialName("background_color") val backgroundColor: Int? = null,
    @SerialName("border_color") val borderColor: Int? = null,
    @SerialName("text_color") val textColor: Int? = null,
    @SerialName("emoji_id") val emojiId: String? = null,
    val elements: List<TextElement>? = null,
)

@Serializable
internal data class ChatCardBlockData(
    @SerialName("chat_id") val chatId: String,
    @SerialName("align") val align: Int? = null,
)

@Serializable
internal data class DiagramBlockData(
    @SerialName("diagram_type") val diagramType: Int? = null,
    val content: String,
)

@Serializable
internal data class FileBlockData(
    val name: String? = null,
    val token: String,
    @SerialName("tmp_url") val tmpUrl: String? = null,
)

@Serializable
internal data class GridBlockData(
    @SerialName("column_size") val columnSize: Int,
)

@Serializable
internal data class GridColumnBlockData(
    @SerialName("width_ratio") val widthRatio: Int,
)

@Serializable
internal data class IframeBlockData(
    val url: String? = null, // 旧格式兼容
    val component: IframeComponent? = null, // 新格式
)

@Serializable
internal data class IframeComponent(
    @SerialName("iframe_type") val iframeType: Int,
    val url: String,
)

@Serializable
internal data class ImageBlockData(
    val token: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val align: Int? = null,
)

@Serializable
internal data class TableBlockData(
    val cells: List<String>? = null,
    @SerialName("property") val property: TableProperty? = null,
)

@Serializable
internal data class TableProperty(
    @SerialName("row_size") val rowSize: Int,
    @SerialName("column_size") val columnSize: Int,
    @SerialName("column_width") val columnWidth: List<Int>? = null,
    @SerialName("merge_info") val mergeInfo: List<MergeInfo>? = null,
)

@Serializable
internal data class MergeInfo(
    @SerialName("row_span") val rowSpan: Int,
    @SerialName("col_span") val colSpan: Int,
)

@Serializable
internal data class TableCellBlockData(
    val elements: List<TextElement>? = null,
)

@Serializable
internal data class QuoteContainerBlockData(
    val elements: List<TextElement>? = null,
)

@Serializable
internal data class BoardBlockData(
    val token: String,
    val width: Int? = null,
    val height: Int? = null,
)

@Serializable
internal data class AddOnsBlockData(
    @SerialName("component_id") val componentId: String? = null,
    @SerialName("component_type_id") val componentTypeId: String? = null,
    val record: String? = null,
)

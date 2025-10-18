package dev.yidafu.feishu2md.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 文本元素
 */
@Serializable
data class TextElement(
    @SerialName("text_run") val textRun: TextRun? = null,
    @SerialName("mention_user") val mentionUser: MentionUser? = null,
    @SerialName("mention_doc") val mentionDoc: MentionDoc? = null,
    val equation: InlineEquation? = null,
    @SerialName("reminder") val reminder: Reminder? = null,
    @SerialName("file") val file: InlineFile? = null,
    @SerialName("undefined") val undefined: UndefinedElement? = null,
)

@Serializable
data class TextRun(
    val content: String,
    @SerialName("text_element_style") val textElementStyle: TextElementStyle? = null,
)

@Serializable
data class TextElementStyle(
    val bold: Boolean? = null,
    val italic: Boolean? = null,
    val strikethrough: Boolean? = null,
    val underline: Boolean? = null,
    @SerialName("inline_code") val inlineCode: Boolean? = null,
    @SerialName("background_color") val backgroundColor: Int? = null,
    @SerialName("text_color") val textColor: Int? = null,
    val link: Link? = null,
)

@Serializable
data class Link(
    val url: String,
)

@Serializable
data class MentionUser(
    @SerialName("user_id") val userId: String,
    @SerialName("text_element_style") val textElementStyle: TextElementStyle? = null,
)

@Serializable
data class MentionDoc(
    val token: String,
    @SerialName("obj_type") val objType: Int,
    val url: String,
    val title: String? = null,
    @SerialName("text_element_style") val textElementStyle: TextElementStyle? = null,
)

@Serializable
data class InlineEquation(
    val content: String,
    @SerialName("text_element_style") val textElementStyle: TextElementStyle? = null,
)

@Serializable
data class Reminder(
    @SerialName("create_user_id") val createUserId: String,
    @SerialName("is_notify") val isNotify: Boolean? = null,
    @SerialName("is_whole_day") val isWholeDay: Boolean? = null,
    @SerialName("expire_time") val expireTime: String,
    @SerialName("notify_time") val notifyTime: String,
)

@Serializable
data class InlineFile(
    @SerialName("file_token") val fileToken: String,
    @SerialName("source_block_id") val sourceBlockId: String,
    @SerialName("text_element_style") val textElementStyle: TextElementStyle? = null,
)

@Serializable
data class UndefinedElement(
    val content: String? = null,
)

@Serializable
data class TextStyle(
    val align: Int? = null,
    val done: Boolean? = null,
    val folded: Boolean? = null,
    val language: Int? = null,
    val wrap: Boolean? = null,
)

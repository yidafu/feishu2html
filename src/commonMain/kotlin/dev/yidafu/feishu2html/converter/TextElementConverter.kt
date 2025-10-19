package dev.yidafu.feishu2html.converter

import dev.yidafu.feishu2html.api.model.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Text element converter - uses kotlinx.html DSL
 */
internal class TextElementConverter {
    private val logger = KotlinLogging.logger {}

    /**
     * Convert text element list to HTML (DSL approach)
     */
    fun convertElements(
        elements: List<TextElement>,
        parent: FlowContent,
    ) {
        logger.debug { "Converting ${elements.size} text elements" }
        elements.forEach { convertElement(it, parent) }
    }

    /**
     * Convert text element list to plain text (for code blocks, etc.)
     */
    fun convertElementsPlainText(elements: List<TextElement>): String {
        return elements.joinToString("") { getPlainText(it) }
    }

    private fun convertElement(
        element: TextElement,
        parent: FlowContent,
    ) {
        when {
            element.textRun != null -> convertTextRun(element.textRun!!, parent)
            element.mentionUser != null -> convertMentionUser(parent)
            element.mentionDoc != null -> convertMentionDoc(element.mentionDoc!!, parent)
            element.equation != null -> convertInlineEquation(element.equation!!, parent)
            element.file != null -> convertInlineFile(parent)
            element.undefined != null -> convertUndefinedElement(element.undefined!!, parent)
        }
    }

    private fun getPlainText(element: TextElement): String {
        return when {
            element.textRun != null -> element.textRun!!.content
            element.mentionUser != null -> "@用户"
            element.mentionDoc != null -> element.mentionDoc!!.title ?: "@文档"
            element.equation != null -> element.equation!!.content
            element.file != null -> "[文件]"
            element.undefined != null -> element.undefined!!.content ?: "Button"
            else -> ""
        }
    }

    private fun convertTextRun(
        textRun: TextRun,
        parent: FlowContent,
    ) {
        val content = textRun.content
        val style = textRun.textElementStyle

        if (style == null) {
            // 无样式，直接输出文本
            parent.text(content)
            return
        }

        // 应用样式层层包裹
        applyTextStyle(content, style, parent)
    }

    private fun applyTextStyle(
        content: String,
        style: TextElementStyle,
        parent: FlowContent,
    ) {
        // 颜色和背景色需要用 span 包裹
        val colorClasses = mutableListOf<String>()
        style.textColor?.let { textColor ->
            BlockColor.getColorClass(textColor)?.let {
                colorClasses.add("text-$it")
            }
        }
        style.backgroundColor?.let { bgColor ->
            BlockColor.getColorClass(bgColor)?.let {
                colorClasses.add("bg-$it")
            }
        }

        // 构建内容，从最内层到最外层
        val renderContent: FlowContent.() -> Unit = {
            // 最内层：链接或纯文本
            if (style.link != null) {
                a(href = style.link!!.url) {
                    text(content)
                }
            } else {
                text(content)
            }
        }

        // 应用内联代码
        val withCode: FlowContent.() -> Unit =
            if (style.inlineCode == true) {
                { code { renderContent() } }
            } else {
                renderContent
            }

        // 应用删除线
        val withStrikethrough: FlowContent.() -> Unit =
            if (style.strikethrough == true) {
                { del { withCode() } }
            } else {
                withCode
            }

        // 应用下划线
        val withUnderline: FlowContent.() -> Unit =
            if (style.underline == true) {
                { u { withStrikethrough() } }
            } else {
                withStrikethrough
            }

        // 应用斜体
        val withItalic: FlowContent.() -> Unit =
            if (style.italic == true) {
                { em { withUnderline() } }
            } else {
                withUnderline
            }

        // 应用粗体
        val withBold: FlowContent.() -> Unit =
            if (style.bold == true) {
                { strong { withItalic() } }
            } else {
                withItalic
            }

        // 最外层：颜色 span
        if (colorClasses.isNotEmpty()) {
            parent.span(classes = colorClasses.joinToString(" ")) {
                withBold()
            }
        } else {
            parent.withBold()
        }
    }

    private fun convertMentionUser(parent: FlowContent) {
        parent.span(classes = "mention-user") {
            +"@用户"
        }
    }

    private fun convertMentionDoc(
        mentionDoc: MentionDoc,
        parent: FlowContent,
    ) {
        val title = mentionDoc.title ?: "文档"
        parent.a(href = mentionDoc.url, classes = "mention-doc") {
            +title
        }
    }

    private fun convertInlineEquation(
        equation: InlineEquation,
        parent: FlowContent,
    ) {
        parent.text("\\(${equation.content}\\)")
    }

    private fun convertInlineFile(parent: FlowContent) {
        parent.span(classes = "inline-file") {
            +"[文件]"
        }
    }

    private fun convertUndefinedElement(
        undefined: UndefinedElement,
        parent: FlowContent,
    ) {
        // Render as a simple button placeholder
        // The API returns {"undefined":{}} for buttons, which are not fully supported yet
        val buttonText = undefined.content ?: "Button"
        parent.button(classes = "ud__button ud__button--default ud__button--size-md feishu-button") {
            type = ButtonType.button
            +buttonText
        }
    }
}

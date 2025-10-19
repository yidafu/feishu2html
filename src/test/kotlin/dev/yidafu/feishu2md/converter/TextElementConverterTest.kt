package dev.yidafu.feishu2md.converter

import dev.yidafu.feishu2md.api.model.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.html.div
import kotlinx.html.stream.createHTML

class TextElementConverterTest : FunSpec({

    val converter = TextElementConverter()

    test("应该正确转换纯文本") {
        val elements = listOf(
            TextElement(textRun = TextRun(content = "Hello World"))
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "Hello World"
    }

    test("应该正确应用粗体样式") {
        val elements = listOf(
            TextElement(
                textRun = TextRun(
                    content = "Bold Text",
                    textElementStyle = TextElementStyle(bold = true)
                )
            )
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "<strong>"
        html shouldContain "Bold Text"
        html shouldContain "</strong>"
    }

    test("应该正确应用斜体样式") {
        val elements = listOf(
            TextElement(
                textRun = TextRun(
                    content = "Italic Text",
                    textElementStyle = TextElementStyle(italic = true)
                )
            )
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "<em>"
        html shouldContain "Italic Text"
    }

    test("应该正确应用下划线样式") {
        val elements = listOf(
            TextElement(
                textRun = TextRun(
                    content = "Underline Text",
                    textElementStyle = TextElementStyle(underline = true)
                )
            )
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "<u>"
        html shouldContain "Underline Text"
    }

    test("应该正确应用删除线样式") {
        val elements = listOf(
            TextElement(
                textRun = TextRun(
                    content = "Strikethrough Text",
                    textElementStyle = TextElementStyle(strikethrough = true)
                )
            )
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "<del>"
        html shouldContain "Strikethrough Text"
    }

    test("应该正确应用内联代码样式") {
        val elements = listOf(
            TextElement(
                textRun = TextRun(
                    content = "console.log()",
                    textElementStyle = TextElementStyle(inlineCode = true)
                )
            )
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "<code>"
        html shouldContain "console.log()"
    }

    test("应该正确组合多种样式") {
        val elements = listOf(
            TextElement(
                textRun = TextRun(
                    content = "Bold and Italic",
                    textElementStyle = TextElementStyle(bold = true, italic = true)
                )
            )
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "<strong>"
        html shouldContain "<em>"
        html shouldContain "Bold and Italic"
    }

    test("应该正确转换链接") {
        val elements = listOf(
            TextElement(
                textRun = TextRun(
                    content = "Click here",
                    textElementStyle = TextElementStyle(
                        link = Link(url = "https://example.com")
                    )
                )
            )
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain """href="https://example.com""""
        html shouldContain "Click here"
    }

    test("应该正确应用文本颜色") {
        val elements = listOf(
            TextElement(
                textRun = TextRun(
                    content = "Red Text",
                    textElementStyle = TextElementStyle(textColor = 1) // 假设1是红色
                )
            )
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "Red Text"
        // 根据实际的颜色class实现来验证
    }

    test("应该正确转换mention用户") {
        val elements = listOf(
            TextElement(mentionUser = MentionUser(userId = "user123", textElementStyle = null))
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "@用户"
        html shouldContain "mention-user"
    }

    test("应该正确转换mention文档") {
        val elements = listOf(
            TextElement(
                mentionDoc = MentionDoc(
                    token = "doc_token",
                    objType = 1,
                    url = "https://feishu.cn/docs/abc123",
                    title = "相关文档",
                    textElementStyle = null
                )
            )
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "相关文档"
        html shouldContain "https://feishu.cn/docs/abc123"
        html shouldContain "mention-doc"
    }

    test("应该正确转换内联公式") {
        val elements = listOf(
            TextElement(equation = InlineEquation(content = "E = mc^2"))
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "\\(E = mc^2\\)"
    }

    test("应该正确转换内联文件") {
        val elements = listOf(
            TextElement(file = InlineFile(fileToken = "file_token", sourceBlockId = "block123", textElementStyle = null))
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "[文件]"
        html shouldContain "inline-file"
    }

    test("convertElementsPlainText应该返回纯文本") {
        val elements = listOf(
            TextElement(textRun = TextRun(
                content = "Bold",
                textElementStyle = TextElementStyle(bold = true)
            )),
            TextElement(textRun = TextRun(content = " and ")),
            TextElement(textRun = TextRun(
                content = "Italic",
                textElementStyle = TextElementStyle(italic = true)
            ))
        )

        val plainText = converter.convertElementsPlainText(elements)

        plainText shouldContain "Bold and Italic"
        plainText shouldNotContain "<"
        plainText shouldNotContain ">"
    }

    test("应该正确处理空元素列表") {
        val elements = emptyList<TextElement>()

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "<div></div>"
    }

    test("应该正确处理包含特殊字符的文本") {
        val elements = listOf(
            TextElement(textRun = TextRun(content = "<script>alert('xss')</script>"))
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        // kotlinx.html应该自动转义特殊字符
        html shouldContain "&lt;"
        html shouldContain "&gt;"
    }

    test("应该正确转换多个元素") {
        val elements = listOf(
            TextElement(textRun = TextRun(content = "Normal ")),
            TextElement(textRun = TextRun(
                content = "Bold",
                textElementStyle = TextElementStyle(bold = true)
            )),
            TextElement(textRun = TextRun(content = " and ")),
            TextElement(textRun = TextRun(
                content = "Italic",
                textElementStyle = TextElementStyle(italic = true)
            ))
        )

        val html = createHTML().div {
            converter.convertElements(elements, this)
        }

        html shouldContain "Normal"
        html shouldContain "<strong>Bold</strong>"
        html shouldContain "and"
        html shouldContain "<em>Italic</em>"
    }
})


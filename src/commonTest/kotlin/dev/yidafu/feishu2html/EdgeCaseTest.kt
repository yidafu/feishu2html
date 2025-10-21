package dev.yidafu.feishu2html

import dev.yidafu.feishu2html.toBlockNodes

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.HtmlBuilder
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class EdgeCaseTest : FunSpec({

    test("应该正确处理空文档") {
        val blocks = emptyList<Block>()
        val allBlocks = emptyMap<String, Block>()

        val builder = HtmlBuilder(title = "Empty", customCss = null)
        val html = builder.build(blocks.toBlockNodes())

        html shouldContain "<html"
        html shouldContain "Empty"
        html shouldContain "<body>"
        html shouldContain "</body>"
    }

    test("应该正确处理只有一个块的文档") {
        val block =
            TextBlock(
                blockId = "single",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Only one"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val blocks = listOf(block)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Single", customCss = null)
        val html = builder.build(blocks.toBlockNodes())

        html shouldContain "Only one"
    }

    test("应该正确转义HTML特殊字符") {
        val block =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "<script>alert('xss')</script>"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val blocks = listOf(block)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "XSS Test", customCss = null)
        val html = builder.build(blocks.toBlockNodes())

        html shouldContain "&lt;script&gt;"
        html shouldNotContain "<script>alert"
    }

    test("应该正确处理特殊Unicode字符") {
        val block =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "表情符号😀🎉中文字符测试"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val blocks = listOf(block)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Unicode Test", customCss = null)
        val html = builder.build(blocks.toBlockNodes())

        html shouldContain "表情符号😀🎉中文字符测试"
    }

    test("应该正确处理超长文本") {
        val longText = "A".repeat(10000)
        val block =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = longText))),
                        style = TextStyle(align = 1),
                    ),
            )

        val blocks = listOf(block)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Long Text Test", customCss = null)
        val html = builder.build(blocks.toBlockNodes())

        html shouldContain longText
    }

    test("应该正确处理深度嵌套块（10层）") {
        // 创建10层嵌套的块
        val blocks = mutableMapOf<String, Block>()
        var parentId = "page1"
        val childrenList = mutableListOf<Block>()

        for (i in 1..10) {
            val blockId = "nested_$i"
            val block =
                CalloutBlock(
                    blockId = blockId,
                    blockType = BlockType.CALLOUT,
                    children = if (i < 10) listOf("nested_${i + 1}") else emptyList(),
                    parentId = parentId,
                    callout = CalloutBlockData(backgroundColor = 1, borderColor = 1),
                )
            blocks[blockId] = block
            childrenList.add(block)
            parentId = blockId
        }

        val builder = HtmlBuilder(title = "Deep Nesting Test", customCss = null)
        val html = builder.build(childrenList.toBlockNodes())

        html shouldContain "callout"
    }

    test("应该正确处理包含所有元素类型的文本") {
        val elements =
            listOf(
                TextElement(textRun = TextRun(content = "Normal ")),
                TextElement(textRun = TextRun(content = "Bold", textElementStyle = TextElementStyle(bold = true))),
                TextElement(textRun = TextRun(content = " ")),
                TextElement(textRun = TextRun(content = "Italic", textElementStyle = TextElementStyle(italic = true))),
                TextElement(textRun = TextRun(content = " ")),
                TextElement(
                    textRun = TextRun(content = "Link", textElementStyle = TextElementStyle(link = Link(url = "https://example.com"))),
                ),
                TextElement(mentionUser = MentionUser(userId = "user123", textElementStyle = null)),
                TextElement(equation = InlineEquation(content = "E=mc^2")),
            )

        val block =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text = TextBlockData(elements = elements, style = TextStyle(align = 1)),
            )

        val blocks = listOf(block)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Mixed Elements Test", customCss = null)
        val html = builder.build(blocks.toBlockNodes())

        html shouldContain "Normal"
        html shouldContain "<strong>Bold</strong>"
        html shouldContain "<em>Italic</em>"
        html shouldContain "href=\"https://example.com\""
        html shouldContain "@用户"
        html shouldContain "E=mc^2"
    }

    test("应该正确处理空元素列表的块") {
        val block =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text = TextBlockData(elements = emptyList(), style = TextStyle(align = 1)),
            )

        val blocks = listOf(block)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Empty Elements Test", customCss = null)
        val html = builder.build(blocks.toBlockNodes())

        html shouldContain "<html"
    }

    test("应该正确处理缺失children的块") {
        val block =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = null,
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "No children"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val blocks = listOf(block)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Null Children Test", customCss = null)
        val html = builder.build(blocks.toBlockNodes())

        html shouldContain "No children"
    }

    test("应该正确处理大量块的文档（性能测试）") {
        val blocks =
            (1..100).map { i ->
                TextBlock(
                    blockId = "text_$i",
                    blockType = BlockType.TEXT,
                    children = emptyList(),
                    parentId = "page1",
                    text =
                        TextBlockData(
                            elements = listOf(TextElement(textRun = TextRun(content = "Text $i"))),
                            style = TextStyle(align = 1),
                        ),
                )
            }

        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Large Document Test", customCss = null)
        val html = builder.build(blocks.toBlockNodes())

        html shouldContain "Text 1"
        html shouldContain "Text 50"
        html shouldContain "Text 100"
    }

    test("应该正确处理混合的列表类型") {
        val blocks =
            listOf(
                BulletBlock(
                    blockId = "b1",
                    blockType = BlockType.BULLET,
                    children = emptyList(),
                    parentId = "page1",
                    bullet =
                        BulletBlockData(
                            elements = listOf(TextElement(textRun = TextRun(content = "Bullet"))),
                            style = TextStyle(align = 1),
                        ),
                ),
                OrderedBlock(
                    blockId = "o1",
                    blockType = BlockType.ORDERED,
                    children = emptyList(),
                    parentId = "page1",
                    ordered =
                        OrderedBlockData(
                            elements = listOf(TextElement(textRun = TextRun(content = "Ordered"))),
                            style = TextStyle(align = 1),
                        ),
                ),
            )

        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Mixed Lists Test", customCss = null)
        val html = builder.build(blocks.toBlockNodes())

        html shouldContain "bullet-list"
        html shouldContain "ordered-list"
    }

    test("应该正确处理所有对齐方式") {
        val blocks =
            listOf(
                TextBlock(
                    blockId = "left",
                    blockType = BlockType.TEXT,
                    children = emptyList(),
                    parentId = "page1",
                    text =
                        TextBlockData(
                            elements = listOf(TextElement(textRun = TextRun(content = "Left"))),
                            style = TextStyle(align = 1),
                        ),
                ),
                TextBlock(
                    blockId = "center",
                    blockType = BlockType.TEXT,
                    children = emptyList(),
                    parentId = "page1",
                    text =
                        TextBlockData(
                            elements = listOf(TextElement(textRun = TextRun(content = "Center"))),
                            style = TextStyle(align = 2),
                        ),
                ),
                TextBlock(
                    blockId = "right",
                    blockType = BlockType.TEXT,
                    children = emptyList(),
                    parentId = "page1",
                    text =
                        TextBlockData(
                            elements = listOf(TextElement(textRun = TextRun(content = "Right"))),
                            style = TextStyle(align = 3),
                        ),
                ),
            )

        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Alignment Test", customCss = null)
        val html = builder.build(blocks.toBlockNodes())

        html shouldContain "Left"
        html shouldContain "Center"
        html shouldContain "Right"
    }

    test("应该正确处理所有代码语言") {
        val languages = listOf(1, 2, 3, 4, 5) // PlainText, Java, JavaScript, Python, C
        val blocks =
            languages.map { lang ->
                CodeBlockItem(
                    blockId = "code_$lang",
                    blockType = BlockType.CODE,
                    children = emptyList(),
                    parentId = "page1",
                    code =
                        CodeBlockData(
                            elements = listOf(TextElement(textRun = TextRun(content = "code for lang $lang"))),
                            language = lang,
                        ),
                )
            }

        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Languages Test", customCss = null)
        val html = builder.build(blocks.toBlockNodes())

        html shouldContain "code for lang 1"
        html shouldContain "code for lang 5"
    }
})

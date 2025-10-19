package dev.yidafu.feishu2html.converter

import dev.yidafu.feishu2html.api.model.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain

class HtmlBuilderSimpleTest : FunSpec({

    test("应该生成完整的HTML文档结构") {
        val textBlock =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Test content"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val blocks = listOf(textBlock)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder =
            HtmlBuilder(
                title = "Test Document",
                customCss = null,
            )

        val html = builder.build(blocks, allBlocks)

        html shouldContain "<html"
        html shouldContain "<head>"
        html shouldContain "<title>Test Document</title>"
        html shouldContain "<body>"
        html shouldContain "Test content"
        html shouldContain "</body>"
        html shouldContain "</html>"
    }

    test("应该包含MathJax脚本") {
        val blocks = emptyList<Block>()
        val allBlocks = emptyMap<String, Block>()

        val builder =
            HtmlBuilder(
                title = "Test",
                customCss = null,
            )

        val html = builder.build(blocks, allBlocks)

        html shouldContain "MathJax"
        html shouldContain "tex-mml-chtml.js"
    }

    test("应该包含CSS样式") {
        val blocks = emptyList<Block>()
        val allBlocks = emptyMap<String, Block>()

        val builder =
            HtmlBuilder(
                title = "Test",
                cssMode = CssMode.INLINE,
                customCss = null,
            )

        val html = builder.build(blocks, allBlocks)

        html shouldContain "<style>"
        html shouldContain "</style>"
    }

    test("应该使用自定义CSS") {
        val customCss = "body { background: red; }"
        val blocks = emptyList<Block>()
        val allBlocks = emptyMap<String, Block>()

        val builder =
            HtmlBuilder(
                title = "Test",
                cssMode = CssMode.INLINE,
                customCss = customCss,
            )

        val html = builder.build(blocks, allBlocks)

        html shouldContain customCss
    }

    test("应该正确处理空块列表") {
        val blocks = emptyList<Block>()
        val allBlocks = emptyMap<String, Block>()

        val builder =
            HtmlBuilder(
                title = "Empty Document",
                customCss = null,
            )

        val html = builder.build(blocks, allBlocks)

        html shouldContain "<html"
        html shouldContain "Empty Document"
        html shouldContain "<body>"
        html shouldContain "</body>"
    }
})

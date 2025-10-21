package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.toBlockNode

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.RenderContext
import dev.yidafu.feishu2html.converter.TextElementConverter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import kotlinx.html.div
import kotlinx.html.stream.createHTML

class HeadingBlockRendererTest : FunSpec({

    val context =
        RenderContext(
            textConverter = TextElementConverter(),
        )

    test("应该正确渲染Heading1块") {
        val block =
            Heading1Block(
                blockId = "h1_1",
                blockType = BlockType.HEADING1,
                children = emptyList(),
                parentId = "page1",
                heading1 =
                    HeadingBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Heading 1"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val html =
            createHTML().div {
                Heading1BlockRenderer.render(this, block.toBlockNode(), context)
            }

        html shouldContain "<h1"
        html shouldContain "Heading 1"
        html shouldContain "</h1>"
    }

    test("应该正确渲染Heading2块") {
        val block =
            Heading2Block(
                blockId = "h2_1",
                blockType = BlockType.HEADING2,
                children = emptyList(),
                parentId = "page1",
                heading2 =
                    HeadingBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Heading 2"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val html =
            createHTML().div {
                Heading2BlockRenderer.render(this, block.toBlockNode(), context)
            }

        html shouldContain "<h2"
        html shouldContain "Heading 2"
        html shouldContain "</h2>"
    }

    test("应该正确渲染Heading3块") {
        val block =
            Heading3Block(
                blockId = "h3_1",
                blockType = BlockType.HEADING3,
                children = emptyList(),
                parentId = "page1",
                heading3 =
                    HeadingBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Heading 3"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val html =
            createHTML().div {
                Heading3BlockRenderer.render(this, block.toBlockNode(), context)
            }

        html shouldContain "<h3"
        html shouldContain "Heading 3"
        html shouldContain "</h3>"
    }

    test("应该处理空heading数据") {
        val block =
            Heading1Block(
                blockId = "h1_empty",
                blockType = BlockType.HEADING1,
                children = emptyList(),
                parentId = "page1",
                heading1 = null,
            )

        val html =
            createHTML().div {
                Heading1BlockRenderer.render(this, block.toBlockNode(), context)
            }

        html shouldContain "<div></div>"
    }

    test("应该正确应用对齐样式") {
        val blockCenter =
            Heading1Block(
                blockId = "h1_center",
                blockType = BlockType.HEADING1,
                children = emptyList(),
                parentId = "page1",
                heading1 =
                    HeadingBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Centered"))),
                        style = TextStyle(align = 2),
                    ),
            )

        val html =
            createHTML().div {
                Heading1BlockRenderer.render(this, blockCenter.toBlockNode(), context)
            }

        html shouldContain "Centered"
    }
})

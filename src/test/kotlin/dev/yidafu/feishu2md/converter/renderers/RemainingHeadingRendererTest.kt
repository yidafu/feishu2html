package dev.yidafu.feishu2md.converter.renderers

import dev.yidafu.feishu2md.api.model.*
import dev.yidafu.feishu2md.converter.RenderContext
import dev.yidafu.feishu2md.converter.TextElementConverter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import kotlinx.html.div
import kotlinx.html.stream.createHTML

class RemainingHeadingRendererTest : FunSpec({

    val context = RenderContext(
        textConverter = TextElementConverter(),
        processedBlocks = mutableSetOf()
    )

    test("应该正确渲染Heading4块") {
        val block = Heading4Block(
            blockId = "h4_1",
            blockType = BlockType.HEADING4,
            children = emptyList(),
            parentId = "page1",
            heading4 = HeadingBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Heading 4"))),
                style = TextStyle(align = 1)
            )
        )

        val html = createHTML().div {
            Heading4BlockRenderer.render(this, block, emptyMap(), context)
        }

        html shouldContain "<h4"
        html shouldContain "Heading 4"
    }

    test("应该正确渲染Heading5块") {
        val block = Heading5Block(
            blockId = "h5_1",
            blockType = BlockType.HEADING5,
            children = emptyList(),
            parentId = "page1",
            heading5 = HeadingBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Heading 5"))),
                style = TextStyle(align = 1)
            )
        )

        val html = createHTML().div {
            Heading5BlockRenderer.render(this, block, emptyMap(), context)
        }

        html shouldContain "<h5"
        html shouldContain "Heading 5"
    }

    test("应该正确渲染Heading6块") {
        val block = Heading6Block(
            blockId = "h6_1",
            blockType = BlockType.HEADING6,
            children = emptyList(),
            parentId = "page1",
            heading6 = HeadingBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Heading 6"))),
                style = TextStyle(align = 1)
            )
        )

        val html = createHTML().div {
            Heading6BlockRenderer.render(this, block, emptyMap(), context)
        }

        html shouldContain "<h6"
        html shouldContain "Heading 6"
    }

    test("应该正确渲染Heading7块") {
        val block = Heading7Block(
            blockId = "h7_1",
            blockType = BlockType.HEADING7,
            children = emptyList(),
            parentId = "page1",
            heading7 = HeadingBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Heading 7"))),
                style = TextStyle(align = 1)
            )
        )

        val html = createHTML().div {
            Heading7BlockRenderer.render(this, block, emptyMap(), context)
        }

        html shouldContain "Heading 7"
    }

    test("应该正确渲染Heading8块") {
        val block = Heading8Block(
            blockId = "h8_1",
            blockType = BlockType.HEADING8,
            children = emptyList(),
            parentId = "page1",
            heading8 = HeadingBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Heading 8"))),
                style = TextStyle(align = 1)
            )
        )

        val html = createHTML().div {
            Heading8BlockRenderer.render(this, block, emptyMap(), context)
        }

        html shouldContain "Heading 8"
    }

    test("应该正确渲染Heading9块") {
        val block = Heading9Block(
            blockId = "h9_1",
            blockType = BlockType.HEADING9,
            children = emptyList(),
            parentId = "page1",
            heading9 = HeadingBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Heading 9"))),
                style = TextStyle(align = 1)
            )
        )

        val html = createHTML().div {
            Heading9BlockRenderer.render(this, block, emptyMap(), context)
        }

        html shouldContain "Heading 9"
    }
})


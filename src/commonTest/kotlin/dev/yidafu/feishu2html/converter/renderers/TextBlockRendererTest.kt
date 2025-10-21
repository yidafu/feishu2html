package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.toBlockNode

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.RenderContext
import dev.yidafu.feishu2html.converter.TextElementConverter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import kotlinx.html.div
import kotlinx.html.stream.createHTML

class TextBlockRendererTest : FunSpec({

    val context =
        RenderContext(
            textConverter = TextElementConverter(),
        )

    test("应该正确渲染普通文本块") {
        val block =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Hello World"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val html =
            createHTML().div {
                TextBlockRenderer.render(this, block.toBlockNode(), context)
            }

        html shouldContain "<p"
        html shouldContain "Hello World"
        html shouldContain "</p>"
    }

    test("应该处理空文本数据") {
        val block =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text = null,
            )

        val html =
            createHTML().div {
                TextBlockRenderer.render(this, block.toBlockNode(), context)
            }

        html shouldContain "<div></div>"
    }

    test("应该正确应用文本对齐") {
        val blockLeft =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Left"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val blockCenter =
            TextBlock(
                blockId = "text2",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Center"))),
                        style = TextStyle(align = 2),
                    ),
            )

        val htmlLeft =
            createHTML().div {
                TextBlockRenderer.render(this, blockLeft.toBlockNode(), context)
            }

        val htmlCenter =
            createHTML().div {
                TextBlockRenderer.render(this, blockCenter.toBlockNode(), context)
            }

        htmlLeft shouldContain "Left"
        htmlCenter shouldContain "Center"
    }

    test("应该正确渲染多个文本元素") {
        val block =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements =
                            listOf(
                                TextElement(textRun = TextRun(content = "Normal ")),
                                TextElement(
                                    textRun =
                                        TextRun(
                                            content = "Bold",
                                            textElementStyle = TextElementStyle(bold = true),
                                        ),
                                ),
                            ),
                        style = TextStyle(align = 1),
                    ),
            )

        val html =
            createHTML().div {
                TextBlockRenderer.render(this, block.toBlockNode(), context)
            }

        html shouldContain "Normal"
        html shouldContain "Bold"
        html shouldContain "<strong>"
    }
})

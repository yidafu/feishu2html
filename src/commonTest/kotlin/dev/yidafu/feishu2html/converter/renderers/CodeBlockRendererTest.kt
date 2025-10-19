package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.RenderContext
import dev.yidafu.feishu2html.converter.TextElementConverter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import kotlinx.html.div
import kotlinx.html.stream.createHTML

class CodeBlockRendererTest : FunSpec({

    val context =
        RenderContext(
            textConverter = TextElementConverter(),
            processedBlocks = mutableSetOf(),
        )

    test("应该正确渲染代码块") {
        val block =
            CodeBlockItem(
                blockId = "code1",
                blockType = BlockType.CODE,
                children = emptyList(),
                parentId = "page1",
                code =
                    CodeBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "console.log('hello');"))),
                        language = 1,
                    ),
            )

        val html =
            createHTML().div {
                CodeBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "<pre"
        html shouldContain "<code"
        html shouldContain "console.log('hello');"
        html shouldContain "</code>"
        html shouldContain "</pre>"
    }

    test("应该处理空代码数据") {
        val block =
            CodeBlockItem(
                blockId = "code1",
                blockType = BlockType.CODE,
                children = emptyList(),
                parentId = "page1",
                code = null,
            )

        val html =
            createHTML().div {
                CodeBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "<div></div>"
    }

    test("应该正确应用语言类") {
        val block =
            CodeBlockItem(
                blockId = "code1",
                blockType = BlockType.CODE,
                children = emptyList(),
                parentId = "page1",
                code =
                    CodeBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "function test() {}"))),
                        language = 1, // JavaScript
                    ),
            )

        val html =
            createHTML().div {
                CodeBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "function test() {}"
        html shouldContain "<code"
    }

    test("应该正确渲染多行代码") {
        val block =
            CodeBlockItem(
                blockId = "code1",
                blockType = BlockType.CODE,
                children = emptyList(),
                parentId = "page1",
                code =
                    CodeBlockData(
                        elements =
                            listOf(
                                TextElement(textRun = TextRun(content = "line 1\n")),
                                TextElement(textRun = TextRun(content = "line 2\n")),
                                TextElement(textRun = TextRun(content = "line 3")),
                            ),
                        language = 1,
                    ),
            )

        val html =
            createHTML().div {
                CodeBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "line 1"
        html shouldContain "line 2"
        html shouldContain "line 3"
    }
})

package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.RenderContext
import dev.yidafu.feishu2html.converter.TextElementConverter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import kotlinx.html.div
import kotlinx.html.stream.createHTML

class BasicBlockRendererTest : FunSpec({

    val context =
        RenderContext(
            textConverter = TextElementConverter(),
        )

    test("应该正确渲染Divider块") {
        val block =
            DividerBlock(
                blockId = "divider1",
                blockType = BlockType.DIVIDER,
                children = emptyList(),
                parentId = "page1",
                divider = null,
            )

        val html =
            createHTML().div {
                DividerBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "<hr"
    }

    test("应该正确渲染Quote块") {
        val block =
            QuoteBlock(
                blockId = "quote1",
                blockType = BlockType.QUOTE,
                children = emptyList(),
                parentId = "page1",
                quote =
                    QuoteBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Quote text"))),
                    ),
            )

        val html =
            createHTML().div {
                QuoteBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "<blockquote"
        html shouldContain "Quote text"
        html shouldContain "</blockquote>"
    }

    test("应该正确渲染Equation块") {
        val block =
            EquationBlock(
                blockId = "eq1",
                blockType = BlockType.EQUATION,
                children = emptyList(),
                parentId = "page1",
                equation = EquationBlockData(content = "E = mc^2"),
            )

        val html =
            createHTML().div {
                EquationBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "E = mc^2"
    }

    test("应该正确渲染Todo块") {
        val block =
            TodoBlock(
                blockId = "todo1",
                blockType = BlockType.TODO,
                children = emptyList(),
                parentId = "page1",
                todo =
                    TodoBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Todo item"))),
                        style = TodoStyle(done = false),
                    ),
            )

        val html =
            createHTML().div {
                TodoBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "Todo item"
        html shouldContain "checkbox"
    }

    test("应该正确渲染已完成的Todo块") {
        val block =
            TodoBlock(
                blockId = "todo2",
                blockType = BlockType.TODO,
                children = emptyList(),
                parentId = "page1",
                todo =
                    TodoBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Done item"))),
                        style = TodoStyle(done = true),
                    ),
            )

        val html =
            createHTML().div {
                TodoBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "Done item"
        html shouldContain "checked"
    }

    test("应该处理空Quote数据") {
        val block =
            QuoteBlock(
                blockId = "quote_empty",
                blockType = BlockType.QUOTE,
                children = emptyList(),
                parentId = "page1",
                quote = null,
            )

        val html =
            createHTML().div {
                QuoteBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "<div></div>"
    }

    test("应该处理空Equation数据") {
        val block =
            EquationBlock(
                blockId = "eq_empty",
                blockType = BlockType.EQUATION,
                children = emptyList(),
                parentId = "page1",
                equation = null,
            )

        val html =
            createHTML().div {
                EquationBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "<div></div>"
    }
})

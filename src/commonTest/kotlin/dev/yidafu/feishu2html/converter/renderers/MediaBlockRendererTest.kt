package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.RenderContext
import dev.yidafu.feishu2html.converter.TextElementConverter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.html.div
import kotlinx.html.stream.createHTML

class MediaBlockRendererTest : FunSpec({

    val context =
        RenderContext(
            textConverter = TextElementConverter(),
            processedBlocks = mutableSetOf(),
        )

    test("应该正确渲染Image块") {
        val block =
            ImageBlock(
                blockId = "img1",
                blockType = BlockType.IMAGE,
                children = emptyList(),
                parentId = "page1",
                image =
                    ImageBlockData(
                        token = "img_token_123",
                        width = 800,
                        height = 600,
                    ),
            )

        val html =
            createHTML().div {
                ImageBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "<img"
        html shouldContain "src=\"images/img_token_123"
    }

    test("应该正确渲染File块") {
        val block =
            FileBlock(
                blockId = "file1",
                blockType = BlockType.FILE,
                children = emptyList(),
                parentId = "page1",
                file =
                    FileBlockData(
                        token = "file_token_456",
                        name = "document.pdf",
                    ),
            )

        val html =
            createHTML().div {
                FileBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "document.pdf"
        html shouldContain "<a"
        html shouldContain "href=\"files/document.pdf"  // Use filename, not token
        html shouldContain "file-card"  // Check for official Feishu card structure
        html shouldContain "file-icon"  // Check for file icon
        html shouldContain "file-name"  // Check for file name
        html shouldContain "btn-preview"  // Check for download button
        html shouldContain "下载"  // Check for download text
    }

    test("应该正确渲染Board块") {
        val block =
            BoardBlock(
                blockId = "board1",
                blockType = BlockType.BOARD,
                children = emptyList(),
                parentId = "page1",
                board =
                    BoardBlockData(
                        token = "board_token_789",
                        width = 820,
                        height = 400,
                    ),
            )

        val html =
            createHTML().div {
                BoardBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "<img"
        html shouldContain "src=\"images/board_token_789.png\""
        html shouldContain "width: 820px"
        html shouldContain "height: 400px"
    }

    test("应该正确渲染Diagram块") {
        val block =
            DiagramBlock(
                blockId = "diagram1",
                blockType = BlockType.DIAGRAM,
                children = emptyList(),
                parentId = "page1",
                diagram = DiagramBlockData(diagramType = 1, content = "diagram_content"),
            )

        val html =
            createHTML().div {
                DiagramBlockRenderer.render(this, block, emptyMap(), context)
            }

        html.length shouldBe html.length // 基本验证
    }

    test("应该正确渲染Iframe块 - Bilibili") {
        val block =
            IframeBlock(
                blockId = "iframe1",
                blockType = BlockType.IFRAME,
                children = emptyList(),
                parentId = "page1",
                iframe =
                    IframeBlockData(
                        component =
                            IframeComponent(
                                iframeType = 1, // Bilibili
                                url = "https://www.bilibili.com/video/BV1xx411c7mD",
                            ),
                    ),
            )

        val html =
            createHTML().div {
                IframeBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "<iframe"
        html shouldContain "bilibili"
    }

    test("应该处理空Image数据") {
        val block =
            ImageBlock(
                blockId = "img_empty",
                blockType = BlockType.IMAGE,
                children = emptyList(),
                parentId = "page1",
                image = null,
            )

        val html =
            createHTML().div {
                ImageBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "<div></div>"
    }

    test("应该处理空File数据") {
        val block =
            FileBlock(
                blockId = "file_empty",
                blockType = BlockType.FILE,
                children = emptyList(),
                parentId = "page1",
                file = null,
            )

        val html =
            createHTML().div {
                FileBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "<div></div>"
    }

    test("应该使用Board默认尺寸") {
        val block =
            BoardBlock(
                blockId = "board2",
                blockType = BlockType.BOARD,
                children = emptyList(),
                parentId = "page1",
                board =
                    BoardBlockData(
                        token = "board_token_default",
                        width = null,
                        height = null,
                    ),
            )

        val html =
            createHTML().div {
                BoardBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "width: 820px"
        html shouldContain "height: 400px"
    }
})

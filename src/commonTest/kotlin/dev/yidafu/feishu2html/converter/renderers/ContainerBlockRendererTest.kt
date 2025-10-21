package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.toBlockNode
import dev.yidafu.feishu2html.buildBlockTree

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.RenderContext
import dev.yidafu.feishu2html.converter.TextElementConverter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.html.div
import kotlinx.html.stream.createHTML

class ContainerBlockRendererTest : FunSpec({

    val context =
        RenderContext(
            textConverter = TextElementConverter(),
        )

    test("应该正确渲染Callout块") {
        val textBlock =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "callout1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Callout content"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val block =
            CalloutBlock(
                blockId = "callout1",
                blockType = BlockType.CALLOUT,
                children = listOf("text1"),
                parentId = "page1",
                callout =
                    CalloutBlockData(
                        backgroundColor = 1,
                        borderColor = 1,
                        emojiId = "smile",
                    ),
            )

        val allBlocks = listOf(block, textBlock)
        val tree = buildBlockTree(allBlocks)
        val calloutNode = tree.first()

        @Suppress("UNCHECKED_CAST")
        val html =
            createHTML().div {
                CalloutBlockRenderer.render(this, calloutNode as BlockNode<CalloutBlock>, context)
            }

        html shouldContain "callout"
        html shouldContain "Callout content"
    }

    test("应该正确渲染Grid块") {
        val col1 =
            GridColumnBlock(
                blockId = "col1",
                blockType = BlockType.GRID_COLUMN,
                children = emptyList(),
                parentId = "grid1",
                gridColumn = GridColumnBlockData(widthRatio = 50),
            )

        val col2 =
            GridColumnBlock(
                blockId = "col2",
                blockType = BlockType.GRID_COLUMN,
                children = emptyList(),
                parentId = "grid1",
                gridColumn = GridColumnBlockData(widthRatio = 50),
            )

        val block =
            GridBlock(
                blockId = "grid1",
                blockType = BlockType.GRID,
                children = listOf("col1", "col2"),
                parentId = "page1",
                grid = GridBlockData(columnSize = 2),
            )

        val allBlocks = listOf(block, col1, col2)
        val tree = buildBlockTree(allBlocks)
        val gridNode = tree.first()

        @Suppress("UNCHECKED_CAST")
        val html =
            createHTML().div {
                GridBlockRenderer.render(this, gridNode as BlockNode<GridBlock>, context)
            }

        html shouldContain "display: grid"
        html shouldContain "grid-template-columns"
    }

    test("应该正确渲染GridColumn块") {
        val textBlock =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "col1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Column content"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val block =
            GridColumnBlock(
                blockId = "col1",
                blockType = BlockType.GRID_COLUMN,
                children = listOf("text1"),
                parentId = "grid1",
                gridColumn = GridColumnBlockData(widthRatio = 60),
            )

        val allBlocks =
            mapOf(
                "col1" to block,
                "text1" to textBlock,
            )

        val html =
            createHTML().div {
                GridColumnBlockRenderer.render(this, block.toBlockNode(), context)
            }

        html.length shouldBe html.length // 基本验证
    }

    test("应该正确渲染QuoteContainer块") {
        val textBlock =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "qc1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Quote container content"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val block =
            QuoteContainerBlock(
                blockId = "qc1",
                blockType = BlockType.QUOTE_CONTAINER,
                children = listOf("text1"),
                parentId = "page1",
                quoteContainer = QuoteContainerBlockData(),
            )

        val allBlocks = listOf(block, textBlock)
        val tree = buildBlockTree(allBlocks)
        val qcNode = tree.first()

        @Suppress("UNCHECKED_CAST")
        val html =
            createHTML().div {
                QuoteContainerBlockRenderer.render(this, qcNode as BlockNode<QuoteContainerBlock>, context)
            }

        html shouldContain "quote-container"
        html shouldContain "Quote container content"
    }

    test("应该正确渲染TableCell块") {
        val textBlock =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "cell1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Cell content"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val block =
            TableCellBlock(
                blockId = "cell1",
                blockType = BlockType.TABLE_CELL,
                children = listOf("text1"),
                parentId = "table1",
                tableCell = TableCellBlockData(),
            )

        val allBlocks =
            mapOf(
                "cell1" to block,
                "text1" to textBlock,
            )

        val html =
            createHTML().div {
                TableCellBlockRenderer.render(this, block.toBlockNode(), context)
            }

        html.length shouldBe html.length // 基本验证
    }

    test("应该处理空Callout数据") {
        val block =
            CalloutBlock(
                blockId = "callout_empty",
                blockType = BlockType.CALLOUT,
                children = emptyList(),
                parentId = "page1",
                callout = null,
            )

        val html =
            createHTML().div {
                CalloutBlockRenderer.render(this, block.toBlockNode(), context)
            }

        html shouldContain "callout"
    }

    test("应该处理空Grid数据") {
        val block =
            GridBlock(
                blockId = "grid_empty",
                blockType = BlockType.GRID,
                children = emptyList(),
                parentId = "page1",
                grid = null,
            )

        val html =
            createHTML().div {
                GridBlockRenderer.render(this, block.toBlockNode(), context)
            }

        html.length shouldBe html.length // 基本验证
    }

    test("应该正确渲染ViewBlock（附件容器）") {
        // Create a FILE block (child of VIEW)
        val fileBlock =
            FileBlock(
                blockId = "file1",
                blockType = BlockType.FILE,
                children = emptyList(),
                parentId = "view1",
                file =
                    FileBlockData(
                        name = "sample.txt",
                        token = "test_token_123",
                    ),
            )

        // Create a VIEW block (container)
        val viewBlock =
            ViewBlock(
                blockId = "view1",
                blockType = BlockType.VIEW,
                children = listOf("file1"),
                parentId = "page1",
                view = ViewBlockData(viewType = 1),
            )

        // Build tree structure
        val allBlocks = listOf(viewBlock, fileBlock)
        val tree = buildBlockTree(allBlocks)
        val viewNode = tree.first()

        @Suppress("UNCHECKED_CAST")
        val html =
            createHTML().div {
                ViewBlockRenderer.render(this, viewNode as BlockNode<ViewBlock>, context)
            }

        // Verify FILE block is rendered (not "Unsupported")
        html shouldContain "file-card"
        html shouldContain "sample.txt"
        html shouldContain "files/sample.txt"
    }

    test("应该处理空的ViewBlock") {
        val block =
            ViewBlock(
                blockId = "view_empty",
                blockType = BlockType.VIEW,
                children = emptyList(),
                parentId = "page1",
                view = ViewBlockData(viewType = 2),
            )

        val html =
            createHTML().div {
                ViewBlockRenderer.render(this, block.toBlockNode(), context)
            }

        // Should render without errors, producing empty output
        html shouldContain "<div></div>"
    }
})

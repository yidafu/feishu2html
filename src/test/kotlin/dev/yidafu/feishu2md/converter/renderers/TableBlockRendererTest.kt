package dev.yidafu.feishu2md.converter.renderers

import dev.yidafu.feishu2md.api.model.*
import dev.yidafu.feishu2md.converter.RenderContext
import dev.yidafu.feishu2md.converter.TextElementConverter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import kotlinx.html.div
import kotlinx.html.stream.createHTML

class TableBlockRendererTest : FunSpec({

    val context = RenderContext(
        textConverter = TextElementConverter(),
        processedBlocks = mutableSetOf()
    )

    test("应该正确渲染表格") {
        val cellBlock = TableCellBlock(
            blockId = "cell1",
            blockType = BlockType.TABLE_CELL,
            children = listOf("text1"),
            parentId = "table1",
            tableCell = TableCellBlockData()
        )

        val textBlock = TextBlock(
            blockId = "text1",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = "cell1",
            text = TextBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Cell content"))),
                style = TextStyle(align = 1)
            )
        )

        val tableBlock = TableBlock(
            blockId = "table1",
            blockType = BlockType.TABLE,
            children = listOf("cell1"),
            parentId = "page1",
            table = TableBlockData(
                cells = listOf("cell1"),
                property = TableProperty(rowSize = 1, columnSize = 1)
            )
        )

        val allBlocks = mapOf(
            "table1" to tableBlock,
            "cell1" to cellBlock,
            "text1" to textBlock
        )

        val html = createHTML().div {
            TableBlockRenderer.render(this, tableBlock, allBlocks, context)
        }

        html shouldContain "<table"
        html shouldContain "<tr"
        html shouldContain "<td"
        html shouldContain "Cell content"
        html shouldContain "</table>"
    }

    test("应该处理空表格") {
        val tableBlock = TableBlock(
            blockId = "table1",
            blockType = BlockType.TABLE,
            children = emptyList(),
            parentId = "page1",
            table = null
        )

        val html = createHTML().div {
            TableBlockRenderer.render(this, tableBlock, emptyMap(), context)
        }

        html shouldContain "<div></div>"
    }

    test("应该正确处理多行多列表格") {
        val cells = mutableMapOf<String, Block>()
        val cellIds = mutableListOf<String>()

        for (row in 0 until 2) {
            for (col in 0 until 2) {
                val cellId = "cell_${row}_${col}"
                val textId = "text_${row}_${col}"
                cellIds.add(cellId)

                cells[cellId] = TableCellBlock(
                    blockId = cellId,
                    blockType = BlockType.TABLE_CELL,
                    children = listOf(textId),
                    parentId = "table1",
                    tableCell = TableCellBlockData()
                )

                cells[textId] = TextBlock(
                    blockId = textId,
                    blockType = BlockType.TEXT,
                    children = emptyList(),
                    parentId = cellId,
                    text = TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "R${row}C${col}"))),
                        style = TextStyle(align = 1)
                    )
                )
            }
        }

        val tableBlock = TableBlock(
            blockId = "table1",
            blockType = BlockType.TABLE,
            children = cellIds,
            parentId = "page1",
            table = TableBlockData(
                cells = cellIds,
                property = TableProperty(rowSize = 2, columnSize = 2)
            )
        )

        val allBlocks = cells + ("table1" to tableBlock)

        val html = createHTML().div {
            TableBlockRenderer.render(this, tableBlock, allBlocks, context)
        }

        html shouldContain "<table"
        html shouldContain "R0C0"
        html shouldContain "R0C1"
        html shouldContain "R1C0"
        html shouldContain "R1C1"
    }
})


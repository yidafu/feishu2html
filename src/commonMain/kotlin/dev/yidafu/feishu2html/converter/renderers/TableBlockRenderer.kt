package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

internal object TableBlockRenderer : Renderable<TableBlock> {
    private val logger = KotlinLogging.logger {}

    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<TableBlock>,
        context: RenderContext,
    ) {
        val tableBlock = blockNode.data
        val property = tableBlock.table?.property ?: return
        val cellNodes = blockNode.children.filter { it.data is TableCellBlock }

        logger.debug {
            "Rendering table: ${property.rowSize}x${property.columnSize} (${cellNodes.size} cells)"
        }

        parent.table(classes = "table-block") {
            var currentRowCells = mutableListOf<BlockNode<out Block>>()

            for (cellNode in cellNodes) {
                currentRowCells.add(cellNode)

                if (currentRowCells.size >= property.columnSize) {
                    tr {
                        currentRowCells.forEach { cellNode ->
                            td {
                                renderCellContent(cellNode, context, this)
                            }
                        }
                    }
                    currentRowCells.clear()
                }
            }

            if (currentRowCells.isNotEmpty()) {
                tr {
                    currentRowCells.forEach { cellNode ->
                        td {
                            renderCellContent(cellNode, context, this)
                        }
                    }
                }
            }
        }
    }

    private fun renderCellContent(
        cellNode: BlockNode<out Block>,
        context: RenderContext,
        parent: FlowContent,
    ) {
        val cellBlock = cellNode.data as TableCellBlock
        val elements = cellBlock.tableCell?.elements ?: emptyList()
        logger.debug {
            "Rendering table cell: ID=${cellBlock.blockId}, elements=${elements.size}, children=${cellNode.children.size}"
        }

        if (elements.isNotEmpty()) {
            context.textConverter.convertElements(elements, parent)
        } else if (cellNode.children.isNotEmpty()) {
            cellNode.renderChildren(parent, context)
        }
    }
}

internal object TableCellBlockRenderer : Renderable<TableCellBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<TableCellBlock>,
        context: RenderContext,
    ) {
        // TableCell 由 TableBlockRenderer 处理，这里不做任何事
    }
}

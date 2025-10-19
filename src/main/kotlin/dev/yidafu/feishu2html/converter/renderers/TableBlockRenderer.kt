package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import org.slf4j.LoggerFactory

internal object TableBlockRenderer : Renderable {
    private val logger = LoggerFactory.getLogger(TableBlockRenderer::class.java)

    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val tableBlock = block as TableBlock
        val property = tableBlock.table?.property ?: return
        val children = tableBlock.children ?: return

        logger.debug("Rendering table: {}x{} ({}cells)",
            property.rowSize, property.columnSize, children.size)

        parent.table {
            var currentRowCells = mutableListOf<TableCellBlock>()

            for (childId in children) {
                val cell = allBlocks[childId] as? TableCellBlock ?: continue
                currentRowCells.add(cell)

                if (currentRowCells.size >= property.columnSize) {
                    tr {
                        currentRowCells.forEach { cellBlock ->
                            td {
                                renderCellContent(cellBlock, allBlocks, context, this)
                            }
                        }
                    }
                    currentRowCells.clear()
                }
            }

            if (currentRowCells.isNotEmpty()) {
                tr {
                    currentRowCells.forEach { cellBlock ->
                        td {
                            renderCellContent(cellBlock, allBlocks, context, this)
                        }
                    }
                }
            }
        }

        // 标记子块为已处理
        children.forEach { cellId ->
            context.processedBlocks.add(cellId)
            val cell = allBlocks[cellId] as? TableCellBlock
            cell?.children?.forEach { context.processedBlocks.add(it) }
        }
    }

    private fun renderCellContent(
        cellBlock: TableCellBlock,
        allBlocks: Map<String, Block>,
        context: RenderContext,
        parent: FlowContent,
    ) {
        val elements = cellBlock.tableCell?.elements ?: emptyList()
        val children = cellBlock.children
        logger.debug("渲染表格单元格: ID=${cellBlock.blockId}, elements=${elements.size}, children=${children?.size ?: 0}")

        if (elements.isNotEmpty()) {
            context.textConverter.convertElements(elements, parent)
        } else if (children != null && children.isNotEmpty()) {
            children.forEach { childId ->
                val childBlock = allBlocks[childId]
                if (childBlock != null) {
                    renderBlock(childBlock, parent, allBlocks, context)
                }
            }
        }
    }
}

internal object TableCellBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        // TableCell 由 TableBlockRenderer 处理，这里不做任何事
    }
}

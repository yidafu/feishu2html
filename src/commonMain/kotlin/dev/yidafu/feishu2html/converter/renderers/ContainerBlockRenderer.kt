package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal object QuoteContainerBlockRenderer : Renderable<QuoteContainerBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<QuoteContainerBlock>,
        context: RenderContext,
    ) {
        val quoteContainer = blockNode.data
        logger.debug { "Rendering quote container with ${blockNode.children.size} children" }
        parent.blockQuote(classes = "quote-container-block") {
            blockNode.renderChildren(this, context)
        }
    }
}

internal object GridBlockRenderer : Renderable<GridBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<GridBlock>,
        context: RenderContext,
    ) {
        val gridBlock = blockNode.data
        logger.debug { "Rendering grid layout with ${blockNode.children.size} children" }

        // Extract grid columns from children
        val columnNodes = blockNode.children.filter { it.data is GridColumnBlock }
        val columns = columnNodes.map { columnNode ->
            val columnBlock = columnNode.data as GridColumnBlock
            val widthRatio = columnBlock.gridColumn?.widthRatio ?: 1
            columnNode to widthRatio
        }

        val gridTemplate = columns.joinToString(" ") { "${it.second}fr" }
        logger.debug { "Grid has ${columns.size} columns with template: $gridTemplate" }

        parent.div(classes = "grid-layout") {
            style = "display: grid; grid-template-columns: $gridTemplate; gap: 20px;"

            columns.forEach { (columnNode, _) ->
                div(classes = "grid-column") {
                    columnNode.renderChildren(this, context)
                }
            }
        }
    }
}

internal object GridColumnBlockRenderer : Renderable<GridColumnBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<GridColumnBlock>,
        context: RenderContext,
    ) {
        // GridColumn 由 GridBlockRenderer 处理
    }
}

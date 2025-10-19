package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal object QuoteContainerBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val quoteContainer = block as QuoteContainerBlock
        logger.debug { "Rendering quote container with ${quoteContainer.children?.size ?: 0} children" }
        parent.blockQuote(classes = "quote-container-block") {
            quoteContainer.children?.forEach { childId ->
                val childBlock = allBlocks[childId]
                if (childBlock != null) {
                    context.processedBlocks.add(childId)
                    renderBlock(childBlock, this, allBlocks, context)
                }
            }
        }
    }
}

internal object GridBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val gridBlock = block as GridBlock
        logger.debug { "Rendering grid layout with ${gridBlock.children?.size ?: 0} children" }
        val columns = mutableListOf<Pair<GridColumnBlock, Int>>()
        gridBlock.children?.forEach { childId ->
            val childBlock = allBlocks[childId]
            if (childBlock is GridColumnBlock) {
                val widthRatio = childBlock.gridColumn?.widthRatio ?: 1
                columns.add(childBlock to widthRatio)
            }
        }

        val gridTemplate = columns.joinToString(" ") { "${it.second}fr" }
        logger.debug { "Grid has ${columns.size} columns with template: $gridTemplate" }

        parent.div(classes = "grid-layout") {
            style = "display: grid; grid-template-columns: $gridTemplate; gap: 20px;"

            columns.forEach { (columnBlock, _) ->
                div(classes = "grid-column") {
                    columnBlock.children?.forEach { childId ->
                        val childBlock = allBlocks[childId]
                        if (childBlock != null) {
                            context.processedBlocks.add(childId)
                            renderBlock(childBlock, this, allBlocks, context)
                        }
                    }
                }
                context.processedBlocks.add(columnBlock.blockId)
            }
        }
    }
}

internal object GridColumnBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        // GridColumn 由 GridBlockRenderer 处理
    }
}

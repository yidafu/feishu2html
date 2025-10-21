package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal object BitableBlockRenderer : Renderable<BitableBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<BitableBlock>,
        context: RenderContext,
    ) {
        logger.warn { "Rendering partially supported block: Bitable (block_id: ${blockNode.data.blockId})" }
        // Bitable not yet supported - optionally show warning
        if (context.showUnsupportedBlocks) {
            parent.div(classes = "unsupported-block") {
                +"[Partially supported: Bitable block]"
            }
        }
    }
}

internal object ChatCardBlockRenderer : Renderable<ChatCardBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<ChatCardBlock>,
        context: RenderContext,
    ) {
        logger.warn { "Rendering partially supported block: ChatCard (block_id: ${blockNode.data.blockId})" }
        // ChatCard not yet supported - optionally show warning
        if (context.showUnsupportedBlocks) {
            parent.div(classes = "unsupported-block") {
                +"[Partially supported: ChatCard block]"
            }
        }
    }
}

internal object UnknownBlockRenderer : Renderable<UnknownBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<UnknownBlock>,
        context: RenderContext,
    ) {
        val unknownBlock = blockNode.data
        logger.warn { "Rendering unknown block type: UNDEFINED (block_id: ${unknownBlock.blockId})" }
        // UNDEFINED may be other unknown block types
        val quoteData = unknownBlock.quote
        if (quoteData != null) {
            logger.debug { "Unknown block contains quote data, rendering as blockquote" }
            parent.blockQuote {
                context.textConverter.convertElements(quoteData.elements, this)
            }
        } else if (context.showUnsupportedBlocks) {
            // Only show warning if no special handling and showUnsupportedBlocks is enabled
            parent.div(classes = "unsupported-block") {
                +"[Unknown block type: UNDEFINED]"
            }
        }
    }
}

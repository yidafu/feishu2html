package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("dev.yidafu.feishu2html.converter.renderers.OtherBlocksRenderer")

internal object BitableBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        logger.warn("Rendering partially supported block: Bitable (block_id: {})", (block as Block).blockId)
        // Bitable not yet supported
    }
}

internal object ChatCardBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        logger.warn("Rendering partially supported block: ChatCard (block_id: {})", (block as Block).blockId)
        // ChatCard not yet supported
    }
}

internal object UnknownBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val unknownBlock = block as UnknownBlock
        logger.warn("Rendering unknown block type: UNDEFINED (block_id: {})", unknownBlock.blockId)
        // UNDEFINED may be other unknown block types
        val quoteData = unknownBlock.quote
        if (quoteData != null) {
            logger.debug("Unknown block contains quote data, rendering as blockquote")
            parent.blockQuote {
                context.textConverter.convertElements(quoteData.elements, this)
            }
        }
    }
}


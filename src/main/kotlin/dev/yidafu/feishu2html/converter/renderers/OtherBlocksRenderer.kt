package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("dev.yidafu.feishu2html.converter.renderers.OtherBlocksRenderer")

object BitableBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        logger.warn("Rendering partially supported block: Bitable (block_id: {})", (block as Block).blockId)
        // Bitable暂不支持
    }
}

object ChatCardBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        logger.warn("Rendering partially supported block: ChatCard (block_id: {})", (block as Block).blockId)
        // ChatCard暂不支持
    }
}

object UnknownBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val unknownBlock = block as UnknownBlock
        logger.warn("Rendering unknown block type: UNDEFINED (block_id: {})", unknownBlock.blockId)
        // UNDEFINED 可能是其他未知块类型
        val quoteData = unknownBlock.quote
        if (quoteData != null) {
            logger.debug("Unknown block contains quote data, rendering as blockquote")
            parent.blockQuote {
                context.textConverter.convertElements(quoteData.elements, this)
            }
        }
    }
}


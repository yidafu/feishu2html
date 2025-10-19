package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*

object BitableBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
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
        // UNDEFINED 可能是其他未知块类型
        val quoteData = unknownBlock.quote
        if (quoteData != null) {
            parent.blockQuote {
                context.textConverter.convertElements(quoteData.elements, this)
            }
        }
    }
}


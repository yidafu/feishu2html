package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*

object BulletBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val bulletBlock = block as BulletBlock
        val elements = bulletBlock.bullet?.elements ?: return
        if (parent is UL) {
            parent.li {
                context.textConverter.convertElements(elements, this)
            }
        }
    }
}

object OrderedBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val orderedBlock = block as OrderedBlock
        val elements = orderedBlock.ordered?.elements ?: return
        if (parent is OL) {
            parent.li {
                context.textConverter.convertElements(elements, this)
            }
        }
    }
}

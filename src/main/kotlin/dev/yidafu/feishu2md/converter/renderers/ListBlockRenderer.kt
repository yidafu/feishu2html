package dev.yidafu.feishu2md.converter.renderers

import dev.yidafu.feishu2md.api.model.*
import dev.yidafu.feishu2md.converter.*
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

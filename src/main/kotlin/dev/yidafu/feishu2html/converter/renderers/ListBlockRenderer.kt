package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("dev.yidafu.feishu2html.converter.renderers.ListBlockRenderer")

internal object BulletBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val bulletBlock = block as BulletBlock
        val elements = bulletBlock.bullet?.elements ?: return
        logger.debug("Rendering bullet list item with {} elements", elements.size)
        if (parent is UL) {
            parent.li {
                context.textConverter.convertElements(elements, this)
            }
        }
    }
}

internal object OrderedBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val orderedBlock = block as OrderedBlock
        val elements = orderedBlock.ordered?.elements ?: return
        logger.debug("Rendering ordered list item with {} elements", elements.size)
        if (parent is OL) {
            parent.li {
                context.textConverter.convertElements(elements, this)
            }
        }
    }
}

package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import org.slf4j.LoggerFactory

internal object TextBlockRenderer : Renderable {
    private val logger = LoggerFactory.getLogger(TextBlockRenderer::class.java)

    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val textBlock = block as TextBlock
        val elements = textBlock.text?.elements ?: return
        logger.debug("Rendering text block with {} elements", elements.size)
        val alignClass = getAlignClass(textBlock.text?.style?.align)

        parent.p(classes = alignClass.takeIf { it.isNotEmpty() }) {
            context.textConverter.convertElements(elements, this)
        }
    }
}

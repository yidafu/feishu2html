package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * PageBlock Renderer - Page blocks are container blocks that don't render themselves
 *
 * PageBlock acts as the root container in a document hierarchy. It doesn't produce
 * visible HTML output, but its children are rendered by the main rendering loop.
 */
internal object PageBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        // PageBlock is a container but doesn't render itself
        // Its children are rendered by the main buildBody loop
    }
}

internal object TextBlockRenderer : Renderable {
    private val logger = KotlinLogging.logger {}

    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val textBlock = block as TextBlock
        val elements = textBlock.text?.elements ?: return
        logger.debug { "Rendering text block with ${elements.size} elements" }
        val alignClass = getAlignClass(textBlock.text?.style?.align)
        val textClass = "text-block" + if (alignClass.isNotEmpty()) " $alignClass" else ""

        parent.p(classes = textClass) {
            context.textConverter.convertElements(elements, this)
        }
    }
}

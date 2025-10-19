package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("dev.yidafu.feishu2html.converter.renderers.HeadingBlockRenderer")

internal object Heading1BlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val heading = block as Heading1Block
        renderHeading(heading.heading1, 1, parent, context)
    }
}

internal object Heading2BlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val heading = block as Heading2Block
        renderHeading(heading.heading2, 2, parent, context)
    }
}

internal object Heading3BlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val heading = block as Heading3Block
        renderHeading(heading.heading3, 3, parent, context)
    }
}

internal object Heading4BlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val heading = block as Heading4Block
        renderHeading(heading.heading4, 4, parent, context)
    }
}

internal object Heading5BlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val heading = block as Heading5Block
        renderHeading(heading.heading5, 5, parent, context)
    }
}

internal object Heading6BlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val heading = block as Heading6Block
        renderHeading(heading.heading6, 6, parent, context)
    }
}

internal object Heading7BlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val heading = block as Heading7Block
        renderHeading(heading.heading7, 7, parent, context)
    }
}

internal object Heading8BlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val heading = block as Heading8Block
        renderHeading(heading.heading8, 8, parent, context)
    }
}

internal object Heading9BlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val heading = block as Heading9Block
        renderHeading(heading.heading9, 9, parent, context)
    }
}

private fun renderHeading(
    headingData: HeadingBlockData?,
    level: Int,
    parent: FlowContent,
    context: RenderContext,
) {
    val elements = headingData?.elements ?: return
    logger.debug("Rendering heading level {} with {} elements", level, elements.size)
    val alignClass = getAlignClass(headingData.style?.align)

    when (level) {
        1 ->
            parent.h1(classes = alignClass.takeIf { it.isNotEmpty() }) {
                context.textConverter.convertElements(elements, this)
            }
        2 ->
            parent.h2(classes = alignClass.takeIf { it.isNotEmpty() }) {
                context.textConverter.convertElements(elements, this)
            }
        3 ->
            parent.h3(classes = alignClass.takeIf { it.isNotEmpty() }) {
                context.textConverter.convertElements(elements, this)
            }
        4 ->
            parent.h4(classes = alignClass.takeIf { it.isNotEmpty() }) {
                context.textConverter.convertElements(elements, this)
            }
        5 ->
            parent.h5(classes = alignClass.takeIf { it.isNotEmpty() }) {
                context.textConverter.convertElements(elements, this)
            }
        6 ->
            parent.h6(classes = alignClass.takeIf { it.isNotEmpty() }) {
                context.textConverter.convertElements(elements, this)
            }
        else ->
            parent.h6(classes = alignClass.takeIf { it.isNotEmpty() }) {
                context.textConverter.convertElements(elements, this)
            }
    }
}

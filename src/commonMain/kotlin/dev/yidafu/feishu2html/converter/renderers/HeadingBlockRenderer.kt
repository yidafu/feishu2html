package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal object Heading1BlockRenderer : Renderable<Heading1Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading1Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading1, 1, parent, context)
    }
}

internal object Heading2BlockRenderer : Renderable<Heading2Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading2Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading2, 2, parent, context)
    }
}

internal object Heading3BlockRenderer : Renderable<Heading3Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading3Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading3, 3, parent, context)
    }
}

internal object Heading4BlockRenderer : Renderable<Heading4Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading4Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading4, 4, parent, context)
    }
}

internal object Heading5BlockRenderer : Renderable<Heading5Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading5Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading5, 5, parent, context)
    }
}

internal object Heading6BlockRenderer : Renderable<Heading6Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading6Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading6, 6, parent, context)
    }
}

internal object Heading7BlockRenderer : Renderable<Heading7Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading7Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading7, 7, parent, context)
    }
}

internal object Heading8BlockRenderer : Renderable<Heading8Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading8Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading8, 8, parent, context)
    }
}

internal object Heading9BlockRenderer : Renderable<Heading9Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading9Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
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
    logger.debug { "Rendering heading level $level with ${elements.size} elements" }
    val alignClass = getAlignClass(headingData.style?.align)
    val feishuClass = "heading heading-h$level" + if (alignClass.isNotEmpty()) " $alignClass" else ""

    when (level) {
        1 ->
            parent.h1(classes = feishuClass) {
                context.textConverter.convertElements(elements, this)
            }
        2 ->
            parent.h2(classes = feishuClass) {
                context.textConverter.convertElements(elements, this)
            }
        3 ->
            parent.h3(classes = feishuClass) {
                context.textConverter.convertElements(elements, this)
            }
        4 ->
            parent.h4(classes = feishuClass) {
                context.textConverter.convertElements(elements, this)
            }
        5 ->
            parent.h5(classes = feishuClass) {
                context.textConverter.convertElements(elements, this)
            }
        6 ->
            parent.h6(classes = feishuClass) {
                context.textConverter.convertElements(elements, this)
            }
        else -> {
            // h7-h9 rendered as h6 since HTML only supports h1-h6
            val headingClass = "heading heading-h$level" + if (alignClass.isNotEmpty()) " $alignClass" else ""
            parent.h6(classes = headingClass) {
                context.textConverter.convertElements(elements, this)
            }
        }
    }
}

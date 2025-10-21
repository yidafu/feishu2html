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
        renderHeading(heading.heading1, 1, parent, blockNode, context)
    }
}

internal object Heading2BlockRenderer : Renderable<Heading2Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading2Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading2, 2, parent, blockNode, context)
    }
}

internal object Heading3BlockRenderer : Renderable<Heading3Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading3Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading3, 3, parent, blockNode, context)
    }
}

internal object Heading4BlockRenderer : Renderable<Heading4Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading4Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading4, 4, parent, blockNode, context)
    }
}

internal object Heading5BlockRenderer : Renderable<Heading5Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading5Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading5, 5, parent, blockNode, context)
    }
}

internal object Heading6BlockRenderer : Renderable<Heading6Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading6Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading6, 6, parent, blockNode, context)
    }
}

internal object Heading7BlockRenderer : Renderable<Heading7Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading7Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading7, 7, parent, blockNode, context)
    }
}

internal object Heading8BlockRenderer : Renderable<Heading8Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading8Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading8, 8, parent, blockNode, context)
    }
}

internal object Heading9BlockRenderer : Renderable<Heading9Block> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<Heading9Block>,
        context: RenderContext,
    ) {
        val heading = blockNode.data
        renderHeading(heading.heading9, 9, parent, blockNode, context)
    }
}

private fun renderHeading(
    headingData: HeadingBlockData?,
    level: Int,
    parent: FlowContent,
    blockNode: BlockNode<*>,
    context: RenderContext,
) {
    val elements = headingData?.elements ?: return
    val hasChildren = blockNode.children.isNotEmpty()
    logger.debug { "Rendering heading level $level with ${elements.size} elements, children=${blockNode.children.size}" }
    val alignClass = getAlignClass(headingData.style?.align)

    if (hasChildren) {
        // 包裹在可折叠容器中
        parent.div(classes = "heading-container collapsible") {
            attributes["data-collapsed"] = "false"

            // 标题行
            renderHeadingElement(level, alignClass, hasChildren = true, this) {
                // 折叠指示器
                span(classes = "collapse-indicator") { +"▼" }
                context.textConverter.convertElements(elements, this)
            }

            // 子节点容器
            div(classes = "heading-children") {
                blockNode.renderChildren(this, context)
            }
        }
    } else {
        // 无子节点，正常渲染
        renderHeadingElement(level, alignClass, hasChildren = false, parent) {
            context.textConverter.convertElements(elements, this)
        }
    }
}

private fun renderHeadingElement(
    level: Int,
    alignClass: String,
    hasChildren: Boolean,
    parent: FlowContent,
    content: FlowContent.() -> Unit,
) {
    val baseClass = "heading heading-h$level" + if (alignClass.isNotEmpty()) " $alignClass" else ""
    val headingClass = if (hasChildren) "$baseClass collapsible-trigger" else baseClass

    when (level) {
        1 -> parent.h1(classes = headingClass, content)
        2 -> parent.h2(classes = headingClass, content)
        3 -> parent.h3(classes = headingClass, content)
        4 -> parent.h4(classes = headingClass, content)
        5 -> parent.h5(classes = headingClass, content)
        6 -> parent.h6(classes = headingClass, content)
        else -> {
            // h7-h9 rendered as h6 since HTML only supports h1-h6
            parent.h6(classes = headingClass, content)
        }
    }
}

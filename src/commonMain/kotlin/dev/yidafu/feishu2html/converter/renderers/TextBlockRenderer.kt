package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Helper function to determine paragraph alignment class
 */
private fun getAlignmentClass(align: Int?): String? {
    return when (align) {
        2 -> "align-center"
        3 -> "align-right"
        else -> null
    }
}

internal object TextBlockRenderer : Renderable<TextBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<TextBlock>,
        context: RenderContext,
    ) {
        val textBlock = blockNode.data
        val elements = textBlock.text?.elements ?: return
        logger.debug { "Rendering text block with ${elements.size} elements, children=${blockNode.children.size}" }
        val alignClass = getAlignmentClass(textBlock.text?.style?.align)

        // 如果有子节点，使用容器包裹
        if (blockNode.children.isNotEmpty()) {
            parent.div(classes = "text-block-container") {
                p(classes = alignClass) {
                    context.textConverter.convertElements(elements, this)
                }
                div(classes = "text-block-children") {
                    blockNode.renderChildren(this, context)
                }
            }
        } else {
            parent.p(classes = alignClass) {
                context.textConverter.convertElements(elements, this)
            }
        }
    }
}


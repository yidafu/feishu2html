package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

internal object CalloutBlockRenderer : Renderable<CalloutBlock> {
    private val logger = KotlinLogging.logger {}

    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<CalloutBlock>,
        context: RenderContext,
    ) {
        val calloutBlock = blockNode.data
        val colorClass = BlockColor.getColorClass(calloutBlock.callout?.backgroundColor) ?: "default"
        val emoji = Emoji.fromId(calloutBlock.callout?.emojiId)
        logger.debug { "Rendering callout block: color=$colorClass, children=${blockNode.children.size}" }

        parent.div(classes = "callout-block callout-$colorClass") {
            if (emoji != null) {
                div(classes = "callout-emoji-container") {
                    span(classes = "callout-block-emoji") { +emoji }
                }
            }

            div(classes = "callout-block-children") {
                // 递归渲染子节点
                blockNode.renderChildren(this, context)
            }
        }
    }
}

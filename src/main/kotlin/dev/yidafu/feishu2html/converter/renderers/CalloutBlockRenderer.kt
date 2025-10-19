package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import org.slf4j.LoggerFactory

object CalloutBlockRenderer : Renderable {
    private val logger = LoggerFactory.getLogger(CalloutBlockRenderer::class.java)

    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val calloutBlock = block as CalloutBlock
        val colorClass = BlockColor.getColorClass(calloutBlock.callout?.backgroundColor) ?: "default"
        val emoji = Emoji.fromId(calloutBlock.callout?.emojiId)
        logger.debug("Rendering callout block: color={}, children={}",
            colorClass, calloutBlock.children?.size ?: 0)

        parent.div("callout callout-$colorClass") {
            if (emoji != null) {
                span(classes = "callout-emoji") { +emoji }
            }

            // 递归渲染子块
            calloutBlock.children?.forEach { childId ->
                val childBlock = allBlocks[childId]
                if (childBlock != null) {
                    context.processedBlocks.add(childId)
                    renderBlock(childBlock, this, allBlocks, context)
                }
            }
        }
    }
}

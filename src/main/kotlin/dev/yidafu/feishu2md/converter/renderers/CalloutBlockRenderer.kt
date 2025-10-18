package dev.yidafu.feishu2md.converter.renderers

import dev.yidafu.feishu2md.api.model.*
import dev.yidafu.feishu2md.converter.*
import kotlinx.html.*

object CalloutBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val calloutBlock = block as CalloutBlock
        val colorClass = BlockColor.getColorClass(calloutBlock.callout?.backgroundColor) ?: "default"
        val emoji = Emoji.fromId(calloutBlock.callout?.emojiId)

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

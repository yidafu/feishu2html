package dev.yidafu.feishu2md.converter.renderers

import dev.yidafu.feishu2md.api.model.*
import dev.yidafu.feishu2md.converter.*
import kotlinx.html.*

object TextBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val textBlock = block as TextBlock
        val elements = textBlock.text?.elements ?: return
        val alignClass = getAlignClass(textBlock.text?.style?.align)

        parent.p(classes = alignClass.takeIf { it.isNotEmpty() }) {
            context.textConverter.convertElements(elements, this)
        }
    }
}

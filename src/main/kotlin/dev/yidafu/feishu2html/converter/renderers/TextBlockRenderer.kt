package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
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

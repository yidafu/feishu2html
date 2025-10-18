package dev.yidafu.feishu2md.converter.renderers

import dev.yidafu.feishu2md.api.model.*
import dev.yidafu.feishu2md.converter.*
import kotlinx.html.*

object ImageBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val imageBlock = block as ImageBlock
        val token = imageBlock.image?.token ?: return
        val width = imageBlock.image?.width
        val height = imageBlock.image?.height
        val alignClass = getAlignClass(imageBlock.image?.align)

        parent.img(src = "images/$token.png", alt = "image") {
            if (alignClass.isNotEmpty()) {
                classes = setOf(alignClass)
            }
            if (width != null || height != null) {
                style =
                    buildList {
                        width?.let { add("max-width: ${it}px") }
                        height?.let { add("max-height: ${it}px") }
                    }.joinToString("; ")
            }
        }
    }
}

object BoardBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val boardBlock = block as BoardBlock
        val token = boardBlock.board?.token ?: return
        val width = boardBlock.board?.width ?: 820
        val height = boardBlock.board?.height ?: 400

        parent.img(src = "images/$token.png", alt = "电子画板") {
            style = "width: ${width}px; height: ${height}px; display: block; margin: 0 auto; object-fit: contain; overflow: hidden;"
        }
    }
}

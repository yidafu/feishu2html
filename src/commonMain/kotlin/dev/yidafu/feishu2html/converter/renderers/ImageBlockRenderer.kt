package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal object ImageBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val imageBlock = block as ImageBlock
        val token = imageBlock.image?.token ?: return
        logger.debug { "Rendering image: token=$token" }
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

internal object BoardBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val boardBlock = block as BoardBlock
        val token = boardBlock.board?.token ?: return

        parent.div(classes = "board-container") {
            style = "width: 820px; height: 400px; overflow: hidden; display: block; margin: 0 auto; margin-top: 0;"
            img(src = "images/$token.png", alt = "Electronic Whiteboard") {
                style = "display: block; margin: 0 auto;"
            }
        }
    }
}

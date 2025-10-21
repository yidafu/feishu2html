package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal object ImageBlockRenderer : Renderable<ImageBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<ImageBlock>,
        context: RenderContext,
    ) {
        val imageBlock = blockNode.data
        val token = imageBlock.image?.token ?: return
        logger.debug { "Rendering image: token=$token" }
        val width = imageBlock.image?.width
        val height = imageBlock.image?.height
        val alignClass = getAlignClass(imageBlock.image?.align)

        // Use base64 data URL if available, otherwise use relative path
        val imageSrc = context.imageBase64Cache[token] ?: "images/$token.png"

        parent.img(src = imageSrc, alt = "image") {
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

internal object BoardBlockRenderer : Renderable<BoardBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<BoardBlock>,
        context: RenderContext,
    ) {
        val boardBlock = blockNode.data
        val token = boardBlock.board?.token ?: return

        // Use base64 data URL if available, otherwise use relative path
        val imageSrc = context.imageBase64Cache[token] ?: "images/$token.png"

        parent.div(classes = "board-container") {
            style = "width: 820px; height: 400px; overflow: hidden; display: block; margin: 0 auto; margin-top: 0;"
            img(src = imageSrc, alt = "Electronic Whiteboard") {
                style = "display: block; margin: 0 auto;"
            }
        }
    }
}

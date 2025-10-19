package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

internal object FileBlockRenderer : Renderable {
    private val logger = KotlinLogging.logger {}

    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val fileBlock = block as FileBlock
        val token = fileBlock.file?.token ?: return
        val name = fileBlock.file?.name ?: "Download File"
        logger.debug { "Rendering file block: name=$name, token=$token" }

        parent.a(href = "files/$token") {
            attributes["download"] = ""
            +name
        }
    }
}

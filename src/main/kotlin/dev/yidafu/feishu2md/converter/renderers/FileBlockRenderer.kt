package dev.yidafu.feishu2md.converter.renderers

import dev.yidafu.feishu2md.api.model.*
import dev.yidafu.feishu2md.converter.*
import kotlinx.html.*

object FileBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val fileBlock = block as FileBlock
        val token = fileBlock.file?.token ?: return
        val name = fileBlock.file?.name ?: "下载文件"

        parent.a(href = "files/$token") {
            attributes["download"] = ""
            +name
        }
    }
}

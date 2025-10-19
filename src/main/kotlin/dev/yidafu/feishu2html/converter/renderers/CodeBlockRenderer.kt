package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("dev.yidafu.feishu2html.converter.renderers.CodeBlockRenderer")

object CodeBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val codeBlock = block as CodeBlockItem
        val codeData = codeBlock.code ?: return
        val elements = codeData.elements
        val language = getLanguageName(codeData.language)
        logger.debug("Rendering code block: language={}, elements={}", language, elements.size)
        val content = elements.joinToString("") { it.textRun?.content ?: "" }

        parent.pre {
            code(classes = "language-$language") {
                +content
            }
        }
    }
}

object QuoteBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val quoteBlock = block as QuoteBlock
        val elements = quoteBlock.quote?.elements ?: return
        logger.debug("Rendering quote block with {} elements", elements.size)
        parent.blockQuote {
            context.textConverter.convertElements(elements, this)
        }
    }
}

object EquationBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val equationBlock = block as EquationBlock
        val content = equationBlock.equation?.content ?: return
        logger.debug("Rendering equation block: content length={}", content.length)
        parent.div(classes = "equation") {
            +"$$$content$$"
        }
    }
}

object TodoBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val todoBlock = block as TodoBlock
        val elements = todoBlock.todo?.elements ?: return
        val checked = todoBlock.todo?.style?.done == true
        logger.debug("Rendering todo block: checked={}, elements={}", checked, elements.size)

        parent.div(classes = "todo") {
            input(type = InputType.checkBox) {
                if (checked) {
                    this.checked = true
                }
                disabled = true
            }
            span {
                context.textConverter.convertElements(elements, this)
            }
        }
    }
}

object DividerBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.hr {}
    }
}

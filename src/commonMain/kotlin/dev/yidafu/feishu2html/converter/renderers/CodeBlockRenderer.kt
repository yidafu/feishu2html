package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal object CodeBlockRenderer : Renderable {
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
        logger.debug { "Rendering code block: language=$language, elements=${elements.size}" }
        val content = elements.joinToString("") { it.textRun?.content ?: "" }

        parent.div(classes = "code-block") {
            pre {
                code(classes = "hljs language-$language") {
                    +content
                }
            }
        }
    }
}

internal object QuoteBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val quoteBlock = block as QuoteBlock
        val elements = quoteBlock.quote?.elements ?: return
        logger.debug { "Rendering quote block with ${elements.size} elements" }
        parent.blockQuote {
            context.textConverter.convertElements(elements, this)
        }
    }
}

internal object EquationBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val equationBlock = block as EquationBlock
        val content = equationBlock.equation?.content ?: return
        logger.debug { "Rendering equation block: content length=${content.length}" }
        parent.div(classes = "equation") {
            +"$$$content$$"
        }
    }
}

internal object TodoBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val todoBlock = block as TodoBlock
        val elements = todoBlock.todo?.elements ?: return
        val checked = todoBlock.todo?.style?.done == true
        logger.debug { "Rendering todo block: checked=$checked, elements=${elements.size}" }

        parent.div(classes = "todo-block") {
            div(classes = "todo-block_content") {
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
}

internal object DividerBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.hr {}
    }
}

package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal object CodeBlockRenderer : Renderable<CodeBlockItem> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<CodeBlockItem>,
        context: RenderContext,
    ) {
        val codeBlock = blockNode.data
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

internal object QuoteBlockRenderer : Renderable<QuoteBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<QuoteBlock>,
        context: RenderContext,
    ) {
        val quoteBlock = blockNode.data
        val elements = quoteBlock.quote?.elements ?: return
        logger.debug { "Rendering quote block with ${elements.size} elements" }
        parent.blockQuote {
            context.textConverter.convertElements(elements, this)
        }
    }
}

internal object EquationBlockRenderer : Renderable<EquationBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<EquationBlock>,
        context: RenderContext,
    ) {
        val equationBlock = blockNode.data
        val content = equationBlock.equation?.content ?: return
        logger.debug { "Rendering equation block: content length=${content.length}" }
        parent.div(classes = "equation") {
            +"$$$content$$"
        }
    }
}

internal object TodoBlockRenderer : Renderable<TodoBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<TodoBlock>,
        context: RenderContext,
    ) {
        val todoBlock = blockNode.data
        val elements = todoBlock.todo?.elements ?: return
        val checked = todoBlock.todo?.style?.done == true
        logger.debug { "Rendering todo block: checked=$checked, elements=${elements.size}, children=${blockNode.children.size}" }

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
            // 渲染嵌套子节点
            if (blockNode.children.isNotEmpty()) {
                div(classes = "nested-list") {
                    blockNode.renderChildren(this, context)
                }
            }
        }
    }
}

internal object DividerBlockRenderer : Renderable<DividerBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<DividerBlock>,
        context: RenderContext,
    ) {
        parent.hr {}
    }
}

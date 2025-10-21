package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal object BulletBlockRenderer : Renderable<BulletBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<BulletBlock>,
        context: RenderContext,
    ) {
        val bulletBlock = blockNode.data
        val elements = bulletBlock.bullet?.elements ?: return
        logger.debug { "Rendering bullet list item with ${elements.size} elements, children=${blockNode.children.size}" }

        // Feishu-style structure with div instead of ul/li
        parent.div(classes = "list-wrapper bullet-list") {
            div(classes = "list") {
                div(classes = "bullet") {
                    unsafe { +"•" }
                }
                div(classes = "list-content") {
                    p {
                        context.textConverter.convertElements(elements, this)
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
    }
}

internal object OrderedBlockRenderer : Renderable<OrderedBlock> {
    private var listCounter = 0

    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<OrderedBlock>,
        context: RenderContext,
    ) {
        val orderedBlock = blockNode.data
        val elements = orderedBlock.ordered?.elements ?: return
        val sequence = orderedBlock.ordered?.style?.sequence

        // Reset or increment counter based on sequence
        listCounter =
            when (sequence) {
                "1" -> 1
                "auto" -> listCounter + 1
                else -> listCounter + 1
            }

        logger.debug { "Rendering ordered list item with ${elements.size} elements, children=${blockNode.children.size}" }

        // Feishu-style structure with div instead of ul/li
        parent.div(classes = "list-wrapper ordered-list") {
            div(classes = "list") {
                div(classes = "order") {
                    unsafe { +"$listCounter." }
                }
                div(classes = "list-content") {
                    p {
                        context.textConverter.convertElements(elements, this)
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
    }
}

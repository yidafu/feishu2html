package dev.yidafu.feishu2md.converter

import dev.yidafu.feishu2md.api.model.Block
import kotlinx.html.FlowContent

/**
 * 可渲染接口 - 所有Block Renderer必须实现
 *
 * 定义了Block渲染器的统一接口。每个Block类型都有对应的Renderer对象实现此接口。
 *
 * ## 实现示例
 * ```kotlin
 * object TextBlockRenderer : Renderable {
 *     override fun <T> render(parent: FlowContent, block: T, allBlocks: Map<String, Block>, context: RenderContext) {
 *         val textBlock = block as TextBlock
 *         parent.p { +textBlock.text?.elements }
 *     }
 * }
 * ```
 *
 * @see RenderContext
 */
interface Renderable {
    /**
     * 渲染Block为HTML
     *
     * @param parent kotlinx.html的FlowContent对象，用于构建HTML
     * @param block 要渲染的Block对象（需要在实现中进行类型转换）
     * @param allBlocks 文档中所有Block的映射表（blockId -> Block），用于渲染子块
     * @param context 渲染上下文，包含共享的转换器和已处理Block集合
     */
    fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    )
}

/**
 * 渲染上下文 - 携带渲染过程中需要的共享资源
 *
 * @property textConverter 文本元素转换器，用于将TextElement转换为HTML
 * @property processedBlocks 已处理的Block ID集合，用于避免重复渲染
 */
data class RenderContext(
    val textConverter: TextElementConverter,
    val processedBlocks: MutableSet<String>,
)

package dev.yidafu.feishu2html.converter

import dev.yidafu.feishu2html.api.model.Block
import dev.yidafu.feishu2html.api.model.BlockNode
import kotlinx.html.FlowContent

/**
 * Renderable interface - must be implemented by all Block Renderers
 *
 * Defines a unified interface for Block renderers. Each Block type has a corresponding Renderer object implementing this interface.
 *
 * ## Generics
 * Uses type parameter T to ensure type safety:
 * - Eliminates the need for type casting
 * - blockNode.data is automatically the correct type
 *
 * ## Implementation Example
 * ```kotlin
 * object TextBlockRenderer : Renderable<TextBlock> {
 *     override fun render(parent: FlowContent, blockNode: BlockNode<TextBlock>, context: RenderContext) {
 *         val textBlock = blockNode.data  // Already TextBlock, no cast needed!
 *         parent.p {
 *             context.textConverter.convertElements(textBlock.text?.elements ?: emptyList(), this)
 *         }
 *     }
 * }
 * ```
 *
 * ## Helper Methods
 * For container blocks with children, use the extension methods:
 * - `blockNode.renderChildren(parent, context)` - Render all children
 * - `blockNode.renderChildrenFiltered(parent, context) { it.data is SpecificBlock }` - Render filtered children
 *
 * @see RenderContext
 * @see BlockNode
 * @see renderChildren
 * @see renderChildrenFiltered
 */
internal interface Renderable<T : Block> {
    /**
     * Render BlockNode as HTML
     *
     * @param parent kotlinx.html FlowContent object for building HTML
     * @param blockNode BlockNode containing block data, children, and parent (data is type T)
     * @param context Rendering context containing shared converters
     */
    fun render(
        parent: FlowContent,
        blockNode: BlockNode<T>,
        context: RenderContext,
    )
}

/**
 * Rendering context - carries shared resources needed during rendering
 *
 * @property textConverter Text element converter for converting TextElement to HTML
 * @property imageBase64Cache Map of image tokens to base64 data URLs for inline images
 * @property showUnsupportedBlocks Whether to render unsupported block warnings
 */
internal data class RenderContext(
    val textConverter: TextElementConverter,
    val imageBase64Cache: Map<String, String> = emptyMap(),
    val showUnsupportedBlocks: Boolean = true,
)

/**
 * Extension: Render all children of this BlockNode
 *
 * Convenience method for container blocks that need to render all their children.
 * Eliminates the need to write forEach loops in every container renderer.
 *
 * ## Example
 * ```kotlin
 * parent.div {
 *     blockNode.renderChildren(this, context)
 * }
 * ```
 *
 * @param parent Parent HTML element to render children into
 * @param context Rendering context
 */
internal fun BlockNode<Block>.renderChildren(
    parent: FlowContent,
    context: RenderContext,
) {
    children.forEach { childNode ->
        childNode.render(parent, context)
    }
}

/**
 * Extension: Render filtered children of this BlockNode
 *
 * Convenience method for container blocks that need to render only specific types of children.
 * Useful for Grid layouts (filtering GridColumn) or other selective rendering scenarios.
 *
 * ## Example
 * ```kotlin
 * parent.div {
 *     blockNode.renderChildrenFiltered(this, context) { it.data is GridColumnBlock }
 * }
 * ```
 *
 * @param parent Parent HTML element to render children into
 * @param context Rendering context
 * @param predicate Filter condition for children to render
 */
internal fun BlockNode<Block>.renderChildrenFiltered(
    parent: FlowContent,
    context: RenderContext,
    predicate: (BlockNode<Block>) -> Boolean,
) {
    children.filter(predicate).forEach { childNode ->
        childNode.render(parent, context)
    }
}

package dev.yidafu.feishu2html.converter

import dev.yidafu.feishu2html.api.model.Block
import kotlinx.html.FlowContent

/**
 * Renderable interface - must be implemented by all Block Renderers
 *
 * Defines a unified interface for Block renderers. Each Block type has a corresponding Renderer object implementing this interface.
 *
 * ## Implementation Example
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
internal interface Renderable {
    /**
     * Render Block as HTML
     *
     * @param parent kotlinx.html FlowContent object for building HTML
     * @param block Block object to render (requires type casting in implementation)
     * @param allBlocks Mapping of all blocks in document (blockId -> Block) for rendering child blocks
     * @param context Rendering context containing shared converters and processed block set
     */
    fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    )
}

/**
 * Rendering context - carries shared resources needed during rendering
 *
 * @property textConverter Text element converter for converting TextElement to HTML
 * @property processedBlocks Set of processed Block IDs to avoid duplicate rendering
 * @property imageBase64Cache Map of image tokens to base64 data URLs for inline images
 * @property showUnsupportedBlocks Whether to render unsupported block warnings
 */
internal data class RenderContext(
    val textConverter: TextElementConverter,
    val processedBlocks: MutableSet<String>,
    val imageBase64Cache: Map<String, String> = emptyMap(),
    val showUnsupportedBlocks: Boolean = true,
)

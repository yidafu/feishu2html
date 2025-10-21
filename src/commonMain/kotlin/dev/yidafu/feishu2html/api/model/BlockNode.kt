package dev.yidafu.feishu2html.api.model

/**
 * Block tree node - represents a Block with its children in a tree structure
 *
 * This class provides a tree representation of the document structure, where each node
 * contains a Block and references to its children and parent nodes.
 *
 * ## Design Considerations
 * - `children`: List of child nodes for downward traversal
 * - `parent`: Reference to parent node for upward traversal (null for root nodes)
 * - `parent` is excluded from equals/hashCode to avoid circular reference issues
 *
 * ## Generics
 * - Uses contravariant type parameter to allow nodes with different block types in the same tree
 * - A parent CalloutBlock can have children of TextBlock, ImageBlock, etc.
 *
 * ## Usage Example
 * ```kotlin
 * val rootNode = BlockNode(
 *     data = pageBlock,
 *     children = listOf(
 *         BlockNode(data = textBlock1, children = emptyList(), parent = rootNode),
 *         BlockNode(data = textBlock2, children = emptyList(), parent = rootNode)
 *     ),
 *     parent = null
 * )
 * ```
 *
 * @property data The Block data
 * @property children List of child BlockNodes (can be of any Block subtype)
 * @property parent Parent BlockNode (null for root nodes)
 */
class BlockNode<out T : Block>(
    val data: T,
    val children: List<BlockNode<Block>>,
    val parent: BlockNode<Block>? = null,
) {
    /**
     * Custom equals that excludes parent to avoid circular reference
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BlockNode<*>

        if (data != other.data) return false
        if (children != other.children) return false

        return true
    }

    /**
     * Custom hashCode that excludes parent to avoid circular reference
     */
    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + children.hashCode()
        return result
    }

    /**
     * Get the depth of this node in the tree (0 for root)
     */
    fun depth(): Int {
        var depth = 0
        var current = parent
        while (current != null) {
            depth++
            current = current.parent
        }
        return depth
    }

    /**
     * Get all ancestors of this node (from immediate parent to root)
     */
    fun ancestors(): List<BlockNode<Block>> {
        val result = mutableListOf<BlockNode<Block>>()
        var current = parent
        while (current != null) {
            result.add(current)
            current = current.parent
        }
        return result
    }

    /**
     * Check if this node is a root node (has no parent)
     */
    fun isRoot(): Boolean = parent == null

    /**
     * Check if this node is a leaf node (has no children)
     */
    fun isLeaf(): Boolean = children.isEmpty()
}

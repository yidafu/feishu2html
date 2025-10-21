package dev.yidafu.feishu2html

import dev.yidafu.feishu2html.api.model.Block
import dev.yidafu.feishu2html.api.model.BlockNode

/**
 * Test helper to convert a single block to a BlockNode.
 *
 * Creates a BlockNode with no children and no parent.
 * Useful for testing individual renderers.
 *
 * Note: Returns BlockNode<Block> to allow passing to any Renderable<T : Block>
 */
@Suppress("UNCHECKED_CAST")
fun <T : Block> T.toBlockNode(): BlockNode<T> {
    return BlockNode(
        data = this,
        children = emptyList(),
        parent = null
    ) as BlockNode<T>
}

/**
 * Test helper to convert a flat list of blocks to BlockNode list.
 *
 * This is a simplified version for testing - it creates a flat structure
 * where each block becomes a root-level BlockNode with no children.
 *
 * For testing with hierarchical structures, use [buildBlockTree] instead.
 */
fun List<Block>.toBlockNodes(): List<BlockNode<Block>> {
    return this.map { it.toBlockNode() }
}

/**
 * Build a proper BlockNode tree from a list of blocks, respecting parent-child relationships.
 *
 * This mimics the behavior of FeishuApiClient.getOrderedBlocks() and properly constructs
 * the tree structure based on each block's children field.
 */
fun buildBlockTree(blocks: List<Block>): List<BlockNode<Block>> {
    val blocksMap = blocks.associateBy { it.blockId }
    val nodeCache = mutableMapOf<String, BlockNode<Block>>()
    
    fun buildNode(blockId: String, parent: BlockNode<Block>? = null): BlockNode<Block>? {
        if (blockId in nodeCache) {
            return nodeCache[blockId]
        }

        val block = blocksMap[blockId] ?: return null

        // Create node without children first
        val node = BlockNode(
            data = block,
            children = emptyList(),
            parent = parent
        )
        nodeCache[blockId] = node

        // Build children recursively
        val children = block.children?.mapNotNull { childId ->
            buildNode(childId, node)
        } ?: emptyList()

        // Update node with children
        val completeNode = BlockNode(
            data = node.data,
            children = children,
            parent = node.parent
        )
        nodeCache[blockId] = completeNode

        return completeNode
    }

    // Find root blocks (blocks with no parent or parent not in the list)
    val rootBlocks = blocks.filter { block ->
        block.parentId == null || blocksMap[block.parentId] == null
    }

    return rootBlocks.mapNotNull { block ->
        buildNode(block.blockId)
    }
}


package dev.yidafu.feishu2html.benchmark

import dev.yidafu.feishu2html.api.model.*

/**
 * Test data generator for performance benchmarks
 */
internal object TestDataGenerator {
    /**
     * Create a small document (10 text blocks, no images)
     * Suitable for basic performance testing
     */
    internal fun createSmallDocument(): Pair<DocumentInfo, DocumentRawContent> {
        val documentInfo = DocumentInfo(
            documentId = "small_doc",
            revisionId = 1,
            title = "Small Test Document"
        )

        val blocks = mutableMapOf<String, Block>()
        val childIds = mutableListOf<String>()

        // Add 10 text blocks
        repeat(10) { i ->
            val blockId = "text_$i"
            childIds.add(blockId)
            blocks[blockId] = TextBlock(
                blockId = blockId,
                blockType = BlockType.TEXT,
                parentId = "page_root",
                children = emptyList(),
                text = TextBlockData(
                    style = TextStyle(),
                    elements = listOf(
                        TextElement(
                            textRun = TextRun(
                                content = "This is test text block number $i with some content.",
                                textElementStyle = TextElementStyle()
                            )
                        )
                    )
                )
            )
        }

        // Add PAGE block as root
        blocks["page_root"] = PageBlock(
            blockId = "page_root",
            blockType = BlockType.PAGE,
            parentId = null,
            children = childIds,
            page = PageBlockData()
        )

        val rawContent = DocumentRawContent(
            document = Document(
                documentId = "small_doc",
                revisionId = 1,
                title = "Small Test Document"
            ),
            blocks = blocks
        )

        return Pair(documentInfo, rawContent)
    }

    /**
     * Create a medium document (100 text blocks, 10 images)
     * Suitable for moderate load testing
     */
    internal fun createMediumDocument(): Pair<DocumentInfo, DocumentRawContent> {
        val documentInfo = DocumentInfo(
            documentId = "medium_doc",
            revisionId = 1,
            title = "Medium Test Document"
        )

        val blocks = mutableMapOf<String, Block>()
        val blockIds = mutableListOf<String>()

        // Add 90 text blocks
        repeat(90) { i ->
            val blockId = "text_$i"
            blockIds.add(blockId)
            blocks[blockId] = TextBlock(
                blockId = blockId,
                blockType = BlockType.TEXT,
                parentId = "page_root",
                children = emptyList(),
                text = TextBlockData(
                    style = TextStyle(),
                    elements = listOf(
                        TextElement(
                            textRun = TextRun(
                                content = "This is test text block number $i with some content for medium document.",
                                textElementStyle = TextElementStyle()
                            )
                        )
                    )
                )
            )
        }

        // Add 10 image blocks
        repeat(10) { i ->
            val blockId = "image_$i"
            blockIds.add(blockId)
            blocks[blockId] = ImageBlock(
                blockId = blockId,
                blockType = BlockType.IMAGE,
                parentId = "page_root",
                children = emptyList(),
                image = ImageBlockData(
                    token = "img_token_$i",
                    width = 800,
                    height = 600
                )
            )
        }

        // Add PAGE block as root
        blocks["page_root"] = PageBlock(
            blockId = "page_root",
            blockType = BlockType.PAGE,
            parentId = null,
            children = blockIds,
            page = PageBlockData()
        )

        val rawContent = DocumentRawContent(
            document = Document(
                documentId = "medium_doc",
                revisionId = 1,
                title = "Medium Test Document"
            ),
            blocks = blocks
        )

        return Pair(documentInfo, rawContent)
    }

    /**
     * Create a large document (950 text blocks, 40 images, 10 files)
     * Suitable for stress testing
     */
    internal fun createLargeDocument(): Pair<DocumentInfo, DocumentRawContent> {
        val documentInfo = DocumentInfo(
            documentId = "large_doc",
            revisionId = 1,
            title = "Large Test Document"
        )

        val blocks = mutableMapOf<String, Block>()
        val blockIds = mutableListOf<String>()

        // Add 950 text blocks
        repeat(950) { i ->
            val blockId = "text_$i"
            blockIds.add(blockId)
            blocks[blockId] = TextBlock(
                blockId = blockId,
                blockType = BlockType.TEXT,
                parentId = "page_root",
                children = emptyList(),
                text = TextBlockData(
                    style = TextStyle(),
                    elements = listOf(
                        TextElement(
                            textRun = TextRun(
                                content = "This is test text block number $i with some content for large document. " +
                                    "It contains more text to simulate real documents.",
                                textElementStyle = TextElementStyle()
                            )
                        )
                    )
                )
            )
        }

        // Add 40 image blocks
        repeat(40) { i ->
            val blockId = "image_$i"
            blockIds.add(blockId)
            blocks[blockId] = ImageBlock(
                blockId = blockId,
                blockType = BlockType.IMAGE,
                parentId = "page_root",
                children = emptyList(),
                image = ImageBlockData(
                    token = "img_token_$i",
                    width = 1200,
                    height = 900
                )
            )
        }

        // Add 10 file blocks
        repeat(10) { i ->
            val blockId = "file_$i"
            blockIds.add(blockId)
            blocks[blockId] = FileBlock(
                blockId = blockId,
                blockType = BlockType.FILE,
                parentId = "page_root",
                children = emptyList(),
                file = FileBlockData(
                    token = "file_token_$i",
                    name = "test_file_$i.pdf"
                )
            )
        }

        // Add PAGE block as root
        blocks["page_root"] = PageBlock(
            blockId = "page_root",
            blockType = BlockType.PAGE,
            parentId = null,
            children = blockIds,
            page = PageBlockData()
        )

        val rawContent = DocumentRawContent(
            document = Document(
                documentId = "large_doc",
                revisionId = 1,
                title = "Large Test Document"
            ),
            blocks = blocks
        )

        return Pair(documentInfo, rawContent)
    }

    /**
     * Create a document with boards (20 text blocks, 5 boards)
     * Suitable for board export testing
     */
    internal fun createDocumentWithBoards(): Pair<DocumentInfo, DocumentRawContent> {
        val documentInfo = DocumentInfo(
            documentId = "board_doc",
            revisionId = 1,
            title = "Document with Boards"
        )

        val blocks = mutableMapOf<String, Block>()
        val blockIds = mutableListOf<String>()

        // Add 20 text blocks
        repeat(20) { i ->
            val blockId = "text_$i"
            blockIds.add(blockId)
            blocks[blockId] = TextBlock(
                blockId = blockId,
                blockType = BlockType.TEXT,
                parentId = "page_root",
                children = emptyList(),
                text = TextBlockData(
                    style = TextStyle(),
                    elements = listOf(
                        TextElement(
                            textRun = TextRun(
                                content = "Text block $i before/after board.",
                                textElementStyle = TextElementStyle()
                            )
                        )
                    )
                )
            )
        }

        // Add 5 board blocks
        repeat(5) { i ->
            val blockId = "board_$i"
            blockIds.add(blockId)
            blocks[blockId] = BoardBlock(
                blockId = blockId,
                blockType = BlockType.BOARD,
                parentId = "page_root",
                children = emptyList(),
                board = BoardBlockData(
                    token = "board_token_$i",
                    width = 1000,
                    height = 800
                )
            )
        }

        // Add PAGE block as root
        blocks["page_root"] = PageBlock(
            blockId = "page_root",
            blockType = BlockType.PAGE,
            parentId = null,
            children = blockIds,
            page = PageBlockData()
        )

        val rawContent = DocumentRawContent(
            document = Document(
                documentId = "board_doc",
                revisionId = 1,
                title = "Document with Boards"
            ),
            blocks = blocks
        )

        return Pair(documentInfo, rawContent)
    }
}


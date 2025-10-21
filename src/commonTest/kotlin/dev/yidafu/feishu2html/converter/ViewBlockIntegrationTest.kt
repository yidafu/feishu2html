package dev.yidafu.feishu2html.converter

import dev.yidafu.feishu2html.buildBlockTree

import dev.yidafu.feishu2html.api.model.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

/**
 * Integration tests for ViewBlock (attachment container) rendering
 */
class ViewBlockIntegrationTest : FunSpec({

    test("应该在完整HTML文档中正确渲染附件（ViewBlock + FileBlock）") {
        // Create blocks that mimic actual Feishu attachment structure
        val fileBlock = FileBlock(
            blockId = "file1",
            blockType = BlockType.FILE,
            children = emptyList(),
            parentId = "view1",
            file = FileBlockData(
                name = "sample.txt",
                token = "test_token_123",
            ),
        )

        val viewBlock = ViewBlock(
            blockId = "view1",
            blockType = BlockType.VIEW,
            children = listOf("file1"),
            parentId = "page1",
            view = ViewBlockData(viewType = 1),
        )

        val blocks = listOf(viewBlock, fileBlock)
        val blockTree = buildBlockTree(blocks)

        val builder = HtmlBuilder(
            title = "Attachment Test",
            customCss = null,
        )

        val html = builder.build(blockTree)

        // Should NOT show unsupported block warning
        html shouldNotContain "Unsupported block type: View"

        // Should render the FILE block correctly
        html shouldContain "file-card"
        html shouldContain "sample.txt"
        html shouldContain "files/sample.txt"
        
        // Should contain Feishu's official file attachment class structure
        html shouldContain "docx-file-block-container"
        html shouldContain "file-block"
    }

    test("应该正确渲染多个附件") {
        val file1 = FileBlock(
            blockId = "file1",
            blockType = BlockType.FILE,
            children = emptyList(),
            parentId = "view1",
            file = FileBlockData(name = "document.pdf", token = "token1"),
        )

        val view1 = ViewBlock(
            blockId = "view1",
            blockType = BlockType.VIEW,
            children = listOf("file1"),
            parentId = "page1",
            view = ViewBlockData(viewType = 1),
        )

        val file2 = FileBlock(
            blockId = "file2",
            blockType = BlockType.FILE,
            children = emptyList(),
            parentId = "view2",
            file = FileBlockData(name = "image.png", token = "token2"),
        )

        val view2 = ViewBlock(
            blockId = "view2",
            blockType = BlockType.VIEW,
            children = listOf("file2"),
            parentId = "page1",
            view = ViewBlockData(viewType = 2),
        )

        val blocks = listOf(view1, file1, view2, file2)
        val blockTree = buildBlockTree(blocks)

        val builder = HtmlBuilder(title = "Multiple Attachments", customCss = null)
        val html = builder.build(blockTree)

        // Should render both files
        html shouldContain "document.pdf"
        html shouldContain "image.png"
        html shouldContain "files/document.pdf"
        html shouldContain "files/image.png"
        
        // Should not show any unsupported warnings
        html shouldNotContain "Unsupported block type: View"
    }

    test("应该正确处理包含附件的混合内容文档") {
        val heading = Heading1Block(
            blockId = "h1",
            blockType = BlockType.HEADING1,
            children = emptyList(),
            parentId = "page1",
            heading1 = HeadingBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "附件"))),
            ),
        )

        val fileBlock = FileBlock(
            blockId = "file1",
            blockType = BlockType.FILE,
            children = emptyList(),
            parentId = "view1",
            file = FileBlockData(name = "report.docx", token = "token123"),
        )

        val viewBlock = ViewBlock(
            blockId = "view1",
            blockType = BlockType.VIEW,
            children = listOf("file1"),
            parentId = "page1",
            view = ViewBlockData(viewType = 1),
        )

        val textBlock = TextBlock(
            blockId = "text1",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = "page1",
            text = TextBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "请查看附件"))),
            ),
        )

        val blocks = listOf(heading, viewBlock, fileBlock, textBlock)
        val blockTree = buildBlockTree(blocks)

        val builder = HtmlBuilder(title = "Mixed Content", customCss = null)
        val html = builder.build(blockTree)

        // Should render all content types
        html shouldContain "<h1"
        html shouldContain "附件"
        html shouldContain "report.docx"
        html shouldContain "请查看附件"
        
        // No unsupported warnings
        html shouldNotContain "Unsupported block type"
    }
})


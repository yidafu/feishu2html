package dev.yidafu.feishu2html.api

import dev.yidafu.feishu2html.api.model.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.*

class FeishuApiClientMockTest : FunSpec({

    lateinit var apiClient: FeishuApiClient

    beforeEach {
        apiClient = FeishuApiClient("test_app_id", "test_app_secret")
    }

    afterEach {
        apiClient.close()
        clearAllMocks()
    }

    test("应该能够成功创建API客户端") {
        val client = FeishuApiClient("app_id", "app_secret")
        client shouldNotBe null
        client.close()
    }

    test("close方法应该可以安全调用多次") {
        val client = FeishuApiClient("app_id", "app_secret")
        client.close()
        client.close() // 不应该抛出异常
    }

    test("getOrderedBlocks应该返回正确顺序的块") {
        val pageBlock =
            PageBlock(
                blockId = "page1",
                blockType = BlockType.PAGE,
                children = listOf("text1", "text2", "text3"),
                parentId = null,
                page =
                    PageBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val text1 =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "First"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val text2 =
            TextBlock(
                blockId = "text2",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Second"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val text3 =
            TextBlock(
                blockId = "text3",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Third"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val content =
            DocumentRawContent(
                document = Document(documentId = "doc1", revisionId = 1, title = "Test"),
                blocks =
                    mapOf(
                        "page1" to pageBlock,
                        "text1" to text1,
                        "text2" to text2,
                        "text3" to text3,
                    ),
            )

        val ordered = apiClient.getOrderedBlocks(content)

        ordered shouldHaveSize 3
        ordered[0].blockId shouldBe "text1"
        ordered[1].blockId shouldBe "text2"
        ordered[2].blockId shouldBe "text3"
    }

    test("getOrderedBlocks应该正确处理深度嵌套") {
        val pageBlock =
            PageBlock(
                blockId = "page1",
                blockType = BlockType.PAGE,
                children = listOf("callout1"),
                parentId = null,
                page =
                    PageBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val callout =
            CalloutBlock(
                blockId = "callout1",
                blockType = BlockType.CALLOUT,
                children = listOf("bullet1"),
                parentId = "page1",
                callout = CalloutBlockData(backgroundColor = 1, borderColor = 1),
            )

        val bullet =
            BulletBlock(
                blockId = "bullet1",
                blockType = BlockType.BULLET,
                children = listOf("text1"),
                parentId = "callout1",
                bullet =
                    BulletBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Bullet"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val text =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "bullet1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Deep"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val content =
            DocumentRawContent(
                document = Document(documentId = "doc1", revisionId = 1, title = "Test"),
                blocks =
                    mapOf(
                        "page1" to pageBlock,
                        "callout1" to callout,
                        "bullet1" to bullet,
                        "text1" to text,
                    ),
            )

        val ordered = apiClient.getOrderedBlocks(content)

        ordered shouldHaveSize 3
        ordered[0].blockId shouldBe "callout1"
        ordered[1].blockId shouldBe "bullet1"
        ordered[2].blockId shouldBe "text1"
    }

    test("getOrderedBlocks应该处理多个顶层块") {
        val pageBlock =
            PageBlock(
                blockId = "page1",
                blockType = BlockType.PAGE,
                children = listOf("h1", "text1", "div1", "text2"),
                parentId = null,
                page =
                    PageBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val h1 =
            Heading1Block(
                blockId = "h1",
                blockType = BlockType.HEADING1,
                children = emptyList(),
                parentId = "page1",
                heading1 =
                    HeadingBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Title"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val text1 =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Para 1"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val div =
            DividerBlock(
                blockId = "div1",
                blockType = BlockType.DIVIDER,
                children = emptyList(),
                parentId = "page1",
                divider = null,
            )

        val text2 =
            TextBlock(
                blockId = "text2",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Para 2"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val content =
            DocumentRawContent(
                document = Document(documentId = "doc1", revisionId = 1, title = "Test"),
                blocks =
                    mapOf(
                        "page1" to pageBlock,
                        "h1" to h1,
                        "text1" to text1,
                        "div1" to div,
                        "text2" to text2,
                    ),
            )

        val ordered = apiClient.getOrderedBlocks(content)

        ordered shouldHaveSize 4
        ordered[0].blockId shouldBe "h1"
        ordered[1].blockId shouldBe "text1"
        ordered[2].blockId shouldBe "div1"
        ordered[3].blockId shouldBe "text2"
    }

    test("getOrderedBlocks应该处理表格结构") {
        val pageBlock =
            PageBlock(
                blockId = "page1",
                blockType = BlockType.PAGE,
                children = listOf("table1"),
                parentId = null,
                page =
                    PageBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val table =
            TableBlock(
                blockId = "table1",
                blockType = BlockType.TABLE,
                children = listOf("cell1", "cell2"),
                parentId = "page1",
                table =
                    TableBlockData(
                        cells = listOf("cell1", "cell2"),
                        property = TableProperty(rowSize = 1, columnSize = 2),
                    ),
            )

        val cell1 =
            TableCellBlock(
                blockId = "cell1",
                blockType = BlockType.TABLE_CELL,
                children = listOf("text1"),
                parentId = "table1",
                tableCell = TableCellBlockData(),
            )

        val text1 =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "cell1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Cell 1"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val cell2 =
            TableCellBlock(
                blockId = "cell2",
                blockType = BlockType.TABLE_CELL,
                children = emptyList(),
                parentId = "table1",
                tableCell = TableCellBlockData(),
            )

        val content =
            DocumentRawContent(
                document = Document(documentId = "doc1", revisionId = 1, title = "Test"),
                blocks =
                    mapOf(
                        "page1" to pageBlock,
                        "table1" to table,
                        "cell1" to cell1,
                        "text1" to text1,
                        "cell2" to cell2,
                    ),
            )

        val ordered = apiClient.getOrderedBlocks(content)

        ordered shouldHaveSize 4
        ordered[0].blockId shouldBe "table1"
        ordered[1].blockId shouldBe "cell1"
    }

    test("getOrderedBlocks应该处理Grid结构") {
        val pageBlock =
            PageBlock(
                blockId = "page1",
                blockType = BlockType.PAGE,
                children = listOf("grid1"),
                parentId = null,
                page =
                    PageBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val grid =
            GridBlock(
                blockId = "grid1",
                blockType = BlockType.GRID,
                children = listOf("col1", "col2"),
                parentId = "page1",
                grid = GridBlockData(columnSize = 2),
            )

        val col1 =
            GridColumnBlock(
                blockId = "col1",
                blockType = BlockType.GRID_COLUMN,
                children = listOf("text1"),
                parentId = "grid1",
                gridColumn = GridColumnBlockData(widthRatio = 50),
            )

        val text1 =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "col1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Col1"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val col2 =
            GridColumnBlock(
                blockId = "col2",
                blockType = BlockType.GRID_COLUMN,
                children = emptyList(),
                parentId = "grid1",
                gridColumn = GridColumnBlockData(widthRatio = 50),
            )

        val content =
            DocumentRawContent(
                document = Document(documentId = "doc1", revisionId = 1, title = "Test"),
                blocks =
                    mapOf(
                        "page1" to pageBlock,
                        "grid1" to grid,
                        "col1" to col1,
                        "text1" to text1,
                        "col2" to col2,
                    ),
            )

        val ordered = apiClient.getOrderedBlocks(content)

        ordered shouldHaveSize 4
        ordered[0].blockId shouldBe "grid1"
    }

    test("getOrderedBlocks应该处理孤儿块") {
        val text1 =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "unknown_parent",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Orphan"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val content =
            DocumentRawContent(
                document = Document(documentId = "doc1", revisionId = 1, title = "Test"),
                blocks = mapOf("text1" to text1),
            )

        val ordered = apiClient.getOrderedBlocks(content)

        // 没有PAGE块时，应该返回所有块
        ordered shouldHaveSize 1
    }

    test("getOrderedBlocks应该忽略已访问的块（防止无限循环）") {
        val block1 =
            TextBlock(
                blockId = "block1",
                blockType = BlockType.TEXT,
                children = listOf("block2"),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "B1"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val block2 =
            TextBlock(
                blockId = "block2",
                blockType = BlockType.TEXT,
                children = listOf("block1"), // 循环引用
                parentId = "block1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "B2"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val pageBlock =
            PageBlock(
                blockId = "page1",
                blockType = BlockType.PAGE,
                children = listOf("block1"),
                parentId = null,
                page =
                    PageBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val content =
            DocumentRawContent(
                document = Document(documentId = "doc1", revisionId = 1, title = "Test"),
                blocks =
                    mapOf(
                        "page1" to pageBlock,
                        "block1" to block1,
                        "block2" to block2,
                    ),
            )

        val ordered = apiClient.getOrderedBlocks(content)

        // 应该只访问每个块一次
        ordered shouldHaveSize 2
    }
})

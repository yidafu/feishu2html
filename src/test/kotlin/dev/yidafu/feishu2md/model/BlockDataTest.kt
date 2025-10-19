package dev.yidafu.feishu2md.model

import dev.yidafu.feishu2md.api.model.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.Json

class BlockDataTest : FunSpec({

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    test("PageBlockData应该正确序列化") {
        val data = PageBlockData(
            elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
            style = TextStyle(align = 1)
        )

        val jsonString = json.encodeToString(PageBlockData.serializer(), data)
        jsonString shouldContain "Page"
    }

    test("TextBlockData应该正确序列化") {
        val data = TextBlockData(
            elements = listOf(TextElement(textRun = TextRun(content = "Text"))),
            style = TextStyle(align = 1)
        )

        val jsonString = json.encodeToString(TextBlockData.serializer(), data)
        jsonString shouldContain "Text"
    }

    test("HeadingBlockData应该正确创建") {
        val data = HeadingBlockData(
            elements = listOf(TextElement(textRun = TextRun(content = "Heading"))),
            style = TextStyle(align = 2)
        )

        data.elements.size shouldBe 1
        data.style?.align shouldBe 2
    }

    test("BulletBlockData应该正确创建") {
        val data = BulletBlockData(
            elements = listOf(TextElement(textRun = TextRun(content = "Bullet"))),
            style = TextStyle(align = 1)
        )

        data.elements.size shouldBe 1
    }

    test("OrderedBlockData应该正确创建") {
        val data = OrderedBlockData(
            elements = listOf(TextElement(textRun = TextRun(content = "Ordered"))),
            style = TextStyle(align = 1)
        )

        data.elements.size shouldBe 1
    }

    test("CodeBlockData应该正确创建") {
        val data = CodeBlockData(
            elements = listOf(TextElement(textRun = TextRun(content = "code"))),
            language = 5
        )

        data.elements.size shouldBe 1
        data.language shouldBe 5
    }

    test("QuoteBlockData应该正确创建") {
        val data = QuoteBlockData(
            elements = listOf(TextElement(textRun = TextRun(content = "Quote")))
        )

        data.elements.size shouldBe 1
    }

    test("EquationBlockData应该正确创建") {
        val data = EquationBlockData(content = "E=mc^2")
        data.content shouldBe "E=mc^2"
    }

    test("TodoBlockData应该正确创建") {
        val data = TodoBlockData(
            elements = listOf(TextElement(textRun = TextRun(content = "Todo"))),
            style = TodoStyle(done = false)
        )

        data.elements.size shouldBe 1
        data.style?.done shouldBe false
    }

    test("ImageBlockData应该正确创建") {
        val data = ImageBlockData(
            token = "img_token",
            width = 800,
            height = 600
        )

        data.token shouldBe "img_token"
        data.width shouldBe 800
        data.height shouldBe 600
    }

    test("FileBlockData应该正确创建") {
        val data = FileBlockData(
            token = "file_token",
            name = "document.pdf"
        )

        data.token shouldBe "file_token"
        data.name shouldBe "document.pdf"
    }

    test("BoardBlockData应该正确创建") {
        val data = BoardBlockData(
            token = "board_token",
            width = 820,
            height = 400
        )

        data.token shouldBe "board_token"
        data.width shouldBe 820
    }

    test("CalloutBlockData应该正确创建") {
        val data = CalloutBlockData(
            backgroundColor = 1,
            borderColor = 2,
            textColor = 3,
            emojiId = "smile"
        )

        data.backgroundColor shouldBe 1
        data.borderColor shouldBe 2
        data.emojiId shouldBe "smile"
    }

    test("GridBlockData应该正确创建") {
        val data = GridBlockData(columnSize = 3)
        data.columnSize shouldBe 3
    }

    test("GridColumnBlockData应该正确创建") {
        val data = GridColumnBlockData(widthRatio = 33)
        data.widthRatio shouldBe 33
    }

    test("TableBlockData应该正确创建") {
        val data = TableBlockData(
            cells = listOf("cell1", "cell2"),
            property = TableProperty(rowSize = 2, columnSize = 2)
        )

        data.cells?.size shouldBe 2
        data.property?.rowSize shouldBe 2
    }

    test("TableProperty应该正确创建") {
        val prop = TableProperty(
            rowSize = 5,
            columnSize = 3,
            columnWidth = listOf(100, 200, 300),
            mergeInfo = null
        )

        prop.rowSize shouldBe 5
        prop.columnSize shouldBe 3
        prop.columnWidth?.size shouldBe 3
    }

    test("IframeBlockData应该支持component结构") {
        val data = IframeBlockData(
            component = IframeComponent(
                iframeType = 1,
                url = "https://bilibili.com"
            )
        )

        data.component?.iframeType shouldBe 1
        data.component?.url shouldBe "https://bilibili.com"
    }

    test("IframeBlockData应该支持直接url字段") {
        val data = IframeBlockData(
            url = "https://example.com"
        )

        data.url shouldBe "https://example.com"
    }

    test("IframeComponent应该正确创建") {
        val comp = IframeComponent(
            iframeType = 3,
            url = "https://airtable.com"
        )

        comp.iframeType shouldBe 3
        comp.url shouldBe "https://airtable.com"
    }

    test("DiagramBlockData应该正确创建") {
        val data = DiagramBlockData(
            diagramType = 1,
            content = "diagram_content"
        )

        data.diagramType shouldBe 1
        data.content shouldBe "diagram_content"
    }
})


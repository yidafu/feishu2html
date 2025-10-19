package dev.yidafu.feishu2html.api

import dev.yidafu.feishu2html.api.model.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.mockk.*
import kotlinx.serialization.json.Json
import java.io.File

class FeishuApiClientCompleteTest : FunSpec({

    fun createMockAuthService(token: String = "test_token"): FeishuAuthService {
        return mockk<FeishuAuthService> {
            coEvery { getAccessToken() } returns token
        }
    }

    test("getOrderedBlocks应该正确排序块") {
        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = listOf("text1", "text2"),
            parentId = null,
            page = PageBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                style = TextStyle(align = 1)
            )
        )

        val text1 = TextBlock(
            blockId = "text1",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = "page1",
            text = TextBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "First"))),
                style = TextStyle(align = 1)
            )
        )

        val text2 = TextBlock(
            blockId = "text2",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = "page1",
            text = TextBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Second"))),
                style = TextStyle(align = 1)
            )
        )

        val blocks = mapOf(
            "page1" to pageBlock,
            "text1" to text1,
            "text2" to text2
        )

        val content = DocumentRawContent(
            document = Document(documentId = "doc1", revisionId = 1, title = "Test"),
            blocks = blocks
        )

        val apiClient = FeishuApiClient("test_app", "test_secret")
        val ordered = apiClient.getOrderedBlocks(content)

        ordered shouldHaveSize 2
        ordered[0].blockId shouldBe "text1"
        ordered[1].blockId shouldBe "text2"

        apiClient.close()
    }

    test("getOrderedBlocks应该处理嵌套块") {
        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = listOf("bullet1"),
            parentId = null,
            page = PageBlockData(elements = listOf(TextElement(textRun = TextRun(content = "Page"))), style = TextStyle(align = 1))
        )

        val bulletBlock = BulletBlock(
            blockId = "bullet1",
            blockType = BlockType.BULLET,
            children = listOf("text1"),
            parentId = "page1",
            bullet = BulletBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Bullet"))),
                style = TextStyle(align = 1)
            )
        )

        val textBlock = TextBlock(
            blockId = "text1",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = "bullet1",
            text = TextBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Nested"))),
                style = TextStyle(align = 1)
            )
        )

        val blocks = mapOf(
            "page1" to pageBlock,
            "bullet1" to bulletBlock,
            "text1" to textBlock
        )

        val content = DocumentRawContent(
            document = Document(documentId = "doc1", revisionId = 1, title = "Test"),
            blocks = blocks
        )

        val apiClient = FeishuApiClient("test_app", "test_secret")
        val ordered = apiClient.getOrderedBlocks(content)

        ordered shouldHaveSize 2
        ordered[0].blockId shouldBe "bullet1"
        ordered[1].blockId shouldBe "text1"

        apiClient.close()
    }

    test("getOrderedBlocks应该处理空文档") {
        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = emptyList(),
            parentId = null,
            page = PageBlockData(elements = listOf(TextElement(textRun = TextRun(content = "Empty"))), style = TextStyle(align = 1))
        )

        val content = DocumentRawContent(
            document = Document(documentId = "doc1", revisionId = 1, title = "Empty"),
            blocks = mapOf("page1" to pageBlock)
        )

        val apiClient = FeishuApiClient("test_app", "test_secret")
        val ordered = apiClient.getOrderedBlocks(content)

        ordered shouldHaveSize 0

        apiClient.close()
    }

    test("getOrderedBlocks应该避免循环引用") {
        // 创建一个有循环引用的块结构（虽然不应该出现）
        val block1 = TextBlock(
            blockId = "block1",
            blockType = BlockType.TEXT,
            children = listOf("block2"),
            parentId = "page1",
            text = TextBlockData(elements = listOf(TextElement(textRun = TextRun(content = "Block 1"))), style = TextStyle(align = 1))
        )

        val block2 = TextBlock(
            blockId = "block2",
            blockType = BlockType.TEXT,
            children = listOf("block1"), // 循环引用
            parentId = "block1",
            text = TextBlockData(elements = listOf(TextElement(textRun = TextRun(content = "Block 2"))), style = TextStyle(align = 1))
        )

        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = listOf("block1"),
            parentId = null,
            page = PageBlockData(elements = listOf(TextElement(textRun = TextRun(content = "Page"))), style = TextStyle(align = 1))
        )

        val blocks = mapOf(
            "page1" to pageBlock,
            "block1" to block1,
            "block2" to block2
        )

        val content = DocumentRawContent(
            document = Document(documentId = "doc1", revisionId = 1, title = "Test"),
            blocks = blocks
        )

        val apiClient = FeishuApiClient("test_app", "test_secret")
        val ordered = apiClient.getOrderedBlocks(content)

        // 应该只访问每个块一次，避免无限循环
        ordered shouldHaveSize 2

        apiClient.close()
    }

    test("getOrderedBlocks应该处理没有PAGE块的情况") {
        val text1 = TextBlock(
            blockId = "text1",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = null,
            text = TextBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Orphan"))),
                style = TextStyle(align = 1)
            )
        )

        val content = DocumentRawContent(
            document = Document(documentId = "doc1", revisionId = 1, title = "Test"),
            blocks = mapOf("text1" to text1)
        )

        val apiClient = FeishuApiClient("test_app", "test_secret")
        val ordered = apiClient.getOrderedBlocks(content)

        // 没有PAGE块时，应该返回所有非PAGE块
        ordered shouldHaveSize 1

        apiClient.close()
    }

    test("close应该正确关闭资源") {
        val apiClient = FeishuApiClient("test_app", "test_secret")

        // 不应该抛出异常
        apiClient.close()
    }
})


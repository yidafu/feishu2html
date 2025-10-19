package dev.yidafu.feishu2html.model

import dev.yidafu.feishu2html.api.model.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.Json

class DocumentTest : FunSpec({

    val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

    test("应该正确创建Document实例") {
        val doc =
            Document(
                documentId = "doc123",
                revisionId = 10,
                title = "Test Document",
            )

        doc.documentId shouldBe "doc123"
        doc.revisionId shouldBe 10
        doc.title shouldBe "Test Document"
    }

    test("应该正确序列化Document") {
        val doc =
            Document(
                documentId = "doc456",
                revisionId = 5,
                title = "Sample",
            )

        val jsonString = json.encodeToString(Document.serializer(), doc)

        jsonString shouldContain "doc456"
        jsonString shouldContain "Sample"
    }

    test("应该正确反序列化Document") {
        val jsonString =
            """
            {
                "document_id": "doc789",
                "revision_id": 15,
                "title": "Deserialized"
            }
            """.trimIndent()

        val doc = json.decodeFromString<Document>(jsonString)

        doc.documentId shouldBe "doc789"
        doc.revisionId shouldBe 15
        doc.title shouldBe "Deserialized"
    }

    test("应该正确创建DocumentRawContent") {
        val doc = Document(documentId = "doc1", revisionId = 1, title = "Test")
        val block =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Content"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val content =
            DocumentRawContent(
                document = doc,
                blocks = mapOf("text1" to block),
            )

        content.document shouldBe doc
        content.blocks.size shouldBe 1
    }

    test("应该正确反序列化DocumentInfo") {
        val jsonString =
            """
            {
                "document_id": "info123",
                "revision_id": 20,
                "title": "Document Info"
            }
            """.trimIndent()

        val info = json.decodeFromString<DocumentInfo>(jsonString)

        info.documentId shouldBe "info123"
        info.revisionId shouldBe 20
        info.title shouldBe "Document Info"
    }

    test("应该正确反序列化DocumentInfoResponse") {
        val jsonString =
            """
            {
                "code": 0,
                "msg": "success",
                "data": {
                    "document": {
                        "document_id": "resp123",
                        "revision_id": 25,
                        "title": "Response Doc"
                    }
                }
            }
            """.trimIndent()

        val response = json.decodeFromString<DocumentInfoResponse>(jsonString)

        response.code shouldBe 0
        response.msg shouldBe "success"
        response.data shouldNotBe null
        response.data?.document?.documentId shouldBe "resp123"
    }

    test("应该正确反序列化DocumentBlocksResponse") {
        val jsonString =
            """
            {
                "code": 0,
                "msg": "success",
                "data": {
                    "items": [
                        {
                            "block_id": "block1",
                            "block_type": 2,
                            "children": [],
                            "parent_id": "page1",
                            "text": {
                                "elements": [{"text_run": {"content": "Test"}}],
                                "style": {"align": 1}
                            }
                        }
                    ],
                    "page_token": "",
                    "has_more": false
                }
            }
            """.trimIndent()

        val response = json.decodeFromString<DocumentBlocksResponse>(jsonString)

        response.code shouldBe 0
        response.data shouldNotBe null
        response.data?.items?.size shouldBe 1
        response.data?.hasMore shouldBe false
    }

    test("应该正确处理错误响应") {
        val jsonString =
            """
            {
                "code": 1770032,
                "msg": "forBidden"
            }
            """.trimIndent()

        val response = json.decodeFromString<DocumentInfoResponse>(jsonString)

        response.code shouldBe 1770032
        response.msg shouldBe "forBidden"
        response.data shouldBe null
    }
})

package dev.yidafu.feishu2md.model

import dev.yidafu.feishu2md.api.model.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.json.Json

class TextElementTest : FunSpec({

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    test("应该正确反序列化TextRun") {
        val jsonString = """
            {
                "text_run": {
                    "content": "Hello",
                    "text_element_style": {
                        "bold": true
                    }
                }
            }
        """.trimIndent()

        val element = json.decodeFromString<TextElement>(jsonString)

        element.textRun shouldNotBe null
        element.textRun?.content shouldBe "Hello"
        element.textRun?.textElementStyle?.bold shouldBe true
    }

    test("应该正确反序列化MentionUser") {
        val jsonString = """
            {
                "mention_user": {
                    "user_id": "user123"
                }
            }
        """.trimIndent()

        val element = json.decodeFromString<TextElement>(jsonString)

        element.mentionUser shouldNotBe null
        element.mentionUser?.userId shouldBe "user123"
    }

    test("应该正确反序列化MentionDoc") {
        val jsonString = """
            {
                "mention_doc": {
                    "token": "doc_token",
                    "obj_type": 22,
                    "url": "https://example.com",
                    "title": "Doc Title"
                }
            }
        """.trimIndent()

        val element = json.decodeFromString<TextElement>(jsonString)

        element.mentionDoc shouldNotBe null
        element.mentionDoc?.title shouldBe "Doc Title"
    }

    test("应该正确反序列化InlineEquation") {
        val jsonString = """
            {
                "equation": {
                    "content": "x^2"
                }
            }
        """.trimIndent()

        val element = json.decodeFromString<TextElement>(jsonString)

        element.equation shouldNotBe null
        element.equation?.content shouldBe "x^2"
    }

    test("应该正确反序列化InlineFile") {
        val jsonString = """
            {
                "file": {
                    "file_token": "file123",
                    "source_block_id": "block456"
                }
            }
        """.trimIndent()

        val element = json.decodeFromString<TextElement>(jsonString)

        element.file shouldNotBe null
        element.file?.fileToken shouldBe "file123"
    }

    test("TextElementStyle应该支持所有样式属性") {
        val style = TextElementStyle(
            bold = true,
            italic = true,
            strikethrough = true,
            underline = true,
            inlineCode = true,
            textColor = 1,
            backgroundColor = 2,
            link = Link(url = "https://example.com")
        )

        style.bold shouldBe true
        style.italic shouldBe true
        style.strikethrough shouldBe true
        style.underline shouldBe true
        style.inlineCode shouldBe true
        style.textColor shouldBe 1
        style.backgroundColor shouldBe 2
        style.link?.url shouldBe "https://example.com"
    }

    test("Link应该正确创建") {
        val link = Link(url = "https://test.com")
        link.url shouldBe "https://test.com"
    }

    test("TextStyle应该正确创建") {
        val style = TextStyle(
            align = 2,
            done = true,
            folded = false,
            language = 5,
            wrap = true
        )

        style.align shouldBe 2
        style.done shouldBe true
        style.folded shouldBe false
    }

    test("TodoStyle应该正确创建") {
        val style = TodoStyle(done = true)
        style.done shouldBe true
    }
})


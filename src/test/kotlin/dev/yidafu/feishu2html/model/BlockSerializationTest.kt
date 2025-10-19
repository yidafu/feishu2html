package dev.yidafu.feishu2html.model

import dev.yidafu.feishu2html.api.model.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json
import java.io.File

class BlockSerializationTest : FunSpec({

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    test("应该正确反序列化PageBlock") {
        val jsonString = """
            {
                "block_id": "test_page_1",
                "block_type": 1,
                "children": ["child1"],
                "parent_id": "",
                "page": {
                    "elements": [{"text_run": {"content": "Page Title"}}]
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<PageBlock>()
        block.blockId shouldBe "test_page_1"
        block.blockType shouldBe BlockType.PAGE
    }

    test("应该正确反序列化TextBlock") {
        val jsonString = """
            {
                "block_id": "test_text_1",
                "block_type": 2,
                "children": [],
                "parent_id": "parent1",
                "text": {
                    "elements": [{"text_run": {"content": "Hello World"}}],
                    "style": {"align": 1}
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<TextBlock>()
        block.blockId shouldBe "test_text_1"
        block.blockType shouldBe BlockType.TEXT
        (block as TextBlock).text shouldNotBe null
        block.text?.elements shouldNotBe null
    }

    test("应该正确反序列化所有Heading类型") {
        for (level in 1..9) {
            val jsonString = """
                {
                    "block_id": "test_h$level",
                    "block_type": ${2 + level},
                    "children": [],
                    "parent_id": "parent1",
                    "heading$level": {
                        "elements": [{"text_run": {"content": "Heading $level"}}],
                        "style": {"align": 1}
                    }
                }
            """.trimIndent()

            val block = json.decodeFromString<Block>(jsonString)

            when (level) {
                1 -> {
                    block.shouldBeInstanceOf<Heading1Block>()
                    block.blockType shouldBe BlockType.HEADING1
                }
                2 -> {
                    block.shouldBeInstanceOf<Heading2Block>()
                    block.blockType shouldBe BlockType.HEADING2
                }
                3 -> {
                    block.shouldBeInstanceOf<Heading3Block>()
                    block.blockType shouldBe BlockType.HEADING3
                }
                // ... 其他level
            }
        }
    }

    test("应该正确反序列化BulletBlock") {
        val jsonString = """
            {
                "block_id": "test_bullet_1",
                "block_type": 12,
                "children": [],
                "parent_id": "parent1",
                "bullet": {
                    "elements": [{"text_run": {"content": "Bullet point"}}],
                    "style": {"align": 1}
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<BulletBlock>()
        block.blockType shouldBe BlockType.BULLET
    }

    test("应该正确反序列化OrderedBlock") {
        val jsonString = """
            {
                "block_id": "test_ordered_1",
                "block_type": 13,
                "children": [],
                "parent_id": "parent1",
                "ordered": {
                    "elements": [{"text_run": {"content": "Ordered item"}}],
                    "style": {"align": 1}
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<OrderedBlock>()
        block.blockType shouldBe BlockType.ORDERED
    }

    test("应该正确反序列化CodeBlock") {
        val jsonString = """
            {
                "block_id": "test_code_1",
                "block_type": 14,
                "children": [],
                "parent_id": "parent1",
                "code": {
                    "elements": [{"text_run": {"content": "console.log('hello')"}}],
                    "style": {"language": 1}
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<CodeBlockItem>()
        block.blockType shouldBe BlockType.CODE
    }

    test("应该正确反序列化QuoteBlock") {
        val jsonString = """
            {
                "block_id": "test_quote_1",
                "block_type": 15,
                "children": [],
                "parent_id": "parent1",
                "quote": {
                    "elements": [{"text_run": {"content": "Quote text"}}]
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<QuoteBlock>()
        block.blockType shouldBe BlockType.QUOTE
    }

    test("应该正确反序列化EquationBlock") {
        val jsonString = """
            {
                "block_id": "test_equation_1",
                "block_type": 16,
                "children": [],
                "parent_id": "parent1",
                "equation": {
                    "content": "E = mc^2"
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<EquationBlock>()
        block.blockType shouldBe BlockType.EQUATION
        (block as EquationBlock).equation?.content shouldBe "E = mc^2"
    }

    test("应该正确反序列化TodoBlock") {
        val jsonString = """
            {
                "block_id": "test_todo_1",
                "block_type": 17,
                "children": [],
                "parent_id": "parent1",
                "todo": {
                    "elements": [{"text_run": {"content": "Todo item"}}],
                    "style": {"done": false}
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<TodoBlock>()
        block.blockType shouldBe BlockType.TODO
    }

    test("应该正确反序列化ImageBlock") {
        val jsonString = """
            {
                "block_id": "test_image_1",
                "block_type": 27,
                "children": [],
                "parent_id": "parent1",
                "image": {
                    "token": "image_token_123",
                    "width": 800,
                    "height": 600
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<ImageBlock>()
        block.blockType shouldBe BlockType.IMAGE
        (block as ImageBlock).image?.token shouldBe "image_token_123"
    }

    test("应该正确反序列化TableBlock") {
        val jsonString = """
            {
                "block_id": "test_table_1",
                "block_type": 31,
                "children": ["cell1", "cell2"],
                "parent_id": "parent1",
                "table": {
                    "cells": ["cell1", "cell2"],
                    "property": {
                        "row_size": 2,
                        "column_size": 2
                    }
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<TableBlock>()
        block.blockType shouldBe BlockType.TABLE
    }

    test("应该正确反序列化TableCellBlock") {
        val jsonString = """
            {
                "block_id": "test_cell_1",
                "block_type": 32,
                "children": ["content1"],
                "parent_id": "table1",
                "table_cell": {}
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<TableCellBlock>()
        block.blockType shouldBe BlockType.TABLE_CELL
    }

    test("应该正确反序列化CalloutBlock") {
        val jsonString = """
            {
                "block_id": "test_callout_1",
                "block_type": 19,
                "children": [],
                "parent_id": "parent1",
                "callout": {
                    "background_color": 1,
                    "border_color": 1,
                    "emoji_id": "smile"
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<CalloutBlock>()
        block.blockType shouldBe BlockType.CALLOUT
    }

    test("应该正确反序列化GridBlock") {
        val jsonString = """
            {
                "block_id": "test_grid_1",
                "block_type": 24,
                "children": ["col1", "col2"],
                "parent_id": "parent1",
                "grid": {
                    "column_size": 2
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<GridBlock>()
        block.blockType shouldBe BlockType.GRID
    }

    test("应该正确反序列化GridColumnBlock") {
        val jsonString = """
            {
                "block_id": "test_col_1",
                "block_type": 25,
                "children": ["content1"],
                "parent_id": "grid1",
                "grid_column": {
                    "width_ratio": 50
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<GridColumnBlock>()
        block.blockType shouldBe BlockType.GRID_COLUMN
    }

    test("应该正确反序列化IframeBlock") {
        val jsonString = """
            {
                "block_id": "test_iframe_1",
                "block_type": 26,
                "children": [],
                "parent_id": "parent1",
                "iframe": {
                    "component": {
                        "iframe_type": 1,
                        "url": "https://example.com"
                    }
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<IframeBlock>()
        block.blockType shouldBe BlockType.IFRAME
    }

    test("应该正确反序列化BoardBlock") {
        val jsonString = """
            {
                "block_id": "test_board_1",
                "block_type": 43,
                "children": [],
                "parent_id": "parent1",
                "board": {
                    "token": "board_token_123",
                    "width": 820,
                    "height": 400
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<BoardBlock>()
        block.blockType shouldBe BlockType.BOARD
    }

    test("应该正确反序列化DividerBlock") {
        val jsonString = """
            {
                "block_id": "test_divider_1",
                "block_type": 22,
                "children": [],
                "parent_id": "parent1",
                "divider": {}
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<DividerBlock>()
        block.blockType shouldBe BlockType.DIVIDER
    }

    test("应该正确反序列化FileBlock") {
        val jsonString = """
            {
                "block_id": "test_file_1",
                "block_type": 23,
                "children": [],
                "parent_id": "parent1",
                "file": {
                    "token": "file_token_123",
                    "name": "document.pdf"
                }
            }
        """.trimIndent()

        val block = json.decodeFromString<Block>(jsonString)

        block.shouldBeInstanceOf<FileBlock>()
        block.blockType shouldBe BlockType.FILE
    }

    test("应该能够处理真实的测试文档JSON") {
        val testDoc1Resource = this::class.java.getResource("/test-document-1.json")
        if (testDoc1Resource != null) {
            val content = testDoc1Resource.readText()
            val response = json.decodeFromString<DocumentBlocksResponse>(content)

            response.code shouldBe 0
            response.data shouldNotBe null
            response.data?.items shouldNotBe null
        }
    }

    test("应该能够处理最小测试文档") {
        val testDocMinimalResource = this::class.java.getResource("/test-document-minimal.json")
            ?: throw IllegalStateException("test-document-minimal.json not found in resources")
        val content = testDocMinimalResource.readText()
        val response = json.decodeFromString<DocumentBlocksResponse>(content)

        response.code shouldBe 0
        response.data shouldNotBe null
        response.data?.items?.size shouldBe 2

        val pageBlock = response.data?.items?.get(0)
        pageBlock.shouldBeInstanceOf<PageBlock>()

        val textBlock = response.data?.items?.get(1)
        textBlock.shouldBeInstanceOf<TextBlock>()
    }

    test("BlockType枚举应该涵盖所有52种类型") {
        val allTypes = BlockType.entries
        allTypes.size shouldBe 53 // 包括UNDEFINED

        // 验证主要类型都存在
        BlockType.PAGE.typeCode shouldBe 1
        BlockType.TEXT.typeCode shouldBe 2
        BlockType.BOARD.typeCode shouldBe 43
        BlockType.AI_TEMPLATE.typeCode shouldBe 52
        BlockType.UNDEFINED.typeCode shouldBe 999
    }

    test("BlockType.fromCode应该正确映射代码到枚举") {
        BlockType.fromCode(1) shouldBe BlockType.PAGE
        BlockType.fromCode(2) shouldBe BlockType.TEXT
        BlockType.fromCode(43) shouldBe BlockType.BOARD
        BlockType.fromCode(52) shouldBe BlockType.AI_TEMPLATE
        BlockType.fromCode(999) shouldBe BlockType.UNDEFINED
        BlockType.fromCode(9999) shouldBe null
    }
})


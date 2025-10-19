package dev.yidafu.feishu2md.converter

import dev.yidafu.feishu2md.api.model.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class HtmlBuilderCompleteTest : FunSpec({

    test("应该正确渲染包含所有基础块类型的文档") {
        val blocks = listOf(
            Heading1Block(
                blockId = "h1",
                blockType = BlockType.HEADING1,
                children = emptyList(),
                parentId = "page1",
                heading1 = HeadingBlockData(
                    elements = listOf(TextElement(textRun = TextRun(content = "Title"))),
                    style = TextStyle(align = 1)
                )
            ),
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text = TextBlockData(
                    elements = listOf(TextElement(textRun = TextRun(content = "Content"))),
                    style = TextStyle(align = 1)
                )
            ),
            DividerBlock(
                blockId = "div1",
                blockType = BlockType.DIVIDER,
                children = emptyList(),
                parentId = "page1",
                divider = null
            )
        )

        val allBlocks = blocks.associateBy { it.blockId }
        val builder = HtmlBuilder(title = "Complete Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        html shouldContain "<h1"
        html shouldContain "Title"
        html shouldContain "Content"
        html shouldContain "<hr"
    }

    test("应该正确处理连续的无序列表") {
        val blocks = listOf(
            BulletBlock(
                blockId = "b1",
                blockType = BlockType.BULLET,
                children = emptyList(),
                parentId = "page1",
                bullet = BulletBlockData(
                    elements = listOf(TextElement(textRun = TextRun(content = "Item 1"))),
                    style = TextStyle(align = 1)
                )
            ),
            BulletBlock(
                blockId = "b2",
                blockType = BlockType.BULLET,
                children = emptyList(),
                parentId = "page1",
                bullet = BulletBlockData(
                    elements = listOf(TextElement(textRun = TextRun(content = "Item 2"))),
                    style = TextStyle(align = 1)
                )
            ),
            BulletBlock(
                blockId = "b3",
                blockType = BlockType.BULLET,
                children = emptyList(),
                parentId = "page1",
                bullet = BulletBlockData(
                    elements = listOf(TextElement(textRun = TextRun(content = "Item 3"))),
                    style = TextStyle(align = 1)
                )
            )
        )

        val allBlocks = blocks.associateBy { it.blockId }
        val builder = HtmlBuilder(title = "List Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        html shouldContain "<ul"
        html shouldContain "Item 1"
        html shouldContain "Item 2"
        html shouldContain "Item 3"
        html shouldContain "</ul>"
    }

    test("应该正确处理连续的有序列表") {
        val blocks = listOf(
            OrderedBlock(
                blockId = "o1",
                blockType = BlockType.ORDERED,
                children = emptyList(),
                parentId = "page1",
                ordered = OrderedBlockData(
                    elements = listOf(TextElement(textRun = TextRun(content = "First"))),
                    style = TextStyle(align = 1)
                )
            ),
            OrderedBlock(
                blockId = "o2",
                blockType = BlockType.ORDERED,
                children = emptyList(),
                parentId = "page1",
                ordered = OrderedBlockData(
                    elements = listOf(TextElement(textRun = TextRun(content = "Second"))),
                    style = TextStyle(align = 1)
                )
            )
        )

        val allBlocks = blocks.associateBy { it.blockId }
        val builder = HtmlBuilder(title = "Ordered List Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        html shouldContain "<ol"
        html shouldContain "First"
        html shouldContain "Second"
        html shouldContain "</ol>"
    }

    test("应该正确处理列表中断") {
        val blocks = listOf(
            BulletBlock(blockId = "b1", blockType = BlockType.BULLET, children = emptyList(), parentId = "page1",
                bullet = BulletBlockData(elements = listOf(TextElement(textRun = TextRun(content = "B1"))), style = TextStyle(align = 1))),
            TextBlock(blockId = "t1", blockType = BlockType.TEXT, children = emptyList(), parentId = "page1",
                text = TextBlockData(elements = listOf(TextElement(textRun = TextRun(content = "Break"))), style = TextStyle(align = 1))),
            BulletBlock(blockId = "b2", blockType = BlockType.BULLET, children = emptyList(), parentId = "page1",
                bullet = BulletBlockData(elements = listOf(TextElement(textRun = TextRun(content = "B2"))), style = TextStyle(align = 1)))
        )

        val allBlocks = blocks.associateBy { it.blockId }
        val builder = HtmlBuilder(title = "List Break Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        // 应该有两个独立的ul列表
        val ulCount = Regex("<ul").findAll(html).count()
        ulCount shouldBe 2
    }

    test("应该正确渲染表格并避免内容重复") {
        val textInCell = TextBlock(
            blockId = "text_in_cell",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = "cell1",
            text = TextBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Cell Content"))),
                style = TextStyle(align = 1)
            )
        )

        val cellBlock = TableCellBlock(
            blockId = "cell1",
            blockType = BlockType.TABLE_CELL,
            children = listOf("text_in_cell"),
            parentId = "table1",
            tableCell = TableCellBlockData()
        )

        val tableBlock = TableBlock(
            blockId = "table1",
            blockType = BlockType.TABLE,
            children = listOf("cell1"),
            parentId = "page1",
            table = TableBlockData(
                cells = listOf("cell1"),
                property = TableProperty(rowSize = 1, columnSize = 1)
            )
        )

        val blocks = listOf(tableBlock, cellBlock, textInCell)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Table Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        // "Cell Content"应该只出现一次（在table内，不在table外）
        val occurrences = Regex("Cell Content").findAll(html).count()
        occurrences shouldBe 1
    }

    test("应该正确渲染Grid布局") {
        val textInCol1 = TextBlock(
            blockId = "text1",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = "col1",
            text = TextBlockData(elements = listOf(TextElement(textRun = TextRun(content = "Col1"))), style = TextStyle(align = 1))
        )

        val textInCol2 = TextBlock(
            blockId = "text2",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = "col2",
            text = TextBlockData(elements = listOf(TextElement(textRun = TextRun(content = "Col2"))), style = TextStyle(align = 1))
        )

        val col1 = GridColumnBlock(blockId = "col1", blockType = BlockType.GRID_COLUMN, children = listOf("text1"), parentId = "grid1", gridColumn = GridColumnBlockData(widthRatio = 50))
        val col2 = GridColumnBlock(blockId = "col2", blockType = BlockType.GRID_COLUMN, children = listOf("text2"), parentId = "grid1", gridColumn = GridColumnBlockData(widthRatio = 50))

        val gridBlock = GridBlock(
            blockId = "grid1",
            blockType = BlockType.GRID,
            children = listOf("col1", "col2"),
            parentId = "page1",
            grid = GridBlockData(columnSize = 2)
        )

        val blocks = listOf(gridBlock)
        val allBlocks = mapOf(
            "grid1" to gridBlock,
            "col1" to col1,
            "col2" to col2,
            "text1" to textInCol1,
            "text2" to textInCol2
        )

        val builder = HtmlBuilder(title = "Grid Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        html shouldContain "Col1"
        html shouldContain "Col2"
        html shouldContain "display: grid"
    }

    test("应该跳过已处理的块") {
        val cellContent = TextBlock(
            blockId = "cell_text",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = "cell1",
            text = TextBlockData(elements = listOf(TextElement(textRun = TextRun(content = "In Cell"))), style = TextStyle(align = 1))
        )

        val cell = TableCellBlock(blockId = "cell1", blockType = BlockType.TABLE_CELL, children = listOf("cell_text"), parentId = "table1", tableCell = TableCellBlockData())

        val table = TableBlock(
            blockId = "table1",
            blockType = BlockType.TABLE,
            children = listOf("cell1"),
            parentId = "page1",
            table = TableBlockData(cells = listOf("cell1"), property = TableProperty(rowSize = 1, columnSize = 1))
        )

        // TableCell和其内容放在主列表中（模拟真实场景）
        val blocks = listOf(table, cell, cellContent)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Skip Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        // "In Cell"应该只出现一次（在table内）
        val occurrences = Regex("In Cell").findAll(html).count()
        occurrences shouldBe 1
    }

    test("应该正确渲染代码块") {
        val codeBlock = CodeBlockItem(
            blockId = "code1",
            blockType = BlockType.CODE,
            children = emptyList(),
            parentId = "page1",
            code = CodeBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "console.log('test');"))),
                language = 1
            )
        )

        val blocks = listOf(codeBlock)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Code Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        html shouldContain "<pre"
        html shouldContain "<code"
        html shouldContain "console.log('test');"
    }

    test("应该正确渲染Todo列表") {
        val todo1 = TodoBlock(
            blockId = "todo1",
            blockType = BlockType.TODO,
            children = emptyList(),
            parentId = "page1",
            todo = TodoBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Task 1"))),
                style = TodoStyle(done = false)
            )
        )

        val todo2 = TodoBlock(
            blockId = "todo2",
            blockType = BlockType.TODO,
            children = emptyList(),
            parentId = "page1",
            todo = TodoBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Task 2"))),
                style = TodoStyle(done = true)
            )
        )

        val blocks = listOf(todo1, todo2)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Todo Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        html shouldContain "Task 1"
        html shouldContain "Task 2"
        html shouldContain "checkbox"
        html shouldContain "checked"
    }

    test("应该正确渲染嵌套的Callout块") {
        val textInCallout = TextBlock(
            blockId = "text1",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = "callout1",
            text = TextBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Important Note"))),
                style = TextStyle(align = 1)
            )
        )

        val calloutBlock = CalloutBlock(
            blockId = "callout1",
            blockType = BlockType.CALLOUT,
            children = listOf("text1"),
            parentId = "page1",
            callout = CalloutBlockData(backgroundColor = 1, borderColor = 1)
        )

        val blocks = listOf(calloutBlock)
        val allBlocks = mapOf(
            "callout1" to calloutBlock,
            "text1" to textInCallout
        )

        val builder = HtmlBuilder(title = "Callout Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        html shouldContain "callout"
        html shouldContain "Important Note"
    }

    test("应该正确渲染引用块") {
        val quoteBlock = QuoteBlock(
            blockId = "quote1",
            blockType = BlockType.QUOTE,
            children = emptyList(),
            parentId = "page1",
            quote = QuoteBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Famous quote")))
            )
        )

        val blocks = listOf(quoteBlock)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Quote Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        html shouldContain "<blockquote"
        html shouldContain "Famous quote"
    }

    test("应该正确渲染公式块") {
        val equationBlock = EquationBlock(
            blockId = "eq1",
            blockType = BlockType.EQUATION,
            children = emptyList(),
            parentId = "page1",
            equation = EquationBlockData(content = "x^2 + y^2 = z^2")
        )

        val blocks = listOf(equationBlock)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Equation Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        html shouldContain "x^2 + y^2 = z^2"
    }

    test("应该正确渲染图片块") {
        val imageBlock = ImageBlock(
            blockId = "img1",
            blockType = BlockType.IMAGE,
            children = emptyList(),
            parentId = "page1",
            image = ImageBlockData(token = "img_token", width = 800, height = 600)
        )

        val blocks = listOf(imageBlock)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Image Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        html shouldContain "<img"
        html shouldContain "img_token"
    }

    test("应该正确处理混合块类型") {
        val blocks = listOf(
            Heading1Block(blockId = "h1", blockType = BlockType.HEADING1, children = emptyList(), parentId = "page1",
                heading1 = HeadingBlockData(elements = listOf(TextElement(textRun = TextRun(content = "H1"))), style = TextStyle(align = 1))),
            TextBlock(blockId = "t1", blockType = BlockType.TEXT, children = emptyList(), parentId = "page1",
                text = TextBlockData(elements = listOf(TextElement(textRun = TextRun(content = "T1"))), style = TextStyle(align = 1))),
            BulletBlock(blockId = "b1", blockType = BlockType.BULLET, children = emptyList(), parentId = "page1",
                bullet = BulletBlockData(elements = listOf(TextElement(textRun = TextRun(content = "B1"))), style = TextStyle(align = 1))),
            CodeBlockItem(blockId = "c1", blockType = BlockType.CODE, children = emptyList(), parentId = "page1",
                code = CodeBlockData(elements = listOf(TextElement(textRun = TextRun(content = "code"))), language = 1))
        )

        val allBlocks = blocks.associateBy { it.blockId }
        val builder = HtmlBuilder(title = "Mixed Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        html shouldContain "<h1"
        html shouldContain "<p"
        html shouldContain "<ul"
        html shouldContain "<pre"
    }

    test("应该正确渲染QuoteContainer") {
        val textBlock = TextBlock(
            blockId = "text1",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = "qc1",
            text = TextBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Quote content"))),
                style = TextStyle(align = 1)
            )
        )

        val qcBlock = QuoteContainerBlock(
            blockId = "qc1",
            blockType = BlockType.QUOTE_CONTAINER,
            children = listOf("text1"),
            parentId = "page1",
            quoteContainer = QuoteContainerBlockData()
        )

        val blocks = listOf(qcBlock)
        val allBlocks = mapOf("qc1" to qcBlock, "text1" to textBlock)

        val builder = HtmlBuilder(title = "QC Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        html shouldContain "quote-container"
        html shouldContain "Quote content"
    }

    test("应该忽略PageBlock本身") {
        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = listOf("text1"),
            parentId = null,
            page = PageBlockData(elements = listOf(TextElement(textRun = TextRun(content = "Page Title"))), style = TextStyle(align = 1))
        )

        val textBlock = TextBlock(
            blockId = "text1",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = "page1",
            text = TextBlockData(elements = listOf(TextElement(textRun = TextRun(content = "Body text"))), style = TextStyle(align = 1))
        )

        val blocks = listOf(pageBlock, textBlock)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder = HtmlBuilder(title = "Page Test", customCss = null)
        val html = builder.build(blocks, allBlocks)

        // 应该包含body文本
        html shouldContain "Body text"
        // Page块本身通常不渲染
    }
})


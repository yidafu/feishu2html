package dev.yidafu.feishu2html.converter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class RenderContextTest : FunSpec({

    test("应该正确创建RenderContext") {
        val textConverter = TextElementConverter()
        val processedBlocks = mutableSetOf<String>()

        val context =
            RenderContext(
                textConverter = textConverter,
                processedBlocks = processedBlocks,
            )

        context.textConverter shouldBe textConverter
        context.processedBlocks shouldBe processedBlocks
    }

    test("processedBlocks应该能够添加和查询") {
        val context =
            RenderContext(
                textConverter = TextElementConverter(),
                processedBlocks = mutableSetOf(),
            )

        context.processedBlocks.add("block1")
        context.processedBlocks.add("block2")

        context.processedBlocks shouldHaveSize 2
        context.processedBlocks shouldContain "block1"
        context.processedBlocks shouldContain "block2"
    }

    test("processedBlocks应该避免重复") {
        val context =
            RenderContext(
                textConverter = TextElementConverter(),
                processedBlocks = mutableSetOf(),
            )

        context.processedBlocks.add("block1")
        context.processedBlocks.add("block1")
        context.processedBlocks.add("block1")

        context.processedBlocks shouldHaveSize 1
    }

    test("应该使用共享的TextElementConverter") {
        val converter = TextElementConverter()
        val context1 =
            RenderContext(
                textConverter = converter,
                processedBlocks = mutableSetOf(),
            )
        val context2 =
            RenderContext(
                textConverter = converter,
                processedBlocks = mutableSetOf(),
            )

        context1.textConverter shouldBe context2.textConverter
    }

    test("不同context应该有独立的processedBlocks") {
        val converter = TextElementConverter()
        val context1 =
            RenderContext(
                textConverter = converter,
                processedBlocks = mutableSetOf(),
            )
        val context2 =
            RenderContext(
                textConverter = converter,
                processedBlocks = mutableSetOf(),
            )

        context1.processedBlocks.add("block1")

        context1.processedBlocks shouldHaveSize 1
        context2.processedBlocks.shouldBeEmpty()
    }
})

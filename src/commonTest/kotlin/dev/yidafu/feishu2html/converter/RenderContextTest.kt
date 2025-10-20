package dev.yidafu.feishu2html.converter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Tests for RenderContext
 *
 * Since processedBlocks was removed in the recursive rendering refactoring,
 * these tests now focus on the core context properties.
 */
class RenderContextTest : FunSpec({

    test("应该正确创建RenderContext") {
        val textConverter = TextElementConverter()

        val context =
            RenderContext(
                textConverter = textConverter,
            )

        context.textConverter shouldBe textConverter
        context.imageBase64Cache shouldBe emptyMap()
        context.showUnsupportedBlocks shouldBe true
    }

    test("应该使用共享的TextElementConverter") {
        val converter = TextElementConverter()
        val context1 =
            RenderContext(
                textConverter = converter,
            )
        val context2 =
            RenderContext(
                textConverter = converter,
            )

        context1.textConverter shouldBe context2.textConverter
    }

    test("应该支持imageBase64Cache") {
        val cache = mapOf("img1" to "data:image/png;base64,...")

        val context =
            RenderContext(
                textConverter = TextElementConverter(),
                imageBase64Cache = cache,
            )

        context.imageBase64Cache shouldBe cache
        context.imageBase64Cache["img1"] shouldBe "data:image/png;base64,..."
    }

    test("应该支持showUnsupportedBlocks配置") {
        val contextWithWarnings =
            RenderContext(
                textConverter = TextElementConverter(),
                showUnsupportedBlocks = true,
            )

        val contextWithoutWarnings =
            RenderContext(
                textConverter = TextElementConverter(),
                showUnsupportedBlocks = false,
            )

        contextWithWarnings.showUnsupportedBlocks shouldBe true
        contextWithoutWarnings.showUnsupportedBlocks shouldBe false
    }

    test("RenderContext应该是data class") {
        val context1 =
            RenderContext(
                textConverter = TextElementConverter(),
            )

        val context2 =
            RenderContext(
                textConverter = context1.textConverter,
            )

        // data class should support copy
        val context3 = context1.copy(showUnsupportedBlocks = false)

        context3.textConverter shouldBe context1.textConverter
        context3.showUnsupportedBlocks shouldBe false
        context1.showUnsupportedBlocks shouldBe true
    }
})

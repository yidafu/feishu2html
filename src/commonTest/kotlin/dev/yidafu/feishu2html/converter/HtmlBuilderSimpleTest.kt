package dev.yidafu.feishu2html.converter

import dev.yidafu.feishu2html.api.model.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class HtmlBuilderSimpleTest : FunSpec({

    test("应该生成完整的HTML文档结构") {
        val textBlock =
            TextBlock(
                blockId = "text1",
                blockType = BlockType.TEXT,
                children = emptyList(),
                parentId = "page1",
                text =
                    TextBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Test content"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val blocks = listOf(textBlock)
        val allBlocks = blocks.associateBy { it.blockId }

        val builder =
            HtmlBuilder(
                title = "Test Document",
                customCss = null,
            )

        val html = builder.build(blocks, allBlocks)

        html shouldContain "<html"
        html shouldContain "<head>"
        html shouldContain "<title>Test Document</title>"
        html shouldContain "<body>"
        html shouldContain "Test content"
        html shouldContain "</body>"
        html shouldContain "</html>"
    }

    test("应该包含MathJax脚本") {
        val blocks = emptyList<Block>()
        val allBlocks = emptyMap<String, Block>()

        val builder =
            HtmlBuilder(
                title = "Test",
                customCss = null,
            )

        val html = builder.build(blocks, allBlocks)

        html shouldContain "MathJax"
        html shouldContain "tex-mml-chtml.js"
    }

    test("应该包含CSS样式") {
        val blocks = emptyList<Block>()
        val allBlocks = emptyMap<String, Block>()

        val builder =
            HtmlBuilder(
                title = "Test",
                cssMode = CssMode.INLINE,
                customCss = null,
            )

        val html = builder.build(blocks, allBlocks)

        html shouldContain "<style>"
        html shouldContain "</style>"
    }

    test("应该使用自定义CSS") {
        val customCss = "body { background: red; }"
        val blocks = emptyList<Block>()
        val allBlocks = emptyMap<String, Block>()

        val builder =
            HtmlBuilder(
                title = "Test",
                cssMode = CssMode.INLINE,
                customCss = customCss,
            )

        val html = builder.build(blocks, allBlocks)

        html shouldContain customCss
    }

    test("应该正确处理空块列表") {
        val blocks = emptyList<Block>()
        val allBlocks = emptyMap<String, Block>()

        val builder =
            HtmlBuilder(
                title = "Empty Document",
                customCss = null,
            )

        val html = builder.build(blocks, allBlocks)

        html shouldContain "<html"
        html shouldContain "Empty Document"
        html shouldContain "<body>"
        html shouldContain "</body>"
    }

    test("StyleMode.FEISHU should generate Feishu styles") {
        val blocks = emptyList<Block>()
        val allBlocks = emptyMap<String, Block>()

        val builder =
            HtmlBuilder(
                title = "Feishu Style Test",
                cssMode = CssMode.INLINE,
                styleMode = StyleMode.FEISHU,
            )

        val html = builder.build(blocks, allBlocks)

        // Should use Feishu class names
        html shouldContain """<div class="protyle-wysiwyg b3-typography" data-node-id="root">"""

        // Should contain Feishu CSS styles and classes
        html shouldContain ".heading"
        html shouldContain ".text-red"
        html shouldContain "Feishu Document Styles"

        // Should NOT contain GitHub variables
        html shouldNotContain "--gh-fg-default"
    }

    test("StyleMode.GITHUB should generate GitHub styles") {
        val blocks = emptyList<Block>()
        val allBlocks = emptyMap<String, Block>()

        val builder =
            HtmlBuilder(
                title = "GitHub Style Test",
                cssMode = CssMode.INLINE,
                styleMode = StyleMode.GITHUB,
            )

        val html = builder.build(blocks, allBlocks)

        // Should use the SAME Feishu class names
        html shouldContain """<div class="protyle-wysiwyg b3-typography" data-node-id="root">"""

        // Should contain GitHub CSS variables
        html shouldContain "--gh-fg-default"
        html shouldContain "--gh-bg-default"
        html shouldContain "--gh-border-default"

        // Should NOT contain Feishu variables
        html shouldNotContain "--b3-theme-primary"

        // Should support dark mode
        html shouldContain "prefers-color-scheme: dark"
    }

    test("Both style modes should use identical HTML class names") {
        val blocks = listOf(
            TextBlock(
                blockId = "test-1",
                parentId = null,
                children = emptyList(),
                blockType = dev.yidafu.feishu2html.api.model.BlockType.TEXT,
            ),
        )
        val allBlocks = blocks.associateBy { it.blockId }

        val feishuBuilder = HtmlBuilder(
            title = "Test",
            styleMode = StyleMode.FEISHU,
        )

        val githubBuilder = HtmlBuilder(
            title = "Test",
            styleMode = StyleMode.GITHUB,
        )

        val feishuHtml = feishuBuilder.build(blocks, allBlocks)
        val githubHtml = githubBuilder.build(blocks, allBlocks)

        // Extract the div class from both
        val classPattern = """<div class="protyle-wysiwyg b3-typography" data-node-id="root">"""

        feishuHtml shouldContain classPattern
        githubHtml shouldContain classPattern

        // Verify no .markdown-body class in either
        feishuHtml shouldNotContain "markdown-body"
        githubHtml shouldNotContain "markdown-body"
    }

    test("GitHub style should have light and dark theme support") {
        val blocks = emptyList<Block>()
        val allBlocks = emptyMap<String, Block>()

        val builder =
            HtmlBuilder(
                title = "GitHub Dark Mode Test",
                cssMode = CssMode.INLINE,
                styleMode = StyleMode.GITHUB,
            )

        val html = builder.build(blocks, allBlocks)

        // Should have media query for dark mode
        html shouldContain "@media (prefers-color-scheme: dark)"

        // Should have different colors for light and dark modes
        html shouldContain "#f0f6fc" // Dark mode text color
        html shouldContain "#1f2328" // Light mode text color
        html shouldContain "#0d1117" // Dark mode background
    }
})

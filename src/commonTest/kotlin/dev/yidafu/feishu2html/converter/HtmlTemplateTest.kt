package dev.yidafu.feishu2html.converter

import dev.yidafu.feishu2html.api.model.TextBlock
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.html.*

/**
 * Tests for HTML template customization feature
 */
class HtmlTemplateTest : FunSpec({

    test("should use default template when no template is specified") {
        val builder = HtmlBuilder(
            title = "Default Template Test",
            customCss = null,
        )
        val blocks = listOf(
            TextBlock(
                blockId = "test-1",
                parentId = null,
                children = emptyList(),
                blockType = dev.yidafu.feishu2html.api.model.BlockType.TEXT,
            ),
        )
        val html = builder.build(blocks, blocks.associateBy { it.blockId })

        html shouldContain "<html"
        html shouldContain "<title>Default Template Test</title>"
        html shouldContain """<div class="protyle-wysiwyg b3-typography" data-node-id="root">"""
        html shouldContain "MathJax"
    }

    test("should use plain template with complete control") {
        val customTemplate = HtmlTemplate.Plain { content ->
            lang = "en"
            head {
                title("Custom Full Template")
                meta(name = "custom", content = "value")
            }
            body {
                div(classes = "my-custom-wrapper") {
                    h1 { +"Custom Header" }
                    content()
                }
            }
        }

        val builder = HtmlBuilder(
            title = "Will be ignored", // Title parameter will be ignored in plain template with custom builder
            template = customTemplate,
        )
        val blocks = listOf(
            TextBlock(
                blockId = "test-1",
                parentId = null,
                children = emptyList(),
                blockType = dev.yidafu.feishu2html.api.model.BlockType.TEXT,
            ),
        )
        val html = builder.build(blocks, blocks.associateBy { it.blockId })

        html shouldContain "<html"
        html shouldContain "<title>Custom Full Template</title>"
        html shouldContain """<meta name="custom" content="value">"""
        html shouldContain """<div class="my-custom-wrapper">"""
        html shouldContain "<h1>Custom Header</h1>"
        html shouldContain """<div class="protyle-wysiwyg b3-typography" data-node-id="root">"""
        // Plain template doesn't include MathJax by default
        html shouldNotContain "MathJax"
    }

    test("should use fragment template without html/head/body tags") {
        val customTemplate = HtmlTemplate.Fragment { content ->
            div(classes = "custom-body-wrapper") {
                header {
                    h1 { +"My Custom Header" }
                }
                main {
                    content()
                }
                footer {
                    p { +"© 2025 Custom Footer" }
                }
            }
        }

        val builder = HtmlBuilder(
            title = "Fragment Template Test",
            template = customTemplate,
        )
        val blocks = listOf(
            TextBlock(
                blockId = "test-1",
                parentId = null,
                children = emptyList(),
                blockType = dev.yidafu.feishu2html.api.model.BlockType.TEXT,
            ),
        )
        val html = builder.build(blocks, blocks.associateBy { it.blockId })

        // Fragment template should NOT contain html/head/body tags
        html shouldNotContain "<html"
        html shouldNotContain "<title>"
        html shouldNotContain "<head>"
        html shouldNotContain "MathJax"
        // Only contains the fragment content
        html shouldContain """<div class="custom-body-wrapper">"""
        html shouldContain "<header>"
        html shouldContain "<h1>My Custom Header</h1>"
        html shouldContain "<main>"
        html shouldContain """<div class="protyle-wysiwyg b3-typography" data-node-id="root">"""
        html shouldContain "<footer>"
        html shouldContain "© 2025 Custom Footer"
    }

    test("should work with fragment template as pure HTML fragment") {
        val customTemplate = HtmlTemplate.Fragment { content ->
            div(classes = "wrapper") {
                content()
            }
        }

        val builder = HtmlBuilder(
            title = "Fragment with Inline CSS",
            cssMode = CssMode.INLINE,
            template = customTemplate,
        )
        val blocks = listOf(
            TextBlock(
                blockId = "test-1",
                parentId = null,
                children = emptyList(),
                blockType = dev.yidafu.feishu2html.api.model.BlockType.TEXT,
            ),
        )
        val html = builder.build(blocks, blocks.associateBy { it.blockId })

        // Fragment should NOT contain style tags or html structure
        html shouldNotContain "<style>"
        html shouldNotContain "<html"
        html shouldNotContain "<body>"
        // Only the fragment content
        html shouldContain """<div class="wrapper">"""
    }

    test("should allow plain template with custom MathJax config") {
        val customTemplate = HtmlTemplate.Plain { content ->
            lang = "en"
            head {
                title("Custom MathJax")
                script {
                    src = "https://cdn.jsdelivr.net/npm/mathjax@4/es5/tex-chtml.js"
                }
            }
            body {
                content()
            }
        }

        val builder = HtmlBuilder(
            title = "Custom MathJax",
            template = customTemplate,
        )
        val blocks = listOf(
            TextBlock(
                blockId = "test-1",
                parentId = null,
                children = emptyList(),
                blockType = dev.yidafu.feishu2html.api.model.BlockType.TEXT,
            ),
        )
        val html = builder.build(blocks, blocks.associateBy { it.blockId })

        html shouldContain "mathjax@4"
        html shouldNotContain "mathjax@3"
    }
})

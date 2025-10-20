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

    test("should use default template with GitHub style mode") {
        val builder = HtmlBuilder(
            title = "GitHub Style Test",
            cssMode = CssMode.INLINE,
            styleMode = StyleMode.GITHUB,
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

        // Should have standard HTML structure
        html shouldContain "<html"
        html shouldContain "<title>GitHub Style Test</title>"
        html shouldContain "MathJax"

        // Should use Feishu class names (not .markdown-body)
        html shouldContain """<div class="protyle-wysiwyg b3-typography" data-node-id="root">"""
        html shouldNotContain "markdown-body"

        // Should contain GitHub CSS variables
        html shouldContain "--gh-fg-default"
        html shouldContain "--gh-bg-default"
        html shouldContain "prefers-color-scheme: dark"
    }

    test("should use plain template with GitHub style mode") {
        val builder = HtmlBuilder(
            title = "Plain GitHub Style",
            cssMode = CssMode.INLINE,
            styleMode = StyleMode.GITHUB,
            template = HtmlTemplate.Plain(),
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

        // Plain template should not have external JS
        html shouldNotContain "MathJax"

        // Should use Feishu class names
        html shouldContain """<div class="protyle-wysiwyg b3-typography" data-node-id="root">"""

        // Should contain GitHub CSS variables in inline style
        html shouldContain "--gh-fg-default"
        html shouldContain "--gh-bg-muted"
    }

    test("should use fragment template with GitHub style mode") {
        val customTemplate = HtmlTemplate.Fragment { content ->
            div(classes = "wrapper") {
                content()
            }
        }

        val builder = HtmlBuilder(
            title = "Fragment GitHub Style",
            styleMode = StyleMode.GITHUB,
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

        // Fragment should not contain html/head/body
        html shouldNotContain "<html"
        html shouldNotContain "<style>"

        // Should still use Feishu class names in content
        html shouldContain """<div class="protyle-wysiwyg b3-typography" data-node-id="root">"""
        html shouldContain """<div class="wrapper">"""
    }

    test("should maintain same HTML class structure for both Feishu and GitHub styles") {
        val feishuBuilder = HtmlBuilder(
            title = "Test",
            cssMode = CssMode.INLINE,
            styleMode = StyleMode.FEISHU,
        )

        val githubBuilder = HtmlBuilder(
            title = "Test",
            cssMode = CssMode.INLINE,
            styleMode = StyleMode.GITHUB,
        )

        val blocks = listOf(
            TextBlock(
                blockId = "test-1",
                parentId = null,
                children = emptyList(),
                blockType = dev.yidafu.feishu2html.api.model.BlockType.TEXT,
            ),
        )

        val feishuHtml = feishuBuilder.build(blocks, blocks.associateBy { it.blockId })
        val githubHtml = githubBuilder.build(blocks, blocks.associateBy { it.blockId })

        // Both should use the same class names
        val feishuHasClass = feishuHtml.contains("""<div class="protyle-wysiwyg b3-typography" data-node-id="root">""")
        val githubHasClass = githubHtml.contains("""<div class="protyle-wysiwyg b3-typography" data-node-id="root">""")

        feishuHasClass shouldBe true
        githubHasClass shouldBe true

        // But should have different CSS content
        feishuHtml shouldContain "Feishu Document Styles"
        githubHtml shouldContain "--gh-fg-default"
        githubHtml shouldContain "GitHub-Style CSS"
    }
})

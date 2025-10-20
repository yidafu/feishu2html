package dev.yidafu.feishu2html.cli

import dev.yidafu.feishu2html.converter.HtmlTemplate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull

/**
 * Tests for CLI argument parsing with kotlinx-cli
 */
class CliRunnerTest : FunSpec({

    test("should parse basic arguments without template option") {
        val args = arrayOf("app_id", "app_secret", "doc1")
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.appId shouldBe "app_id"
        result.appSecret shouldBe "app_secret"
        result.documentIds shouldBe listOf("doc1")
        result.template shouldBe HtmlTemplate.DefaultCli
    }

    test("should parse multiple document IDs") {
        val args = arrayOf("app_id", "app_secret", "doc1", "doc2", "doc3")
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.documentIds shouldBe listOf("doc1", "doc2", "doc3")
    }

    test("should parse --template default option") {
        val args = arrayOf("--template", "default", "app_id", "app_secret", "doc1")
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.template shouldBe HtmlTemplate.DefaultCli
        result.appId shouldBe "app_id"
        result.appSecret shouldBe "app_secret"
        result.documentIds shouldBe listOf("doc1")
    }

    test("should parse --template fragment option") {
        val args = arrayOf("--template", "fragment", "app_id", "app_secret", "doc1")
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.template shouldBe HtmlTemplate.FragmentCli
    }

    test("should parse --template full option") {
        val args = arrayOf("--template", "full", "app_id", "app_secret", "doc1", "doc2")
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.template shouldBe HtmlTemplate.PlainCli
        result.documentIds shouldBe listOf("doc1", "doc2")
    }

    test("should parse -t short form option") {
        val args = arrayOf("-t", "fragment", "app_id", "app_secret", "doc1")
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.template shouldBe HtmlTemplate.FragmentCli
    }

    test("should be case insensitive for template mode") {
        val args1 = arrayOf("--template", "FRAGMENT", "app_id", "app_secret", "doc1")
        val result1 = CliRunner.parseArguments(args1)
        result1.shouldNotBeNull()
        result1.template shouldBe HtmlTemplate.FragmentCli

        val args2 = arrayOf("--template", "Full", "app_id", "app_secret", "doc1")
        val result2 = CliRunner.parseArguments(args2)
        result2.shouldNotBeNull()
        result2.template shouldBe HtmlTemplate.PlainCli
    }

    test("should handle --template option with multiple documents") {
        val args = arrayOf("--template", "fragment", "app_id", "app_secret", "doc1", "doc2", "doc3", "doc4")
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.template shouldBe HtmlTemplate.FragmentCli
        result.documentIds shouldBe listOf("doc1", "doc2", "doc3", "doc4")
    }

    test("should parse --inline-images option") {
        val args = arrayOf("--inline-images", "app_id", "app_secret", "doc1")
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.inlineImages shouldBe true
    }

    test("should default --inline-images to false when not specified") {
        val args = arrayOf("app_id", "app_secret", "doc1")
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.inlineImages shouldBe false
    }

    test("should parse --inline-css option") {
        val args = arrayOf("--inline-css", "app_id", "app_secret", "doc1")
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.inlineCss shouldBe true
    }

    test("should default --inline-css to false when not specified") {
        val args = arrayOf("app_id", "app_secret", "doc1")
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.inlineCss shouldBe false
    }

    test("should parse --hide-unsupported option") {
        val args = arrayOf("--hide-unsupported", "app_id", "app_secret", "doc1")
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.hideUnsupported shouldBe true
    }

    test("should default --hide-unsupported to false when not specified") {
        val args = arrayOf("app_id", "app_secret", "doc1")
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.hideUnsupported shouldBe false
    }

    test("should parse all options combined") {
        val args = arrayOf(
            "--template", "fragment",
            "--inline-images",
            "--inline-css",
            "--hide-unsupported",
            "my_app_id",
            "my_secret",
            "doc1",
            "doc2"
        )
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.template shouldBe HtmlTemplate.FragmentCli
        result.inlineImages shouldBe true
        result.inlineCss shouldBe true
        result.hideUnsupported shouldBe true
        result.appId shouldBe "my_app_id"
        result.appSecret shouldBe "my_secret"
        result.documentIds shouldBe listOf("doc1", "doc2")
    }

    test("should parse options in any order") {
        val args = arrayOf(
            "--inline-images",
            "--template", "full",
            "app_id",
            "app_secret",
            "doc1"
        )
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.appId shouldBe "app_id"
        result.appSecret shouldBe "app_secret"
        result.documentIds shouldBe listOf("doc1")
        result.inlineImages shouldBe true
        result.template shouldBe HtmlTemplate.PlainCli
    }

    test("should handle --help flag") {
        val args = arrayOf("--help")
        val result = CliRunner.parseArguments(args)

        result.shouldBeNull()
    }

    test("should handle -h flag") {
        val args = arrayOf("-h")
        val result = CliRunner.parseArguments(args)

        result.shouldBeNull()
    }

    test("should parse short and long options together") {
        val args = arrayOf(
            "-t", "fragment",
            "--inline-images",
            "app_id",
            "app_secret",
            "doc1"
        )
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.template shouldBe HtmlTemplate.FragmentCli
        result.inlineImages shouldBe true
    }

    test("should handle complex document IDs") {
        val args = arrayOf(
            "app_id",
            "app_secret",
            "doxcnABCDEFG123456",
            "doxcnXYZ789",
            "wikicnTest123"
        )
        val result = CliRunner.parseArguments(args)

        result.shouldNotBeNull()
        result.documentIds shouldBe listOf("doxcnABCDEFG123456", "doxcnXYZ789", "wikicnTest123")
    }

    // Note: The following tests are commented out because kotlinx-cli may call exitProcess()
    // which terminates the test process. These edge cases are better tested manually.

    /*
    test("should return null for insufficient arguments") {
        val args = arrayOf("app_id", "app_secret")
        val result = CliRunner.parseArguments(args)
        result.shouldBeNull()
    }

    test("should return null for invalid template mode") {
        val args = arrayOf("--template", "invalid", "app_id", "app_secret", "doc1")
        val result = CliRunner.parseArguments(args)
        result.shouldBeNull()
    }
    */
})

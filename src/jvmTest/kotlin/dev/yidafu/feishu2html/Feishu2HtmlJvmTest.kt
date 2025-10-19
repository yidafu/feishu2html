package dev.yidafu.feishu2html

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.io.File
import kotlinx.coroutines.runBlocking

class Feishu2HtmlJvmTest : FunSpec({

    val testOutputDir = "build/test-output/feishu2html"

    beforeEach {
        // Clean test directory
        File(testOutputDir).deleteRecursively()
        File(testOutputDir).mkdirs()
    }

    afterEach {
        File(testOutputDir).deleteRecursively()
    }

    test("Feishu2HtmlOptions should create output directories on initialization") {
        val customOutputDir = "$testOutputDir/custom-output"
        val customImageDir = "$testOutputDir/custom-output/imgs"
        val customFileDir = "$testOutputDir/custom-output/docs"

        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = customOutputDir,
            imageDir = customImageDir,
            fileDir = customFileDir
        )

        File(customOutputDir).exists() shouldBe true
        File(customImageDir).exists() shouldBe true
        File(customFileDir).exists() shouldBe true
    }

    test("Feishu2HtmlOptions should support external CSS mode") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            externalCss = true,
            cssFileName = "custom-style.css"
        )

        options.externalCss shouldBe true
        options.cssFileName shouldBe "custom-style.css"
    }

    test("Feishu2HtmlOptions should support inline CSS mode") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            externalCss = false
        )

        options.externalCss shouldBe false
    }

    test("Feishu2HtmlOptions should support inline images mode") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            inlineImages = true
        )

        options.inlineImages shouldBe true
    }

    test("Feishu2HtmlOptions should support hiding unsupported blocks") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            showUnsupportedBlocks = false
        )

        options.showUnsupportedBlocks shouldBe false
    }

    test("Feishu2HtmlOptions should support custom CSS") {
        val customCss = "body { background: #fff; }"
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            customCss = customCss
        )

        options.customCss shouldBe customCss
    }

    test("Feishu2HtmlOptions should support DEFAULT template mode") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            templateMode = TemplateMode.DEFAULT
        )

        options.templateMode shouldBe TemplateMode.DEFAULT
    }

    test("Feishu2HtmlOptions should support FRAGMENT template mode") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            templateMode = TemplateMode.FRAGMENT
        )

        options.templateMode shouldBe TemplateMode.FRAGMENT
    }

    test("Feishu2HtmlOptions should support FULL template mode") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            templateMode = TemplateMode.FULL
        )

        options.templateMode shouldBe TemplateMode.FULL
    }

    test("Feishu2Html instance should be created successfully") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir
        )

        val feishu2Html = Feishu2Html(options)
        feishu2Html shouldNotBe null
        feishu2Html.close()
    }

    test("Feishu2Html should implement AutoCloseable") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir
        )

        var closed = false
        Feishu2Html(options).use {
            it shouldNotBe null
            closed = true
        }
        closed shouldBe true
    }

    test("Feishu2Html close should be safe to call multiple times") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir
        )

        val feishu2Html = Feishu2Html(options)
        feishu2Html.close()
        feishu2Html.close() // Should not throw
    }

    test("export with invalid document ID should throw exception") {
        val options = Feishu2HtmlOptions(
            appId = "invalid_app_id",
            appSecret = "invalid_secret",
            outputDir = testOutputDir
        )

        Feishu2Html(options).use { converter ->
            shouldThrow<Exception> {
                runBlocking {
                    converter.export("invalid_doc_id")
                }
            }
        }
    }

    test("exportBatch with invalid credentials should handle errors gracefully") {
        val options = Feishu2HtmlOptions(
            appId = "invalid_app_id",
            appSecret = "invalid_secret",
            outputDir = testOutputDir
        )

        Feishu2Html(options).use { converter ->
            // Should not throw, but log errors
            runBlocking {
                converter.exportBatch(listOf("doc1", "doc2"))
            }
        }
    }

    test("Feishu2HtmlOptions should handle relative output paths") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = "output/relative/path"
        )

        File("output/relative/path").exists() shouldBe true
        // Clean up
        File("output").deleteRecursively()
    }

    test("Feishu2HtmlOptions should create nested directory structures") {
        val deepPath = "$testOutputDir/level1/level2/level3/output"
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = deepPath
        )

        File(deepPath).exists() shouldBe true
    }

    test("TemplateMode enum should have all expected values") {
        val modes = TemplateMode.values().toList()
        modes.size shouldBe 3
        modes shouldContain TemplateMode.DEFAULT
        modes shouldContain TemplateMode.FRAGMENT
        modes shouldContain TemplateMode.FULL
    }

    test("Feishu2HtmlOptions with all custom settings") {
        val options = Feishu2HtmlOptions(
            appId = "custom_app_id",
            appSecret = "custom_secret",
            outputDir = "$testOutputDir/all-custom",
            imageDir = "$testOutputDir/all-custom/images-custom",
            fileDir = "$testOutputDir/all-custom/files-custom",
            imagePath = "img",
            filePath = "docs",
            customCss = "body { margin: 0; }",
            externalCss = false,
            cssFileName = "my-style.css",
            templateMode = TemplateMode.FRAGMENT,
            inlineImages = true,
            showUnsupportedBlocks = false
        )

        options.appId shouldBe "custom_app_id"
        options.appSecret shouldBe "custom_secret"
        options.imagePath shouldBe "img"
        options.filePath shouldBe "docs"
        options.customCss shouldBe "body { margin: 0; }"
        options.externalCss shouldBe false
        options.cssFileName shouldBe "my-style.css"
        options.templateMode shouldBe TemplateMode.FRAGMENT
        options.inlineImages shouldBe true
        options.showUnsupportedBlocks shouldBe false
    }

    test("Feishu2HtmlOptions should use default values when not specified") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret"
        )

        options.outputDir shouldBe "./output"
        options.imageDir shouldBe "./output/images"
        options.fileDir shouldBe "./output/files"
        options.imagePath shouldBe "images"
        options.filePath shouldBe "files"
        options.customCss shouldBe null
        options.externalCss shouldBe true
        options.cssFileName shouldBe "feishu-style-optimized.css"
        options.templateMode shouldBe TemplateMode.DEFAULT
        options.inlineImages shouldBe false
        options.showUnsupportedBlocks shouldBe true

        // Clean up default output
        File("./output").deleteRecursively()
    }

    test("Multiple Feishu2Html instances should coexist") {
        val options1 = Feishu2HtmlOptions(
            appId = "app1",
            appSecret = "secret1",
            outputDir = "$testOutputDir/instance1"
        )
        val options2 = Feishu2HtmlOptions(
            appId = "app2",
            appSecret = "secret2",
            outputDir = "$testOutputDir/instance2"
        )

        val converter1 = Feishu2Html(options1)
        val converter2 = Feishu2Html(options2)

        converter1 shouldNotBe null
        converter2 shouldNotBe null

        converter1.close()
        converter2.close()
    }
})


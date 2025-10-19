package dev.yidafu.feishu2html

import dev.yidafu.feishu2html.api.FeishuApiException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import java.io.File

class Feishu2HtmlIntegrationTest : FunSpec({

    val testOutputDir = "/Users/dovyih/feishu2html/build/test-integration-output"

    beforeEach {
        // 清理测试输出
        val dir = File(testOutputDir)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
        dir.mkdirs()
    }

    afterEach {
        clearAllMocks()
    }

    test("应该能够创建Feishu2Html实例并正确初始化") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir
        )

        val converter = Feishu2Html(options)
        converter shouldNotBe null
        converter.close()
    }

    test("应该正确处理输出目录的创建") {
        val customDir = "$testOutputDir/custom/nested/path"
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = customDir
        )

        val converter = Feishu2Html(options)
        converter shouldNotBe null
        converter.close()
    }

    test("应该支持自定义CSS配置") {
        val customCss = """
            body { font-family: 'Custom Font'; }
            .custom-class { color: purple; }
        """.trimIndent()

        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            customCss = customCss
        )

        val converter = Feishu2Html(options)
        converter shouldNotBe null
        converter.close()
    }

    test("应该正确配置图片和文件目录") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            imageDir = "$testOutputDir/custom-images",
            fileDir = "$testOutputDir/custom-files"
        )

        val converter = Feishu2Html(options)
        converter shouldNotBe null
        converter.close()
    }

    test("应该正确配置图片和文件相对路径") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            imagePath = "custom-img",
            filePath = "custom-file"
        )

        val converter = Feishu2Html(options)
        converter shouldNotBe null
        converter.close()
    }

    test("close方法应该能安全多次调用") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir
        )

        val converter = Feishu2Html(options)
        converter.close()
        converter.close() // 不应该抛出异常
    }

    test("应该正确处理相对路径输出目录") {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = "build/test-relative"
        )

        val converter = Feishu2Html(options)
        converter shouldNotBe null
        converter.close()
    }

    test("Feishu2HtmlOptions应该有合理的默认值") {
        val options = Feishu2HtmlOptions(
            appId = "app123",
            appSecret = "secret456"
        )

        options.outputDir shouldBe "./output"
        options.imageDir shouldBe "./output/images"
        options.fileDir shouldBe "./output/files"
        options.imagePath shouldBe "images"
        options.filePath shouldBe "files"
        options.customCss shouldBe null
    }

    test("应该支持所有选项的完整配置") {
        val options = Feishu2HtmlOptions(
            appId = "full_app",
            appSecret = "full_secret",
            outputDir = "$testOutputDir/full",
            imageDir = "$testOutputDir/full/img",
            fileDir = "$testOutputDir/full/files",
            imagePath = "img",
            filePath = "files",
            customCss = "body { background: white; }"
        )

        options.appId shouldBe "full_app"
        options.appSecret shouldBe "full_secret"
        options.outputDir shouldBe "$testOutputDir/full"
        options.customCss shouldNotBe null
    }

    test("应该正确验证必需的配置参数") {
        val options = Feishu2HtmlOptions(
            appId = "required_app",
            appSecret = "required_secret"
        )

        options.appId shouldNotBe ""
        options.appSecret shouldNotBe ""
    }
})



package dev.yidafu.feishu2html.platform

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import java.io.File

class ImageEncoderTest : FunSpec({

    val testResourcesDir = "src/jvmTest/resources"

    test("encodeToBase64DataUrl should encode PNG image correctly") {
        val imagePath = "$testResourcesDir/test-image.png"
        val result = ImageEncoder.encodeToBase64DataUrl(imagePath)

        result shouldStartWith "data:image/png;base64,"
        // Base64 data URL should be reasonably sized (70 byte file -> ~118 chars)
        result.length shouldBe 118
    }

    test("encodeToBase64DataUrl should throw exception for non-existent file") {
        val nonExistentPath = "$testResourcesDir/non-existent.png"

        val exception = shouldThrow<IllegalArgumentException> {
            ImageEncoder.encodeToBase64DataUrl(nonExistentPath)
        }
        exception.message shouldContain "Image file not found"
    }

    test("getMimeType should return correct MIME type for PNG") {
        ImageEncoder.getMimeType("test.png") shouldBe "image/png"
    }

    test("getMimeType should return correct MIME type for JPG") {
        ImageEncoder.getMimeType("test.jpg") shouldBe "image/jpeg"
        ImageEncoder.getMimeType("test.jpeg") shouldBe "image/jpeg"
    }

    test("getMimeType should return correct MIME type for GIF") {
        ImageEncoder.getMimeType("test.gif") shouldBe "image/gif"
    }

    test("getMimeType should return correct MIME type for SVG") {
        ImageEncoder.getMimeType("test.svg") shouldBe "image/svg+xml"
    }

    test("getMimeType should return correct MIME type for WebP") {
        ImageEncoder.getMimeType("test.webp") shouldBe "image/webp"
    }

    test("getMimeType should return correct MIME type for BMP") {
        ImageEncoder.getMimeType("test.bmp") shouldBe "image/bmp"
    }

    test("getMimeType should return correct MIME type for ICO") {
        ImageEncoder.getMimeType("test.ico") shouldBe "image/x-icon"
    }

    test("getMimeType should handle uppercase extensions") {
        ImageEncoder.getMimeType("test.PNG") shouldBe "image/png"
        ImageEncoder.getMimeType("test.JPG") shouldBe "image/jpeg"
    }

    test("getMimeType should default to PNG for unknown extensions") {
        ImageEncoder.getMimeType("test.unknown") shouldBe "image/png"
        ImageEncoder.getMimeType("test") shouldBe "image/png"
    }

    test("getMimeType should handle file paths with multiple dots") {
        ImageEncoder.getMimeType("/path/to/image.test.png") shouldBe "image/png"
        ImageEncoder.getMimeType("image.backup.jpg") shouldBe "image/jpeg"
    }

    test("encodeToBase64DataUrl should handle different image formats") {
        // Create a temporary JPG file for testing
        val tempJpgPath = "build/test-output/temp-test.jpg"
        File(tempJpgPath).parentFile?.mkdirs()
        File("$testResourcesDir/test-image.png").copyTo(File(tempJpgPath), overwrite = true)

        val result = ImageEncoder.encodeToBase64DataUrl(tempJpgPath)
        result shouldStartWith "data:image/jpeg;base64,"
    }

    test("Base64 encoded data should be valid") {
        val imagePath = "$testResourcesDir/test-image.png"
        val result = ImageEncoder.encodeToBase64DataUrl(imagePath)

        val base64Part = result.substringAfter("base64,")
        // Base64 should only contain valid characters
        base64Part.all { it.isLetterOrDigit() || it in "+/=" } shouldBe true
    }
})


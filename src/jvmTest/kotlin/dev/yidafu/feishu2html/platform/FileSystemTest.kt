package dev.yidafu.feishu2html.platform

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File

class FileSystemTest : FunSpec({

    val testOutputDir = "build/test-output/filesystem"
    val fileSystem = PlatformFileSystem()

    beforeEach {
        // Clean up test directory
        File(testOutputDir).deleteRecursively()
    }

    afterEach {
        // Clean up after tests
        File(testOutputDir).deleteRecursively()
    }

    test("createDirectories should create single directory") {
        val dirPath = "$testOutputDir/single"
        fileSystem.createDirectories(dirPath)

        File(dirPath).exists() shouldBe true
        File(dirPath).isDirectory shouldBe true
    }

    test("createDirectories should create nested directories") {
        val dirPath = "$testOutputDir/level1/level2/level3"
        fileSystem.createDirectories(dirPath)

        File(dirPath).exists() shouldBe true
        File(dirPath).isDirectory shouldBe true
        File("$testOutputDir/level1").exists() shouldBe true
        File("$testOutputDir/level1/level2").exists() shouldBe true
    }

    test("createDirectories should not fail if directory already exists") {
        val dirPath = "$testOutputDir/existing"
        fileSystem.createDirectories(dirPath)
        // Call again - should not throw
        fileSystem.createDirectories(dirPath)

        File(dirPath).exists() shouldBe true
    }

    test("exists should return true for existing file") {
        val filePath = "$testOutputDir/existing-file.txt"
        File(filePath).parentFile.mkdirs()
        File(filePath).writeText("test content")

        fileSystem.exists(filePath) shouldBe true
    }

    test("exists should return true for existing directory") {
        val dirPath = "$testOutputDir/existing-dir"
        File(dirPath).mkdirs()

        fileSystem.exists(dirPath) shouldBe true
    }

    test("exists should return false for non-existent file") {
        val filePath = "$testOutputDir/non-existent.txt"

        fileSystem.exists(filePath) shouldBe false
    }

    test("writeText should write text content to file") {
        val filePath = "$testOutputDir/text-file.txt"
        val content = "Hello, World!"

        fileSystem.writeText(filePath, content)

        File(filePath).exists() shouldBe true
        File(filePath).readText() shouldBe content
    }

    test("writeText should create parent directories automatically") {
        val filePath = "$testOutputDir/deep/nested/path/file.txt"
        val content = "Auto-created parents"

        fileSystem.writeText(filePath, content)

        File(filePath).exists() shouldBe true
        File(filePath).readText() shouldBe content
        File("$testOutputDir/deep/nested/path").exists() shouldBe true
    }

    test("writeText should overwrite existing file") {
        val filePath = "$testOutputDir/overwrite.txt"
        fileSystem.writeText(filePath, "Original content")
        fileSystem.writeText(filePath, "New content")

        File(filePath).readText() shouldBe "New content"
    }

    test("writeText should handle Unicode content") {
        val filePath = "$testOutputDir/unicode.txt"
        val content = "你好世界 🌍 Привет мир"

        fileSystem.writeText(filePath, content)

        File(filePath).readText() shouldBe content
    }

    test("writeBytes should write binary content to file") {
        val filePath = "$testOutputDir/binary-file.bin"
        val content = byteArrayOf(0x00, 0x01, 0x02, 0x03, 0xFF.toByte())

        fileSystem.writeBytes(filePath, content)

        File(filePath).exists() shouldBe true
        File(filePath).readBytes().contentEquals(content) shouldBe true
    }

    test("writeBytes should create parent directories automatically") {
        val filePath = "$testOutputDir/deep/nested/binary/file.bin"
        val content = byteArrayOf(0x42, 0x69, 0x6E)

        fileSystem.writeBytes(filePath, content)

        File(filePath).exists() shouldBe true
        File(filePath).readBytes().contentEquals(content) shouldBe true
    }

    test("writeBytes should overwrite existing file") {
        val filePath = "$testOutputDir/overwrite.bin"
        fileSystem.writeBytes(filePath, byteArrayOf(0x01, 0x02))
        fileSystem.writeBytes(filePath, byteArrayOf(0x03, 0x04, 0x05))

        File(filePath).readBytes().contentEquals(byteArrayOf(0x03, 0x04, 0x05)) shouldBe true
    }

    test("writeBytes should handle empty byte array") {
        val filePath = "$testOutputDir/empty.bin"
        val content = byteArrayOf()

        fileSystem.writeBytes(filePath, content)

        File(filePath).exists() shouldBe true
        File(filePath).length() shouldBe 0
    }

    test("writeText should handle empty string") {
        val filePath = "$testOutputDir/empty.txt"

        fileSystem.writeText(filePath, "")

        File(filePath).exists() shouldBe true
        File(filePath).readText() shouldBe ""
    }

    test("writeText should handle large content") {
        val filePath = "$testOutputDir/large.txt"
        val content = "A".repeat(10000)

        fileSystem.writeText(filePath, content)

        File(filePath).readText() shouldBe content
        File(filePath).length() shouldBe 10000
    }

    test("getPlatformFileSystem should return valid instance") {
        val fs = getPlatformFileSystem()
        val testPath = "$testOutputDir/platform-test.txt"

        fs.writeText(testPath, "test")

        File(testPath).exists() shouldBe true
    }
})


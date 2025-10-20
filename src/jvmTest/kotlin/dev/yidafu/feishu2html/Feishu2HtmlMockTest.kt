package dev.yidafu.feishu2html

import dev.yidafu.feishu2html.api.FeishuApiClient
import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.platform.PlatformFileSystem
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.*
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Mock tests for Feishu2Html core functionality using dependency injection
 */
class Feishu2HtmlMockTest : FunSpec({

    val testOutputDir = "build/test-output/feishu2html-mock"
    lateinit var mockApiClient: FeishuApiClient
    lateinit var mockFileSystem: PlatformFileSystem
    lateinit var options: Feishu2HtmlOptions

    beforeEach {
        File(testOutputDir).deleteRecursively()
        File(testOutputDir).mkdirs()

        mockApiClient = mockk()
        mockFileSystem = mockk(relaxed = true)

        options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            imageDir = "$testOutputDir/images",
            fileDir = "$testOutputDir/files"
        )

        // Mock file system operations
        every { mockFileSystem.createDirectories(any()) } just Runs
        every { mockFileSystem.exists(any()) } returns false
        every { mockFileSystem.writeText(any(), any()) } just Runs
        every { mockFileSystem.writeBytes(any(), any()) } just Runs
    }

    afterEach {
        clearAllMocks()
        File(testOutputDir).deleteRecursively()
    }

    test("export should successfully export a simple document") {
        // Prepare mock data
        val documentId = "test_doc_123"
        val documentInfo = DocumentInfo(
            documentId = documentId,
            revisionId = 1,
            title = "Test Document"
        )

        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = listOf("text1"),
            parentId = null,
            page = PageBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                style = TextStyle(align = 1)
            )
        )

        val textBlock = TextBlock(
            blockId = "text1",
            blockType = BlockType.TEXT,
            children = emptyList(),
            parentId = "page1",
            text = TextBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Hello World"))),
                style = TextStyle(align = 1)
            )
        )

        val documentContent = DocumentRawContent(
            document = Document(documentId = documentId, revisionId = 1, title = "Test Document"),
            blocks = mapOf("page1" to pageBlock, "text1" to textBlock)
        )

        // Setup mocks
        coEvery { mockApiClient.getDocumentInfo(documentId) } returns documentInfo
        coEvery { mockApiClient.getDocumentRawContent(documentId) } returns documentContent
        coEvery { mockApiClient.getOrderedBlocks(documentContent) } returns listOf(textBlock)
        every { mockApiClient.close() } just Runs

        // Execute
        val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
        runBlocking {
            converter.export(documentId)
        }
        converter.close()

        // Verify
        coVerify { mockApiClient.getDocumentInfo(documentId) }
        coVerify { mockApiClient.getDocumentRawContent(documentId) }
        verify { mockFileSystem.writeText(match { it.endsWith("Test Document.html") }, any()) }
        verify { mockApiClient.close() }
    }

    test("export should download images when document contains ImageBlock") {
        val documentId = "doc_with_image"
        val imageToken = "img_token_123"

        val documentInfo = DocumentInfo(
            documentId = documentId,
            revisionId = 1,
            title = "Doc with Image"
        )

        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = listOf("img1"),
            parentId = null,
            page = PageBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                style = TextStyle(align = 1)
            )
        )

        val imageBlock = ImageBlock(
            blockId = "img1",
            blockType = BlockType.IMAGE,
            children = emptyList(),
            parentId = "page1",
            image = ImageBlockData(
                token = imageToken,
                width = 800,
                height = 600
            )
        )

        val documentContent = DocumentRawContent(
            document = Document(documentId = documentId, revisionId = 1, title = "Doc with Image"),
            blocks = mapOf("page1" to pageBlock, "img1" to imageBlock)
        )

        coEvery { mockApiClient.getDocumentInfo(documentId) } returns documentInfo
        coEvery { mockApiClient.getDocumentRawContent(documentId) } returns documentContent
        coEvery { mockApiClient.getOrderedBlocks(documentContent) } returns listOf(imageBlock)
        coEvery { mockApiClient.downloadFile(imageToken, any()) } just Runs
        every { mockApiClient.close() } just Runs

        val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
        runBlocking {
            converter.export(documentId)
        }
        converter.close()

        // Verify image was downloaded
        coVerify { mockApiClient.downloadFile(imageToken, match { it.contains(imageToken) && it.endsWith(".png") }) }
    }

    test("export should download files when document contains FileBlock") {
        val documentId = "doc_with_file"
        val fileToken = "file_token_456"
        val fileName = "document.pdf"

        val documentInfo = DocumentInfo(
            documentId = documentId,
            revisionId = 1,
            title = "Doc with File"
        )

        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = listOf("file1"),
            parentId = null,
            page = PageBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                style = TextStyle(align = 1)
            )
        )

        val fileBlock = FileBlock(
            blockId = "file1",
            blockType = BlockType.FILE,
            children = emptyList(),
            parentId = "page1",
            file = FileBlockData(
                token = fileToken,
                name = fileName
            )
        )

        val documentContent = DocumentRawContent(
            document = Document(documentId = documentId, revisionId = 1, title = "Doc with File"),
            blocks = mapOf("page1" to pageBlock, "file1" to fileBlock)
        )

        coEvery { mockApiClient.getDocumentInfo(documentId) } returns documentInfo
        coEvery { mockApiClient.getDocumentRawContent(documentId) } returns documentContent
        coEvery { mockApiClient.getOrderedBlocks(documentContent) } returns listOf(fileBlock)
        coEvery { mockApiClient.downloadFile(fileToken, any()) } just Runs
        every { mockApiClient.close() } just Runs

        val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
        runBlocking {
            converter.export(documentId)
        }
        converter.close()

        coVerify { mockApiClient.downloadFile(fileToken, match { it.endsWith(fileName) }) }
    }

    test("export should handle API errors gracefully") {
        val documentId = "invalid_doc"

        coEvery { mockApiClient.getDocumentInfo(documentId) } throws Exception("Document not found")
        every { mockApiClient.close() } just Runs

        val converter = Feishu2Html(options, mockApiClient, mockFileSystem)

        shouldThrow<Exception> {
            runBlocking {
                converter.export(documentId)
            }
        }

        converter.close()

        coVerify { mockApiClient.getDocumentInfo(documentId) }
        coVerify(exactly = 0) { mockApiClient.getDocumentRawContent(any()) }
    }

    test("export should use custom output filename when provided") {
        val documentId = "test_doc"
        val customFilename = "custom-output.html"

        val documentInfo = DocumentInfo(
            documentId = documentId,
            revisionId = 1,
            title = "Original Title"
        )

        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = emptyList(),
            parentId = null,
            page = PageBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                style = TextStyle(align = 1)
            )
        )

        val documentContent = DocumentRawContent(
            document = Document(documentId = documentId, revisionId = 1, title = "Original Title"),
            blocks = mapOf("page1" to pageBlock)
        )

        coEvery { mockApiClient.getDocumentInfo(documentId) } returns documentInfo
        coEvery { mockApiClient.getDocumentRawContent(documentId) } returns documentContent
        coEvery { mockApiClient.getOrderedBlocks(documentContent) } returns emptyList()
        every { mockApiClient.close() } just Runs

        val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
        runBlocking {
            converter.export(documentId, customFilename)
        }
        converter.close()

        verify { mockFileSystem.writeText(match { it.endsWith(customFilename) }, any()) }
    }

    test("export should write CSS file when externalCss is true") {
        val documentId = "test_doc"

        val documentInfo = DocumentInfo(
            documentId = documentId,
            revisionId = 1,
            title = "Test"
        )

        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = emptyList(),
            parentId = null,
            page = PageBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                style = TextStyle(align = 1)
            )
        )

        val documentContent = DocumentRawContent(
            document = Document(documentId = documentId, revisionId = 1, title = "Test"),
            blocks = mapOf("page1" to pageBlock)
        )

        coEvery { mockApiClient.getDocumentInfo(documentId) } returns documentInfo
        coEvery { mockApiClient.getDocumentRawContent(documentId) } returns documentContent
        coEvery { mockApiClient.getOrderedBlocks(documentContent) } returns emptyList()
        every { mockApiClient.close() } just Runs

        val optionsWithExternalCss = options.copy(externalCss = true, cssFileName = "my-style.css")
        val converter = Feishu2Html(optionsWithExternalCss, mockApiClient, mockFileSystem)
        runBlocking {
            converter.export(documentId)
        }
        converter.close()

        verify { mockFileSystem.writeText(match { it.endsWith("my-style.css") }, any()) }
    }

    test("export should NOT write CSS file when externalCss is false") {
        val documentId = "test_doc"

        val documentInfo = DocumentInfo(
            documentId = documentId,
            revisionId = 1,
            title = "Test"
        )

        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = emptyList(),
            parentId = null,
            page = PageBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                style = TextStyle(align = 1)
            )
        )

        val documentContent = DocumentRawContent(
            document = Document(documentId = documentId, revisionId = 1, title = "Test"),
            blocks = mapOf("page1" to pageBlock)
        )

        coEvery { mockApiClient.getDocumentInfo(documentId) } returns documentInfo
        coEvery { mockApiClient.getDocumentRawContent(documentId) } returns documentContent
        coEvery { mockApiClient.getOrderedBlocks(documentContent) } returns emptyList()
        every { mockApiClient.close() } just Runs

        val optionsWithInlineCss = options.copy(externalCss = false)
        val converter = Feishu2Html(optionsWithInlineCss, mockApiClient, mockFileSystem)
        runBlocking {
            converter.export(documentId)
        }
        converter.close()

        verify(exactly = 0) { mockFileSystem.writeText(match { it.endsWith(".css") }, any()) }
    }

    test("exportBatch should export multiple documents") {
        val docIds = listOf("doc1", "doc2", "doc3")

        docIds.forEach { docId ->
            val documentInfo = DocumentInfo(
                documentId = docId,
                revisionId = 1,
                title = "Document $docId"
            )

            val pageBlock = PageBlock(
                blockId = "page_$docId",
                blockType = BlockType.PAGE,
                children = emptyList(),
                parentId = null,
                page = PageBlockData(
                    elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                    style = TextStyle(align = 1)
                )
            )

            val documentContent = DocumentRawContent(
                document = Document(documentId = docId, revisionId = 1, title = "Document $docId"),
                blocks = mapOf("page_$docId" to pageBlock)
            )

            coEvery { mockApiClient.getDocumentInfo(docId) } returns documentInfo
            coEvery { mockApiClient.getDocumentRawContent(docId) } returns documentContent
            coEvery { mockApiClient.getOrderedBlocks(documentContent) } returns emptyList()
        }

        every { mockApiClient.close() } just Runs

        val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
        runBlocking {
            converter.exportBatch(docIds)
        }
        converter.close()

        // Verify all documents were processed
        docIds.forEach { docId ->
            coVerify { mockApiClient.getDocumentInfo(docId) }
            coVerify { mockApiClient.getDocumentRawContent(docId) }
        }

        // 3 HTML files + 1 CSS file (externalCss = true by default)
        verify(atLeast = 3) { mockFileSystem.writeText(any(), any()) }
    }

    test("exportBatch should continue on error and process all documents") {
        val docIds = listOf("doc1", "doc2_fail", "doc3")

        // doc1 - success
        val doc1Info = DocumentInfo(documentId = "doc1", revisionId = 1, title = "Doc 1")
        val page1 = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = emptyList(),
            parentId = null,
            page = PageBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                style = TextStyle(align = 1)
            )
        )
        val content1 = DocumentRawContent(
            document = Document(documentId = "doc1", revisionId = 1, title = "Doc 1"),
            blocks = mapOf("page1" to page1)
        )

        coEvery { mockApiClient.getDocumentInfo("doc1") } returns doc1Info
        coEvery { mockApiClient.getDocumentRawContent("doc1") } returns content1
        coEvery { mockApiClient.getOrderedBlocks(content1) } returns emptyList()

        // doc2 - failure
        coEvery { mockApiClient.getDocumentInfo("doc2_fail") } throws Exception("API Error")

        // doc3 - success
        val doc3Info = DocumentInfo(documentId = "doc3", revisionId = 1, title = "Doc 3")
        val page3 = PageBlock(
            blockId = "page3",
            blockType = BlockType.PAGE,
            children = emptyList(),
            parentId = null,
            page = PageBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                style = TextStyle(align = 1)
            )
        )
        val content3 = DocumentRawContent(
            document = Document(documentId = "doc3", revisionId = 1, title = "Doc 3"),
            blocks = mapOf("page3" to page3)
        )

        coEvery { mockApiClient.getDocumentInfo("doc3") } returns doc3Info
        coEvery { mockApiClient.getDocumentRawContent("doc3") } returns content3
        coEvery { mockApiClient.getOrderedBlocks(content3) } returns emptyList()

        every { mockApiClient.close() } just Runs

        val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
        runBlocking {
            converter.exportBatch(docIds)  // Should not throw
        }
        converter.close()

        // Verify all were attempted
        coVerify { mockApiClient.getDocumentInfo("doc1") }
        coVerify { mockApiClient.getDocumentInfo("doc2_fail") }
        coVerify { mockApiClient.getDocumentInfo("doc3") }

        // Only doc1 and doc3 should have HTML written
        verify(exactly = 2) { mockFileSystem.writeText(match { it.endsWith(".html") }, any()) }
    }

    test("export should skip downloading existing images") {
        val documentId = "doc_existing_image"
        val imageToken = "img_exists"

        val documentInfo = DocumentInfo(
            documentId = documentId,
            revisionId = 1,
            title = "Test"
        )

        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = listOf("img1"),
            parentId = null,
            page = PageBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                style = TextStyle(align = 1)
            )
        )

        val imageBlock = ImageBlock(
            blockId = "img1",
            blockType = BlockType.IMAGE,
            children = emptyList(),
            parentId = "page1",
            image = ImageBlockData(token = imageToken, width = 100, height = 100)
        )

        val documentContent = DocumentRawContent(
            document = Document(documentId = documentId, revisionId = 1, title = "Test"),
            blocks = mapOf("page1" to pageBlock, "img1" to imageBlock)
        )

        coEvery { mockApiClient.getDocumentInfo(documentId) } returns documentInfo
        coEvery { mockApiClient.getDocumentRawContent(documentId) } returns documentContent
        coEvery { mockApiClient.getOrderedBlocks(documentContent) } returns listOf(imageBlock)
        every { mockFileSystem.exists(any()) } returns true  // Image already exists
        every { mockApiClient.close() } just Runs

        val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
        runBlocking {
            converter.export(documentId)
        }
        converter.close()

        // Should NOT download since file exists
        coVerify(exactly = 0) { mockApiClient.downloadFile(any(), any()) }
    }

    test("export should handle BoardBlock and export as image") {
        val documentId = "doc_with_board"
        val boardToken = "board_token_789"

        val documentInfo = DocumentInfo(
            documentId = documentId,
            revisionId = 1,
            title = "Doc with Board"
        )

        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = listOf("board1"),
            parentId = null,
            page = PageBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                style = TextStyle(align = 1)
            )
        )

        val boardBlock = BoardBlock(
            blockId = "board1",
            blockType = BlockType.BOARD,
            children = emptyList(),
            parentId = "page1",
            board = BoardBlockData(token = boardToken)
        )

        val documentContent = DocumentRawContent(
            document = Document(documentId = documentId, revisionId = 1, title = "Doc with Board"),
            blocks = mapOf("page1" to pageBlock, "board1" to boardBlock)
        )

        coEvery { mockApiClient.getDocumentInfo(documentId) } returns documentInfo
        coEvery { mockApiClient.getDocumentRawContent(documentId) } returns documentContent
        coEvery { mockApiClient.getOrderedBlocks(documentContent) } returns listOf(boardBlock)
        coEvery { mockApiClient.exportBoard(boardToken, any()) } just Runs
        every { mockApiClient.close() } just Runs

        val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
        runBlocking {
            converter.export(documentId)
        }
        converter.close()

        coVerify { mockApiClient.exportBoard(boardToken, match { it.contains(boardToken) && it.endsWith(".png") }) }
    }

    test("export with inlineImages option should work") {
        val documentId = "doc_inline_images"

        val documentInfo = DocumentInfo(
            documentId = documentId,
            revisionId = 1,
            title = "Inline Images"
        )

        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = emptyList(),
            parentId = null,
            page = PageBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                style = TextStyle(align = 1)
            )
        )

        val documentContent = DocumentRawContent(
            document = Document(documentId = documentId, revisionId = 1, title = "Inline Images"),
            blocks = mapOf("page1" to pageBlock)
        )

        coEvery { mockApiClient.getDocumentInfo(documentId) } returns documentInfo
        coEvery { mockApiClient.getDocumentRawContent(documentId) } returns documentContent
        coEvery { mockApiClient.getOrderedBlocks(documentContent) } returns emptyList()
        every { mockApiClient.close() } just Runs

        val optionsWithInlineImages = options.copy(inlineImages = true, externalCss = false)
        val converter = Feishu2Html(optionsWithInlineImages, mockApiClient, mockFileSystem)
        runBlocking {
            converter.export(documentId)
        }
        converter.close()

        // Verify basic flow worked
        coVerify { mockApiClient.getDocumentInfo(documentId) }
        verify { mockFileSystem.writeText(any(), any()) }
    }

    test("exportBatch with empty list should complete without error") {
        every { mockApiClient.close() } just Runs

        val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
        runBlocking {
            converter.exportBatch(emptyList())
        }
        converter.close()

        coVerify(exactly = 0) { mockApiClient.getDocumentInfo(any()) }
    }

    test("export should use different template modes") {
        val documentId = "test_doc"

        val documentInfo = DocumentInfo(
            documentId = documentId,
            revisionId = 1,
            title = "Template Test"
        )

        val pageBlock = PageBlock(
            blockId = "page1",
            blockType = BlockType.PAGE,
            children = emptyList(),
            parentId = null,
            page = PageBlockData(
                elements = listOf(TextElement(textRun = TextRun(content = "Page"))),
                style = TextStyle(align = 1)
            )
        )

        val documentContent = DocumentRawContent(
            document = Document(documentId = documentId, revisionId = 1, title = "Template Test"),
            blocks = mapOf("page1" to pageBlock)
        )

        coEvery { mockApiClient.getDocumentInfo(documentId) } returns documentInfo
        coEvery { mockApiClient.getDocumentRawContent(documentId) } returns documentContent
        coEvery { mockApiClient.getOrderedBlocks(documentContent) } returns emptyList()
        every { mockApiClient.close() } just Runs

        // Test each template mode (disable external CSS to avoid extra writes)
        listOf(TemplateMode.DEFAULT, TemplateMode.FRAGMENT, TemplateMode.FULL).forEach { mode ->
            val optionsWithMode = options.copy(templateMode = mode, externalCss = false)
            val converter = Feishu2Html(optionsWithMode, mockApiClient, mockFileSystem)
            runBlocking {
                converter.export(documentId, "test-$mode.html")
            }
            converter.close()
        }

        verify(exactly = 3) { mockFileSystem.writeText(match { it.endsWith(".html") }, any()) }
    }

    test("close should close API client") {
        every { mockApiClient.close() } just Runs

        val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
        converter.close()

        verify { mockApiClient.close() }
    }

    test("using use block should auto-close resources") {
        every { mockApiClient.close() } just Runs

        Feishu2Html(options, mockApiClient, mockFileSystem).use {
            // Do nothing
        }

        verify { mockApiClient.close() }
    }
})


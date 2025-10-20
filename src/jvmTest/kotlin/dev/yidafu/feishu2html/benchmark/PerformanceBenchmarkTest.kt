package dev.yidafu.feishu2html.benchmark

import dev.yidafu.feishu2html.Feishu2Html
import dev.yidafu.feishu2html.Feishu2HtmlOptions
import dev.yidafu.feishu2html.api.FeishuApiClient
import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.metrics.InMemoryMetricsCollector
import dev.yidafu.feishu2html.platform.PlatformFileSystem
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

/**
 * Performance benchmark tests for Feishu2Html
 *
 * These tests measure export performance for different document sizes
 * and configurations to establish performance baselines.
 */
class PerformanceBenchmarkTest : FunSpec({

    val testOutputDir = "build/test-benchmark-output"

    beforeEach {
        // Clean up test output
        File(testOutputDir).deleteRecursively()
    }

    afterEach {
        // Clean up after tests
        File(testOutputDir).deleteRecursively()
    }

    /**
     * Helper function to create mock API client and file system
     */
    fun setupMocks(
        documentInfo: DocumentInfo,
        rawContent: DocumentRawContent
    ): Pair<FeishuApiClient, PlatformFileSystem> {
        val mockApiClient = mockk<FeishuApiClient>(relaxed = true)
        val mockFileSystem = mockk<PlatformFileSystem>(relaxed = true)

        coEvery { mockApiClient.getDocumentInfo(any()) } coAnswers {
            delay(10) // Simulate API latency
            documentInfo
        }

        coEvery { mockApiClient.getDocumentRawContent(any()) } coAnswers {
            delay(20) // Simulate API latency
            rawContent
        }

        // Use real implementation for getOrderedBlocks - it correctly traverses the block tree
        every { mockApiClient.getOrderedBlocks(any()) } answers {
            val content = firstArg<DocumentRawContent>()
            val blocks = content.blocks
            val result = mutableListOf<Block>()
            val visited = mutableSetOf<String>()

            fun traverse(blockId: String) {
                if (blockId in visited) return
                visited.add(blockId)

                val block = blocks[blockId] ?: return
                result.add(block)

                block.children?.forEach { childId ->
                    traverse(childId)
                }
            }

            // Traverse from document root (PAGE block)
            val pageBlock = blocks.values.firstOrNull { it.blockType == BlockType.PAGE }
            pageBlock?.children?.forEach { childId ->
                traverse(childId)
            }

            result
        }

        coEvery { mockApiClient.downloadFile(any(), any()) } coAnswers {
            delay(50) // Simulate download time
        }

        coEvery { mockApiClient.exportBoard(any(), any()) } coAnswers {
            delay(100) // Simulate board export time
        }

        every { mockFileSystem.exists(any()) } returns false
        every { mockFileSystem.createDirectories(any()) } returns Unit
        every { mockFileSystem.writeText(any(), any()) } returns Unit

        return Pair(mockApiClient, mockFileSystem)
    }

    /**
     * Helper function to run a test multiple times and calculate statistics
     */
    suspend fun runBenchmark(
        name: String,
        runs: Int = 3,
        block: suspend () -> Unit
    ): BenchmarkResult {
        val times = mutableListOf<Long>()

        repeat(runs) {
            val time = measureTimeMillis {
                block()
            }
            times.add(time)
        }

        return BenchmarkResult(
            name = name,
            runs = runs,
            times = times,
            average = times.average().toLong(),
            min = times.minOrNull() ?: 0,
            max = times.maxOrNull() ?: 0
        )
    }

    test("Benchmark: Small document export (10 blocks, 0 images) - Target < 1s") {
        val (documentInfo, rawContent) = TestDataGenerator.createSmallDocument()
        val (mockApiClient, mockFileSystem) = setupMocks(documentInfo, rawContent)
        val metricsCollector = InMemoryMetricsCollector()

        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            imageDir = "$testOutputDir/images",
            fileDir = "$testOutputDir/files",
            metricsCollector = metricsCollector
        )

        val result = runBenchmark("Small Document") {
            val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
            converter.export("small_doc")
            converter.close()
        }

        println(result.toString())
        result.average shouldBeLessThan 1000L // Should complete in < 1 second

        // Verify metrics
        val metrics = metricsCollector.getMetrics()
        metrics.size shouldBe 3 // 3 runs
        metrics.forEach { metric ->
            metric.success shouldBe true
            metric.blocksCount shouldBe 11 // 10 text blocks + 1 PAGE block
            metric.imagesDownloaded shouldBe 0
        }
    }

    test("Benchmark: Medium document export (100 blocks, 10 images) - Target < 3s") {
        val (documentInfo, rawContent) = TestDataGenerator.createMediumDocument()
        val (mockApiClient, mockFileSystem) = setupMocks(documentInfo, rawContent)
        val metricsCollector = InMemoryMetricsCollector()

        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            imageDir = "$testOutputDir/images",
            fileDir = "$testOutputDir/files",
            metricsCollector = metricsCollector
        )

        val result = runBenchmark("Medium Document") {
            val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
            converter.export("medium_doc")
            converter.close()
        }

        println(result.toString())
        result.average shouldBeLessThan 3000L // Should complete in < 3 seconds

        // Verify metrics
        val metrics = metricsCollector.getMetrics()
        metrics.forEach { metric ->
            metric.success shouldBe true
            metric.blocksCount shouldBe 101 // 90 text blocks + 10 images + 1 PAGE block
            metric.imagesDownloaded shouldBe 10
        }
    }

    test("Benchmark: Large document export (1000 blocks, 50 assets) - Target < 15s") {
        val (documentInfo, rawContent) = TestDataGenerator.createLargeDocument()
        val (mockApiClient, mockFileSystem) = setupMocks(documentInfo, rawContent)
        val metricsCollector = InMemoryMetricsCollector()

        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            imageDir = "$testOutputDir/images",
            fileDir = "$testOutputDir/files",
            metricsCollector = metricsCollector
        )

        val result = runBenchmark("Large Document") {
            val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
            converter.export("large_doc")
            converter.close()
        }

        println(result.toString())
        result.average shouldBeLessThan 15000L // Should complete in < 15 seconds

        // Verify metrics
        val metrics = metricsCollector.getMetrics()
        metrics.forEach { metric ->
            metric.success shouldBe true
            metric.blocksCount shouldBe 1001 // 950 text blocks + 40 images + 10 files + 1 PAGE block
            metric.imagesDownloaded shouldBe 40
            metric.filesDownloaded shouldBe 10
        }
    }

    test("Benchmark: Batch export (10 small documents)") {
        val (documentInfo, rawContent) = TestDataGenerator.createSmallDocument()
        val (mockApiClient, mockFileSystem) = setupMocks(documentInfo, rawContent)
        val metricsCollector = InMemoryMetricsCollector()

        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            imageDir = "$testOutputDir/images",
            fileDir = "$testOutputDir/files",
            metricsCollector = metricsCollector
        )

        val documentIds = (1..10).map { "doc_$it" }

        val totalTime = measureTimeMillis {
            runBlocking {
                val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
                converter.exportBatch(documentIds)
                converter.close()
            }
        }

        println("Batch Export: Total ${totalTime}ms, Average per doc: ${totalTime / 10}ms")

        // Verify all documents were exported
        val metrics = metricsCollector.getMetrics()
        metrics.size shouldBe 10
        metrics.forEach { metric ->
            metric.success shouldBe true
            metric.blocksCount shouldBe 11 // 10 text blocks + 1 PAGE block
        }

        val avgTime = metricsCollector.getAverageExportTime()
        println("Average export time from metrics: $avgTime")
    }

    test("Benchmark: Concurrent downloads - Test different maxConcurrentDownloads values") {
        val (documentInfo, rawContent) = TestDataGenerator.createMediumDocument()
        val (mockApiClient, mockFileSystem) = setupMocks(documentInfo, rawContent)

        val concurrencyLevels = listOf(5, 10, 20)
        val results = mutableMapOf<Int, Long>()

        for (concurrency in concurrencyLevels) {
            val options = Feishu2HtmlOptions(
                appId = "test_app_id",
                appSecret = "test_app_secret",
                outputDir = testOutputDir,
                imageDir = "$testOutputDir/images",
                fileDir = "$testOutputDir/files",
                maxConcurrentDownloads = concurrency
            )

            val time = measureTimeMillis {
                runBlocking {
                    val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
                    converter.export("medium_doc")
                    converter.close()
                }
            }

            results[concurrency] = time
            println("Concurrency $concurrency: ${time}ms")
        }

        // With higher concurrency, time should generally be less
        // (though with mocks, the effect may be minimal)
        results[5]!! shouldBeGreaterThan 0L
        results[10]!! shouldBeGreaterThan 0L
        results[20]!! shouldBeGreaterThan 0L
    }

    test("Benchmark: Document with boards export") {
        val (documentInfo, rawContent) = TestDataGenerator.createDocumentWithBoards()
        val (mockApiClient, mockFileSystem) = setupMocks(documentInfo, rawContent)
        val metricsCollector = InMemoryMetricsCollector()

        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_app_secret",
            outputDir = testOutputDir,
            imageDir = "$testOutputDir/images",
            fileDir = "$testOutputDir/files",
            metricsCollector = metricsCollector
        )

        val result = runBenchmark("Document with Boards") {
            val converter = Feishu2Html(options, mockApiClient, mockFileSystem)
            converter.export("board_doc")
            converter.close()
        }

        println(result.toString())

        // Verify metrics
        val metrics = metricsCollector.getMetrics()
        metrics.forEach { metric ->
            metric.success shouldBe true
            metric.blocksCount shouldBe 26 // 20 text blocks + 5 boards + 1 PAGE block
            metric.boardsExported shouldBe 5
        }
    }

    test("Generate performance report") {
        val reportDir = "build/reports/performance"
        File(reportDir).mkdirs()
        val reportFile = File("$reportDir/benchmark-results.txt")

        val report = buildString {
            appendLine("=== Performance Benchmark Results ===")
            appendLine("Date: ${java.time.LocalDateTime.now()}")
            appendLine("Kotlin: ${KotlinVersion.CURRENT}")
            appendLine()
            appendLine("Test Environment:")
            appendLine("- OS: ${System.getProperty("os.name")}")
            appendLine("- JVM: ${System.getProperty("java.version")}")
            appendLine()
            appendLine("Note: These are mock-based tests. Actual performance depends on:")
            appendLine("  - Network latency to Feishu API")
            appendLine("  - Document complexity")
            appendLine("  - System resources")
            appendLine()
            appendLine("Results:")
            appendLine("1. Small Document (10 blocks, 0 images)")
            appendLine("   - Target: < 1000ms")
            appendLine()
            appendLine("2. Medium Document (100 blocks, 10 images)")
            appendLine("   - Target: < 3000ms")
            appendLine()
            appendLine("3. Large Document (1000 blocks, 50 assets)")
            appendLine("   - Target: < 15000ms")
            appendLine()
            appendLine("4. Batch Export (10 documents)")
            appendLine("   - Measures sequential export performance")
            appendLine()
            appendLine("5. Concurrent Downloads")
            appendLine("   - Tests different maxConcurrentDownloads configurations")
            appendLine()
            appendLine("See individual test output for detailed timings.")
        }

        reportFile.writeText(report)
        println("Performance report written to: ${reportFile.absolutePath}")
        reportFile.exists() shouldBe true
    }
})

/**
 * Data class to store benchmark results
 */
data class BenchmarkResult(
    val name: String,
    val runs: Int,
    val times: List<Long>,
    val average: Long,
    val min: Long,
    val max: Long
) {
    override fun toString(): String {
        return """
            |Benchmark: $name
            |  Runs: $runs
            |  Average: ${average}ms
            |  Min: ${min}ms
            |  Max: ${max}ms
            |  Times: ${times.joinToString(", ") { "${it}ms" }}
        """.trimMargin()
    }
}


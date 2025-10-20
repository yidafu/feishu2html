package dev.yidafu.feishu2html.metrics

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Integration tests for MetricsCollector that run on all platforms
 * 
 * These tests verify metrics collection in realistic scenarios
 */
class MetricsCollectorIntegrationTest : FunSpec({

    test("Integration: MetricsCollector should track multiple export operations") {
        val collector = InMemoryMetricsCollector()
        val baseTime = Clock.System.now()

        // Simulate 5 successful exports
        repeat(5) { i ->
            val startTime = baseTime + (i * 10).seconds
            val endTime = startTime + (2 + i).seconds

            collector.recordExport(
                ExportMetrics(
                    documentId = "doc_$i",
                    startTime = startTime,
                    endTime = endTime,
                    duration = (2 + i).seconds,
                    blocksCount = 50 + (i * 10),
                    imagesDownloaded = 5 + i,
                    filesDownloaded = 2,
                    boardsExported = 1,
                    success = true
                )
            )
        }

        val metrics = collector.getMetrics()
        metrics.size shouldBe 5

        // Verify metrics are tracked correctly
        metrics.forEachIndexed { i, metric ->
            metric.documentId shouldBe "doc_$i"
            metric.blocksCount shouldBe (50 + i * 10)
            metric.imagesDownloaded shouldBe (5 + i)
            metric.duration shouldBe (2 + i).seconds
        }

        // Average: (2 + 3 + 4 + 5 + 6) / 5 = 4 seconds
        collector.getAverageExportTime() shouldBe 4.seconds
        collector.getSuccessRate() shouldBeExactly 1.0
    }

    test("Integration: MetricsCollector should handle mixed success/failure scenarios") {
        val collector = InMemoryMetricsCollector()
        val baseTime = Clock.System.now()

        // 7 successful exports
        repeat(7) { i ->
            collector.recordExport(
                ExportMetrics(
                    documentId = "success_$i",
                    startTime = baseTime,
                    endTime = baseTime + 1.seconds,
                    duration = 1.seconds,
                    blocksCount = 100,
                    imagesDownloaded = 10,
                    filesDownloaded = 5,
                    boardsExported = 0,
                    success = true
                )
            )
        }

        // 3 failed exports
        repeat(3) { i ->
            collector.recordExport(
                ExportMetrics(
                    documentId = "failed_$i",
                    startTime = baseTime,
                    endTime = baseTime + 500.milliseconds,
                    duration = 500.milliseconds,
                    blocksCount = 50,
                    imagesDownloaded = 0,
                    filesDownloaded = 0,
                    boardsExported = 0,
                    success = false,
                    errorMessage = "Test error"
                )
            )
        }

        collector.getMetrics().size shouldBe 10

        // Success rate: 7/10 = 0.7
        collector.getSuccessRate() shouldBeExactly 0.7

        // Average time: (7000ms + 1500ms) / 10 = 850ms
        val avgTime = collector.getAverageExportTime()
        avgTime.inWholeMilliseconds shouldBe 850
    }

    test("Integration: MetricsCollector should calculate averageTimePerBlock correctly") {
        val collector = InMemoryMetricsCollector()
        val baseTime = Clock.System.now()

        collector.recordExport(
            ExportMetrics(
                documentId = "doc_1",
                startTime = baseTime,
                endTime = baseTime + 10.seconds,
                duration = 10.seconds,
                blocksCount = 100,
                imagesDownloaded = 5,
                filesDownloaded = 2,
                boardsExported = 1,
                success = true
            )
        )

        val metrics = collector.getMetrics()[0]
        // 10 seconds / 100 blocks = 100ms per block
        metrics.averageTimePerBlock shouldBe 100.milliseconds
    }

    test("Integration: MetricsCollector should track asset download statistics") {
        val collector = InMemoryMetricsCollector()
        val baseTime = Clock.System.now()

        // Export with many images
        collector.recordExport(
            ExportMetrics(
                documentId = "image_heavy_doc",
                startTime = baseTime,
                endTime = baseTime + 5.seconds,
                duration = 5.seconds,
                blocksCount = 50,
                imagesDownloaded = 40,
                filesDownloaded = 0,
                boardsExported = 0,
                success = true
            )
        )

        // Export with many files
        collector.recordExport(
            ExportMetrics(
                documentId = "file_heavy_doc",
                startTime = baseTime,
                endTime = baseTime + 8.seconds,
                duration = 8.seconds,
                blocksCount = 30,
                imagesDownloaded = 5,
                filesDownloaded = 20,
                boardsExported = 0,
                success = true
            )
        )

        // Export with boards
        collector.recordExport(
            ExportMetrics(
                documentId = "board_doc",
                startTime = baseTime,
                endTime = baseTime + 6.seconds,
                duration = 6.seconds,
                blocksCount = 25,
                imagesDownloaded = 0,
                filesDownloaded = 0,
                boardsExported = 10,
                success = true
            )
        )

        val metrics = collector.getMetrics()
        metrics[0].imagesDownloaded shouldBe 40
        metrics[1].filesDownloaded shouldBe 20
        metrics[2].boardsExported shouldBe 10

        // Total assets downloaded
        val totalImages = metrics.sumOf { it.imagesDownloaded }
        val totalFiles = metrics.sumOf { it.filesDownloaded }
        val totalBoards = metrics.sumOf { it.boardsExported }

        totalImages shouldBe 45
        totalFiles shouldBe 20
        totalBoards shouldBe 10
    }

    test("Integration: MetricsCollector clear should reset all data") {
        val collector = InMemoryMetricsCollector()
        val baseTime = Clock.System.now()

        // Add some metrics
        repeat(5) { i ->
            collector.recordExport(
                ExportMetrics(
                    documentId = "doc_$i",
                    startTime = baseTime,
                    endTime = baseTime + 1.seconds,
                    duration = 1.seconds,
                    blocksCount = 100,
                    imagesDownloaded = 10,
                    filesDownloaded = 5,
                    boardsExported = 0,
                    success = true
                )
            )
        }

        collector.getMetrics().size shouldBe 5

        // Clear all metrics
        collector.clear()

        collector.getMetrics().size shouldBe 0
        collector.getAverageExportTime() shouldBe 0.milliseconds
        collector.getSuccessRate() shouldBeExactly 0.0

        // Can add new metrics after clearing
        collector.recordExport(
            ExportMetrics(
                documentId = "new_doc",
                startTime = baseTime,
                endTime = baseTime + 2.seconds,
                duration = 2.seconds,
                blocksCount = 50,
                imagesDownloaded = 5,
                filesDownloaded = 0,
                boardsExported = 0,
                success = true
            )
        )

        collector.getMetrics().size shouldBe 1
    }

    test("Integration: MetricsCollector should handle large number of exports") {
        val collector = InMemoryMetricsCollector()
        val baseTime = Clock.System.now()

        // Simulate 100 exports
        repeat(100) { i ->
            val duration = (100 + i).milliseconds
            collector.recordExport(
                ExportMetrics(
                    documentId = "doc_$i",
                    startTime = baseTime + (i * 1).seconds,
                    endTime = baseTime + (i * 1).seconds + duration,
                    duration = duration,
                    blocksCount = 10 + i,
                    imagesDownloaded = i % 10,
                    filesDownloaded = i % 5,
                    boardsExported = i % 3,
                    success = i % 10 != 0 // 10% failure rate
                )
            )
        }

        val metrics = collector.getMetrics()
        metrics.size shouldBe 100

        // Success rate should be 90% (10 failures out of 100)
        val successRate = collector.getSuccessRate()
        successRate shouldBe 0.9

        // Average time should be around 150ms (100ms to 200ms average)
        val avgTime = collector.getAverageExportTime()
        avgTime.inWholeMilliseconds shouldBeGreaterThan 140
    }

    test("Integration: MetricsCollector should preserve metrics order") {
        val collector = InMemoryMetricsCollector()
        val baseTime = Clock.System.now()

        val documentIds = listOf("first", "second", "third", "fourth", "fifth")

        documentIds.forEachIndexed { index, docId ->
            collector.recordExport(
                ExportMetrics(
                    documentId = docId,
                    startTime = baseTime + (index * 1).seconds,
                    endTime = baseTime + (index * 1 + 1).seconds,
                    duration = 1.seconds,
                    blocksCount = 10,
                    imagesDownloaded = 0,
                    filesDownloaded = 0,
                    boardsExported = 0,
                    success = true
                )
            )
        }

        val metrics = collector.getMetrics()
        metrics.forEachIndexed { index, metric ->
            metric.documentId shouldBe documentIds[index]
        }
    }

    test("Integration: MetricsCollector should handle zero-duration exports") {
        val collector = InMemoryMetricsCollector()
        val now = Clock.System.now()

        collector.recordExport(
            ExportMetrics(
                documentId = "instant_doc",
                startTime = now,
                endTime = now,
                duration = 0.milliseconds,
                blocksCount = 0,
                imagesDownloaded = 0,
                filesDownloaded = 0,
                boardsExported = 0,
                success = true
            )
        )

        val metrics = collector.getMetrics()
        metrics[0].duration shouldBe 0.milliseconds
        metrics[0].averageTimePerBlock shouldBe 0.milliseconds
    }
})


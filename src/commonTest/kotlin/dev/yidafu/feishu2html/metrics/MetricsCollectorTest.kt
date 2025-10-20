package dev.yidafu.feishu2html.metrics

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MetricsCollectorTest : FunSpec({

    test("InMemoryMetricsCollector should record single export") {
        val collector = InMemoryMetricsCollector()
        val now = Clock.System.now()

        val metrics = ExportMetrics(
            documentId = "doc1",
            startTime = now,
            endTime = now + 1.seconds,
            duration = 1.seconds,
            blocksCount = 10,
            imagesDownloaded = 2,
            filesDownloaded = 1,
            boardsExported = 0,
            totalBytes = 1024,
            success = true
        )

        collector.recordExport(metrics)

        val recorded = collector.getMetrics()
        recorded.size shouldBe 1
        recorded[0] shouldBe metrics
    }

    test("InMemoryMetricsCollector should record multiple exports") {
        val collector = InMemoryMetricsCollector()
        val now = Clock.System.now()

        val metrics1 = ExportMetrics(
            documentId = "doc1",
            startTime = now,
            endTime = now + 1.seconds,
            duration = 1.seconds,
            blocksCount = 10,
            imagesDownloaded = 2,
            filesDownloaded = 1,
            boardsExported = 0,
            success = true
        )

        val metrics2 = ExportMetrics(
            documentId = "doc2",
            startTime = now + 2.seconds,
            endTime = now + 4.seconds,
            duration = 2.seconds,
            blocksCount = 20,
            imagesDownloaded = 5,
            filesDownloaded = 0,
            boardsExported = 1,
            success = true
        )

        collector.recordExport(metrics1)
        collector.recordExport(metrics2)

        val recorded = collector.getMetrics()
        recorded.size shouldBe 2
        recorded[0] shouldBe metrics1
        recorded[1] shouldBe metrics2
    }

    test("InMemoryMetricsCollector should calculate average export time") {
        val collector = InMemoryMetricsCollector()
        val now = Clock.System.now()

        collector.recordExport(
            ExportMetrics(
                documentId = "doc1",
                startTime = now,
                endTime = now + 1.seconds,
                duration = 1.seconds,
                blocksCount = 10,
                imagesDownloaded = 0,
                filesDownloaded = 0,
                boardsExported = 0,
                success = true
            )
        )

        collector.recordExport(
            ExportMetrics(
                documentId = "doc2",
                startTime = now,
                endTime = now + 3.seconds,
                duration = 3.seconds,
                blocksCount = 20,
                imagesDownloaded = 0,
                filesDownloaded = 0,
                boardsExported = 0,
                success = true
            )
        )

        // Average: (1000ms + 3000ms) / 2 = 2000ms
        collector.getAverageExportTime() shouldBe 2.seconds
    }

    test("InMemoryMetricsCollector should calculate success rate") {
        val collector = InMemoryMetricsCollector()
        val now = Clock.System.now()

        // 3 successful exports
        repeat(3) { i ->
            collector.recordExport(
                ExportMetrics(
                    documentId = "doc$i",
                    startTime = now,
                    endTime = now + 1.seconds,
                    duration = 1.seconds,
                    blocksCount = 10,
                    imagesDownloaded = 0,
                    filesDownloaded = 0,
                    boardsExported = 0,
                    success = true
                )
            )
        }

        // 1 failed export
        collector.recordExport(
            ExportMetrics(
                documentId = "doc_fail",
                startTime = now,
                endTime = now + 1.seconds,
                duration = 1.seconds,
                blocksCount = 5,
                imagesDownloaded = 0,
                filesDownloaded = 0,
                boardsExported = 0,
                success = false,
                errorMessage = "Test error"
            )
        )

        // Success rate: 3/4 = 0.75
        collector.getSuccessRate() shouldBeExactly 0.75
    }

    test("InMemoryMetricsCollector should return zero for empty metrics") {
        val collector = InMemoryMetricsCollector()

        collector.getMetrics().size shouldBe 0
        collector.getAverageExportTime() shouldBe 0.milliseconds
        collector.getSuccessRate() shouldBeExactly 0.0
    }

    test("InMemoryMetricsCollector should clear all metrics") {
        val collector = InMemoryMetricsCollector()
        val now = Clock.System.now()

        collector.recordExport(
            ExportMetrics(
                documentId = "doc1",
                startTime = now,
                endTime = now + 1.seconds,
                duration = 1.seconds,
                blocksCount = 10,
                imagesDownloaded = 0,
                filesDownloaded = 0,
                boardsExported = 0,
                success = true
            )
        )

        collector.getMetrics().size shouldBe 1

        collector.clear()

        collector.getMetrics().size shouldBe 0
        collector.getAverageExportTime() shouldBe 0.milliseconds
        collector.getSuccessRate() shouldBeExactly 0.0
    }

    test("ExportMetrics should calculate averageTimePerBlock correctly") {
        val now = Clock.System.now()

        val metrics = ExportMetrics(
            documentId = "doc1",
            startTime = now,
            endTime = now + 10.seconds,
            duration = 10.seconds,
            blocksCount = 5,
            imagesDownloaded = 0,
            filesDownloaded = 0,
            boardsExported = 0,
            success = true
        )

        // 10 seconds / 5 blocks = 2 seconds per block
        metrics.averageTimePerBlock shouldBe 2.seconds
    }

    test("ExportMetrics should return zero averageTimePerBlock for zero blocks") {
        val now = Clock.System.now()

        val metrics = ExportMetrics(
            documentId = "doc1",
            startTime = now,
            endTime = now + 10.seconds,
            duration = 10.seconds,
            blocksCount = 0,
            imagesDownloaded = 0,
            filesDownloaded = 0,
            boardsExported = 0,
            success = true
        )

        metrics.averageTimePerBlock shouldBe 0.milliseconds
    }
})


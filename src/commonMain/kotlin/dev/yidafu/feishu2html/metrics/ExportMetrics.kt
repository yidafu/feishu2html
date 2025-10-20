package dev.yidafu.feishu2html.metrics

import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Metrics data for a single document export operation.
 *
 * @property documentId The ID of the exported document
 * @property startTime The timestamp when export started
 * @property endTime The timestamp when export completed
 * @property duration The total duration of the export
 * @property blocksCount The number of blocks processed
 * @property imagesDownloaded The number of images downloaded
 * @property filesDownloaded The number of files downloaded
 * @property boardsExported The number of boards exported
 * @property totalBytes Total bytes downloaded (optional)
 * @property success Whether the export succeeded
 * @property errorMessage Error message if export failed
 */
data class ExportMetrics(
    val documentId: String,
    val startTime: Instant,
    val endTime: Instant,
    val duration: Duration,
    val blocksCount: Int,
    val imagesDownloaded: Int,
    val filesDownloaded: Int,
    val boardsExported: Int,
    val totalBytes: Long = 0,
    val success: Boolean,
    val errorMessage: String? = null,
) {
    /**
     * Average time spent per block.
     */
    val averageTimePerBlock: Duration
        get() = if (blocksCount > 0) duration / blocksCount else Duration.ZERO
}

/**
 * Interface for collecting and analyzing export metrics.
 */
interface MetricsCollector {
    /**
     * Record a single export operation's metrics.
     */
    fun recordExport(metrics: ExportMetrics)

    /**
     * Get all recorded metrics.
     */
    fun getMetrics(): List<ExportMetrics>

    /**
     * Calculate average export time across all recorded exports.
     */
    fun getAverageExportTime(): Duration

    /**
     * Calculate success rate across all recorded exports.
     * Returns a value between 0.0 and 1.0.
     */
    fun getSuccessRate(): Double

    /**
     * Clear all recorded metrics.
     */
    fun clear()
}

/**
 * In-memory implementation of MetricsCollector.
 * Stores all metrics in memory for the lifetime of the collector.
 */
class InMemoryMetricsCollector : MetricsCollector {
    private val metrics = mutableListOf<ExportMetrics>()

    override fun recordExport(metrics: ExportMetrics) {
        this.metrics.add(metrics)
    }

    override fun getMetrics(): List<ExportMetrics> = metrics.toList()

    override fun getAverageExportTime(): Duration {
        if (metrics.isEmpty()) return Duration.ZERO
        val totalMs = metrics.sumOf { it.duration.inWholeMilliseconds }
        return (totalMs / metrics.size).milliseconds
    }

    override fun getSuccessRate(): Double {
        if (metrics.isEmpty()) return 0.0
        val successCount = metrics.count { it.success }
        return successCount.toDouble() / metrics.size
    }

    override fun clear() {
        metrics.clear()
    }
}


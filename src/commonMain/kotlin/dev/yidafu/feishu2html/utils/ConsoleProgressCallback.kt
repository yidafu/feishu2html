package dev.yidafu.feishu2html.utils

import dev.yidafu.feishu2html.ExportProgressCallback
import kotlinx.datetime.Clock

/**
 * Console-based progress callback with enhanced visual output
 */
class ConsoleProgressCallback(
    private val verbose: Boolean = false
) : ExportProgressCallback {
    private val documentStartTimes = mutableMapOf<String, Long>()
    private val documentStats = mutableMapOf<String, MutableMap<String, Int>>()

    override fun onStart(documentId: String) {
        documentStartTimes[documentId] = Clock.System.now().toEpochMilliseconds()
        documentStats[documentId] = mutableMapOf()

        println(LogFormatter.processing("Starting export for document: $documentId"))
    }

    override fun onMetadataFetched(documentId: String, title: String, blocksCount: Int) {
        println(LogFormatter.documentStart(documentId, title))
        println(LogFormatter.keyValue("Blocks Count", blocksCount.toString(), LogIcons.DOCUMENT))

        documentStats[documentId]?.set("Blocks", blocksCount)
    }

    override fun onContentFetched(documentId: String, blocksCount: Int) {
        if (verbose) {
            println(LogFormatter.success("Content fetched: $blocksCount blocks"))
        }
    }

    override fun onAssetDownloading(documentId: String, current: Int, total: Int) {
        val progress = LogFormatter.progressBar(current, total)
        val icon = when {
            current < total / 3 -> LogIcons.DOWNLOAD
            current < total * 2 / 3 -> LogIcons.PROCESSING
            else -> LogIcons.SUCCESS
        }

        // Use carriage return to update the same line
        print("\r  $icon Downloading assets: $progress")

        if (current >= total) {
            println() // New line when complete
            documentStats[documentId]?.set("Assets Downloaded", total)
        }
    }

    override fun onComplete(documentId: String, outputPath: String) {
        val startTime = documentStartTimes[documentId] ?: 0
        val duration = Clock.System.now().toEpochMilliseconds() - startTime
        val stats = documentStats[documentId] ?: mutableMapOf()

        // Extract title from outputPath if available
        val title = outputPath.substringAfterLast('/').substringBeforeLast('.')

        println(LogFormatter.documentComplete(title, duration, stats))
        println(LogFormatter.keyValue("Output File", outputPath, LogIcons.FILE))
    }

    override fun onError(documentId: String, error: Throwable) {
        println()
        println(LogFormatter.error("Export failed for document: $documentId"))
        println(LogFormatter.keyValue("Error", error.message ?: "Unknown error", LogIcons.CROSS))

        if (verbose && error.stackTraceToString().isNotEmpty()) {
            println(LogFormatter.debug("Stack trace:"))
            println(LogFormatter.colorize(error.stackTraceToString(), AnsiColor.BRIGHT_BLACK))
        }
    }
}

/**
 * Extension function to colorize strings (for debugging)
 */
private fun LogFormatter.colorize(text: String, color: String): String {
    return "$color$text${AnsiColor.RESET}"
}


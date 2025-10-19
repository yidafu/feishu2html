package dev.yidafu.feishu2html.cli

import dev.yidafu.feishu2html.Feishu2Html
import dev.yidafu.feishu2html.Feishu2HtmlOptions

/**
 * Shared CLI logic for all platforms
 *
 * This contains the common command-line interface logic that is reused
 * across JVM, JS, and Native platforms.
 */
object CliRunner {
    /**
     * Display help message
     */
    fun showHelp() {
        println("Usage: feishu2html <app_id> <app_secret> <document_id> [document_id2] [document_id3] ...")
        println()
        println("Arguments:")
        println("  app_id       - Feishu application App ID")
        println("  app_secret   - Feishu application App Secret")
        println("  document_id  - Document ID(s) to export (can specify multiple)")
        println()
        println("Example:")
        println("  feishu2html cli_a1234567890abcde cli_1234567890abcdef1234567890abcd doxcnABCDEFGHIJK")
    }

    /**
     * Parse and validate command line arguments
     *
     * @return Triple of (appId, appSecret, documentIds) or null if invalid
     */
    fun parseArguments(args: List<String>): Triple<String, String, List<String>>? {
        if (args.size < 3) {
            return null
        }

        val appId = args[0]
        val appSecret = args[1]
        val documentIds = args.drop(2)

        return Triple(appId, appSecret, documentIds)
    }

    /**
     * Print export banner
     */
    fun printBanner(
        appId: String,
        documentCount: Int,
        platformName: String = "",
    ) {
        val title =
            if (platformName.isNotEmpty()) {
                "Feishu Document to HTML Converter ($platformName)"
            } else {
                "Feishu Document to HTML Converter"
            }

        println("=".repeat(60))
        println(title)
        println("=".repeat(60))
        println("App ID: $appId")
        println("Documents to export: $documentCount")
        println("=".repeat(60))
        println()
    }

    /**
     * Create default options
     */
    fun createOptions(
        appId: String,
        appSecret: String,
    ): Feishu2HtmlOptions =
        Feishu2HtmlOptions(
            appId = appId,
            appSecret = appSecret,
            outputDir = "./output",
            imageDir = "./output/images",
            fileDir = "./output/files",
        )

    /**
     * Execute the export process
     *
     * This is a suspend function that can be called from any platform's
     * coroutine context (runBlocking, GlobalScope.promise, etc.)
     */
    suspend fun runExport(
        appId: String,
        appSecret: String,
        documentIds: List<String>,
    ) {
        val options = createOptions(appId, appSecret)

        println("Initializing Feishu2Html with output directory: ${options.outputDir}")

        Feishu2Html(options).use { feishu2Html ->
            println("Starting document export process")

            if (documentIds.size == 1) {
                println("Exporting single document: ${documentIds[0]}")
                feishu2Html.export(documentIds[0])
            } else {
                println("Batch exporting ${documentIds.size} documents")
                feishu2Html.exportBatch(documentIds)
            }

            println()
            println("=".repeat(60))
            println("Export completed!")
            println("Output directory: ${options.outputDir}")
            println("=".repeat(60))
        }
    }

    /**
     * Handle export errors
     */
    fun handleError(e: Exception) {
        println()
        println("=".repeat(60))
        println("Error: ${e.message}")
        println("=".repeat(60))
    }
}


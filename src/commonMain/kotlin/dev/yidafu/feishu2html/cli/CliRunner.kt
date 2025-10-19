package dev.yidafu.feishu2html.cli

import dev.yidafu.feishu2html.Feishu2Html
import dev.yidafu.feishu2html.Feishu2HtmlOptions
import dev.yidafu.feishu2html.TemplateMode
import kotlinx.cli.*

/**
 * Parsed CLI arguments
 */
data class CliArgs(
    val appId: String,
    val appSecret: String,
    val documentIds: List<String>,
    val templateMode: TemplateMode = TemplateMode.DEFAULT,
)

/**
 * Shared CLI logic for all platforms using kotlinx-cli
 *
 * This contains the common command-line interface logic that is reused
 * across JVM, JS, and Native platforms.
 */
object CliRunner {
    /**
     * Parse and validate command line arguments using kotlinx-cli
     *
     * @return CliArgs or null if invalid
     */
    fun parseArguments(args: Array<String>): CliArgs? {
        // Check for help flag first
        if (args.contains("--help") || args.contains("-h")) {
            showHelp()
            return null
        }

        val parser = ArgParser("feishu2html")

        // Define --template option
        val templateValue by parser.option(
            ArgType.String,
            shortName = "t",
            fullName = "template",
            description = "HTML template mode (default, fragment, or full)"
        ).default("default")

        // Define required positional arguments
        val appId by parser.argument(
            ArgType.String,
            fullName = "app-id",
            description = "Feishu application App ID"
        )

        val appSecret by parser.argument(
            ArgType.String,
            fullName = "app-secret",
            description = "Feishu application App Secret"
        )

        val documentIds by parser.argument(
            ArgType.String,
            fullName = "document-ids",
            description = "Document ID(s) to export"
        ).vararg()

        return try {
            parser.parse(args)

            if (documentIds.isEmpty()) {
                println("Error: At least one document ID is required")
                println()
                showHelp()
                return null
            }

            // Convert template string to TemplateMode
            val template = when (templateValue.lowercase()) {
                "default" -> TemplateMode.DEFAULT
                "fragment" -> TemplateMode.FRAGMENT
                "full" -> TemplateMode.FULL
                else -> {
                    println("Error: Invalid template mode '$templateValue'. Use: default, fragment, or full")
                    println()
                    showHelp()
                    return null
                }
            }

            CliArgs(
                appId = appId,
                appSecret = appSecret,
                documentIds = documentIds.toList(),
                templateMode = template
            )
        } catch (e: IllegalStateException) {
            // kotlinx-cli throws IllegalStateException for parsing errors
            // Silently return null for tests
            null
        } catch (e: Exception) {
            // Other exceptions - return null
            null
        }
    }

    /**
     * Display help message
     */
    fun showHelp() {
        println("Usage: feishu2html [OPTIONS] <app-id> <app-secret> <document-ids>...")
        println()
        println("Arguments:")
        println("  app-id                  Feishu application App ID")
        println("  app-secret              Feishu application App Secret")
        println("  document-ids            Document ID(s) to export (one or more)")
        println()
        println("Options:")
        println("  -t, --template <mode>   HTML template mode: default | fragment | full")
        println("                          default:  Standard Feishu template (default)")
        println("                          fragment: Minimal template with custom body wrapper")
        println("                          full:     Minimal template with basic HTML structure")
        println("  -h, --help              Show this help message")
        println()
        println("Examples:")
        println("  feishu2html cli_a1234567890abcde cli_1234567890abcdef1234567890abcd doxcnABCDEFGHIJK")
        println("  feishu2html --template fragment cli_a123 cli_1234 doxcnABC")
        println("  feishu2html -t full cli_a123 cli_1234 doxcnABC doxcnDEF")
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
     * Create options from CLI args
     */
    fun createOptions(
        appId: String,
        appSecret: String,
        templateMode: TemplateMode = TemplateMode.DEFAULT,
    ): Feishu2HtmlOptions =
        Feishu2HtmlOptions(
            appId = appId,
            appSecret = appSecret,
            outputDir = "./output",
            imageDir = "./output/images",
            fileDir = "./output/files",
            templateMode = templateMode,
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
        templateMode: TemplateMode = TemplateMode.DEFAULT,
    ) {
        val options = createOptions(appId, appSecret, templateMode)

        println("Initializing Feishu2Html with output directory: ${options.outputDir}")
        println("Template mode: $templateMode")

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

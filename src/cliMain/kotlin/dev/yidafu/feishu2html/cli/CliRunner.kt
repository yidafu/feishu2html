package dev.yidafu.feishu2html.cli

import dev.yidafu.feishu2html.Feishu2Html
import dev.yidafu.feishu2html.Feishu2HtmlOptions
import dev.yidafu.feishu2html.converter.HtmlTemplate
import dev.yidafu.feishu2html.utils.LogFormatter
import dev.yidafu.feishu2html.utils.ConsoleProgressCallback
import dev.yidafu.feishu2html.utils.LogIcons
import kotlinx.cli.*

/**
 * Parsed CLI arguments
 */
data class CliArgs(
    val appId: String,
    val appSecret: String,
    val documentIds: List<String>,
    val template: HtmlTemplate = HtmlTemplate.DefaultCli,
    val inlineImages: Boolean = false,
    val inlineCss: Boolean = false,
    val hideUnsupported: Boolean = false,
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

        // Define --inline-images option
        val inlineImages by parser.option(
            ArgType.Boolean,
            fullName = "inline-images",
            description = "Embed images as base64 data URLs"
        ).default(false)

        // Define --inline-css option
        val inlineCss by parser.option(
            ArgType.Boolean,
            fullName = "inline-css",
            description = "Embed CSS styles inline in <style> tag"
        ).default(false)

        // Define --hide-unsupported option
        val hideUnsupported by parser.option(
            ArgType.Boolean,
            fullName = "hide-unsupported",
            description = "Hide unsupported block type warnings"
        ).default(false)

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

            // Convert template string to HtmlTemplate
            val template = when (templateValue.lowercase()) {
                "default" -> HtmlTemplate.DefaultCli
                "fragment" -> HtmlTemplate.FragmentCli
                "full" -> HtmlTemplate.PlainCli
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
                template = template,
                inlineImages = inlineImages,
                inlineCss = inlineCss,
                hideUnsupported = hideUnsupported
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
        println("  --inline-images         Embed images as base64 data URLs")
        println("  --inline-css            Embed CSS styles inline in <style> tag")
        println("  --hide-unsupported      Hide unsupported block type warnings")
        println("  -h, --help              Show this help message")
        println()
        println("Examples:")
        println("  feishu2html cli_a1234567890abcde cli_1234567890abcdef1234567890abcd doxcnABCDEFGHIJK")
        println("  feishu2html --template fragment cli_a123 cli_1234 doxcnABC")
        println("  feishu2html --inline-images --inline-css cli_a123 cli_1234 doxcnABC")
        println("  feishu2html -t full --inline-images cli_a123 cli_1234 doxcnABC doxcnDEF")
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

        println(LogFormatter.banner(title))
        println(LogFormatter.keyValue("App ID", appId, LogIcons.STAR))
        println(LogFormatter.keyValue("Documents", documentCount.toString(), LogIcons.DOCUMENT))
        println()
    }

    /**
     * Create options from CLI args
     */
    fun createOptions(
        appId: String,
        appSecret: String,
        template: HtmlTemplate = HtmlTemplate.DefaultCli,
        inlineImages: Boolean = false,
        inlineCss: Boolean = false,
        hideUnsupported: Boolean = false,
    ): Feishu2HtmlOptions =
        Feishu2HtmlOptions(
            appId = appId,
            appSecret = appSecret,
            outputDir = "./output",
            imageDir = "./output/images",
            fileDir = "./output/files",
            template = template,
            inlineImages = inlineImages,
            externalCss = !inlineCss,  // Invert logic: inlineCss = true means externalCss = false
            showUnsupportedBlocks = !hideUnsupported,  // Invert logic: hideUnsupported = true means showUnsupportedBlocks = false
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
        template: HtmlTemplate = HtmlTemplate.DefaultCli,
        inlineImages: Boolean = false,
        inlineCss: Boolean = false,
        hideUnsupported: Boolean = false,
    ) {
        val options = createOptions(appId, appSecret, template, inlineImages, inlineCss, hideUnsupported)

        println(LogFormatter.section("Configuration"))
        println(LogFormatter.keyValue("Output Directory", options.outputDir, LogIcons.FOLDER))
        println(LogFormatter.keyValue("Template", template::class.simpleName ?: "Default", LogIcons.DOCUMENT))
        println(LogFormatter.keyValue("Inline Images", if (inlineImages) "Enabled" else "Disabled", LogIcons.IMAGE))
        println(LogFormatter.keyValue("Inline CSS", if (inlineCss) "Enabled" else "Disabled", LogIcons.PROCESSING))
        println(LogFormatter.keyValue("Hide Unsupported", if (hideUnsupported) "Yes" else "No", LogIcons.INFO))

        val progressCallback = ConsoleProgressCallback(verbose = true)

        Feishu2Html(options).use { feishu2Html ->
            println(LogFormatter.section("Export Process"))

            if (documentIds.size == 1) {
                println(LogFormatter.processing("Exporting single document: ${documentIds[0]}"))
                feishu2Html.export(documentIds[0], progressCallback = progressCallback)
            } else {
                println(LogFormatter.processing("Batch exporting ${documentIds.size} documents"))
                feishu2Html.exportBatch(documentIds, progressCallback = progressCallback)
            }

            println()
            println(LogFormatter.section("Summary"))
            println(LogFormatter.success("Export completed successfully!"))
            println(LogFormatter.keyValue("Output Directory", options.outputDir, LogIcons.FOLDER))
            println()
        }
    }

    /**
     * Handle export errors
     */
    fun handleError(e: Exception) {
        println()
        println(LogFormatter.error("Export failed"))
        println(LogFormatter.keyValue("Error Message", e.message ?: "Unknown error", LogIcons.CROSS))
        println()
    }
}

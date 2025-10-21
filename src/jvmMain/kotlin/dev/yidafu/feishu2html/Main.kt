package dev.yidafu.feishu2html

import dev.yidafu.feishu2html.cli.CliRunner
import dev.yidafu.feishu2html.utils.LogFormatter
import dev.yidafu.feishu2html.utils.LogIcons
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("dev.yidafu.feishu2html.Main")

/**
 * JVM command line entry point
 *
 * Usage:
 * ./gradlew run --args="<app_id> <app_secret> <document_id>"
 */
fun main(args: Array<String>) {
    val parsed = CliRunner.parseArguments(args)
    if (parsed == null) {
        logger.error("Insufficient arguments provided: expected at least 3, got {}", args.size)
        CliRunner.showHelp()
        return
    }

    // Only show detailed startup info in verbose mode
    if (parsed.verbose) {
        logger.info("Feishu2HTML application started (JVM)")
        println(LogFormatter.info("Feishu2HTML application started"))
        println(LogFormatter.keyValue("Platform", "JVM", LogIcons.PROCESSING))
        println(LogFormatter.keyValue("Arguments", args.size.toString(), LogIcons.INFO))
        logger.info("Parsed arguments - App ID: {}, Document count: {}, Template: {}",
            parsed.appId, parsed.documentIds.size, parsed.template::class.simpleName)
        logger.debug("Document IDs to export: {}", parsed.documentIds.joinToString(", "))
    }

    CliRunner.printBanner(parsed.appId, parsed.documentIds.size, "JVM")

    try {
        if (parsed.verbose) {
            logger.info("Starting export process with template: {}, inline images: {}, inline CSS: {}, hide unsupported: {}, verbose: {}",
                parsed.template::class.simpleName, parsed.inlineImages, parsed.inlineCss, parsed.hideUnsupported, parsed.verbose)
        }
        runBlocking {
            CliRunner.runExport(
                parsed.appId,
                parsed.appSecret,
                parsed.documentIds,
                parsed.template,
                parsed.inlineImages,
                parsed.inlineCss,
                parsed.hideUnsupported,
                parsed.verbose
            )
        }
        if (parsed.verbose) {
            logger.info("Export completed successfully")
        }
    } catch (e: Exception) {
        logger.error("Export failed: {}", e.message, e)
        CliRunner.handleError(e)
        if (parsed.verbose) {
            e.printStackTrace()
        }
    }

    if (parsed.verbose) {
        logger.info("Feishu2HTML application terminated")
    }
}

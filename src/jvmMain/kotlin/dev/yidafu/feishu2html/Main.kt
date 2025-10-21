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
    logger.info("Feishu2HTML application started (JVM)")

    // Visual startup message
    println(LogFormatter.info("Feishu2HTML application started"))
    println(LogFormatter.keyValue("Platform", "JVM", LogIcons.PROCESSING))
    println(LogFormatter.keyValue("Arguments", args.size.toString(), LogIcons.INFO))

    val parsed = CliRunner.parseArguments(args)
    if (parsed == null) {
        logger.error("Insufficient arguments provided: expected at least 3, got {}", args.size)
        CliRunner.showHelp()
        return
    }

    logger.info("Parsed arguments - App ID: {}, Document count: {}, Template: {}",
        parsed.appId, parsed.documentIds.size, parsed.template::class.simpleName)
    logger.debug("Document IDs to export: {}", parsed.documentIds.joinToString(", "))

    CliRunner.printBanner(parsed.appId, parsed.documentIds.size, "JVM")

    try {
        logger.info("Starting export process with template: {}, inline images: {}, inline CSS: {}, hide unsupported: {}",
            parsed.template::class.simpleName, parsed.inlineImages, parsed.inlineCss, parsed.hideUnsupported)
        runBlocking {
            CliRunner.runExport(
                parsed.appId,
                parsed.appSecret,
                parsed.documentIds,
                parsed.template,
                parsed.inlineImages,
                parsed.inlineCss,
                parsed.hideUnsupported
            )
        }
        logger.info("Export completed successfully")
        println(LogFormatter.success("All operations completed successfully!"))
    } catch (e: Exception) {
        logger.error("Export failed: {}", e.message, e)
        CliRunner.handleError(e)
        if (logger.isDebugEnabled) {
            e.printStackTrace()
        }
    }

    logger.info("Feishu2HTML application terminated")
    println(LogFormatter.info("Application terminated"))
}

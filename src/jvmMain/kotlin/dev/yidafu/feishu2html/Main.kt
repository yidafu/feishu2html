package dev.yidafu.feishu2html

import dev.yidafu.feishu2html.cli.CliRunner
import dev.yidafu.feishu2html.utils.LogFormatter
import dev.yidafu.feishu2html.utils.LogIcons
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger

private val logger = LoggerFactory.getLogger("dev.yidafu.feishu2html.Main")

/**
 * Set log level dynamically
 */
private fun setLogLevel(level: String) {
    val rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
    rootLogger.level = when (level.uppercase()) {
        "DEBUG" -> Level.DEBUG
        "INFO" -> Level.INFO
        "WARN" -> Level.WARN
        "ERROR" -> Level.ERROR
        else -> Level.WARN
    }
}

/**
 * JVM command line entry point
 *
 * Usage:
 * ./gradlew run --args="<app_id> <app_secret> <document_id>"
 */
fun main(args: Array<String>) {
    val parsed = CliRunner.parseArguments(args)
    if (parsed == null) {
        CliRunner.showHelp()
        return
    }

    // Set log level based on verbose flag
    setLogLevel(if (parsed.verbose) "DEBUG" else "WARN")

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

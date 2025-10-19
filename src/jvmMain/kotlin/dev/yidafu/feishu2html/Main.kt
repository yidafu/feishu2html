package dev.yidafu.feishu2html

import dev.yidafu.feishu2html.cli.CliRunner
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
    println("Feishu2HTML application started")
    println("Received ${args.size} command line arguments")

    val parsed = CliRunner.parseArguments(args.toList())
    if (parsed == null) {
        logger.error("Insufficient arguments provided: expected at least 3, got {}", args.size)
        CliRunner.showHelp()
        return
    }

    val (appId, appSecret, documentIds) = parsed
    logger.info("Parsed arguments - App ID: {}, Document count: {}", appId, documentIds.size)
    logger.debug("Document IDs to export: {}", documentIds.joinToString(", "))

    CliRunner.printBanner(appId, documentIds.size, "JVM")

    try {
        logger.info("Starting export process")
        runBlocking {
            CliRunner.runExport(appId, appSecret, documentIds)
        }
        logger.info("Export completed successfully")
    } catch (e: Exception) {
        logger.error("Export failed: {}", e.message, e)
        CliRunner.handleError(e)
        e.printStackTrace()
    }

    logger.info("Feishu2HTML application terminated")
}

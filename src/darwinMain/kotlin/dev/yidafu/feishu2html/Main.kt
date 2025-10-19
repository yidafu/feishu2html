package dev.yidafu.feishu2html

import dev.yidafu.feishu2html.cli.CliRunner
import kotlinx.coroutines.runBlocking

/**
 * Command line entry point for macOS/Darwin platforms
 *
 * Usage:
 * ./feishu2html <app_id> <app_secret> <document_id> [document_id2] [document_id3] ...
 */
fun main(args: Array<String>) {
    println("Feishu2HTML application started")
    println("Received ${args.size} command line arguments")

    val parsed = CliRunner.parseArguments(args)
    if (parsed == null) {
        CliRunner.showHelp()
        return
    }

    println("Template mode: ${parsed.templateMode}")
    CliRunner.printBanner(parsed.appId, parsed.documentIds.size, "macOS")

    runBlocking {
        try {
            CliRunner.runExport(parsed.appId, parsed.appSecret, parsed.documentIds, parsed.templateMode)
        } catch (e: Exception) {
            CliRunner.handleError(e)
            e.printStackTrace()
            throw e
        }
    }

    println("Feishu2HTML application terminated")
}


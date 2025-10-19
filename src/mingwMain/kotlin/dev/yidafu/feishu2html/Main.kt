package dev.yidafu.feishu2html

import dev.yidafu.feishu2html.cli.CliRunner
import kotlinx.coroutines.runBlocking

/**
 * Command line entry point for Windows platforms
 *
 * Usage:
 * feishu2html.exe <app_id> <app_secret> <document_id> [document_id2] [document_id3] ...
 */
fun main(args: Array<String>) {
    println("Feishu2HTML application started")
    println("Received ${args.size} command line arguments")

    val parsed = CliRunner.parseArguments(args.toList())
    if (parsed == null) {
        CliRunner.showHelp()
        return
    }

    val (appId, appSecret, documentIds) = parsed
    CliRunner.printBanner(appId, documentIds.size, "Windows")

    runBlocking {
        try {
            CliRunner.runExport(appId, appSecret, documentIds)
        } catch (e: Exception) {
            CliRunner.handleError(e)
            e.printStackTrace()
            throw e
        }
    }

    println("Feishu2HTML application terminated")
}


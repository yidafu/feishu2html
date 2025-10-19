package dev.yidafu.feishu2html

import dev.yidafu.feishu2html.cli.CliRunner
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

/**
 * Node.js process object for accessing command line arguments
 */
external val process: Process

external interface Process {
    val argv: Array<String>
}

/**
 * Command line entry point for JS/Node.js platform
 *
 * Usage:
 * node feishu2html.js <app_id> <app_secret> <document_id> [document_id2] [document_id3] ...
 */
fun main() {
    // process.argv[0] is node executable, argv[1] is script path
    // Actual arguments start from argv[2]
    val args = process.argv.sliceArray(2 until process.argv.size)

    println("Feishu2HTML application started")
    println("Received ${args.size} command line arguments")

    val parsed = CliRunner.parseArguments(args)
    if (parsed == null) {
        CliRunner.showHelp()
        return
    }

    println("Template mode: ${parsed.templateMode}")
    CliRunner.printBanner(parsed.appId, parsed.documentIds.size, "Node.js")

    // Use GlobalScope.promise to return a Promise for Node.js async handling
    GlobalScope.promise {
        try {
            CliRunner.runExport(
                parsed.appId,
                parsed.appSecret,
                parsed.documentIds,
                parsed.templateMode,
                parsed.inlineImages,
                parsed.inlineCss,
                parsed.hideUnsupported
            )
        } catch (e: Exception) {
            CliRunner.handleError(e)
            console.error(e)
            throw e
        }
    }
}

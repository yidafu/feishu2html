package dev.yidafu.feishu2html

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

    if (args.size < 3) {
        println("Usage: feishu2html <app_id> <app_secret> <document_id> [document_id2] [document_id3] ...")
        println()
        println("Arguments:")
        println("  app_id       - Feishu application App ID")
        println("  app_secret   - Feishu application App Secret")
        println("  document_id  - Document ID(s) to export (can specify multiple)")
        println()
        println("Example:")
        println("  feishu2html cli_a1234567890abcde cli_1234567890abcdef1234567890abcd doxcnABCDEFGHIJK")
        return
    }

    val appId = args[0]
    val appSecret = args[1]
    val documentIds = args.drop(2)

    println("=".repeat(60))
    println("Feishu Document to HTML Converter")
    println("=".repeat(60))
    println("App ID: $appId")
    println("Documents to export: ${documentIds.size}")
    println("=".repeat(60))
    println()

    val options =
        Feishu2HtmlOptions(
            appId = appId,
            appSecret = appSecret,
            outputDir = "./output",
            imageDir = "./output/images",
            fileDir = "./output/files",
        )

    println("Initializing Feishu2Html with output directory: ${options.outputDir}")

    // Use GlobalScope.promise to return a Promise for Node.js async handling
    GlobalScope.promise {
        try {
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
        } catch (e: Exception) {
            println()
            println("=".repeat(60))
            println("Error: ${e.message}")
            console.error(e)
            println("=".repeat(60))
            throw e
        }
    }
}

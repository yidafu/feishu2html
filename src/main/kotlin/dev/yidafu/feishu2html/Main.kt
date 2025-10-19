package dev.yidafu.feishu2html

import kotlinx.coroutines.runBlocking

/**
 * 命令行入口
 *
 * 使用方式：
 * ./gradlew :feishu2html:run --args="<app_id> <app_secret> <document_id>"
 */
fun main(args: Array<String>) {
    if (args.size < 3) {
        println("使用方式: feishu2html <app_id> <app_secret> <document_id> [document_id2] [document_id3] ...")
        println()
        println("参数说明:")
        println("  app_id       - 飞书应用的 App ID")
        println("  app_secret   - 飞书应用的 App Secret")
        println("  document_id  - 要导出的文档 ID（可以指定多个）")
        println()
        println("示例:")
        println("  feishu2html cli_a1234567890abcde cli_1234567890abcdef1234567890abcd doxcnABCDEFGHIJK")
        return
    }

    val appId = args[0]
    val appSecret = args[1]
    val documentIds = args.drop(2)

    println("=".repeat(60))
    println("飞书文档转HTML工具")
    println("=".repeat(60))
    println("App ID: $appId")
    println("要导出的文档数量: ${documentIds.size}")
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

    val feishu2Html = Feishu2Html(options)

    try {
        runBlocking {
            if (documentIds.size == 1) {
                feishu2Html.export(documentIds[0])
            } else {
                feishu2Html.exportBatch(documentIds)
            }
        }

        println()
        println("=".repeat(60))
        println("导出完成！")
        println("输出目录: ${options.outputDir}")
        println("=".repeat(60))
    } catch (e: Exception) {
        println()
        println("=".repeat(60))
        println("错误: ${e.message}")
        e.printStackTrace()
        println("=".repeat(60))
    } finally {
        feishu2Html.close()
    }
}

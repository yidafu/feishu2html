/**
 * 飞书文档转HTML配置示例
 *
 * 使用方法：
 * 1. 复制此文件为 config.kts
 * 2. 填写你的飞书应用信息
 * 3. 添加要导出的文档ID
 */

// 飞书应用配置
val appId = "cli_your_app_id_here" // 替换为你的 App ID
val appSecret = "your_app_secret_here" // 替换为你的 App Secret

// 要导出的文档ID列表
val documentIds =
    listOf(
        "doxcnXXXXXXXXXXXXXX", // 文档1
        "doxcnYYYYYYYYYYYYYY", // 文档2
        "doxcnZZZZZZZZZZZZZZ", // 文档3
    )

// 输出配置
val outputDir = "./output" // HTML文件输出目录
val imageDir = "./output/images" // 图片保存目录
val fileDir = "./output/files" // 附件保存目录

// 自定义CSS（可选）
val customCss: String? = null
// 如需自定义样式，取消注释下面的代码：
/*
val customCss = """
    body {
        font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
        background-color: #f9f9f9;
    }

    h1 {
        color: #1a73e8;
    }
"""
*/
// 使用kotest作为单测框架。单测覆盖率要达到98%。测试请求结果

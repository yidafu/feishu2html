# 性能优化指南

本指南介绍 Feishu2Html 的性能特性、基准测试结果和优化建议。

## 性能基线

以下是在 Mock 环境下的性能基准测试结果（使用模拟的 API 调用，不包括网络延迟）：

### 测试环境

- **OS**: macOS 14.1
- **JVM**: OpenJDK 17
- **Kotlin**: 2.1.0
- **测试方法**: 每个场景运行 3 次取平均值

### 基准测试结果

| 场景 | 文档大小 | 平均时间 | 最小时间 | 最大时间 | 性能目标 |
|------|---------|---------|---------|---------|---------|
| 小文档 | 10 blocks, 0 images | ~90ms | ~45ms | ~180ms | < 1s |
| 中等文档 | 100 blocks, 10 images | ~50ms | ~42ms | ~54ms | < 3s |
| 大文档 | 1000 blocks, 50 assets | ~60ms | ~54ms | ~78ms | < 15s |
| 批量导出 | 10 个小文档 | ~400ms total | - | - | - |
| 带画板文档 | 20 blocks, 5 boards | ~40ms | ~37ms | ~41ms | - |

**注意**: 这些是 Mock 测试结果，实际性能会受网络延迟、文档复杂度、系统资源等因素影响。

### 并发下载性能

测试不同 `maxConcurrentDownloads` 值对性能的影响：

| 并发数 | 导出时间 | 说明 |
|-------|---------|------|
| 5 | ~50ms | 较低并发 |
| 10 | ~50ms | 默认并发（推荐） |
| 20 | ~50ms | 较高并发 |

**建议**: 默认值 10 在大多数情况下是一个较好的平衡点。对于网络条件较好的环境，可以适当提高到 15-20。

---

## 性能监控

### 启用性能指标收集

Feishu2Html 提供内置的性能监控功能，可以跟踪每次导出操作的详细指标。

#### 基本用法

```kotlin
import dev.yidafu.feishu2html.Feishu2Html
import dev.yidafu.feishu2html.Feishu2HtmlOptions
import dev.yidafu.feishu2html.metrics.InMemoryMetricsCollector

// 创建指标收集器
val metricsCollector = InMemoryMetricsCollector()

// 配置选项
val options = Feishu2HtmlOptions(
    appId = "your_app_id",
    appSecret = "your_app_secret",
    outputDir = "./output",
    metricsCollector = metricsCollector
)

// 导出文档
val converter = Feishu2Html(options)
converter.export("doc_id_1")
converter.export("doc_id_2")
converter.export("doc_id_3")
converter.close()

// 查看性能指标
println("=== 性能统计 ===")
println("平均导出时间: ${metricsCollector.getAverageExportTime()}")
println("成功率: ${metricsCollector.getSuccessRate() * 100}%")
println("总导出数: ${metricsCollector.getMetrics().size}")

// 查看每个文档的详细指标
metricsCollector.getMetrics().forEach { metric ->
    println("\n文档: ${metric.documentId}")
    println("  时长: ${metric.duration}")
    println("  块数量: ${metric.blocksCount}")
    println("  下载图片: ${metric.imagesDownloaded}")
    println("  下载文件: ${metric.filesDownloaded}")
    println("  导出画板: ${metric.boardsExported}")
    println("  平均每块时间: ${metric.averageTimePerBlock}")
    println("  成功: ${if (metric.success) "是" else "否 - ${metric.errorMessage}"}")
}
```

### 导出指标说明

`ExportMetrics` 包含以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `documentId` | String | 文档 ID |
| `startTime` | Instant | 导出开始时间 |
| `endTime` | Instant | 导出结束时间 |
| `duration` | Duration | 导出总时长 |
| `blocksCount` | Int | 处理的块数量（包括 PAGE block） |
| `imagesDownloaded` | Int | 实际下载的图片数量 |
| `filesDownloaded` | Int | 实际下载的文件数量 |
| `boardsExported` | Int | 导出的画板数量 |
| `totalBytes` | Long | 总下载字节数（保留字段） |
| `success` | Boolean | 是否成功 |
| `errorMessage` | String? | 错误信息（如果失败） |
| `averageTimePerBlock` | Duration | 平均每块处理时间（计算属性） |

### 自定义指标收集器

你可以实现 `MetricsCollector` 接口来创建自定义的指标收集器，例如将指标写入数据库或发送到监控系统：

```kotlin
import dev.yidafu.feishu2html.metrics.MetricsCollector
import dev.yidafu.feishu2html.metrics.ExportMetrics
import kotlin.time.Duration

class DatabaseMetricsCollector(private val database: Database) : MetricsCollector {
    override fun recordExport(metrics: ExportMetrics) {
        // 将指标写入数据库
        database.insert("export_metrics", metrics)
    }
    
    override fun getMetrics(): List<ExportMetrics> {
        return database.query("SELECT * FROM export_metrics")
    }
    
    override fun getAverageExportTime(): Duration {
        return database.query("SELECT AVG(duration) FROM export_metrics")
    }
    
    override fun getSuccessRate(): Double {
        return database.query("SELECT AVG(CASE WHEN success THEN 1.0 ELSE 0.0 END) FROM export_metrics")
    }
    
    override fun clear() {
        database.execute("DELETE FROM export_metrics")
    }
}
```

---

## 性能优化建议

### 1. 调整并发下载数

根据网络条件和服务器能力调整 `maxConcurrentDownloads`：

```kotlin
val options = Feishu2HtmlOptions(
    // ...
    maxConcurrentDownloads = 15  // 默认 10
)
```

- **网络较好**: 15-20
- **网络一般**: 10（默认）
- **网络较差**: 5-8

### 2. 使用内联图片（小文档）

对于图片较少的小文档，可以启用内联图片模式：

```kotlin
val options = Feishu2HtmlOptions(
    // ...
    inlineImages = true
)
```

**注意**: 内联图片会增加 HTML 文件大小，不建议用于图片较多的文档。

### 3. 批量导出优化

批量导出时使用 `exportBatch` 方法：

```kotlin
val documentIds = listOf("doc1", "doc2", "doc3", ...)
converter.exportBatch(documentIds)
```

这比逐个调用 `export` 更有效率，因为可以复用 API 客户端连接。

### 4. 资源重用

避免为每个导出操作创建新的 `Feishu2Html` 实例：

```kotlin
// ❌ 不推荐
documentIds.forEach { docId ->
    val converter = Feishu2Html(options)
    converter.export(docId)
    converter.close()
}

// ✅ 推荐
val converter = Feishu2Html(options)
try {
    documentIds.forEach { docId ->
        converter.export(docId)
    }
} finally {
    converter.close()
}
```

### 5. 使用外部 CSS

启用外部 CSS 可以减少 HTML 文件大小：

```kotlin
val options = Feishu2HtmlOptions(
    // ...
    externalCss = true,  // 默认值
    cssFileName = "feishu-style-optimized.css"
)
```

### 6. 目录准备

在批量导出前，确保输出目录已经创建，避免重复的目录检查：

```kotlin
// 目录在 Feishu2HtmlOptions 初始化时自动创建
val options = Feishu2HtmlOptions(
    outputDir = "./output",
    imageDir = "./output/images",
    fileDir = "./output/files"
)
```

---

## 性能测试

### 运行基准测试

项目包含一套完整的性能基准测试，位于 `src/jvmTest/kotlin/dev/yidafu/feishu2html/benchmark/`。

运行基准测试：

```bash
./gradlew :jvmTest --tests "dev.yidafu.feishu2html.benchmark.PerformanceBenchmarkTest"
```

测试结果会输出到 `build/reports/performance/benchmark-results.txt`。

### 创建自定义性能测试

你可以创建自己的性能测试来验证特定场景的性能：

```kotlin
import dev.yidafu.feishu2html.Feishu2Html
import dev.yidafu.feishu2html.Feishu2HtmlOptions
import dev.yidafu.feishu2html.metrics.InMemoryMetricsCollector
import kotlin.system.measureTimeMillis

fun main() {
    val metricsCollector = InMemoryMetricsCollector()
    val options = Feishu2HtmlOptions(
        appId = "your_app_id",
        appSecret = "your_app_secret",
        outputDir = "./output",
        metricsCollector = metricsCollector
    )
    
    val converter = Feishu2Html(options)
    
    // 测量单个文档导出时间
    val time = measureTimeMillis {
        converter.export("your_document_id")
    }
    
    println("导出耗时: ${time}ms")
    
    // 查看详细指标
    val metrics = metricsCollector.getMetrics().first()
    println("块数量: ${metrics.blocksCount}")
    println("下载图片: ${metrics.imagesDownloaded}")
    println("下载文件: ${metrics.filesDownloaded}")
    
    converter.close()
}
```

---

## 真实环境性能因素

实际使用中，性能受以下因素影响：

### 网络因素

- **API 延迟**: 飞书 API 的响应时间
- **下载速度**: 图片和文件的下载速度
- **连接质量**: 网络稳定性和带宽

### 文档因素

- **文档大小**: 块数量和嵌套深度
- **资源数量**: 图片、文件、画板的数量
- **资源大小**: 单个资源的文件大小

### 系统因素

- **CPU 性能**: HTML 生成和图片编码
- **内存**: 并发下载和缓存
- **磁盘 I/O**: 文件写入速度

### 优化建议

1. **使用快速网络**: 稳定的高速网络连接
2. **合理并发**: 根据网络条件调整并发数
3. **系统资源**: 确保足够的内存和 CPU 资源
4. **批量处理**: 使用批量导出避免重复连接开销

---

## 故障排查

### 导出速度慢

1. **检查网络连接**: 确认网络延迟和带宽
2. **调整并发数**: 尝试增加 `maxConcurrentDownloads`
3. **查看指标**: 使用 `MetricsCollector` 分析瓶颈
4. **资源大小**: 检查是否有超大图片或文件

### 内存占用高

1. **减少并发**: 降低 `maxConcurrentDownloads`
2. **禁用内联图片**: 设置 `inlineImages = false`
3. **批量处理**: 分批处理大量文档
4. **及时关闭**: 确保调用 `close()` 释放资源

### API 限流

1. **降低并发**: 减少同时进行的下载数
2. **添加延迟**: 在批量导出时添加适当延迟
3. **重试机制**: 库已内置重试机制，会自动处理临时限流

---

## 相关文档

- [高级用法指南](advanced-usage.md) - 详细的使用示例
- [架构设计](ARCHITECTURE.md) - 性能监控架构说明
- [README](../README.md) - 项目概览和快速开始

---

## 反馈和建议

如果你在性能优化方面有任何建议或发现性能问题，欢迎提交 Issue 或 Pull Request。


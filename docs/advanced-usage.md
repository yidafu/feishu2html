# Advanced Usage Guide

This guide covers advanced features and best practices for using Feishu2HTML.

## Table of Contents

- [Progress Monitoring](#progress-monitoring)
- [Error Handling](#error-handling)
- [Configuration Options](#configuration-options)
- [Performance Optimization](#performance-optimization)
- [Dependency Injection](#dependency-injection)

---

## Progress Monitoring

### Using ExportProgressCallback

Monitor export progress in real-time:

```kotlin
import dev.yidafu.feishu2html.*

class MyProgressCallback : ExportProgressCallback {
    override fun onStart(documentId: String) {
        println("▶ Starting export: $documentId")
    }

    override fun onContentFetched(documentId: String, blocksCount: Int) {
        println("📄 Fetched $blocksCount blocks")
    }

    override fun onComplete(documentId: String, outputPath: String) {
        println("✅ Completed: $outputPath")
    }

    override fun onError(documentId: String, error: Throwable) {
        println("❌ Error in $documentId: ${error.message}")
    }
}

// Usage
val options = Feishu2HtmlOptions(
    appId = "your_app_id",
    appSecret = "your_app_secret"
)

Feishu2Html(options).use { converter ->
    val callback = MyProgressCallback()
    converter.export("doc_id", progressCallback = callback)
}
```

### Batch Export with Progress

```kotlin
val callback = object : ExportProgressCallback {
    override fun onComplete(documentId: String, outputPath: String) {
        println("✓ $documentId → $outputPath")
    }
}

converter.exportBatch(
    documentIds = listOf("doc1", "doc2", "doc3"),
    progressCallback = callback
)
```

---

## Error Handling

### Exception Types

Feishu2HTML provides fine-grained exception types:

```kotlin
import dev.yidafu.feishu2html.api.FeishuApiException

try {
    converter.export("doc_id")
} catch (e: FeishuApiException.DocumentNotFound) {
    println("Document not found: ${e.message}")
} catch (e: FeishuApiException.InsufficientPermission) {
    println("No permission: ${e.message}, code: ${e.code}")
} catch (e: FeishuApiException.RateLimitError) {
    println("Rate limited, retry after ${e.retryAfterSeconds} seconds")
} catch (e: FeishuApiException.AuthenticationError) {
    println("Authentication failed: ${e.message}")
} catch (e: FeishuApiException.NetworkError) {
    println("Network error: ${e.message}")
    e.cause?.printStackTrace()
} catch (e: FeishuApiException) {
    println("API error: ${e.message}, code: ${e.code}")
}
```

### Retry Mechanism

API calls automatically retry with exponential backoff:

- Network errors: Retried automatically
- Rate limit errors: Retried automatically
- Server errors (5xx): Retried automatically
- Auth errors: Not retried (permanent failures)
- Permission errors: Not retried (permanent failures)

The retry mechanism uses:
- Max retries: 3
- Initial delay: 1 second
- Max delay: 10 seconds
- Exponential factor: 2.0

---

## Configuration Options

### Complete Options Reference

```kotlin
val options = Feishu2HtmlOptions(
    // Required
    appId = "cli_a1234567890abcde",
    appSecret = "1234567890abcdef1234567890abcdef",
    
    // Output paths
    outputDir = "./output",
    imageDir = "./output/images",
    fileDir = "./output/files",
    imagePath = "images",  // Relative path in HTML
    filePath = "files",    // Relative path in HTML
    
    // CSS options
    externalCss = true,  // false = inline CSS
    cssFileName = "feishu-style-optimized.css",
    customCss = null,  // Custom CSS to append
    
    // Template mode
    templateMode = TemplateMode.DEFAULT,  // or FRAGMENT, FULL
    
    // Image handling
    inlineImages = false,  // true = base64 data URLs
    
    // Debug options
    showUnsupportedBlocks = true,
    enableDebugLogging = false,
    quietMode = false,  // true = suppress all non-error logs
    
    // Performance
    maxConcurrentDownloads = 10  // Control concurrency
)
```

### Template Modes

#### DEFAULT Mode
```kotlin
val options = Feishu2HtmlOptions(
    appId = "...",
    appSecret = "...",
    templateMode = TemplateMode.DEFAULT
)
// Produces complete HTML with Feishu styling
```

#### FRAGMENT Mode
```kotlin
val options = Feishu2HtmlOptions(
    appId = "...",
    appSecret = "...",
    templateMode = TemplateMode.FRAGMENT
)
// Produces minimal HTML fragment for embedding
```

#### FULL Mode
```kotlin
val options = Feishu2HtmlOptions(
    appId = "...",
    appSecret = "...",
    templateMode = TemplateMode.FULL
)
// Produces full HTML with basic structure
```

---

## Performance Optimization

### 1. Concurrent Downloads

Control the number of simultaneous downloads:

```kotlin
val options = Feishu2HtmlOptions(
    appId = "...",
    appSecret = "...",
    maxConcurrentDownloads = 20  // Increase for faster downloads
)
```

Considerations:
- Higher values = faster but more memory usage
- Lower values = slower but more stable
- Default (10) is balanced for most cases

### 2. Inline vs External CSS

```kotlin
// Faster generation (no CSS file write)
val options = Feishu2HtmlOptions(
    appId = "...",
    appSecret = "...",
    externalCss = false
)
```

### 3. Skip Existing Files

The library automatically skips downloading files that already exist:

```kotlin
// Second run will skip existing images and files
converter.export("doc_id")
```

### 4. Batch Export

More efficient than individual exports:

```kotlin
// Efficient: Reuses HTTP connections
converter.exportBatch(listOf("doc1", "doc2", "doc3"))

// Less efficient: Creates new connections each time
listOf("doc1", "doc2", "doc3").forEach { 
    Feishu2Html(options).use { it.export(it) } 
}
```

---

## Dependency Injection

For advanced testing scenarios:

```kotlin
import dev.yidafu.feishu2html.api.FeishuApiClient
import dev.yidafu.feishu2html.platform.PlatformFileSystem

// Create custom API client (for testing or proxying)
val customApiClient = FeishuApiClient("app_id", "app_secret")

// Create custom file system (for testing or cloud storage)
val customFileSystem = PlatformFileSystem()

// Inject dependencies
val converter = Feishu2Html(
    options = options,
    apiClient = customApiClient,
    fileSystem = customFileSystem
)
```

### Testing with Mocks

```kotlin
import io.mockk.*

class Feishu2HtmlTest {
    @Test
    fun testExport() {
        val mockApi = mockk<FeishuApiClient>()
        val mockFs = mockk<PlatformFileSystem>(relaxed = true)
        
        coEvery { mockApi.getDocumentInfo(any()) } returns DocumentInfo(...)
        every { mockFs.writeText(any(), any()) } just Runs
        
        val converter = Feishu2Html(options, mockApi, mockFs)
        converter.export("test_doc")
        
        coVerify { mockApi.getDocumentInfo("test_doc") }
    }
}
```

---

## Best Practices

### 1. Use `use` Block for Resource Management

```kotlin
// ✅ Good: Automatic cleanup
Feishu2Html(options).use { converter ->
    converter.export("doc_id")
}

// ❌ Bad: Manual cleanup required
val converter = Feishu2Html(options)
try {
    converter.export("doc_id")
} finally {
    converter.close()
}
```

### 2. Handle Errors Gracefully

```kotlin
converter.exportBatch(documentIds, object : ExportProgressCallback {
    override fun onError(documentId: String, error: Throwable) {
        // Log to your monitoring system
        logger.error("Failed: $documentId", error)
        
        // Notify user
        notifyUser("Document $documentId failed")
    }
})
```

### 3. Validate Configuration Early

```kotlin
// The library validates parameters at initialization
try {
    val options = Feishu2HtmlOptions(
        appId = "",  // ❌ Will throw IllegalArgumentException
        appSecret = "..."
    )
} catch (e: IllegalArgumentException) {
    println("Invalid configuration: ${e.message}")
}
```

### 4. Configure Logging Appropriately

```kotlin
// Production: Quiet mode
val prodOptions = Feishu2HtmlOptions(
    appId = "...",
    appSecret = "...",
    quietMode = true
)

// Development: Debug logging
val devOptions = Feishu2HtmlOptions(
    appId = "...",
    appSecret = "...",
    enableDebugLogging = true
)
```

---

## Common Scenarios

### Scenario 1: Offline-Ready HTML

```kotlin
val options = Feishu2HtmlOptions(
    appId = "...",
    appSecret = "...",
    inlineImages = true,  // Embed all images
    externalCss = false    // Inline CSS
)
```

Result: Single HTML file with everything embedded

### Scenario 2: Web Publishing

```kotlin
val options = Feishu2HtmlOptions(
    appId = "...",
    appSecret = "...",
    outputDir = "/var/www/html/docs",
    externalCss = true
)
```

Result: Clean HTML with external resources for web servers

### Scenario 3: Documentation Generation

```kotlin
val callback = object : ExportProgressCallback {
    override fun onComplete(documentId: String, outputPath: String) {
        // Generate index page
        // Update sitemap
        // Trigger rebuild
    }
}

converter.exportBatch(allDocumentIds, callback)
```

---

## Troubleshooting

### Problem: Slow Exports

**Solution**: Increase concurrent downloads

```kotlin
val options = options.copy(maxConcurrentDownloads = 20)
```

### Problem: Out of Memory

**Solution**: Decrease concurrent downloads

```kotlin
val options = options.copy(maxConcurrentDownloads = 5)
```

### Problem: Rate Limiting

**Solution**: The library handles this automatically with retries

Logs will show:
```
WARN - Rate limited, retrying in 1000ms
```

### Problem: Permission Errors

**Solution**: Check Feishu app permissions

```kotlin
try {
    converter.export("doc_id")
} catch (e: FeishuApiException.InsufficientPermission) {
    println("Grant permission in Feishu Open Platform")
    println("Error code: ${e.code}")
}
```

---

## Performance Monitoring

Feishu2Html provides built-in performance monitoring to track export operations and identify bottlenecks.

### Enable Metrics Collection

```kotlin
import dev.yidafu.feishu2html.Feishu2Html
import dev.yidafu.feishu2html.Feishu2HtmlOptions
import dev.yidafu.feishu2html.metrics.InMemoryMetricsCollector

// Create metrics collector
val metricsCollector = InMemoryMetricsCollector()

// Configure options with metrics collector
val options = Feishu2HtmlOptions(
    appId = "your_app_id",
    appSecret = "your_app_secret",
    outputDir = "./output",
    metricsCollector = metricsCollector
)

// Export documents
val converter = Feishu2Html(options)
converter.export("doc_id_1")
converter.export("doc_id_2")
converter.export("doc_id_3")
converter.close()

// View performance metrics
println("Average export time: ${metricsCollector.getAverageExportTime()}")
println("Success rate: ${metricsCollector.getSuccessRate()}")

// View detailed metrics for each export
metricsCollector.getMetrics().forEach { metric ->
    println("Document ${metric.documentId}:")
    println("  Duration: ${metric.duration}")
    println("  Blocks: ${metric.blocksCount}")
    println("  Images downloaded: ${metric.imagesDownloaded}")
    println("  Files downloaded: ${metric.filesDownloaded}")
    println("  Boards exported: ${metric.boardsExported}")
    println("  Success: ${metric.success}")
}
```

### Metrics Data

Each export operation records the following metrics:

- **documentId**: Document ID
- **startTime**: Export start timestamp
- **endTime**: Export end timestamp
- **duration**: Total export duration
- **blocksCount**: Number of blocks processed
- **imagesDownloaded**: Number of images actually downloaded
- **filesDownloaded**: Number of files actually downloaded
- **boardsExported**: Number of boards exported
- **success**: Whether the export succeeded
- **errorMessage**: Error message if failed
- **averageTimePerBlock**: Average processing time per block (computed property)

### Custom Metrics Collector

You can implement the `MetricsCollector` interface to create custom collectors:

```kotlin
import dev.yidafu.feishu2html.metrics.MetricsCollector
import dev.yidafu.feishu2html.metrics.ExportMetrics
import kotlin.time.Duration

class DatabaseMetricsCollector : MetricsCollector {
    override fun recordExport(metrics: ExportMetrics) {
        // Store metrics in database
    }
    
    override fun getMetrics(): List<ExportMetrics> {
        // Retrieve metrics from database
        return emptyList()
    }
    
    override fun getAverageExportTime(): Duration {
        // Calculate average from database
        return Duration.ZERO
    }
    
    override fun getSuccessRate(): Double {
        // Calculate success rate from database
        return 0.0
    }
    
    override fun clear() {
        // Clear metrics from database
    }
}
```

For more details, see [Performance Guide](PERFORMANCE.md).

---

## Related Documentation

- [README.md](../README.md) - Quick start and installation
- [JVM Usage](jvm-usage.md) - JVM-specific guide
- [Node.js Usage](nodejs-usage.md) - JavaScript/Node.js guide
- [Performance Guide](PERFORMANCE.md) - Performance optimization and monitoring
- [API Documentation](https://yidafu.github.io/feishu2html/) - Complete API reference


# JVM Usage Guide

## Overview

Feishu2HTML provides full-featured JVM support with both CLI tool and library usage. The JVM platform is the most mature and recommended for production use.

## Prerequisites

- **JDK**: 17 or higher
- **Gradle**: 8.0+ (or use the included Gradle wrapper)

## CLI Usage

### Building the Project

```bash
# Clone the repository
git clone https://github.com/yidafu/feishu2html.git
cd feishu2html

# Build the project
./gradlew build
```

### Basic Commands

```bash
# Export a single document
./gradlew runJvm --args="<app_id> <app_secret> <document_id>"

# Export multiple documents
./gradlew runJvm --args="<app_id> <app_secret> <doc_id_1> <doc_id_2> <doc_id_3>"

# With options for standalone HTML
./gradlew runJvm --args="--inline-images --inline-css <app_id> <app_secret> <document_id>"

# Clean output (hide unsupported warnings)
./gradlew runJvm --args="--hide-unsupported <app_id> <app_secret> <document_id>"

# All options combined
./gradlew runJvm --args="-t fragment --inline-images --inline-css --hide-unsupported <app_id> <app_secret> <document_id>"
```

### CLI Options

```
Options:
  -t, --template <mode>   HTML template mode: default | fragment | full
  --inline-images         Embed images as base64 data URLs (for standalone HTML)
  --inline-css            Embed CSS styles inline in <style> tag (for standalone HTML)
  --hide-unsupported      Hide unsupported block type warnings (for cleaner output)
  -h, --help              Show help message
```

### Examples

```bash
# Basic export
./gradlew runJvm --args="cli_a1234567890abcde your_app_secret_here doxcnABC123XYZ456"

# Generate standalone HTML file (single file with everything embedded)
./gradlew runJvm --args="--inline-images --inline-css cli_a123 secret123 doxcnABC"

# Clean export without warnings
./gradlew runJvm --args="--hide-unsupported cli_a123 secret123 doxcnABC"
```

Output files will be saved to the `./output/` directory by default.

## Library Usage

### Add to Your Project

#### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("dev.yidafu.feishu2html:feishu2html-jvm:1.0.0")
}
```

#### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'dev.yidafu.feishu2html:feishu2html-jvm:1.0.0'
}
```

#### Maven

```xml
<dependency>
    <groupId>dev.yidafu.feishu2html</groupId>
    <artifactId>feishu2html-jvm</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Example

```kotlin
import dev.yidafu.feishu2html.Feishu2Html
import dev.yidafu.feishu2html.Feishu2HtmlOptions
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val options = Feishu2HtmlOptions(
        appId = "your_app_id",
        appSecret = "your_app_secret"
    )

    // Use .use {} for automatic resource management
    Feishu2Html(options).use { converter ->
        converter.export("doxcnABC123XYZ456")
        println("Export completed successfully!")
    }
}
```

### Advanced Example with Custom Options

```kotlin
import dev.yidafu.feishu2html.Feishu2Html
import dev.yidafu.feishu2html.Feishu2HtmlOptions
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val options = Feishu2HtmlOptions(
        appId = "your_app_id",
        appSecret = "your_app_secret",
        outputDir = "./output",          // HTML output directory
        imageDir = "./output/images",    // Image save directory
        fileDir = "./output/files",      // Attachment save directory
        imagePath = "images",            // Relative path for images in HTML
        filePath = "files",              // Relative path for files in HTML
        customCss = null                 // Custom CSS (optional)
    )

    Feishu2Html(options).use { converter ->
        // Batch export multiple documents
        val documentIds = listOf(
            "doxcnABC123XYZ456",
            "doxcnDEF789GHI012",
            "doxcnJKL345MNO678"
        )
        
        converter.exportBatch(documentIds)
        println("Batch export completed!")
    }
}
```

### Batch Export Example

```kotlin
import dev.yidafu.feishu2html.Feishu2Html
import dev.yidafu.feishu2html.Feishu2HtmlOptions
import kotlinx.coroutines.runBlocking
import java.io.File

fun main() = runBlocking {
    val options = Feishu2HtmlOptions(
        appId = System.getenv("FEISHU_APP_ID"),
        appSecret = System.getenv("FEISHU_APP_SECRET"),
        outputDir = "./exports"
    )

    val documentIds = File("document-list.txt")
        .readLines()
        .filter { it.isNotBlank() }

    Feishu2Html(options).use { converter ->
        converter.exportBatch(documentIds)
    }
}
```

## CSS Styling Options

### Using Official Feishu Styles (Default)

```kotlin
val options = Feishu2HtmlOptions(
    appId = "your_app_id",
    appSecret = "your_app_secret",
    externalCss = true,                        // Use external CSS file (default)
    cssFileName = "feishu-style-optimized.css" // Optimized CSS (16KB)
)

Feishu2Html(options).use { converter ->
    converter.export("document_id")
}
```

**Output:**
- `document.html` (with `<link>` to CSS)
- `feishu-style-optimized.css` (optimized styles, only 16KB!)

### Inline CSS Mode

For single-file portability:

```kotlin
val options = Feishu2HtmlOptions(
    appId = "your_app_id",
    appSecret = "your_app_secret",
    externalCss = false  // Embed CSS in <style> tag
)
```

### Custom CSS

```kotlin
val customCss = """
    .protyle-wysiwyg { 
        font-family: "Inter", sans-serif;
        background-color: #f8f9fa;
    }
    .heading-h1 { 
        color: #2c3e50;
        border-bottom: 3px solid #3498db;
    }
""".trimIndent()

val options = Feishu2HtmlOptions(
    appId = "your_app_id",
    appSecret = "your_app_secret",
    externalCss = false,  // Must use inline mode for custom CSS
    customCss = customCss
)
```

## Configuration Options

### Feishu2HtmlOptions

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `appId` | String | - | Feishu App ID (required) |
| `appSecret` | String | - | Feishu App Secret (required) |
| `outputDir` | String | "./output" | HTML output directory |
| `imageDir` | String | "./output/images" | Image save directory |
| `fileDir` | String | "./output/files" | Attachment save directory |
| `imagePath` | String | "images" | Relative path for images in HTML |
| `filePath` | String | "files" | Relative path for files in HTML |
| `externalCss` | Boolean | true | Use external CSS file |
| `cssFileName` | String | "feishu-style-optimized.css" | CSS filename |
| `customCss` | String? | null | Custom CSS content |

## Build Commands

```bash
# Compile JVM target
./gradlew compileKotlinJvm

# Build JAR
./gradlew jvmJar

# Run tests
./gradlew jvmTest

# Full build (all platforms)
./gradlew build
```

## Logging

The JVM platform uses Logback for logging. Configure logging by creating `src/jvmMain/resources/logback.xml`:

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

## Error Handling

```kotlin
import dev.yidafu.feishu2html.Feishu2Html
import dev.yidafu.feishu2html.Feishu2HtmlOptions
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val options = Feishu2HtmlOptions(
        appId = "your_app_id",
        appSecret = "your_app_secret"
    )

    try {
        Feishu2Html(options).use { converter ->
            converter.export("document_id")
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
}
```

## Testing

### Unit Tests

```kotlin
import kotlin.test.Test
import kotlin.test.assertNotNull
import dev.yidafu.feishu2html.Feishu2HtmlOptions

class Feishu2HtmlTest {
    @Test
    fun testOptionsCreation() {
        val options = Feishu2HtmlOptions(
            appId = "test_app_id",
            appSecret = "test_secret"
        )
        assertNotNull(options)
    }
}
```

### Running Tests

```bash
./gradlew jvmTest
```

## Performance Tips

1. **Batch Export**: Use `exportBatch()` for multiple documents to reuse HTTP connections
2. **Parallel Processing**: Consider using coroutines for concurrent exports
3. **Resource Cleanup**: Always use `.use {}` for proper resource management
4. **External CSS**: Use external CSS files for better caching when exporting multiple documents

## Troubleshooting

### OutOfMemoryError

Increase JVM heap size:
```bash
./gradlew run --args="..." -Dorg.gradle.jvmargs="-Xmx2g"
```

### SSL Certificate Issues

Add certificate to JVM truststore or disable SSL verification (not recommended for production):
```kotlin
// Not recommended for production
System.setProperty("jdk.tls.client.protocols", "TLSv1.2")
```

### File Permission Issues

Ensure the output directory is writable:
```kotlin
val outputDir = File("./output")
if (!outputDir.exists()) {
    outputDir.mkdirs()
}
```

## Features

✅ **Full Feature Set**: All features available on JVM  
✅ **CLI Tool**: Command-line interface for easy usage  
✅ **Production Ready**: Stable and tested  
✅ **Logging**: Logback integration  
✅ **Type Safety**: Kotlin compile-time type checking  
✅ **Resource Management**: AutoCloseable support  
✅ **Performance**: Optimized for large documents

## See Also

- [Node.js Usage Guide](./nodejs-usage.md)
- [API Documentation](https://yidafu.github.io/feishu2html/)
- [Contributing Guide](../CONTRIBUTING.md)


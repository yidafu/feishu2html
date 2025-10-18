# Feishu2Md

A Kotlin library and CLI tool to convert Feishu (Lark) documents to HTML.

## Features

- ✅ Support for all Feishu document block types (headings, paragraphs, lists, tables, code blocks, etc.)
- ✅ Automatic download and save of images and attachments
- ✅ Support for text styles (bold, italic, underline, strikethrough, links, etc.)
- ✅ Mathematical formulas rendering (via MathJax)
- ✅ Code syntax highlighting (70+ languages)
- ✅ Modular design - use as library or CLI tool
- ✅ Asynchronous resource downloading
- ✅ Type-safe HTML generation using kotlinx.html DSL
- ✅ Elegant Renderer delegation pattern

## Supported Block Types

| Block Type | Type Code | Support Status | Notes |
|------------|-----------|----------------|-------|
| Page | 1 | ✅ Full | - |
| Text | 2 | ✅ Full | - |
| Heading 1-9 | 3-11 | ✅ Full | All 9 levels supported |
| Bullet List | 12 | ✅ Full | - |
| Ordered List | 13 | ✅ Full | - |
| Code Block | 14 | ✅ Full | 70+ languages |
| Quote | 15 | ✅ Full | - |
| Equation | 16 | ✅ Full | MathJax rendering |
| Todo | 17 | ✅ Full | - |
| Bitable | 18 | ⚠️ Partial | Placeholder only |
| Callout | 19 | ✅ Full | - |
| Chat Card | 20 | ⚠️ Partial | Placeholder only |
| Diagram | 21 | ✅ Full | - |
| Divider | 22 | ✅ Full | - |
| File | 23 | ✅ Full | - |
| Grid | 24 | ✅ Full | Column layout |
| Grid Column | 25 | ✅ Full | - |
| Iframe | 26 | ✅ Full | Multiple embed types |
| Image | 27 | ✅ Full | - |
| ISV | 28 | ❌ Unsupported | - |
| Mindnote | 29 | ❌ Unsupported | - |
| Sheet | 30 | ❌ Unsupported | - |
| Table | 31 | ✅ Full | - |
| Table Cell | 32 | ✅ Full | - |
| View | 33 | ❌ Unsupported | - |
| Quote Container | 34 | ✅ Full | - |
| Task | 35 | ❌ Unsupported | - |
| OKR | 36 | ❌ Unsupported | - |
| OKR Objective | 37 | ❌ Unsupported | - |
| OKR Key Result | 38 | ❌ Unsupported | - |
| OKR Progress | 39 | ❌ Unsupported | - |
| Add-ons | 40 | ❌ Unsupported | Plugin components |
| Jira Issue | 41 | ❌ Unsupported | - |
| Wiki Catalog | 42 | ❌ Unsupported | Legacy wiki subpage list |
| Board | 43 | ✅ Full | Electronic whiteboard |
| Agenda | 44 | ❌ Unsupported | - |
| Agenda Item | 45 | ❌ Unsupported | - |
| Agenda Item Title | 46 | ❌ Unsupported | - |
| Agenda Item Content | 47 | ❌ Unsupported | - |
| Link Preview | 48 | ❌ Unsupported | - |
| Source Synced | 49 | ❌ Unsupported | - |
| Reference Synced | 50 | ❌ Unsupported | - |
| Sub Page List | 51 | ❌ Unsupported | Wiki subpage list (new) |
| AI Template | 52 | ❌ Unsupported | - |


## Quick Start

### Prerequisites

- JDK 17 or higher
- Gradle 8.0 or higher (or use the included gradlew wrapper)

### 1. Get Feishu App Credentials

1. Visit [Feishu Open Platform](https://open.feishu.cn/app)
2. Create a self-built app
3. Get your `App ID` and `App Secret`
4. Add the following permissions to your app:
   - `docx:document` - View, comment, and export documents
   - `drive:drive` - View and download files in cloud storage

### 2. Grant Document Access

**Important**: You must grant your app access to the documents you want to export:

1. Open the Feishu document you want to export
2. Click the "Share" button in the top-right corner
3. Add your app/bot to the document collaborators
4. Grant at least "View" permission

**⚠️ Limitation**: If your document contains embedded/referenced external documents (e.g., via links or iframe blocks), you must also grant your app access to those external documents. Otherwise, the content may fail to load or appear as broken links.

### 3. CLI Usage

```bash
# Clone the repository
git clone <repository-url>
cd feishu2html

# Export a single document
./gradlew run --args="<app_id> <app_secret> <document_id>"

# Export multiple documents
./gradlew run --args="<app_id> <app_secret> <doc_id_1> <doc_id_2> <doc_id_3>"

# Example
./gradlew run --args="cli_a8790687b4bdd01c 3sxXNpzmX4ErVg07gNMOgdMkQn2usPgq TPDddjY5foJZ8axlf9fctf2Wnse"
```

### 4. Library Usage

#### Add to Your Project

In your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("dev.yidafu.feishu2md:feishu2md:1.0.0")
}
```

#### Code Example

```kotlin
import dev.yidafu.feishu2md.Feishu2Html
import dev.yidafu.feishu2md.Feishu2HtmlOptions
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // Configure options
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

    val feishu2Html = Feishu2Html(options)

    try {
        // Export a single document
        feishu2Html.export("document_id_here")

        // Batch export
        feishu2Html.exportBatch(listOf(
            "document_id_1",
            "document_id_2",
            "document_id_3"
        ))
    } finally {
        feishu2Html.close()
    }
}
```

### 5. Custom CSS Styling

```kotlin
val customCss = """
    body {
        font-family: "Custom Font", sans-serif;
        background-color: #f5f5f5;
    }
    
    h1 {
        color: #2c3e50;
    }
    
    /* More custom styles... */
"""

val options = Feishu2HtmlOptions(
    appId = "your_app_id",
    appSecret = "your_app_secret",
    customCss = customCss
)
```



## Getting Document ID

The document ID can be extracted from the Feishu document URL:

```
https://example.feishu.cn/docx/abc123xyz456
                              ↑
                         This is the document_id
```

## Troubleshooting

### 1. Token Acquisition Failure

**Error**: `Failed to get token: app access token invalid`

**Solutions**:
- Verify `app_id` and `app_secret` are correct
- Ensure the app is enabled and published
- Check app permissions are configured

### 2. Document Access Failure

**Error**: `Failed to get document content: no permission`

**Solutions**:
- Confirm app has required permissions
- Ensure document is accessible to the app
- Try adding the app to document collaborators

### 3. Image Download Failure

**Solutions**:
- Check network connection
- Confirm app has `drive:drive` permission
- Some legacy documents may have API limitations

## Known Limitations

### 1. External Document References

When a Feishu document contains references to other documents (e.g., embedded documents, links to other docs), you must grant your app access to **all referenced documents** as well. The tool cannot automatically propagate permissions.

**Workaround**: Manually share each referenced document with your app before exporting.

### 2. Real-time Collaboration Content

Content from real-time collaboration features (e.g., comments, suggestions) is not included in the export.

### 3. Unsupported Block Types

The following block types are currently not supported and will display a placeholder message:

**Confirmed in API but not yet supported:**
- ISV (Type 28) - Third-party integrations
- Mindnote (Type 29) - Mind maps
- Sheet (Type 30) - Spreadsheet blocks
- View (Type 33) - Database views
- Task (Type 35) - Task blocks
- OKR (Type 36) - OKR blocks
- OKR Objective (Type 37) - OKR objectives
- OKR Key Result (Type 38) - OKR key results
- OKR Progress (Type 39) - OKR progress tracking
- Add-ons (Type 40) - Plugin/Extension components (observed in documents)
- Jira Issue (Type 41) - Jira issue integration
- Wiki Catalog (Type 42) - Legacy wiki subpage list
- Agenda (Type 44) - Meeting agenda
- Agenda Item (Type 45) - Agenda items
- Agenda Item Title (Type 46) - Agenda item titles
- Agenda Item Content (Type 47) - Agenda item content
- Link Preview (Type 48) - Link preview cards
- Source Synced (Type 49) - Source synchronization blocks
- Reference Synced (Type 50) - Reference synchronization blocks
- Sub Page List (Type 51) - Wiki subpage list (new version)
- AI Template (Type 52) - AI template blocks

These blocks will be rendered with a placeholder: `[暂不支持的Block类型: XXX]`

### 4. API Rate Limiting

The Feishu API has rate limits. The tool includes built-in rate limiting (QPS=2) to avoid exceeding limits, but very large documents may take time to process.


## API Documentation

Full API documentation is available after building:

```bash
# Generate HTML documentation
./gradlew dokkaHtml

# Open the documentation
open build/dokka/html/index.html
```

Documentation will be generated in `build/dokka/html/`.

## References

- [Feishu Open Platform Docs](https://open.feishu.cn/document/home/index)
- [Document Block API](https://open.feishu.cn/document/server-docs/docs/docs/docx-v1/document/list)
- [Block Type Reference](https://open.feishu.cn/document/docs/docs/data-structure/block)
- [API Documentation](build/dokka/html/index.html) (generate with `./gradlew dokkaHtml`)

## License

MIT License - see [LICENSE](LICENSE) file for details.

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on:
- Project architecture and structure
- How to add support for new block types
- Coding standards and best practices
- Pull request process

Issues and Pull Requests are welcome!

## Acknowledgments

This project was inspired by [feishu2md](https://github.com/S-TE11A/feishu2md).

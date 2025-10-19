# Module feishu2md

Feishu2Md is a Kotlin library and CLI tool for converting Feishu (Lark) documents to HTML format.

## Overview

This module provides a complete solution for exporting Feishu cloud documents to standalone HTML files, with automatic downloading of images and attachments.

## Key Components

### API Layer (`dev.yidafu.feishu2html.api`)

- **FeishuApiClient**: Main API client for interacting with Feishu Open Platform
- **FeishuAuthService**: Handles authentication and token management
- **RateLimiter**: Prevents API rate limit violations

### Data Models (`dev.yidafu.feishu2html.api.model`)

- **Block**: Sealed class hierarchy representing all 52 Feishu document block types
- **Document**: Document metadata and structure
- **TextElement**: Rich text elements with styling
- Various enums for colors, languages, alignments, etc.

### Converter Layer (`dev.yidafu.feishu2html.converter`)

- **HtmlBuilder**: Orchestrates HTML document generation
- **Renderable**: Interface for block renderers
- **TextElementConverter**: Converts text elements to HTML
- **FeishuStyles**: Default CSS styles matching Feishu's visual design
- **renderers/**: Individual renderer objects for each block type

## Architecture

The project uses a **Renderable + Delegate** pattern:

1. Each `Block` type is a pure data class
2. Each `Renderer` is a stateless singleton object
3. The `renderBlock()` function dispatches blocks to appropriate renderers
4. Renderers use kotlinx.html DSL for type-safe HTML generation

This design provides:
- Separation of concerns (data vs. rendering logic)
- Easy extensibility (add new block types)
- Type safety (sealed classes + smart casts)
- Testability (stateless renderers)

## Usage Example

```kotlin
import dev.yidafu.feishu2html.Feishu2Html
import dev.yidafu.feishu2html.Feishu2HtmlOptions
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val options = Feishu2HtmlOptions(
        appId = "your_app_id",
        appSecret = "your_app_secret",
        outputDir = "./output"
    )
    
    val converter = Feishu2Html(options)
    try {
        converter.export("document_id")
    } finally {
        converter.close()
    }
}
```

## Package Structure

- `dev.yidafu.feishu2html` - Main classes (Feishu2Html, Feishu2HtmlOptions)
- `dev.yidafu.feishu2html.api` - Feishu API integration
- `dev.yidafu.feishu2html.api.model` - Data models
- `dev.yidafu.feishu2html.converter` - HTML conversion logic
- `dev.yidafu.feishu2html.converter.renderers` - Block renderers

## Dependencies

- Kotlin 2.1.0
- kotlinx.html 0.11.0
- kotlinx.serialization 1.6.2
- kotlinx.coroutines 1.7.3
- Ktor Client 2.3.7

## License

MIT License


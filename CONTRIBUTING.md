# Contributing to Feishu2HTML

Thank you for your interest in contributing to Feishu2HTML! This document provides guidelines and technical details for contributors.

## 📋 Table of Contents

- [Project Structure](#project-structure)
- [Multiplatform Architecture](#multiplatform-architecture)
- [Architecture Highlights](#architecture-highlights)
- [Supported Features](#supported-features)
- [Tech Stack](#tech-stack)
- [Building the Project](#building-the-project)
- [Platform-Specific Development](#platform-specific-development)
- [Generating Documentation](#generating-documentation)
- [API Reference](#api-reference)
- [Adding Support for New Block Types](#adding-support-for-new-block-types)
- [Testing](#testing)
- [Coding Guidelines](#coding-guidelines)
- [Troubleshooting Development Issues](#troubleshooting-development-issues)
- [Pull Request Process](#pull-request-process)

---

## Project Structure

This is a **Kotlin Multiplatform** project supporting JVM, JS (Node.js), and Native platforms.

```
feishu2html/
├── src/
│   ├── commonMain/kotlin/dev/yidafu/feishu2html/  # Shared code (95.5%)
│   │   ├── Feishu2Html.kt              # Main export class
│   │   ├── api/                         # Feishu API layer
│   │   │   ├── FeishuApiClient.kt      # API client
│   │   │   ├── FeishuAuthService.kt    # Authentication service
│   │   │   ├── RateLimiter.kt          # Rate limiting
│   │   │   └── model/                   # Data models (30+ files)
│   │   │       ├── Block.kt            # Block base class & serializer
│   │   │       ├── TextBlocks.kt       # Page, Text blocks
│   │   │       ├── HeadingBlocks.kt    # Heading1-9 blocks
│   │   │       ├── ListBlocks.kt       # Bullet, Ordered blocks
│   │   │       ├── ContentBlocks.kt    # Code, Quote, Equation, etc.
│   │   │       ├── MediaBlocks.kt      # Image, File, Board, etc.
│   │   │       ├── ContainerBlocks.kt  # Callout, Grid, Table, etc.
│   │   │       └── ... (more models)
│   │   ├── converter/                   # HTML converter
│   │   │   ├── HtmlBuilder.kt          # HTML builder
│   │   │   ├── TextElementConverter.kt # Text element converter
│   │   │   ├── FeishuStyles.kt         # Feishu styles
│   │   │   ├── EmbeddedResources.kt    # Embedded CSS (16KB)
│   │   │   ├── Renderable.kt           # Renderer interface
│   │   │   └── renderers/              # Block renderers (13 files)
│   │   └── platform/                    # Platform abstractions (expect)
│   │       ├── HttpClientFactory.kt    # HTTP client factory
│   │       ├── FileSystem.kt           # File system abstraction
│   │       └── UrlUtils.kt             # URL utilities
│   │
│   ├── jvmMain/kotlin/dev/yidafu/feishu2html/  # JVM-specific (3%)
│   │   ├── Main.kt                     # CLI tool (JVM only)
│   │   └── platform/                   # JVM actual implementations
│   │       ├── HttpClientFactory.kt    # CIO engine
│   │       ├── FileSystem.kt           # java.io.File
│   │       └── UrlUtils.kt             # URLDecoder
│   │
│   ├── jsMain/kotlin/dev/yidafu/feishu2html/platform/  # JS-specific
│   │   ├── HttpClientFactory.kt        # Js engine
│   │   ├── FileSystem.kt               # Node.js fs module
│   │   └── UrlUtils.kt                 # decodeURIComponent
│   │
│   ├── nativeMain/kotlin/dev/yidafu/feishu2html/platform/  # Native shared
│   │   ├── FileSystem.kt               # POSIX API
│   │   └── UrlUtils.kt                 # Basic URL decode
│   │
│   ├── darwinMain/kotlin/dev/yidafu/feishu2html/platform/  # macOS/iOS
│   │   └── HttpClientFactory.kt        # Darwin engine
│   │
│   ├── desktopNativeMain/kotlin/dev/yidafu/feishu2html/platform/  # Linux/Windows
│   │   └── HttpClientFactory.kt        # Curl engine
│   │
```

**Key Points:**
- **95.5% code sharing**: Most code is in `commonMain`, shared across all platforms
- **Platform-specific implementations**: Only essential platform differences in separate source sets
- **expect/actual mechanism**: Clean abstraction for platform-specific functionality

---

## Multiplatform Architecture

### Platform Support

Feishu2HTML supports multiple platforms through Kotlin Multiplatform:

| Platform | Status | Source Set | Features |
|----------|--------|------------|----------|
| JVM | 🟢 Production Ready | jvmMain | CLI + Library |
| JS (Node.js) | 🟢 Fully Supported | jsMain | Library |
| macOS | 🟡 Experimental | darwinMain | Library |
| Linux | 🟡 Experimental | desktopNativeMain | Library |
| Windows | 🟡 Experimental | desktopNativeMain | Library |
| iOS | 🟡 Experimental | iosMain | Library |
| Android Native | 🟡 Experimental | androidNativeMain | Library |

### Source Set Hierarchy

```
commonMain (95.5% shared code)
    ↓
├── jvmMain (JVM-specific: CLI tool)
├── jsMain (JS-specific: Node.js fs integration)
└── nativeMain (Native shared)
    ├── darwinMain (macOS + iOS: Darwin HTTP engine)
    ├── desktopNativeMain (Linux + Windows: Curl engine)
    └── androidNativeMain (Android Native: Curl engine)
```

### Platform Abstractions (expect/actual)

#### 1. HTTP Client Factory
```kotlin
// commonMain
expect fun createHttpClient(): HttpClient

// Implementations
// jvmMain: CIO engine
// jsMain: Js engine
// darwinMain: Darwin engine (macOS/iOS)
// desktopNativeMain: Curl engine (Linux/Windows)
```

#### 2. File System
```kotlin
// commonMain
expect class PlatformFileSystem {
    fun createDirectories(path: String)
    fun exists(path: String): Boolean
    fun writeText(path: String, content: String)
    fun writeBytes(path: String, content: ByteArray)
}

// Implementations
// jvmMain: java.io.File
// jsMain: Node.js fs module
// nativeMain: POSIX API
```

#### 3. URL Utilities
```kotlin
// commonMain
expect fun decodeUrl(url: String): String

// Implementations
// jvmMain: URLDecoder
// jsMain: decodeURIComponent
// nativeMain: basic string replacement
```

### Embedded Resources

CSS resources are embedded directly as Kotlin code constants:

```kotlin
// commonMain/converter/EmbeddedResources.kt
object EmbeddedResources {
    val FEISHU_STYLE_CSS = """
        /* 16KB optimized CSS */
    """.trimIndent()
}
```

**Benefits:**
- ✅ Fully cross-platform
- ✅ No external resource libraries needed
- ✅ Compile-time validation
- ✅ No runtime I/O overhead

---

## Architecture Highlights

### Overall Architecture

Feishu2HTML uses a **three-layer architecture + Renderable delegate pattern** in a **Kotlin Multiplatform** setup:

```
API Layer (Feishu API interaction) - commonMain
  ↓
Converter Layer (HTML transformation) - commonMain
  ↓
Renderer Layer (Block-specific rendering) - commonMain
  ↓
Platform Layer (expect/actual) - platform-specific
```

### Core Design Patterns

#### 1. Sealed Class

`Block` uses sealed class for type-safe block hierarchy:

```kotlin
sealed class Block {
    abstract val blockId: String
    abstract val blockType: BlockType
    // ...
}

data class TextBlock(...) : Block()
data class ImageBlock(...) : Block()
// ...
```

#### 2. Renderable + Delegate Pattern

Each Block type has its own dedicated Renderer object:

```kotlin
// Renderable interface
interface Renderable {
    fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext
    )
}

// Renderer implementation (singleton)
object TextBlockRenderer : Renderable {
    override fun <T> render(...) {
        val textBlock = block as TextBlock
        val elements = textBlock.text?.elements ?: return
        parent.p {
            context.textConverter.convertElements(elements, this)
        }
    }
}

// Global dispatcher function in HtmlBuilder
fun renderBlock(block: Block, parent: FlowContent, allBlocks: Map<String, Block>, context: RenderContext) {
    when (block) {
        is TextBlock -> TextBlockRenderer.render(parent, block, allBlocks, context)
        is ImageBlock -> ImageBlockRenderer.render(parent, block, allBlocks, context)
        // ... other block types
    }
}
```

**Benefits**:
- **Separation of concerns**: Data (Block) and rendering logic (Renderer) are separated
- **High cohesion**: Each renderer focuses on one block type
- **Easy to extend**: Add new block types in 3 steps:
  1. Add BlockType enum value
  2. Create Block data class
  3. Create corresponding Renderer object
- **Easy to maintain**: Rendering logic separated into individual files (one renderer per block type)
- **Type-safe**: Sealed class ensures all block types are handled
- **Testable**: Test each renderer independently

#### 3. kotlinx.html DSL

Type-safe HTML generation using Kotlin DSL:

```kotlin
parent.div("callout callout-blue") {
    span(classes = "callout-emoji") { +"💡" }
    p { 
        +"Highlighted content with "
        strong { +"bold text" }
    }
}
```

**Advantages over string concatenation**:
- Compile-time safety
- IDE autocomplete support
- Impossible to generate malformed HTML
- Clean and readable code

#### 4. Polymorphic Serialization

Custom polymorphic serialization using `kotlinx.serialization`:

```kotlin
object BlockSerializer : JsonContentPolymorphicSerializer<Block>(Block::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Block> {
        val typeCode = element.jsonObject["block_type"]?.jsonPrimitive?.int
        val blockType = BlockType.fromCode(typeCode)
        
        return when (blockType) {
            BlockType.TEXT -> TextBlock.serializer()
            BlockType.IMAGE -> ImageBlock.serializer()
            // ... dispatch to correct Block subclass
        }
    }
}
```

---

## Supported Features

### Text Styling

The project supports all Feishu text styling features:

- ✅ Bold
- ✅ Italic
- ✅ Underline
- ✅ Strikethrough
- ✅ Inline code
- ✅ Hyperlinks
- ✅ Text color
- ✅ Background color
- ✅ @Mention users
- ✅ @Mention documents
- ✅ Inline equations

These are handled by `TextElementConverter.kt`.

---

## Tech Stack

### Multiplatform Core Dependencies

| Dependency | Version | Purpose | Platforms |
|------------|---------|---------|-----------|
| Kotlin | 2.1.0 | Main language | All |
| kotlinx-serialization-json | 1.6.3 | JSON serialization | All |
| kotlinx-html | 0.11.0 | Type-safe HTML DSL | All |
| kotlinx-coroutines-core | 1.8.0 | Coroutines | All |
| kotlinx-datetime | 0.5.0 | Date/time handling | All |
| kotlin-logging | 6.0.3 | Multiplatform logging | All |
| Ktor Client Core | 2.3.7 | Async HTTP client | All |

### Platform-Specific Dependencies

| Dependency | Version | Purpose | Platform |
|------------|---------|---------|----------|
| ktor-client-cio | 2.3.7 | HTTP engine | JVM |
| ktor-client-js | 2.3.7 | HTTP engine | JS |
| ktor-client-darwin | 2.3.7 | HTTP engine | macOS/iOS |
| ktor-client-curl | 2.3.7 | HTTP engine | Linux/Windows/Android |
| logback-classic | 1.4.14 | Logging implementation | JVM |

### Test Dependencies

| Dependency | Version | Purpose | Platform |
|------------|---------|---------|----------|
| kotlin-test | - | Test framework | All |
| kotlinx-coroutines-test | 1.8.0 | Coroutine testing | All |
| ktor-client-mock | 2.3.7 | HTTP mocking | All |
| Kotest | 5.8.0 | BDD-style testing | JVM |
| MockK | 1.13.9 | Mocking | JVM |
| JaCoCo | 0.8.11 | Code coverage | JVM |

### Development Tools

| Tool | Version | Purpose |
|------|---------|---------|
| Dokka | 1.9.20 | API documentation generator |
| ktlint | 12.1.0 | Kotlin linter & formatter |
| Gradle | 8.5 | Build tool |

---

## Building the Project

```bash
# Clone repository
git clone <repository-url>
cd feishu2md

# Compile the project
./gradlew build

# Run tests
./gradlew test

# Generate coverage report
./gradlew testCoverage

# View coverage report
open build/reports/jacoco/test/html/index.html

# Run the CLI application
./gradlew run --args="<app_id> <app_secret> <document_id>"

# Create executable distribution
./gradlew installDist
# The distribution will be created in: build/install/feishu2md/
```

---

## Generating Documentation

The project uses [Dokka](https://kotlin.github.io/dokka/) to generate API documentation from KDoc comments.

```bash
# Generate HTML documentation (Dokka V2)
./gradlew dokkaGenerateHtml

# Generate all documentation formats
./gradlew dokkaGenerate

# Or use the convenient docs task
./gradlew docs

# Open HTML documentation
open build/dokka/html/index.html
```

**Output locations**:
- HTML format: `build/dokka/html/`
- Markdown format: `build/dokka/markdown/`

**When to update documentation**:
- After adding new public APIs
- After changing method signatures
- After adding new parameters or return types
- Before releasing a new version

---

## API Reference

### Feishu2HtmlOptions

Configuration options for the export process.

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| appId | String | ✅ | - | Feishu App ID from the developer console |
| appSecret | String | ✅ | - | Feishu App Secret |
| outputDir | String | ❌ | "./output" | Directory for HTML output files |
| imageDir | String | ❌ | "./output/images" | Directory for downloaded images |
| fileDir | String | ❌ | "./output/files" | Directory for downloaded attachments |
| imagePath | String | ❌ | "images" | Relative path for image references in HTML |
| filePath | String | ❌ | "files" | Relative path for file references in HTML |
| customCss | String? | ❌ | null | Custom CSS styles (overrides default) |

### Feishu2Html

Main class for document export operations.

#### Constructor

```kotlin
Feishu2Html(options: Feishu2HtmlOptions)
```

Creates a new instance with the specified configuration.

#### Methods

##### export

```kotlin
suspend fun export(documentId: String, outputFileName: String? = null)
```

Export a single Feishu document to HTML.

**Parameters**:
- `documentId`: Feishu document ID (extract from document URL)
- `outputFileName`: Optional custom output filename (defaults to document title)

**Behavior**:
- Fetches document metadata and content from Feishu API
- Downloads all images and attachments
- Generates HTML file with embedded styles
- Saves to `outputDir`

**Example**:
```kotlin
feishu2Html.export("abc123xyz456")
feishu2Html.export("abc123xyz456", "my-custom-name.html")
```

##### exportBatch

```kotlin
suspend fun exportBatch(documentIds: List<String>)
```

Batch export multiple documents in sequence.

**Parameters**:
- `documentIds`: List of document IDs to export

**Behavior**:
- Processes documents sequentially
- Respects rate limiting
- Continues on error (logs but doesn't stop)

**Example**:
```kotlin
feishu2Html.exportBatch(listOf("doc1", "doc2", "doc3"))
```

##### close

```kotlin
fun close()
```

Close HTTP client and release resources. **Always call this when done**.

**Example**:
```kotlin
try {
    feishu2Html.export("document_id")
} finally {
    feishu2Html.close()
}
```

---

## Adding Support for New Block Types

If you want to add support for a currently unsupported block type:

### 1. Update BlockType Enum

In `Block.kt`, add the new type to the enum:

```kotlin
enum class BlockType(val typeCode: Int) {
    // ... existing types
    NEW_BLOCK_TYPE(53), // New type
    // ...
}
```

### 2. Create Block Data Class

Add to the appropriate `*Blocks.kt` file (or create new if needed):

```kotlin
@Serializable
@SerialName("53")
data class NewBlock(
    @SerialName("block_id") override val blockId: String,
    @SerialName("block_type") override val blockType: BlockType,
    @SerialName("parent_id") override val parentId: String? = null,
    override val children: List<String>? = null,
    @SerialName("comment_ids") override val commentIds: List<String>? = null,
    @SerialName("new_data") val newData: NewBlockData? = null
) : Block()
```

### 3. Create BlockData Class

Add to `BlockData.kt`:

```kotlin
@Serializable
data class NewBlockData(
    val field1: String,
    val field2: Int? = null,
    // ... other fields from Feishu API
)
```

### 4. Create Renderer

Create `NewBlockRenderer.kt` in `converter/renderers/`:

```kotlin
package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*

object NewBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext
    ) {
        val newBlock = block as NewBlock
        val data = newBlock.newData ?: return
        
        parent.div("new-block-class") {
            // Your rendering logic here
            +data.field1
        }
    }
}
```

### 5. Update BlockSerializer

In `Block.kt`, add to the `selectDeserializer` method:

```kotlin
object BlockSerializer : JsonContentPolymorphicSerializer<Block>(Block::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Block> {
        // ...
        return when (blockType) {
            // ... existing types
            BlockType.NEW_BLOCK_TYPE -> NewBlock.serializer()
            // ...
        }
    }
}
```

### 6. Update renderBlock Function

In `HtmlBuilder.kt`, add to the `renderBlock` when expression:

```kotlin
fun renderBlock(block: Block, parent: FlowContent, allBlocks: Map<String, Block>, context: RenderContext) {
    when (block) {
        // ... existing types
        is NewBlock -> NewBlockRenderer.render(parent, block, allBlocks, context)
        // ...
    }
}
```

### 7. Add Tests

Create `NewBlockRendererTest.kt` in `src/test/kotlin/dev/yidafu/feishu2md/converter/renderers/`:

```kotlin
class NewBlockRendererTest : FunSpec({
    val context = RenderContext(
        textConverter = TextElementConverter(),
        processedBlocks = mutableSetOf()
    )
    
    test("should render NewBlock correctly") {
        val block = NewBlock(
            blockId = "new1",
            blockType = BlockType.NEW_BLOCK_TYPE,
            children = emptyList(),
            parentId = "page1",
            newData = NewBlockData(field1 = "test")
        )
        
        val html = createHTML().div {
            NewBlockRenderer.render(this, block, emptyMap(), context)
        }
        
        html shouldContain "test"
    }
})
```

### 8. Update Documentation

- Update the Supported Block Types table in README.md
- Mark the block type as "✅ Full" or "⚠️ Partial"
- Add any notes about limitations

---

## Testing

The project has comprehensive test coverage using **Kotest + MockK + JaCoCo**.

### Current Test Coverage

- **Overall**: 61% (Target: 98%)
- **Renderer Layer**: 78% ✅
- **Converter Layer**: 67% ✅
- **Model Layer**: 65% ✅
- **API Layer**: 41% ⚠️
- **Main Layer**: 20% ⚠️

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "dev.yidafu.feishu2html.api.RateLimiterTest"

# Generate coverage report
./gradlew testCoverage

# View coverage report
open build/reports/jacoco/test/html/index.html

# View test report
open build/reports/tests/test/index.html
```

### Test Framework

#### Kotest FunSpec Style

```kotlin
class MyTest : FunSpec({
    test("should do something") {
        // Test logic
        result shouldBe expected
    }
    
    test("should handle edge cases") {
        // Edge case testing
        result shouldNotBe null
    }
})
```

#### Kotest Assertions

```kotlin
// Equality assertions
actual shouldBe expected
actual shouldNotBe unexpected

// String assertions
str shouldContain "substring"
str shouldNotContain "badstring"

// Collection assertions
list shouldHaveSize 5
list.shouldBeEmpty()
list shouldContain element

// Exception assertions
shouldThrow<Exception> {
    // Code that should throw
}
```

#### MockK for Mocking

```kotlin
val mockClient = mockk<HttpClient> {
    coEvery { get(any()) } returns mockResponse
}

// Verify calls
coVerify { mockClient.get(any()) }
```

### Test Structure

Organize tests to mirror the source structure:

```
src/test/kotlin/dev/yidafu/feishu2md/
├── api/
│   ├── RateLimiterTest.kt           # Rate limiter tests
│   ├── FeishuAuthServiceTest.kt     # Auth service tests
│   ├── FeishuApiClientCompleteTest.kt
│   └── FeishuApiClientMockTest.kt
├── model/
│   ├── BlockSerializationTest.kt    # Block serialization tests
│   ├── BlockTypeTest.kt             # BlockType enum tests
│   ├── DocumentTest.kt
│   ├── EnumsTest.kt
│   ├── TextElementTest.kt
│   └── BlockDataTest.kt
├── converter/
│   ├── HtmlBuilderSimpleTest.kt
│   ├── HtmlBuilderCompleteTest.kt
│   ├── TextElementConverterTest.kt
│   ├── FeishuStylesTest.kt
│   ├── RenderContextTest.kt
│   ├── RenderHelpersTest.kt
│   └── renderers/
│       ├── TextBlockRendererTest.kt
│       ├── HeadingBlockRendererTest.kt
│       ├── RemainingHeadingRendererTest.kt
│       ├── ListBlockRendererTest.kt
│       ├── BasicBlockRendererTest.kt
│       ├── MediaBlockRendererTest.kt
│       ├── ContainerBlockRendererTest.kt
│       ├── UnsupportedBlocksRendererTest.kt
│       ├── TableBlockRendererTest.kt
│       └── CodeBlockRendererTest.kt
├── EdgeCaseTest.kt                  # Edge case tests
├── Feishu2HtmlTest.kt              # Main class tests
└── Feishu2HtmlIntegrationTest.kt   # Integration tests
```

### Test Data

Test resources are sanitized versions of real Feishu API responses:

- **test-document-1.json**: Based on feishu.json (sanitized)
- **test-document-2.json**: Based on feishu2.json (sanitized)
- **test-document-minimal.json**: Minimal test document

**Sanitization removes**:
- user_id, chat_id, email → `REDACTED_*`
- Real tokens → `test_token_*`
- URL parameters

---

## Coding Guidelines

### Kotlin Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Add KDoc comments for public APIs
- Keep functions focused and small (< 50 lines when possible)

### Code Formatting

The project uses [ktlint](https://pinterest.github.io/ktlint/) for consistent code formatting.

**Check code style:**
```bash
./gradlew ktlintCheck
```

**Auto-format code:**
```bash
./gradlew ktlintFormat
```

**Before committing:**
- Always run `./gradlew ktlintFormat` to auto-format your code
- Ensure `./gradlew ktlintCheck` passes without errors
- The ktlint configuration is defined in `build.gradle.kts`
- EditorConfig settings are in `.editorconfig`

### ktlint Configuration

The project uses a custom `.editorconfig` file:

```ini
[*.{kt,kts}]
indent_size = 4
max_line_length = 120
ij_kotlin_allow_trailing_comma = true

# Disabled rules
ktlint_standard_no-wildcard-imports = disabled
ktlint_standard_import-ordering = disabled
ktlint_standard_filename = disabled
ktlint_standard_value-argument-comment = disabled
ktlint_standard_value-parameter-comment = disabled
```

### Naming Conventions

- **Block classes**: `XxxBlock` (e.g., `TextBlock`, `ImageBlock`)
- **Renderer objects**: `XxxBlockRenderer` (e.g., `TextBlockRenderer`)
- **Data classes**: `XxxBlockData` (e.g., `TextBlockData`)
- **Enum values**: `UPPER_SNAKE_CASE` (e.g., `BLOCK_TYPE`)
- **Package names**: lowercase (e.g., `dev.yidafu.feishu2html`)

### File Organization

- One renderer per file (exception: related renderers can be grouped)
- Block classes grouped by functionality:
  - `TextBlocks.kt` - Page, Text
  - `HeadingBlocks.kt` - Heading1-9
  - `ListBlocks.kt` - Bullet, Ordered
  - `ContentBlocks.kt` - Code, Quote, Equation, Todo, Divider
  - `MediaBlocks.kt` - Image, File, Board, Diagram, Iframe
  - `ContainerBlocks.kt` - Callout, Grid, Table, etc.
  - `OtherBlocks.kt` - Bitable, ChatCard, Unknown
  - `UnsupportedBlocks.kt` - All unsupported types
- All BlockData classes in `BlockData.kt`

---

## Troubleshooting Development Issues

### 1. Compilation Errors After Adding New Block

- Ensure the Block class is added to `BlockSerializer.selectDeserializer`
- Ensure the Block class is added to `HtmlBuilder.renderBlock`
- Check that all imports are correct (`import dev.yidafu.feishu2html.api.model.*`)
- Verify the `@SerialName` annotation matches the BlockType code

### 2. Serialization Issues

- Use `@SerialName` for all fields that map to JSON keys
- Make optional fields nullable with `? = null`
- Test with actual Feishu API responses
- Enable DEBUG logging to see raw API responses

### 3. Rendering Not Working

- Check that the Renderer is correctly implementing `Renderable`
- Ensure the render logic handles null data gracefully
- Use logging to debug (`logger.debug(...)`)
- Check the generated HTML in browser DevTools

### 4. Test Failures

- Check `build/reports/tests/test/index.html` for detailed error messages
- Ensure test data matches actual API response structure
- Use `shouldBe` for exact matches, `shouldContain` for partial matches

### 5. Coverage Not Improving

- Check `build/reports/jacoco/test/html/index.html` to find uncovered code
- Add targeted tests for uncovered branches
- Note: Some code paths (like HTTP calls) require architecture changes to test

---

## Pull Request Process

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Make** your changes
4. **Format** your code (`./gradlew ktlintFormat`)
5. **Test** your changes thoroughly (`./gradlew test`)
6. **Update** documentation if needed
7. **Commit** with clear messages following Conventional Commits:
   ```
   feat(renderer): add TaskBlock rendering support
   
   - Create TaskBlockRenderer
   - Add task-block CSS styles
   - Update BlockSerializer mapping
   - Add tests for TaskBlockRenderer
   
   Closes #123
   ```
8. **Push** to your branch (`git push origin feature/amazing-feature`)
9. **Open** a Pull Request

### Commit Message Format

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code style (formatting, no logic change)
- `refactor`: Refactoring
- `test`: Adding or updating tests
- `chore`: Build/tool changes

**Examples**:
```
feat(renderer): add support for TaskBlock rendering
fix(api): handle rate limiting properly
docs: update README with new block types
test: add tests for TableBlockRenderer
```

### PR Guidelines

- Provide a clear description of what the PR does
- Reference any related issues
- Ensure code compiles without warnings
- Ensure all tests pass
- Ensure ktlint check passes
- Add or update documentation as needed
- Keep PRs focused on a single feature/fix

---

## Code Review

All contributions will be reviewed for:
- Code quality and style (ktlint compliance)
- Correctness and completeness
- Test coverage for new code
- Documentation (KDoc for public APIs)
- Potential performance issues
- Security considerations (especially API credentials handling)

---

## Questions?

Feel free to:
- Open an issue for questions
- Start a discussion
- Reach out to the maintainers

---

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to Feishu2Md! 🎉

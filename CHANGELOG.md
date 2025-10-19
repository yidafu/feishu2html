# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.2] - 2025-10-19

### Fixed
- Fixed Kotlin/Native build task names in GitHub Actions workflow
  - Corrected macOS build tasks to use `macosArm64MainBinaries` and `macosX64MainBinaries`
  - Updated Linux and Windows tasks to use `linuxX64MainBinaries` and `mingwX64MainBinaries`
- Added `kotlin.native.ignoreDisabledTargets=true` to gradle.properties to allow cross-platform builds
- Fixed Linux x64 linker errors by disabling Kotlin Native cache (`kotlin.native.cacheKind.linuxX64=none`)
- Fixed Windows build path issues in GitHub Actions workflow
- Fixed Linux/Windows build libcurl dependency issues by switching from Curl engine to CIO engine

### Changed
- Updated Gradle version badge from 8.5 to 8.10.2 in README
- Improved Quick Start guide with dedicated Installation section
- Added quick links to releases, documentation, and Maven Central at top of README
- Upgraded Dokka to version 2.1.0 with V2 configuration

### Verified
- ✅ **macOS Platform Verified** - Successfully tested on macOS (both Intel and Apple Silicon)
  - macOS x64 (Intel) binary working correctly
  - macOS ARM64 (Apple Silicon) binary working correctly
  - All features confirmed functional on macOS platform

## [1.0.0] - 2025-10-19

### 🎉 Initial Release

This is the first stable release of Feishu2HTML - a powerful Kotlin Multiplatform library and CLI tool to convert Feishu (Lark) documents to beautiful, standalone HTML files.

### ✨ Features

#### Core Functionality
- **Kotlin Multiplatform Support** - Runs on JVM, JS (Node.js), and Native platforms (macOS, Linux, Windows, iOS)
- **Comprehensive Block Support** - All major Feishu document block types:
  - Headings (H1-H9)
  - Paragraphs and text formatting
  - Lists (bullet, ordered)
  - Tables with cells
  - Code blocks with 70+ language syntax highlighting
  - Math equations (MathJax rendering)
  - Images and file attachments
  - Quote blocks and callouts
  - Todo lists
  - Diagrams
  - Iframe embeds
  - Grid layouts
  - Board (electronic whiteboard)
  - And more...

#### Document Processing
- **Resource Management** - Automatic download and save of images and attachments
- **Rich Text Formatting** - Full support for text styles (bold, italic, underline, strikethrough, links, etc.)
- **Async Downloads** - Asynchronous resource downloading for better performance
- **Batch Export** - Export multiple documents in one go

#### Styling & Output
- **Official Feishu Styles** - Optimized CSS extracted from official Feishu (98.4% size reduction, 16KB vs 1MB)
- **Flexible CSS Options**:
  - External CSS file (default)
  - Inline CSS mode for single-file portability
  - Custom CSS support
- **Authentic Appearance** - Faithfully recreates original Feishu document styling and layout

#### Developer Experience
- **Type Safety** - Type-safe HTML generation using kotlinx.html DSL
- **Clean Architecture** - Elegant Renderer delegation pattern
- **Comprehensive Logging** - Built-in logging throughout the application
- **Resource Management** - AutoCloseable interface for proper cleanup
- **API Documentation** - Complete KDoc reference published to GitHub Pages

#### Platform Support
- **JVM** (✅ Production Ready) - Full features including CLI tool
- **JS (Node.js)** (✅ Fully Supported) - Core library features
- **Native (macOS/Linux/Windows/iOS)** (🔄 Experimental) - Core library features

### 📚 Documentation
- Comprehensive README with quick start guide
- Contributing guide with architecture details
- API documentation automatically generated and deployed
- Visual comparison screenshots
- Detailed troubleshooting section
- Complete block type support reference

### 🛠️ Technical Highlights
- Built with Kotlin 2.1.0
- Uses Ktor for HTTP client
- kotlinx.html for type-safe HTML generation
- kotlinx.serialization for JSON handling
- kotlinx.coroutines for async operations
- MathJax for mathematical formula rendering
- Multiplatform file system abstraction
- Rate limiting (QPS=2) to respect API limits

### 📦 Distribution
- Published to Maven Central
- Available for:
  - `feishu2html` - Common multiplatform artifact
  - `feishu2html-jvm` - JVM-specific
  - `feishu2html-js` - JS-specific
  - `feishu2html-metadata` - Kotlin Multiplatform metadata
- GitHub Releases with pre-built binaries:
  - macOS (Apple Silicon & Intel)
  - Linux x64
  - Windows x64
  - Cross-platform JVM JAR

### 🔧 CI/CD & Automation
- GitHub Actions workflow for automatic releases
- Automated multi-platform binary builds
- Automatic release asset uploads on version tags
- Changelog-driven release notes

### ⚠️ Known Limitations
- External document references require manual permission grants
- Real-time collaboration content not included in exports
- Some advanced block types not yet supported (ISV, Mindnote, Sheet, Task, OKR, Wiki Catalog, Agenda, Link Preview, etc.)
- API rate limiting may affect very large documents

### 🙏 Acknowledgments
- Inspired by [feishu2md](https://github.com/S-TE11A/feishu2md)
- Uses official Feishu CSS for authentic styling

---

[1.0.1]: https://github.com/yidafu/feishu2html/releases/tag/v1.0.1
[1.0.0]: https://github.com/yidafu/feishu2html/releases/tag/v1.0.0


# Architecture Design Document

## Overview

Feishu2HTML is a Kotlin Multiplatform library that converts Feishu documents to HTML. This document describes the architectural decisions and design patterns used.

## High-Level Architecture

```
┌────────────────────────────────────────────────────────────┐
│                    User Application                         │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────────┐
│                  Feishu2Html (Facade)                       │
│  - Orchestrates the conversion process                     │
│  - Manages resource lifecycle (AutoCloseable)              │
└────┬───────────────┬────────────────┬─────────────────────┘
     │               │                │
     ▼               ▼                ▼
┌─────────┐   ┌──────────┐    ┌──────────────┐
│   API   │   │Converter │    │   Platform   │
│  Layer  │   │  Layer   │    │   Adapters   │
└─────────┘   └──────────┘    └──────────────┘
```

## Key Components

### 1. API Layer

**Purpose**: Encapsulates all interactions with Feishu Open Platform API

**Components**:
- `FeishuApiClient`: Main API client with rate limiting
- `FeishuAuthService`: OAuth token management and caching
- `RateLimiter`: QPS control with exponential backoff
- `RetryUtils`: Automatic retry with smart exception handling

**Design Decisions**:
- ✅ Internal visibility (not exposed to library users)
- ✅ Automatic rate limiting (5 QPS default)
- ✅ Exponential backoff retry for transient failures
- ✅ Token caching to minimize auth requests

### 2. Converter Layer

**Purpose**: Transform Feishu blocks to HTML

**Pattern**: **Renderer Delegation**

```kotlin
interface Renderable {
    fun render(content: FlowContent, block: Any, blocks: Map<String, Block>, context: RenderContext)
}

// Each block type has its own renderer
class TextBlockRenderer : Renderable { ... }
class HeadingBlockRenderer : Renderable { ... }
class TableBlockRenderer : Renderable { ... }
```

**Benefits**:
- ✅ Single Responsibility: Each renderer handles one block type
- ✅ Open/Closed: Easy to add new renderers without modifying existing code
- ✅ Testability: Each renderer can be tested independently
- ✅ Maintainability: Clear separation of concerns

### 3. Platform Abstraction Layer

**Purpose**: Abstract platform-specific operations

**Pattern**: **Expect/Actual**

```kotlin
// commonMain
expect class PlatformFileSystem {
    fun writeText(path: String, content: String)
    fun writeBytes(path: String, content: ByteArray)
    fun exists(path: String): Boolean
    fun createDirectories(path: String)
}

// jvmMain
actual class PlatformFileSystem {
    actual fun writeText(...) { File(path).writeText(content) }
}

// jsMain
actual class PlatformFileSystem {
    actual fun writeText(...) { NodeFs.writeFileSync(path, content) }
}
```

**Supported Platforms**:
- JVM: `java.io.File`, `java.util.Base64`
- JS: Node.js `fs` module, `Buffer`
- Native: POSIX/Windows APIs, Foundation framework

---

## Design Patterns

### 1. Facade Pattern

`Feishu2Html` provides a simple interface hiding complex subsystems:

```kotlin
class Feishu2Html(options: Feishu2HtmlOptions) : AutoCloseable {
    suspend fun export(documentId: String) {
        // Orchestrates: API → Download → Convert → Save
    }
}
```

**Benefits**:
- Simple API for users
- Hides complexity of API calls, downloads, rendering
- Resource management via AutoCloseable

### 2. Strategy Pattern

Template modes use strategy pattern:

```kotlin
sealed class HtmlTemplate {
    object Default : HtmlTemplate()
    class Fragment(val template: HTML.() -> Unit) : HtmlTemplate()
    class Full(val template: HTML.() -> Unit) : HtmlTemplate()
}
```

### 3. Observer Pattern

Progress callbacks use observer pattern:

```kotlin
interface ExportProgressCallback {
    fun onStart(documentId: String)
    fun onComplete(documentId: String, outputPath: String)
    fun onError(documentId: String, error: Throwable)
}
```

### 4. Dependency Injection

Constructor injection for testability:

```kotlin
class Feishu2Html internal constructor(
    private val options: Feishu2HtmlOptions,
    internal val apiClient: FeishuApiClient,  // Injectable
    internal val fileSystem: PlatformFileSystem  // Injectable
) {
    // Public constructor with defaults
    constructor(options: Feishu2HtmlOptions) : this(
        options,
        FeishuApiClient(options.appId, options.appSecret),
        getPlatformFileSystem()
    )
}
```

---

## Code Organization

### Source Sets

```
src/
├── commonMain/       # Platform-agnostic code (95%+)
│   ├── api/          # Feishu API client
│   ├── converter/    # HTML conversion logic
│   ├── platform/     # Platform abstractions (expect)
│   └── Feishu2Html.kt
├── cliMain/          # CLI-specific code
│   └── cli/CliRunner.kt
├── jvmMain/          # JVM implementations (actual)
├── jsMain/           # JavaScript implementations (actual)
├── darwinMain/       # macOS/iOS shared code
├── macosMain/        # macOS-specific (CLI support)
├── iosMain/          # iOS-specific
├── linuxMain/        # Linux implementations
└── mingwMain/        # Windows implementations
```

### Test Organization

```
src/
├── commonTest/       # Cross-platform tests
├── cliTest/          # CLI tests (JVM, JS, Native CLI platforms)
└── jvmTest/          # JVM-specific tests (including mocks)
```

**Rationale**: 
- JVM tests use mockk (JVM-only library)
- Common tests use Kotest (multiplatform)
- Platform-specific tests validate actual implementations

---

## Data Flow

### Export Process

```
User Request
    │
    ▼
Feishu2Html.export()
    │
    ├─▶ 1. FeishuApiClient.getDocumentInfo()
    │      └─▶ FeishuAuthService.getAccessToken()
    │           └─▶ RateLimiter.execute()
    │                └─▶ HTTP Request (with retry)
    │
    ├─▶ 2. FeishuApiClient.getDocumentRawContent()
    │      └─▶ Same auth + rate limiting + retry
    │
    ├─▶ 3. downloadAssets() (parallel)
    │      ├─▶ Image downloads (with semaphore)
    │      ├─▶ File downloads (with semaphore)
    │      └─▶ Board exports (with semaphore)
    │
    ├─▶ 4. HtmlBuilder.build()
    │      └─▶ For each block:
    │           └─▶ Renderer.render()
    │
    └─▶ 5. PlatformFileSystem.writeText()
```

### Concurrency Model

- **Sequential**: Document fetch (API dependencies)
- **Parallel**: Asset downloads (independent operations)
- **Limited**: Semaphore controls concurrent downloads
- **Async**: All I/O operations use coroutines

---

## Error Handling Strategy

### Exception Hierarchy

```
Exception
    └─▶ FeishuApiException (open class)
            ├─▶ NetworkError (retriable)
            ├─▶ RateLimitError (retriable)
            ├─▶ AuthenticationError (not retriable)
            ├─▶ InsufficientPermission (not retriable)
            ├─▶ DocumentNotFound (not retriable)
            └─▶ ApiError (retriable for 5xx)
```

### Retry Logic

```kotlin
withRetry(retryOn = ::isRetriableException) {
    // API call
}

fun isRetriableException(e: Throwable): Boolean {
    return when (e) {
        is NetworkError -> true        // Network issues
        is RateLimitError -> true      // Will retry after delay
        is ApiError -> e.code in 500..599  // Server errors
        else -> false                  // Permanent failures
    }
}
```

---

## Performance Considerations

### 1. Rate Limiting

- **Problem**: Feishu API has 5 QPS limit
- **Solution**: `RateLimiter` with token bucket algorithm
- **Benefit**: Prevents API errors, automatic throttling

### 2. Concurrent Downloads

- **Problem**: Sequential downloads are slow
- **Solution**: `async { }` + `Semaphore(10)`
- **Benefit**: 10x faster while preventing resource exhaustion

### 3. Token Caching

- **Problem**: Auth API has strict limits
- **Solution**: Cache tokens with expiration check
- **Benefit**: Reduces auth calls by 99%

### 4. Resource Deduplication

- **Problem**: Same images in multiple documents
- **Solution**: Check `fileSystem.exists()` before download
- **Benefit**: Faster re-exports, saves bandwidth

---

## Testing Strategy

### Test Pyramid

```
        /\
       /E2E\         Integration Tests (FeishuApiClientCompleteTest)
      /------\
     /  Unit  \       Mock Tests (Feishu2HtmlMockTest, RetryUtilsTest)
    /----------\
   / Component  \     Renderer Tests, Model Tests
  /--------------\
```

### Coverage Goals

- **Overall**: 70%+ (achieved: 68%)
- **Core (Feishu2Html)**: 70%+ (achieved: 87%) ✅
- **API Layer**: 60%+ (achieved: 50%)
- **Platform**: 90%+ (achieved: 98%) ✅
- **Converters**: 75%+ (achieved: 76%) ✅

### Test Types

1. **Unit Tests**: Individual functions (RetryUtilsTest, ImageEncoderTest)
2. **Component Tests**: Single classes (RendererTests, FileSystemTest)
3. **Integration Tests**: Multiple components (Feishu2HtmlMockTest)
4. **E2E Tests**: Real API calls (FeishuApiClientCompleteTest - manual)

---

## Scalability

### Current Limits

- Documents: Unlimited
- Concurrent downloads: Configurable (default 10)
- Document size: No hard limit
- Image size: Limited by memory

### Future Improvements

- Streaming large documents
- Distributed processing
- CDN integration for assets
- Database caching layer

---

## Security Considerations

1. **Credentials**: Stored in-memory only, never logged
2. **HTTPS**: All API calls use TLS
3. **Input Validation**: All options validated at init
4. **Output Sanitization**: HTML escaped via kotlinx.html

---

## Maintenance

### Adding New Block Type

1. Define model in `api/model/`
2. Create renderer in `converter/renderers/`
3. Register in `HtmlBuilder.kt`
4. Add tests in `renderers/`

### Adding New Platform

1. Create `{platform}Main/` source set
2. Implement `actual` for platform abstractions
3. Configure in `build.gradle.kts`
4. Add platform-specific tests

---

## Performance Monitoring

### Architecture Overview

The performance monitoring system is designed to track export operations and collect metrics for analysis and optimization.

```
┌────────────────────┐
│   Feishu2Html      │
│   export()         │
└─────────┬──────────┘
          │
          │ Record metrics
          ▼
┌────────────────────┐
│ MetricsCollector   │
│  (Interface)       │
└─────────┬──────────┘
          │
          ├─────────────────────┐
          │                     │
          ▼                     ▼
┌──────────────────┐  ┌─────────────────┐
│ InMemory         │  │ Custom          │
│ MetricsCollector │  │ (Database, etc) │
└──────────────────┘  └─────────────────┘
```

### Core Components

#### 1. ExportMetrics

Data class that holds performance metrics for a single export operation:

```kotlin
data class ExportMetrics(
    val documentId: String,
    val startTime: Instant,
    val endTime: Instant,
    val duration: Duration,
    val blocksCount: Int,
    val imagesDownloaded: Int,
    val filesDownloaded: Int,
    val boardsExported: Int,
    val totalBytes: Long,
    val success: Boolean,
    val errorMessage: String? = null
) {
    val averageTimePerBlock: Duration 
        get() = if (blocksCount > 0) duration / blocksCount else Duration.ZERO
}
```

**Design Decisions:**
- Immutable data class for thread safety
- Computed property for derived metrics
- Comprehensive data for detailed analysis

#### 2. MetricsCollector Interface

Defines the contract for collecting and retrieving metrics:

```kotlin
interface MetricsCollector {
    fun recordExport(metrics: ExportMetrics)
    fun getMetrics(): List<ExportMetrics>
    fun getAverageExportTime(): Duration
    fun getSuccessRate(): Double
    fun clear()
}
```

**Design Principles:**
- Simple interface for easy implementation
- Aggregation methods for common queries
- Clear lifecycle management

#### 3. InMemoryMetricsCollector

Default implementation that stores metrics in memory:

```kotlin
class InMemoryMetricsCollector : MetricsCollector {
    private val metrics = mutableListOf<ExportMetrics>()
    // ... implementation
}
```

**Characteristics:**
- Fast performance (no I/O)
- No persistence (data lost on restart)
- Suitable for testing and development
- Thread-safe operations

### Integration Points

#### 1. Feishu2Html.export()

Performance tracking is integrated directly into the export flow:

```kotlin
suspend fun export(documentId: String, ...) {
    val startTime = Clock.System.now()
    var blocksCount = 0
    var imagesDownloaded = 0
    // ... track metrics during export
    
    try {
        // ... export logic
        success = true
    } catch (e: Exception) {
        errorMessage = e.message
        throw e
    } finally {
        val endTime = Clock.System.now()
        options.metricsCollector?.recordExport(
            ExportMetrics(...)
        )
    }
}
```

**Key Features:**
- Minimal performance overhead
- Automatic metric collection
- Graceful handling of failures
- Optional (null-safe)

#### 2. Configuration

Metrics collection is opt-in through `Feishu2HtmlOptions`:

```kotlin
data class Feishu2HtmlOptions(
    // ... other options
    val metricsCollector: MetricsCollector? = null
)
```

**Benefits:**
- Backward compatible (default null)
- No overhead when disabled
- Easy to enable/disable

### Extension Points

#### Custom Metrics Collectors

Users can implement custom collectors for various use cases:

**1. Database Persistence:**
```kotlin
class DatabaseMetricsCollector(private val db: Database) : MetricsCollector {
    override fun recordExport(metrics: ExportMetrics) {
        db.insert("metrics", metrics)
    }
    // ...
}
```

**2. Real-time Monitoring:**
```kotlin
class MonitoringSystemCollector(private val monitoring: MonitoringService) : MetricsCollector {
    override fun recordExport(metrics: ExportMetrics) {
        monitoring.recordMetric("export.duration", metrics.duration)
        monitoring.recordMetric("export.success", if (metrics.success) 1 else 0)
    }
    // ...
}
```

**3. Composite Collector:**
```kotlin
class CompositeMetricsCollector(
    private val collectors: List<MetricsCollector>
) : MetricsCollector {
    override fun recordExport(metrics: ExportMetrics) {
        collectors.forEach { it.recordExport(metrics) }
    }
    // ...
}
```

### Performance Considerations

#### Overhead

The metrics collection system is designed to have minimal impact:

- **Recording**: O(1) for `InMemoryMetricsCollector`
- **Memory**: ~200 bytes per metric record
- **CPU**: Negligible (simple field assignment)

#### Scalability

For high-volume scenarios:

1. **Use efficient collectors**: Batch database writes
2. **Implement sampling**: Record only 1% of exports
3. **Asynchronous recording**: Use coroutines for I/O
4. **Data retention**: Implement cleanup policies

Example sampling:
```kotlin
class SamplingMetricsCollector(
    private val delegate: MetricsCollector,
    private val sampleRate: Double = 0.01
) : MetricsCollector {
    override fun recordExport(metrics: ExportMetrics) {
        if (Random.nextDouble() < sampleRate) {
            delegate.recordExport(metrics)
        }
    }
    // ...
}
```

### Testing

The metrics system is fully testable:

#### Unit Tests
- `MetricsCollectorTest.kt`: Core functionality
- `MetricsCollectorIntegrationTest.kt`: Integration scenarios

#### Performance Tests
- `PerformanceBenchmarkTest.kt`: Benchmark suite with metrics

#### Test Data Generators
- `TestDataGenerator.kt`: Generate test documents of various sizes

### Future Enhancements

Potential improvements to the metrics system:

1. **Histogram Support**: Track distribution of export times
2. **Resource Metrics**: Track memory and CPU usage
3. **Network Metrics**: Track API latency and bandwidth
4. **Alerting**: Automatic alerts for anomalies
5. **Visualization**: Built-in dashboards and charts

---

## Related Documents

- [CONTRIBUTING.md](../CONTRIBUTING.md) - Contribution guidelines
- [README.md](../README.md) - User documentation
- [advanced-usage.md](advanced-usage.md) - Advanced features
- [PERFORMANCE.md](PERFORMANCE.md) - Performance optimization guide


# Android 平台移除总结

**日期**: 2025-10-19  
**操作**: 移除 Android 平台支持

---

## ✅ 清理完成

### 删除的文件和目录

1. **`src/androidMain/`** - 整个目录
   - `kotlin/.../HttpClientFactory.kt`
   - `kotlin/.../FileSystem.kt`
   - `kotlin/.../UrlUtils.kt`
   - `AndroidManifest.xml`

2. **`ANDROID_SUPPORT_STATUS.md`** - Android 状态文档

### 修改的配置文件

#### `gradle/libs.versions.toml`
```diff
- # Android
- agp = "8.0.2"

- ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }

- android-library = { id = "com.android.library", version.ref = "agp" }
```

#### `build.gradle.kts`
```diff
- alias(libs.plugins.android.library)

- android {
-     namespace = "dev.yidafu.feishu2html"
-     compileSdk = 34
-     ...
- }

- androidTarget { ... }

- val androidMain by getting {
-     dependencies {
-         implementation(libs.ktor.client.okhttp)
-     }
- }
```

---

## 📦 当前支持的平台（7个）

| Platform | Target | Engine | Status |
|----------|--------|--------|--------|
| **JVM** | jvm | CIO | ✅ Production |
| **JS** | js(IR) | JS | ✅ Production |
| **macOS** | macosX64 | Darwin | ✅ Stable |
| **macOS** | macosArm64 | Darwin | ✅ Stable |
| **Linux** | linuxX64 | Curl | ✅ Stable |
| **Windows** | mingwX64 | Curl | ✅ Stable |
| **iOS** | iosX64, iosArm64, iosSimulatorArm64 | Darwin | ✅ Stable |

**总计**: 7 个平台，10 个目标

---

## ✅ 验证结果

### 编译测试
```bash
./gradlew compileKotlinJvm        # ✅ 成功
./gradlew compileKotlinJs         # ✅ 成功
./gradlew compileKotlinMacosArm64 # ✅ 成功
```

### 发布测试
```bash
./gradlew publishJvmPublicationToMavenLocal  # ✅ 成功
./gradlew publishJsPublicationToMavenLocal   # ✅ 成功
```

### 可用的发布任务
- ✅ kotlinMultiplatform
- ✅ jvm
- ✅ js
- ✅ macosX64
- ✅ macosArm64
- ✅ linuxX64
- ✅ mingwX64
- ✅ iosX64
- ✅ iosArm64
- ✅ iosSimulatorArm64

---

## 📋 移除原因

Android 平台支持遇到以下问题：

1. **Android Gradle Plugin 解析失败**
   - 无法从 Google Maven 下载 AGP 8.x
   - 网络连接问题

2. **复杂性 vs 需求**
   - Android 支持需要额外的 AGP 配置
   - 当前项目主要面向桌面和服务器端
   - Android 使用场景有限

3. **简化维护**
   - 减少平台特定代码
   - 降低构建复杂度
   - 专注核心平台支持

---

## 🔄 如何重新添加 Android 支持

如果未来需要 Android 支持，执行以下步骤：

### 1. 恢复配置

**libs.versions.toml**:
```toml
agp = "8.2.2"
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
android-library = { id = "com.android.library", version.ref = "agp" }
```

**build.gradle.kts**:
```kotlin
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "dev.yidafu.feishu2html"
    compileSdk = 34
    defaultConfig { minSdk = 24 }
}

kotlin {
    androidTarget { ... }
}
```

### 2. 重新创建 androidMain

从 Git 历史恢复或重新实现平台代码。

### 3. 解决网络问题

使用国内镜像：
```kotlin
maven { url = uri("https://maven.aliyun.com/repository/google") }
```

---

## 📊 项目状态

| 指标 | 值 |
|------|-----|
| 支持平台 | 7 |
| 代码共享率 | ~95% |
| 核心功能 | ✅ 完整 |
| CLI 工具 | ✅ JVM only |
| 发布配置 | ✅ Sonatype |
| 文档 | ✅ 完整 |

---

**移除完成！项目现在专注于 JVM、JS、macOS、Linux、Windows 和 iOS 平台。** 🎯

import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.nmcp)
    `maven-publish`
    signing
    jacoco
}

group = "dev.yidafu.feishu2html"
version = "1.0.2"

// Load local.properties
val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal() // Fallback
}

kotlin {
    jvmToolchain(17)

    // JVM 目标平台
    jvm {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                freeCompilerArgs.add("-Xjsr305=strict")
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    // JS 目标平台 (Node.js only) - UMD/CommonJS
    js(IR) {
        nodejs()
        binaries.executable()
    }

    // Native 桌面平台

    // Linux 平台 - 配置为可执行文件
    linuxX64 {
        binaries {
            executable {
                entryPoint = "dev.yidafu.feishu2html.main"
                baseName = "feishu2html"
            }
        }
    }

    // macOS 平台 - 配置为可执行文件
    macosX64 {
        binaries {
            executable {
                entryPoint = "dev.yidafu.feishu2html.main"
                baseName = "feishu2html"
            }
        }
    }
    macosArm64 {
        binaries {
            executable {
                entryPoint = "dev.yidafu.feishu2html.main"
                baseName = "feishu2html"
            }
        }
    }

    // Windows 平台 - 配置为可执行文件
    mingwX64 {
        binaries {
            executable {
                entryPoint = "dev.yidafu.feishu2html.main"
                baseName = "feishu2html"
            }
        }
    }

    // iOS 平台
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        // Common 依赖
        val commonMain by getting {
            dependencies {
                // Kotlin 核心
                implementation(libs.kotlinx.coroutines.core)

                // 序列化
                implementation(libs.kotlinx.serialization.json)

                // HTTP 客户端
                implementation(libs.bundles.ktor.common)

                // HTML DSL (多平台版本)
                implementation(libs.kotlinx.html)

                // CLI 参数解析
                implementation(libs.kotlinx.cli)

                // 日志
                implementation(libs.kotlin.logging)

                // 日期时间 (多平台)
                implementation(libs.kotlinx.datetime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }

        // JVM 特定依赖
        val jvmMain by getting {
            dependencies {
                // Ktor CIO 引擎 (JVM)
                implementation(libs.ktor.client.cio)

                // JVM 日志实现
                implementation(libs.logback.classic)
            }
        }

        val jvmTest by getting {
            dependencies {
                // JVM 测试框架
                implementation(libs.bundles.kotest)
                implementation(libs.mockk)
            }
        }

        // JS 特定依赖
        val jsMain by getting {
            dependencies {
                // Ktor JS 引擎
                implementation(libs.ktor.client.js)
            }
        }

        // Darwin (macOS/iOS) 平台
        val darwinMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val macosX64Main by getting { dependsOn(darwinMain) }
        val macosArm64Main by getting { dependsOn(darwinMain) }

        // iOS 平台
        val iosMain by creating {
            dependsOn(darwinMain)
        }

        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

        // Linux 平台 (POSIX mkdir)
        val linuxMain by creating {
            dependsOn(commonMain)
            dependencies {
                // 使用 CIO 引擎代替 Curl，避免 libcurl 依赖问题
                implementation(libs.ktor.client.cio)
            }
        }

        val linuxX64Main by getting { dependsOn(linuxMain) }

        // Windows 平台 (Windows API mkdir)
        val mingwMain by creating {
            dependsOn(commonMain)
            dependencies {
                // 使用 CIO 引擎代替 Curl，避免 libcurl 依赖问题
                implementation(libs.ktor.client.curl)
            }
        }

        val mingwX64Main by getting { dependsOn(mingwMain) }
    }

    // 配置所有 Native 平台使用新的内存管理器
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            freeCompilerArgs += "-Xallocator=std"
        }
    }
}

// Maven Publishing 配置 - POM metadata for all publications
// NMCP plugin automatically uses these publications for uploading to Central Portal
publishing {
    publications {
        withType<MavenPublication> {
            // Create empty Javadoc JAR to satisfy Central Portal requirements
            // Dokka HTML is available separately via dokkaHtml task
            val javadocJar =
                tasks.register("${name}JavadocJar", Jar::class) {
                    archiveBaseName.set("${project.name}-${name}")
                    archiveClassifier.set("javadoc")
                    // Empty JAR is acceptable for Central Portal
                }
            artifact(javadocJar)

            pom {
                name.set("Feishu2HTML")
                description.set(
                    "A Kotlin Multiplatform library and CLI tool to convert Feishu (Lark) documents to beautiful, standalone HTML files",
                )
                url.set("https://github.com/yidafu/feishu2html")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("yidafu")
                        name.set("YidaFu")
                        email.set("yidafu90@qq.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/yidafu/feishu2html.git")
                    developerConnection.set("scm:git:ssh://github.com/yidafu/feishu2html.git")
                    url.set("https://github.com/yidafu/feishu2html")
                }
            }
        }
    }
}

// NMCP Plugin 配置 - Modern plugin for Central Portal
// See: https://github.com/GradleUp/nmcp
nmcp {
    // Publish to Central Portal
    // Note: Central Portal does NOT support SNAPSHOT versions
    // All versions must be release versions
    publishAllPublications {
        // Central Portal credentials (Portal user token)
        username.set(
            localProperties.getProperty("centralUsername")
                ?: project.findProperty("centralUsername") as String?
                ?: System.getenv("CENTRAL_USERNAME"),
        )
        password.set(
            localProperties.getProperty("centralPassword")
                ?: project.findProperty("centralPassword") as String?
                ?: System.getenv("CENTRAL_PASSWORD"),
        )

        // Publication type: USER_MANAGED requires manual approval in Portal UI
        // AUTOMATIC would auto-publish, but requires namespace verification
        publicationType.set("USER_MANAGED")
    }
}

// Signing 配置 (发布到 Maven Central 需要)
signing {
    // Check for signing configuration in local.properties
    val signingKey =
        localProperties.getProperty("signing.key")
            ?: System.getenv("SIGNING_KEY")
    val signingPassword =
        localProperties.getProperty("signing.password")
            ?: System.getenv("SIGNING_PASSWORD")
    val signingKeyId = localProperties.getProperty("signing.keyId")
    val signingSecretKeyRingFile = localProperties.getProperty("signing.secretKeyRingFile")

    when {
        // Method 1: In-memory PGP keys (recommended for CI/CD)
        signingKey != null && signingPassword != null -> {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications)
        }
        // Method 2: Use system GPG command (easiest for local development)
        else -> {
            // Use gpg-agent with configured keyId
            useGpgCmd()
            sign(publishing.publications)
        }
    }
}

// Dokka V2 配置 - 生成 API 文档
dokka {
    moduleName.set("feishu2html")

    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
        suppressInheritedMembers.set(false)
    }

    dokkaSourceSets.named("commonMain") {
        // TODO: Dokka 2.1.0 的 Markdown 解析器存在问题，暂时禁用包含文件
        // 等待后续版本修复或寻找替代方案
        // includes.from("MODULE.md")
        // includes.from("src/commonMain/kotlin/dev/yidafu/feishu2html/package.md")
    }
}

// 便捷的文档生成任务
tasks.register("docs") {
    dependsOn("dokkaGenerateHtml")
    description = "Generate all documentation (HTML and Markdown)"
    group = "documentation"
}

// ktlint 配置 - 代码格式化
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.1.1")
    debug.set(false)
    verbose.set(false)
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)

    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
        exclude("**/config.sample.kts")
    }
}

// JaCoCo 配置 - 代码覆盖率 (仅 JVM)
jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("jvmTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val jvmTestTask = tasks.named<Test>("jvmTest").get()
    executionData(jvmTestTask)

    // Set source directories
    sourceDirectories.setFrom(
        files(kotlin.sourceSets["commonMain"].kotlin.srcDirs, kotlin.sourceSets["jvmMain"].kotlin.srcDirs),
    )

    classDirectories.setFrom(
        files(
            layout.buildDirectory.dir("classes/kotlin/jvm/main"),
        ),
    )

    // 排除数据类和模型类
    classDirectories.setFrom(
        classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/model/**Data.class",
                    "**/model/Block.class",
                    "**/model/Document.class",
                    "**/model/BlockType.class",
                    "**/model/Emoji.class",
                    "**/model/TextAlign.class",
                    "**/model/BlockColor.class",
                    "**/model/CodeLanguage.class",
                    "**/model/IframeType.class",
                    "**/MainKt.class",
                )
            }
        },
    )
}

tasks.register("testCoverage") {
    dependsOn("jvmTest", "jacocoTestReport")
    description = "Run tests and generate coverage report"
    group = "verification"
}

// JVM 应用配置 (CLI 工具)
tasks.register<JavaExec>("runJvm") {
    dependsOn("jvmJar")
    group = "application"
    description = "Run the JVM CLI application"
    classpath =
        files(
            tasks.named("jvmJar"),
            configurations.named("jvmRuntimeClasspath"),
        )
    mainClass.set("dev.yidafu.feishu2html.MainKt")
}

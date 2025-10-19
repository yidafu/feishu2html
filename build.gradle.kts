import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.nexus.publish)
    `maven-publish`
    signing
    jacoco
}

group = "dev.yidafu.feishu2html"
version = "1.0.0-SNAPSHOT"

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

    // JS 目标平台 (Node.js only)
    js(IR) {
        nodejs()
        binaries.executable()
    }

    // Native 桌面平台
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64() // Windows

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

        // Native 共享依赖
        val nativeMain by creating {
            dependsOn(commonMain)
        }

        // macOS 和 iOS 使用 Darwin 引擎
        val darwinMain by creating {
            dependsOn(nativeMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val macosX64Main by getting { dependsOn(darwinMain) }
        val macosArm64Main by getting { dependsOn(darwinMain) }
        val iosX64Main by getting { dependsOn(darwinMain) }
        val iosArm64Main by getting { dependsOn(darwinMain) }
        val iosSimulatorArm64Main by getting { dependsOn(darwinMain) }

        // iOS 平台
        val iosMain by creating {
            dependsOn(darwinMain)
        }

        iosX64Main.dependsOn(iosMain)
        iosArm64Main.dependsOn(iosMain)
        iosSimulatorArm64Main.dependsOn(iosMain)

        // 桌面 Native 平台（Linux/Windows）
        val desktopNativeMain by creating {
            dependsOn(nativeMain)
            dependencies {
                implementation(libs.ktor.client.curl)
            }
        }

        val linuxX64Main by getting { dependsOn(desktopNativeMain) }
        val mingwX64Main by getting { dependsOn(desktopNativeMain) }
    }

    // 配置所有 Native 平台使用新的内存管理器
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            freeCompilerArgs += "-Xallocator=std"
        }
    }
}

// Maven Publishing 配置
publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set("Feishu2HTML")
                description.set("A Kotlin Multiplatform library and CLI tool to convert Feishu (Lark) documents to beautiful, standalone HTML files")
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

    // Note: Repository configuration moved to nexusPublishing block below
}

// Nexus Publishing Plugin 配置 - 简化 Central Portal 发布流程
// See: https://github.com/gradle-nexus/publish-plugin
nexusPublishing {
    repositories {
        sonatype {
            // Central Portal endpoints (replaces OSSRH)
            nexusUrl.set(uri("https://central.sonatype.com/api/v1/publisher"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/api/v1/publisher"))

            username.set(
                localProperties.getProperty("centralUsername")
                    ?: localProperties.getProperty("ossrhUsername")
                    ?: project.findProperty("centralUsername") as String?
                    ?: project.findProperty("ossrhUsername") as String?
                    ?: System.getenv("CENTRAL_USERNAME")
                    ?: System.getenv("OSSRH_USERNAME")
            )
            password.set(
                localProperties.getProperty("centralPassword")
                    ?: localProperties.getProperty("ossrhPassword")
                    ?: project.findProperty("centralPassword") as String?
                    ?: project.findProperty("ossrhPassword") as String?
                    ?: System.getenv("CENTRAL_PASSWORD")
                    ?: System.getenv("OSSRH_PASSWORD")
            )
        }
    }

    // Configure timeouts and retry
    connectTimeout.set(java.time.Duration.ofMinutes(3))
    clientTimeout.set(java.time.Duration.ofMinutes(3))

    // Transition from OSSRH to Central Portal
    transitionCheckOptions {
        maxRetries.set(40)
        delayBetween.set(java.time.Duration.ofSeconds(10))
    }
}

// Signing 配置 (发布到 Maven Central 需要)
signing {
    val signingKey = localProperties.getProperty("signing.key")
        ?: project.findProperty("signing.key") as String?
        ?: System.getenv("SIGNING_KEY")
    val signingPassword = localProperties.getProperty("signing.password")
        ?: project.findProperty("signing.password") as String?
        ?: System.getenv("SIGNING_PASSWORD")

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

// Dokka 配置 - 生成 API 文档
tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml").configure {
    outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
    dokkaSourceSets {
        configureEach {
            includes.from("MODULE.md")
            suppressInheritedMembers.set(false)
        }
    }
}

tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaGfm").configure {
    outputDirectory.set(layout.buildDirectory.dir("dokka/markdown"))
}

// 便捷的文档生成任务
tasks.register("docs") {
    dependsOn("dokkaHtml", "dokkaGfm")
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
        files(kotlin.sourceSets["commonMain"].kotlin.srcDirs, kotlin.sourceSets["jvmMain"].kotlin.srcDirs)
    )

    classDirectories.setFrom(
        files(
            layout.buildDirectory.dir("classes/kotlin/jvm/main")
        )
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
        }
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
    classpath = files(
        tasks.named("jvmJar"),
        configurations.named("jvmRuntimeClasspath")
    )
    mainClass.set("dev.yidafu.feishu2html.MainKt")
}

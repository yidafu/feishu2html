plugins {
    kotlin("multiplatform") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    jacoco
}

group = "dev.yidafu.feishu2html"
version = "1.0.0"

repositories {
    mavenCentral()
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

    // JS 目标平台 (Node.js and Browser)
    js(IR) {
        nodejs()
        binaries.executable()
    }

    // Native 桌面平台
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    mingwX64() // Windows

    // iOS 平台
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // Android Native 平台
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()

    sourceSets {
        // Common 依赖
        val commonMain by getting {
            dependencies {
                // Kotlin 核心
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

                // 序列化
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

                // HTTP 客户端
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

                // HTML DSL (多平台版本)
                implementation("org.jetbrains.kotlinx:kotlinx-html:0.11.0")

                // 日志
                implementation("io.github.oshai:kotlin-logging:6.0.3")

                // 文件系统 (多平台)
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.3.1")

                // 日期时间 (多平台)
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
                implementation("io.ktor:ktor-client-mock:2.3.7")
            }
        }

        // JVM 特定依赖
        val jvmMain by getting {
            dependencies {
                // Ktor CIO 引擎 (JVM)
                implementation("io.ktor:ktor-client-cio:2.3.7")

                // JVM 日志实现
                implementation("ch.qos.logback:logback-classic:1.4.14")
            }
        }

        val jvmTest by getting {
            dependencies {
                // JVM 测试框架
                implementation("io.kotest:kotest-runner-junit5:5.8.0")
                implementation("io.kotest:kotest-assertions-core:5.8.0")
                implementation("io.kotest:kotest-property:5.8.0")
                implementation("io.mockk:mockk:1.13.9")
            }
        }

        // JS 特定依赖
        val jsMain by getting {
            dependencies {
                // Ktor JS 引擎
                implementation("io.ktor:ktor-client-js:2.3.7")
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
                implementation("io.ktor:ktor-client-darwin:2.3.7")
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
                implementation("io.ktor:ktor-client-curl:2.3.7")
            }
        }

        val linuxX64Main by getting { dependsOn(desktopNativeMain) }
        val linuxArm64Main by getting { dependsOn(desktopNativeMain) }
        val mingwX64Main by getting { dependsOn(desktopNativeMain) }

        // Android Native 平台
        val androidNativeMain by creating {
            dependsOn(nativeMain)
            dependencies {
                implementation("io.ktor:ktor-client-curl:2.3.7")
            }
        }

        val androidNativeArm32Main by getting { dependsOn(androidNativeMain) }
        val androidNativeArm64Main by getting { dependsOn(androidNativeMain) }
        val androidNativeX86Main by getting { dependsOn(androidNativeMain) }
        val androidNativeX64Main by getting { dependsOn(androidNativeMain) }
    }

    // 配置所有 Native 平台使用新的内存管理器
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            freeCompilerArgs += "-Xallocator=std"
        }
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

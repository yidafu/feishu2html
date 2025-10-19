plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    jacoco
    application
}

group = "dev.yidafu.feishu2html"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // 序列化
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // HTTP客户端
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // 日志
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // kotlinx.html for HTML DSL
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")

    // kotlin-css for CSS DSL (kotlin-wrappers)
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css:2025.10.8")

    // 测试框架
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-property:5.8.0")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.ktor:ktor-client-mock:2.3.7")
}

application {
    mainClass.set("dev.yidafu.feishu2html.MainKt")
}

kotlin {
    jvmToolchain(17)
}

// Dokka配置 - 生成API文档
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

// ktlint配置 - 代码格式化
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
        exclude("**/config.sample.kts") // 排除示例配置文件
    }

    // ktlint会自动读取.editorconfig中的规则配置
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// JaCoCo配置 - 代码覆盖率
jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(
        files(
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
        ),
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.98".toBigDecimal()
            }
        }
    }
}

tasks.register("testCoverage") {
    dependsOn(tasks.test, tasks.jacocoTestReport)
    description = "Run tests and generate coverage report"
    group = "verification"
}

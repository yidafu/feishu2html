package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

internal object FileBlockRenderer : Renderable {
    private val logger = KotlinLogging.logger {}

    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val fileBlock = block as FileBlock
        val token = fileBlock.file?.token ?: return
        val name = fileBlock.file?.name ?: "未命名文件"
        logger.debug { "Rendering file block: name=$name, token=$token" }

        // Use actual filename for href, matching the download logic in Feishu2Html.kt
        val fileName = fileBlock.file?.name ?: token

        // Render Feishu-style file attachment card using official class names
        parent.div(classes = "docx-file-block-container docx-view-type-Card") {
            div(classes = "docx-file-block-inner-container") {
                div(classes = "file-block file-card") {
                    // File icon
                    div(classes = "layout-column file-icon") {
                        +getFileIcon(name)
                    }

                    // File info section
                    div(classes = "layout-column layout-main-center flex") {
                        div(classes = "file-name") {
                            +name
                        }
                        div(classes = "file-desc") {
                            // File size would go here if available from API
                            // For now, leave empty to avoid errors
                        }
                    }

                    // Download button section
                    div(classes = "layout-row file-btn") {
                        a(href = "files/$fileName", classes = "btn-preview") {
                            attributes["download"] = name
                            attributes["title"] = "下载 $name"
                            span(classes = "download-icon") { +"⬇" }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get file icon emoji based on file extension
     */
    private fun getFileIcon(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()

        return when (extension) {
            // Documents
            "txt", "md", "doc", "docx" -> "📄"

            // PDFs
            "pdf" -> "📕"

            // Spreadsheets
            "xls", "xlsx", "csv" -> "📊"

            // Presentations
            "ppt", "pptx" -> "📽️"

            // Images
            "jpg", "jpeg", "png", "gif", "svg", "webp", "bmp" -> "🖼️"

            // Archives
            "zip", "rar", "7z", "tar", "gz", "bz2" -> "📦"

            // Code files
            "js", "ts", "jsx", "tsx" -> "💛"
            "kt", "kts" -> "💜"
            "java" -> "☕"
            "py" -> "🐍"
            "go" -> "🐹"
            "rs" -> "🦀"
            "c", "cpp", "cc", "h", "hpp" -> "⚙️"
            "html", "htm", "css" -> "🌐"
            "json", "xml", "yaml", "yml", "toml" -> "📋"

            // Audio
            "mp3", "wav", "flac", "aac", "ogg" -> "🎵"

            // Video
            "mp4", "avi", "mkv", "mov", "wmv", "flv" -> "🎬"

            // Default
            else -> "📎"
        }
    }
}

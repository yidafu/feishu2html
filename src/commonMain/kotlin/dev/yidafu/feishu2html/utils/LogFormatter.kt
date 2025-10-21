package dev.yidafu.feishu2html.utils

/**
 * ANSI color codes for terminal output
 */
object AnsiColor {
    const val RESET = "\u001B[0m"
    const val BOLD = "\u001B[1m"
    const val DIM = "\u001B[2m"

    // Foreground colors
    const val BLACK = "\u001B[30m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val MAGENTA = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"

    // Bright foreground colors
    const val BRIGHT_BLACK = "\u001B[90m"
    const val BRIGHT_RED = "\u001B[91m"
    const val BRIGHT_GREEN = "\u001B[92m"
    const val BRIGHT_YELLOW = "\u001B[93m"
    const val BRIGHT_BLUE = "\u001B[94m"
    const val BRIGHT_MAGENTA = "\u001B[95m"
    const val BRIGHT_CYAN = "\u001B[96m"
    const val BRIGHT_WHITE = "\u001B[97m"

    // Background colors
    const val BG_BLACK = "\u001B[40m"
    const val BG_RED = "\u001B[41m"
    const val BG_GREEN = "\u001B[42m"
    const val BG_YELLOW = "\u001B[43m"
    const val BG_BLUE = "\u001B[44m"
    const val BG_MAGENTA = "\u001B[45m"
    const val BG_CYAN = "\u001B[46m"
    const val BG_WHITE = "\u001B[47m"
}

/**
 * Icon set for visual logging
 */
object LogIcons {
    const val SUCCESS = "✓"
    const val ERROR = "✗"
    const val WARNING = "⚠"
    const val INFO = "ℹ"
    const val DEBUG = "⚙"
    const val DOWNLOAD = "⬇"
    const val UPLOAD = "⬆"
    const val PROCESSING = "⚡"
    const val DOCUMENT = "📄"
    const val IMAGE = "🖼"
    const val FILE = "📎"
    const val FOLDER = "📁"
    const val CLOCK = "⏱"
    const val CHECK = "✔"
    const val CROSS = "✖"
    const val ARROW_RIGHT = "→"
    const val ARROW_DOWN = "↓"
    const val BULLET = "•"
    const val STAR = "★"
}

/**
 * Enhanced log formatter with visual improvements
 */
object LogFormatter {
    private var useColors = true

    /**
     * Enable or disable ANSI color codes
     */
    fun setColorEnabled(enabled: Boolean) {
        useColors = enabled
    }

    /**
     * Apply color to text if colors are enabled
     */
    private fun colorize(text: String, color: String): String {
        return if (useColors) "$color$text${AnsiColor.RESET}" else text
    }

    /**
     * Format a success message
     */
    fun success(message: String): String {
        return colorize("${LogIcons.SUCCESS} ", AnsiColor.BRIGHT_GREEN) +
               colorize(message, AnsiColor.GREEN)
    }

    /**
     * Format an error message
     */
    fun error(message: String): String {
        return colorize("${LogIcons.ERROR} ", AnsiColor.BRIGHT_RED) +
               colorize(message, AnsiColor.RED)
    }

    /**
     * Format a warning message
     */
    fun warning(message: String): String {
        return colorize("${LogIcons.WARNING} ", AnsiColor.BRIGHT_YELLOW) +
               colorize(message, AnsiColor.YELLOW)
    }

    /**
     * Format an info message
     */
    fun info(message: String): String {
        return colorize("${LogIcons.INFO} ", AnsiColor.BRIGHT_CYAN) +
               colorize(message, AnsiColor.CYAN)
    }

    /**
     * Format a debug message
     */
    fun debug(message: String): String {
        return colorize("${LogIcons.DEBUG} ", AnsiColor.BRIGHT_BLACK) +
               colorize(message, AnsiColor.BRIGHT_BLACK)
    }

    /**
     * Format a processing/action message
     */
    fun processing(message: String): String {
        return colorize("${LogIcons.PROCESSING} ", AnsiColor.BRIGHT_YELLOW) +
               colorize(message, AnsiColor.WHITE)
    }

    /**
     * Create a section header
     */
    fun section(title: String): String {
        val line = "═".repeat(60)
        return buildString {
            appendLine()
            appendLine(colorize(line, AnsiColor.BRIGHT_BLUE))
            appendLine(colorize("  $title", AnsiColor.BOLD + AnsiColor.BRIGHT_BLUE))
            appendLine(colorize(line, AnsiColor.BRIGHT_BLUE))
        }
    }

    /**
     * Create a subsection header
     */
    fun subsection(title: String): String {
        val line = "─".repeat(50)
        return buildString {
            appendLine()
            appendLine(colorize("  $line", AnsiColor.BLUE))
            appendLine(colorize("  $title", AnsiColor.BOLD + AnsiColor.BLUE))
            appendLine(colorize("  $line", AnsiColor.BLUE))
        }
    }

    /**
     * Format a key-value pair
     */
    fun keyValue(key: String, value: String, icon: String = LogIcons.BULLET): String {
        return "  $icon ${colorize(key, AnsiColor.BRIGHT_WHITE)}: ${colorize(value, AnsiColor.WHITE)}"
    }

    /**
     * Format a progress bar
     */
    fun progressBar(current: Int, total: Int, width: Int = 40): String {
        val percentage = if (total > 0) (current * 100.0 / total).toInt() else 0
        val filled = (current * width / total).coerceIn(0, width)
        val empty = width - filled

        val bar = colorize("█".repeat(filled), AnsiColor.BRIGHT_GREEN) +
                  colorize("░".repeat(empty), AnsiColor.BRIGHT_BLACK)

        val stats = colorize("$current/$total", AnsiColor.BRIGHT_WHITE)
        val pct = colorize("${percentage}%", AnsiColor.BRIGHT_CYAN)

        return "  [$bar] $stats ($pct)"
    }

    /**
     * Create a simple table
     */
    fun table(headers: List<String>, rows: List<List<String>>): String {
        if (headers.isEmpty() || rows.isEmpty()) return ""

        // Calculate column widths
        val columnWidths = headers.mapIndexed { index, header ->
            maxOf(header.length, rows.maxOfOrNull { it.getOrNull(index)?.length ?: 0 } ?: 0)
        }

        return buildString {
            // Header row
            appendLine()
            append("  ")
            headers.forEachIndexed { index, header ->
                append(colorize(header.padEnd(columnWidths[index] + 2), AnsiColor.BOLD + AnsiColor.BRIGHT_CYAN))
            }
            appendLine()

            // Separator
            append("  ")
            columnWidths.forEach { width ->
                append(colorize("─".repeat(width + 2), AnsiColor.BRIGHT_BLACK))
            }
            appendLine()

            // Data rows
            rows.forEach { row ->
                append("  ")
                row.forEachIndexed { index, cell ->
                    append(colorize(cell.padEnd(columnWidths[index] + 2), AnsiColor.WHITE))
                }
                appendLine()
            }
        }
    }

    /**
     * Format elapsed time
     */
    fun duration(milliseconds: Long): String {
        val seconds = milliseconds / 1000.0
        return when {
            seconds < 1 -> colorize("${milliseconds}ms", AnsiColor.GREEN)
            seconds < 60 -> colorize("${"%.2f".format(seconds)}s", AnsiColor.CYAN)
            else -> {
                val minutes = (seconds / 60).toInt()
                val secs = (seconds % 60).toInt()
                colorize("${minutes}m ${secs}s", AnsiColor.YELLOW)
            }
        }
    }

    /**
     * Format file size
     */
    fun fileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> colorize("${bytes}B", AnsiColor.WHITE)
            bytes < 1024 * 1024 -> colorize("${"%.2f".format(bytes / 1024.0)}KB", AnsiColor.CYAN)
            bytes < 1024 * 1024 * 1024 -> colorize("${"%.2f".format(bytes / (1024.0 * 1024.0))}MB", AnsiColor.BLUE)
            else -> colorize("${"%.2f".format(bytes / (1024.0 * 1024.0 * 1024.0))}GB", AnsiColor.MAGENTA)
        }
    }

    /**
     * Create a banner with borders
     */
    fun banner(text: String): String {
        val width = maxOf(text.length + 4, 60)
        val topBottom = "═".repeat(width)
        val padding = " ".repeat((width - text.length - 2) / 2)

        return buildString {
            appendLine()
            appendLine(colorize("╔$topBottom╗", AnsiColor.BRIGHT_MAGENTA))
            appendLine(colorize("║$padding$text$padding║", AnsiColor.BOLD + AnsiColor.BRIGHT_MAGENTA))
            appendLine(colorize("╚$topBottom╝", AnsiColor.BRIGHT_MAGENTA))
        }
    }

    /**
     * Format document export start
     */
    fun documentStart(documentId: String, title: String): String {
        return buildString {
            appendLine()
            appendLine(colorize("╭─────────────────────────────────────────────────╮", AnsiColor.BRIGHT_CYAN))
            appendLine(colorize("│ ${LogIcons.DOCUMENT} Exporting Document", AnsiColor.BRIGHT_CYAN))
            appendLine(colorize("├─────────────────────────────────────────────────┤", AnsiColor.BRIGHT_CYAN))
            appendLine("│ ${keyValue("Title", title, "")}")
            appendLine("│ ${keyValue("ID", documentId, "")}")
            appendLine(colorize("╰─────────────────────────────────────────────────╯", AnsiColor.BRIGHT_CYAN))
        }
    }

    /**
     * Format document export completion
     */
    fun documentComplete(title: String, duration: Long, stats: Map<String, Int>): String {
        return buildString {
            appendLine()
            appendLine(colorize("╭─────────────────────────────────────────────────╮", AnsiColor.BRIGHT_GREEN))
            appendLine(colorize("│ ${LogIcons.SUCCESS} Export Completed", AnsiColor.BRIGHT_GREEN))
            appendLine(colorize("├─────────────────────────────────────────────────┤", AnsiColor.BRIGHT_GREEN))
            appendLine("│ ${keyValue("Document", title, "")}")
            appendLine("│ ${keyValue("Duration", duration(duration), "")}")
            stats.forEach { (key, value) ->
                appendLine("│ ${keyValue(key, value.toString(), "")}")
            }
            appendLine(colorize("╰─────────────────────────────────────────────────╯", AnsiColor.BRIGHT_GREEN))
        }
    }
}


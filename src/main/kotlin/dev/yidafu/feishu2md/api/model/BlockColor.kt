package dev.yidafu.feishu2md.api.model

/**
 * 块颜色枚举（用于背景色、边框色、文字颜色）
 * 参考: https://open.feishu.cn/document/docs/docs/data-structure/block
 */
enum class BlockColor(val code: Int, val lightColor: String, val darkColor: String) {
    RED(1, "#fee2e2", "#ef4444"),
    YELLOW(2, "#fef3c7", "#f59e0b"),
    GREEN(3, "#d1fae5", "#10b981"),
    BLUE(4, "#dbeafe", "#3b82f6"),
    INDIGO(5, "#e0e7ff", "#6366f1"),
    PURPLE(6, "#f3e8ff", "#a855f7"),
    PINK(7, "#ffe4e6", "#ec4899"),
    GRAY(8, "#f5f5f5", "#6b7280"),
    ;

    companion object {
        /**
         * 根据颜色代码获取浅色（用于背景）
         */
        fun getLightColor(code: Int?): String? {
            if (code == null) return null
            return entries.firstOrNull { it.code == code }?.lightColor
        }

        /**
         * 根据颜色代码获取深色（用于文字/边框）
         */
        fun getDarkColor(code: Int?): String? {
            if (code == null) return null
            return entries.firstOrNull { it.code == code }?.darkColor
        }

        /**
         * 根据颜色代码获取CSS class名称
         */
        fun getColorClass(code: Int?): String? {
            if (code == null) return null
            return entries.firstOrNull { it.code == code }?.name?.lowercase()
        }
    }
}

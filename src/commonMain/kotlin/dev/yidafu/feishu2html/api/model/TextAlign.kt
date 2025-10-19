package dev.yidafu.feishu2html.api.model

/**
 * 文本对齐方式枚举
 * 参考: https://open.feishu.cn/document/docs/docs/data-structure/block
 */
internal enum class TextAlign(val code: Int, val cssValue: String) {
    LEFT(1, "left"),
    CENTER(2, "center"),
    RIGHT(3, "right"),
    ;

    companion object {
        fun fromCode(code: Int?): TextAlign? {
            if (code == null) return null
            return entries.firstOrNull { it.code == code }
        }
    }
}

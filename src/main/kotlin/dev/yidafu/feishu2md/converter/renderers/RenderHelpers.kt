package dev.yidafu.feishu2md.converter.renderers

import dev.yidafu.feishu2md.api.model.*

/**
 * 渲染辅助函数 - 所有Renderer共享
 */

fun getAlignClass(align: Int?): String {
    return when (TextAlign.fromCode(align)) {
        TextAlign.CENTER -> "text-align-center"
        TextAlign.RIGHT -> "text-align-right"
        else -> ""
    }
}

fun getLanguageName(code: Int?): String {
    return CodeLanguage.fromCode(code)
}

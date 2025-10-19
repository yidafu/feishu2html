package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*

/**
 * Rendering helper functions - shared by all Renderers
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

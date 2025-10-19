package dev.yidafu.feishu2html.api.model

/**
 * Iframe embed type enum
 *
 * Defines iframe_type as per Feishu API documentation
 * Reference: https://open.feishu.cn/document/docs/docs/data-structure/block#f7b07e0c
 */
internal enum class IframeType(val typeCode: Int, val displayName: String) {
    BILIBILI(1, "Bilibili"),
    AIRTABLE(3, "Airtable"),
    YOUKU(5, "Youku"),
    YOUTUBE(6, "YouTube"),
    FIGMA(7, "Figma"),
    MODAO(8, "Modao"),
    CANVA(9, "Canva"),
    CODEPEN(10, "CodePen"),
    FEISHU_DOCS(11, "Feishu Docs"),
    FEISHU_SHEET(12, "Feishu Sheet"),
    FEISHU_BITABLE(15, "Feishu Bitable"),
    FEISHU_BOARD(17, "Feishu Board"),
    INVISION(18, "InVision"),
    LANHU(21, "Lanhu"),
    PROCESSON(24, "ProcessOn"),
    MODIAN(28, "Modian"),
    AXURE(31, "Axure"),
    XIAOPENG(36, "XPeng"),
    GENERIC(99, "Generic Embed"), // Custom iframe
    UNDEFINED(999, "Unknown"),
    ;

    companion object {
        /**
         * Get IframeType from type code
         */
        fun fromCode(code: Int): IframeType {
            return entries.firstOrNull { it.typeCode == code } ?: UNDEFINED
        }
    }
}

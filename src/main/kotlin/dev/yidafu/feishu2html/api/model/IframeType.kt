package dev.yidafu.feishu2html.api.model

/**
 * Iframe 嵌入类型枚举
 *
 * 根据飞书 API 文档定义的 iframe_type 类型
 * 参考：https://open.feishu.cn/document/docs/docs/data-structure/block#f7b07e0c
 */
internal enum class IframeType(val typeCode: Int, val displayName: String) {
    BILIBILI(1, "哔哩哔哩视频"),
    AIRTABLE(3, "Airtable"),
    YOUKU(5, "优酷视频"),
    YOUTUBE(6, "YouTube"),
    FIGMA(7, "Figma"),
    MODAO(8, "墨刀"),
    CANVA(9, "Canva"),
    CODEPEN(10, "CodePen"),
    FEISHU_DOCS(11, "飞书文档"),
    FEISHU_SHEET(12, "飞书表格"),
    FEISHU_BITABLE(15, "飞书多维表格"),
    FEISHU_BOARD(17, "飞书白板"),
    INVISION(18, "InVision"),
    LANHU(21, "蓝湖"),
    PROCESSON(24, "ProcessOn"),
    MODIAN(28, "摩点"),
    AXURE(31, "Axure"),
    XIAOPENG(36, "小鹏汽车"),
    GENERIC(99, "通用嵌入"), // 自定义iframe
    UNDEFINED(999, "未知类型"),
    ;

    companion object {
        /**
         * 根据类型代码获取 IframeType
         */
        fun fromCode(code: Int): IframeType {
            return entries.firstOrNull { it.typeCode == code } ?: UNDEFINED
        }
    }
}

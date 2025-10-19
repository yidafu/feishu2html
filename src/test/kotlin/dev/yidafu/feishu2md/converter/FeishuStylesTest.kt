package dev.yidafu.feishu2md.converter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.string.shouldContain

class FeishuStylesTest : FunSpec({

    test("应该生成完整的CSS") {
        val css = FeishuStyles.generateCSS()

        css.length shouldBeGreaterThan 100
        css shouldContain "body"
        css shouldContain "font-family"
    }

    test("CSS应该包含基础样式") {
        val css = FeishuStyles.generateCSS()

        css shouldContain "body"
        css shouldContain "margin"
        css shouldContain "padding"
    }

    test("CSS应该包含标题样式") {
        val css = FeishuStyles.generateCSS()

        css shouldContain "h1"
        css shouldContain "h2"
        css shouldContain "h3"
    }

    test("CSS应该包含列表样式") {
        val css = FeishuStyles.generateCSS()

        css shouldContain "ul"
        css shouldContain "ol"
        css shouldContain "li"
    }

    test("CSS应该包含代码块样式") {
        val css = FeishuStyles.generateCSS()

        css shouldContain "pre"
        css shouldContain "code"
    }

    test("CSS应该包含表格样式") {
        val css = FeishuStyles.generateCSS()

        css shouldContain "table"
        css shouldContain "td"
        css shouldContain "th"
    }

    test("CSS应该包含Callout样式") {
        val css = FeishuStyles.generateCSS()

        css shouldContain "callout"
    }

    test("CSS应该包含颜色类") {
        val css = FeishuStyles.generateCSS()

        // 应该包含一些颜色相关的样式
        css.length shouldBeGreaterThan 500
    }

    test("CSS应该包含对齐类") {
        val css = FeishuStyles.generateCSS()

        css shouldContain "text-align"
    }

    test("CSS应该包含Grid布局样式") {
        val css = FeishuStyles.generateCSS()

        css shouldContain "grid"
    }
})


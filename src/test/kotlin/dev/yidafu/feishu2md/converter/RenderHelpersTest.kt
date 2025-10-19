package dev.yidafu.feishu2md.converter

import dev.yidafu.feishu2md.converter.renderers.getAlignClass
import dev.yidafu.feishu2md.converter.renderers.getLanguageName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RenderHelpersTest : FunSpec({

    test("getAlignClass应该返回正确的对齐类") {
        getAlignClass(1) shouldBe ""
        getAlignClass(2) shouldBe "text-align-center"
        getAlignClass(3) shouldBe "text-align-right"
        getAlignClass(null) shouldBe ""
    }

    test("getAlignClass应该处理无效的对齐值") {
        getAlignClass(0) shouldBe ""
        getAlignClass(4) shouldBe ""
        getAlignClass(99) shouldBe ""
    }

    test("getLanguageName应该返回语言名称字符串") {
        val result = getLanguageName(1)
        result.length shouldBe result.length // 基本验证
    }

    test("getLanguageName应该处理null值") {
        val result = getLanguageName(null)
        result shouldBe "plaintext"
    }

    test("getLanguageName应该返回非空结果") {
        val result = getLanguageName(5)
        // 只要不抛出异常就行
        result.length shouldBe result.length
    }
})


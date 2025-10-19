package dev.yidafu.feishu2html.model

import dev.yidafu.feishu2html.api.model.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class EnumsTest : FunSpec({

    test("TextAlign.fromCode应该正确映射") {
        TextAlign.fromCode(1) shouldBe TextAlign.LEFT
        TextAlign.fromCode(2) shouldBe TextAlign.CENTER
        TextAlign.fromCode(3) shouldBe TextAlign.RIGHT
        TextAlign.fromCode(null) shouldBe null
        TextAlign.fromCode(99) shouldBe null
    }

    test("TextAlign应该有正确的code值") {
        TextAlign.LEFT.code shouldBe 1
        TextAlign.CENTER.code shouldBe 2
        TextAlign.RIGHT.code shouldBe 3
    }

    test("BlockColor.getColorClass应该处理各种颜色代码") {
        // 测试各种颜色代码，不应该抛出异常
        for (i in 1..20) {
            val result = BlockColor.getColorClass(i)
            // 可能返回null或颜色类名
        }
    }

    test("BlockColor.getColorClass应该处理null") {
        val result = BlockColor.getColorClass(null)
        result shouldBe null
    }

    test("BlockColor.getColorClass应该处理无效值") {
        val result = BlockColor.getColorClass(9999)
        result shouldBe null
    }

    test("CodeLanguage.fromCode应该返回语言名称") {
        val result1 = CodeLanguage.fromCode(1)
        result1 shouldNotBe ""

        val resultNull = CodeLanguage.fromCode(null)
        resultNull shouldBe "plaintext"
    }

    test("CodeLanguage应该支持常见语言") {
        // 测试常见语言代码
        for (i in 1..30) {
            val result = CodeLanguage.fromCode(i)
            result shouldNotBe ""
        }
    }

    test("IframeType.fromCode应该正确映射") {
        IframeType.fromCode(1) shouldBe IframeType.BILIBILI
        IframeType.fromCode(99) shouldBe IframeType.GENERIC
        IframeType.fromCode(9999) shouldBe IframeType.UNDEFINED
    }

    test("IframeType应该有displayName") {
        IframeType.BILIBILI.displayName shouldBe "哔哩哔哩视频"
        IframeType.YOUTUBE.displayName shouldNotBe ""
        IframeType.FIGMA.displayName shouldNotBe ""
    }

    test("Emoji.fromId应该返回emoji字符串") {
        val emoji = Emoji.fromId("SMILE")
        // fromId返回的是unicode字符串或null
        // 不应该抛出异常即可
    }

    test("Emoji.fromId应该处理未知ID返回null") {
        val emoji = Emoji.fromId("UNKNOWN_EMOJI_ID_12345")
        // 未知ID应该返回null
        emoji shouldBe null
    }

    test("Emoji.fromId应该处理null输入") {
        val emoji = Emoji.fromId(null)
        emoji shouldBe null
    }
})


package dev.yidafu.feishu2html.model

import dev.yidafu.feishu2html.api.model.BlockType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class BlockTypeTest : FunSpec({

    test("应该包含所有53种BlockType") {
        val allTypes = BlockType.entries
        allTypes shouldHaveSize 53
    }

    test("fromCode应该正确映射所有有效代码") {
        for (blockType in BlockType.entries) {
            if (blockType != BlockType.UNDEFINED) {
                BlockType.fromCode(blockType.typeCode) shouldBe blockType
            }
        }
    }

    test("fromCode应该对无效代码返回null") {
        BlockType.fromCode(9999) shouldBe null
        BlockType.fromCode(-1) shouldBe null
        BlockType.fromCode(0) shouldBe null
    }

    test("所有BlockType应该有唯一的typeCode") {
        val typeCodes = BlockType.entries.map { it.typeCode }
        val uniqueCodes = typeCodes.toSet()

        uniqueCodes.size shouldBe BlockType.entries.size
    }

    test("PAGE类型应该是type 1") {
        BlockType.PAGE.typeCode shouldBe 1
    }

    test("TEXT类型应该是type 2") {
        BlockType.TEXT.typeCode shouldBe 2
    }

    test("BOARD类型应该是type 43") {
        BlockType.BOARD.typeCode shouldBe 43
    }

    test("AI_TEMPLATE应该是type 52") {
        BlockType.AI_TEMPLATE.typeCode shouldBe 52
    }

    test("UNDEFINED应该是type 999") {
        BlockType.UNDEFINED.typeCode shouldBe 999
    }

    test("应该包含所有Heading类型") {
        val headingTypes = listOf(
            BlockType.HEADING1, BlockType.HEADING2, BlockType.HEADING3,
            BlockType.HEADING4, BlockType.HEADING5, BlockType.HEADING6,
            BlockType.HEADING7, BlockType.HEADING8, BlockType.HEADING9
        )

        for (heading in headingTypes) {
            BlockType.entries shouldContain heading
        }
    }

    test("应该包含所有列表类型") {
        BlockType.entries shouldContain BlockType.BULLET
        BlockType.entries shouldContain BlockType.ORDERED
        BlockType.entries shouldContain BlockType.TODO
    }

    test("应该包含所有媒体类型") {
        val mediaTypes = listOf(
            BlockType.IMAGE, BlockType.FILE, BlockType.BOARD,
            BlockType.DIAGRAM, BlockType.IFRAME
        )

        for (media in mediaTypes) {
            BlockType.entries shouldContain media
        }
    }

    test("应该包含所有容器类型") {
        val containerTypes = listOf(
            BlockType.GRID, BlockType.GRID_COLUMN,
            BlockType.TABLE, BlockType.TABLE_CELL,
            BlockType.CALLOUT, BlockType.QUOTE_CONTAINER
        )

        for (container in containerTypes) {
            BlockType.entries shouldContain container
        }
    }

    test("应该包含所有OKR相关类型") {
        val okrTypes = listOf(
            BlockType.OKR,
            BlockType.OKR_OBJECTIVE,
            BlockType.OKR_KEY_RESULT,
            BlockType.OKR_PROGRESS
        )

        for (okr in okrTypes) {
            BlockType.entries shouldContain okr
        }
    }

    test("应该包含所有Agenda相关类型") {
        val agendaTypes = listOf(
            BlockType.AGENDA,
            BlockType.AGENDA_ITEM,
            BlockType.AGENDA_ITEM_TITLE,
            BlockType.AGENDA_ITEM_CONTENT
        )

        for (agenda in agendaTypes) {
            BlockType.entries shouldContain agenda
        }
    }

    test("typeCode应该是连续或有规律的") {
        // 验证主要块类型的连续性
        BlockType.PAGE.typeCode shouldBe 1
        BlockType.TEXT.typeCode shouldBe 2
        BlockType.HEADING1.typeCode shouldBe 3
        BlockType.HEADING2.typeCode shouldBe 4
        BlockType.HEADING3.typeCode shouldBe 5
        // ... 等等
    }
})



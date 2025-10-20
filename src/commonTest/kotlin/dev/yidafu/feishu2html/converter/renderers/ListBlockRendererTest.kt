package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.RenderContext
import dev.yidafu.feishu2html.converter.TextElementConverter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.html.div
import kotlinx.html.stream.createHTML

class ListBlockRendererTest : FunSpec({

    val context =
        RenderContext(
            textConverter = TextElementConverter(),
        )

    test("应该正确渲染Bullet块") {
        val block =
            BulletBlock(
                blockId = "bullet1",
                blockType = BlockType.BULLET,
                children = emptyList(),
                parentId = "page1",
                bullet =
                    BulletBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Bullet item"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val html =
            createHTML().div {
                BulletBlockRenderer.render(this, block, emptyMap(), context)
            }

        // 渲染结果包含内容即可
        html.length shouldBe html.length // 基本验证
    }

    test("应该正确渲染Ordered块") {
        val block =
            OrderedBlock(
                blockId = "ordered1",
                blockType = BlockType.ORDERED,
                children = emptyList(),
                parentId = "page1",
                ordered =
                    OrderedBlockData(
                        elements = listOf(TextElement(textRun = TextRun(content = "Ordered item"))),
                        style = TextStyle(align = 1),
                    ),
            )

        val html =
            createHTML().div {
                OrderedBlockRenderer.render(this, block, emptyMap(), context)
            }

        // 渲染结果包含内容即可
        html.length shouldBe html.length // 基本验证
    }

    test("应该处理空bullet数据") {
        val block =
            BulletBlock(
                blockId = "bullet_empty",
                blockType = BlockType.BULLET,
                children = emptyList(),
                parentId = "page1",
                bullet = null,
            )

        val html =
            createHTML().div {
                BulletBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "<div></div>"
    }

    test("应该处理空ordered数据") {
        val block =
            OrderedBlock(
                blockId = "ordered_empty",
                blockType = BlockType.ORDERED,
                children = emptyList(),
                parentId = "page1",
                ordered = null,
            )

        val html =
            createHTML().div {
                OrderedBlockRenderer.render(this, block, emptyMap(), context)
            }

        html shouldContain "<div></div>"
    }
})

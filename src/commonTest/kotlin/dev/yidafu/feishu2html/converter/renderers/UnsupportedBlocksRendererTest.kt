package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.RenderContext
import dev.yidafu.feishu2html.converter.TextElementConverter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.html.div
import kotlinx.html.stream.createHTML

class UnsupportedBlocksRendererTest : FunSpec({

    val context =
        RenderContext(
            textConverter = TextElementConverter(),
        )

    test("应该渲染Bitable块占位符") {
        val block =
            BitableBlock(
                blockId = "bitable1",
                blockType = BlockType.BITABLE,
                children = emptyList(),
                parentId = "page1",
                bitable = null,
            )

        val html =
            createHTML().div {
                BitableBlockRenderer.render(this, block, emptyMap(), context)
            }

        html.length shouldBe html.length
    }

    test("应该渲染ChatCard块占位符") {
        val block =
            ChatCardBlock(
                blockId = "chatcard1",
                blockType = BlockType.CHAT_CARD,
                children = emptyList(),
                parentId = "page1",
                chatCard = null,
            )

        val html =
            createHTML().div {
                ChatCardBlockRenderer.render(this, block, emptyMap(), context)
            }

        html.length shouldBe html.length
    }

    test("应该渲染Isv块占位符") {
        val block =
            IsvBlock(
                blockId = "isv1",
                blockType = BlockType.ISV,
                children = emptyList(),
                parentId = "page1",
            )

        val html =
            createHTML().div {
                IsvBlockRenderer.render(this, block, emptyMap(), context)
            }

        html.length shouldBe html.length
    }

    test("应该渲染Mindnote块占位符") {
        val block =
            MindnoteBlock(
                blockId = "mindnote1",
                blockType = BlockType.MINDNOTE,
                children = emptyList(),
                parentId = "page1",
            )

        val html =
            createHTML().div {
                MindnoteBlockRenderer.render(this, block, emptyMap(), context)
            }

        html.length shouldBe html.length
    }

    test("应该渲染Sheet块占位符") {
        val block =
            SheetBlock(
                blockId = "sheet1",
                blockType = BlockType.SHEET,
                children = emptyList(),
                parentId = "page1",
            )

        val html =
            createHTML().div {
                SheetBlockRenderer.render(this, block, emptyMap(), context)
            }

        html.length shouldBe html.length
    }

    test("应该渲染View块占位符") {
        val block =
            ViewBlock(
                blockId = "view1",
                blockType = BlockType.VIEW,
                children = emptyList(),
                parentId = "page1",
            )

        val html =
            createHTML().div {
                ViewBlockRenderer.render(this, block, emptyMap(), context)
            }

        html.length shouldBe html.length
    }

    test("应该渲染Task块占位符") {
        val block =
            TaskBlock(
                blockId = "task1",
                blockType = BlockType.TASK,
                children = emptyList(),
                parentId = "page1",
            )

        val html =
            createHTML().div {
                TaskBlockRenderer.render(this, block, emptyMap(), context)
            }

        html.length shouldBe html.length
    }

    test("应该渲染Okr块占位符") {
        val block =
            OkrBlock(
                blockId = "okr1",
                blockType = BlockType.OKR,
                children = emptyList(),
                parentId = "page1",
            )

        val html =
            createHTML().div {
                OkrBlockRenderer.render(this, block, emptyMap(), context)
            }

        html.length shouldBe html.length
    }

    test("应该渲染AddOns块占位符") {
        val block =
            AddOnsBlock(
                blockId = "addons1",
                blockType = BlockType.ADD_ONS,
                children = emptyList(),
                parentId = "page1",
                addOns = null,
            )

        val html =
            createHTML().div {
                AddOnsBlockRenderer.render(this, block, emptyMap(), context)
            }

        html.length shouldBe html.length
    }

    test("应该渲染JiraIssue块占位符") {
        val block =
            JiraIssueBlock(
                blockId = "jira1",
                blockType = BlockType.JIRA_ISSUE,
                children = emptyList(),
                parentId = "page1",
            )

        val html =
            createHTML().div {
                JiraIssueBlockRenderer.render(this, block, emptyMap(), context)
            }

        html.length shouldBe html.length
    }

    test("应该渲染WikiCatalog块占位符") {
        val block =
            WikiCatalogBlock(
                blockId = "wiki1",
                blockType = BlockType.WIKI_CATALOG,
                children = emptyList(),
                parentId = "page1",
            )

        val html =
            createHTML().div {
                WikiCatalogBlockRenderer.render(this, block, emptyMap(), context)
            }

        html.length shouldBe html.length
    }

    test("应该渲染Agenda块占位符") {
        val block =
            AgendaBlock(
                blockId = "agenda1",
                blockType = BlockType.AGENDA,
                children = emptyList(),
                parentId = "page1",
            )

        val html =
            createHTML().div {
                AgendaBlockRenderer.render(this, block, emptyMap(), context)
            }

        html.length shouldBe html.length
    }
})

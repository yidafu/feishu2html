package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("dev.yidafu.feishu2html.converter.renderers.UnsupportedBlocksRenderer")

private fun renderUnsupportedBlock(parent: FlowContent, blockType: String, block: Block) {
    logger.warn("Rendering unsupported block type: {} (block_id: {})", blockType, block.blockId)
    parent.div(classes = "unsupported-block") {
        +"[暂不支持的Block类型: $blockType]"
    }
}

object IsvBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "ISV", block as Block)
    }
}

object MindnoteBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Mindnote", block as Block)
    }
}

object SheetBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Sheet", block as Block)
    }
}

object ViewBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "View", block as Block)
    }
}

object TaskBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "任务", block as Block)
    }
}

object OkrBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "OKR", block as Block)
    }
}

object OkrObjectiveBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "OKR Objective", block as Block)
    }
}

object OkrKeyResultBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "OKR Key Result", block as Block)
    }
}

object OkrProgressBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "OKR Progress", block as Block)
    }
}

object AddOnsBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "新版文档小组件", block as Block)
    }
}

object JiraIssueBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Jira问题", block as Block)
    }
}

object WikiCatalogBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Wiki子页面列表(旧版)", block as Block)
    }
}

object AgendaBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "议程", block as Block)
    }
}

object AgendaItemBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "议程项", block as Block)
    }
}

object AgendaItemTitleBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "议程项标题", block as Block)
    }
}

object AgendaItemContentBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "议程项内容", block as Block)
    }
}

object LinkPreviewBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "链接预览", block as Block)
    }
}

object SourceSyncedBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "源同步块", block as Block)
    }
}

object ReferenceSyncedBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "引用同步块", block as Block)
    }
}

object SubPageListBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Wiki子页面列表(新版)", block as Block)
    }
}

object AiTemplateBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "AI模板", block as Block)
    }
}

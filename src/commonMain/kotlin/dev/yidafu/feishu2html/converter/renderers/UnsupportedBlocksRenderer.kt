package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * PageBlock Renderer - Page blocks are container blocks that don't render themselves
 */
internal object PageBlockRenderer : Renderable<PageBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<PageBlock>,
        context: RenderContext,
    ) {
        // PageBlock is a container but doesn't render itself
        // Its children are rendered by the main buildBody loop
    }
}

private fun renderUnsupportedBlock(
    parent: FlowContent,
    blockType: String,
    block: Block,
    context: RenderContext,
) {
    logger.warn { "Rendering unsupported block type: $blockType (block_id: ${block.blockId})" }

    // Only render if showUnsupportedBlocks is enabled
    if (context.showUnsupportedBlocks) {
        parent.div(classes = "unsupported-block") {
            +"[Unsupported block type: $blockType]"
        }
    }
}

internal object IsvBlockRenderer : Renderable<IsvBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<IsvBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "ISV", blockNode.data, context)
    }
}

internal object MindnoteBlockRenderer : Renderable<MindnoteBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<MindnoteBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Mindnote", blockNode.data, context)
    }
}

internal object SheetBlockRenderer : Renderable<SheetBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<SheetBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Sheet", blockNode.data, context)
    }
}

internal object ViewBlockRenderer : Renderable<ViewBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<ViewBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "View", blockNode.data, context)
    }
}

internal object TaskBlockRenderer : Renderable<TaskBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<TaskBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Task", blockNode.data, context)
    }
}

internal object OkrBlockRenderer : Renderable<OkrBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<OkrBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "OKR", blockNode.data, context)
    }
}

internal object OkrObjectiveBlockRenderer : Renderable<OkrObjectiveBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<OkrObjectiveBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "OKR Objective", blockNode.data, context)
    }
}

internal object OkrKeyResultBlockRenderer : Renderable<OkrKeyResultBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<OkrKeyResultBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "OKR Key Result", blockNode.data, context)
    }
}

internal object OkrProgressBlockRenderer : Renderable<OkrProgressBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<OkrProgressBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "OKR Progress", blockNode.data, context)
    }
}

internal object AddOnsBlockRenderer : Renderable<AddOnsBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<AddOnsBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Add-ons", blockNode.data, context)
    }
}

internal object JiraIssueBlockRenderer : Renderable<JiraIssueBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<JiraIssueBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Jira Issue", blockNode.data, context)
    }
}

internal object WikiCatalogBlockRenderer : Renderable<WikiCatalogBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<WikiCatalogBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Wiki Catalog (Legacy)", blockNode.data, context)
    }
}

internal object AgendaBlockRenderer : Renderable<AgendaBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<AgendaBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Agenda", blockNode.data, context)
    }
}

internal object AgendaItemBlockRenderer : Renderable<AgendaItemBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<AgendaItemBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Agenda Item", blockNode.data, context)
    }
}

internal object AgendaItemTitleBlockRenderer : Renderable<AgendaItemTitleBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<AgendaItemTitleBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Agenda Item Title", blockNode.data, context)
    }
}

internal object AgendaItemContentBlockRenderer : Renderable<AgendaItemContentBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<AgendaItemContentBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Agenda Item Content", blockNode.data, context)
    }
}

internal object LinkPreviewBlockRenderer : Renderable<LinkPreviewBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<LinkPreviewBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Link Preview", blockNode.data, context)
    }
}

internal object SourceSyncedBlockRenderer : Renderable<SourceSyncedBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<SourceSyncedBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Source Synced", blockNode.data, context)
    }
}

internal object ReferenceSyncedBlockRenderer : Renderable<ReferenceSyncedBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<ReferenceSyncedBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Reference Synced", blockNode.data, context)
    }
}

internal object SubPageListBlockRenderer : Renderable<SubPageListBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<SubPageListBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "Sub Page List", blockNode.data, context)
    }
}

internal object AiTemplateBlockRenderer : Renderable<AiTemplateBlock> {
    override fun render(
        parent: FlowContent,
        blockNode: BlockNode<AiTemplateBlock>,
        context: RenderContext,
    ) {
        renderUnsupportedBlock(parent, "AI Template", blockNode.data, context)
    }
}

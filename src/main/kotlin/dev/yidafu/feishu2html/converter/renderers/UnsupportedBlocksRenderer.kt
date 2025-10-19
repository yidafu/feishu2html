package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import kotlinx.html.*

object IsvBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: ISV]"
        }
    }
}

object MindnoteBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: Mindnote]"
        }
    }
}

object SheetBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: Sheet]"
        }
    }
}

object ViewBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: View]"
        }
    }
}

object TaskBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: 任务]"
        }
    }
}

object OkrBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: OKR]"
        }
    }
}

object OkrObjectiveBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: OKR Objective]"
        }
    }
}

object OkrKeyResultBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: OKR Key Result]"
        }
    }
}

object OkrProgressBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: OKR Progress]"
        }
    }
}

object AddOnsBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: 新版文档小组件]"
        }
    }
}

object JiraIssueBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: Jira问题]"
        }
    }
}

object WikiCatalogBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: Wiki子页面列表(旧版)]"
        }
    }
}

object AgendaBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: 议程]"
        }
    }
}

object AgendaItemBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: 议程项]"
        }
    }
}

object AgendaItemTitleBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: 议程项标题]"
        }
    }
}

object AgendaItemContentBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: 议程项内容]"
        }
    }
}

object LinkPreviewBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: 链接预览]"
        }
    }
}

object SourceSyncedBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: 源同步块]"
        }
    }
}

object ReferenceSyncedBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: 引用同步块]"
        }
    }
}

object SubPageListBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: Wiki子页面列表(新版)]"
        }
    }
}

object AiTemplateBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        parent.div(classes = "unsupported-block") {
            +"[暂不支持的Block类型: AI模板]"
        }
    }
}

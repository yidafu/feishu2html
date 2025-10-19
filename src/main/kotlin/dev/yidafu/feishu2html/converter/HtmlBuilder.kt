package dev.yidafu.feishu2html.converter

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.renderers.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("dev.yidafu.feishu2html.converter.HtmlBuilder")

/**
 * 全局Block渲染函数 - 根据Block类型分发到对应的Renderer
 *
 * 使用when表达式根据Block的实际类型，分发到对应的Renderer进行渲染。
 * 这个函数是整个渲染系统的入口点。
 *
 * @param block 要渲染的Block对象
 * @param parent kotlinx.html的FlowContent对象
 * @param allBlocks 文档中所有Block的映射表
 * @param context 渲染上下文
 *
 * @see Renderable
 */
fun renderBlock(
    block: Block,
    parent: FlowContent,
    allBlocks: Map<String, Block>,
    context: RenderContext,
) {
    logger.debug("Rendering block: type={}, id={}", block::class.simpleName, block.blockId)
    when (block) {
        is PageBlock -> { /* Page块通常不需要渲染 */ }
        is TextBlock -> TextBlockRenderer.render(parent, block, allBlocks, context)
        is Heading1Block -> Heading1BlockRenderer.render(parent, block, allBlocks, context)
        is Heading2Block -> Heading2BlockRenderer.render(parent, block, allBlocks, context)
        is Heading3Block -> Heading3BlockRenderer.render(parent, block, allBlocks, context)
        is Heading4Block -> Heading4BlockRenderer.render(parent, block, allBlocks, context)
        is Heading5Block -> Heading5BlockRenderer.render(parent, block, allBlocks, context)
        is Heading6Block -> Heading6BlockRenderer.render(parent, block, allBlocks, context)
        is Heading7Block -> Heading7BlockRenderer.render(parent, block, allBlocks, context)
        is Heading8Block -> Heading8BlockRenderer.render(parent, block, allBlocks, context)
        is Heading9Block -> Heading9BlockRenderer.render(parent, block, allBlocks, context)
        is BulletBlock -> BulletBlockRenderer.render(parent, block, allBlocks, context)
        is OrderedBlock -> OrderedBlockRenderer.render(parent, block, allBlocks, context)
        is CodeBlockItem -> CodeBlockRenderer.render(parent, block, allBlocks, context)
        is QuoteBlock -> QuoteBlockRenderer.render(parent, block, allBlocks, context)
        is EquationBlock -> EquationBlockRenderer.render(parent, block, allBlocks, context)
        is TodoBlock -> TodoBlockRenderer.render(parent, block, allBlocks, context)
        is BitableBlock -> BitableBlockRenderer.render(parent, block, allBlocks, context)
        is CalloutBlock -> CalloutBlockRenderer.render(parent, block, allBlocks, context)
        is ChatCardBlock -> ChatCardBlockRenderer.render(parent, block, allBlocks, context)
        is DiagramBlock -> DiagramBlockRenderer.render(parent, block, allBlocks, context)
        is DividerBlock -> DividerBlockRenderer.render(parent, block, allBlocks, context)
        is FileBlock -> FileBlockRenderer.render(parent, block, allBlocks, context)
        is GridBlock -> GridBlockRenderer.render(parent, block, allBlocks, context)
        is GridColumnBlock -> GridColumnBlockRenderer.render(parent, block, allBlocks, context)
        is IframeBlock -> IframeBlockRenderer.render(parent, block, allBlocks, context)
        is ImageBlock -> ImageBlockRenderer.render(parent, block, allBlocks, context)
        is TableBlock -> TableBlockRenderer.render(parent, block, allBlocks, context)
        is TableCellBlock -> TableCellBlockRenderer.render(parent, block, allBlocks, context)
        is QuoteContainerBlock -> QuoteContainerBlockRenderer.render(parent, block, allBlocks, context)
        is BoardBlock -> BoardBlockRenderer.render(parent, block, allBlocks, context)
        is IsvBlock -> IsvBlockRenderer.render(parent, block, allBlocks, context)
        is MindnoteBlock -> MindnoteBlockRenderer.render(parent, block, allBlocks, context)
        is SheetBlock -> SheetBlockRenderer.render(parent, block, allBlocks, context)
        is ViewBlock -> ViewBlockRenderer.render(parent, block, allBlocks, context)
        is TaskBlock -> TaskBlockRenderer.render(parent, block, allBlocks, context)
        is OkrBlock -> OkrBlockRenderer.render(parent, block, allBlocks, context)
        is OkrObjectiveBlock -> OkrObjectiveBlockRenderer.render(parent, block, allBlocks, context)
        is OkrKeyResultBlock -> OkrKeyResultBlockRenderer.render(parent, block, allBlocks, context)
        is OkrProgressBlock -> OkrProgressBlockRenderer.render(parent, block, allBlocks, context)
        is AddOnsBlock -> AddOnsBlockRenderer.render(parent, block, allBlocks, context)
        is JiraIssueBlock -> JiraIssueBlockRenderer.render(parent, block, allBlocks, context)
        is WikiCatalogBlock -> WikiCatalogBlockRenderer.render(parent, block, allBlocks, context)
        is AgendaBlock -> AgendaBlockRenderer.render(parent, block, allBlocks, context)
        is AgendaItemBlock -> AgendaItemBlockRenderer.render(parent, block, allBlocks, context)
        is AgendaItemTitleBlock -> AgendaItemTitleBlockRenderer.render(parent, block, allBlocks, context)
        is AgendaItemContentBlock -> AgendaItemContentBlockRenderer.render(parent, block, allBlocks, context)
        is LinkPreviewBlock -> LinkPreviewBlockRenderer.render(parent, block, allBlocks, context)
        is SourceSyncedBlock -> SourceSyncedBlockRenderer.render(parent, block, allBlocks, context)
        is ReferenceSyncedBlock -> ReferenceSyncedBlockRenderer.render(parent, block, allBlocks, context)
        is SubPageListBlock -> SubPageListBlockRenderer.render(parent, block, allBlocks, context)
        is AiTemplateBlock -> AiTemplateBlockRenderer.render(parent, block, allBlocks, context)
        is UnknownBlock -> UnknownBlockRenderer.render(parent, block, allBlocks, context)
    }
}

/**
 * HTML文档构建器
 *
 * 使用kotlinx.html DSL生成完整的HTML文档，包括：
 * - HTML head（元数据、样式、MathJax配置）
 * - Body内容（所有Block的渲染）
 *
 * 采用Renderable + Delegate模式，将每个Block的渲染逻辑委托给专门的Renderer。
 *
 * @property title HTML文档标题
 * @property customCss 自定义CSS样式，如果提供则覆盖默认的Feishu样式
 *
 * @see renderBlock
 * @see FeishuStyles
 */
class HtmlBuilder(
    private val title: String,
    private val customCss: String? = null,
) {
    private val builderLogger = LoggerFactory.getLogger(HtmlBuilder::class.java)

    /**
     * 构建完整的HTML文档
     *
     * 生成包含head和body的完整HTML5文档。
     * head部分包含CSS样式和MathJax配置，body部分渲染所有Block。
     *
     * @param blocks 有序的Block列表（按文档结构排序）
     * @param allBlocks 所有Block的映射表（blockId -> Block）
     * @return 完整的HTML字符串
     */
    fun build(
        blocks: List<Block>,
        allBlocks: Map<String, Block>,
    ): String {
        builderLogger.info("Starting HTML build for document: {}", title)
        builderLogger.debug("Building with {} blocks (total {} in map)", blocks.size, allBlocks.size)
        builderLogger.debug("Using {} CSS", if (customCss != null) "custom" else "default")

        try {
            val html = createHTML().html {
            lang = "zh-CN"

            head {
                meta(charset = "UTF-8")
                meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                title(this@HtmlBuilder.title)
                style {
                    unsafe {
                        raw(customCss ?: FeishuStyles.generateCSS())
                    }
                }
                // MathJax 支持数学公式渲染
                script {
                    src = "https://polyfill.io/v3/polyfill.min.js?features=es6"
                }
                script {
                    attributes["id"] = "MathJax-script"
                    async = true
                    src = "https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"
                }
                script {
                    unsafe {
                        raw(
                            """
                            window.MathJax = {
                                tex: {
                                    inlineMath: [['$', '$'], ['\\(', '\\)']],
                                    displayMath: [['$$', '$$'], ['\\[', '\\]']]
                                },
                                svg: {
                                    fontCache: 'global'
                                }
                            };
                            """.trimIndent(),
                        )
                    }
                }
            }

            body {
                div(classes = "container") {
                    buildBody(blocks, allBlocks, this)
                }
            }
            }

            builderLogger.info("HTML build completed successfully for document: {}", title)
            builderLogger.debug("Generated HTML size: {} characters", html.length)
            return html
        } catch (e: Exception) {
            builderLogger.error("Failed to build HTML for document {}: {}", title, e.message, e)
            throw e
        }
    }

    private fun buildBody(
        blocks: List<Block>,
        allBlocks: Map<String, Block>,
        parent: FlowContent,
    ) {
        var inBulletList = false
        var inOrderedList = false
        var currentList: UL? = null
        var currentOL: OL? = null
        val processedBlocks = mutableSetOf<String>() // 记录已处理的块

        // 创建渲染上下文
        val context =
            RenderContext(
                textConverter = TextElementConverter(),
                processedBlocks = processedBlocks,
            )

        for (block in blocks) {
            // 跳过已经作为子块处理过的块
            if (block.blockId in processedBlocks) {
                continue
            }

            // 处理列表的开始和结束
            when (block.blockType) {
                BlockType.BULLET -> {
                    if (!inBulletList) {
                        // 关闭有序列表（如果有）
                        if (inOrderedList) {
                            currentOL = null
                            inOrderedList = false
                        }
                        // 开启无序列表
                        parent.ul {
                            currentList = this
                            inBulletList = true
                            renderBlock(block, this, allBlocks, context)
                        }
                    } else {
                        // 在现有列表中添加项
                        currentList?.let { renderBlock(block, it, allBlocks, context) }
                    }
                    continue
                }
                BlockType.ORDERED -> {
                    if (!inOrderedList) {
                        // 关闭无序列表（如果有）
                        if (inBulletList) {
                            currentList = null
                            inBulletList = false
                        }
                        // 开启有序列表
                        parent.ol {
                            currentOL = this
                            inOrderedList = true
                            renderBlock(block, this, allBlocks, context)
                        }
                    } else {
                        // 在现有列表中添加项
                        currentOL?.let { renderBlock(block, it, allBlocks, context) }
                    }
                    continue
                }
                else -> {
                    // 关闭所有列表
                    if (inBulletList) {
                        currentList = null
                        inBulletList = false
                    }
                    if (inOrderedList) {
                        currentOL = null
                        inOrderedList = false
                    }
                }
            }

            // 跳过某些块类型（它们会被父块处理）
            if (shouldSkipBlock(block)) {
                continue
            }

            // 使用全局renderBlock函数渲染
            renderBlock(block, parent, allBlocks, context)
        }
    }

    private fun shouldSkipBlock(block: Block): Boolean {
        return when (block.blockType) {
            BlockType.PAGE -> true
            BlockType.TABLE_CELL -> true
            BlockType.GRID_COLUMN -> true // GRID_COLUMN 由 GRID 父块处理
            else -> false
        }
    }
}

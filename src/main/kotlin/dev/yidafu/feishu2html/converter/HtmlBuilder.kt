package dev.yidafu.feishu2html.converter

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.renderers.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("dev.yidafu.feishu2html.converter.HtmlBuilder")

/**
 * Global Block rendering function - dispatches to appropriate Renderer based on Block type
 *
 * Uses a when expression to dispatch blocks to their corresponding Renderer based on actual type.
 * This function is the entry point for the entire rendering system.
 *
 * @param block Block object to render
 * @param parent kotlinx.html FlowContent object
 * @param allBlocks Mapping of all blocks in the document
 * @param context Rendering context
 *
 * @see Renderable
 */
internal fun renderBlock(
    block: Block,
    parent: FlowContent,
    allBlocks: Map<String, Block>,
    context: RenderContext,
) {
    logger.debug("Rendering block: type={}, id={}", block::class.simpleName, block.blockId)
    when (block) {
        is PageBlock -> { /* Page blocks typically don't need rendering */ }
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
 * HTML document builder
 *
 * Generates complete HTML documents using kotlinx.html DSL, including:
 * - HTML head (metadata, styles, MathJax configuration)
 * - Body content (rendering of all Blocks)
 *
 * Uses Renderable + Delegate pattern, delegating each Block's rendering logic to a specialized Renderer.
 *
 * @property title HTML document title
 * @property customCss Custom CSS styles, overrides default Feishu styles if provided
 *
 * @see renderBlock
 * @see FeishuStyles
 */
internal class HtmlBuilder(
    private val title: String,
    private val customCss: String? = null,
) {
    private val builderLogger = LoggerFactory.getLogger(HtmlBuilder::class.java)

    /**
     * Build complete HTML document
     *
     * Generates a complete HTML5 document with head and body sections.
     * The head contains CSS styles and MathJax configuration, body renders all Blocks.
     *
     * @param blocks Ordered Block list (sorted by document structure)
     * @param allBlocks Mapping of all blocks (blockId -> Block)
     * @return Complete HTML string
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

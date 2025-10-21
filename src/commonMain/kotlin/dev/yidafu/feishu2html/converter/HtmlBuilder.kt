package dev.yidafu.feishu2html.converter

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.renderers.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

/**
 * Block Renderer Registry - Maps Block types to their corresponding Renderers
 *
 * This registry provides a centralized mapping of Block classes to Renderable implementations,
 * eliminating the need for large when expressions in renderBlock function.
 *
 * Benefits:
 * - Cleaner code (reduces 50+ line when expression to a simple map lookup)
 * - Easier maintenance (adding new Block types requires only one line)
 * - Type-safe (compile-time checking of Block to Renderer mappings)
 * - Better testability (registry can be tested independently)
 */
private object BlockRendererRegistry {
    private val renderers: Map<KClass<out Block>, Renderable<*>> = mapOf(
        PageBlock::class to PageBlockRenderer,
        TextBlock::class to TextBlockRenderer,
        Heading1Block::class to Heading1BlockRenderer,
        Heading2Block::class to Heading2BlockRenderer,
        Heading3Block::class to Heading3BlockRenderer,
        Heading4Block::class to Heading4BlockRenderer,
        Heading5Block::class to Heading5BlockRenderer,
        Heading6Block::class to Heading6BlockRenderer,
        Heading7Block::class to Heading7BlockRenderer,
        Heading8Block::class to Heading8BlockRenderer,
        Heading9Block::class to Heading9BlockRenderer,
        BulletBlock::class to BulletBlockRenderer,
        OrderedBlock::class to OrderedBlockRenderer,
        CodeBlockItem::class to CodeBlockRenderer,
        QuoteBlock::class to QuoteBlockRenderer,
        EquationBlock::class to EquationBlockRenderer,
        TodoBlock::class to TodoBlockRenderer,
        BitableBlock::class to BitableBlockRenderer,
        CalloutBlock::class to CalloutBlockRenderer,
        ChatCardBlock::class to ChatCardBlockRenderer,
        DiagramBlock::class to DiagramBlockRenderer,
        DividerBlock::class to DividerBlockRenderer,
        FileBlock::class to FileBlockRenderer,
        GridBlock::class to GridBlockRenderer,
        GridColumnBlock::class to GridColumnBlockRenderer,
        IframeBlock::class to IframeBlockRenderer,
        ImageBlock::class to ImageBlockRenderer,
        TableBlock::class to TableBlockRenderer,
        TableCellBlock::class to TableCellBlockRenderer,
        QuoteContainerBlock::class to QuoteContainerBlockRenderer,
        BoardBlock::class to BoardBlockRenderer,
        IsvBlock::class to IsvBlockRenderer,
        MindnoteBlock::class to MindnoteBlockRenderer,
        SheetBlock::class to SheetBlockRenderer,
        ViewBlock::class to ViewBlockRenderer,
        TaskBlock::class to TaskBlockRenderer,
        OkrBlock::class to OkrBlockRenderer,
        OkrObjectiveBlock::class to OkrObjectiveBlockRenderer,
        OkrKeyResultBlock::class to OkrKeyResultBlockRenderer,
        OkrProgressBlock::class to OkrProgressBlockRenderer,
        AddOnsBlock::class to AddOnsBlockRenderer,
        JiraIssueBlock::class to JiraIssueBlockRenderer,
        WikiCatalogBlock::class to WikiCatalogBlockRenderer,
        AgendaBlock::class to AgendaBlockRenderer,
        AgendaItemBlock::class to AgendaItemBlockRenderer,
        AgendaItemTitleBlock::class to AgendaItemTitleBlockRenderer,
        AgendaItemContentBlock::class to AgendaItemContentBlockRenderer,
        LinkPreviewBlock::class to LinkPreviewBlockRenderer,
        SourceSyncedBlock::class to SourceSyncedBlockRenderer,
        ReferenceSyncedBlock::class to ReferenceSyncedBlockRenderer,
        SubPageListBlock::class to SubPageListBlockRenderer,
        AiTemplateBlock::class to AiTemplateBlockRenderer,
        UnknownBlock::class to UnknownBlockRenderer,
    )

    /**
     * Render a BlockNode using the appropriate Renderer
     *
     * Inline function that directly dispatches to the correct renderer without
     * intermediate getRenderer() call. Reduces function call overhead.
     *
     * @param blockNode BlockNode to render
     * @param parent Parent HTML element to render into
     * @param context Rendering context
     * @throws IllegalStateException if no renderer found for block type
     */
    @Suppress("UNCHECKED_CAST")
    inline fun render(
        blockNode: BlockNode<Block>,
        parent: FlowContent,
        context: RenderContext,
    ) {
        val block = blockNode.data
        val renderer = renderers[block::class]
            ?: throw IllegalStateException(
                "No renderer found for block type: ${block::class.simpleName}. " +
                    "Please ensure all Block types are registered in BlockRendererRegistry.",
            )

        (renderer as Renderable<Block>).render(parent, blockNode as BlockNode<Block>, context)
    }
}

/**
 * BlockNode rendering extension function - dispatches to appropriate Renderer based on Block type
 *
 * Uses BlockRendererRegistry to efficiently map Block types to their corresponding Renderers.
 * This extension function is the entry point for the entire rendering system.
 *
 * Benefits of inline delegation:
 * - No intermediate function calls (inlined at call site)
 * - Type casting centralized in Registry.render()
 * - Cleaner code without explicit casts
 *
 * @param parent kotlinx.html FlowContent object
 * @param context Rendering context
 *
 * @see Renderable
 * @see BlockRendererRegistry
 * @see BlockNode
 */
internal fun BlockNode<Block>.render(
    parent: FlowContent,
    context: RenderContext,
) {
    logger.debug { "Rendering block: type=${this.data::class.simpleName}, id=${this.data.blockId}" }
    BlockRendererRegistry.render(this, parent, context)
}

/**
 * HTML build context - encapsulates common build parameters
 *
 * @property title HTML document title
 * @property cssMode CSS inclusion mode (inline or external)
 * @property cssFileName CSS file name when using external mode
 * @property customCss Custom CSS styles, overrides default styles if provided
 * @property styleMode Style framework to use (Feishu or GitHub)
 */
data class HtmlBuildContext(
    val title: String,
    val cssMode: CssMode,
    val cssFileName: String,
    val customCss: String?,
    val styleMode: StyleMode = StyleMode.FEISHU,
) {
    /**
     * Get the default CSS content based on style mode
     *
     * Both styles use the same Feishu class names (.protyle-wysiwyg, .b3-typography, etc.)
     * but apply different visual styles (Feishu official colors vs GitHub colors)
     */
    fun getDefaultCss(): String {
        return when (styleMode) {
            StyleMode.FEISHU -> FeishuStyles.generateCSS()
            StyleMode.GITHUB -> GitHubStyles.CSS
        }
    }
}

/**
 * Build standard HTML head section with CSS and MathJax configuration
 *
 * This function provides the common head structure used by Default and Fragment templates.
 *
 * @param context Build context containing CSS and title configuration
 */
fun HEAD.buildStandardHead(context: HtmlBuildContext) {
    meta(charset = "UTF-8")
    meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
    title(context.title)

    if (context.cssMode == CssMode.INLINE) {
        style {
            unsafe {
                raw(context.customCss ?: context.getDefaultCss())
            }
        }
    } else {
        link(rel = "stylesheet", href = context.cssFileName)
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

/**
 * HTML template type for customizing HTML output structure
 *
 * Supports three template modes:
 * - Default: Complete HTML with head and body, includes external JS and CSS
 * - Plain: Complete HTML with head and body, but without external JS and CSS (inline CSS only)
 * - Fragment: Only HTML fragment without html/head/body tags
 *
 * Each template implements its own build logic using the Strategy pattern.
 *
 * Predefined templates for common use cases:
 * - DefaultCli: Standard Feishu template with full HTML structure
 * - FragmentCli: Minimal fragment with simple div wrapper
 * - PlainCli: Basic HTML structure without external JS/CSS
 */
interface HtmlTemplate {
    /**
     * Build HTML document
     *
     * @param content Content builder for rendering document blocks
     * @param context Build context containing CSS and title configuration
     * @return Complete HTML string or HTML fragment
     */
    fun build(
        content: FlowContent.() -> Unit,
        context: HtmlBuildContext,
    ): String

    /**
     * Default template - complete HTML with external CSS and JS
     *
     * Generates a full HTML document with:
     * - Standard head section with meta tags
     * - External CSS file link (or inline CSS based on cssMode)
     * - External MathJax scripts for equation rendering
     * - Standard body with content
     */
    data object Default : HtmlTemplate {
        override fun build(
            content: FlowContent.() -> Unit,
            context: HtmlBuildContext,
        ): String {
            return createHTML().html {
                lang = "zh-CN"
                head {
                    buildStandardHead(context)
                }
                body {
                    content()
                }
            }
        }
    }

    /**
     * Plain template - complete HTML without external JS and CSS
     *
     * This template provides two modes:
     * 1. Default Plain mode: Standard HTML with inline CSS and no external resources
     * 2. Custom mode: User has full control over the HTML structure
     *
     * The Plain template is useful for standalone, self-contained HTML files that don't
     * depend on external resources, making them easier to distribute and archive.
     *
     * Example:
     * ```kotlin
     * // Default Plain mode
     * HtmlTemplate.Plain()
     *
     * // Custom mode
     * HtmlTemplate.Plain { content ->
     *     lang = "en"
     *     head {
     *         title("Custom")
     *         style { /* custom inline styles */ }
     *     }
     *     body { content() }
     * }
     * ```
     */
    class Plain(
        private val builder: (HTML.(content: FlowContent.() -> Unit) -> Unit)? = null,
    ) : HtmlTemplate {
        override fun build(
            content: FlowContent.() -> Unit,
            context: HtmlBuildContext,
        ): String {
            return createHTML().html {
                if (builder != null) {
                    // User has full control
                    builder(this, content)
                } else {
                    // Default Plain mode: inline CSS, no external JS
                    lang = "zh-CN"
                    head {
                        meta(charset = "UTF-8")
                        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                        title(context.title)

                    // Always use inline CSS for Plain template
                    style {
                        unsafe {
                            raw(context.customCss ?: context.getDefaultCss())
                        }
                    }
                        // No external JavaScript - equations won't be rendered
                    }
                    body {
                        content()
                    }
                }
            }
        }

        companion object {
            /**  Default Plain template instance */
            operator fun invoke(): Plain = Plain(null)
        }
    }

    /**
     * Fragment template - HTML fragment only
     *
     * Generates only the content fragment without html, head, or body tags.
     * This is useful for embedding content into existing HTML pages.
     *
     * Example:
     * ```kotlin
     * HtmlTemplate.Fragment { content ->
     *     div(classes = "my-custom-wrapper") {
     *         content()
     *     }
     * }
     * ```
     */
    class Fragment(
        val builder: FlowContent.(content: FlowContent.() -> Unit) -> Unit = { it() },
    ) : HtmlTemplate {
        override fun build(
            content: FlowContent.() -> Unit,
            context: HtmlBuildContext,
        ): String {
            // Create a temporary div to build the fragment
            return createHTML().div {
                builder(this, content)
            }
        }
    }

    companion object {
        /**
         * Predefined template for CLI: Standard Feishu template with full HTML structure
         */
        val DefaultCli: HtmlTemplate = Default

        /**
         * Predefined template for CLI: Minimal fragment with simple div wrapper
         */
        val FragmentCli: HtmlTemplate = Fragment { content ->
            div(classes = "feishu-document") {
                content()
            }
        }

        /**
         * Predefined template for CLI: Basic HTML structure without external JS/CSS
         */
        val PlainCli: HtmlTemplate = Plain { content ->
            lang = "zh-CN"
            head {
                meta(charset = "UTF-8")
                meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                title("Feishu Document")
                style {
                    unsafe {
                        raw("""
                            body {
                                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
                                line-height: 1.6;
                                max-width: 900px;
                                margin: 0 auto;
                                padding: 20px;
                            }
                        """.trimIndent())
                    }
                }
            }
            body {
                content()
            }
        }
    }
}

/**
 * CSS mode for HTML generation
 */
enum class CssMode {
    INLINE, // <style> tag with CSS content
    EXTERNAL, // <link> tag referencing external file
}

/**
 * Style mode for HTML generation
 *
 * Determines the visual styling to apply (colors, fonts, spacing).
 * Both modes use the same Feishu class structure (.protyle-wysiwyg, .b3-typography, etc.)
 *
 * - FEISHU: Official Feishu colors and styling
 * - GITHUB: GitHub-style colors and typography
 */
enum class StyleMode {
    /** Feishu official style (blue accent, Feishu colors) */
    FEISHU,
    /** GitHub style (blue accent, GitHub colors, same class names) */
    GITHUB,
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
 * @property cssMode CSS inclusion mode (inline or external)
 * @property cssFileName CSS file name when using external mode
 * @property customCss Custom CSS styles, overrides default styles if provided
 * @property styleMode Style framework to use (Feishu or GitHub)
 * @property template HTML template for customizing output structure
 *
 * @see renderBlock
 * @see FeishuStyles
 * @see GitHubStyles
 * @see HtmlTemplate
 */
internal class HtmlBuilder(
    private val title: String,
    private val cssMode: CssMode = CssMode.EXTERNAL,
    private val cssFileName: String = "feishu-style.css",
    private val customCss: String? = null,
    private val styleMode: StyleMode = StyleMode.FEISHU,
    private val template: HtmlTemplate = HtmlTemplate.Default,
    private val imageBase64Cache: Map<String, String> = emptyMap(),
    private val showUnsupportedBlocks: Boolean = true,
) {
    private val builderLogger = KotlinLogging.logger {}

    /**
     * Build complete HTML document
     *
     * Generates a complete HTML5 document with head and body sections.
     * The head contains CSS styles and MathJax configuration, body renders all BlockNodes.
     *
     * @param blockNodes Tree of BlockNodes representing document structure
     * @return Complete HTML string
     */
    fun build(blockNodes: List<BlockNode<Block>>): String {
        builderLogger.info { "Starting HTML build for document: $title" }
        builderLogger.debug { "Building with ${blockNodes.size} root nodes" }
        builderLogger.debug { "Using ${if (customCss != null) "custom" else "default"} CSS" }
        builderLogger.debug { "Using template mode: ${template::class.simpleName}" }

        try {
            // Create build context
            val buildContext = HtmlBuildContext(
                title = title,
                cssMode = cssMode,
                cssFileName = cssFileName,
                customCss = customCss,
                styleMode = styleMode,
            )

            // Create content builder that will be passed to templates
            // Use unified Feishu class names for both styles
            val contentBuilder: FlowContent.() -> Unit = {
                div(classes = "protyle-wysiwyg b3-typography") {
                    attributes["data-node-id"] = "root"
                    buildBody(blockNodes, this)
                }
            }

            // Use polymorphism - let each template build itself
            val html = template.build(contentBuilder, buildContext)

            builderLogger.info { "HTML build completed successfully for document: $title" }
            builderLogger.debug { "Generated HTML size: ${html.length} characters" }
            return html
        } catch (e: Exception) {
            builderLogger.error(e) { "Failed to build HTML for document $title: ${e.message}" }
            throw e
        }
    }

    private fun buildBody(
        blockNodes: List<BlockNode<Block>>,
        parent: FlowContent,
    ) {
        // 创建渲染上下文
        val context =
            RenderContext(
                textConverter = TextElementConverter(),
                imageBase64Cache = imageBase64Cache,
                showUnsupportedBlocks = showUnsupportedBlocks,
            )

        // 树形渲染：直接渲染所有顶层节点
        // 每个节点的子节点由其 Renderer 递归渲染
        // 不需要复杂的过滤逻辑，因为 getOrderedBlocks() 已返回正确的顶层节点
        for (node in blockNodes) {
            node.render(parent, context)
        }
    }
}

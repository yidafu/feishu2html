package dev.yidafu.feishu2html.converter.renderers

import dev.yidafu.feishu2html.api.model.*
import dev.yidafu.feishu2html.converter.*
import dev.yidafu.feishu2html.platform.decodeUrl
import kotlinx.html.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal object IframeBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val iframeBlock = block as IframeBlock
        val url = iframeBlock.iframe?.component?.url ?: iframeBlock.iframe?.url
        val iframeType =
            iframeBlock.iframe?.component?.iframeType?.let {
                IframeType.fromCode(it)
            } ?: IframeType.GENERIC

        if (url.isNullOrEmpty()) return

        logger.debug { "Rendering iframe: type=${iframeType.displayName}, url=$url" }

        val decodedUrl = decodeUrl(url)

        when (iframeType) {
            IframeType.BILIBILI, IframeType.YOUKU, IframeType.YOUTUBE -> {
                parent.div(classes = "embed-container embed-video") {
                    attributes["data-type"] = iframeType.displayName
                    div(classes = "embed-header") {
                        span(classes = "embed-icon") { +"🎬" }
                        span(classes = "embed-title") { +iframeType.displayName }
                    }
                    div(classes = "embed-content") {
                        iframe {
                            src = decodedUrl
                            attributes["frameborder"] = "0"
                            attributes["allowfullscreen"] = ""
                        }
                    }
                }
            }
            IframeType.FIGMA, IframeType.MODAO, IframeType.CANVA,
            IframeType.INVISION, IframeType.LANHU, IframeType.AXURE,
            -> {
                parent.div(classes = "embed-container embed-design") {
                    attributes["data-type"] = iframeType.displayName
                    div(classes = "embed-header") {
                        span(classes = "embed-icon") { +"🎨" }
                        span(classes = "embed-title") { +iframeType.displayName }
                    }
                    div(classes = "embed-content") {
                        iframe {
                            src = decodedUrl
                            attributes["frameborder"] = "0"
                        }
                    }
                }
            }
            IframeType.FEISHU_DOCS, IframeType.FEISHU_SHEET,
            IframeType.FEISHU_BITABLE, IframeType.FEISHU_BOARD,
            -> {
                parent.div(classes = "embed-container embed-feishu") {
                    attributes["data-type"] = iframeType.displayName
                    div(classes = "embed-header") {
                        span(classes = "embed-icon") { +"📄" }
                        span(classes = "embed-title") { +iframeType.displayName }
                    }
                    div(classes = "embed-content") {
                        iframe {
                            src = decodedUrl
                            attributes["frameborder"] = "0"
                        }
                    }
                }
            }
            else -> {
                parent.iframe(classes = "embed-generic") {
                    src = decodedUrl
                    attributes["frameborder"] = "0"
                    attributes["allowfullscreen"] = ""
                }
            }
        }
    }
}

internal object DiagramBlockRenderer : Renderable {
    override fun <T> render(
        parent: FlowContent,
        block: T,
        allBlocks: Map<String, Block>,
        context: RenderContext,
    ) {
        val diagramBlock = block as DiagramBlock
        val content = diagramBlock.diagram?.content ?: return
        val type = diagramBlock.diagram?.diagramType
        logger.debug("Rendering diagram block: type={}, content length={}", type, content.length)

        parent.div(classes = "diagram") {
            attributes["data-type"] = type?.toString() ?: ""
            pre {
                +content
            }
        }
    }
}

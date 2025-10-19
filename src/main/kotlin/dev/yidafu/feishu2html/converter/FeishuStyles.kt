package dev.yidafu.feishu2html.converter

/**
 * Feishu document style CSS definitions
 */
internal object FeishuStyles {
    fun generateCSS(): String {
        return """        /* ========== Feishu Document Styles ========== */
        
        /* Base Styles */
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
            line-height: 1.75;
            color: #1f2329;
            background-color: #f7f8fa;
            margin: 0;
            padding: 0;
        }

        .container {
            max-width: 920px;
            margin: 24px auto;
            padding: 48px 60px;
            background: #ffffff;
            border-radius: 8px;
            box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03), 0 1px 6px -1px rgba(0, 0, 0, 0.02);
        }

        /* Heading Styles - Feishu Style */
        .heading,
        h1, h2, h3, h4, h5, h6 {
            margin-top: 28px;
            margin-bottom: 12px;
            font-weight: 600;
            line-height: 1.4;
            color: #1f2329;
        }

        h1 { font-size: 28px; margin-top: 32px; }
        h2 { font-size: 24px; margin-top: 28px; }
        h3 { font-size: 20px; }
        h4 { font-size: 18px; }
        h5 { font-size: 16px; font-weight: 500; }
        h6 { font-size: 14px; font-weight: 500; color: #646a73; }
        
        .heading-extra-bold {
            font-weight: 700;
        }

        /* Paragraph and Text */
        p {
            margin: 8px 0;
            font-size: 15px;
            line-height: 1.75;
            color: #1f2329;
        }

        /* Text Alignment */
        .text-align-center {
            text-align: center;
        }

        .text-align-right {
            text-align: right;
        }

        /* Text Color - Feishu Style */
        .text-red { color: #f54a45; }
        .text-yellow { color: #ff8800; }
        .text-green { color: #00b578; }
        .text-blue { color: #3370ff; }
        .text-indigo { color: #6366f1; }
        .text-purple { color: #8957e5; }
        .text-pink { color: #ec4899; }
        .text-gray { color: #646a73; }

        /* Text Background Color - Feishu Style */
        .bg-red { background-color: #ffecea; padding: 2px 6px; border-radius: 4px; }
        .bg-yellow { background-color: #fff3e0; padding: 2px 6px; border-radius: 4px; }
        .bg-green { background-color: #e6f7ed; padding: 2px 6px; border-radius: 4px; }
        .bg-blue { background-color: #e8f3ff; padding: 2px 6px; border-radius: 4px; }
        .bg-indigo { background-color: #eff0ff; padding: 2px 6px; border-radius: 4px; }
        .bg-purple { background-color: #f3ebff; padding: 2px 6px; border-radius: 4px; }
        .bg-pink { background-color: #ffe4e6; padding: 2px 6px; border-radius: 4px; }
        .bg-gray { background-color: #f2f3f5; padding: 2px 6px; border-radius: 4px; }

        /* Links - Feishu Style */
        a {
            color: #3370ff;
            text-decoration: none;
            border-bottom: 1px solid transparent;
            transition: border-color 0.2s;
        }

        a:hover {
            border-bottom-color: #3370ff;
        }

        /* Lists - Feishu Style */
        ul, ol {
            padding-left: 28px;
            margin: 12px 0;
        }

        li {
            margin: 6px 0;
            line-height: 1.75;
        }

        /* Code - Feishu Style */
        code {
            padding: 2px 6px;
            margin: 0 2px;
            font-size: 14px;
            background-color: rgba(31, 35, 41, 0.05);
            border: 1px solid rgba(31, 35, 41, 0.08);
            border-radius: 4px;
            font-family: "JetBrains Mono", "Fira Code", "Consolas", "Monaco", monospace;
        }

        pre {
            padding: 16px 20px;
            overflow: auto;
            font-size: 14px;
            line-height: 1.6;
            background-color: #f7f8fa;
            border: 1px solid #e7e9ed;
            border-radius: 8px;
            margin: 16px 0;
        }

        pre code {
            display: block;
            padding: 0;
            margin: 0;
            background-color: transparent;
            border: none;
            font-size: inherit;
        }

        /* Blockquote - Feishu Style */
        blockquote {
            padding: 12px 16px;
            color: #646a73;
            background-color: #f7f8fa;
            border-left: 4px solid #dee0e3;
            border-radius: 0 4px 4px 0;
            margin: 16px 0;
        }

        /* Table - Feishu Style */
        table {
            border-spacing: 0;
            border-collapse: collapse;
            margin: 16px 0;
            width: 100%;
            border: 1px solid #e7e9ed;
            border-radius: 8px;
            overflow: hidden;
        }

        table th, table td {
            padding: 12px 16px;
            border: 1px solid #e7e9ed;
            text-align: left;
            font-size: 14px;
            line-height: 1.6;
        }

        table th {
            font-weight: 600;
            background-color: #f7f8fa;
            color: #1f2329;
        }

        table tr {
            background-color: #fff;
            transition: background-color 0.2s;
        }

        table tr:hover {
            background-color: #f7f8fa;
        }

        /* Divider - Feishu Style */
        hr {
            height: 1px;
            padding: 0;
            margin: 24px 0;
            background-color: #e7e9ed;
            border: 0;
        }

        /* Image - Feishu Style */
        img {
            max-width: 100%;
            height: auto;
            display: block;
            margin: 20px 0;
            border-radius: 8px;
        }

        img.align-center {
            margin-left: auto;
            margin-right: auto;
        }

        img.align-right {
            margin-left: auto;
            margin-right: 0;
        }

        /* Todo - Feishu Style */
        .todo {
            display: flex;
            align-items: flex-start;
            margin: 8px 0;
            padding: 8px 12px;
            border-radius: 6px;
            transition: background-color 0.2s;
        }

        .todo:hover {
            background-color: #f7f8fa;
        }

        .todo input[type="checkbox"] {
            margin-right: 10px;
            margin-top: 6px;
            width: 16px;
            height: 16px;
        }

        /* Callout - Feishu Style */
        .callout {
            padding: 16px 20px;
            margin: 16px 0;
            border-radius: 8px;
            border-left: 4px solid;
            font-size: 15px;
            line-height: 1.75;
        }

        .callout-default {
            background-color: #f7f8fa;
            border-color: #8f959e;
        }

        .callout-red {
            background-color: #ffecea;
            border-color: #f54a45;
        }

        .callout-yellow {
            background-color: #fff3e0;
            border-color: #ff8800;
        }

        .callout-green {
            background-color: #e6f7ed;
            border-color: #00b578;
        }

        .callout-blue {
            background-color: #e8f3ff;
            border-color: #3370ff;
        }

        .callout-indigo {
            background-color: #eff0ff;
            border-color: #6366f1;
        }

        .callout-purple {
            background-color: #f3ebff;
            border-color: #8957e5;
        }

        .callout-pink {
            background-color: #ffe4e6;
            border-color: #ec4899;
        }

        .callout-gray {
            background-color: #f2f3f5;
            border-color: #8f959e;
        }

        .callout-emoji {
            font-size: 20px;
            margin-right: 8px;
            vertical-align: middle;
        }

        /* Equation - Feishu Style */
        .equation {
            overflow-x: auto;
            margin: 20px 0;
            padding: 16px;
            text-align: center;
            background-color: #f7f8fa;
            border-radius: 8px;
        }

        /* Diagram - Feishu Style */
        .diagram {
            margin: 16px 0;
            padding: 20px;
            background-color: #f7f8fa;
            border: 1px solid #e7e9ed;
            border-radius: 8px;
            overflow-x: auto;
        }

        /* iframe - Feishu Style */
        iframe {
            width: 100%;
            height: 400px;
            border: 1px solid #e7e9ed;
            border-radius: 8px;
            margin: 16px 0;
        }

        /* Quote Container - Feishu Style */
        .quote-container {
            padding: 16px 20px;
            margin: 16px 0;
            background-color: #fffbf5;
            border-left: 4px solid #ff8800;
            border-radius: 8px;
            color: #646a73;
        }

        /* Mention - Feishu Style */
        .mention-user, .mention-doc {
            color: #3370ff;
            background-color: #e8f3ff;
            padding: 2px 8px;
            border-radius: 4px;
            font-weight: 500;
        }

        /* Inline File */
        .inline-file {
            color: #646a73;
            font-style: italic;
        }

        /* Grid Column Layout - Feishu Style */
        .grid-layout {
            margin: 20px 0;
            gap: 20px;
        }

        .grid-column {
            min-width: 0;
            padding: 16px;
            background-color: #f7f8fa;
            border-radius: 8px;
        }

        /* Embedded Block - Feishu Style */
        .embed-container {
            margin: 20px 0;
            border: 1px solid #e7e9ed;
            border-radius: 8px;
            overflow: hidden;
            background: #ffffff;
        }

        .embed-header {
            padding: 12px 16px;
            background: #f7f8fa;
            border-bottom: 1px solid #e7e9ed;
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            font-weight: 500;
            color: #646a73;
        }

        .embed-icon {
            font-size: 18px;
        }

        .embed-content {
            position: relative;
            width: 100%;
        }

        /* Video Embed - 16:9 Responsive */
        .embed-video .embed-content {
            padding-bottom: 56.25%; /* 16:9 */
            height: 0;
        }

        .embed-video iframe {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
        }

        /* Design Tool Embed */
        .embed-design .embed-content {
            height: 600px;
        }

        .embed-design iframe {
            width: 100%;
            height: 100%;
        }

        /* Feishu Content Embed */
        .embed-feishu .embed-content {
            height: 500px;
        }

        .embed-feishu iframe {
            width: 100%;
            height: 100%;
        }

        /* Generic Embed */
        .embed-generic {
            width: 100%;
            height: 400px;
            border: 1px solid #e7e9ed;
            border-radius: 8px;
            margin: 16px 0;
        }

        /* Responsive */
        @media (max-width: 768px) {
            .container {
                padding: 32px 24px;
                margin: 16px;
                border-radius: 0;
            }

            .grid-layout {
                display: block !important;
            }
            
            .grid-column {
                margin-bottom: 16px;
            }
            
            h1 { font-size: 24px; }
            h2 { font-size: 20px; }
            h3 { font-size: 18px; }
        }"""
    }
}

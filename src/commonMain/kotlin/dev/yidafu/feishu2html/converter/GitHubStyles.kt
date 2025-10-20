package dev.yidafu.feishu2html.converter

/**
 * GitHub-style CSS using Feishu class names
 *
 * This provides GitHub's visual styling (colors, typography, spacing)
 * while maintaining Feishu's CSS class structure (.protyle-wysiwyg, .b3-typography, etc.)
 *
 * This approach allows switching between Feishu and GitHub styles
 * without changing the HTML structure or class names.
 */
internal object GitHubStyles {
    /**
     * GitHub-style CSS with Feishu class names
     *
     * Features:
     * - GitHub color scheme (light/dark mode via prefers-color-scheme)
     * - GitHub typography (system fonts, spacing)
     * - Uses Feishu class structure (.protyle-wysiwyg, .heading, .code-block, etc.)
     * - Compatible with Feishu HTML output
     */
    const val CSS = """
/* ========================================
   GitHub-Style CSS with Feishu Classes
   ======================================== */

/* ==================== CSS Variables (GitHub Colors) ==================== */
:root {
  /* GitHub Light Theme Colors */
  --gh-fg-default: #1f2328;
  --gh-fg-muted: #59636e;
  --gh-fg-accent: #0969da;
  --gh-bg-default: #ffffff;
  --gh-bg-muted: #f6f8fa;
  --gh-bg-subtle: #f6f8fa;
  --gh-border-default: #d1d9e0;
  --gh-border-muted: #d1d9e0b3;

  /* Font settings */
  --gh-font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Noto Sans", Helvetica, Arial, sans-serif;
  --gh-font-mono: ui-monospace, SFMono-Regular, SF Mono, Menlo, Consolas, Liberation Mono, monospace;
}

@media (prefers-color-scheme: dark) {
  :root {
    /* GitHub Dark Theme Colors */
    --gh-fg-default: #f0f6fc;
    --gh-fg-muted: #9198a1;
    --gh-fg-accent: #4493f8;
    --gh-bg-default: #0d1117;
    --gh-bg-muted: #151b23;
    --gh-bg-subtle: #151b23;
    --gh-border-default: #3d444d;
    --gh-border-muted: #3d444db3;
  }
}

/* ==================== Base Styles ==================== */
body {
  margin: 0;
  padding: 0;
  background-color: var(--gh-bg-default);
  color: var(--gh-fg-default);
  font-family: var(--gh-font-family);
  font-size: 16px;
  line-height: 1.5;
}

/* ==================== Container ==================== */
.protyle-wysiwyg,
.b3-typography {
  -ms-text-size-adjust: 100%;
  -webkit-text-size-adjust: 100%;
  margin: 0;
  color: var(--gh-fg-default);
  background-color: var(--gh-bg-default);
  font-family: var(--gh-font-family);
  font-size: 16px;
  line-height: 1.5;
  word-wrap: break-word;
  padding: 24px;
}

.protyle-wysiwyg > *:first-child,
.b3-typography > *:first-child {
  margin-top: 0 !important;
}

.protyle-wysiwyg > *:last-child,
.b3-typography > *:last-child {
  margin-bottom: 0 !important;
}

/* ==================== Headings ==================== */
.heading,
.protyle-wysiwyg h1,
.protyle-wysiwyg h2,
.protyle-wysiwyg h3,
.protyle-wysiwyg h4,
.protyle-wysiwyg h5,
.protyle-wysiwyg h6 {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
  color: var(--gh-fg-default);
}

.heading-h1,
.protyle-wysiwyg h1 {
  font-size: 2em;
  margin: 0.67em 0;
  padding-bottom: 0.3em;
  border-bottom: 1px solid var(--gh-border-muted);
}

.heading-h2,
.protyle-wysiwyg h2 {
  font-size: 1.5em;
  padding-bottom: 0.3em;
  border-bottom: 1px solid var(--gh-border-muted);
}

.heading-h3,
.protyle-wysiwyg h3 {
  font-size: 1.25em;
}

.heading-h4,
.protyle-wysiwyg h4 {
  font-size: 1em;
}

.heading-h5,
.protyle-wysiwyg h5 {
  font-size: 0.875em;
}

.heading-h6,
.protyle-wysiwyg h6 {
  font-size: 0.85em;
  color: var(--gh-fg-muted);
}

/* ==================== Text ==================== */
.protyle-wysiwyg p,
.b3-typography p {
  margin-top: 0;
  margin-bottom: 16px;
}

.protyle-wysiwyg a {
  color: var(--gh-fg-accent);
  text-decoration: none;
}

.protyle-wysiwyg a:hover {
  text-decoration: underline;
}

/* ==================== Code ==================== */
.fn__code,
.protyle-wysiwyg code:not(.hljs) {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: var(--gh-bg-muted);
  border-radius: 6px;
  font-family: var(--gh-font-mono);
}

.code-block,
.protyle-wysiwyg pre {
  padding: 16px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background-color: var(--gh-bg-muted);
  border-radius: 6px;
  margin-bottom: 16px;
}

.code-block code,
.protyle-wysiwyg pre code {
  padding: 0;
  margin: 0;
  background: transparent;
  border: 0;
  font-family: var(--gh-font-mono);
}

/* ==================== Lists ==================== */
.protyle-wysiwyg ul,
.protyle-wysiwyg ol {
  margin-top: 0;
  margin-bottom: 16px;
  padding-left: 2em;
}

.protyle-wysiwyg li + li {
  margin-top: 0.25em;
}

.bullet-list,
.ordered-list {
  list-style-position: outside;
  padding-left: 2em;
}

/* ==================== Blockquote ==================== */
.protyle-wysiwyg blockquote {
  margin: 0 0 16px 0;
  padding: 0 1em;
  color: var(--gh-fg-muted);
  border-left: 0.25em solid var(--gh-border-default);
}

/* ==================== Tables ==================== */
.protyle-wysiwyg table {
  border-spacing: 0;
  border-collapse: collapse;
  display: block;
  width: max-content;
  max-width: 100%;
  overflow: auto;
  margin-bottom: 16px;
}

.protyle-wysiwyg table th,
.protyle-wysiwyg table td {
  padding: 6px 13px;
  border: 1px solid var(--gh-border-default);
}

.protyle-wysiwyg table th {
  font-weight: 600;
}

.protyle-wysiwyg table tr {
  background-color: var(--gh-bg-default);
  border-top: 1px solid var(--gh-border-muted);
}

.protyle-wysiwyg table tr:nth-child(2n) {
  background-color: var(--gh-bg-muted);
}

/* ==================== Images ==================== */
.protyle-wysiwyg img {
  max-width: 100%;
  box-sizing: content-box;
  background-color: var(--gh-bg-default);
}

/* ==================== Callout Blocks ==================== */
.callout-block {
  padding: 8px 16px;
  margin-bottom: 16px;
  border-left: 0.25em solid var(--gh-border-default);
  background-color: var(--gh-bg-subtle);
}

.callout-blue {
  border-left-color: #0969da;
  background-color: #ddf4ff;
}

.callout-green {
  border-left-color: #1a7f37;
  background-color: #dafbe1;
}

.callout-yellow {
  border-left-color: #9a6700;
  background-color: #fff8c5;
}

.callout-red {
  border-left-color: #d1242f;
  background-color: #ffebe9;
}

/* ==================== Horizontal Rule ==================== */
.protyle-wysiwyg hr {
  height: 0.25em;
  padding: 0;
  margin: 24px 0;
  background-color: var(--gh-border-default);
  border: 0;
}

/* ==================== Todo/Checkbox ==================== */
.protyle-wysiwyg input[type="checkbox"] {
  margin: 0 0.2em 0.25em -1.4em;
  vertical-align: middle;
}

/* ==================== File Blocks ==================== */
.file-card,
.docx-file-block {
  padding: 12px;
  background-color: var(--gh-bg-muted);
  border: 1px solid var(--gh-border-default);
  border-radius: 6px;
  margin-bottom: 16px;
}

/* ==================== Grid Layout ==================== */
.grid-layout {
  display: grid;
  gap: 16px;
  margin-bottom: 16px;
}

.grid-column {
  padding: 12px;
  border: 1px solid var(--gh-border-default);
  border-radius: 6px;
}

/* ==================== Divider ==================== */
.divider {
  height: 0.25em;
  margin: 24px 0;
  background-color: var(--gh-border-default);
  border: 0;
}
"""
}


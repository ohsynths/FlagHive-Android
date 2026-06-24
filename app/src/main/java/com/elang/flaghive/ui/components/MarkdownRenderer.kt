package com.elang.flaghive.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient

@Composable
fun MarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    line-height: 1.6;
                    color: #1a1a1a;
                    padding: 8px;
                    word-wrap: break-word;
                }
                pre {
                    background-color: #f4f4f4;
                    padding: 12px;
                    border-radius: 6px;
                    overflow-x: auto;
                }
                code {
                    background-color: #f4f4f4;
                    padding: 2px 4px;
                    border-radius: 3px;
                    font-size: 0.9em;
                }
                pre code {
                    background-color: transparent;
                    padding: 0;
                }
                img { max-width: 100%; height: auto; }
                table {
                    border-collapse: collapse;
                    width: 100%;
                }
                th, td {
                    border: 1px solid #ddd;
                    padding: 8px;
                    text-align: left;
                }
                th { background-color: #f8f8f8; }
                blockquote {
                    border-left: 4px solid #ddd;
                    margin: 0;
                    padding-left: 16px;
                    color: #666;
                }
                @media (prefers-color-scheme: dark) {
                    body { color: #e0e0e0; }
                    pre, code { background-color: #2d2d2d; }
                    th { background-color: #2d2d2d; }
                    th, td { border-color: #444; }
                    blockquote { border-left-color: #555; color: #aaa; }
                }
            </style>
        </head>
        <body>${convertMarkdownToHtml(markdown)}</body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = false
                loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            }
        }
    )
}

private fun convertMarkdownToHtml(markdown: String): String {
    var html = markdown
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    val lines = html.split("\n")
    val result = StringBuilder()
    var inCodeBlock = false
    var inList = false

    for (line in lines) {
        when {
            line.startsWith("```") -> {
                if (inCodeBlock) {
                    result.appendLine("</code></pre>")
                    inCodeBlock = false
                } else {
                    result.appendLine("<pre><code>")
                    inCodeBlock = true
                }
            }
            inCodeBlock -> {
                result.appendLine(line)
            }
            line.startsWith("# ") -> {
                result.appendLine("<h1>${line.removePrefix("# ")}</h1>")
            }
            line.startsWith("## ") -> {
                result.appendLine("<h2>${line.removePrefix("## ")}</h2>")
            }
            line.startsWith("### ") -> {
                result.appendLine("<h3>${line.removePrefix("### ")}</h3>")
            }
            line.startsWith("- ") -> {
                if (!inList) {
                    result.appendLine("<ul>")
                    inList = true
                }
                result.appendLine("<li>${line.removePrefix("- ")}</li>")
            }
            line.matches(Regex("^\\d+\\.\\s.*")) -> {
                if (!inList) {
                    result.appendLine("<ol>")
                    inList = true
                }
                result.appendLine("<li>${line.replaceFirst(Regex("^\\d+\\.\\s"), "")}</li>")
            }
            line.isBlank() -> {
                if (inList) {
                    result.appendLine(if (inList) "</ul>" else "</ol>")
                    inList = false
                }
                result.appendLine("<br>")
            }
            else -> {
                var formatted = line
                formatted = formatted.replace(Regex("\\*\\*(.+?)\\*\\*")) { "<strong>${it.groupValues[1]}</strong>" }
                formatted = formatted.replace(Regex("\\*(.+?)\\*")) { "<em>${it.groupValues[1]}</em>" }
                formatted = formatted.replace(Regex("`(.+?)`")) { "<code>${it.groupValues[1]}</code>" }
                formatted = formatted.replace(Regex("\\[([^]]+)]\\(([^)]+)\\)")) { "<a href='${it.groupValues[2]}'>${it.groupValues[1]}</a>" }
                result.appendLine("<p>$formatted</p>")
            }
        }
    }

    if (inCodeBlock) result.appendLine("</code></pre>")
    if (inList) result.appendLine("</ul>")

    return result.toString()
}

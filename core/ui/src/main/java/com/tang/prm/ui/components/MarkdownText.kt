package com.tang.prm.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    baseFontSize: Float = 12f,
    baseColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    val lines = text.split("\n")
    val baseStyle = SpanStyle(
        color = baseColor,
        fontSize = baseFontSize.sp,
        fontFamily = FontFamily.Monospace
    )
    val boldStyle = baseStyle.copy(fontWeight = FontWeight.Bold)
    val italicStyle = baseStyle.copy(fontStyle = FontStyle.Italic)
    val headerStyle = baseStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = (baseFontSize + 2).sp
    )

    Column(modifier = modifier) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            when {
                line.trimStart().startsWith("## ") -> {
                    Text(
                        text = line.trimStart().removePrefix("## ").trim(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = baseColor
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                line.trimStart().startsWith("# ") -> {
                    Text(
                        text = line.trimStart().removePrefix("# ").trim(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = baseColor
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                line.trimStart().startsWith("- ") || line.trimStart().startsWith("• ") -> {
                    val content = line.trimStart().removePrefix("- ").removePrefix("• ")
                    Text(
                        text = buildAnnotatedString {
                            withStyle(baseStyle) { append("• ") }
                            appendInlineMarkdown(content, baseStyle, boldStyle, italicStyle)
                        }
                    )
                }
                line.trimStart().startsWith("⚠") || line.trimStart().startsWith("⚠️") -> {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(baseStyle.copy(color = MaterialTheme.colorScheme.error)) {
                                append(line.trim())
                            }
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                else -> {
                    Text(
                        text = buildAnnotatedString {
                            appendInlineMarkdown(line, baseStyle, boldStyle, italicStyle)
                        }
                    )
                }
            }
            i++
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendInlineMarkdown(
    text: String,
    baseStyle: SpanStyle,
    boldStyle: SpanStyle,
    italicStyle: SpanStyle
) {
    var pos = 0
    while (pos < text.length) {
        when {
            pos + 1 < text.length && text[pos] == '*' && text[pos + 1] == '*' -> {
                val end = text.indexOf("**", pos + 2)
                if (end != -1) {
                    withStyle(boldStyle) { append(text.substring(pos + 2, end)) }
                    pos = end + 2
                } else {
                    withStyle(baseStyle) { append(text[pos]) }
                    pos++
                }
            }
            text[pos] == '*' && (pos == 0 || text[pos - 1] != '*') && pos + 1 < text.length && text[pos + 1] != '*' -> {
                val end = text.indexOf('*', pos + 1)
                if (end != -1 && (end + 1 >= text.length || text[end + 1] != '*')) {
                    withStyle(italicStyle) { append(text.substring(pos + 1, end)) }
                    pos = end + 1
                } else {
                    withStyle(baseStyle) { append(text[pos]) }
                    pos++
                }
            }
            else -> {
                withStyle(baseStyle) { append(text[pos]) }
                pos++
            }
        }
    }
}

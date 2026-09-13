package org.emunix.unipatcher.ui.util

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.emunix.unipatcher.ui.model.LinkMatch

@Composable
fun resourceText(@StringRes resId: Int): AnnotatedString {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val linkColor = MaterialTheme.colorScheme.primary
    return remember(resId, context, uriHandler, linkColor) {
        buildLinkifiedResource(context.resources.getText(resId), uriHandler, linkColor)
    }
}

private fun buildLinkifiedResource(
    spanned: CharSequence,
    uriHandler: UriHandler,
    linkColor: Color,
): AnnotatedString {
    val text = spanned.toString()
    val links = findLinks(text)

    return buildAnnotatedString {
        var lastIndex = 0
        for (link in links) {
            if (link.range.first < lastIndex) continue
            append(text.substring(lastIndex, link.range.first))
            pushLink(
                LinkAnnotation.Clickable(
                    tag = link.uri,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ),
                    linkInteractionListener = { uriHandler.openUri(link.uri) },
                )
            )
            append(text.substring(link.range))
            pop()
            lastIndex = link.range.last + 1
        }
        append(text.substring(lastIndex))

        if (spanned is Spanned) {
            spanned.getSpans(0, spanned.length, StyleSpan::class.java).forEach { span ->
                when (span.style) {
                    Typeface.BOLD -> addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        spanned.getSpanStart(span),
                        spanned.getSpanEnd(span),
                    )
                    Typeface.ITALIC -> addStyle(
                        SpanStyle(fontStyle = FontStyle.Italic),
                        spanned.getSpanStart(span),
                        spanned.getSpanEnd(span),
                    )
                    Typeface.BOLD_ITALIC -> addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                        spanned.getSpanStart(span),
                        spanned.getSpanEnd(span),
                    )
                }
            }
        }
    }
}

fun findLinks(text: String): List<LinkMatch> {
    val urlRegex = Regex("""https?://[\w\-._~:/?#\[\]@!$&'()*+,;=%]+""")
    val emailRegex = Regex("""[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*""")

    val links = mutableListOf<LinkMatch>()
    urlRegex.findAll(text).forEach { match ->
        links += LinkMatch(match.range, match.value)
    }
    emailRegex.findAll(text).forEach { match ->
        if (links.none { it.range.first in match.range }) {
            links += LinkMatch(match.range, "mailto:${match.value}")
        }
    }
    return links.sortedBy { it.range.first }
}
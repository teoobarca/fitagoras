package dev.hamper.tabula.ui

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import dev.hamper.tabula.math.nb

/**
 * Every plain text of the app, set as Slovak is set ([nb]): a one-letter word never ends a line ("Euklidov algoritmus
 * a / Bézout"), a number keeps its word ("30 / bodov"), a date, a range or a name with its number stays whole. The
 * screens import this instead of Material's Text, so no screen can forget it (inspector, 18. 11.: the rule lived in
 * Rich and the buttons, the names on Cesta and the lines of Nastavenia went without it); the button audit sees every
 * text that breaks one anyway ([LabelAudit.split]).
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
    autoSize: TextAutoSize? = null,
) {
    val set = remember(text) { text.nb() }
    androidx.compose.material3.Text(
        set, modifier = modifier, color = color, autoSize = autoSize, fontSize = fontSize, fontStyle = fontStyle, fontWeight = fontWeight,
        fontFamily = fontFamily, letterSpacing = letterSpacing, textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight,
        overflow = overflow, softWrap = softWrap, maxLines = maxLines, minLines = minLines, style = style,
        onTextLayout = { r -> onTextLayout?.invoke(r); if (softWrap && maxLines > 1) LabelAudit.split(r) },
    )
}

/** A text built of spans (a caption with its words marked): drawn as given, its breaks still seen by the audit. */
@Composable
fun Text(
    text: androidx.compose.ui.text.AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    inlineContent: Map<String, androidx.compose.foundation.text.InlineTextContent> = emptyMap(),
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) = androidx.compose.material3.Text(
    text, modifier = modifier, color = color, textAlign = textAlign, overflow = overflow, softWrap = softWrap, maxLines = maxLines,
    inlineContent = inlineContent, style = style, onTextLayout = { r -> onTextLayout?.invoke(r) },
)

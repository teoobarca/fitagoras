package dev.hamper.tabula.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * The widest the app is drawn. A wider window (the web on a computer, a tablet, a phone on its side) gets the app as a
 * column of this width in its middle, from the top of the window to its bottom, with a calm tone around it (Teo, 20. 11.:
 * "zúžilo na šírku mobilu"). 480 dp is a big phone (the largest are 430 to 450 dp wide), so every screen keeps the
 * proportions it was made for at 360 to 430 dp: a line of text stays under about 60 characters, the answer options and
 * the GEM matrix keep their size. A window up to this width looks exactly as before.
 */
val AppWidth = 480.dp

/**
 * A dialog in the column, never wider than the app around it: 20 dp from each edge of it, the margin Compose itself
 * leaves around a dialog on the web, so a window up to [AppWidth] gets the same dialog as before.
 */
val DialogWidth = AppWidth - 40.dp

/** The tone around the column: one step darker than the page, in both themes, so the column reads as the app. */
val Palette.around: Color get() = if (dark) Color(0xFF111513) else Color(0xFFEBE4D6)

/**
 * The app's column ([AppWidth]) in the middle of whatever window it is given. The root of the app ([dev.hamper.tabula.App])
 * and every dialog that is a screen of its own (a full card, the tutor, marking lessons done) draw through this, so all
 * of them stand in the same column. In a wider window the sides take the system's side insets (a phone on its side, its
 * cutout and three-button bar), so the screens inside never pad for an edge they are not at.
 */
@Composable
fun AppColumn(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val c = T.c
    Box(modifier.fillMaxSize().background(c.around).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
        Box(
            Modifier.align(Alignment.TopCenter).widthIn(max = AppWidth).fillMaxSize()
                // a hairline on each side, drawn just outside the column: out of the window when the column fills it
                .drawBehind {
                    val w = 1.dp.toPx()
                    drawRect(c.aroundLine, Offset(-w, 0f), Size(w, size.height))
                    drawRect(c.aroundLine, Offset(size.width, 0f), Size(w, size.height))
                }
                .background(c.bg),
            content = content,
        )
    }
}

/** The hairline between the column and the tone around it: softer than a card's frame, there but never a frame. */
val Palette.aroundLine: Color get() = if (dark) Color(0xFF2A322F) else Color(0xFFDFD5C4)

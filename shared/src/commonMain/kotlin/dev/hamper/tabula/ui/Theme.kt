package dev.hamper.tabula.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.math.LocalMathFace
import dev.hamper.tabula.math.SkriptaMath
import dev.hamper.tabula.math.LocalMathInks
import dev.hamper.tabula.math.MathFace
import dev.hamper.tabula.math.MathInks
import dev.hamper.tabula.math.LocalReading
import dev.hamper.tabula.math.Reading

/** One colour family: a fill, its darker shade, two quiet tints and a version safe for text. */
@Immutable
data class Hue(val main: Color, val edge: Color, val tint: Color, val tint2: Color, val text: Color)

/**
 * Same product, two palettes: paper (light) and a chalkboard (dark), the FITagoras style (design review 17. 11.,
 * its style notes outside the repository). One accent, green ([lesson]): the main button, the bar, the active tab, a right
 * answer. Terracotta ([bad]) only for a mistake. Nothing else is coloured, except meaning inside pictures: u, v, the
 * named objects and the hues [gem], [mem], [paper], which only pictures use.
 */
@Immutable
data class Palette(
    val dark: Boolean,
    /** bg: the screen; card: cards, the bar, options; sand: the sunk track of a bar, the stage after an answer;
     *  line: 1 dp frame of a card; edge: the frame of a ghost button (rule). */
    val bg: Color, val card: Color, val sand: Color, val line: Color, val edge: Color,
    val ink: Color, val ink2: Color, val ink3: Color, val dot: Color, val axis: Color,
    val u: Color, val v: Color, val vText: Color, val target: Color,
    val good: Color, val goodTint: Color, val bad: Color, val badTint: Color,
    val lesson: Hue, val gem: Hue, val mem: Hue, val paper: Hue,
    val cream: Color, val creamEdge: Color, val onCream: Color,
    /** The named objects of a statement (P, Q, R): muted, the same in text, formula and picture. */
    val objA: Color, val objB: Color, val objC: Color,
    /** The thin line under a tappable symbol and the low marker under a key word: there, but never loud. */
    val tapLine: Color, val keyMark: Color,
    /** Text on the accent and on terracotta; terracotta text ("Tentoraz nie.") and the frame of a button on a mistake. */
    val onAcc: Color, val onBad: Color, val badInk: Color, val badLine: Color,
    /** Pictures: one green family. A disk ("the board") with its light, chalk, the mint glow of the last move, labels. */
    val art: Art,
    /** How much of DML's indigo [lesson] carries: 0 everywhere but the course's own parts of Dnes (ui/Smer.kt, [withCourse]). */
    val courseShare: Float = 0f,
)

/** The illustration colours (the style notes, 6): the clock disk, chalk, glow, the rings around the hero, small tiles, the mascot. */
@Immutable
data class Art(
    val disk: Color, val diskHi: Color, val chalk: Color, val glow: Color, val label: Color,
    val r1: Color, val r2: Color, val r3: Color, val r4: Color,
    /** A small picture: its tile, the shape on it, dots on the shape, a line that glows. */
    val tile: Color, val shape: Color, val shapeDot: Color, val glowOn: Color,
    /** Pytagoras sits in a circle of the sheet's colour. */
    val mascotGood: Color, val mascotBad: Color,
    /**
     * The notebook (ArtStyle.Zosit): squared paper, its grid and margin, the pencil's line, the highlighter under what
     * matters, the green that circles the goal, and the pencil lying on the page.
     */
    val paper: Color, val grid: Color, val margin: Color, val pencil: Color, val marker: Color, val circled: Color,
    val pencilBody: Color, val pencilWood: Color,
)

private val green = Hue(Color(0xFF2B6656), Color(0xFF235647), Color(0xFFDFEBE3), Color(0xFFEDF3EE), Color(0xFF235647))
private val greenDark = Hue(Color(0xFF8FD0B6), Color(0xFF9BD8BF), Color(0xFF24382F), Color(0xFF1E2B26), Color(0xFF9BD8BF))

val Hrejivy = Palette(
    dark = false,
    bg = Color(0xFFF5F0E6), card = Color(0xFFFFFDF8), sand = Color(0xFFECE5D8), line = Color(0xFFE4DACA), edge = Color(0xFFD3C7B3),
    ink = Color(0xFF211F1B), ink2 = Color(0xFF5D564C), ink3 = Color(0xFF7F776B), dot = Color(0xFFD3C7B3), axis = Color(0xFFC2B59F),
    u = Color(0xFF2B6656), v = Color(0xFFBF8420), vText = Color(0xFF8A5E14), target = Color(0xFF211F1B),
    good = Color(0xFF2B6656), goodTint = Color(0xFFDFEBE3), bad = Color(0xFFB4512F), badTint = Color(0xFFF7E5DA),
    lesson = green,
    gem = Hue(Color(0xFFAD5A35), Color(0xFF884326), Color(0xFFF6E2D3), Color(0xFFFBF0E6), Color(0xFF984C2A)),
    mem = Hue(Color(0xFF5A5E98), Color(0xFF434676), Color(0xFFE5E3F0), Color(0xFFF1EFF4), Color(0xFF4E5290)),
    paper = Hue(Color(0xFF96691A), Color(0xFF745013), Color(0xFFF3E6CB), Color(0xFFFAF2E1), Color(0xFF80590F)),
    cream = Color(0xFF211F1B), creamEdge = Color(0xFF16130F), onCream = Color(0xFFFFFDF8),
    objA = Color(0xFF2B6656), objB = Color(0xFF96691A), objC = Color(0xFF984C2A),
    tapLine = Color(0xFFC9BCA6), keyMark = Color(0xFFF5E7C6),
    onAcc = Color(0xFFFFFDF8), onBad = Color(0xFFFFFDF8), badInk = Color(0xFF953F22), badLine = Color(0xFFD9B8A6),
    art = Art(
        disk = Color(0xFF245E4F), diskHi = Color(0xFF3E8770), chalk = Color(0xFFFFFDF8), glow = Color(0xFF9DEBC7), label = Color(0xFF163D32),
        r1 = Color(0xFFD2E4D7), r2 = Color(0xFFBBD7C4), r3 = Color(0xFFA0C6AF), r4 = Color(0xFF82B298),
        tile = Color(0xFFBBD7C4), shape = Color(0xFF245E4F), shapeDot = Color(0xFFFFFDF8), glowOn = Color(0xFF245E4F),
        mascotGood = Color(0xFFC9DFD0), mascotBad = Color(0xFFF0D3C3),
        paper = Color(0xFFFBF7EE), grid = Color(0xFFEBE2D1), margin = Color(0xFFE9C6B8), pencil = Color(0xFF24493F),
        marker = Color(0xFFCFE7D6), circled = Color(0xFF2B6656), pencilBody = Color(0xFF2B6656), pencilWood = Color(0xFFE6C79C),
    ),
)

val Tmavy = Palette(
    dark = true,
    // card, sunk track and 1 dp line lifted (sweep of 18. 11.: outlines of keys and options at 1.3 to 1.45:1, the bar's
    // track at 1.35:1 were hard to see on the dark page)
    bg = Color(0xFF181D1B), card = Color(0xFF232B28), sand = Color(0xFF37423E), line = Color(0xFF45514B), edge = Color(0xFF5A6761),
    ink = Color(0xFFEEEBE3), ink2 = Color(0xFFB3BAB4), ink3 = Color(0xFF8E9690), dot = Color(0xFF46524C), axis = Color(0xFF5A6660),
    u = Color(0xFF93C8F0), v = Color(0xFFF4B48E), vText = Color(0xFFF4B48E), target = Color(0xFFC6BBF0),
    good = Color(0xFF8FD0B6), goodTint = Color(0xFF24382F), bad = Color(0xFFF0A486), badTint = Color(0xFF3B2A24),
    lesson = greenDark,
    gem = Hue(Color(0xFFF4B48E), Color(0xFFC48A68), Color(0xFF4A3D37), Color(0xFF3A3431), Color(0xFFF4B48E)),
    mem = Hue(Color(0xFFC6BBF0), Color(0xFF9B90C4), Color(0xFF403C52), Color(0xFF363444), Color(0xFFC6BBF0)),
    paper = Hue(Color(0xFFF3DD9B), Color(0xFFC2AE74), Color(0xFF47432F), Color(0xFF3A382D), Color(0xFFF3DD9B)),
    cream = Color(0xFFEEEBE3), creamEdge = Color(0xFFB9B4A8), onCream = Color(0xFF181D1B),
    objA = Color(0xFF8CC7B8), objB = Color(0xFFE2BF7C), objC = Color(0xFFE8A88A),
    tapLine = Color(0xFF5E6A64), keyMark = Color(0xFF3F4A35),
    onAcc = Color(0xFF0F1E18), onBad = Color(0xFF1B1411), badInk = Color(0xFFF3AE93), badLine = Color(0xFF6A4E43),
    art = Art(
        disk = Color(0xFF1C473B), diskHi = Color(0xFF2F6D5A), chalk = Color(0xFFF1EDE4), glow = Color(0xFF9FEFCA), label = Color(0xFFCFE6DA),
        r1 = Color(0xFF273D34), r2 = Color(0xFF2C463B), r3 = Color(0xFF325044), r4 = Color(0xFF395A4D),
        tile = Color(0xFF2C463B), shape = Color(0xFF7CC3A5), shapeDot = Color(0xFF183029), glowOn = Color(0xFF7CC3A5),
        mascotGood = Color(0xFF2E463D), mascotBad = Color(0xFF4A342C),
        paper = Color(0xFF1E2522), grid = Color(0xFF2A3330), margin = Color(0xFF4B3A33), pencil = Color(0xFFB4DECB),
        marker = Color(0xFF2D4C40), circled = Color(0xFF8FD0B6), pencilBody = Color(0xFF8FD0B6), pencilWood = Color(0xFFC6A47C),
    ),
)

val LocalPalette = staticCompositionLocalOf { Hrejivy }
/** The colour family of the work on screen: lesson, GEM, memory or paper. */
val LocalHue = compositionLocalOf<Hue?> { null }

object T {
    val c: Palette @Composable get() = LocalPalette.current
    /** One accent everywhere (the style notes): the kind of work no longer colours buttons, bars or picks. */
    val hue: Hue @Composable get() = LocalPalette.current.lesson
}

private fun sans(weight: Int, opsz: Float = 18f) = fontFile(
    "google_sans_flex", weight = FontWeight(weight), style = FontStyle.Normal,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight), FontVariation.opticalSizing(opsz.sp), FontVariation.Setting("ROND", 100f),
    ),
)

/** Digits on a clock and labels in pictures: the TeX roman of the formulas, so a 3 on the clock is the 3 in the formula. */
val TexDigits = FontFamily(fontFile("mjx_tex_n", weight = FontWeight.Normal, style = FontStyle.Normal, variationSettings = FontVariation.Settings()))

val Sans = FontFamily(sans(400), sans(500), sans(600), sans(700), sans(750))
private val Display = FontFamily(sans(700, 32f), sans(750, 36f), sans(600, 32f))

private val trim = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
private fun s(fam: FontFamily, w: Int, size: Int, lh: Int, track: Double = 0.0) = TextStyle(
    fontFamily = fam, fontWeight = FontWeight(w), fontSize = size.sp, lineHeight = lh.sp,
    letterSpacing = track.em, lineHeightStyle = trim, platformStyle = noFontPadding(),
)

/**
 * Seven steps (the style notes, 2, adjusted after the blind comparison of 17. 11.: the problem and the reading text larger, as
 * before): 26/750 the verdict, 26/700 a question or an item's title, 24/700 a card's title, 20/700 a section, 17/700
 * buttons, 17 reading text in lessons, 16 text and items, 14/500 metadata, 13/600 kickers and the tab bar. A screen of
 * its own (Nácvik, the end of a lesson) keeps a larger title.
 */
val TabulaType = Typography(
    displayLarge = s(Display, 700, 56, 60, -0.02),
    displayMedium = s(Display, 700, 40, 46, -0.015),
    displaySmall = s(Display, 700, 32, 38, -0.016),
    headlineLarge = s(Display, 700, 28, 32, -0.018),
    headlineMedium = s(Display, 700, 24, 28, -0.016),
    headlineSmall = s(Display, 700, 26, 31, -0.014),
    titleLarge = s(Display, 700, 20, 25, -0.006),
    titleMedium = s(Sans, 600, 16, 22),
    titleSmall = s(Sans, 600, 16, 22),
    bodyLarge = s(Sans, 400, 17, 25),
    bodyMedium = s(Sans, 400, 16, 23),
    bodySmall = s(Sans, 500, 14, 20),
    labelLarge = s(Sans, 700, 17, 22, 0.005),
    labelMedium = s(Sans, 600, 13, 18, 0.01),
    labelSmall = s(Sans, 600, 12, 16),
)

/** The verdict of an answer on its sheet: "Správne.", "Tentoraz nie." */
val VerdictBase = s(Display, 750, 26, 30, -0.014)
/** The verdict in the direction of the look on screen (ui/Smer.kt). */
val VerdictStyle: TextStyle get() = smerVerdict()

val TabulaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

enum class ThemeMode { System, Light, Dark }

/** Formulas look as in the skriptá: their MathJax TeX font, set by TeX's rules. */
val Math = SkriptaMath

@Composable
fun TabulaTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) { ThemeMode.System -> isSystemInDarkTheme(); ThemeMode.Dark -> true; ThemeMode.Light -> false }
    val smer = SmerNow
    // one look in every course: the course's own colour is only on Dnes (ui/Smer.kt, withCourse)
    val c = smerPalette(dark)
    val main = c.lesson.main
    val scheme = remember(dark, smer, main) {
        if (dark) darkColorScheme(
            primary = c.lesson.main, onPrimary = c.onAcc, secondary = c.lesson.main, tertiary = c.good, error = c.bad,
            background = c.bg, onBackground = c.ink, surface = c.bg, onSurface = c.ink, surfaceVariant = c.sand, onSurfaceVariant = c.ink2,
            surfaceContainerLowest = c.bg, surfaceContainerLow = c.card, surfaceContainer = c.card, surfaceContainerHigh = c.card, surfaceContainerHighest = c.sand,
            outline = c.axis, outlineVariant = c.line, surfaceTint = Color.Transparent,
        ) else lightColorScheme(
            primary = c.lesson.main, onPrimary = c.onAcc, secondary = c.lesson.main, tertiary = c.good, error = c.bad,
            background = c.bg, onBackground = c.ink, surface = c.bg, onSurface = c.ink, surfaceVariant = c.sand, onSurfaceVariant = c.ink2,
            surfaceContainerLowest = c.card, surfaceContainerLow = c.card, surfaceContainer = c.card, surfaceContainerHigh = c.card, surfaceContainerHighest = c.sand,
            outline = c.axis, outlineVariant = c.line, surfaceTint = Color.Transparent,
        )
    }
    val inks = remember(dark, smer) { MathInks(c.u.toArgb(), c.vText.toArgb(), c.bad.toArgb(), c.good.toArgb(), c.objA.toArgb(), c.objB.toArgb(), c.objC.toArgb(), c.tapLine.toArgb()) }
    val reading = remember(dark, smer) { Reading(c.keyMark) }
    CompositionLocalProvider(LocalPalette provides c, LocalMathInks provides inks, LocalMathFace provides Math, LocalReading provides reading) {
        MaterialExpressiveTheme(colorScheme = scheme, typography = smerType(), shapes = TabulaShapes, motionScheme = MotionScheme.standard(), content = content)
    }
}

package dev.hamper.tabula.ui

import androidx.compose.material3.Typography
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawBehind

/*
 * Three directions of the look for Teo to choose from (18. 11.: today's look "príliš sterilný", "slabá hierarchia", the
 * end of a lesson "mid"; "go bold, ale nie 100 % playful", screens with a task stay on the math). Each is drawn on the
 * three screens that carry it: Dnes, a lesson step with its feedback, the end of a lesson. Teo, 18. 11.: the look
 * stays as it is and no proposal goes into the app; the directions stay here for comparison screenshots only
 * (-Psnap.smer). Nothing a student can reach sets them, and a record saved by 1.5.6 with one is read without it. Light palettes only: the dark one stays
 * today's for all of them until a direction is chosen.
 */
enum class Smer(val label: String) {
    /** Today's look (1.5.5). */
    Dnes("Dnešný"),
    /** A: the warmth of a notebook. Squared paper under the pages, blue ink, a red pencil for mistakes, the sketches large, Pytagoras in measure. */
    Zosit("A · Zošit"),
    /** B: typesetting. The math is the picture: the lesson's key statement set large in the skriptá's TeX type, black on white, one ultramarine. */
    Sadzba("B · Sadzba"),
    /** C: bold hierarchy. Today's warmth, but one dominant block per screen in full green, a large contrast of sizes. */
    Odvazna("C · Odvážny"),
    /** C+: C with A's warmth (Teo, 18. 11.): the sketch large on a scrap of squared paper, Pytagoras in measure on Dnes and at the end. */
    CPlus("C+"),
}

/**
 * The course open on Dnes: its colour marks only the course's own parts of Dnes, the underline of its tab and the card
 * of its lesson (Teo, 18. 11.: "jasnejšie, ktorý predmet je aký ... iná farba"; 19. 11.: "možno stačí iba home screen",
 * "spravme minimálny ten diff"). LA1 green, DML an indigo of the same depth. Everywhere else, in a lesson, on the Cesta,
 * the tab Test, the tutor and Settings, both courses look the same, in the one green: the app is one, not two
 * recoloured. Meaning stays the same in both: a right answer is green, a mistake terracotta ([goodFill]).
 */
var CourseAccentNow by mutableStateOf("la1")

private val indigo = Hue(Color(0xFF3B4C9B), Color(0xFF2F3D7E), Color(0xFFE2E6F5), Color(0xFFF0F2FA), Color(0xFF34448C))
private val indigoDark = Hue(Color(0xFFAEB9EE), Color(0xFFBAC4F2), Color(0xFF2A3150), Color(0xFF23283E), Color(0xFFBAC4F2))

/** [p] with the colour of the course open on Dnes: the hue of the way on and the ink of the pictures (Dnes only). */
fun withCourse(p: Palette): Palette {
    val share = dmlShare()
    if (share <= 0f) return p
    val d = if (p.dark) indigoDark else indigo
    val g = p.lesson
    fun l(a: Color, b: Color) = androidx.compose.ui.graphics.lerp(a, b, share)
    val h = Hue(l(g.main, d.main), l(g.edge, d.edge), l(g.tint, d.tint), l(g.tint2, d.tint2), l(g.text, d.text))
    // the pictures' ink turns over with the accent's second half, so a sketch never stands in two colours at once
    return if (share < .5f) p.copy(lesson = h, courseShare = share) else p.copy(
        lesson = h, courseShare = share,
        art = p.art.copy(shape = d.main, disk = d.edge, diskHi = d.main, circled = d.main, pencil = if (p.dark) d.text else d.edge, pencilBody = d.main,
            tile = d.tint, glowOn = d.main, marker = d.tint, r1 = d.tint2, r2 = d.tint, r3 = d.tint, r4 = d.tint),
    )
}

/**
 * A switch of course in progress (App.kt, Home): the course it comes from and how far it is (0 to 1, one curve with
 * the picture of the old page fading). The colour of Dnes's course parts is lerped by it, so they turn with the page.
 */
var AccentFrom by mutableStateOf("la1")
var AccentT by androidx.compose.runtime.mutableFloatStateOf(1f)

/** How much of DML's indigo the accent has now: 0 LA1, 1 DML, in between while a switch runs. */
fun dmlShare(): Float {
    val to = if (CourseAccentNow == "dml") 1f else 0f
    val from = if (AccentFrom == "dml") 1f else 0f
    return from + (to - from) * AccentT.coerceIn(0f, 1f)
}

/** The fill of a right answer's panel in C+: green in both courses, a deep green in the dark. */
fun goodFill(c: Palette): Color = if (c.dark) Color(0xFF1E3A30) else c.good

/** C and C+: one full-green block per screen. */
val Smer.bold get() = this == Smer.Odvazna || this == Smer.CPlus

/** The green of C's block: the accent on paper; in the dark a deep green, the dark theme's mint would not carry white type. */
fun boldFill(c: Palette): Color = if (!c.dark) c.lesson.main else androidx.compose.ui.graphics.lerp(Color(0xFF1E3A30), Color(0xFF272D52), c.courseShare)

/** The direction on screen: always today's in the app; only snapshots set another (-Psnap.smer). */
var SmerNow by mutableStateOf(Smer.CPlus)


private val blue = Hue(Color(0xFF2E4F9E), Color(0xFF243F80), Color(0xFFDDE5F5), Color(0xFFEEF2FA), Color(0xFF27448A))
private val ultramarine = Hue(Color(0xFF1D3FB8), Color(0xFF16308E), Color(0xFFE2E7F8), Color(0xFFF1F3FB), Color(0xFF1D3FB8))
private val boldGreen = Hue(Color(0xFF1C6B4E), Color(0xFF145239), Color(0xFFD6EADF), Color(0xFFEAF4EE), Color(0xFF165A41))

/** A: paper, blue-black ink, blue for the way on and a right answer, the red pencil for a mistake. */
val ZositPalette = Hrejivy.copy(
    bg = Color(0xFFF6F1E6), card = Color(0xFFFFFEF9), ink = Color(0xFF1C2640), ink2 = Color(0xFF48526B), ink3 = Color(0xFF747C8E),
    good = blue.main, goodTint = blue.tint, bad = Color(0xFFC0392B), badTint = Color(0xFFF8E2DC), badInk = Color(0xFFA5281B), badLine = Color(0xFFE2B4AB),
    lesson = blue, u = blue.main, objA = blue.main, keyMark = Color(0xFFFFF0A8),
    art = Hrejivy.art.copy(
        paper = Color(0xFFFFFEF9), grid = Color(0xFFDCE3EE), margin = Color(0xFFE7A9A0), pencil = Color(0xFF243F80),
        marker = Color(0xFFFFF0A8), circled = Color(0xFFC0392B), shape = blue.main, disk = blue.edge, diskHi = blue.main,
        tile = blue.tint, mascotGood = blue.tint, glowOn = blue.main,
    ),
)

/** B: white paper, black type, one ultramarine; a mistake in a dark red. */
val SadzbaPalette = Hrejivy.copy(
    bg = Color(0xFFFBFAF7), card = Color(0xFFFFFFFF), sand = Color(0xFFEFEDE8), line = Color(0xFFE2DFD8), edge = Color(0xFFCFCBC2),
    ink = Color(0xFF111111), ink2 = Color(0xFF4A4740), ink3 = Color(0xFF77736A),
    good = ultramarine.main, goodTint = ultramarine.tint, bad = Color(0xFFB3261E), badTint = Color(0xFFF9E3E0), badInk = Color(0xFF9C1F18), badLine = Color(0xFFE3B5B1),
    lesson = ultramarine, u = ultramarine.main, objA = ultramarine.main, keyMark = Color(0xFFE2E7F8),
    art = Hrejivy.art.copy(
        paper = Color(0xFFFFFFFF), grid = Color(0xFFEAE8E3), pencil = Color(0xFF111111), circled = ultramarine.main, marker = ultramarine.tint,
        shape = ultramarine.main, disk = ultramarine.edge, diskHi = ultramarine.main, tile = ultramarine.tint, mascotGood = ultramarine.tint, glowOn = ultramarine.main,
    ),
)

/** C: today's warm paper, a deeper green, darker ink. */
val OdvaznaPalette = Hrejivy.copy(
    bg = Color(0xFFF3EEE3), ink = Color(0xFF161512), good = boldGreen.main, goodTint = boldGreen.tint, lesson = boldGreen, u = boldGreen.main,
    art = Hrejivy.art.copy(circled = boldGreen.main, pencilBody = boldGreen.main),
)

/** The palette inside a full-colour block of C ([fill]): white type, white buttons with the block's colour on them. */
fun solidOn(c: Palette, fill: Color): Palette {
    val w = Color(0xFFFFFDF8)
    val soft = w.copy(alpha = .82f)
    return c.copy(
        card = Color.White.copy(alpha = .12f), sand = Color.White.copy(alpha = .22f), line = Color.White.copy(alpha = .28f), edge = Color.White.copy(alpha = .55f),
        ink = w, ink2 = soft, ink3 = w.copy(alpha = .66f), good = w, bad = w, badInk = w, badLine = w.copy(alpha = .55f),
        // in the dark a pale button, not paper white, which glared: the block's own tone, indigo on DML's card on Dnes,
        // green everywhere else
        lesson = Hue(if (!c.dark) w else if (fill == goodFill(c)) Color(0xFFD9E8DF) else androidx.compose.ui.graphics.lerp(Color(0xFFD9E8DF), Color(0xFFDDE1F6), c.courseShare), w, Color.White.copy(alpha = .18f), Color.White.copy(alpha = .12f), w), onAcc = fill, onBad = fill,
        art = c.art.copy(mascotGood = Color.White.copy(alpha = .2f), mascotBad = Color.White.copy(alpha = .2f)),
    )
}

/** The palette of the direction on screen, light; dark stays today's. */
fun smerPalette(dark: Boolean): Palette = if (dark) Tmavy else when (SmerNow) {
    Smer.Dnes -> Hrejivy; Smer.Zosit -> ZositPalette; Smer.Sadzba -> SadzbaPalette; Smer.Odvazna, Smer.CPlus -> OdvaznaPalette
}

/** The skriptá's TeX roman, for B's headings: the type of the formulas is the type of the page. */
val TexSerif = FontFamily(fontFile("mjx_tex_n", weight = FontWeight.Normal, style = FontStyle.Normal, variationSettings = FontVariation.Settings()))

private fun Typography.serifHeads(): Typography {
    fun TextStyle.serif(size: Int, lh: Int) = copy(fontFamily = TexSerif, fontWeight = FontWeight.Normal, fontSize = size.sp, lineHeight = lh.sp, letterSpacing = 0.sp)
    return copy(
        displayLarge = displayLarge.serif(60, 64), displayMedium = displayMedium.serif(46, 50), displaySmall = displaySmall.serif(38, 42),
        headlineLarge = headlineLarge.serif(32, 36), headlineMedium = headlineMedium.serif(30, 34), headlineSmall = headlineSmall.serif(28, 32),
        titleLarge = titleLarge.serif(23, 28),
    )
}

private fun Typography.bigger(): Typography = copy(
    displaySmall = displaySmall.copy(fontSize = 42.sp, lineHeight = 46.sp),
    headlineMedium = headlineMedium.copy(fontSize = 32.sp, lineHeight = 36.sp),
)

private val SerifType = TabulaType.serifHeads()
private val BoldType = TabulaType.bigger()

/** The type of the direction on screen. */
fun smerType(): Typography = when (SmerNow) { Smer.Sadzba -> SerifType; Smer.Odvazna, Smer.CPlus -> BoldType; else -> TabulaType }

private val SerifVerdict = VerdictBase.copy(fontFamily = TexSerif, fontWeight = FontWeight.Normal, fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = 0.sp)
private val BoldVerdict = VerdictBase.copy(fontSize = 32.sp, lineHeight = 36.sp)

/** The verdict's type in the direction on screen. */
fun smerVerdict(): TextStyle = when (SmerNow) { Smer.Sadzba -> SerifVerdict; Smer.Odvazna, Smer.CPlus -> BoldVerdict; else -> VerdictBase }

/** A's squared paper under a surface: the grid every [step], a red margin [margin] from the left when given. */
fun androidx.compose.ui.Modifier.squaredPaper(grid: Color, step: androidx.compose.ui.unit.Dp = 18.dp, margin: Color? = null, marginAt: androidx.compose.ui.unit.Dp = 30.dp) =
    drawBehind {
        val g = step.toPx()
        var x = g; while (x < size.width) { drawLine(grid, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), density * .8f); x += g }
        var y = g; while (y < size.height) { drawLine(grid, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), density * .8f); y += g }
        margin?.let { val m = marginAt.toPx(); drawLine(it, androidx.compose.ui.geometry.Offset(m, 0f), androidx.compose.ui.geometry.Offset(m, size.height), density * 1.4f) }
    }

/**
 * B's picture of a lesson: its key statement as one display formula (the core of its first theorem that has one, else
 * the first display line of a theorem's claim), null when the lesson states none.
 */
fun keyFormula(l: dev.hamper.tabula.data.LessonDef): String? {
    val ts = l.steps.filterIsInstance<dev.hamper.tabula.data.Step.Thm>().mapNotNull { dev.hamper.tabula.data.TheoremById[it.theorem] }
    return ts.firstNotNullOfOrNull { it.wording.core }
        ?: ts.firstNotNullOfOrNull { t -> t.wording.claim.filterIsInstance<dev.hamper.tabula.data.Block>().firstOrNull()?.tex }
}

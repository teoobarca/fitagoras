package dev.hamper.tabula.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * What Pytagoras is doing. One figure in every pose: white hair round a bare crown, a laurel wreath, a beard cut as a
 * right triangle (the right angle at its tip), a light chiton; flat shapes, no outlines, the face drawn with the icons'
 * line (1.8 at 64).
 */
enum class Mood {
    /** A right answer: eyes closed in a smile. */
    Pleased,
    /** A wrong answer: kind, not judging: brows up at the inner ends, a soft mouth, the head tilted a little. */
    Thinking,
    /** A guess before any explanation: a finger at the beard, eyes up towards a rising thought. */
    Pondering,
    /** The end of a lesson, done for today: both arms up. */
    Celebrating,
    /** A new start, nothing here yet: one hand up in greeting. */
    Greeting,
}

/** The face and robe are the same on paper and on the board: a character does not change skin in the dark. */
private object Body {
    val skin = Color(0xFFEFD3B4)
    val nose = Color(0xFFE3BC97)
    val hair = Color(0xFFFBF7EF)
    val robe = Color(0xFFF6F1E6)
    val fold = Color(0xFFE2D8C6)
    val moustache = Color(0xFFB9AE9C)
    val ink = Color(0xFF211F1B)
    /** The pencil outline of the notebook style: the same hand as the pictures (screens/Art.kt). */
    val outline = Color(0xFF2E4D43)
}

/** Pytagoras is outlined in pencil when the pictures are (ArtStyle.Zosit), so the app has one way of drawing. */
private val sketched get() = dev.hamper.tabula.screens.ArtStyleNow == dev.hamper.tabula.screens.ArtStyle.Zosit

private fun DrawScope.outline(path: Path, seed: Int) {
    if (sketched) drawPath(wobble(path, .45f, seed, 1.2f), Body.outline, style = Stroke(1.25f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private val parsed = HashMap<String, Path>()
private fun p(d: String): Path = parsed.getOrPut(d) { PathParser().parsePathString(d).toPath() }

/**
 * Pytagoras in [mood], in a circle of [bg] (the colour of the sheet he sits on). [size] grows with the font, a little
 * slower than the text (×(1 + 0.4·(scale − 1)), the style notes, 7), at most ×1.2. Under 48 dp the small details go (the
 * thought's dots), so he stays readable.
 */
@Composable
fun Pytagoras(mood: Mood, modifier: Modifier = Modifier, bg: Color = T.c.art.mascotGood, size: Dp = 64.dp) {
    val c = T.c
    // the wreath sits on the white hair: the board's green on paper, a mid green on the board
    val laurel = if (c.dark) Color(0xFF3E8B6D) else c.art.disk
    val accent = c.lesson.main
    val fs = LocalDensity.current.fontScale
    // at a large font he grows a little, never so much that the sheet covers the answers (at most 120 %)
    val s = size * (1f + .4f * (fs - 1f).coerceAtLeast(0f)).coerceAtMost(1.2f)
    // drawn once into its own layer: the pencil outline is not resampled on every frame of an animation around it
    Canvas(modifier.size(s).graphicsLayer().clearAndSetSemantics {}) {
        val small = this.size.width < 48.dp.toPx()
        scale(this.size.width / 64f, this.size.height / 64f, pivot = Offset.Zero) { figure(mood, bg, laurel, accent, small) }
    }
}

private fun DrawScope.line(d: String, c: Color = Body.ink, w: Float = 1.8f) =
    drawPath(p(d), c, style = Stroke(w, cap = StrokeCap.Round, join = StrokeJoin.Round))

private fun DrawScope.leaf(x: Float, y: Float, r: Float, c: Color) = rotate(r, Offset(x, y)) {
    drawOval(c, Offset(x - 2.1f, y - 1.05f), Size(4.2f, 2.1f))
}

/** An arm of the robe from a shoulder to a raised hand. */
private fun DrawScope.arm(from: Offset, hand: Offset) {
    drawLine(Body.robe, from, hand, 6.4f, StrokeCap.Round)
    drawCircle(Body.skin, 3.5f, hand)
}

private fun DrawScope.figure(mood: Mood, bg: Color, laurel: Color, accent: Color, small: Boolean) {
    drawCircle(bg, 30f, Offset(32f, 32f))
    val disk = Path().apply { addOval(androidx.compose.ui.geometry.Rect(2f, 2f, 62f, 62f)) }
    clipPath(disk) {
        // the chiton over the shoulders, a fold from the left shoulder; the neck under the beard
        drawPath(p("M7 66C8.5 52 18 44.5 32 44.5S55.5 52 57 66Z"), Body.robe)
        drawPath(p("M19 48.5C24 52 27 58 28 66H23.5C23 59 21 54 16.5 50Z"), Body.fold)
        outline(p("M7 66C8.5 52 18 44.5 32 44.5S55.5 52 57 66"), 1)
        drawRect(Body.skin, Offset(29f, 38f), Size(6f, 8f))
        when (mood) {
            Mood.Celebrating -> { arm(Offset(19f, 50f), Offset(12f, 27f)); arm(Offset(45f, 50f), Offset(52f, 27f)) }
            Mood.Greeting -> drawLine(Body.robe, Offset(45f, 50f), Offset(51f, 29f), 6.4f, StrokeCap.Round)
            Mood.Pondering -> drawLine(Body.robe, Offset(44f, 53f), Offset(37.5f, 45.5f), 5.2f, StrokeCap.Round)
            else -> {}
        }
    }
    // the head: one white shape for hair and side locks, the face inside it, a little tilted when he is sorry
    val tilt = if (mood == Mood.Thinking) -6f else 0f
    rotate(tilt, Offset(32f, 34f)) {
        drawOval(Body.hair, Offset(17f, 11f), Size(30f, 24f))
        drawOval(Body.skin, Offset(22f, 14.5f), Size(20f, 18f))
        outline(Path().apply { addOval(androidx.compose.ui.geometry.Rect(22f, 14.5f, 42f, 32.5f)) }, 3)
        drawCircle(Body.nose, 1.6f, Offset(32f, 27.6f))
        // the laurel: one wreath along the hairline, open only at the front
        for (side in listOf(-1f, 1f)) for (i in 0 until 5) {
            val deg = if (side < 0) 200f + i * 15f else 340f - i * 15f
            val a = deg * PI.toFloat() / 180f
            leaf(32f + 14.2f * cos(a), 23f + 11.4f * sin(a), deg + 90f - side * 24f, laurel)
        }
        // the beard: a right triangle, its right angle at the tip
        drawPath(p("M20 30H44L32 42Z"), Body.hair)
        drawPath(p("M20 30H44L32 42Z"), Body.hair, style = Stroke(1.6f, join = StrokeJoin.Round))
        outline(p("M19.4 30.6L32 42.6 44.6 30.6"), 4)
        drawPath(p("M25.4 31.4q3.3-2.7 6.6-.6 3.3-2.1 6.6.6-3.3 1.8-6.6.7-3.3 1.1-6.6-.7Z"), Body.moustache)
        when (mood) {
            Mood.Pleased -> {
                line("M25.4 25q2.1-2.3 4.2 0M34.4 25q2.1-2.3 4.2 0", w = 1.9f)
                line("M29.4 34.6q2.6 2.1 5.2 0", w = 1.7f)
            }
            Mood.Thinking -> {
                drawCircle(Body.ink, 1.6f, Offset(27.6f, 24.6f)); drawCircle(Body.ink, 1.6f, Offset(36.4f, 24.6f))
                line("M25 21.6q1.6-.2 4.2-1.5M39 21.6q-1.6-.2-4.2-1.5", w = 1.7f)
                line("M29.5 35.6q2.5-1 5 0", w = 1.7f)
            }
            Mood.Pondering -> {
                drawCircle(Body.ink, 1.6f, Offset(29.4f, 22.8f)); drawCircle(Body.ink, 1.6f, Offset(38.2f, 22.8f))
                line("M25.2 20.6q2.2-1 4.4-.4M34.6 20.2q2.2-.6 4.4.4", w = 1.7f)
                line("M32 35.4h3", w = 1.7f)
            }
            Mood.Celebrating -> {
                line("M25.4 24.6q2.1-2.4 4.2 0M34.4 24.6q2.1-2.4 4.2 0", w = 1.9f)
                drawPath(p("M28.8 33.8q3.2 4.8 6.4 0Z"), Body.ink)
            }
            Mood.Greeting -> {
                drawCircle(Body.ink, 1.6f, Offset(27.6f, 24.4f)); drawCircle(Body.ink, 1.6f, Offset(36.4f, 24.4f))
                line("M25.4 21q2.1-.9 4.2-.3M34.4 20.7q2.1-.6 4.2.3", w = 1.6f)
                line("M29.4 34.6q2.6 2.1 5.2 0", w = 1.7f)
            }
        }
    }
    when (mood) {
        Mood.Pondering -> {
            // a hand cupped under the beard's tip, the thumb along its edge: stroking the beard; the thought rising
            drawOval(Body.skin, Offset(29f, 41.2f), Size(8f, 4.6f))
            drawLine(Body.skin, Offset(35.6f, 42.4f), Offset(37.6f, 39.4f), 2.2f, StrokeCap.Round)
            if (!small) { drawCircle(accent, 1.2f, Offset(49f, 13f)); drawCircle(accent, 1.8f, Offset(53f, 9f)); drawCircle(accent, 2.4f, Offset(58f, 4f)) }
        }
        Mood.Greeting -> {
            // an open hand: a mitten with its thumb, two short waves beside it
            drawOval(Body.skin, Offset(48f, 22.4f), Size(6.2f, 7.2f))
            drawCircle(Body.skin, 1.7f, Offset(48.2f, 27.6f))
            if (!small) line("M56.4 21.6q1.8 2.8 0 5.6M59 19.6q3 4.6 0 9.6", accent, 1.8f)
        }
        Mood.Celebrating -> if (!small) line("M8.5 12v4.6M6.2 14.3h4.6M55.5 8.5v4.6M53.2 10.8h4.6", accent, 1.8f)
        else -> {}
    }
}

package dev.hamper.tabula.ui

import dev.hamper.tabula.compat.PRIMITIVE_CLICK
import dev.hamper.tabula.compat.PRIMITIVE_LOW_TICK
import dev.hamper.tabula.compat.PRIMITIVE_QUICK_RISE
import dev.hamper.tabula.compat.PRIMITIVE_SLOW_RISE
import dev.hamper.tabula.compat.PRIMITIVE_THUD
import dev.hamper.tabula.compat.PRIMITIVE_TICK
import dev.hamper.tabula.compat.HapticFeedbackConstants
import dev.hamper.tabula.compat.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import dev.hamper.tabula.compat.LocalView
import kotlinx.coroutines.delay

/*
 * How the app feels: one vocabulary of haptics and one of springs, so every screen answers the same way.
 * The spec, with its sources, is outside the repository (pocit/SPEC.md).
 */

/** One primitive of a rich haptic. [delay] is the pause after the previous primitive, in ms. */
data class Prim(val id: Int, val scale: Float, val delay: Int = 0)

/**
 * Every haptic the app plays. [rich] is the composition for phones whose actuator has all the primitives (Pixel);
 * elsewhere the platform constant [fallback] plays, tuned by the phone maker. Strength follows importance:
 * three levels only (0.5, 0.7, 1.0), and beats inside a pattern are at least 50 ms apart so they read as two.
 */
enum class Buzz(val rich: List<Prim>, val fallback: Int) {
    /**
     * A tap that moves around (a secondary button, a link, a row, a stepper): the phone's own key click (as Duolingo
     * does): every maker tunes it to the actuator, and it follows the phone's touch-feedback setting. No sound.
     */
    Tap(emptyList(), HapticFeedbackConstants.KEYBOARD_TAP),
    /** The main button of a screen (Pokračovať, Skontrolovať): the key click and the button's sound. */
    Button(emptyList(), HapticFeedbackConstants.KEYBOARD_TAP),
    /** An answer chosen: an option, a vector or a point of a picture, a cell. The key click and the tap's sound. */
    Choose(emptyList(), HapticFeedbackConstants.KEYBOARD_TAP),
    /** An option ticked or unticked (several to pick): as [Choose]. */
    Select(emptyList(), HapticFeedbackConstants.KEYBOARD_TAP),
    /** A key of the number pad: the same key click, on every key, at once. */
    Key(emptyList(), HapticFeedbackConstants.KEYBOARD_TAP),
    /** A slider step or a tab: frequent, so the faintest there is. */
    Tick(emptyList(), HapticFeedbackConstants.SEGMENT_FREQUENT_TICK),
    /** A right answer: a light beat, then a firm click, rising like the green does. */
    Right(listOf(Prim(PRIMITIVE_TICK, .5f), Prim(PRIMITIVE_CLICK, 1f, 60)), HapticFeedbackConstants.CONFIRM),
    /**
     * The 5th, 10th… right answer in a row in a lesson: the right answer's beat and then a short run of four, the
     * last one strongest (Duolingo marks its 5 and 10 combos with a ~0.6 s run of four beats).
     */
    // Teo, 18. 11.: denser, like Duolingo's chest opening: after the answer's beat a quick run of six rising beats
    // 50 ms apart (the shortest that still reads as separate), landing on a full click
    Series(listOf(Prim(PRIMITIVE_TICK, .5f), Prim(PRIMITIVE_CLICK, 1f, 60), Prim(PRIMITIVE_TICK, .5f, 110), Prim(PRIMITIVE_TICK, .5f, 50), Prim(PRIMITIVE_TICK, .7f, 50), Prim(PRIMITIVE_TICK, .7f, 50), Prim(PRIMITIVE_TICK, 1f, 50), Prim(PRIMITIVE_TICK, 1f, 50), Prim(PRIMITIVE_CLICK, 1f, 60)), HapticFeedbackConstants.CONFIRM),
    /** Right, but not at the first try ("Teraz je to správne."): the right answer's beat, a quieter sound, no series. */
    RightAgain(listOf(Prim(PRIMITIVE_TICK, .5f), Prim(PRIMITIVE_CLICK, .7f, 60)), HapticFeedbackConstants.CONFIRM),
    /** A wrong answer: two low knocks, in step with the shake. Never louder than a right one. */
    Wrong(listOf(Prim(PRIMITIVE_LOW_TICK, 1f), Prim(PRIMITIVE_LOW_TICK, .7f, 90)), HapticFeedbackConstants.REJECT),
    /** A wrong number in a GEM cell: one low knock and a soft sound; it happens often and is fixed at once. */
    Slip(listOf(Prim(PRIMITIVE_LOW_TICK, .7f)), HapticFeedbackConstants.REJECT),
    /** An exercise of several parts done: one firm click. */
    StepDone(listOf(Prim(PRIMITIVE_CLICK, .7f)), HapticFeedbackConstants.CONFIRM),
    /** One right number in a GEM cell. */
    Cell(listOf(Prim(PRIMITIVE_TICK, .7f)), HapticFeedbackConstants.CLOCK_TICK),
    /** "Prečo?" opens or closes. */
    Expand(listOf(Prim(PRIMITIVE_TICK, .5f)), HapticFeedbackConstants.CLOCK_TICK),
    /** A counted score lands on its number. */
    Land(listOf(Prim(PRIMITIVE_CLICK, .7f)), HapticFeedbackConstants.CONFIRM),
    /**
     * A lesson done, once per lesson: two quick beats growing (Teo, 18. 11.: like a chest opening in Duolingo) into a
     * firm click on the sound's chord, about 120 ms after the start, and a small echo on its second chord 0.65 s on
     * (lesson_done without its lead-in; before, a 0.5 s swell ran ahead of a sound that came late: "nie je sync").
     */
    Finish(listOf(Prim(PRIMITIVE_TICK, .5f), Prim(PRIMITIVE_TICK, .7f, 50), Prim(PRIMITIVE_CLICK, 1f, 50), Prim(PRIMITIVE_TICK, .5f, 500)), HapticFeedbackConstants.CONFIRM),
    /** Days in a row grew today, said once a day at the end of a lesson after its own sound: a rise and a click. */
    Streak(listOf(Prim(PRIMITIVE_SLOW_RISE, .5f), Prim(PRIMITIVE_CLICK, 1f, 60)), HapticFeedbackConstants.CONFIRM);

    /** What plays, in words, for the log Teo reads next to his Pixel. */
    fun describe(rich: Boolean): String =
        if (rich && this.rich.isNotEmpty()) this.rich.joinToString(" → ") { (if (it.delay > 0) "+${it.delay}ms " else "") + "${primName(it.id)} ${it.scale}" }
        else constName(fallback)

    companion object {
        /** Every primitive any pattern uses: if the phone lacks one, all patterns fall back together. */
        val primitives: IntArray get() = entries.flatMap { b -> b.rich.map { it.id } }.distinct().toIntArray()
    }
}

fun primName(id: Int) = when (id) {
    PRIMITIVE_CLICK -> "CLICK"; PRIMITIVE_TICK -> "TICK"; PRIMITIVE_LOW_TICK -> "LOW_TICK"; PRIMITIVE_THUD -> "THUD"
    PRIMITIVE_SLOW_RISE -> "SLOW_RISE"; PRIMITIVE_QUICK_RISE -> "QUICK_RISE"; else -> "PRIMITIVE_$id"
}

fun constName(c: Int) = when (c) {
    HapticFeedbackConstants.KEYBOARD_TAP -> "KEYBOARD_TAP"; HapticFeedbackConstants.SEGMENT_FREQUENT_TICK -> "SEGMENT_FREQUENT_TICK"
    HapticFeedbackConstants.CONFIRM -> "CONFIRM"; HapticFeedbackConstants.REJECT -> "REJECT"; HapticFeedbackConstants.SEGMENT_TICK -> "SEGMENT_TICK"
    HapticFeedbackConstants.CLOCK_TICK -> "CLOCK_TICK"; else -> "CONSTANT_$c"
}

/**
 * Plays [Buzz]es. The in-app "Haptika" switch ([enabled]) silences all of them; the phone's own touch-feedback
 * setting is kept too: constants go through performHapticFeedback, compositions as touch vibrations
 * (Android: shared/src/androidMain/.../ui/Haptics.android.kt; nothing vibrates on the desktop or the web).
 */
expect class Haptics(view: View, enabled: () -> Boolean) {
    val enabled: () -> Boolean
    fun play(b: Buzz)
    fun tap()
    fun key()
    fun tick()
    fun right()
    fun wrong()
    fun cell()
    fun expand()
    fun land()
    fun finish()
}

val LocalHapticsOn = compositionLocalOf<() -> Boolean> { { true } }

/** Where a desktop or web build reports the pattern it would play (tests listen here). */
object HapticTrace { var sink: ((Buzz) -> Unit)? = null }

/**
 * Right answers at the first try in a row; a lesson starts it at 0, a wrong answer resets it. Every 5th one plays
 * [Buzz.Series]; the sound climbs with it (ui/Sound.kt).
 */
object Series {
    var n = 0
    fun right(): Buzz = if (++n % 5 == 0) Buzz.Series else Buzz.Right
    fun wrong() { n = 0 }
}

/** An answer chosen (an option, a vector, a point, a cell): its click and the tap's sound. */
fun Haptics.choose() = play(Buzz.Choose)
/** The main button of a screen: its click and the button's sound (dropped when a verdict sounds right after it). */
fun Haptics.button() = play(Buzz.Button)
/** Right after a miss: no series, a quieter sound. */
fun Haptics.rightAgain() = play(Buzz.RightAgain)
/** A wrong GEM cell: breaks the series like any mistake. */
fun Haptics.slip() { Series.wrong(); play(Buzz.Slip) }
fun Haptics.stepDone() = play(Buzz.StepDone)
fun Haptics.streak() = play(Buzz.Streak)

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    val on = LocalHapticsOn.current
    return remember(view) { Haptics(view, on) }
}

/* ---- Motion ---------------------------------------------------------------------------------------------------- */

/** Springs from Material 3 (damping / stiffness). Bounce only for success; failure settles without overshoot. */
object Motion {
    /** Success pops: expressive fast spatial, 0.6 / 800. */
    fun <T> pop(): SpringSpec<T> = spring(.6f, 800f)
    /** Something arriving: expressive default spatial, 0.8 / 380. */
    fun <T> rise(): SpringSpec<T> = spring(.8f, 380f)
    /** Moving without a bounce: standard default spatial, 0.9 / 700. */
    fun <T> settle(): SpringSpec<T> = spring(.9f, 700f)
    /** Colour and alpha: effects default, no bounce, 1.0 / 1600. */
    fun <T> fade(): SpringSpec<T> = spring(Spring.DampingRatioNoBouncy, 1600f)
}

/** The phone's "Remove animations": decoration goes (pops, shakes, slides, confetti); colour and haptics stay. */
@Composable
expect fun calm(): Boolean

/** Rises into place [order] beats after it appears: the stagger of an end screen. Still under reduced motion. */
@Composable
fun Modifier.arrive(order: Int, start: Int = 100, beat: Int = 70): Modifier {
    val still = calm()
    val a = remember { Animatable(if (still) 1f else 0f) }
    LaunchedEffect(Unit) { if (!still) { delay((start + order * beat).toLong()); a.animateTo(1f, Motion.rise()) } }
    return graphicsLayer { alpha = a.value.coerceIn(0f, 1f); translationY = (1f - a.value) * 16f * density }
}

/* ---- Celebration ----------------------------------------------------------------------------------------------- */

private class Bit(val angle: Float, val speed: Float, val spin: Float, val color: Color, val round: Boolean, val w: Float, val h: Float)

/**
 * One burst of paper bits from [origin], once, 1.4 s, gone by the end: the end of a lesson, nothing more often.
 * Draws nothing under reduced motion.
 */
@Composable
fun Burst(origin: Offset?, colors: List<Color>, modifier: Modifier = Modifier, count: Int = 28) {
    if (calm() || origin == null) return
    val t = remember { Animatable(0f) }
    // starts with the check mark's pop (60 ms), and draws nothing before
    LaunchedEffect(Unit) { delay(60); t.animateTo(1f, tween(1400, easing = LinearEasing)) }
    val bits = remember(colors) {
        val r = Random(11)
        List(count) {
            // a fan up and to the right of the check mark, a few drifting left
            val a = ((-150.0 + r.nextDouble() * 150.0) * kotlin.math.PI / 180.0).toFloat()
            Bit(a, 380f + r.nextFloat() * 520f, (r.nextFloat() - .5f) * 2.4f, colors[it % colors.size], r.nextFloat() < .3f, 5f + r.nextFloat() * 3f, 8f + r.nextFloat() * 5f)
        }
    }
    Canvas(modifier) {
        val p = t.value
        if (p <= 0f || p >= 1f) return@Canvas
        val s = p * 1.4f
        val k = 2.2f
        val drift = (1f - exp(-k * s)) / k
        val fall = 520.dp.toPx() * s * s * .5f
        val alpha = if (p < .6f) 1f else (1f - (p - .6f) / .4f)
        for (b in bits) {
            val v = b.speed.dp.toPx()
            val x = origin.x + cos(b.angle) * v * drift
            val y = origin.y + sin(b.angle) * v * drift + fall
            rotate(b.spin * s * 360f, Offset(x, y)) {
                if (b.round) drawCircle(b.color.copy(alpha = alpha), b.w.dp.toPx() / 2, Offset(x, y))
                else drawRect(b.color.copy(alpha = alpha), Offset(x - b.w.dp.toPx() / 2, y - b.h.dp.toPx() / 2), Size(b.w.dp.toPx(), b.h.dp.toPx()))
            }
        }
    }
}

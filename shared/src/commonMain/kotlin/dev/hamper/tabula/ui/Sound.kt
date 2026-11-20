package dev.hamper.tabula.ui

/**
 * The sounds, one file each (app/src/main/assets/zvuky/<file>.wav; where they come from: POVOD.md there). [gain] is
 * on top of the file's own level, which scripts/zvuky.py sets by role: a verdict loudest, nothing above "correct", a
 * tap far below. A sound is replaced by replacing its file; nothing here changes.
 */
enum class Sfx(val file: String, val gain: Float = 1f) {
    Tap("tap"),
    Button("button"),
    Correct("correct"),
    Combo1("combo_1"), Combo2("combo_2"), Combo3("combo_3"), Combo4("combo_4"), Combo5("combo_5"),
    CorrectAgain("correct_alt"),
    Wrong("wrong"),
    Slip("wrong_alt"),
    StepDone("step_done"),
    LessonDone("lesson_done"),
    Score("lesson_done_alt"),
    Streak("streak"),
}

/** Plays a sound (Android: a SoundPool in the app module, loaded at start; the desktop and the web play nothing). */
fun interface SfxPlayer { fun play(s: Sfx) }

/**
 * The register of the moments the app answers: every [Buzz] is one moment, played from one place (Haptics.play): its
 * haptic and its sound together, in the frame the screen changes (a verdict when it shows, not when the button is
 * let go). What sounds (Teo, 18. 11.: "dobre ich zakomponuj"):
 * - a tap only where an answer is chosen: an option, a vector or a point, a cell, a key ([Buzz.Choose], [Buzz.Select],
 *   [Buzz.Key], [Buzz.Cell]); moving around is silent ([Buzz.Tap], [Buzz.Tick], [Buzz.Expand]);
 * - the main button its own short sound ([Buzz.Button]); a verdict in the same touch cuts a click short (Android player);
 * - right at the first try climbs with the series: the first "correct", then combo_1 … combo_5 for the 2nd to the 6th
 *   and every further one in a row ([Series]); right after a miss a quieter "correct" and no combo; a mistake resets it;
 * - the end of a lesson once ([Buzz.Finish]), and the days in a row once a day after it ([Buzz.Streak]).
 * [on] is the "Zvuky" switch (on by default); the player keeps to the media volume and the phone's silent mode.
 */
object Sounds {
    var player: SfxPlayer? = null
    var on: () -> Boolean = { true }
    /** The media volume is at 0 (Nastavenia say why nothing is heard). */
    var muted: () -> Boolean = { false }
    /** Where tests hear which sound a moment plays (before the switch: what would play). */
    var trace: ((Sfx) -> Unit)? = null

    fun play(s: Sfx) {
        trace?.invoke(s)
        if (!on()) return
        player?.play(s)
    }

    private val combo = listOf(Sfx.Combo1, Sfx.Combo2, Sfx.Combo3, Sfx.Combo4, Sfx.Combo5)

    /** The sound of right answer number [n] in a row at the first try. */
    fun right(n: Int): Sfx = if (n <= 1) Sfx.Correct else combo[minOf(n - 1, combo.size) - 1]

    /** The sound of a moment, or null for the silent ones. */
    fun of(b: Buzz): Sfx? = when (b) {
        Buzz.Tap, Buzz.Tick, Buzz.Expand -> null
        Buzz.Choose, Buzz.Select, Buzz.Key, Buzz.Cell -> Sfx.Tap
        Buzz.Button -> Sfx.Button
        Buzz.Right, Buzz.Series -> right(Series.n)
        Buzz.RightAgain -> Sfx.CorrectAgain
        Buzz.Wrong -> Sfx.Wrong
        Buzz.Slip -> Sfx.Slip
        Buzz.StepDone -> Sfx.StepDone
        Buzz.Land -> Sfx.Score
        Buzz.Finish -> Sfx.LessonDone
        Buzz.Streak -> Sfx.Streak
    }

    fun forBuzz(b: Buzz) { of(b)?.let(::play) }
}

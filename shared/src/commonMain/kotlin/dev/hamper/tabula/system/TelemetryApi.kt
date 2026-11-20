package dev.hamper.tabula.system

import dev.hamper.tabula.data.ClipDef

/**
 * Anonymous learning statistics and error reports. Android: the Telemetry object in shared/src/androidMain, around a
 * Tracker (jvmCommonMain), which decides what each call sends. The desktop JVM has the same Tracker, off unless a test
 * points it at a server; the web sends nothing.
 */
expect object Telemetry {
    /** Statistics on (Nastavenia); off drops what waits and forgets the install id. */
    var enabled: Boolean
    fun switch(on: Boolean)
    /** The app came to the front with this course and parallel (a new sitting after 30 min away). */
    fun opened(courseId: String, parallel: String?)
    fun lessonStarted(lessonId: String)
    /** Step [pos] of the lesson is on screen: its clock starts, answers and help taps belong to it. */
    fun stepShown(lessonId: String, pos: Int)
    /** "Viem to" ("known") or "Teraz nie" ("later") on the step on screen. */
    fun skipped(how: String)
    /** The student went on from the step on screen. */
    fun stepLeft()
    /** The lesson was closed before its end. */
    fun lessonLeft()
    fun lessonFinished(lessonId: String)
    /** A wrong answer to [key] as the student entered it (sent with that answer only). */
    fun given(key: String, text: String)
    /** The numbers of a generated exercise, so the admin can see which variant was hard. */
    fun seed(key: String, seed: String)
    /** An answer to [key], the [attempt]-th ever. */
    fun answer(key: String, ok: Boolean, aided: Boolean, attempt: Int, courseId: String, fallbackText: String?)
    /** hint / howto / why / solution */
    fun help(kind: String, key: String?)
    fun clipPlayed(clip: ClipDef, watched: Double, lecture: String)
    fun mockTest(courseId: String, paper: String, score: Int, max: Int)
    /** offered / accepted (the update sheet), with the new version. */
    fun update(stage: String, version: String?)
    fun report(item: String, text: String, courseId: String)
}

package dev.hamper.tabula.screens

import dev.hamper.tabula.platform.installJvmAssets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every clip's length is the difference of the two times it shows (design review 18. 11.: "1:09:15–1:10:18 · 62 s"; each
 * end and the length were cut down on their own, so 10 of the 19 LA1 clips and DML ones read a second short).
 */
class ClipTimesTest {
    private fun sec(stamp: String) = stamp.split(':').fold(0) { acc, p -> acc * 60 + p.toInt() }

    @Test fun lengthIsTheShownRange() {
        installJvmAssets()
        ClipLibrary.load()
        assertTrue("the LA1 and DML clips", ClipLibrary.all.size > 19)
        for (c in ClipLibrary.all) {
            val shown = sec(stamp(c, c.to - c.from)) - sec(stamp(c, 0.0))
            assertEquals("${c.id}: ${stamp(c, 0.0)}–${stamp(c, c.to - c.from)}", shown, c.seconds())
        }
        val dml = ClipLibrary.all.first { it.from == 4155.5 && it.to == 4218.4 }
        assertEquals("1:09:16–1:10:18", "${stamp(dml, 0.0)}–${stamp(dml, dml.to - dml.from)}")
        assertEquals(62, dml.seconds())
    }
}

package dev.hamper.tabula.compat.graphics

import dev.hamper.tabula.math.MathFonts
import dev.hamper.tabula.platform.installJvmAssets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

/** A run is shaped once and drawn from memory after, but never for another face, size or paint. */
class ShapedTest {
    @Test fun a_run_is_shaped_once_per_face_size_and_flags() {
        installJvmAssets()
        val face = MathFonts.text(400)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = 48f }
        val a = p.native.line(p, "x+y")
        assertSame(a, p.native.line(p, "x+y"))
        // another Paint with the same face, size and flags draws the very same line
        assertSame(a, Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = 48f }.let { it.native.line(it, "x+y") })
        assertEquals(p.measureText("x+y"), a.width, 0f)
        assertNotSame(a, p.native.line(p, "x+z"))
        assertNotSame(a, Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = 47f }.let { it.native.line(it, "x+y") })
        assertNotSame(a, Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply { typeface = face; textSize = 48f }.let { it.native.line(it, "x+y") })
        // a clone with other variations is another face even if Skia calls the two equal
        assertNotSame(a, Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = MathFonts.text(700); textSize = 48f }.let { it.native.line(it, "x+y") })
    }
}

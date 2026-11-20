package dev.hamper.tabula.snap

import androidx.test.core.app.ApplicationProvider
import dev.hamper.tabula.SkriptaFormulas
import dev.hamper.tabula.math.MathEngine
import dev.hamper.tabula.math.MathInks
import org.junit.Test
import java.io.File

/** Layout time per specimen formula on this JVM (indicative only, not a phone): `-Psnap.test=TimingShots -Psnap.out=<dir>`. */
class TimingShots : Shots() {
    @Test fun timing() {
        val c = ApplicationProvider.getApplicationContext<android.content.Context>()
        dev.hamper.tabula.platform.AndroidContext.install(c)
        val inks = MathInks(0, 0, 0, 0)
        val out = StringBuilder()
        for ((name, face) in listOf("app" to dev.hamper.tabula.ui.Math)) {
            // warm up, then a fresh size each time so the cache never answers
            repeat(3) { k -> for ((_, inline, tex) in SkriptaFormulas) MathEngine.layout(tex, 51f + k * .01f, face, inks, text = inline) }
            val n = 200
            val t0 = System.nanoTime()
            for (k in 0 until n) for ((_, inline, tex) in SkriptaFormulas) MathEngine.layout(tex, 52f + k * .001f, face, inks, text = inline)
            val us = (System.nanoTime() - t0) / 1000.0 / (n * SkriptaFormulas.size)
            out.append("$name ${"%.1f".format(us)} µs per formula (${SkriptaFormulas.size} formulas × $n)\n")
        }
        Snap.out.mkdirs(); File(Snap.out, "timing.txt").writeText(out.toString())
    }
}

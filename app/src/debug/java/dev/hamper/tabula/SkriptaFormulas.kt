package dev.hamper.tabula

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.math.LocalMathFace
import dev.hamper.tabula.math.LocalMathInks
import dev.hamper.tabula.math.MathEngine
import dev.hamper.tabula.ui.T

/** Every formula of the specimen set on its own (generated from sadzba/tools/formulas.py): id, inline, the app's TeX. */
val SkriptaFormulas: List<Triple<String, Boolean, String>> = listOf(
    Triple("1-tucne-i0", true, """m"""),
    Triple("1-tucne-i1", true, """(\mathbf{x}_1,\dots,\mathbf{x}_m)"""),
    Triple("1-tucne-i2", true, """(x_1,\dots,x_m)"""),
    Triple("2-podpriestor-i0", true, """\{\theta\}"""),
    Triple("2-podpriestor-i1", true, """T^n"""),
    Triple("2-podpriestor-i2", true, """T^n"""),
    Triple("2-podpriestor-i3", true, """P\ssub T^n"""),
    Triple("2-podpriestor-i4", true, """P\neq T^n"""),
    Triple("3-alfa-i0", true, """\alpha,\beta\in\mathbb{R}"""),
    Triple("3-alfa-i1", true, """\alpha(1,1)+\beta(1,3)=(1,5)"""),
    Triple("3-alfa-i2", true, """\alpha+\beta=1"""),
    Triple("3-alfa-i3", true, """\alpha+3\beta=5"""),
    Triple("4-gem-d0", false, """\begin{pmatrix|2}1&1&1\\1&3&5\end{pmatrix}\gemstep{(G3)}{\text{ř}2-\text{ř}1}\begin{pmatrix|2}1&1&1\\0&2&4\end{pmatrix}"""),
    Triple("5-zlomky-i0", true, """\left(\tfrac{7}{2},-1\right)=2(1,1)+\left(-\tfrac{3}{2}\right)(-1,2)"""),
    Triple("5-zlomky-d1", false, """-\tfrac{3}{2}(-1,2)=\left(\tfrac{3}{2},-3\right)"""),
    Triple("6-mnozina-i0", true, """f^{-1}[N]=\{a\in A\mid f(a)\in N\}"""),
    Triple("6-mnozina-i1", true, """N\subseteq B"""),
    Triple("7-kongruencia-i0", true, """9x\equiv 12\pmod{15}"""),
    Triple("7-kongruencia-d1", false, """3\cdot 3x\equiv 3\cdot 4\pmod{15}\ \Leftrightarrow\ 3x\equiv 4\pmod 5"""),
    Triple("8-suma-i0", true, """V(n)"""),
    Triple("8-suma-i1", true, """\sum_{k=1}^{n}(2k-1)=n^2"""),
    Triple("8-suma-i2", true, """n\in\mathbb{N}"""),
    Triple("8-suma-d3", false, """\sum_{k=1}^{n+1}(2k-1)=\sum_{k=1}^{n}(2k-1)+(2n+1)"""),
    Triple("9-koren-i0", true, """\sqrt{2}<\sqrt{13}"""),
    Triple("9-koren-i1", true, """\sqrt[3]{y}"""),
    Triple("9-koren-i2", true, """\hat{m}"""),
    Triple("9-koren-i3", true, """\hat{A}"""),
    Triple("9-koren-i4", true, """\overline{A\cup B}"""),
    Triple("9-koren-i5", true, """\mathcal{B}"""),
    Triple("9-koren-i6", true, """\mathcal{E}"""),
    Triple("9-koren-d7", false, """\sqrt{\frac{a}{b}}=\frac{\sqrt{a}}{\sqrt{b}}"""),
    Triple("10-dalsie-i0", true, """\mathbb{Z}_5^4"""),
    Triple("10-dalsie-i1", true, """\mathbf{A}^{-1}"""),
    Triple("10-dalsie-i2", true, """\tilde{\mathbf{x}}"""),
    Triple("10-dalsie-i3", true, """\mathrm{id}_A"""),
    Triple("10-dalsie-i4", true, """a\not\equiv b"""),
)

/** One formula as the calibration crops expect it: 460×120 dp, left edge at 10 dp, baseline at 70 dp; inline at 17 sp, display at 18 sp. */
@Composable
fun FormulaProbe(id: String) {
    val (_, inline, tex) = SkriptaFormulas.first { it.first == id }
    val px = with(LocalDensity.current) { (if (inline) 17.sp else 18.sp).toPx() }
    val x0 = with(LocalDensity.current) { 10.dp.toPx() }; val y0 = with(LocalDensity.current) { 70.dp.toPx() }
    val face = LocalMathFace.current; val inks = LocalMathInks.current; val ink = T.c.ink.toArgb()
    val box = MathEngine.layout(tex, px, face, inks, text = inline)
    Box(Modifier.size(460.dp, 120.dp).background(T.c.bg)) {
        Canvas(Modifier.size(460.dp, 120.dp)) { drawIntoCanvas { box.draw(it.nativeCanvas, x0, y0, ink) } }
    }
}

/** All specimen formulas under each other, one [FormulaProbe] (120 dp) per formula: one capture per candidate and theme. */
@Composable
fun FormulaSheet() {
    androidx.compose.foundation.layout.Column { for ((id, _, _) in SkriptaFormulas) FormulaProbe(id) }
}

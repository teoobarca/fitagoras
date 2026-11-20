package dev.hamper.tabula

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.math.*
import dev.hamper.tabula.ui.T

/** The specimen set (sadzba/tools/specimen.py, the app's dialect): 'p' a sentence with §inline§ math, 'd' a display formula. */
val SkriptaItems: List<Pair<String, List<Pair<Char, String>>>> = listOf(
    "1-tucne" to listOf('p' to """Súbor je §m§-tica vektorov, píš ich tučne: §(\mathbf{x}_1,\dots,\mathbf{x}_m)§. Netučné §(x_1,\dots,x_m)§ je jeden vektor so zložkami."""),
    "2-podpriestor" to listOf('p' to """Podprostory §\{\theta\}§ a §T^n§ vektorového prostoru §T^n§ nazýváme **triviálními podprostory**. Každý podprostor §P\ssub T^n§, pro který současně platí §P\neq T^n§, nazýváme **vlastním podprostorem**."""),
    "3-alfa" to listOf('p' to """Hľadám §\alpha,\beta\in\mathbb{R}§ s §\alpha(1,1)+\beta(1,3)=(1,5)§. Po zložkách: §\alpha+\beta=1§ a §\alpha+3\beta=5§."""),
    "4-gem" to listOf('d' to """\begin{pmatrix|2}1&1&1\\1&3&5\end{pmatrix}\gemstep{(G3)}{\text{ř}2-\text{ř}1}\begin{pmatrix|2}1&1&1\\0&2&4\end{pmatrix}"""),
    "5-zlomky" to listOf('p' to """Rovnosť §\left(\tfrac{7}{2},-1\right)=2(1,1)+\left(-\tfrac{3}{2}\right)(-1,2)§.""", 'd' to """-\tfrac{3}{2}(-1,2)=\left(\tfrac{3}{2},-3\right)"""),
    "6-mnozina" to listOf('p' to """Vzor §f^{-1}[N]=\{a\in A\mid f(a)\in N\}§ pre §N\subseteq B§."""),
    "7-kongruencia" to listOf('p' to """Riešime §9x\equiv 12\pmod{15}§.""", 'd' to """3\cdot 3x\equiv 3\cdot 4\pmod{15}\ \Leftrightarrow\ 3x\equiv 4\pmod 5"""),
    "8-suma" to listOf('p' to """Dokazujem §V(n)§: §\sum_{k=1}^{n}(2k-1)=n^2§ pre všetky §n\in\mathbb{N}§.""", 'd' to """\sum_{k=1}^{n+1}(2k-1)=\sum_{k=1}^{n}(2k-1)+(2n+1)"""),
    "9-koren" to listOf('p' to """Platí §\sqrt{2}<\sqrt{13}§ aj §\sqrt[3]{y}§, potom §\hat{m}§ a §\hat{A}§, doplnok §\overline{A\cup B}§ a bázy §\mathcal{B}§, §\mathcal{E}§.""", 'd' to """\sqrt{\frac{a}{b}}=\frac{\sqrt{a}}{\sqrt{b}}"""),
    "10-dalsie" to listOf('p' to """Ďalej §\mathbb{Z}_5^4§, §\mathbf{A}^{-1}§, §\tilde{\mathbf{x}}§, §\mathrm{id}_A§ a §a\not\equiv b§."""),
)

/** One specimen item as the golden page lays it out: 20 dp margins, body 17 sp on 26 sp lines, display formulas 18 sp. */
@Composable
fun SkriptaItem(name: String) {
    val parts = SkriptaItems.first { it.first == name }.second
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 14.dp, bottom = 14.dp)) {
        for ((k, t) in parts) {
            if (k == 'p') { Rich(t, style = MaterialTheme.typography.bodyLarge, color = T.c.ink); Spacer(Modifier.height(8.dp)) }
            else { Spacer(Modifier.height(4.dp)); TexBlock(t, size = 18.sp, color = T.c.ink); Spacer(Modifier.height(8.dp)) }
        }
    }
}

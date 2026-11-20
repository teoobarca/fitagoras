package dev.hamper.tabula.data.dml

import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.data.*
import kotlin.math.sqrt
import kotlin.random.Random

/*
 * Problem 1 of the test 5. 12. 2025 (variant K) with new numbers: x_1, x_2 given, x_{n+2} = √(a + b·x_n + d·x_{n+1}), for
 * which n is x_n < K, proved by strong induction. Every number is chosen so the proof on paper needs only whole numbers:
 * the terms before the bound are whole or compared through their squares, the base cases close by the same estimate as
 * the step, and the step closes because a + (b + d)·K < K².
 */

private fun tx(s: String) = s.replace('§', '$')

/** A nonlinear sequence and its bound (test 5. 12. 2025, variant K). */
val MiSeqPostup = postup(
    "Prvé členy a odhad" to "Spočítaj prvé členy a porovnaj ich s hranicou cez druhé mocniny; odhad napíš ako §V(n)§.",
    "Základné kroky" to "Krok siaha dva členy späť: over prvé dve §n§, od ktorých má tvrdenie platiť.",
    "Indukčný krok, silná indukcia" to "Za oba predošlé členy dosaď hranicu z IP; odmocnina je rastúca, preto nerovnosť ostane.",
    "Záver" to "Podľa silného princípu MI platí §V(n)§ od prvého základného kroku; pred ním nie (prvé členy).",
)

/** x_1 = c1, x_2 = c2, x_{n+2} = √(a + b x_n + d x_{n+1}); x_n < k exactly from n = [from]. */
data class MiSeqTask(val c1: Long, val c2: Long, val a: Long, val b: Long, val d: Long, val k: Long, val from: Int) {
    /** The square of x_3, a whole number. */
    val sq3 get() = a + b * c1 + d * c2
    /** The bound the step gives: x_{n+2} < √[step] < k. */
    val step get() = a + (b + d) * k
    /** The first terms as numbers (for the tests and a check of the bound). */
    fun terms(n: Int): List<Double> {
        val x = arrayListOf(c1.toDouble(), c2.toDouble())
        while (x.size < n) x += sqrt(a + b * x[x.size - 2] + d * x[x.size - 1])
        return x
    }
}

private fun isSquare(n: Long): Boolean { val r = sqrt(n.toDouble()).toLong(); return (r - 1..r + 1).any { it >= 0 && it * it == n } }
private fun root(n: Long): Long = sqrt(n.toDouble()).toLong().let { r -> (r - 1..r + 1).first { it >= 0 && it * it == n } }

/** A task whose terms before [from] are at least k and whose first two terms from it are below k by whole-number estimates. */
fun miSeqTask(r: Random): MiSeqTask {
    while (true) {
        val k = r.nextLong(3, 7)
        val b = r.nextLong(1, 3); val d = r.nextLong(1, 3)
        val aMax = k * k - (b + d) * k - 1
        if (aMax < 1) continue
        val a = r.nextLong(1, aMax + 1)
        val from = listOf(1, 2, 3, 3, 4, 4, 4).random(r)
        val c1 = if (from >= 2) r.nextLong(k, k + 5) else r.nextLong(1, k)
        val c2 = if (from >= 3) r.nextLong(k, k + 5) else r.nextLong(1, k)
        val t = MiSeqTask(c1, c2, a, b, d, k, from)
        val kk = k * k
        val ok = when (from) {
            // both start values below k: the step alone
            1 -> true
            // x_2 < k, and x_3 < k from its square
            2 -> t.sq3 < kk
            // x_3 < k from its square, x_4 < √(a + b·c_2 + d·k) < k
            3 -> t.sq3 < kk && a + b * c2 + d * k < kk
            // x_3 ≥ k whole, x_4 < k from its square, x_5 < √(a + b·x_3 + d·k) < k
            else -> t.sq3 >= kk && isSquare(t.sq3) && a + b * c2 + d * root(t.sq3) < kk && a + b * root(t.sq3) + d * k < kk
        }
        if (!ok) continue
        // a sequence that is not constant from the start, and terms on paper that stay small
        if (t.sq3 > 120 || c1 == c2 && from == 1 && c1 * c1 == a + (b + d) * c1) continue
        return t
    }
}

private fun xTex(t: MiSeqTask) = "x_{n+2}=\\sqrt{" + Terms.sum("${t.a}", Terms.times(t.b, "x_n"), Terms.times(t.d, "x_{n+1}")) + "}"

fun miSeq(t: MiSeqTask): Exercise {
    val (c1, c2, a, b, d, k, from) = t
    val kk = k * k
    val rec = xTex(t)
    val stepTex = "\\sqrt{" + Terms.sum("${a}", Terms.times(b, "$k"), Terms.times(d, "$k")) + "}=\\sqrt{${t.step}}"
    // the first terms as written on paper: x_1, x_2 given, x_3 from its square, x_4 when it is needed
    val sq3 = t.sq3
    val x3Tex = if (isSquare(sq3)) "${root(sq3)}" else "\\sqrt{$sq3}"
    val firstLines = buildList {
        add("x_1=$c1,\\quad x_2=$c2")
        add("x_3=\\sqrt{" + Terms.sum("$a", Terms.times(b, "$c1"), Terms.times(d, "$c2")) + "}=" + (if (isSquare(sq3)) "\\sqrt{$sq3}=$x3Tex" else x3Tex) + (if (sq3 >= kk) "\\geq $k" else "<\\sqrt{$kk}=$k"))
        if (from == 4) { val sq4 = a + b * c2 + d * root(sq3); add("x_4=\\sqrt{" + Terms.sum("$a", Terms.times(b, "$c2"), Terms.times(d, x3Tex)) + "}=\\sqrt{$sq4}<\\sqrt{$kk}=$k") }
    }
    // the two base cases and why each is below k
    val base: List<String> = when (from) {
        1 -> listOf("x_1=$c1<$k", "x_2=$c2<$k")
        2 -> listOf("x_2=$c2<$k", "x_3=\\sqrt{$sq3}<\\sqrt{$kk}=$k")
        3 -> listOf("x_3=\\sqrt{$sq3}<\\sqrt{$kk}=$k", "x_4=\\sqrt{" + Terms.sum("$a", Terms.times(b, "$c2"), Terms.times(d, "x_3")) + "}<\\sqrt{" + Terms.sum("$a", Terms.times(b, "$c2"), Terms.times(d, "$k")) + "}=\\sqrt{${a + b * c2 + d * k}}<$k")
        else -> listOf("x_4=\\sqrt{${a + b * c2 + d * root(sq3)}}<\\sqrt{$kk}=$k", "x_5=\\sqrt{" + Terms.sum("$a", Terms.times(b, x3Tex), Terms.times(d, "x_4")) + "}<\\sqrt{" + Terms.sum("$a", Terms.times(b, x3Tex), Terms.times(d, "$k")) + "}=\\sqrt{${a + b * root(sq3) + d * k}}<$k")
    }
    val answerTex = if (from == 1) "x_n<$k\\ \\text{pre všetky}\\ n\\in\\mathbb{N}" else "x_n<$k\\ \\Leftrightarrow\\ n\\geq $from"
    val sample = listOf(
        "Odhad: §$answerTex§" + (if (from > 1) "; pre §n<$from§ je §x_n\\geq $k§ (prvé členy)." else "."),
        "§V(n)§: §x_n<$k§. Základné kroky §n=$from§ a §n=${from + 1}§: §" + base.joinToString(",\\ ") + "§.",
        "Indukčný krok: nech §n\\geq ${from + 1}§ je pevné ľubovoľné a §x_j<$k§ pre všetky §j\\in\\{$from,\\dots,n\\}§ (IP). Potom §x_{n+1}=\\sqrt{" + Terms.sum("$a", Terms.times(b, "x_{n-1}"), Terms.times(d, "x_n")) + "}\\stackrel{IP}{<}$stepTex<$k§, lebo odmocnina je rastúca a §${t.step}<$kk§.",
        "Záver: podľa silného princípu MI platí §x_n<$k§ pre všetky §n\\geq $from§.",
    )
    val look = listOf(
        "Prvé členy spočítané a odpoveď §$answerTex§" + (if (from > 1) ", aj prečo pre §n<$from§ nie" else ""),
        "Dva základné kroky, §n=$from§ a §n=${from + 1}§, lebo krok siaha dva členy späť",
        "IP pre všetky §j\\in\\{$from,\\dots,n\\}§, nielen pre §n§",
        "Odhad §x_{n+1}<\\sqrt{${t.step}}<$k§ s vyznačeným IP a rastúcou odmocninou",
        "Záver podľa silného princípu MI",
    )
    // what a wrong n_0 says about the terms
    fun termWhy(n: Int): String = when (n) {
        1 -> "§x_1=$c1§"; 2 -> "§x_2=$c2§"
        3 -> "§x_3=" + (if (isSquare(sq3)) "$x3Tex" else "\\sqrt{$sq3}") + "§"
        else -> "§x_4^2=${a + b * c2 + d * root(sq3)}§"
    }
    return Exercise(
        "dml-mi-postupnost", "Postupnosť s odmocninou", "Šablóna písomky 5. 12. 2025 (variant K), príklad 1",
        tx("Mějme posloupnost reálných čísel definovanou předpisem §x_1=$c1§, §x_2=$c2§ a §$rec§ pro všechna §n\\in\\mathbb{N}§. Zjistěte, pro která §n\\in\\mathbb{N}§ platí §x_n<$k§. Své tvrzení dokažte matematickou indukcí. Nezapomeňte uvést, jak vypadá Váš indukční krok, a kde využíváte indukční předpoklad."),
        parts = listOf(
            { _ -> Part(tx("Od ktorého §n_0§ platí §x_n<$k§ (pre všetky §n\\geq n_0§)?"), Answer.Numbers(listOf("n_0"), null, listOf(Frac.of(from.toLong())), tx("§n_0=$from§"), diagnose = { g ->
                val v = g[0].let { if (it.isInt) it.num else -1L }
                tx(when {
                    v in 1 until from -> "Pre §n=$v§ ešte neplatí: ${termWhy(v.toInt())}, a to nie je menšie než §$k§."
                    v > from -> "Už §x_{$from}<$k§: ${if (from <= 2) termWhy(from) else if (from == 3) "§x_3^2=$sq3<${kk}§" else termWhy(4) + ", a to je menej než §$kk§"}."
                    else -> "Spočítaj prvé členy a porovnaj ich druhé mocniny s §$kk§. Odpoveď je §n_0=$from§."
                })
            }), aim = 0, hint = tx("Spočítaj §x_3§ (a ak treba §x_4§). Odmocninu s §$k§ porovnáš cez druhé mocniny: §\\sqrt{N}<$k§ práve vtedy, keď §N<$kk§.")) },
            { _ -> Part(tx("Indukčný krok: §x_{n+1}\\stackrel{IP}{<}\\sqrt{A}§. §A=§"), Answer.Numbers(listOf("A"), null, listOf(Frac.of(t.step)), tx("§A=" + Terms.sum("$a", Terms.times(b, "$k"), Terms.times(d, "$k")) + "=${t.step}<$kk§"), diagnose = { g ->
                val v = g[0].let { if (it.isInt) it.num else -1L }
                tx(when (v) {
                    a + (b + d) * maxOf(c1, c2) -> "Za IP dosaď hranicu §$k§, nie začiatočné členy. IP hovorí §x_j<$k§."
                    a + 2 * k -> "Koeficienty ostávajú: §" + Terms.sum("$a", Terms.times(b, "$k"), Terms.times(d, "$k")) + "§."
                    kk -> "§$kk§ je druhá mocnina hranice; s ňou §A§ len porovnáš. §A§ vznikne dosadením §$k§ za oba členy."
                    else -> "Za §x_{n-1}§ aj §x_n§ dosaď hranicu §$k§ z IP: §A=" + Terms.sum("$a", Terms.times(b, "$k"), Terms.times(d, "$k")) + "=${t.step}§."
                })
            }), aim = 2, hint = tx("IP dáva §x_{n-1}<$k§ aj §x_n<$k§; dosaď ich do predpisu. Odmocnina je rastúca, preto sa nerovnosť zachová.")) },
            { _ -> Part(tx("Na papier: celý dôkaz silnou indukciou, so základnými krokmi a vyznačeným IP."), Answer.Paper(sample.map(::tx), look.map(::tx),
                tx("Dôkaz potrebuje dva základné kroky a IP pre všetky predošlé §j§. Krok siaha dva členy späť, preto §n=$from§ aj §n=${from + 1}§; v kroku dosadíš §x_{n-1}<$k§ aj §x_n<$k§ a povieš, že odmocnina je rastúca.")),
                aim = 2, hint = tx("Základné kroky §n=$from§ a §n=${from + 1}§; v kroku §x_{n+1}<\\sqrt{${t.step}}<$k§.")) },
        ),
        postup = MiSeqPostup,
        solution = { _ ->
            val (first, baseAim, ik, end) = MiSeqPostup
            MiSeqPostup.solve {
                first(tx("Prvé členy porovnám s §$k§ cez druhé mocniny, lebo odmocnina je rastúca: §\\sqrt{N}<$k§ práve vtedy, keď §N<$kk§."), firstLines.map { Formula(it) })
                first(tx("Odhad zapíšem ako výrok §V(n)§: §x_n<$k§ pre §n\\geq $from§."), answerTex)
                baseAim(tx("Krok siaha dva členy späť, preto overím §n=$from§ aj §n=${from + 1}§."), base.map { Formula(it) })
                ik(tx("Nech §n\\geq ${from + 1}§ je pevné ľubovoľné a §V(j)§ platí pre všetky §j\\in\\{$from,\\dots,n\\}§ (IP). Na §x_{n-1}§ aj §x_n§ použijem IP, lebo §n-1\\geq $from§."),
                    Formula("x_{n+1}=\\sqrt{" + Terms.sum("$a", Terms.times(b, "x_{n-1}"), Terms.times(d, "x_n")) + "}\\stackrel{IP}{<}$stepTex<\\sqrt{$kk}=$k", tx("Odmocnina je rastúca, preto z menšieho výrazu pod ňou je menšia aj ona; §${t.step}<$kk§.")))
                end(tx("Podľa silného princípu MI platí §x_n<$k§ pre všetky §n\\geq $from§" + (if (from > 1) "; pre §n<$from§ je §x_n\\geq $k§ podľa prvých členov." else ".")))
            }
        },
        hint = tx("Prvé členy porovnaj s §$k§ cez druhé mocniny, potom silná indukcia s dvoma základnými krokmi."),
    )
}

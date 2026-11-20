package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*
import kotlin.random.Random

/*
 * Problem 3 (2023 D: problem 2) of the past 2nd tests, the Czech task word for word (fit-wiki, transcribed in
 * the course sources outside the repository, zdroje/testy-dml-2.md). The solution is the one the template computes for these very numbers,
 * so the paper and the practice tab say the same thing. Rubric points are our split of the problem's points.
 */

/** The answers of an exercise part by part, as written on paper, and its solution. */
private fun answersOf(ex: Exercise): Pair<String, Solution> {
    val answers = ArrayList<Any>(); val shown = ArrayList<String>()
    for (build in ex.parts) {
        when (val a = build(answers).answer) {
            is Answer.Choice -> { if (a.options.size > 2 || a.correct == 1) shown += a.options[a.correct]; answers.add(a.correct) }
            is Answer.Numbers -> { shown += a.shown; answers.add(a.expected) }
            is Answer.Pick -> {}
            is Answer.Vectors -> answers.add(a.example)
            is Answer.Paper -> answers.add(1)
        }
    }
    return shown.joinToString("; ") to ex.solution(answers)
}

private fun rb(text: String, pts: Int, drill: String? = null) = Rubric(text.replace('§', '$'), pts, drill)

private fun problem(pts: Int, title: String, text: String, ex: Exercise, rubric: List<Rubric>, drill: String): PaperProblem {
    val (answer, solution) = answersOf(ex)
    return PaperProblem(0, pts, title, text.replace('§', '$'), 25, rubric, answer, drill, solution)
}

private fun towerRubric(pts: Int) = listOf(
    rb("a) Základ zredukovaný a overené §\\gcd=1§, preto Eulerova veta; §\\varphi§ z rozkladu", 3, "dml-tc-pow"),
    rb("a) Exponent zredukovaný modulo §\\varphi(m)§, nie modulo §m§; výsledok", pts - 7, "dml-tc-pow"),
    rb("b) Modul je prvočíslo, MFV: exponent stačí modulo §p-1§, a ten je výsledok a)", 2, "dml-tc-tower"),
    rb("b) Výsledok, §=§ a §\\equiv§ rozlíšené", 2, "dml-tc-tower"),
)

private val tower2025k = problem(10, "Dva zvyšky",
    "Určete hodnoty následujících dvou zbytků. Tip: při výpočtu toho druhého využijte výsledek a). a) §667^{325}\\bmod 60§ b) §917^{(667^{325})}\\bmod 61§.",
    tcTower(TcTowerTask(667, 325, 917, 61)), towerRubric(10), "dml-tc-tower")

private val tower2024r = problem(11, "Dva zvyšky",
    "Určete hodnoty zbytků. Tip: u druhého využijte a). a) §463^{221}\\bmod 46§ b) §947^{(463^{221})}\\bmod 47§.",
    tcTower(TcTowerTask(463, 221, 947, 47)), towerRubric(11), "dml-tc-tower")

private val tower2023d = problem(10, "Dva zvyšky",
    "Určete hodnoty následujících dvou zbytků: a) §425^{181}\\bmod 42§ b) §862^{(425^{181})}\\bmod 43§.",
    tcTower(TcTowerTask(425, 181, 862, 43)), towerRubric(10), "dml-tc-tower")

private val inverse2025e = problem(10, "Inverzia a najmenší modul",
    "Co musí celé číslo §m\\ge 422§ splňovat, aby číslo §a=420§ mělo v §\\mathbb{Z}_m§ multiplikativní inverzi? Nalezněte nejmenší modul §m\\ge 422§, který toto splňuje, a v něm §420^{-1}§ vypočítejte. Své kroky poctivě zdůvodňujte.",
    tcInv(TcInvTask(420, 0, true)), listOf(
        rb("Podmienka §\\gcd(420,m)=1§ s odkazom na vetu o existencii inverzie", 3, "dml-tc-inv-min"),
        rb("§420=2^2\\cdot 3\\cdot 5\\cdot 7§ a vylúčené §422§ až §430§, každé s deliteľom", 3, "dml-tc-inv-min"),
        rb("REA na §431§ a §420§, Bézoutova rovnosť", 2, "dml-tc-rea"),
        rb("§420^{-1}=235§ v §\\mathbb{Z}_{431}§ so skúškou", 2, "dml-tc-inv"),
    ), "dml-tc-inv-min")

private val system2023a = problem(10, "Sústava kongruencií",
    "Nalezněte všechna řešení soustavy kongruencí. Které z těchto řešení je největší záporné? Všechna svá tvrzení zdůvodněte. §6x\\equiv 36\\pmod{195}§, §5x\\equiv 4\\pmod{91}§.",
    tcCombo(TcComboTask(6, 36, 195, 5, 4, 91)), listOf(
        rb("Prvá: §\\gcd(6,195)=3§ delí §36§, krátenie s modulom §65§, §x\\equiv 6\\pmod{65}§", 3, "dml-tc-lkon"),
        rb("Druhá: §5^{-1}\\equiv 73§, §x\\equiv 19\\pmod{91}§", 2, "dml-tc-inv"),
        rb("§\\gcd(65,91)=13§ delí §19-6§: riešenie existuje, jediné modulo §\\lcm=455§", 2, "dml-tc-crt"),
        rb("Dosadenie a §x\\equiv 201\\pmod{455}§", 2, "dml-tc-crt"),
        rb("Najväčšie záporné §-254§", 1, "dml-tc-crt"),
    ), "dml-tc-combo")

/** Two separate tasks on one sheet (2024 J: "1." and "2."), each solved by its template, one subgoal each. */
private fun twoTasks(pts: Int, title: String, text: String, a: Pair<String, Exercise>, b: Pair<String, Exercise>, rubric: List<Rubric>, drill: String): PaperProblem {
    val (ansA, solA) = answersOf(a.second); val (ansB, solB) = answersOf(b.second)
    return PaperProblem(0, pts, title, text.replace('§', '$'), 25, rubric, "1. $ansA 2. $ansB".replace('§', '$'), drill, parts(a.first to solA, b.first to solB))
}

private val tower2024h = problem(11, "Dva zvyšky",
    "Určete hodnoty následujících dvou zbytků. a) §667^{325}\\bmod 60§ b) §917^{(667^{325})}\\bmod 61§.",
    tcTower(TcTowerTask(667, 325, 917, 61)), towerRubric(11), "dml-tc-tower")

private val system2024f = problem(11, "Sústava kongruencií",
    "Nalezněte všechna řešení následující soustavy kongruencí. Které z těchto řešení je největší záporné? Všechna svá tvrzení zdůvodněte. §3^{30}x\\equiv 5\\pmod{11}§, §50x\\equiv 10\\pmod{65}§.",
    tcCombo(TcComboTask(50, 10, 65, 1, 5, 11, base = 3, exp = 30, powerFirst = true)), listOf(
        rb("Prvá: §\\gcd(3,11)=1§, MFV, §3^{30}\\equiv 1§, teda §x\\equiv 5\\pmod{11}§", 3, "dml-tc-pow"),
        rb("Druhá: §\\gcd(50,65)=5§ delí §10§, krátenie s modulom §13§, §10^{-1}\\equiv 4§, §x\\equiv 8\\pmod{13}§", 3, "dml-tc-lkon"),
        rb("Moduly §11§ a §13§ nesúdeliteľné: riešenie jediné modulo §143§ (ČVOZ)", 2, "dml-tc-crt"),
        rb("Dosadenie a §x\\equiv 60\\pmod{143}§", 2, "dml-tc-crt"),
        rb("Najväčšie záporné §-83§", 1, "dml-tc-crt"),
    ), "dml-tc-combo")

private val system2026r = problem(10, "Sústava kongruencií",
    "Nalezněte všechna řešení následující soustavy kongruencí. Každý svůj krok pečlivě vysvětlujte. Kongruence neřešte hrubou silou. §26^{488}x\\equiv 4\\pmod{21}§, §18x\\equiv 108\\pmod{60}§.",
    tcCombo(TcComboTask(18, 108, 60, 4, 4, 21, base = 26, exp = 488, powerFirst = true, negative = false)), listOf(
        rb("Prvá: §26\\equiv 5§, §\\gcd(5,21)=1§, Euler s §\\varphi(21)=12§, §488\\equiv 8\\pmod{12}§, §5^8\\equiv 4§", 3, "dml-tc-pow"),
        rb("Prvá: §4x\\equiv 4\\pmod{21}§, inverzia, §x\\equiv 1\\pmod{21}§", 1, "dml-tc-inv"),
        rb("Druhá: §\\gcd(18,60)=6§ delí §108§, krátenie s modulom §10§, §x\\equiv 6\\pmod{10}§", 3, "dml-tc-lkon"),
        rb("Moduly §21§ a §10§ nesúdeliteľné, dosadenie a §x\\equiv 106\\pmod{210}§ (všetky riešenia)", 3, "dml-tc-crt"),
    ), "dml-tc-combo")

private val powLkon2024j = twoTasks(11, "Mocnina a lineárna kongruencia",
    "1. Spočtěte §3^{6624125}\\bmod 110§. 2. Nalezněte všechna řešení kongruence §308x\\equiv 231\\pmod{121}§.",
    "1. Veľká mocnina" to tcPow(TcPowTask(3, 6624125, 110)), "2. Lineárna kongruencia" to tcLkon(TcLkonTask(308, 231, 121)), listOf(
        rb("1. §\\gcd(3,110)=1§, §\\varphi(110)=40§ z rozkladu §2\\cdot 5\\cdot 11§, Eulerova veta", 3, "dml-tc-pow"),
        rb("1. Exponent modulo §40§: §6624125\\equiv 5§, §3^5\\equiv 23\\pmod{110}§", 2, "dml-tc-pow"),
        rb("2. §\\gcd(308,121)=11§ delí §231§: riešenie existuje", 2, "dml-tc-lkon"),
        rb("2. Krátenie s modulom §11§: §6x\\equiv 10§, §6^{-1}\\equiv 2§, §x\\equiv 9\\pmod{11}§", 2, "dml-tc-lkon"),
        rb("2. Všetkých §11§ riešení modulo §121§: §9,20,\\dots,119§", 2, "dml-tc-lkon"),
    ), "dml-tc-lkon")

internal val tcPaperProblems: Map<String, PaperProblem> = mapOf(
    "dml-2025-e" to inverse2025e,
    "dml-2025-k" to tower2025k,
    "dml-2024-r" to tower2024r,
    "dml-2023-d" to tower2023d,
    "dml-2023-a" to system2023a,
    "dml-2026-r" to system2026r,
    "dml-2024-f" to system2024f,
    "dml-2024-j" to powLkon2024j,
    "dml-2024-h" to tower2024h,
)

/** Problem 3 of a generated paper: one of the three shapes the tests had, new numbers. */
internal fun tcGeneratedProblem(r: Random): PaperProblem = when (r.nextInt(3)) {
    0 -> tcTower(tcTowerTask(r)).let { ex ->
        problem(10, "Dva zvyšky", ex.prompt, ex, towerRubric(10), "dml-tc-tower")
    }
    1 -> tcInv(TcInvTask(listOf<Long>(210, 330, 390, 462, 510, 546, 630, 660, 770, 840, 910, 990).random(r), 0, true)).let { ex ->
        problem(10, "Inverzia a najmenší modul", ex.prompt, ex, listOf(
            rb("Podmienka §\\gcd(a,m)=1§ s odkazom na vetu o existencii inverzie", 3),
            rb("Rozklad §a§ a vylúčené menšie §m§, každé s deliteľom", 3),
            rb("REA a Bézoutova rovnosť", 2),
            rb("Inverzia v §\\mathbb{Z}_m§ so skúškou", 2),
        ), "dml-tc-inv-min")
    }
    else -> tcCombo(tcComboTask(r)).let { ex ->
        problem(10, "Sústava kongruencií", ex.prompt, ex, listOf(
            rb("Prvá kongruencia s krátením modulu", 3),
            rb("Druhá kongruencia cez inverziu", 2),
            rb("Podmienka riešiteľnosti a modul riešenia", 2),
            rb("Riešenie sústavy", 2),
            rb("Najväčšie záporné riešenie", 1),
        ), "dml-tc-combo")
    }
}

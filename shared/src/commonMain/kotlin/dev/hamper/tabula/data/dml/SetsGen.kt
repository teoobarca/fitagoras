package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.gcd
import dev.hamper.tabula.la.q
import kotlin.math.abs
import kotlin.random.Random
import dev.hamper.tabula.compat.JInteger

/*
 * Generators of Množiny a zobrazenia. Numbers and sets are drawn, then everything asked and checked is computed by
 * SetsLogic.kt and SetsMaps.kt; nothing is typed by hand. Shapes follow the past 2nd tests (fit-wiki 2021 to 2025) and
 * cvičenia 5.3 to 5.11.
 */

private fun mm(s: String) = m(s)

/* ---- 1. Does the identity always hold? ---------------------------------------------------------------------------- */

/** An identity from a past test or a cvičení; [key] names the step that closes the proof when it holds. */
data class PoolIdentity(val s: String, val src: String, val key: String)

val IdentityPool: List<PoolIdentity> = listOf(
    PoolIdentity("A - B <= A", "Cvičení 5.3 a", "Z konjunkcie plynie jej časť: §(p\\wedge q)\\Rightarrow p§."),
    PoolIdentity("A - B = A & ~B", "Cvičení 5.3 b", "§x\\notin B§ je to isté ako §x\\in\\overline{B}§ (definícia doplnku)."),
    PoolIdentity("A & (B - A) = 0", "Cvičení 5.3 c", "V konjunkcii sú §x\\in A§ aj §x\\notin A§, to je kontradikcia §\\bot§."),
    PoolIdentity("(A - B) & (B - C) = 0", "Cvičení 5.3 d", "V konjunkcii sú §x\\notin B§ aj §x\\in B§, to je §\\bot§."),
    PoolIdentity("(A - B) - C <= A - C", "Cvičení 5.3 e", "Z §(p\\wedge q\\wedge r)§ plynie §(p\\wedge r)§."),
    PoolIdentity("A | (B - A) = A | B", "Cvičení 5.3 f", "Distributivita: §p\\vee(q\\wedge\\neg p)\\Leftrightarrow(p\\vee q)\\wedge(p\\vee\\neg p)§ a §p\\vee\\neg p§ je §\\top§."),
    PoolIdentity("(A | B) - C = (A - C) | (B - C)", "Cvičení 5.3 g", "Distributivita §\\wedge§ cez §\\vee§."),
    PoolIdentity("A - (B | C) = (A - B) & (A - C)", "Cvičení 5.3 h", "De Morgan: §\\neg(q\\vee r)\\Leftrightarrow(\\neg q\\wedge\\neg r)§, potom §p§ zdvojíš (idempotencia)."),
    PoolIdentity("A - (B & C) = (A - B) | (A - C)", "Cvičení 5.3 i", "De Morgan: §\\neg(q\\wedge r)\\Leftrightarrow(\\neg q\\vee\\neg r)§, potom distributivita."),
    PoolIdentity("A & (B - C) = (A & B) - (A & C)", "Cvičení 5.3 j", "Po De Morganovi a distributivite je jedna disjunkcia §(x\\in A\\wedge x\\notin A\\wedge\\dots)§, teda §\\bot§."),
    PoolIdentity("(X - Y) & (Y - X) = 0", "Cvičení 5.4 a", "V konjunkcii sú §x\\in X§ aj §x\\notin X§, to je §\\bot§."),
    PoolIdentity("X - (Y | Z) = (X - Y) | Z", "Cvičení 5.4 b", ""),
    PoolIdentity("~(X - Y) = ~(Y - X)", "Cvičení 5.4 c", ""),
    PoolIdentity("~(X & Y) <= X", "Cvičení 5.4 d", ""),
    PoolIdentity("(X & Y) | (Y - X) = X", "Cvičení 5.4 e", ""),
    PoolIdentity("X * (Y | Z) = (X * Y) | (X * Z)", "Cvičení 5.5 a", "Distributivita §\\wedge§ cez §\\vee§ pre §u\\in X§."),
    PoolIdentity("X * (Y - Z) = (X * Y) - (X * Z)", "Cvičení 5.5 b", "De Morgan a distributivita; disjunkcia s §u\\in X\\wedge u\\notin X§ je §\\bot§."),
    PoolIdentity("X - (Y * Z) = (X - Y) * (X - Z)", "Cvičení 5.5 c", ""),
    PoolIdentity("~(X * Y) = ~X * ~Y", "Cvičení 5.5 f", ""),
    PoolIdentity("(C - B) & (C - A) = C - (A | B)", "Písomka 4. 12. 2025 (variant E), c1", "De Morgan: §x\\notin A\\wedge x\\notin B\\Leftrightarrow\\neg(x\\in A\\vee x\\in B)§."),
    PoolIdentity("(C - B) | (C - A) = C - (A | B)", "Písomka 4. 12. 2025 (variant E), c2", ""),
    PoolIdentity("~X * ~Y <= ~(X * Y)", "Písomka 2024 (variant N), 2c", "Z §(u\\notin X\\wedge v\\notin Y)§ plynie §(u\\notin X\\vee v\\notin Y)§, to je De Morgan pre §\\neg(u\\in X\\wedge v\\in Y)§."),
    PoolIdentity("(X - Y) * (X - Z) = (X - Z) * (X - Y)", "Písomka 6. 12. 2023 (variant A), 2c", ""),
    PoolIdentity("(A & B & ~C) <= (A | B) - C", "Písomka 8. 12. 2023 (variant D), 3c", "Z §x\\in A§ plynie §(x\\in A\\vee x\\in B)§."),
    PoolIdentity("(A * B) & (A * ~(C & D)) = A * ((B - C) | (B - D))", "Písomka 3. 12. 2021, príklad 4", "De Morgan §\\neg(v\\in C\\wedge v\\in D)§ a distributivita."),
    PoolIdentity("(X - W) - (Z - Y) = (X - Z) - (W - Y)", "Písomka 29. 11. 2021, príklad 3", ""),
    PoolIdentity("A - (B & C) = (A - B) & (A - C)", "Obmena Cvičenia 5.3 i", ""),
    PoolIdentity("(A | B) - C = A | (B - C)", "Obmena Cvičenia 5.3 g", ""),
    PoolIdentity("X * (Y & Z) = (X * Y) & (X * Z)", "Obmena Cvičenia 5.5 a", "Idempotencia §u\\in X§ a komutativita §\\wedge§."),
    PoolIdentity("(X * Y) | (Y * X) = (X | Y) * (X | Y)", "Obmena Cvičenia 5.5 a", ""),
)

private val Letters = listOf(listOf("A", "B", "C", "D"), listOf("X", "Y", "Z", "W"), listOf("K", "L", "M", "N"))

/** Renames the variables to another trio of letters, keeping their order; returns the renaming too. */
fun renamedWith(id: Identity, r: Random): Pair<Identity, Map<String, String>> {
    val vs = id.vars.sortedBy { v -> Letters.flatten().indexOf(v) }
    val trio = Letters.random(r)
    val m = vs.mapIndexed { i, v -> v to trio[i] }.toMap()
    return id.rename(m) to m
}

fun renamed(id: Identity, r: Random): Identity = renamedWith(id, r).first

/** The same renaming in a text: single capital letters inside math (§...§), not command names like \mathcal{U}. */
fun renameTex(text: String, m: Map<String, String>): String =
    text.split('§').mapIndexed { i, part -> if (i % 2 == 0) part else Regex("(?<![\\\\A-Za-z])[A-Z](?![A-Za-z])").replace(part) { mt -> m[mt.value] ?: mt.value } }.joinToString("§")

/** A random concrete choice of the variables in a small universe. */
fun randomConcrete(vars: List<String>, r: Random, size: Int = r.pick(2, 3)): Concrete {
    val u = if (r.nextBoolean()) (1..size).map { "$it" } else listOf("a", "b", "c", "d").take(size)
    return Concrete(u, vars.associateWith { u.filter { r.nextBoolean() }.toSet() })
}

fun concreteTex(c: Concrete): String = "\\mathcal{U}=" + setTex(c.universe.toSet(), c.universe) + ",\\ " + c.sets.entries.sortedBy { it.key }.joinToString(",\\ ") { (k, v) -> "$k=" + setTex(v, c.universe) }

fun sidesTex(id: Identity, c: Concrete): String = "§" + id.lhs.tex() + "=" + id.lhs.eval(c).tex(c.universe) + "§ a §" + id.rhs.tex() + "=" + id.rhs.eval(c).tex(c.universe) + "§"

/** An unfolding chain as a formula: "x ∈ L ⇔ ... ". */
fun unfoldLine(s: SE): Formula {
    val ch = unfold(s)
    return Formula(ch.first().tex() + " \\Leftrightarrow " + ch.drop(1).joinToString(" \\Leftrightarrow ") { it.tex() })
}

/** Is a set identity always true: defined?, both sides as formulas, compare, the verdict (Definice 5.3, Cvičení 5.5). */
val SetIdentityPostup = postup(
    "Je vzťah definovaný?" to "Obe strany musia byť podmnožiny toho istého univerza.",
    "Rozpíš obe strany na formuly" to "Pre ľubovoľný prvok prepíš príslušnosť k ľavej aj pravej strane podľa definícií operácií, zvonku dovnútra.",
    "Porovnaj formuly" to "Rovnosť: formuly sú ekvivalentné. Inklúzia: ľavá implikuje pravú. Inak hľadaj, kde sa líšia.",
    "Záver" to "Platí pre každý prvok, alebo konkrétny protipríklad, na ktorom overíš obe strany.",
)

/** A set from concrete sets: membership as a formula, element by element. */
val ConcreteSetPostup = postup(
    "Rozpíš príslušnosť" to "Prepíš §x\\in E§ na formulu s §x\\in A§, §x\\in B§, ... podľa definícií operácií.",
    "Prvok po prvku" to "Pre každý prvok univerza zisti, kde leží, a dosaď do formuly.",
    "Vypíš množinu" to "Do §E§ patria práve prvky, pre ktoré formula platí.",
)

/** A finite map: injective, surjective, image, preimage (Definice 5.8 a 5.9). */
val FiniteMapPostup = postup(
    "Injektivita" to "Hľadaj dva rôzne prvky s rovnakým obrazom; ak nie sú, je injektívne.",
    "Surjektivita" to "Hľadaj prvok cieľa bez vzoru; ak taký nie je, je surjektívne.",
    "Obraz §f[M]§" to "Zober obrazy prvkov §M§.",
    "Vzor §f^{-1}[N]§" to "Zober prvky, ktorých obraz leží v §N§.",
)

/** Composition and inverse of finite maps (Definice 5.10 a 5.13, Věta 5.3). */
val ComposePostup = postup(
    "Zlož zobrazenia" to "§(f\\circ g)(a)=f(g(a))§: najprv §g§, potom §f§.",
    "Je zloženie prosté?" to "Pozri, či majú dva rôzne prvky rovnaký obraz.",
    "Inverzia" to "Bijekcia má inverziu: obráť šípky. Inak inverzia neexistuje.",
)

/** Injectivity and surjectivity of a map given by a formula (Definice 5.9). */
val InjSurjPostup = postup(
    "Injektivita z definície" to "Z §f(\\cdot)=f(\\cdot')§ odvoď rovnosť argumentov, alebo nájdi dva rôzne body s rovnakým obrazom.",
    "Surjektivita z definície" to "Z §f(\\cdot)=y§ vyjadri vzor a over, že leží v definičnom obore, alebo nájdi bod bez vzoru.",
)

fun identityExercise(r: Random): Exercise {
    val pool = IdentityPool.random(r)
    val (id, names) = renamedWith(identity(pool.s), r)
    val pick = pool.copy(key = renameTex(pool.key, names))
    // the relation as a display formula of its own: inside the sentence it broke at "=" on a 320 dp phone (Zásada 11)
    val text = "Nechť §${id.vars.joinToString(",")}§ jsou libovolné podmnožiny společného universa §\\mathcal{U}§. Rozhodněte, zda vztah §§${id.tex()}§§ vždy platí."
    val defined = id.defined
    val holds = defined && id.holds()
    val verdict = when { !defined -> 2; holds -> 0; else -> 1 }
    val opts = listOf("Platí vždy", "Neplatí vždy", "Nie je definovaný")
    val pair = defined && id.level == 2
    val elem = if (pair) "dvojicu §(u,v)\\in\\mathcal{U}\\times\\mathcal{U}§" else "prvok §x\\in\\mathcal{U}§"
    val some = (if (pair) "niektorú " else "niektorý ") + elem
    val any = (if (pair) "ľubovoľnú " else "ľubovoľný ") + elem
    val each = (if (pair) "každú " else "každý ") + elem
    val sub = id.rel == Rel.SUB
    val holdsWhy = if (sub) "Platí, formula ľavej strany implikuje formulu pravej." else "Platí, obe strany sú ekvivalentné formuly."
    val why = when (verdict) {
        0 -> "$holdsWhy ${pick.key}"
        1 -> if (sub) "Neplatí, pre $some platí formula ľavej strany a pravej nie. Stačí jeden protipríklad." else "Neplatí, formuly oboch strán sa pre $some líšia. Stačí jeden protipríklad."
        else -> "Nie je definovaný. Na jednej strane stojí operácia medzi podmnožinou §\\mathcal{U}§ a podmnožinou §\\mathcal{U}\\times\\mathcal{U}§ (Řešení Cvičení 5.5 c)."
    }
    val wrong = (0..2).filter { it != verdict }.associateWith { k ->
        when (k) {
            0 -> if (verdict == 1) "Neplatí vždy, nájde sa protipríklad. Prepíš obe strany na formuly a hľadaj, kde sa líšia." else "Vzťah nemá zmysel, nie je čo overovať. Prienik či rozdiel je definovaný len pre podmnožiny toho istého univerza."
            1 -> if (verdict == 0) (if (sub) "Protipríklad neexistuje, formula ľavej strany implikuje pravú. ${pick.key}" else "Protipríklad neexistuje, formuly oboch strán sú ekvivalentné. ${pick.key}") else "Neplatnosť sa ani nedá overiť, vzťah nie je definovaný. Množina §\\mathcal{U}§ a §\\mathcal{U}\\times\\mathcal{U}§ sú iné univerzá."
            // one sentence when nothing more is to be said (Zásada 7: no "Prečo?" that only repeats it)
            else -> if (id.level == 2) "Vzťah je definovaný, obe strany sú podmnožiny toho istého univerza. Súčiny sú podmnožiny §\\mathcal{U}\\times\\mathcal{U}§ a tam sa s nimi dá počítať."
                else "Vzťah je definovaný, všetky množiny aj výsledky operácií sú podmnožiny §\\mathcal{U}§."
        }
    }
    val parts = ArrayList<(List<Any>) -> Part>()
    parts += { _ -> Part("Platí vzťah vždy?", Answer.Choice(opts, verdict, mm(why), wrong.mapValues { mm(it.value) }),
        hint = mm("Over, či sú obe strany v tom istom univerze. Potom prepíš príslušnosť k ľavej aj pravej strane na formulu a porovnaj ich."), aim = 2) }
    var cexs: List<Concrete> = emptyList()
    if (verdict == 1) {
        val bad = generateSequence { randomConcrete(id.vars, r) }.first { !id.holdsOn(it) }
        val goods = generateSequence { randomConcrete(id.vars, r) }.take(400).filter { id.holdsOn(it) }.distinctBy { concreteTex(it) }.take(2).toList()
        cexs = (listOf(bad) + goods)
        if (goods.size == 2) {
            val order = listOf(0, 1, 2).shuffled(r)
            parts += { _ -> Part("Ktorá voľba je protipríklad?", Answer.Choice(order.map { "§" + concreteTex(cexs[it]) + "§" }.map(::mm), order.indexOf(0),
                mm("Táto voľba vzťah porušuje: ${sidesTex(id, bad)}."),
                order.withIndex().filter { it.value != 0 }.associate { (i, v) -> i to mm("Táto voľba vzťah spĺňa: ${sidesTex(id, cexs[v])}. Protipríklad musí vzťah porušiť.") }),
                hint = mm("Pre každú voľbu spočítaj ľavú aj pravú stranu. Protipríklad je voľba, pri ktorej vzťah neplatí."), aim = 3) }
        }
    }
    return Exercise(
        "dml-mz-rovnost", "Množinová identita", pick.src, mm(text),
        parts = parts, postup = SetIdentityPostup,
        solution = {
            val (defined, unfoldAim, compare, end) = SetIdentityPostup
            SetIdentityPostup.solve {
                if (verdict == 2) {
                    defined(mm(why))
                    end(mm("Odpoveď: vzťah nie je definovaný, jeho platnosť sa preto neoveruje."))
                    return@solve
                }
                defined(mm(if (id.level == 2) "Súčiny sú podmnožiny §\\mathcal{U}\\times\\mathcal{U}§ a všetko ostatné tiež, vzťah je definovaný." else "Všetko sú podmnožiny §\\mathcal{U}§, vzťah je definovaný."))
                unfoldAim(mm("Vezmem $any a príslušnosť k ľavej strane prepíšem podľa definícií operácií, zvonku dovnútra."), listOf(unfoldLine(id.lhs)))
                unfoldAim(mm("Rovnako pravú stranu."), listOf(unfoldLine(id.rhs)))
                if (verdict == 0) {
                    compare(mm(if (sub) "${pick.key} Ľavá formula teda implikuje pravú." else "${pick.key} Obe formuly sú teda ekvivalentné."))
                    end(mm(if (sub) "Platí to pre $each, preto inklúzia platí vždy." else "Platí to pre $each, preto rovnosť platí vždy."))
                } else {
                    val c = cexs[0]
                    compare(mm(if (sub) "Ľavá formula neimplikuje pravú, preto hľadám protipríklad." else "Formuly nie sú ekvivalentné, preto hľadám protipríklad."))
                    end(mm("Protipríklad: §${concreteTex(c)}§. Potom ${sidesTex(id, c)}, a vzťah §${id.rel.tex}§ neplatí."))
                }
            }
        },
        hint = "Prepíš §x\\in§ ľavej aj pravej strany na formulu s §x\\in A§, §x\\in B§, ... a porovnaj ich.".let(::mm),
    )
}

/* ---- 2. Compute a set from concrete sets ------------------------------------------------------------------------- */

private val ConcreteShapes = listOf("(A - B) | C", "A & ~B", "~(A | C)", "(A | B) - (A & B)", "A - (B - C)", "(A - B) - C", "(A & C) | (B - A)", "~A & (B | C)")

/** Why an element is in or out: its memberships in the variables. */
private fun memberships(e: String, c: Concrete): String = c.sets.keys.sorted().joinToString(", ") { v -> "§$e" + (if (e in c.sets.getValue(v)) "\\in " else "\\notin ") + v + "§" }

fun concreteExercise(r: Random): Exercise {
    while (true) {
        val e = se(ConcreteShapes.random(r))
        val u = (1..6).map { "$it" }
        val c = Concrete(u, e.vars().sorted().associateWith { u.filter { r.nextInt(5) < 2 }.toSet() })
        val res = e.eval(c).elems!!
        if (res.isEmpty() || res.size == u.size) continue
        val name = "E"
        val ok: (Set<Int>) -> Boolean = { s -> s.map { u[it] }.toSet() == res }
        val why: (Set<Int>) -> String = { s ->
            val picked = s.map { u[it] }.toSet()
            val miss = u.firstOrNull { it in res && it !in picked }
            val extra = u.firstOrNull { it !in res && it in picked }
            if (miss != null) mm("Prvok §$miss§ do §$name§ patrí, chýba ti. Platí ${memberships(miss, c)}.")
            else if (extra != null) mm("Prvok §$extra§ do §$name§ nepatrí. Platí ${memberships(extra, c)}.")
            else mm("Vyber presne prvky §$name§.")
        }
        return Exercise(
            "dml-mz-mnoziny", "Výpočet množiny", "Definice 5.3",
            mm("V univerze §\\mathcal{U}=\\{1,2,3,4,5,6\\}§ je ${c.sets.entries.sortedBy { it.key }.joinToString(", ") { (k, v) -> "§$k=" + setTex(v, u) + "§" }}. Urči §$name=${e.tex()}§."),
            parts = listOf({ _ -> Part("Ktoré prvky má §$name§?".let(::mm), Answer.Pick(u.map { "§$it§".let(::mm) }, 0..6, ok, why)) }),
            postup = ConcreteSetPostup,
            solution = {
                val (unfoldAim, each, list) = ConcreteSetPostup
                ConcreteSetPostup.solve {
                    unfoldAim(mm("Príslušnosť k §$name§ prepíšem podľa definícií operácií na formulu."), listOf(unfoldLine(e)))
                    each(mm("Pre každý prvok univerza zistím, v ktorých množinách leží, a dosadím do formuly."),
                        u.map { x -> Formula(c.sets.keys.sorted().joinToString(",\\ ") { v -> x + (if (x in c.sets.getValue(v)) "\\in " else "\\notin ") + v } + "\\quad\\Rightarrow\\quad " + x + (if (x in res) "\\in " else "\\notin ") + name) })
                    list(mm("Teda §$name=" + setTex(res, u) + "§."))
                }
            },
            hint = mm("Rozhoduj prvok po prvku: kde leží v §A§, §B§, §C§?"),
        )
    }
}

/* ---- 3. A finite map: injective, surjective, image, preimage ---------------------------------------------------- */

private val BNames = listOf("a", "b", "c", "d", "e")

fun tableTex(f: IntArray, an: List<String>, bn: List<String>, name: String = "f") = f.indices.joinToString(",\\ ") { "$name(${an[it]})=${bn[f[it]]}" }

/**
 * The values of finite maps as a display formula, each map on its own rows of at most three values, as even as they go
 * (four as two and two): in a sentence the list
 * wrapped after "f(a) =" on a 320 dp phone (Zásada 11, parallel things under each other).
 */
fun tableRows(vararg maps: Triple<IntArray, Pair<List<String>, List<String>>, String>): String =
    "\\begin{aligned}" + maps.flatMap { (f, sets, name) -> f.indices.chunked((f.size + (f.size + 2) / 3 - 1) / ((f.size + 2) / 3)).map { row -> "&" + row.joinToString(",\\ ") { "$name(${sets.first[it]})=${sets.second[f[it]]}" } } }
        .joinToString(",\\\\") + "\\end{aligned}"

fun finiteMapExercise(r: Random): Exercise {
    val a = r.pick(3, 5); val b = r.pick(3, 5)
    val an = (1..a).map { "$it" }; val bn = BNames.take(b)
    val f = if (r.nextInt(3) == 0 && a <= b) (0 until b).shuffled(r).take(a).toIntArray() else IntArray(a) { r.nextInt(b) }
    val inj = injective(f); val sur = surjective(f, b)
    val clash = if (inj) null else f.indices.flatMap { i -> f.indices.filter { it > i && f[it] == f[i] }.map { i to it } }.first()
    val missing = (0 until b).firstOrNull { it !in f }
    val mSet = (0 until a).shuffled(r).take(r.pick(2, minOf(3, a))).toSet()
    val img = image(f, mSet)
    var nSet: Set<Int>; var pre: Set<Int>
    do { nSet = (0 until b).shuffled(r).take(r.pick(1, 2)).toSet(); pre = preimage(f, nSet) } while (pre.isEmpty())
    val dom = "\\{" + an.joinToString(",") + "\\}"; val cod = "\\{" + bn.joinToString(",") + "\\}"
    val injWhy = if (inj) "Je injektívne, žiadne dva rôzne prvky nemajú rovnaký obraz." else "Nie je injektívne: §f(${an[clash!!.first]})=f(${an[clash.second]})=${bn[f[clash.first]]}§, hoci §${an[clash.first]}\\neq ${an[clash.second]}§."
    val surWhy = if (sur) "Je surjektívne, každý prvok §$cod§ má vzor." else "Nie je surjektívne: §${bn[missing!!]}§ nemá vzor, nikto sa naň nezobrazí."
    fun pickWhy(univ: List<String>, want: Set<Int>, what: String, reason: (Int) -> String): (Set<Int>) -> String = { s ->
        val miss = want.firstOrNull { it !in s }; val extra = s.firstOrNull { it !in want }
        if (miss != null) mm("Prvok §${univ[miss]}§ do $what patrí, chýba ti. ${reason(miss)}")
        else if (extra != null) mm("Prvok §${univ[extra]}§ do $what nepatrí. ${reason(extra)}")
        else mm("Presne tieto prvky patria do $what.")
    }
    val mTex = "\\{" + mSet.sorted().joinToString(",") { an[it] } + "\\}"
    val nTex = "\\{" + nSet.sorted().joinToString(",") { bn[it] } + "\\}"
    return Exercise(
        "dml-mz-sipky", "Zobrazenie konečných množín", "Definice 5.8 a 5.9",
        mm("Zobrazenie §f:$dom\\to $cod§ je dané predpisom §§${tableRows(Triple(f, an to bn, "f"))}§§"),
        parts = listOf(
            { _ -> Part("Je §f§ injektívne?".let(::mm), Answer.Choice(listOf("Áno", "Nie"), if (inj) 0 else 1, mm(injWhy),
                mapOf((if (inj) 1 else 0) to mm(if (inj) "Rovnaký obraz tu nemajú žiadne dva prvky. Pozri obrazy: §${tableTex(f, an, bn)}§ sú navzájom rôzne." else "Dva rôzne prvky tu majú rovnaký obraz. $injWhy"))),
                hint = mm("Injektívne znamená, že žiadne dva rôzne prvky nemajú rovnaký obraz. Opakuje sa v predpise niektorý obraz?"), aim = 0) },
            { _ -> Part("Je §f§ surjektívne?".let(::mm), Answer.Choice(listOf("Áno", "Nie"), if (sur) 0 else 1, mm(surWhy),
                mapOf((if (sur) 1 else 0) to mm(if (sur) "Každý prvok §$cod§ sa tu niekde objaví ako obraz. Surjektivita chce vzor pre každý prvok cieľa." else "Niektorý prvok cieľa nemá vzor. $surWhy"))),
                hint = mm("Surjektívne znamená, že každý prvok cieľa má vzor. Objaví sa v predpise každý prvok cieľa ako obraz?"), aim = 1) },
            { _ -> Part("Ktoré prvky má §f[M]§ pre §M=$mTex§?".let(::mm), Answer.Pick(bn.map { "§$it§".let(::mm) }, 1..b, { s -> s == img },
                pickWhy(bn, img, "§f[M]§") { y -> val xs = mSet.filter { f[it] == y }; if (xs.isEmpty()) "Žiadny prvok §M§ sa naň nezobrazí." else "§f(${an[xs.first()]})=${bn[y]}§ a §${an[xs.first()]}\\in M§." }),
                hint = mm("§f[M]§ tvoria obrazy prvkov §M§: pre každý prvok §M§ nájdi v predpise, kam sa zobrazí."), aim = 2) },
            { _ -> Part("Ktoré prvky má §f^{-1}[N]§ pre §N=$nTex§?".let(::mm), Answer.Pick(an.map { "§$it§".let(::mm) }, 1..a, { s -> s == pre },
                pickWhy(an, pre, "§f^{-1}[N]§") { x -> "§f(${an[x]})=${bn[f[x]]}§ a " + (if (f[x] in nSet) "§${bn[f[x]]}\\in N§." else "§${bn[f[x]]}\\notin N§.") }),
                hint = mm("§f^{-1}[N]§ tvoria prvky definičného oboru, ktorých obraz leží v §N§. Prejdi predpis prvok po prvku."), aim = 3) },
        ),
        postup = FiniteMapPostup,
        solution = {
            val (injAim, surAim, imgAim, preAim) = FiniteMapPostup
            FiniteMapPostup.solve {
                injAim(mm(injWhy))
                surAim(mm(surWhy))
                imgAim(mm("Obraz §M=$mTex§ tvoria obrazy jej prvkov (Definícia 5.8)."), "f[M]=\\{f(a)\\mid a\\in M\\}=\\{" + img.sorted().joinToString(",") { bn[it] } + "\\}")
                preAim(mm("Vzor §N=$nTex§ tvoria prvky, ktorých obraz leží v §N§ (Definícia 5.8)."), "f^{-1}[N]=\\{a\\mid f(a)\\in N\\}=\\{" + pre.sorted().joinToString(",") { an[it] } + "\\}")
            }
        },
        hint = mm("Injektívne: žiadne dva rôzne prvky s rovnakým obrazom. Surjektívne: každý prvok cieľa má vzor."),
    )
}

/* ---- 4. Composition and inverse of finite maps ------------------------------------------------------------------- */

fun composeExercise(r: Random): Exercise {
    val n = r.pick(3, 4)
    val an = (1..n).map { "$it" }; val bn = BNames.take(n); val cn = listOf("p", "q", "r", "s").take(n)
    val bij = r.nextBoolean()
    val g = if (bij) (0 until n).shuffled(r).toIntArray() else IntArray(n) { r.nextInt(n) }
    val f = (0 until n).shuffled(r).toIntArray()
    val h = compose(f, g)
    val x = r.nextInt(n)
    val inj = injective(h)
    val parts = ArrayList<(List<Any>) -> Part>()
    parts += { _ -> Part("Koľko je §(f\\circ g)(${an[x]})§?".let(::mm), Answer.Choice(cn.map { "§$it§".let(::mm) }, h[x],
        mm("§(f\\circ g)(${an[x]})=f(g(${an[x]}))=f(${bn[g[x]]})=${cn[h[x]]}§."),
        cn.indices.filter { it != h[x] }.associateWith { k ->
            val gx = (0 until n).firstOrNull { f[it] == k }
            mm("Najprv §g§, potom §f§: §f(g(${an[x]}))=f(${bn[g[x]]})=${cn[h[x]]}§." + (if (gx != null) " Hodnotu §${cn[k]}=f(${bn[gx]})§ dáva iný prvok §B§." else ""))
        }), hint = mm("Zloženie §f\\circ g§ čítaj sprava: §(f\\circ g)(a)=f(g(a))§, teda najprv §g§, potom §f§."), aim = 0) }
    parts += { _ -> Part("Je §f\\circ g§ injektívne?".let(::mm), Answer.Choice(listOf("Áno", "Nie"), if (inj) 0 else 1,
        mm(if (inj) "Áno, §f§ aj §g§ sú tu prosté, a zloženie prostých je prosté (Veta 5.5)." else "Nie, už §g§ zlepí dva prvky, a §f§ ich nerozlepí."),
        mapOf((if (inj) 1 else 0) to mm(if (inj) "Zloženie dvoch prostých zobrazení je prosté (Veta 5.5). Tu sú §f§ aj §g§ prosté." else
            run { val (i, j) = (0 until n).flatMap { i -> (i + 1 until n).filter { g[it] == g[i] }.map { i to it } }.first(); "Prvky §${an[i]}\\neq ${an[j]}§ majú rovnaký obraz. §g(${an[i]})=g(${an[j]})=${bn[g[i]]}§, teda aj §(f\\circ g)(${an[i]})=(f\\circ g)(${an[j]})§." }))),
        hint = mm("Spočítaj §(f\\circ g)§ pre každý prvok a pozri, či majú dva rôzne prvky rovnaký obraz."), aim = 1) }
    if (bij) {
        val y = r.nextInt(n)
        val inv = inverse(h, n)!!
        parts += { _ -> Part("Koľko je §(f\\circ g)^{-1}(${cn[y]})§?".let(::mm), Answer.Choice(an.map { "§$it§".let(::mm) }, inv[y],
            mm("§(f\\circ g)(${an[inv[y]]})=${cn[y]}§, preto §(f\\circ g)^{-1}(${cn[y]})=${an[inv[y]]}§ (Veta 5.3)."),
            an.indices.filter { it != inv[y] }.associateWith { k -> mm("§(f\\circ g)(${an[k]})=${cn[h[k]]}§, nie §${cn[y]}§. Inverzia vráti ten vzor, ktorý sa na §${cn[y]}§ zobrazí.") }),
            hint = mm("Inverzia obráti šípky: hľadáš prvok, ktorý §f\\circ g§ zobrazí na daný prvok."), aim = 2) }
    }
    return Exercise(
        "dml-mz-zlozenie", "Zloženie a inverzia", "Definice 5.10 a 5.13, Věta 5.3",
        mm("Zobrazenia §g:\\{${an.joinToString(",")}\\}\\to\\{${bn.joinToString(",")}\\}§ a §f:\\{${bn.joinToString(",")}\\}\\to\\{${cn.joinToString(",")}\\}§ sú dané predpismi §§${tableRows(Triple(g, an to bn, "g"), Triple(f, bn to cn, "f"))}§§"),
        parts = parts,
        postup = ComposePostup,
        solution = {
            val (comp, injAim, invAim) = ComposePostup
            ComposePostup.solve {
                comp(mm("§(f\\circ g)(a)=f(g(a))§, teda najprv §g§, potom §f§ (Definícia 5.10). Tak dostanem obraz každého prvku."),
                    an.indices.map { Formula("(f\\circ g)(${an[it]})=f(${bn[g[it]]})=${cn[h[it]]}") })
                injAim(mm(if (inj) "Obrazy sú navzájom rôzne, §f\\circ g§ je prosté." else "Dva prvky majú rovnaký obraz, §f\\circ g§ nie je prosté."))
                if (inj) invAim(mm("§f\\circ g§ je prosté zobrazenie medzi množinami s rovnakým počtom prvkov, teda bijekcia. Preto má inverziu, ktorá obráti šípky (Veta 5.4)."), an.indices.map { Formula("(f\\circ g)^{-1}(${cn[h[it]]})=${an[it]}") })
                else invAim(mm("§f\\circ g§ nie je prosté, teda ani bijekcia, a inverzné zobrazenie nemá."))
            }
        },
        hint = mm("Zloženie §f\\circ g§ čítaj sprava: najprv §g§, potom §f§."),
    )
}

/* ---- 5. Injectivity and surjectivity of f(m,n) = (...) on Z^k or R^k --------------------------------------------- */

private val ZVars = listOf("m", "n", "p"); private val RVars = listOf("x", "y", "z")

/** "2m-3n+2" with integer coefficients; [primes] adds a prime to every variable. */
fun linTex(cs: List<Long>, vs: List<String>, c: Long, prime: String = ""): String {
    val sb = StringBuilder()
    for ((i, k) in cs.withIndex()) {
        if (k == 0L) continue
        val v = vs[i] + prime
        val abs = abs(k)
        sb.append(if (k < 0) "-" else if (sb.isNotEmpty()) "+" else "")
        sb.append(if (abs == 1L) v else "$abs$v")
    }
    if (c != 0L || sb.isEmpty()) sb.append(if (c < 0) "$c" else if (sb.isNotEmpty()) "+$c" else "$c")
    return sb.toString()
}

/** A linear form with fraction coefficients in x, y plus a constant, for preimage formulas. */
fun fracLinTex(cs: List<Frac>, vs: List<String>, c: Frac): String {
    val sb = StringBuilder()
    fun term(k: Frac, v: String?) {
        if (k.isZero) return
        val neg = k < Frac.ZERO; val a = if (neg) -k else k
        sb.append(if (neg) "-" else if (sb.isNotEmpty()) "+" else "")
        val num = if (v == null) a.tex(true) else if (a == Frac.ONE) v else a.tex(true) + v
        sb.append(num)
    }
    cs.forEachIndexed { i, k -> term(k, vs[i]) }
    term(c, null)
    return if (sb.isEmpty()) "0" else sb.toString()
}

private fun ptTex(p: List<Long>) = if (p.size == 1) "${p[0]}" else "(" + p.joinToString(",") + ")"

enum class AffShape(val k: Int, val l: Int, val real: Boolean) { Z1(1, 1, false), Z21(2, 1, false), Z22(2, 2, false), R22(2, 2, true), R32(3, 2, true) }

fun spaceTex(real: Boolean, n: Int) = (if (real) "\\mathbb{R}" else "\\mathbb{Z}") + (if (n > 1) "^{$n}" else "")

fun drawAffine(r: Random, shape: AffShape): Affine {
    while (true) {
        val m = List(shape.l) { List(shape.k) { r.pick(-3, 3).toLong() } }
        val c = List(shape.l) { r.pick(-3, 3).toLong() }
        if (m.any { row -> row.all { it == 0L } }) continue
        if ((0 until shape.k).any { j -> m.all { it[j] == 0L } }) continue
        val f = Affine(m, c, shape.real)
        // a mix: for Z^2 → Z^2 half of them bijective (det ±1), as the 2025 sample
        if (shape == AffShape.Z22 && r.nextBoolean() && abs(det(m)) != 1L) continue
        if (shape == AffShape.Z1 && r.nextBoolean() && abs(m[0][0]) != 1L) continue
        if (shape == AffShape.Z21 && r.nextBoolean() && f.minorsGcd() != 1L) continue
        if (shape == AffShape.R32 && r.nextInt(3) == 0 && f.rank != 1) continue
        if (shape == AffShape.R22 && r.nextInt(3) == 0 && det(m) != 0L) continue
        return f
    }
}

fun affineText(f: Affine): String {
    val vs = if (f.real) RVars else ZVars
    val args = vs.take(f.k).joinToString(",")
    val out = f.m.indices.map { linTex(f.m[it], vs, f.c[it]) }
    val value = if (f.l == 1) out[0] else "(" + out.joinToString(",") + ")"
    return "f:${spaceTex(f.real, f.k)}\\to ${spaceTex(f.real, f.l)},\\quad f($args)=$value"
}

/** The injectivity part, its steps under [aim]. */
fun SolutionScope.injectivity(f: Affine, aim: Aim) {
    val vs = if (f.real) RVars else ZVars
    val a = vs.take(f.k); val args = a.joinToString(","); val args2 = a.joinToString(",") { "$it'" }
    val eqs = f.m.indices.map { i -> linTex(f.m[i], vs, f.c[i]) + "=" + linTex(f.m[i], vs, f.c[i], "'") }
    val diffs = f.m.indices.map { i -> Terms.lin(f.m[i].map { Frac.of(it) }, a.map { v -> "($v-$v')" }) + "=0" }
    aim(mm("Podľa Definície 5.9 je §f§ prosté, ak z rovnosti obrazov plynie rovnosť argumentov. Rovnosť obrazov rozpíšem po zložkách a prevediem na rozdiely."),
        Formula("(\\forall ${args.let { if (f.k == 1) it else "($it)" }},${args2.let { if (f.k == 1) it else "($it)" }})\\big(f($args)=f($args2)\\Rightarrow ${if (f.k == 1) "$args=$args2" else "($args)=($args2)"}\\big)"),
        Formula("f($args)=f($args2)\\Leftrightarrow " + eqs.joinToString("\\wedge ") { "($it)" } + "\\Leftrightarrow " + diffs.joinToString("\\wedge ") { "($it)" }))
    if (f.injective) {
        if (f.k == 1) aim(mm("Keďže §${f.m[0][0]}\\neq 0§, plynie §${a[0]}-${a[0]}'=0§, teda §${a[0]}=${a[0]}'§. Zobrazenie **je** injektívne."))
        else {
            val d = det(f.m)
            aim(mm("Označím §s=${a[0]}-${a[0]}'§ a §t=${a[1]}-${a[1]}'§. Prvú rovnicu vynásobím číslom §${f.m[1][1]}§, druhú číslom §${f.m[0][1]}§ a odčítam: §${Terms.times(d, "s")}=0§. Podobne §${Terms.times(d, "t")}=0§."))
            aim(mm("Keďže §$d\\neq 0§, je §s=t=0§, teda §(${a[0]},${a[1]})=(${a[0]}',${a[1]}')§. Zobrazenie **je** injektívne."))
        }
    } else {
        val d = f.kernel()!!
        val p0 = List(f.k) { 0L }
        val p1 = d
        aim(mm("Rovnice platia aj pre rôzne body, napríklad keď sú rozdiely §${a.joinToString(",") { "$it-$it'" }}§ rovné §${ptTex(d)}§. Zobrazenie **nie je** injektívne, protipríklad:"),
            Formula("f${ptTex(p0).let { if (f.k == 1) "($it)" else it }}=f${ptTex(p1).let { if (f.k == 1) "($it)" else it }}=${ptTex(f.at(p0))},\\quad ${ptTex(p0)}\\neq ${ptTex(p1)}"))
    }
}

/** The surjectivity part, its steps under [aim]. */
fun SolutionScope.surjectivity(f: Affine, aim: Aim) {
    val vs = if (f.real) RVars else ZVars
    val outVs = if (f.real) listOf("a", "b") else listOf("x", "y")
    val a = vs.take(f.k); val args = a.joinToString(",")
    val target = outVs.take(f.l).joinToString(",")
    val tgt = if (f.l == 1) target else "($target)"
    aim(mm("Podľa Definície 5.9 hľadám ku každému §$tgt\\in ${spaceTex(f.real, f.l)}§ vzor §${if (f.k == 1) args else "($args)"}\\in ${spaceTex(f.real, f.k)}§ s §f($args)=$tgt§."),
        Formula("f($args)=$tgt\\Leftrightarrow " + f.m.indices.joinToString("\\wedge ") { "(" + linTex(f.m[it], vs, f.c[it]) + "=" + outVs[it] + ")" }))
    if (f.surjective) {
        val pre: List<String> = when {
            f.k == f.l -> {
                // Cramer with the targets as unknowns: coefficients of x, y and the constant
                (0 until f.k).map { j ->
                    val coef = (0 until f.l).map { tIdx -> val y = List(f.l) { if (it == tIdx) Frac.ONE else Frac.ZERO }; f.preimageQ(y.mapIndexed { i, v -> v + f.c[i].q() })[j] - f.preimageQ(f.c.map { it.q() })[j] }
                    val const = f.preimageQ(List(f.l) { Frac.ZERO })[j]
                    fracLinTex(coef, outVs, const)
                }
            }
            f.l == 1 -> {
                val (s, t) = bezout(f.m[0][0], f.m[0][1])
                listOf(fracLinTex(listOf(s.q()), outVs, (-s * f.c[0]).q()), fracLinTex(listOf(t.q()), outVs, (-t * f.c[0]).q()))
            }
            else -> {
                // R^3 → R^2 of rank 2: two columns with a nonzero minor, the third coordinate 0
                val cols = combos(f.k, 2).first { cs -> det(f.m.map { row -> cs.map { row[it] } }) != 0L }
                val sub = Affine(f.m.map { row -> cols.map { row[it] } }, f.c, true)
                val forms = (0 until 2).map { j ->
                    val coef = (0 until 2).map { tIdx -> val y = List(2) { if (it == tIdx) Frac.ONE else Frac.ZERO }; sub.preimageQ(y.mapIndexed { i, v -> v + f.c[i].q() })[j] - sub.preimageQ(f.c.map { it.q() })[j] }
                    fracLinTex(coef, outVs, sub.preimageQ(List(2) { Frac.ZERO })[j])
                }
                (0 until f.k).map { j -> if (j in cols) forms[cols.indexOf(j)] else "0" }
            }
        }
        aim(mm("Z rovníc vyjadrím neznáme" + (if (!f.real) "; pre každé celé §$target§ sú to celé čísla." else ".")), (if (f.k == 1) args else "($args)") + "=" + (if (f.k == 1) pre[0] else "(" + pre.joinToString(",") + ")"))
        aim(mm("Dosadením sa overí §f(${pre.joinToString(",")})=$tgt§. Zobrazenie **je** surjektívne."))
    } else {
        val y0 = f.noPreimage()!!
        // the negation of Definícia 5.9: one concrete target, and every candidate preimage ruled out (balík kap. 5, „Vyvrátenie surjektivity“)
        aim(mm("Zvolím konkrétny cieľ §${ptTex(y0)}\\in ${spaceTex(f.real, f.l)}§ a dosadím ho do rovníc."),
            Formula("f($args)=${ptTex(y0)}\\Leftrightarrow " + f.m.indices.joinToString("\\wedge ") { "(" + linTex(f.m[it], vs, f.c[it]) + "=" + y0[it] + ")" }))
        val reason = when {
            rankOf(f.m.mapIndexed { i, row -> row + (y0[i] - f.c[i]) }) != f.rank -> run {
                val rel = relation(f)
                "Každý obraz spĺňa §${linTex(rel, outVs, 0)}=${rel.indices.sumOf { rel[it] * f.c[it] }}§ (kombinácia rovníc), bod §${ptTex(y0)}§ nie"
            }
            f.k == 1 && f.l == 1 -> "Rovnica má jediné riešenie §${a[0]}=" + f.preimageQ(y0.map { it.q() })[0].tex(true) + "§, a to nie je celé číslo"
            f.k == f.l -> "Determinant sústavy je §${det(f.m)}\\neq 0§, preto má jediné riešenie aj medzi racionálnymi číslami, §(" + f.preimageQ(y0.map { it.q() }).joinToString(",") { it.tex(true) } + ")§, a to nie je celočíselné"
            else -> { val g = f.m[0].fold(0L) { s, v -> gcd(s, abs(v)) }; "Hodnota §${linTex(f.m[0], vs, 0)}§ je vždy násobok §$g§, teda §f§ dáva len čísla tvaru §${g}k${if (f.c[0] == 0L) "" else if (f.c[0] > 0) "+${f.c[0]}" else "${f.c[0]}"}§" }
        }
        aim(mm("$reason. Bod §${ptTex(y0)}§ teda vzor nemá a zobrazenie **nie je** surjektívne."))
    }
}

/** Integer (λ, μ) ≠ 0 with λ·row1 + μ·row2 = 0, for maps whose image lies on a line. */
fun relation(f: Affine): List<Long> {
    for (s in 1..12) for (l1 in -s..s) for (l2 in -s..s) {
        if (l1 == 0L.toInt() && l2 == 0) continue
        val lam = listOf(l1.toLong(), l2.toLong())
        if ((0 until f.k).all { j -> lam[0] * f.m[0][j] + lam[1] * f.m[1][j] == 0L }) return lam
    }
    error("no relation")
}

/** s, t with a s + b t = 1 (gcd 1), small. */
fun bezout(a: Long, b: Long): Pair<Long, Long> {
    for (s in 0..20) for (sv in listOf(s.toLong(), -s.toLong())) for (t in -20L..20L) if (a * sv + b * t == 1L) return sv to t
    error("no bezout")
}

fun affineExercise(r: Random, shape: AffShape = AffShape.entries.random(r)): Exercise {
    val f = drawAffine(r, shape)
    val vs = if (f.real) RVars else ZVars
    val parts = ArrayList<(List<Any>) -> Part>()
    val inj = f.injective; val sur = f.surjective
    // why the differences must be zero, for this f: the same argument as its solution (injectivity above)
    val zeroWhy = if (!inj) "" else vs.take(f.k).let { a ->
        if (f.k == 1) "Z §${Terms.times(f.m[0][0], "(${a[0]}-${a[0]}')")}=0§ a §${f.m[0][0]}\\neq 0§ plynie §${a[0]}=${a[0]}'§."
        else "Rovnice pre rozdiely majú determinant §${det(f.m)}\\neq 0§, preto len riešenie §${a[0]}-${a[0]}'=${a[1]}-${a[1]}'=0§."
    }
    parts += { _ -> Part("Je §f§ injektívne?".let(::mm), Answer.Choice(listOf("Áno", "Nie"), if (inj) 0 else 1,
        mm(if (inj) "Áno, z §f(\\cdot)=f(\\cdot')§ vyjde rovnosť argumentov." else "Nie, dva rôzne body majú rovnaký obraz."),
        mapOf((if (inj) 1 else 0) to mm(if (inj) "Protipríklad neexistuje, z rovnosti obrazov plynie rovnosť bodov. $zeroWhy" else
            if (f.k > f.l) "Viac neznámych než rovníc, rozdiely bodov môžu byť nenulové. Protipríklad: §f${ptTex(List(f.k) { 0L })}=f${ptTex(f.kernel()!!)}§." else "Rovnice pre rozdiely majú aj nenulové riešenie. Protipríklad: §f${ptTex(List(f.k) { 0L }).let { if (f.k == 1) "($it)" else it }}=f${ptTex(f.kernel()!!).let { if (f.k == 1) "($it)" else it }}§."))),
        hint = mm("Z §f(\\cdot)=f(\\cdot')§ skús odvodiť rovnosť argumentov. Ak to nejde, hľadaj dva rôzne body s rovnakým obrazom."), aim = 0) }
    parts += { _ -> Part("Je §f§ surjektívne?".let(::mm), Answer.Choice(listOf("Áno", "Nie"), if (sur) 0 else 1,
        mm(if (sur) "Áno, vzor sa dá vyjadriť pre každý bod cieľa." else "Nie, bod §${ptTex(f.noPreimage()!!)}§ nemá vzor."),
        mapOf((if (sur) 1 else 0) to mm(if (sur) "Každý bod cieľa má vzor, vzorec pre vzor dáva vždy ${if (f.real) "reálne" else "celé"} čísla." else
            "Nájsť vzor pre jeden bod nestačí, musí ho mať každý. Bod §${ptTex(f.noPreimage()!!)}§ ho nemá (Varovanie 5.4)."))),
        hint = mm("Z §f(\\cdot)=y§ vyjadri vzor pre všeobecné §y§ a pozri, či leží v definičnom obore."), aim = 1) }
    if (!inj) {
        val d = f.kernel()!!
        val z = List(f.k) { 0L }
        val cands = listOf(List(f.k) { if (it == 0) 1L else 0L }, List(f.k) { if (it == f.k - 1) 1L else 0L }, List(f.k) { 1L }, List(f.k) { if (it == 0) -1L else 0L })
            .filter { it != d && f.at(it) != f.at(z) }.distinct().take(2)
        if (cands.size == 2) {
            fun pr(p: List<Long>) = "§" + ptTex(z) + "§ a §" + ptTex(p) + "§"
            val opts = listOf(d) + cands
            val order = listOf(0, 1, 2).shuffled(r)
            parts += { _ -> Part("Ktorá dvojica je protipríklad na injektivitu?", Answer.Choice(order.map { mm(pr(opts[it])) }, order.indexOf(0),
                mm("Rôzne body s rovnakým obrazom: §f${ptTex(z).let { if (f.k == 1) "($it)" else it }}=f${ptTex(d).let { if (f.k == 1) "($it)" else it }}=${ptTex(f.at(z))}§."),
                order.withIndex().filter { it.value != 0 }.associate { (i, v) -> i to mm("Obrazy sa líšia, to nie je protipríklad: §${ptTex(f.at(z))}\\neq ${ptTex(f.at(opts[v]))}§.") }),
                hint = mm("Protipríklad na injektivitu sú dva rôzne body s rovnakým obrazom. Spočítaj §f§ v oboch bodoch každej dvojice."), aim = 0) }
        }
    }
    if (!sur) {
        val y0 = f.noPreimage()!!
        val imgs = listOf(f.at(List(f.k) { 0L }), f.at(List(f.k) { if (it == 0) 1L else 0L }), f.at(List(f.k) { 1L })).filter { it != y0 }.distinct().take(2)
        if (imgs.size == 2) {
            val opts = listOf(y0) + imgs
            val pre = listOf(List(f.k) { 0L }, List(f.k) { if (it == 0) 1L else 0L }, List(f.k) { 1L })
            val order = listOf(0, 1, 2).shuffled(r)
            parts += { _ -> Part("Ktorý bod nemá vzor?", Answer.Choice(order.map { mm("§" + ptTex(opts[it]) + "§") }, order.indexOf(0),
                mm("Bod §${ptTex(y0)}§ vzor nemá, žiadne ${if (f.real) "reálne" else "celé"} argumenty naň nevedú."),
                order.withIndex().filter { it.value != 0 }.associate { (i, v) -> val p = pre.first { f.at(it) == opts[v] }; i to mm("Tento bod vzor má: §f${ptTex(p).let { if (f.k == 1) "($it)" else it }}=${ptTex(opts[v])}§.") }),
                hint = mm("Pre každý bod skús z §f(\\cdot)=y§ vyjadriť vzor a over, či leží v definičnom obore."), aim = 1) }
        }
    }
    val src = when (shape) {
        AffShape.Z22 -> "Šablóna z písomky 4. 12. 2025 (vzor), príklad 2b"
        AffShape.R32 -> "Šablóna z písomky 6. 12. 2024 (variant R), príklad 2c"
        AffShape.Z21 -> "Cvičení 5.10 e, g"
        AffShape.Z1 -> "Cvičení 5.9 a, c"
        AffShape.R22 -> "Příklad 5.7"
    }
    return Exercise(
        "dml-mz-injsurj", "Injektivita a surjektivita", src,
        mm("Vyšetřete injektivitu a surjektivitu zobrazení §${affineText(f)}§."),
        parts = parts,
        postup = InjSurjPostup,
        solution = { InjSurjPostup.solve { injectivity(f, InjSurjPostup[0]); surjectivity(f, InjSurjPostup[1]) } },
        hint = mm("Injektivita: z §f(\\cdot)=f(\\cdot')§ odvoď rovnosť argumentov. Surjektivita: z §f(\\cdot)=y§ vyjadri vzor a pozri, či je v definičnom obore."),
    )
}

/* ---- 6. Cardinalities: do such finite sets exist? ---------------------------------------------------------------- */

/** Every way to write [p] as c·d with c ≤ d: the sizes |C|, |D| a product |C × D| = p allows (Tvrzení 5.3). */
fun factorPairs(p: Int): List<Pair<Int, Int>> = (1..p).filter { p % it == 0 && it * it <= p }.map { it to p / it }

/** One line per factor pair: the product and the sum, as the generator's solution and the lesson's worked example show it. */
fun factorPairLines(p: Int): List<Formula> = factorPairs(p).map { Formula("${it.first}\\cdot ${it.second}=$p,\\quad ${it.first}+${it.second}=${it.first + it.second}") }

fun cardinalityExercise(r: Random): Exercise = when (r.nextInt(3)) {
    0 -> {
        // |C| + |D| = s and |C × D| = p (Písomka 5. 12. 2025 (variant K), 2b)
        val s = r.pick(5, 12)
        val pairs = (1 until s).map { it to s - it }
        val p = if (r.nextBoolean()) pairs.filter { it.first <= it.second }.random(r).let { it.first * it.second } else r.pick(2, s * s / 4 + 3)
        val sol = pairs.filter { it.first * it.second == p && it.first <= it.second }
        val yes = sol.isNotEmpty()
        val divisors = factorPairs(p)
        Exercise(
            "dml-mz-velkosti", "Existujú také množiny?", "Šablóna z písomky 5. 12. 2025 (variant K), príklad 2b; Tvrzení 5.2",
            mm("Existují množiny §C§ a §D§ takové, že §|C|+|D|=$s§ a současně platí §|C\\times D|=$p§?"),
            parts = listOf({ _ -> Part("Odpoveď:", Answer.Choice(listOf("Áno", "Nie"), if (yes) 0 else 1,
                mm(if (yes) "Áno, napr. §|C|=${sol[0].first}§ a §|D|=${sol[0].second}§." else "Nie, žiadne dve čísla so súčtom §$s§ nemajú súčin §$p§."),
                mapOf((if (yes) 1 else 0) to mm(if (yes) "Také veľkosti existujú: §${sol[0].first}+${sol[0].second}=$s§ a §${sol[0].first}\\cdot ${sol[0].second}=$p§." else
                    "Súčin §$p§ dávajú len dvojice ${divisors.joinToString(", ") { "§${it.first}\\cdot ${it.second}§" }}, a ich súčty nie sú §$s§."))))
            }),
            postup = CardSumPostup,
            solution = {
                val (numbers, factor, end) = CardSumPostup
                CardSumPostup.solve {
                    numbers(mm(Reasons.productSize + " Hľadám teda čísla §c,d\\in\\mathbb{N}_0§."), "c+d=$s,\\qquad c\\cdot d=$p")
                    factor(mm("Číslo §$p§ rozložím na súčin všetkými spôsobmi a pozriem sa na súčty."), factorPairLines(p))
                    end(mm(if (yes) "Vyhovuje §c=${sol[0].first}§, §d=${sol[0].second}§, napríklad §C=\\{1,\\dots,${sol[0].first}\\}§ a §D=\\{1,\\dots,${sol[0].second}\\}§." else "Žiadny rozklad nemá súčet §$s§, teda také množiny neexistujú."))
                }
            },
            hint = mm("§|C\\times D|=|C|\\cdot|D|§. Rozlož súčin na dvojice čísel."),
        )
    }
    1 -> {
        // C ⊆ D, |C × D| = p: which values can |D ∖ C| take? (Písomka 6. 12. 2023 (variant A), 2b)
        val p = listOf(12, 16, 18, 20, 24, 30, 36, 40, 48).random(r)
        val pairs = (1..p).filter { p % it == 0 && it * it <= p }.map { it to p / it }
        val values = pairs.map { it.second - it.first }.toSet()
        val decoys = pairs.map { it.second + it.first }.toSet() - values
        val opts = (values + decoys.shuffled(r).take(maxOf(0, 6 - values.size))).sorted()
        Exercise(
            "dml-mz-velkosti", "Možné veľkosti rozdielu", "Šablóna z písomky 6. 12. 2023 (variant A), príklad 2b; Tvrzení 5.2",
            mm("Mějme dvě množiny §C\\subseteq D§, a platí, že §|C\\times D|=$p§. Jakých všech hodnot může nabývat §|D\\setminus C|§?"),
            parts = listOf({ _ -> Part("Vyber všetky možné hodnoty.", Answer.Pick(opts.map { "§$it§".let(::mm) }, 1..opts.size, { s -> s.map { opts[it] }.toSet() == values }, { s ->
                val picked = s.map { opts[it] }.toSet()
                val miss = values.firstOrNull { it !in picked }
                if (miss != null) { val pr = pairs.first { it.second - it.first == miss }; mm("Hodnota §$miss§ chýba. Pre §|C|=${pr.first}§, §|D|=${pr.second}§ je §|D\\setminus C|=${pr.second}-${pr.first}=$miss§.") }
                else picked.firstOrNull { it !in values }?.let { ex -> mm("Hodnota §$ex§ nevyjde. Pre §C\\subseteq D§ je §|D\\setminus C|=|D|-|C|§, nie súčet.") } ?: mm("Presne tieto hodnoty sa dajú dosiahnuť.")
            })) }),
            postup = CardDiffPostup,
            solution = {
                val (numbers, pairsAim, end) = CardDiffPostup
                CardDiffPostup.solve {
                    numbers(mm(Reasons.productSize + " Tu je to §$p§, preto §C\\neq\\emptyset§, inak by bol súčin prázdny. Z §C\\subseteq D§ je §|C|\\leq|D|§ a §|D\\setminus C|=|D|-|C|§."))
                    pairsAim(mm("Vypíšem dvojice §(|C|,|D|)§ so súčinom §$p§ a §|C|\\leq|D|§; každú dosiahnem napríklad cez §C=\\{1,\\dots,|C|\\}\\subseteq D=\\{1,\\dots,|D|\\}§."),
                        pairs.map { Formula("(${it.first},${it.second}):\\quad |D\\setminus C|=${it.second}-${it.first}=${it.second - it.first}") })
                    end(mm("Možné hodnoty §|D\\setminus C|§ sú §${values.sorted().joinToString(",")}§."))
                }
            },
            hint = mm("§|C|\\cdot|D|=$p§ a §|C|\\leq|D|§; rozdiel má §|D|-|C|§ prvkov."),
        )
    }
    else -> {
        // |P(A)| = k (Písomka 8. 12. 2023 (variant D), 3b)
        val k = if (r.nextBoolean()) listOf(2, 4, 8, 16, 32, 64).random(r) else listOf(3, 5, 6, 7, 10, 12, 24, 48).random(r)
        val pow = k > 0 && (k and (k - 1)) == 0
        val n = 31 - JInteger.numberOfLeadingZeros(k)
        Exercise(
            "dml-mz-velkosti", "Potenčná množina", "Šablóna z písomky 8. 12. 2023 (variant D), príklad 3b; Definice 5.2",
            mm("Nalezněte příklad množiny §A§ (v libovolném universu), pro kterou platí, že §\\mathcal{P}(A)§ má právě §$k§ prvků, nebo dokažte, že taková množina v žádném universu neexistuje."),
            parts = listOf({ _ -> Part("Existuje taká §A§?".let(::mm), Answer.Choice(listOf("Áno", "Nie"), if (pow) 0 else 1,
                mm(if (pow) "Áno, každá §A§ s §$n§ prvkami, lebo §${Terms.pow(2L, n)}=$k§." else "Nie, §|\\mathcal{P}(A)|=2^{|A|}§ a §$k§ nie je mocnina dvojky."),
                mapOf((if (pow) 1 else 0) to mm(if (pow) "Taká množina existuje, napr. §A=\\{1,\\dots,$n\\}§. Má §${Terms.pow(2L, n)}=$k§ podmnožín." else "Počet podmnožín je vždy mocnina dvojky, §$k§ ňou nie je. Každý prvok buď v podmnožine je, alebo nie: §2^{|A|}§ možností."))))
            }),
            postup = PowerSetPostup,
            solution = {
                val (count, power, end) = PowerSetPostup
                PowerSetPostup.solve {
                    count(mm("Podmnožinu §n§-prvkovej §A§ určí §n§ nezávislých volieb „prvok v nej je alebo nie je“."), "|\\mathcal{P}(A)|=2^n")
                    power(mm(if (pow) "§$k§ je mocnina dvojky." else "§$k§ nie je mocnina dvojky: leží medzi dvoma susednými mocninami."), if (pow) "$k=${Terms.pow(2L, n)}" else "${Terms.pow(2L, n)}<$k<${Terms.pow(2L, n + 1)}")
                    end(mm(if (pow) "Vyhovuje každá §$n§-prvková množina, napríklad §A=\\{1,\\dots,$n\\}§." else "Počet podmnožín je vždy mocnina dvojky, taká §A§ neexistuje."))
                }
            },
            hint = mm("Koľko podmnožín má množina s §n§ prvkami?"),
        )
    }
}

/* ---- 7. A problem of a generated paper --------------------------------------------------------------------------- */

private val DefinitionAsks = listOf(
    "Pro množiny §A,B\\subseteq\\mathcal{U}§ definujte pojmy: „§A=B§“, rozdíl množin §A,B§ a kartézský součin §A,B§.",
    "Pro §f:A\\to B§ definujte pojmy injektivní, surjektivní a bijektivní zobrazení.",
    "Definujte (korektně, i se správnou volbou dotčených množin) pojmy složení zobrazení a identické zobrazení.",
    "Pro množiny §X,Y\\subseteq\\mathcal{U}§ definujte pojmy: „§X\\subseteq Y§“, „rozdíl množin §X,Y§“ a „potenční množina množiny §X§“.",
)

fun setsGeneratedProblem(r: Random): PaperProblem {
    val defs = DefinitionAsks.random(r)
    val card = cardinalityExercise(r)
    val third = if (r.nextBoolean()) identityExercise(r) else affineExercise(r, listOf(AffShape.Z22, AffShape.R32, AffShape.Z21).random(r))
    val text = "a) $defs b) ${card.prompt.replace('$', '§')} c) ${third.prompt.replace('$', '§')}" +
        (if (third.template == "dml-mz-injsurj") " Zkoumanou vlastnost vždy definujte. Platnost dokažte formálně z definice, neplatnost ukažte vymyšlením protipříkladu." else " Tvrzení buď podrobně dokažte odvozením (bez Vennova diagramu), nebo nalezněte protipříklad a ukažte, že se o protipříklad skutečně jedná.")
    return PaperProblem(
        n = 2, pts = 10, title = "Množiny a zobrazenia", text = mm(text), minutes = 25,
        checklist = listOf(
            Rubric("a) Definície presne, s množinami, na ktorých pojem žije, a s kvantifikátormi", 3),
            Rubric("b) Odpoveď zdôvodnená výpočtom alebo príkladom, nie len „áno/nie“", 3),
            Rubric("c) Rozpísanie z definície (formula pre §x\\in§ alebo §f(\\cdot)=f(\\cdot')§)", 2),
            Rubric("c) Záver: dôkaz pre každý prvok, alebo konkrétny overený protipríklad", 2),
        ).map { it.copy(text = mm(it.text)) },
        answer = mm("Pozri riešenie: b) a c) sú vypočítané z tých istých čísel."),
        drill = third.template,
        solution = parts("b) ${card.label}" to card.solution(emptyList()), "c) ${third.label}" to third.solution(emptyList())),
    )
}

/** |C|+|D| and |C×D| given: do such sets exist (Písomka 5. 12. 2025 (variant K), 2b). */
val CardSumPostup = postup(
    "Prelož na čísla" to "§|C\\times D|=|C|\\cdot|D|§: hľadáš dve čísla s daným súčtom a súčinom.",
    "Rozklady súčinu" to "Rozlož súčin na dvojice čísel a pozri ich súčty.",
    "Záver" to "Ak niektorá dvojica sedí, ukáž konkrétne množiny; inak neexistujú.",
)

/** |D∖C| for C ⊆ D with |C×D| given (Písomka 6. 12. 2023 (variant A), 2b). */
val CardDiffPostup = postup(
    "Prelož na čísla" to "§|C\\times D|=|C|\\cdot|D|§ a pri §C\\subseteq D§ je §|D\\setminus C|=|D|-|C|§.",
    "Možné dvojice" to "Vypíš dvojice §(|C|,|D|)§ s daným súčinom a §|C|\\leq|D|§.",
    "Možné hodnoty" to "Pre každú dvojicu spočítaj §|D|-|C|§.",
)

/** |P(A)| = k (Písomka 8. 12. 2023 (variant D), 3b). */
val PowerSetPostup = postup(
    "Koľko podmnožín?" to "Množina s §n§ prvkami má §2^n§ podmnožín.",
    "Je to mocnina dvojky?" to "Porovnaj dané číslo s mocninami dvojky.",
    "Záver" to "Mocnina dvojky: príklad množiny. Inak taká množina neexistuje.",
)

/* ---- Methods of the worked examples in the lessons ------------------------------------------------------------- */

val PowerSetListPostup = postup(
    "Vypíš podmnožiny" to "Vypisuj podľa počtu prvkov, od §\\emptyset§ po celú množinu.",
    "Pridaj nový prvok" to "Podmnožiny väčšej množiny sú staré podmnožiny, bez nového prvku aj s ním.",
)

val DomainRangePostup = postup(
    "Definičný obor" to "Pre ktoré vstupy má predpis zmysel?",
    "Obor hodnôt" to "Ktoré hodnoty naozaj vyjdú? Ku každej nájdi vstup, a vylúč tie, ktoré nevyjdú.",
)

val ImagePreimagePostup = postup(
    "Obraz §f[M]§" to "Zober obrazy prvkov §M§.",
    "Vzor §f^{-1}[N]§" to "Zober prvky, ktorých obraz leží v §N§.",
)

val InjPostup = postup(
    "Rozpíš §f(m)=f(n)§" to "Pre ľubovoľné argumenty napíš rovnosť obrazov a uprav ju.",
    "Vyrieš rovnicu" to "Zisti, či z nej plynie rovnosť argumentov; inak uvidíš, kde hľadať protipríklad.",
    "Záver" to "Plynie: prosté. Neplynie: konkrétne dva rôzne argumenty s rovnakým obrazom.",
)

val SurjPostup = postup(
    "Rozpíš §f(\\cdot)=y§" to "Pre ľubovoľný prvok cieľa napíš rovnicu pre vzor.",
    "Vyjadri vzor" to "Z rovníc vyjadri argumenty pomocou prvku cieľa.",
    "Over vzor" to "Leží vzor v definičnom obore a dá naozaj §y§? Potom je surjektívne.",
)

val InversePostup = postup(
    "Vyjadri vzor" to "Z §y=f(x)§ vyjadri §x§ pomocou §y§.",
    "Over obe zloženia" to "§f\\circ g§ aj §g\\circ f§ musia byť identity.",
    "Záver" to "Kandidát je inverzia; pozor, v akej množine žijú hodnoty.",
)

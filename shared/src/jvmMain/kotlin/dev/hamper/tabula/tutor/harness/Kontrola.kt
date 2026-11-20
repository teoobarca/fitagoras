package dev.hamper.tabula.tutor.harness

import dev.hamper.tabula.data.DmlGenerators
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.tutor.GemUloha
import dev.hamper.tabula.tutor.gemUlohy
import dev.hamper.tabula.tutor.overPostup
import kotlin.random.Random

/*
 * K: the engine's check without any model (docs/TUTOR.md, "Harness"). The tasks come from the app's own LA1
 * generators (every one whose solution has a numeric GEM); the student simulator does each by hand with one known
 * mistake or none; the engine must find exactly that mistake, and nothing in a right chain.
 */

/** The GEM tasks of the LA1 generators, [perGenerator] seeds each: the same tasks the tutor gets in the app. */
fun ulohyLa1(perGenerator: Int, seed0: Int = 0): List<Pair<String, GemUloha>> = ulohyLa1SoZadanim(perGenerator, seed0).map { it.first to it.second }

/** The same, with each exercise's assignment as the student sees it (what the reading call gets). */
fun ulohyLa1SoZadanim(perGenerator: Int, seed0: Int = 0): List<Triple<String, GemUloha, String>> {
    val ids = Generators.keys.filter { it !in DmlGenerators.keys }.sorted()
    val out = ArrayList<Triple<String, GemUloha, String>>()
    for (id in ids) for (s in seed0 until seed0 + perGenerator) {
        val ex = runCatching { Generators.getValue(id)(Random(s)) }.getOrNull() ?: continue
        gemUlohy(ex).forEach { out += Triple("$id/$s", it, ex.prompt) }
    }
    return out
}

/** What each mistake kind contributes to a run: a third of the cases have none. */
val ChybyVBehu: List<Chyba?> = listOf(null, null, null, null, Chyba.E1, Chyba.E1, Chyba.E2, Chyba.E3, Chyba.E4, Chyba.E5, Chyba.E6, Chyba.E9)

data class VysledokK(
    val pripady: Int, val bezChyby: Int, val spravne: Int, val zlyhania: List<String>,
    val poDruhoch: Map<String, Pair<Int, Int>>, val bunkyNahlasene: Int, val bunkyChybne: Int, val bunkyMozne: Int, val typOk: Int, val typSpolu: Int,
    val generatory: Set<String>,
    val pravdepodobne: Int = 0, val pravdepodobneOk: Int = 0,
)

fun behK(n: Int, seed: Long = 1): VysledokK {
    val r = Random(seed)
    val tasks = ulohyLa1(perGenerator = 40)
    require(tasks.isNotEmpty()) { "žiadna úloha s GEM" }
    val st = Student(r)
    var count = 0; var clean = 0; var ok = 0
    val fails = ArrayList<String>()
    val kinds = HashMap<String, IntArray>()
    var cellsReported = 0; var cellsWrong = 0; var cellsPossible = 0; var typOk = 0; var typAll = 0; var likely = 0; var likelyOk = 0
    val gens = HashSet<String>()
    var i = 0
    while (count < n && i < n * 20) {
        val (tid, u) = tasks[r.nextInt(tasks.size)]
        val e = ChybyVBehu[i % ChybyVBehu.size]
        i++
        val p = st.pripad("k$i", u, e) ?: continue
        count++
        gens += tid.substringBefore('/')
        if (e == null) clean++
        val v = overPostup(listOf(u), p.prepis)
        val h = ohodnot(p, v)
        val key = e?.name ?: "bez chyby"
        val c = kinds.getOrPut(key) { IntArray(2) }
        c[1]++
        if (h.ok) { ok++; c[0]++ } else if (fails.size < 40) fails += "$tid ${p.id} ${p.styl}: ${h.dovod}"
        if (p.pravda.bunka != null) { cellsPossible++; if (h.bunkaOk != null) { cellsReported++; if (h.bunkaOk == false) cellsWrong++ } }
        if (p.pravda.typ != null) { typAll++; if (h.typOk) typOk++ }
        h.pravdepodobnaOk?.let { likely++; if (it) likelyOk++ }
    }
    return VysledokK(count, clean, ok, fails, kinds.mapValues { it.value[0] to it.value[1] }, cellsReported, cellsWrong, cellsPossible, typOk, typAll, gens, likely, likelyOk)
}

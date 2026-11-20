package dev.hamper.tabula.la

/*
 * A GEM walk: every matrix of an elimination and the operations between them, in the form the course writes it
 * (konvencie 15. 11., skriptá LA1 chap-02.woo:3011-3083 and chap-04.woo:1282-1296): one kind of operation per "∼",
 * the kind (G1), (G2) or (G3) above it, the operations ("ř2 - 2·ř1") below it. Every GEM chain in the app is a walk and
 * is shown by one component (screens/GemWalk.kt), which keeps the matrix in place from step to step.
 *
 * Cells are TeX, so a walk can hold numbers, polynomials in a parameter or symbols (v_1); the walks built from numbers
 * are computed by the field, never typed.
 */

/** One matrix of a walk: cells as TeX and the bar of an augmented matrix (before column [bar]). */
data class GemFrame(val cells: List<List<String>>, val bar: Int? = null) {
    val m get() = cells.size
    val n get() = cells.firstOrNull()?.size ?: 0

    /** The column of the first nonzero element of each row ("první nenulový prvek"), null for a zero row. */
    val leads: List<Int?> get() = cells.map { r -> r.indices.firstOrNull { !isZeroTex(r[it]) } }

    /** Horní stupňovitý tvar (Definice 2.x): zero rows only at the bottom, first nonzero elements strictly to the right. */
    val isHst: Boolean get() {
        val l = cells.map { r -> r.indices.firstOrNull { !isZeroTex(r[it]) } }
        val nonzero = l.takeWhile { it != null }
        if (l.drop(nonzero.size).any { it != null }) return false
        return nonzero.zipWithNext().all { (a, b) -> a!! < b!! }
    }

    fun tex(): String {
        val spec = if (bar == null) "" else "|$bar"
        return "\\begin{pmatrix$spec}" + cells.joinToString("\\\\") { it.joinToString("&") } + "\\end{pmatrix}"
    }

    companion object {
        fun of(m: Mat) = GemFrame(m.rows.map { r -> r.map { it.tex(true) } }, m.bar)
        fun of(m: PMat) = GemFrame(m.rows.map { r -> r.map { it.tex() } }, m.bar)
    }
}

private fun isZeroTex(s: String) = s.trim() == "0"

/** One "∼": operations of a single kind and the matrix after them. */
data class GemMove(val ops: List<RowOp>, val after: GemFrame) {
    /** "G1", "G2", "G3" (or "G2, G3" for a multiple of the row itself plus another one). */
    val kind: String get() = ops.first().kind
    /** What goes above the ∼: "(G3)". */
    val kindTex: String get() = if (kind.isEmpty()) "" else "($kind)"
}

/**
 * [pole]: the field of a walk computed from numbers (null for a parameter or symbols): its matrices can then be read back
 * exactly, which is how the tutor knows which GEM an exercise asks for on paper (tutor/Ulohy.kt).
 */
class GemWalk(val start: GemFrame, val moves: List<GemMove>, val pole: Field? = null) {
    val frames: List<GemFrame> = listOf(start) + moves.map { it.after }
    val size get() = moves.size

    /** Rows that differ between frame [k] and the one before it. */
    fun changed(k: Int): Set<Int> = if (k == 0) emptySet() else frames[k].cells.indices.filter { frames[k].cells[it] != frames[k - 1].cells[it] }.toSet()

    /**
     * The first nonzero elements framed on step [k], one meaning: a first nonzero element ("první nenulový prvek")
     * under which the step makes zeros, the row it subtracts for (G3); on the last matrix in HST all of them, the steps
     * of the stairs. A swap or a multiple frames nothing: the tinted rows show what it changed.
     */
    fun framed(k: Int): Set<Pair<Int, Int>> {
        if (k == 0) return emptySet()
        val f = frames[k]
        val rows: Collection<Int> = if (k == size && endsInHst) f.cells.indices.toList()
            else moves[k - 1].ops.flatMap { o -> when (o) { is Combine -> listOf(o.src); is PolyCombine -> listOf(o.src); else -> emptyList() } }
        return rows.mapNotNull { i -> f.leads[i]?.let { i to it } }.toSet()
    }

    /** The last matrix is in HST: its main columns (hlavní sloupce) can be read and are shown under it. */
    val endsInHst: Boolean get() = frames.last().isHst

    /** H or V under every column of the last matrix (Klouda's board): H where a row has its first nonzero element. */
    val mainColumns: List<Boolean> get() {
        val last = frames.last()
        val lead = last.cells.mapNotNull { r -> r.indices.firstOrNull { !isZeroTex(r[it]) } }.toSet()
        return (0 until last.n).map { it in lead }
    }

    /** The whole chain on paper, for the tests and wherever only text can go. */
    fun tex(): String = start.tex() + moves.joinToString("") { "\\sim " + it.after.tex() }

    /** The operations as a note, one kind per ∼: "(G3) ř2-ř1, ř3-ř1; (G3) ř3-ř2". */
    fun note(): String = moves.joinToString("; ") { mv -> mv.kindTex + " " + mv.ops.joinToString(", ") { "$" + it.tex() + "$" } }

    companion object {
        /** The engine's elimination, each of its steps split into one ∼ per kind of operation. */
        fun of(e: Echelon): GemWalk = of(e.start, e.steps, pole = e.pole)

        /** A part of a chain: steps [from, to) continuing from the matrix before them. */
        fun of(start: Mat, steps: List<GemStep>, from: Int = 0, to: Int = steps.size, pole: Field? = null): GemWalk {
            var prev = if (from == 0) start else steps[from - 1].after
            val out = ArrayList<Pair<List<RowOp>, GemFrame>>()
            for (st in steps.subList(from, to)) { out += expandMat(prev, st.ops, st.after); prev = st.after }
            return fromSteps(GemFrame.of(if (from == 0) start else steps[from - 1].after), out, pole)
        }

        fun of(start: PMat, steps: List<PStep>): GemWalk {
            var prev = start
            val out = ArrayList<Pair<List<RowOp>, GemFrame>>()
            for (st in steps) { out += expandPoly(prev, st.ops, st.after); prev = st.after }
            return fromSteps(GemFrame.of(start), out)
        }

        /*
         * The engine keeps fractions out with ř_t := p·ř_t - q·ř_s, which on paper is two operations: (G2) p·ř_t, then
         * (G3) ř_t - q·ř_s. Such a step becomes its swaps, then its (G2) with the scaled rows computed, then the rest.
         * The engine does this only over R (in Z_p it divides), so the scaled rows are plain products.
         */
        private fun expandMat(prev: Mat, ops: List<RowOp>, after: Mat): List<Pair<List<RowOp>, GemFrame>> {
            val mixed = ops.filterIsInstance<Combine>().filter { it.a != Frac.ONE }
            if (mixed.isEmpty()) return listOf(ops to GemFrame.of(after))
            val swaps = ops.filterIsInstance<Swap>()
            var cur = prev
            val out = ArrayList<Pair<List<RowOp>, GemFrame>>()
            if (swaps.isNotEmpty()) { for (o in swaps) cur = Field.R.applyOp(cur, o); out += swaps to GemFrame.of(cur) }
            val scales = mixed.map { Scale(it.target, it.a) }
            for (o in scales) cur = Field.R.applyOp(cur, o)
            out += scales to GemFrame.of(cur)
            out += ops.filter { it !is Swap }.map { if (it is Combine && it.a != Frac.ONE) Combine(it.target, Frac.ONE, it.src, it.b) else it } to GemFrame.of(after)
            return out
        }

        private fun expandPoly(prev: PMat, ops: List<RowOp>, after: PMat): List<Pair<List<RowOp>, GemFrame>> {
            // a constant multiple (Combine) or a polynomial one (PolyCombine), both with a ≠ 1
            val mixed = ops.mapNotNull { o -> when { o is PolyCombine && o.a != Frac.ONE -> o.target to o.a; o is Combine && o.a != Frac.ONE -> o.target to o.a; else -> null } }
            if (mixed.isEmpty()) return listOf(ops to GemFrame.of(after))
            val rows = prev.rows.toMutableList()
            val out = ArrayList<Pair<List<RowOp>, GemFrame>>()
            val swaps = ops.filterIsInstance<Swap>()
            if (swaps.isNotEmpty()) { for (o in swaps) { val t = rows[o.i]; rows[o.i] = rows[o.j]; rows[o.j] = t }; out += swaps to GemFrame.of(PMat(rows.toList(), prev.bar)) }
            val scales = mixed.map { (t, a) -> Scale(t, a) }
            for (o in scales) rows[o.i] = rows[o.i].map { it * o.k }
            out += scales to GemFrame.of(PMat(rows.toList(), prev.bar))
            out += ops.filter { it !is Swap }.map { when {
                it is PolyCombine && it.a != Frac.ONE -> PolyCombine(it.target, Frac.ONE, it.src, it.q)
                it is Combine && it.a != Frac.ONE -> Combine(it.target, Frac.ONE, it.src, it.b)
                else -> it
            } } to GemFrame.of(after)
            return out
        }

        /**
         * Steps of the engine (several operations, maybe of different kinds, then the matrix) as one ∼ per kind. A
         * step changes each row at most once, so the matrix between two kinds is the previous one with the rows
         * done so far taken from the result.
         */
        fun fromSteps(start: GemFrame, steps: List<Pair<List<RowOp>, GemFrame>>, pole: Field? = null): GemWalk {
            val moves = ArrayList<GemMove>()
            var cur = start
            for ((ops, after) in steps) {
                if (ops.isEmpty()) continue
                val runs = ArrayList<MutableList<RowOp>>()
                for (o in ops) if (runs.isNotEmpty() && runs.last().first().kind == o.kind) runs.last() += o else runs += mutableListOf(o)
                // a swap is done on the rows as they are; any other operation takes its row from the result, which
                // is right only if no later run of this step touches that row again
                val splittable = runs.withIndex().all { (ri, run) -> run.filter { it !is Swap }.flatMap { touches(it) }
                    .none { r -> runs.drop(ri + 1).any { later -> later.any { r in touches(it) } } } }
                if (!splittable) { moves += GemMove(ops, after); cur = after; continue } // cannot split: one ∼
                for ((ri, run) in runs.withIndex()) {
                    val rows = cur.cells.toMutableList()
                    for (o in run) when (o) {
                        is Swap -> { val t = rows[o.i]; rows[o.i] = rows[o.j]; rows[o.j] = t }
                        else -> touches(o).forEach { rows[it] = after.cells[it] }
                    }
                    cur = if (ri == runs.lastIndex) after else GemFrame(rows, after.bar)
                    moves += GemMove(run, cur)
                }
                check(cur == after) { "a GEM step does not end in its matrix: ${ops.map { it.tex() }}" }
            }
            return GemWalk(start, moves, pole)
        }

        private fun touches(o: RowOp): List<Int> = when (o) {
            is Swap -> listOf(o.i, o.j)
            is Combine -> listOf(o.target)
            is Scale -> listOf(o.i)
            is PolyCombine -> listOf(o.target)
            else -> error("unknown row operation $o")
        }

        /**
         * A walk with symbols in the cells (v_1, 3v_1+v_2): the matrices are given, so nothing can be computed. Use
         * only where the entries are not numbers; a numeric chain goes through [Field.walk].
         */
        fun symbolic(start: List<List<String>>, bar: Int?, vararg moves: Pair<List<RowOp>, List<List<String>>>): GemWalk =
            GemWalk(GemFrame(start, bar), moves.map { (ops, cells) -> GemMove(ops, GemFrame(cells, bar)) })
    }
}

/* ---- Numeric walks: the operations are given, the matrices computed ------------------------------------------- */

/** Applies one row operation exactly in the field. */
fun Field.applyOp(m: Mat, op: RowOp): Mat {
    val rows = m.rows.toMutableList()
    when (op) {
        is Swap -> { val t = rows[op.i]; rows[op.i] = rows[op.j]; rows[op.j] = t }
        is Scale -> { require(!norm(op.k).isZero) { "(G2) needs a nonzero number" }; rows[op.i] = rows[op.i].map { mul(op.k, it) } }
        is Combine -> { require(op.target != op.src); rows[op.target] = rows[op.target].indices.map { j -> add(mul(op.a, rows[op.target][j]), mul(op.b, rows[op.src][j])) } }
        else -> error("no numeric meaning for $op")
    }
    return Mat(rows, m.bar)
}

/**
 * A walk from a start and the operations of each ∼ (each list one kind), computed in the field: `Field.R.walk(a,
 * listOf(rowSub(2, 2, 1), rowSub(3, 1, 1)), listOf(rowSwap(2, 3)))`. Rows are numbered from 1, as on paper.
 */
fun Field.walk(start: Mat, vararg moves: List<RowOp>): GemWalk {
    val s = Mat(start.rows.map { r -> r.map { norm(it) } }, start.bar)
    var cur = s
    val out = ArrayList<GemMove>()
    for (ops in moves) {
        require(ops.isNotEmpty() && ops.all { it.kind == ops[0].kind }) { "one kind of operation per ∼: ${ops.map { it.kind }}" }
        for (o in ops) cur = applyOp(cur, o)
        out += GemMove(ops, GemFrame.of(cur))
    }
    return GemWalk(GemFrame.of(s), out, this)
}

/** ř[t] - k·ř[s] (G3), rows from 1. */
fun rowSub(t: Int, k: Int, s: Int): RowOp = Combine(t - 1, Frac.ONE, s - 1, (-k).q())
fun rowSub(t: Int, k: Frac, s: Int): RowOp = Combine(t - 1, Frac.ONE, s - 1, -k)
/** ř[t] + k·ř[s] (G3), rows from 1. */
fun rowAdd(t: Int, k: Int, s: Int): RowOp = Combine(t - 1, Frac.ONE, s - 1, k.q())
/** ř[i] ↔ ř[j] (G1), rows from 1. */
fun rowSwap(i: Int, j: Int): RowOp = Swap(i - 1, j - 1)
/** k·ř[i] (G2), rows from 1. */
fun rowTimes(i: Int, k: Frac): RowOp = Scale(i - 1, k)

/** A matrix from integer rows, with an optional bar. */
fun intMat(vararg rows: List<Int>, bar: Int? = null) = Mat(rows.map { r -> r.map { it.q() } }, bar)

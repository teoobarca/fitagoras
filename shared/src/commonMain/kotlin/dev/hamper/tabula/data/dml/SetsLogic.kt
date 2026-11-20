package dev.hamper.tabula.data.dml

/*
 * Sets and set identities, decided by logic (Poznámka 5.3): membership in a set expression is a formula over the atoms
 * "x ∈ A". An identity holds iff the two formulas agree for every truth assignment of the atoms. For subsets of
 * U × U a pair (u,v) has atoms "u ∈ A" and "v ∈ A"; whether (u,v) lies in an expression depends only on those, so the
 * same check decides identities with ×. Nothing here is typed by hand: counterexamples are found and checked by code.
 */

/** A set expression. [level] 1: a subset of U; 2: a subset of U × U; null: not defined (Řešení 5.21: X ∖ (Y × Z)). */
sealed interface SE {
    data class V(val name: String) : SE
    data class Co(val a: SE) : SE
    data class Cap(val a: SE, val b: SE) : SE
    data class Cup(val a: SE, val b: SE) : SE
    data class Minus(val a: SE, val b: SE) : SE
    data class Times(val a: SE, val b: SE) : SE
    data object Empty : SE
    data object Univ : SE
}

/** 0 for ∅ (fits either level), 1 for U, 2 for U × U, -1 when undefined. */
fun SE.level(): Int = when (this) {
    is SE.V -> 1
    SE.Univ -> 1
    SE.Empty -> 0
    is SE.Co -> a.level()
    is SE.Times -> { val x = a.level(); val y = b.level(); if (x < 0 || y < 0 || x == 2 || y == 2) -1 else 2 }
    is SE.Cap -> both(a.level(), b.level())
    is SE.Cup -> both(a.level(), b.level())
    is SE.Minus -> both(a.level(), b.level())
}

private fun both(x: Int, y: Int): Int = when {
    x < 0 || y < 0 -> -1
    x == 0 -> y
    y == 0 -> x
    x == y -> x
    else -> -1
}

fun SE.vars(): Set<String> = when (this) {
    is SE.V -> setOf(name)
    is SE.Co -> a.vars()
    is SE.Cap -> a.vars() + b.vars()
    is SE.Cup -> a.vars() + b.vars()
    is SE.Minus -> a.vars() + b.vars()
    is SE.Times -> a.vars() + b.vars()
    SE.Empty, SE.Univ -> emptySet()
}

fun SE.rename(m: Map<String, String>): SE = when (this) {
    is SE.V -> SE.V(m[name] ?: name)
    is SE.Co -> SE.Co(a.rename(m))
    is SE.Cap -> SE.Cap(a.rename(m), b.rename(m))
    is SE.Cup -> SE.Cup(a.rename(m), b.rename(m))
    is SE.Minus -> SE.Minus(a.rename(m), b.rename(m))
    is SE.Times -> SE.Times(a.rename(m), b.rename(m))
    SE.Empty, SE.Univ -> this
}

/** Is the element (one of "x", or "u","v" for the parts of a pair) in the variable? */
typealias Assign = (elem: String, v: String) -> Boolean

/** Membership of x (level 1) or of the pair (u,v) (level 2) in this expression. */
fun SE.has(a: Assign, pair: Boolean, elem: String = "x"): Boolean = when (this) {
    is SE.V -> a(elem, name)
    SE.Univ -> true
    SE.Empty -> false
    is SE.Co -> !this.a.has(a, pair, elem)
    is SE.Cap -> this.a.has(a, pair, elem) && b.has(a, pair, elem)
    is SE.Cup -> this.a.has(a, pair, elem) || b.has(a, pair, elem)
    is SE.Minus -> this.a.has(a, pair, elem) && !b.has(a, pair, elem)
    is SE.Times -> this.a.has(a, false, "u") && b.has(a, false, "v")
}

enum class Rel(val tex: String) { EQ("="), SUB("\\subseteq") }

/** An identity "lhs rel rhs" between set expressions. */
data class Identity(val lhs: SE, val rel: Rel, val rhs: SE) {
    val level: Int get() = both(lhs.level(), rhs.level()).let { if (it == 0) 1 else it }
    val defined: Boolean get() = lhs.level() >= 0 && rhs.level() >= 0 && both(lhs.level(), rhs.level()) >= 0
    val vars: List<String> get() = (lhs.vars() + rhs.vars()).sorted()
    fun tex() = lhs.tex() + " " + rel.tex + " " + rhs.tex()
    fun rename(m: Map<String, String>) = Identity(lhs.rename(m), rel, rhs.rename(m))
}

/** All truth assignments of the atoms; for level 2 each variable has a "u" and a "v" atom. */
fun assignments(vars: List<String>, pair: Boolean): Sequence<Assign> = sequence {
    val elems = if (pair) listOf("u", "v") else listOf("x")
    val atoms = elems.flatMap { e -> vars.map { e to it } }
    for (mask in 0 until (1 shl atoms.size)) {
        val on = atoms.filterIndexed { i, _ -> mask shr i and 1 == 1 }.toSet()
        yield { e: String, v: String -> (e to v) in on }
    }
}

/** The atom assignment that breaks the identity, or null when it always holds. */
fun Identity.breaker(): Assign? {
    require(defined) { "undefined identity" }
    val pair = level == 2
    return assignments(vars, pair).firstOrNull { a ->
        val l = lhs.has(a, pair); val r = rhs.has(a, pair)
        when (rel) { Rel.EQ -> l != r; Rel.SUB -> l && !r }
    }
}

fun Identity.holds(): Boolean = breaker() == null

/* ---- Concrete finite sets -------------------------------------------------------------------------------------- */

/** A concrete choice: universe elements and which of them lie in each variable. */
data class Concrete(val universe: List<String>, val sets: Map<String, Set<String>>)

/** A concrete value: a set of elements, or of pairs. */
data class Value(val elems: Set<String>? = null, val pairs: Set<Pair<String, String>>? = null)

fun SE.eval(c: Concrete): Value = when (level()) {
    2 -> Value(pairs = c.universe.flatMap { u -> c.universe.map { v -> u to v } }.filter { (u, v) -> has({ e, n -> (if (e == "u") u else v) in c.sets.getValue(n) }, true) }.toSet())
    0 -> Value(elems = emptySet(), pairs = emptySet())
    1 -> Value(elems = c.universe.filter { x -> has({ _, n -> x in c.sets.getValue(n) }, false) }.toSet())
    else -> error("undefined")
}

fun Value.isEmpty() = (elems ?: emptySet()).isEmpty() && (pairs ?: emptySet()).isEmpty()

fun Identity.holdsOn(c: Concrete): Boolean {
    val l = lhs.eval(c); val r = rhs.eval(c)
    val le = l.elems ?: emptySet<Any>(); val re = r.elems ?: emptySet<Any>()
    val lp = l.pairs ?: emptySet(); val rp = r.pairs ?: emptySet()
    return when (rel) {
        Rel.EQ -> (if (level == 2) lp == rp else le == re)
        Rel.SUB -> (if (level == 2) rp.containsAll(lp) else re.containsAll(le))
    }
}

/** Elements in the order of the universe, so {3,1} prints as {1,3}. */
fun Value.tex(order: List<String>): String {
    val p = pairs; val e = elems
    return when {
        p != null && (e == null || p.isNotEmpty()) -> if (p.isEmpty()) "\\emptyset" else "\\{" + p.sortedWith(compareBy({ order.indexOf(it.first) }, { order.indexOf(it.second) })).joinToString(",") { "(${it.first},${it.second})" } + "\\}"
        e != null -> if (e.isEmpty()) "\\emptyset" else "\\{" + e.sortedBy { order.indexOf(it) }.joinToString(",") + "\\}"
        else -> "\\emptyset"
    }
}

fun setTex(s: Set<String>, order: List<String>) = Value(elems = s).tex(order)

/* ---- TeX ------------------------------------------------------------------------------------------------------- */

private fun SE.prec(): Int = when (this) { is SE.V, SE.Empty, SE.Univ, is SE.Co -> 9; else -> 1 }

/** As the textbook writes it: a binary operation inside another one is parenthesised. */
fun SE.tex(): String = when (this) {
    is SE.V -> name
    SE.Empty -> "\\emptyset"
    SE.Univ -> "\\mathcal{U}"
    is SE.Co -> "\\overline{" + a.tex() + "}"
    is SE.Cap -> bin(a, "\\cap", b)
    is SE.Cup -> bin(a, "\\cup", b)
    is SE.Minus -> bin(a, "\\setminus", b)
    is SE.Times -> bin(a, "\\times", b)
}

private fun bin(a: SE, op: String, b: SE) = wrap(a) + " " + op + " " + wrap(b)
private fun wrap(s: SE) = if (s.prec() < 9) "(" + s.tex() + ")" else s.tex()

/* ---- A compact syntax for content: "(A - B) & C", "~(X * Y)", "X * (Y | Z)" ----------------------------------- */

/**
 * Parses set expressions. Operators: ~ complement (prefix, binds tightest), & ∩, | ∪, - ∖, * ×, 0 ∅, U universe.
 * Mixing two different binary operators without parentheses is an error, so every expression is read one way only.
 */
fun se(s: String): SE = SeParser(s).run { val e = expr(); ws(); require(i == s.length) { "trailing input in '$s' at $i" }; e }

private class SeParser(val s: String) {
    var i = 0
    fun ws() { while (i < s.length && s[i] == ' ') i++ }
    fun expr(): SE {
        var left = unary()
        var op: Char? = null
        var minus = 0
        while (true) {
            ws()
            val c = s.getOrNull(i) ?: break
            if (c !in "&|-*") break
            require(op == null || op == c) { "mixed operators without parentheses in '$s'" }
            op = c; i++
            val right = unary()
            left = when (c) { '&' -> SE.Cap(left, right); '|' -> SE.Cup(left, right); '-' -> SE.Minus(left, right); else -> SE.Times(left, right) }
            if (c == '-') require(++minus == 1) { "A - B - C is ambiguous, write the parentheses: '$s'" }
        }
        return left
    }
    fun unary(): SE {
        ws()
        val c = s[i]
        return when {
            c == '~' -> { i++; SE.Co(unary()) }
            c == '(' -> { i++; val e = expr(); ws(); require(s[i] == ')') { "missing ) in '$s'" }; i++; e }
            c == '0' -> { i++; SE.Empty }
            c == 'U' -> { i++; SE.Univ }
            c.isLetter() -> { i++; SE.V(c.toString()) }
            else -> error("unexpected '$c' in '$s'")
        }
    }
}

fun identity(s: String): Identity {
    val (l, r, rel) = when {
        "<=" in s -> Triple(s.substringBefore("<="), s.substringAfter("<="), Rel.SUB)
        else -> Triple(s.substringBefore("="), s.substringAfter("="), Rel.EQ)
    }
    return Identity(se(l.trim()), rel, se(r.trim()))
}

/* ---- Unfolding membership into a formula, line by line (the shape of the textbook's solutions, Řešení 5.3 a ďalšie) */

/** A formula over memberships; [Mem] of a non-variable set is unfolded by the definitions of the operations. */
sealed interface Fm {
    data class Mem(val elem: String, val set: SE) : Fm
    data class Not(val a: Fm) : Fm
    data class And(val parts: List<Fm>) : Fm
    data class Or(val parts: List<Fm>) : Fm
    data object Top : Fm
    data object Bot : Fm
}

/** Truth value under an assignment of the atoms "e ∈ V". */
fun Fm.value(a: Assign): Boolean = when (this) {
    is Fm.Mem -> if (elem == "(u,v)") set.has(a, true) else set.has(a, false, elem)
    is Fm.Not -> !this.a.value(a)
    is Fm.And -> parts.all { it.value(a) }
    is Fm.Or -> parts.any { it.value(a) }
    Fm.Top -> true
    Fm.Bot -> false
}

private fun Fm.done(): Boolean = when (this) {
    is Fm.Mem -> set is SE.V
    is Fm.Not -> a.done()
    is Fm.And -> parts.all { it.done() }
    is Fm.Or -> parts.all { it.done() }
    else -> true
}

/** One level of unfolding everywhere: every membership in a compound set becomes the formula of its definition. */
private fun Fm.step(): Fm = when (this) {
    is Fm.Mem -> when (val s = set) {
        is SE.V -> this
        is SE.Co -> Fm.Not(Fm.Mem(elem, s.a))
        is SE.Cap -> Fm.And(listOf(Fm.Mem(elem, s.a), Fm.Mem(elem, s.b)))
        is SE.Cup -> Fm.Or(listOf(Fm.Mem(elem, s.a), Fm.Mem(elem, s.b)))
        is SE.Minus -> Fm.And(listOf(Fm.Mem(elem, s.a), Fm.Not(Fm.Mem(elem, s.b))))
        is SE.Times -> Fm.And(listOf(Fm.Mem("u", s.a), Fm.Mem("v", s.b)))
        SE.Empty -> Fm.Bot
        SE.Univ -> Fm.Top
    }
    is Fm.Not -> Fm.Not(a.step())
    is Fm.And -> Fm.And(parts.map { it.step() })
    is Fm.Or -> Fm.Or(parts.map { it.step() })
    else -> this
}

/** Same-kind nesting flattened, so ((p ∧ q) ∧ r) prints as (p ∧ q ∧ r). */
fun Fm.flat(): Fm = when (this) {
    is Fm.And -> Fm.And(parts.map { it.flat() }.flatMap { if (it is Fm.And) it.parts else listOf(it) })
    is Fm.Or -> Fm.Or(parts.map { it.flat() }.flatMap { if (it is Fm.Or) it.parts else listOf(it) })
    is Fm.Not -> Fm.Not(a.flat())
    else -> this
}

/** The chain of formulas from "e ∈ S" down to atoms; each line is equivalent to the previous by a definition. */
fun unfold(s: SE): List<Fm> {
    val start: Fm = Fm.Mem(if (s.level() == 2) "(u,v)" else "x", s)
    val out = arrayListOf(start)
    var f = start
    while (!f.done()) { f = f.step().flat(); out += f }
    return out
}

fun Fm.tex(top: Boolean = true): String = when (this) {
    is Fm.Mem -> elem + " \\in " + set.tex()
    is Fm.Not -> if (a is Fm.Mem && (a as Fm.Mem).set is SE.V) (a as Fm.Mem).elem + " \\notin " + (a as Fm.Mem).set.tex() else if (a is Fm.Mem) (a as Fm.Mem).elem + " \\notin " + (a as Fm.Mem).set.tex() else "\\neg " + a.tex(false)
    is Fm.And -> "(" + parts.joinToString(" \\wedge ") { it.tex(false) } + ")"
    is Fm.Or -> "(" + parts.joinToString(" \\vee ") { it.tex(false) } + ")"
    Fm.Top -> "\\top"
    Fm.Bot -> "\\bot"
}

/** Do two formulas agree for every assignment of the atoms over [vars]? */
fun equivalent(f: Fm, g: Fm, vars: List<String>, pair: Boolean) = assignments(vars, pair).all { f.value(it) == g.value(it) }
fun implies(f: Fm, g: Fm, vars: List<String>, pair: Boolean) = assignments(vars, pair).all { !f.value(it) || g.value(it) }

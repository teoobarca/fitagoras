package dev.hamper.tabula.math

import dev.hamper.tabula.compat.graphics.Canvas
import dev.hamper.tabula.compat.graphics.Paint
import dev.hamper.tabula.compat.graphics.Path
import dev.hamper.tabula.compat.graphics.RectF
import dev.hamper.tabula.compat.graphics.Typeface
import dev.hamper.tabula.compat.graphics.*
import dev.hamper.tabula.compat.LruCache
import kotlin.math.ceil
import kotlin.math.max
import dev.hamper.tabula.compat.JCharacter
import kotlin.math.roundToInt

/**
 * Formula layout written for this app: the textbooks' own font (the MathJax TeX font, Computer Modern shapes) set by
 * TeX's rules ([TexL]), so a formula looks as it does in the skriptá while the text around it stays Google Sans Flex.
 * The parser below reads the app's TeX dialect; [TexL] lays it out.
 */
data class MathFace(
    val name: String,
    /** The TeX font family with TeX's metrics. */
    val tex: TexFont,
    /** Math size relative to the text: MathJax sizes its font so the x-heights meet, 116.4 % next to Google Sans Flex. */
    val scale: Float = 1.164f,
    /** Extra stroke on every glyph in em: Computer Modern's thin strokes come out lighter on a phone than in a browser. */
    val boost: Float = 0f,
)

/**
 * The look of every formula in the app: the textbooks' (MathJax 4) TeX font, as they set it. Drawn plain, the glyphs
 * carry 19 % less ink than MathJax's in a browser at phone size; .015 em of stroke brings them to 103 % (no formula of
 * the specimen set lighter than the skriptá), and the shapes still overlay MathJax's within 2 px.
 */
val SkriptaMath = MathFace("Skriptá (MathJax TeX)", TexFont.MathJax, boost = .015f)

object MathFonts {
    private val faces = HashMap<String, Typeface>()

    fun plain(font: String): Typeface = faces.getOrPut("p$font") { loadTypeface(font, null) }

    /** The text font at a given weight, rounded like the rest of the app; [opsz] follows the size it is drawn at. */
    fun text(weight: Int, slant: Float = 0f, opsz: Float = 18f): Typeface = faces.getOrPut("t$weight/$slant/$opsz") {
        loadTypeface("google_sans_flex", "'wght' $weight, 'opsz' $opsz, 'ROND' 100, 'slnt' $slant")
    }
}

/**
 * Colours a formula may refer to by meaning: vectors u and v, wrong and right, the named objects of a statement
 * ([a], [b], [c]: `\ca{P}`) and the thin line under a tappable symbol ([mark]).
 */
data class MathInks(val u: Int, val v: Int, val bad: Int, val good: Int, val a: Int = u, val b: Int = v, val c: Int = bad, val mark: Int = 0)

enum class Cls { Ord, Op, Bin, Rel, Open, Close, Punct }

// ---- Nodes -----------------------------------------------------------------------------------

internal sealed interface N
internal enum class Kind { Letter, Greek, Upright, Sym, Double, Script, Bold }
internal data class Atom(val s: String, val cls: Cls, val kind: Kind) : N
internal data class Row(val items: List<N>, val cls: Cls = Cls.Ord) : N
internal data class Tint(val ink: Char, val inner: N) : N
/** A tappable symbol: [k] is its index in the screen's glossary; drawn with a thin line under it. */
internal data class Mark(val k: Int, val inner: N) : N
internal data class Vec(val inner: N) : N
internal data class Txt(val s: String) : N
internal data class Sup(val base: N, val sub: N?, val sup: N?) : N
/** [text]: \\tfrac, always in text style (TeX), whatever the style around it. */
internal data class Frac(val top: N, val bottom: N, val text: Boolean = false) : N
/** A base with a small note over and/or under it, as \\underset{u}{\\overset{o}{x}}; [cls] is the class of the whole. */
internal data class UnderOver(val base: N, val over: N?, val under: N?, val cls: Cls = Cls.Rel) : N
/** A big operator; [text]: as in running text (\\textstyle before it), limits beside it even in a display. */
internal data class Big(val s: String, val sub: N?, val sup: N?, val text: Boolean = false) : N
internal data class Fence(val l: String, val inner: N, val r: String) : N
/**
 * A matrix or table. [align]: per column l, c or r (null: all centred, a matrix); [aligned]: TeX's aligned, columns in
 * r l pairs with nothing between a pair (the relation keeps its own space) and a quad between pairs, cells in display style.
 */
internal data class Grid(val rows: List<List<N>>, val l: String, val r: String, val bar: Int? = null, val align: String? = null, val aligned: Boolean = false) : N
internal data class Gap(val em: Float) : N
/** An accent over [inner]: ˆ (\\hat) or ˜ (\\tilde). */
internal data class Hat(val inner: N, val accent: String = "ˆ") : N
/** A relation with a small note over it, \\stackrel{IP}{=}. */
internal data class Stackrel(val top: N, val base: N) : N
internal data class Over(val inner: N) : N
internal data class Sqrt(val inner: N, val index: N?) : N
internal data class Fn(val s: String) : N
internal data class Tall(val s: String, val h: Float) : N
/** The subspace sign ⊂⊂ (\\ssub): one relation drawn from two ⊂. */
internal data object Ssub : N
/** A control space "\\ ": a text space in the formula. */
internal data object CtrlSpace : N
/** (mod m) after a congruence, \\pmod{m}. */
internal data class Mod(val arg: N) : N

// ---- Parser ----------------------------------------------------------------------------------

private val Greek = mapOf(
    "alpha" to 'α', "beta" to 'β', "gamma" to 'γ', "delta" to 'δ', "varepsilon" to 'ε', "epsilon" to 'ε', "zeta" to 'ζ',
    "eta" to 'η', "theta" to 'θ', "lambda" to 'λ', "mu" to 'μ', "pi" to 'π', "rho" to 'ρ', "sigma" to 'σ', "tau" to 'τ',
    "phi" to 'φ', "varphi" to 'φ', "omega" to 'ω', "Delta" to 'Δ', "Sigma" to 'Σ', "kappa" to 'κ', "Theta" to 'Θ', "Gamma" to 'Γ', "Lambda" to 'Λ', "Pi" to 'Π', "Phi" to 'Φ', "Omega" to 'Ω', "nu" to 'ν', "iota" to 'ι', "xi" to 'ξ', "psi" to 'ψ', "chi" to 'χ',
)
private val Syms = mapOf(
    "in" to ("∈" to Cls.Rel), "notin" to ("∉" to Cls.Rel), "subset" to ("⊂" to Cls.Rel), "subseteq" to ("⊆" to Cls.Rel),
    "neq" to ("≠" to Cls.Rel), "ne" to ("≠" to Cls.Rel), "equiv" to ("≡" to Cls.Rel), "to" to ("→" to Cls.Rel), "mapsto" to ("↦" to Cls.Rel),
    "Rightarrow" to ("⇒" to Cls.Rel), "Leftrightarrow" to ("⇔" to Cls.Rel), "leq" to ("≤" to Cls.Rel), "geq" to ("≥" to Cls.Rel),
    "cdot" to ("⋅" to Cls.Bin), "times" to ("×" to Cls.Bin), "pm" to ("±" to Cls.Bin), "setminus" to ("∖" to Cls.Bin),
    "mid" to ("∣" to Cls.Rel), "forall" to ("∀" to Cls.Ord), "exists" to ("∃" to Cls.Ord), "emptyset" to ("∅" to Cls.Ord),
    "langle" to ("⟨" to Cls.Open), "rangle" to ("⟩" to Cls.Close), "lbrace" to ("{" to Cls.Open), "rbrace" to ("}" to Cls.Close),
    "cap" to ("∩" to Cls.Bin), "cup" to ("∪" to Cls.Bin), "vee" to ("∨" to Cls.Bin), "neg" to ("¬" to Cls.Ord),
    "vdots" to ("⋮" to Cls.Ord), "infty" to ("∞" to Cls.Ord), "implies" to ("⟹" to Cls.Rel), "impliedby" to ("⟸" to Cls.Rel), "iff" to ("⟺" to Cls.Rel),
    "dots" to ("…" to Cls.Ord), "ldots" to ("…" to Cls.Ord), "cdots" to ("⋯" to Cls.Ord), "wedge" to ("∧" to Cls.Bin),
    // sets, mappings, logic, number theory (BI-DML)
    "sim" to ("∼" to Cls.Rel), "simeq" to ("≃" to Cls.Rel), "cong" to ("≅" to Cls.Rel), "circ" to ("∘" to Cls.Bin), "supseteq" to ("⊇" to Cls.Rel), "supset" to ("⊃" to Cls.Rel), "subsetneq" to ("⊊" to Cls.Rel),
    "nsubseteq" to ("⊈" to Cls.Rel), "nRightarrow" to ("⇏" to Cls.Rel), "nLeftarrow" to ("⇍" to Cls.Rel), "Leftarrow" to ("⇐" to Cls.Rel), "top" to ("⊤" to Cls.Ord),
    "bot" to ("⊥" to Cls.Ord), "nmid" to ("∤" to Cls.Rel), "le" to ("≤" to Cls.Rel), "ge" to ("≥" to Cls.Rel), "lnot" to ("¬" to Cls.Ord),
    "land" to ("∧" to Cls.Bin), "lor" to ("∨" to Cls.Bin), "varnothing" to ("∅" to Cls.Ord), "approx" to ("≈" to Cls.Rel),
    "rightarrow" to ("→" to Cls.Rel), "leftrightarrow" to ("↔" to Cls.Rel), "colon" to (":" to Cls.Punct), "lt" to ("<" to Cls.Rel), "gt" to (">" to Cls.Rel),
    "lfloor" to ("⌊" to Cls.Open), "rfloor" to ("⌋" to Cls.Close), "lceil" to ("⌈" to Cls.Open), "rceil" to ("⌉" to Cls.Close),
    "triangleq" to ("≜" to Cls.Rel), "models" to ("⊨" to Cls.Rel),
)

/** Delimiters the renderer draws (\left, \big): anything else would come out blank. */
internal val DrawnDelims = setOf("(", ")", "[", "]", "{", "}", "|", "∣", "⟨", "⟩", "")
private val KnownEnvs = setOf("pmatrix", "vmatrix", "matrix", "aligned", "array")

/** \not and what follows it, as the precomposed negated glyph. */
private val NotOf = mapOf(
    "in" to "∉", "ni" to "∌", "=" to "≠", "<" to "≮", ">" to "≯", "subset" to "⊄", "supset" to "⊅", "subseteq" to "⊈",
    "supseteq" to "⊉", "mid" to "∤", "equiv" to "≢", "sim" to "≁", "simeq" to "≄", "cong" to "≇", "approx" to "≉",
    "leq" to "≰", "le" to "≰", "geq" to "≱", "ge" to "≱", "parallel" to "∦", "exists" to "∄",
)

/** The class of a symbol typed as a character, from its command (outside ASCII: ':' and '|' stay as they are). */
private val TypedCls: Map<String, Cls> by lazy {
    (Syms.values.associate { it.first to it.second } + NotOf.values.associateWith { if (it == "∄") Cls.Ord else Cls.Rel })
        .filterKeys { k -> k.length == 1 && k[0].code > 127 }
}

private class P(val s: String) {
    var i = 0
    /** Commands, environments and delimiters this engine does not know: shown as plain text or not at all. */
    val unknown = ArrayList<String>()
    /** \textstyle was just read: the next big operator or fraction is drawn as in running text. */
    var textStyle = false
    fun peek() = s.getOrNull(i)
    fun ws() { while (peek()?.isWhitespace() == true) i++ }
    /** The command [cmd] (with its backslash) starts here as a whole word: \\right, but not the start of \\rightarrow. */
    fun cmd(cmd: String) = s.startsWith(cmd, i) && s.getOrNull(i + cmd.length)?.isLetter() != true

    fun list(stop: (Char?) -> Boolean): List<N> {
        val out = ArrayList<N>()
        while (true) {
            ws()
            val c = peek()
            if (c == null || stop(c)) break
            if (cmd("\\right") || cmd("\\end") || s.startsWith("\\\\", i)) break
            // a soft break of a display formula (softLines) is no atom at all: the formula reads and spaces as without it
            if (cmd("\\softbreak")) { i += 10; if (peek() == '[') { while (i < s.length && s[i] != ']') i++; i++ }; continue }
            if (c == '^' || c == '_') {
                val base = if (out.isEmpty()) Row(emptyList()) else out.removeAt(out.lastIndex)
                out += scripts(base)
                continue
            }
            out += atom()
        }
        return out
    }

    private fun scripts(base: N): N {
        var sub: N? = (base as? Sup)?.sub
        var sup: N? = (base as? Sup)?.sup
        val b = (base as? Sup)?.base ?: base
        while (true) {
            ws()
            when (peek()) {
                '^' -> { i++; sup = arg() }
                '_' -> { i++; sub = arg() }
                else -> break
            }
        }
        return if (b is Big) b.copy(sub = sub, sup = sup) else Sup(b, sub, sup)
    }

    private fun arg(): N {
        ws()
        if (peek() == '{') {
            i++
            val l = list { it == '}' }
            if (peek() == '}') i++
            return Row(l)
        }
        return atom()
    }

    private fun name(): String {
        val st = i
        while (peek()?.isLetter() == true) i++
        return s.substring(st, i)
    }

    private fun braced(): String {
        ws(); if (peek() != '{') return ""
        val st = ++i
        var d = 1
        while (i < s.length && d > 0) { if (s[i] == '{') d++ else if (s[i] == '}') d--; i++ }
        return s.substring(st, i - 1)
    }

    private fun delim(): String {
        ws()
        val c = peek() ?: return ""
        if (c == '\\') {
            i++
            if (peek() == '{' || peek() == '}' || peek() == '|') return (s[i++]).toString()
            return when (val n = name()) { "langle" -> "⟨"; "rangle" -> "⟩"; "lbrace" -> "{"; "rbrace" -> "}"; "vert" -> "|"; "lvert" -> "|"; "rvert" -> "|"; else -> { unknown += "\\$n"; n } }
        }
        i++
        val d = if (c == '.') "" else c.toString()
        if (d !in DrawnDelims) unknown += "delimiter $d"
        return d
    }

    private fun atom(): N {
        val c = s[i]
        if (s.startsWith(":=", i)) {
            i += 2
            return Atom(":=", Cls.Rel, Kind.Upright)
        }
        when {
            c == '{' -> {
                i++
                val l = list { it == '}' }
                if (peek() == '}') i++
                // {,} is a decimal comma, a plain ordinary symbol.
                return if (l.size == 1 && l[0] is Atom) (l[0] as Atom).copy(cls = Cls.Ord) else Row(l)
            }
            c == '\\' -> return command()
            c.isDigit() -> { i++; return Atom(c.toString(), Cls.Ord, Kind.Upright) }
            c.isLetter() -> { i++; return Atom(c.toString(), Cls.Ord, Kind.Letter) }
        }
        i++
        return when (c) {
            '+' -> Atom("+", Cls.Bin, Kind.Upright)
            '-' -> Atom("−", Cls.Bin, Kind.Upright)
            '=' -> Atom("=", Cls.Rel, Kind.Upright)
            '<', '>' -> Atom(c.toString(), Cls.Rel, Kind.Upright)
            '(', '[' -> Atom(c.toString(), Cls.Open, Kind.Upright)
            ')', ']' -> Atom(c.toString(), Cls.Close, Kind.Upright)
            ',', ';' -> Atom(c.toString(), Cls.Punct, Kind.Upright)
            '|' -> Atom("|", Cls.Ord, Kind.Sym)
            // a symbol typed as itself (a ≢ b in the skriptá's definition) is the class its command has, as in MathJax:
            // a relation keeps its spaces
            else -> TypedCls[c.toString()]?.let { Atom(c.toString(), it, Kind.Sym) } ?: Atom(c.toString(), Cls.Ord, Kind.Upright)
        }
    }

    private fun command(): N {
        i++
        val c = peek()
        if (c != null && !c.isLetter()) {
            i++
            return when (c) {
                ',' -> Gap(.17f); ';' -> Gap(.28f); ' ' -> CtrlSpace; ':' -> Gap(.22f); '!' -> Gap(-.1f)
                '{' -> Atom("{", Cls.Open, Kind.Upright); '}' -> Atom("}", Cls.Close, Kind.Upright)
                else -> Atom(c.toString(), Cls.Ord, Kind.Upright)
            }
        }
        val n = name()
        Greek[n]?.let { return Atom(it.toString(), Cls.Ord, Kind.Greek) }
        Syms[n]?.let { return Atom(it.first, it.second, Kind.Sym) }
        return when (n) {
            "quad" -> Gap(1f)
            "qquad" -> Gap(2f)
            "U" -> Tint('u', Vec(Atom("u", Cls.Ord, Kind.Letter)))
            "V" -> Tint('v', Vec(Atom("v", Cls.Ord, Kind.Letter)))
            "cu", "cv", "cr", "cg", "ca", "cb", "cc" -> Tint(n[1], arg())
            // a single symbol keeps its class (a relation keeps its spacing); a longer piece is ordinary
            "tap" -> { val k = braced().trim().toIntOrNull() ?: -1; val a = arg(); Mark(k, (a as? Row)?.items?.singleOrNull() ?: a) }
            "ssub" -> Ssub
            "mathbf", "vec", "bm" -> Vec(arg())
            "mathbb" -> { val t = braced(); Atom(t, Cls.Ord, Kind.Double) }
            "mathcal" -> { val t = braced(); Atom(t, Cls.Ord, Kind.Script) }
            // \\mathrm: upright letters of the math font (MathJax); \\text: the words of the text around
            "mathrm" -> Atom(braced(), Cls.Ord, Kind.Upright)
            "text" -> Txt(braced())
            "gcd", "lcm", "max", "min", "deg", "det", "rank", "ker" -> Fn(n)
            "bmod" -> Row(listOf(Fn("mod")), Cls.Bin)
            "overline", "bar" -> Over(arg())
            "sqrt" -> {
                ws()
                val idx = if (peek() == '[') { i++; val st = i; while (i < s.length && s[i] != ']') i++; val t = s.substring(st, i); i++; P(t).let { Row(it.list { false }) } } else null
                Sqrt(arg(), idx)
            }
            "prod" -> Big("∏", null, null, text = textStyle).also { textStyle = false }
            "bigcup" -> Big("⋃", null, null, text = textStyle).also { textStyle = false }
            "bigcap" -> Big("⋂", null, null, text = textStyle).also { textStyle = false }
            "dim", "pmod" -> if (n == "dim") Fn("dim") else Mod(arg())
            "frac", "dfrac" -> { val t = textStyle && n == "frac"; textStyle = false; val a = arg(); val b = arg(); Frac(a, b, text = t) }
            // TeX's style switches: \textstyle makes the operator or fraction after it as in running text; the others
            // are drawn in the style the formula already has (never printed as a word)
            "textstyle" -> { textStyle = true; Row(emptyList()) }
            "displaystyle", "scriptstyle", "scriptscriptstyle", "nolimits", "limits" -> { if (n == "displaystyle") textStyle = false; Row(emptyList()) }
            "tfrac" -> { val a = arg(); val b = arg(); Frac(a, b, text = true) }
            // a GEM step as the textbook and the board write it: the rule over ∼, the row operation under it
            "gemstep" -> { val o = arg(); val u = arg(); UnderOver(Atom("∼", Cls.Rel, Kind.Sym), o, u) }
            "underset" -> { val u = arg(); val b = arg(); UnderOver(b, null, u, clsOf(b)) }
            "mathrel" -> Row(listOf(arg()), Cls.Rel)
            "sum" -> Big("∑", null, null, text = textStyle).also { textStyle = false }
            "hat" -> Hat(arg())
            "tilde" -> Hat(arg(), "˜")
            "stackrel", "overset" -> { val t = arg(); val b = arg(); Stackrel(t, b) }
            "ell" -> Atom("ℓ", Cls.Ord, Kind.Sym)
            "square" -> Atom("□", Cls.Ord, Kind.Sym)
            "not" -> {
                // \not before a relation, as one precomposed glyph (\not\in is ∉); a pair Unicode has no glyph for is
                // reported, never drawn as the word "not"
                ws()
                val save = i
                val next = when (peek()) {
                    '\\' -> { i++; name().ifEmpty { s.getOrNull(i)?.also { i++ }?.toString() ?: "" } }
                    '{' -> braced().trim().removePrefix("\\")
                    null -> ""
                    else -> s[i++].toString()
                }
                val neg = NotOf[next]
                if (neg != null) Atom(neg, if (next == "exists") Cls.Ord else Cls.Rel, Kind.Sym)
                else { i = save; unknown += "\\not $next"; Row(emptyList()) }
            }
            "Big", "big", "bigg", "Bigl", "Bigr", "bigl", "bigr" -> { val d = delim(); Tall(d, 1.5f) }
            "mod" -> Row(listOf(Gap(.5f), Fn("mod"), Gap(.25f)), Cls.Bin)
            "left" -> {
                val l = delim()
                val inner = Row(list { false })
                if (cmd("\\right")) i += 6
                val r = delim()
                Fence(l, inner, r)
            }
            "begin" -> {
                val env = braced()
                if (env.substringBefore('|') !in KnownEnvs) unknown += "\\begin{$env}"
                // array{rcl}: one letter per column, l, c or r; a | between columns is a divider, as in an augmented matrix
                val rawSpec = if (env == "array") braced() else null
                val spec = rawSpec?.filter { it in "lcr" }
                val specBar = rawSpec?.indexOf('|')?.takeIf { it > 0 }?.let { at -> rawSpec.take(at).count { it in "lcr" } }
                val rows = ArrayList<List<N>>()
                var row = ArrayList<N>()
                while (i < s.length) {
                    row.add(Row(list { it == '&' }))
                    ws()
                    when {
                        peek() == '&' -> i++
                        s.startsWith("\\\\", i) -> { i += 2; rows += row; row = ArrayList() }
                        cmd("\\end") -> { i += 4; braced(); rows += row; break }
                        else -> break
                    }
                }
                // "pmatrix|3": an augmented matrix, a bar before the fourth column
                val bar = env.substringAfter('|', "").toIntOrNull()
                when (env.substringBefore('|')) {
                    "pmatrix" -> Grid(rows, "(", ")", bar); "vmatrix" -> Grid(rows, "|", "|", bar)
                    // an l cell of aligned starts with an empty atom, so "&=" keeps the relation's space (amsmath's {})
                    "aligned" -> Grid(rows.map { r -> r.mapIndexed { k, c -> if (k % 2 == 1 && c is Row) Row(listOf(Row(emptyList())) + c.items) else c } }, "", "", null, aligned = true)
                    "array" -> Grid(rows, "", "", specBar, align = spec)
                    else -> Grid(rows, "", "", bar)
                }
            }
            else -> { unknown += "\\$n"; Txt(n) }
        }
    }
}

/**
 * Commands, environments and delimiters of [tex] the engine cannot draw; empty when everything is supported. Anything
 * left after the parser stopped would not be drawn at all (a stray \\right, \\end or \\\\): it is reported too.
 */
internal fun unknownCommands(tex: String): List<String> = P(tex).let { p ->
    p.list { false }
    p.unknown + listOfNotNull(tex.substring(p.i).takeIf { it.isNotBlank() }?.let { "not drawn: $it" })
}

/** The class of a node as the layout gives its box. */
internal fun clsOf(n: N): Cls = when (n) {
    is Atom -> n.cls
    is Row -> n.cls
    is Tint -> clsOf(n.inner)
    is Mark -> clsOf(n.inner)
    is Vec -> clsOf(n.inner)
    is Sup -> clsOf(n.base)
    is Big, is Fn -> Cls.Op
    is Ssub -> Cls.Rel
    is UnderOver -> n.cls
    is Stackrel -> (n.base as? Row)?.items?.singleOrNull()?.let(::clsOf) ?: clsOf(n.base)
    else -> Cls.Ord
}

/** The items of a row as spacing and the line breaker see them: a minus after an operator or an opening bracket is a sign. */
internal fun rowTokens(items: List<N>): List<Tok> {
    var prev: Cls? = null
    return items.map { n ->
        if (n is Gap || n is CtrlSpace) return@map Tok(Cls.Ord, gap = true)
        var cl = clsOf(n)
        // a sized bracket (\big(, or a \left( opened for line breaking) opens or closes, so a minus after it is a sign
        if (n is Tall) cl = when (n.s) { "(", "[", "{", "⟨" -> Cls.Open; ")", "]", "}", "⟩" -> Cls.Close; else -> cl }
        if (cl == Cls.Bin && (prev == null || prev in setOf(Cls.Bin, Cls.Rel, Cls.Open, Cls.Punct, Cls.Op))) cl = Cls.Ord
        prev = cl
        // only a \big bracket written as such counts as sized: an opened \left( keeps the rules of a plain bracket
        when (n) { is Atom -> Tok(cl, n.s); is Tall -> Tok(cl, n.s, sized = n.h != OpenedFence); is Mod -> Tok(cl, PMOD); else -> Tok(cl) }
    }
}

/** The top-level items of a formula, as the line breaker sees them. */
internal fun formulaTokens(tex: String): List<Tok> = rowTokens(breakItems(tex))

/**
 * The items of a formula when it may wrap: a \left…\right fence is opened into its delimiters and its items, so a
 * long span or set written with \left\langle or \left\{ can break between its items like one written without
 * \left. The delimiters then take the size of a \big one instead of the size of their content.
 */
internal fun breakItems(tex: String): List<N> = openFences(P(tex).list { false })

/** The height mark of a delimiter that came from an opened \\left…\\right. */
private const val OpenedFence = 1.25f

private fun openFences(items: List<N>): List<N> = items.flatMap { n ->
    if (n is Fence && n.l in DrawnDelims && n.r in DrawnDelims) {
        listOfNotNull(n.l.takeIf { it.isNotEmpty() }?.let { Tall(it, OpenedFence) }) + openFences((n.inner as? Row)?.items ?: listOf(n.inner)) +
            listOfNotNull(n.r.takeIf { it.isNotEmpty() }?.let { Tall(it, OpenedFence) })
    } else listOf(n)
}

// ---- Boxes -----------------------------------------------------------------------------------

/** Everything measured in pixels from the baseline; [x] is the pen position. */
abstract class MBox {
    abstract val w: Float
    abstract val asc: Float
    abstract val desc: Float
    abstract fun draw(c: Canvas, x: Float, y: Float, ink: Int)
    var cls: Cls = Cls.Ord
    /** Italic correction already inside [w]: a subscript tucks back by it, a superscript does not. */
    var ic: Float = 0f
    /** Tappable symbols inside, as (glossary index, box relative to the pen position and the baseline). */
    open fun marks(): List<Pair<Int, RectF>> = emptyList()
}

private fun List<Pair<Int, RectF>>.moved(dx: Float, dy: Float) = map { (k, r) -> k to RectF(r.left + dx, r.top + dy, r.right + dx, r.bottom + dy) }

/**
 * A tappable symbol, drawn exactly as the formula sets it: no mark on it. Dots under | and ≡ read as a typesetting
 * error and a patch behind them as a forgotten cursor (judges, 18. 11.); a screen that has tappable symbols says so in
 * words until the first tap (SymbolHint). The tap lands on a box as tall as a capital at least and never narrower than
 * half a letter.
 */
internal class MarkBox(val k: Int, val b: MBox, val size: Float, val line: Int) : MBox() {
    override val w get() = b.w
    override val asc get() = b.asc
    // the room the dotted line had under the letters: kept, so no line of any formula moves
    override val desc get() = maxOf(b.desc, size * .1f) + size * .07f + maxOf(1.6f, size * .05f) * (if (b.w < size * .5f) 1.25f else 1f)
    override fun draw(c: Canvas, x: Float, y: Float, ink: Int) = b.draw(c, x, y, ink)
    override fun marks(): List<Pair<Int, RectF>> {
        val half = maxOf(b.w / 2 + size * .07f, size * .3f)
        return listOf(k to RectF(b.w / 2 - half, -maxOf(b.asc, size * .72f) - size * .05f, b.w / 2 + half, maxOf(b.desc, size * .2f) + size * .05f)) + b.marks()
    }
}

// Lazy, so the parser and the line breaker load in a plain JVM unit test, where Paint does not exist.
private val paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG) }

class HBox(val parts: List<Pair<Float, MBox>>, override val w: Float) : MBox() {
    override val asc = parts.maxOfOrNull { it.second.asc } ?: 0f
    override val desc = parts.maxOfOrNull { it.second.desc } ?: 0f
    override fun draw(c: Canvas, x: Float, y: Float, ink: Int) { for ((dx, b) in parts) b.draw(c, x + dx, y, ink) }
    override fun marks() = parts.flatMap { (dx, b) -> b.marks().moved(dx, 0f) }
}

internal class Shifted(val b: MBox, val dy: Float) : MBox() {
    override val w get() = b.w
    override val asc get() = b.asc - dy
    override val desc get() = b.desc + dy
    override fun draw(c: Canvas, x: Float, y: Float, ink: Int) = b.draw(c, x, y + dy, ink)
    override fun marks() = b.marks().moved(0f, dy)
}

internal class Blank(override val w: Float) : MBox() {
    override val asc = 0f
    override val desc = 0f
    override fun draw(c: Canvas, x: Float, y: Float, ink: Int) = Unit
}

internal class Stack(val items: List<Triple<Float, Float, MBox>>, override val w: Float, override val asc: Float, override val desc: Float, val rules: List<RectF> = emptyList(), val color: Int? = null) : MBox() {
    override fun draw(c: Canvas, x: Float, y: Float, ink: Int) {
        for ((dx, dy, b) in items) b.draw(c, x + dx, y + dy, ink)
        paint.color = color ?: ink
        for (r in rules) c.drawRoundRect(r.left + x, r.top + y, r.right + x, r.bottom + y, r.height() / 2, r.height() / 2, paint)
    }
    override fun marks() = items.flatMap { (dx, dy, b) -> b.marks().moved(dx, dy) }
}

internal class Delim(val d: String, override val w: Float, override val asc: Float, override val desc: Float, val stroke: Float, val color: Int?, val taper: Boolean = false) : MBox() {
    override fun draw(c: Canvas, x: Float, y: Float, ink: Int) {
        val t = y - asc; val b = y + desc; val h = b - t; val m = (t + b) / 2
        val p = Path()
        val l = x + stroke; val r = x + w - stroke
        if (taper && (d == "(" || d == ")")) {
            // a filled crescent, thick in the middle and thin at the ends, like a font's own parenthesis
            val k = if (d == "(") 1f else -1f
            val ox = if (d == "(") r else l; val bulge = (r - l) * 1.25f
            val half = stroke * .62f; val end = stroke * .16f
            p.moveTo(ox, t)
            p.cubicTo(ox - k * (bulge + half), t + h * .3f, ox - k * (bulge + half), t + h * .7f, ox, b)
            p.lineTo(ox + k * end * 2, b)
            p.cubicTo(ox - k * (bulge - half), t + h * .7f, ox - k * (bulge - half), t + h * .3f, ox + k * end * 2, t)
            // filling closes the outline
            paint.style = Paint.Style.FILL; paint.color = color ?: ink
            c.drawPath(p, paint)
            return
        }
        when (d) {
            "(" -> { p.moveTo(r, t); p.cubicTo(l - (r - l) * .25f, t + h * .3f, l - (r - l) * .25f, t + h * .7f, r, b) }
            ")" -> { p.moveTo(l, t); p.cubicTo(r + (r - l) * .25f, t + h * .3f, r + (r - l) * .25f, t + h * .7f, l, b) }
            "|", "∣" -> { val mx = (l + r) / 2; p.moveTo(mx, t); p.lineTo(mx, b) }
            "[" -> { p.moveTo(r, t); p.lineTo(l + (r - l) * .25f, t); p.lineTo(l + (r - l) * .25f, b); p.lineTo(r, b) }
            "]" -> { p.moveTo(l, t); p.lineTo(r - (r - l) * .25f, t); p.lineTo(r - (r - l) * .25f, b); p.lineTo(l, b) }
            "⟨" -> { p.moveTo(r, t); p.lineTo(l, m); p.lineTo(r, b) }
            "⟩" -> { p.moveTo(l, t); p.lineTo(r, m); p.lineTo(l, b) }
            "{" -> {
                val mx = (l + r) / 2
                p.moveTo(r, t)
                p.cubicTo(mx - (r - mx) * .1f, t, mx, t + h * .06f, mx, t + h * .16f)
                p.lineTo(mx, m - h * .1f)
                p.cubicTo(mx, m - h * .03f, l + (mx - l) * .4f, m, l, m)
                p.cubicTo(l + (mx - l) * .4f, m, mx, m + h * .03f, mx, m + h * .1f)
                p.lineTo(mx, b - h * .16f)
                p.cubicTo(mx, b - h * .06f, mx - (r - mx) * .1f, b, r, b)
            }
            "}" -> {
                val mx = (l + r) / 2
                p.moveTo(l, t)
                p.cubicTo(mx + (mx - l) * .1f, t, mx, t + h * .06f, mx, t + h * .16f)
                p.lineTo(mx, m - h * .1f)
                p.cubicTo(mx, m - h * .03f, r - (r - mx) * .4f, m, r, m)
                p.cubicTo(r - (r - mx) * .4f, m, mx, m + h * .03f, mx, m + h * .1f)
                p.lineTo(mx, b - h * .16f)
                p.cubicTo(mx, b - h * .06f, mx + (mx - l) * .1f, b, l, b)
            }
        }
        paint.style = Paint.Style.STROKE; paint.strokeWidth = stroke; paint.strokeCap = Paint.Cap.ROUND; paint.strokeJoin = Paint.Join.ROUND
        paint.color = color ?: ink
        c.drawPath(p, paint)
        paint.style = Paint.Style.FILL
    }
}

object MathEngine {
    private val whole = LruCache<Key, MBox>(600)
    private val split = LruCache<Key, Pair<List<MBox>, List<Int>>>(600)

    /** Everything a formula's boxes depend on, compared whole: two themes whose inks hash alike never share a layout. */
    private data class Key(val tex: String, val px: Float, val face: MathFace, val inks: MathInks, val weight: Int, val bold: Boolean, val text: Boolean, val modBreak: Boolean = false)

    private fun key(t: String, px: Float, f: MathFace, i: MathInks, w: Int, b: Boolean, text: Boolean) = Key(t, px, f, i, w, b, text)

    /** [text]: the formula sits in running text (Rich), not on a line of its own. */
    fun layout(tex: String, px: Float, face: MathFace, inks: MathInks, weight: Int = 400, bold: Boolean = false, text: Boolean = false): MBox {
        val k = key(tex, px, face, inks, weight, bold, text)
        whole.get(k)?.let { return it }
        val b = TexL(face, inks, weight, bold).root(P(tex).list { false }, px, text)
        whole.put(k, b)
        return b
    }

    /** The same formula as pieces a text line may break between. */
    fun pieces(tex: String, px: Float, face: MathFace, inks: MathInks, weight: Int = 400, bold: Boolean = false, text: Boolean = false): List<MBox> =
        cut(tex, px, face, inks, weight, bold, text).first

    /**
     * The pieces of an inline formula for a line [lineWidth] wide: cut at its strong breaks (relations, commas), and at a
     * weak one (after +, −, ⋅) only inside a piece that is wider than the whole line (inlineBreakKinds).
     */
    fun inlinePieces(tex: String, px: Float, face: MathFace, inks: MathInks, weight: Int, bold: Boolean, lineWidth: Float, modBreak: Boolean = false): List<MBox> {
        val (ps, weak) = cut(tex, px, face, inks, weight, bold, true, modBreak)
        return joinLevelPieces(ps, weak, lineWidth, { ceil(it.w) + 1f }) { seg ->
            var x = 0f
            HBox(seg.map { b -> (x to b).also { x += b.w } }, x)
        }
    }

    private fun cut(tex: String, px: Float, face: MathFace, inks: MathInks, weight: Int, bold: Boolean, text: Boolean, modBreak: Boolean = false): Pair<List<MBox>, List<Int>> {
        val k = key(tex, px, face, inks, weight, bold, text).copy(modBreak = modBreak)
        split.get(k)?.let { return it }
        val out = ArrayList<MBox>()
        val weak = ArrayList<Int>()
        // only a formula that must wrap comes in pieces: a \\left…\\right group is opened so a long set can break inside it
        TexL(face, inks, weight, bold).root(breakItems(tex), px, text, out, weak, modBreak)
        return (out to weak).also { split.put(k, it) }
    }

    /** Ascent and descent (both positive) of the text font at [px], the font the formula sits among. */
    fun textMetrics(px: Float, weight: Int): Pair<Float, Float> {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = MathFonts.text(weight); textSize = px }
        val fm = p.fontMetrics
        return -fm.ascent to fm.descent
    }
}

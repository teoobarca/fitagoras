package dev.hamper.tabula.math

import dev.hamper.tabula.compat.graphics.Canvas
import dev.hamper.tabula.compat.graphics.Paint
import dev.hamper.tabula.compat.graphics.RectF
import dev.hamper.tabula.compat.graphics.Typeface
import kotlin.math.max
import dev.hamper.tabula.compat.JCharacter
import dev.hamper.tabula.compat.graphics.*
import dev.hamper.tabula.platform.Assets

/**
 * A TeX font family as the textbooks' MathJax ships it: one main face with every math alphabet, and the larger sizes
 * of big operators and delimiters, in composeResources/font ([files], in the order of [Variants]). [metricsFile]
 * (composeResources/files) holds TeX's own box of every glyph in em, one per line: variant, code point (hex), height,
 * depth, width, italic correction and, where it isn't 0, the skew an accent over the glyph moves right by. Both load
 * on the first formula.
 */
class TexFont(private val files: List<String>, private val metricsFile: String, val p: TexParams = TexParams()) {
    private val faces by lazy { files.map(MathFonts::plain) }
    private val tsv by lazy { Assets.read(metricsFile) }
    private val metrics: Map<String, Map<Int, FloatArray>> by lazy { parseMetrics(tsv) }
    private val composites: Map<Int, String> by lazy { parseComposites(tsv) }
    fun face(variant: String): Typeface = faces[Variants.indexOf(variant).coerceAtLeast(0)]
    fun m(variant: String, cp: Int): FloatArray? = metrics[variant]?.get(cp)
    /** What to draw for [cp] in the main face: the font has no glyph of its own for ≢ ⊄ ∄ ″…, MathJax draws these characters. */
    fun text(cp: Int): String = composites[cp] ?: JCharacter.toChars(cp).concatToString()

    companion object {
        /** "cal" is \\mathcal: its own face (MathJax's mjx-tex-c), the letters at their ASCII code points. */
        val Variants = listOf("normal", "smallop", "largeop", "size3", "size4", "cal")
        /** The MathJax TeX font (Apache-2.0 / OFL-1.1, files/mathjax-tex-font-LICENSE.txt), as the skriptá use it. */
        val MathJax = TexFont(listOf("mjx_tex_n", "mjx_tex_so", "mjx_tex_lo", "mjx_tex_s3", "mjx_tex_s4", "mjx_tex_c"), "mathjax-tex-metrics.tsv")

        internal fun parseMetrics(tsv: String): Map<String, Map<Int, FloatArray>> {
            val m = HashMap<String, HashMap<Int, FloatArray>>()
            // "0" in front of ".25" and "-.5": every platform's number parser takes that
            fun num(t: String) = (if (t.startsWith(".")) "0$t" else if (t.startsWith("-.")) "-0" + t.substring(1) else t).toFloat()
            for (line in tsv.lineSequence()) {
                val t = line.trimEnd('\r').split('\t'); if (t.size < 6) continue
                m.getOrPut(t[0]) { HashMap() }[t[1].toInt(16)] = floatArrayOf(num(t[2]), num(t[3]), num(t[4]), num(t[5]), if (t.size > 6) num(t[6]) else 0f)
            }
            return m
        }

        /** The main face's composites (8th column): code point -> the characters drawn for it. */
        internal fun parseComposites(tsv: String): Map<Int, String> = tsv.lineSequence().map { it.trimEnd('\r').split('\t') }
            .filter { it.size > 7 && it[0] == "normal" && it[7].isNotEmpty() }
            .associate { t -> t[1].toInt(16) to t[7].split('+').joinToString("") { JCharacter.toChars(it.toInt(16)).concatToString() } }
    }
}

/** TeX's font parameters (σ and ξ) for Computer Modern, as MathJax uses them; [script]: script size, [rowPitch]: array rows. */
data class TexParams(
    val xHeight: Float = .442f, val num1: Float = .676f, val num2: Float = .394f, val num3: Float = .444f,
    val denom1: Float = .686f, val denom2: Float = .345f, val sup1: Float = .413f, val sup2: Float = .363f, val sup3: Float = .289f,
    val sub1: Float = .15f, val sub2: Float = .247f, val supDrop: Float = .386f, val subDrop: Float = .05f,
    val axis: Float = .25f, val rule: Float = .06f, val bos1: Float = .111f, val bos2: Float = .167f, val bos3: Float = .2f,
    val bos4: Float = .6f, val bos5: Float = .1f, val scriptspace: Float = .05f, val nulldelim: Float = .12f,
    val delimFactor: Float = .901f, val delimShortfall: Float = .3f, val script: Float = .7071f,
    val colSep: Float = .5f, val strutH: Float = .75f, val strutD: Float = .25f, val rowGap: Float = .4f, val tablePad: Float = .124f,
    /** MathJax: scripts [extraIc] further out, fraction bars [fracExt] longer than the numerals on each side. */
    val extraIc: Float = .033f, val fracExt: Float = .1f,
)

private val texPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG) }

/** A glyph with TeX's box (height, depth, width from the font's metrics), not its ink. [dy] lowers it inside the box. */
internal class TexGlyph(val t: String, val tf: Typeface, val size: Float, override val w: Float, override val asc: Float, override val desc: Float, val color: Int?, val dy: Float = 0f, val dx: Float = 0f, val stroke: Float = 0f) : MBox() {
    /** How far right of the centre an accent over this glyph sits (the font's skew), px. */
    var sk = 0f
    override fun draw(c: Canvas, x: Float, y: Float, ink: Int) {
        texPaint.typeface = tf; texPaint.textSize = size; texPaint.color = color ?: ink
        if (stroke > 0f) { texPaint.style = Paint.Style.FILL_AND_STROKE; texPaint.strokeWidth = stroke * size; texPaint.strokeJoin = Paint.Join.ROUND } else texPaint.style = Paint.Style.FILL
        c.drawText(t, x + dx, y + dy, texPaint)
        texPaint.style = Paint.Style.FILL
    }
}


/** The first code point of [s]: a surrogate pair (the math alphabets above U+FFFF) is one. */
private fun firstCp(s: String): Int =
    if (s.length >= 2 && s[0].isHighSurrogate() && s[1].isLowSurrogate()) 0x10000 + ((s[0].code - 0xD800) shl 10) + (s[1].code - 0xDC00) else s[0].code
private fun oneCp(s: String) = s.length == 1 || (s.length == 2 && s[0].isHighSurrogate() && s[1].isLowSurrogate())

/** TeX's atom classes for spacing; [Inner] is a \\left…\\right group (and \\ldots). */
private enum class TC { Ord, Op, Bin, Rel, Open, Close, Punct, Inner }

/** TeX's spacing table (The TeXbook, p. 170) in mu/18: 1 thin, 2 medium, 3 thick; negative = only outside scripts. */
private val TexTable = arrayOf(
    //        Ord Op  Bin Rel Open Close Punct Inner
    intArrayOf(0, 1, -2, -3, 0, 0, 0, -1),   // Ord
    intArrayOf(1, 1, 0, -3, 0, 0, 0, -1),    // Op
    intArrayOf(-2, -2, 0, 0, -2, 0, 0, -2),  // Bin
    intArrayOf(-3, -3, 0, 0, -3, 0, 0, -3),  // Rel
    intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),      // Open
    intArrayOf(0, 1, -2, -3, 0, 0, 0, -1),   // Close
    intArrayOf(-1, -1, 0, -1, -1, -1, -1, -1), // Punct
    intArrayOf(-1, 1, -2, -3, -1, 0, -1, -1), // Inner
)
private fun texGap(a: TC, b: TC, script: Boolean): Float {
    val v = TexTable[a.ordinal][b.ordinal]
    if (v < 0 && script) return 0f
    return when (kotlin.math.abs(v)) { 1 -> 3f / 18; 2 -> 4f / 18; 3 -> 5f / 18; else -> 0f }
}

/** TeX's styles: display, text, script, scriptscript. */
internal enum class Sty { D, T, S, SS;
    fun sup() = when (this) { D, T -> S; else -> SS }
    fun num() = when (this) { D -> T; T -> S; else -> SS }
}

/**
 * Lays a parsed formula out by TeX's rules (The TeXbook, appendix G) with the textbooks' MathJax TeX font, so a formula
 * comes out as in the skriptá: italic correction, script shifts from σ13–σ19, fractions from σ8–σ12, big operators and
 * their limits from ξ9–ξ13, delimiters chosen from the font's sizes, TeX's math spacing.
 */
internal class TexL(val face: MathFace, val inks: MathInks, val weight: Int, val headBold: Boolean) {
    private val f = face.tex
    private val p = f.p
    /** Extra stroke of every TeX glyph in em: the face's [MathFace.boost], and more in heavier text (a heading stays one colour). */
    private val stroke = face.boost + if (headBold) (weight - 400) / 300f * .03f else 0f

    /** [top]: the formula's size in px; the style scales it (script 0.707, scriptscript 0.5). */
    data class St(val top: Float, val k: Float, val sty: Sty, val cramped: Boolean = false, val color: Int? = null, val vec: Boolean = false) {
        val size get() = top * when (sty) { Sty.D, Sty.T -> 1f; Sty.S -> k; Sty.SS -> k * k }
        val script get() = sty == Sty.S || sty == Sty.SS
    }

    fun root(items: List<N>, px: Float, inline: Boolean, split: ArrayList<MBox>? = null, weak: ArrayList<Int>? = null, modBreak: Boolean = false): MBox =
        row(items, St(px * face.scale, p.script, if (inline) Sty.T else Sty.D), split, weak, modBreak)

    private fun with(st: St, sty: Sty, cramped: Boolean = st.cramped) = st.copy(sty = sty, cramped = cramped)

    /** [weak]: with [split], the level of each cut between two pieces (inlineBreakLevels: 0 and 1 top, 2 and 3 inside a bracket, 4 weak). */
    fun row(items: List<N>, st: St, split: ArrayList<MBox>? = null, weak: ArrayList<Int>? = null, modBreak: Boolean = false): MBox {
        val toks = rowTokens(items)
        // TeX's classes: a \\left…\\right group and \\ldots are inner atoms
        val tcs = items.mapIndexed { i, n -> if (n is Fence || (n is Grid && (n.l.isNotEmpty() || n.r.isNotEmpty())) || (n as? Atom)?.s == "…") TC.Inner else if (isBmod(n)) TC.Ord else TC.valueOf(toks[i].cls.name) }
        val boxes = items.mapIndexed { i, n -> toks[i].cls to (if (n is Gap) Blank(n.em * st.size) else box(n, st)) }
        val kinds: Map<Int, Int> = if (split != null) inlineBreakLevels(toks, modBreak) else emptyMap()
        val breaks: Set<Int> = kinds.keys
        val parts = ArrayList<Pair<Float, MBox>>()
        var x = 0f; var startIdx = 0; var chunkStart = 0f
        // TeX puts the space between two atoms by their classes, whatever glue (\\quad, \\ ) stands between them
        var prev: TC? = null
        for ((i, pair) in boxes.withIndex()) {
            val b = pair.second
            if (items[i] !is Gap) { prev?.let { x += texGap(it, tcs[i], st.script) * st.size }; prev = tcs[i] }
            if (split != null && i > 0 && (i - 1) in breaks) {
                // a modulus starts with its own space ("−1 (mod 666)"): that space ends the piece before it, so a line
                // that starts with "(mod 666)" starts at its parenthesis, not indented (inspector, 18. 11.)
                val lead = if (items[i] is Mod) ((b as? HBox)?.parts?.firstOrNull()?.second as? Blank)?.w ?: 0f else 0f
                split += HBox(parts.subList(startIdx, parts.size).map { (px, pb) -> (px - chunkStart) to pb }.toList(), x + lead - chunkStart)
                weak?.add(kinds[i - 1] ?: 0)
                startIdx = parts.size; chunkStart = x + lead
            }
            parts += x to b
            x += b.w
        }
        split?.let { if (startIdx < parts.size) it += HBox(parts.subList(startIdx, parts.size).map { (px, pb) -> (px - chunkStart) to pb }.toList(), x - chunkStart) }
        return HBox(parts, x)
    }

    /** A character of the TeX font with its TeX box; italic correction kept apart in [MBox.ic] and added to the width. */
    private fun ch(cp: Int, st: St, cl: Cls, variant: String = "normal", addIc: Boolean = true): MBox {
        val m = f.m(variant, cp) ?: f.m("normal", cp)
        val own = f.m(variant, cp) != null
        val s = if (own && variant != "normal") JCharacter.toChars(cp).concatToString() else f.text(cp)
        val tf = f.face(if (own) variant else "normal")
        // a character the TeX font lacks (a letter with a diacritic typed into a formula): the text font's, never a blank box
        if (m == null) return textGlyph(s, st, cl)
        val ic = m[3] * st.size
        return TexGlyph(s, tf, st.size, m[2] * st.size + if (addIc) ic else 0f, m[0] * st.size, m[1] * st.size, st.color, stroke = stroke).also { it.cls = cl; it.ic = if (addIc) ic else 0f; it.sk = m[4] * st.size }
    }

    private fun textGlyph(s: String, st: St, cl: Cls): MBox {
        val tf = MathFonts.text(weight)
        texPaint.typeface = tf; texPaint.textSize = st.size
        // MathJax's \text inherits the page font at the formula's size; a text box of .75 em over and .25 em under the baseline
        return TexGlyph(s, tf, st.size, texPaint.measureText(s), .75f * st.size, .25f * st.size, st.color).also { it.cls = cl }
    }

    private fun cpOfLetter(c: Char, st: St): Int = when {
        st.vec -> when (c) { in 'A'..'Z' -> 0x1D400 + (c - 'A'); in 'a'..'z' -> 0x1D41A + (c - 'a'); in '0'..'9' -> 0x1D7CE + (c - '0'); else -> c.code }
        c == 'h' -> 0x210E
        c in 'a'..'z' -> 0x1D44E + (c - 'a')
        c in 'A'..'Z' -> 0x1D434 + (c - 'A')
        else -> c.code
    }

    private fun box(n: N, st: St): MBox = when (n) {
        is Atom -> when (n.kind) {
            Kind.Letter -> ch(cpOfLetter(n.s[0], st), st, n.cls)
            Kind.Greek -> ch(if (st.vec) 0x1D736 + (n.s[0] - 'α') else if (n.s[0] in 'α'..'ω') 0x1D6FC + (n.s[0] - 'α') else n.s[0].code, st, n.cls)
            Kind.Upright -> if (st.vec && n.s.length == 1 && n.s[0].isDigit()) ch(cpOfLetter(n.s[0], st), st, n.cls)
                else if (n.s.length == 1) ch(n.s[0].code, st, n.cls) else row(n.s.map { Atom(it.toString(), n.cls, Kind.Upright) }, st).also { it.cls = n.cls }
            Kind.Sym, Kind.Bold -> if (oneCp(n.s)) ch(firstCp(n.s), st, n.cls) else row(n.s.map { Atom(it.toString(), n.cls, Kind.Sym) }, st).also { it.cls = n.cls }
            Kind.Double -> row(n.s.map { Atom(dbl(it), Cls.Ord, Kind.Sym) }, st).also { it.cls = n.cls }
            // \\mathcal: Computer Modern's calligraphic capitals, as MathJax sets them (not the script letters of U+1D49C)
            Kind.Script -> n.s.map { c -> ch(c.code, st, Cls.Ord, if (c in 'A'..'Z') "cal" else "normal") }.let { bs ->
                if (bs.size == 1) bs[0].also { it.cls = n.cls } else { var x = 0f; HBox(bs.map { b -> (x to b).also { x += b.w } }, x).also { it.cls = n.cls } }
            }
        }
        is Txt -> textGlyph(n.s, st, Cls.Ord)
        // \operatorname: upright roman from the TeX font, an operator
        is Fn -> row(n.s.map { Atom(it.toString(), Cls.Ord, Kind.Upright) }, st).also { it.cls = Cls.Op }
        // \\bmod: "mod" with a thick space on both sides, as MathJax sets it (a binary operator would get a medium one)
        is Row -> if (isBmod(n)) row(n.items, st).let { m -> val k = 5f / 18 * st.size; HBox(listOf(k to m), m.w + 2 * k).also { it.cls = Cls.Ord } }
            else row(n.items, st).also { it.cls = n.cls }
        is Tint -> box(n.inner, st.copy(color = when (n.ink) { 'u' -> inks.u; 'v' -> inks.v; 'r' -> inks.bad; 'a' -> inks.a; 'b' -> inks.b; 'c' -> inks.c; else -> inks.good }))
        // a tappable symbol (glossary): a thin line under it, its box for the tap
        is Mark -> box(n.inner, st).let { inner -> MarkBox(n.k, inner, st.size, inks.mark).also { it.cls = inner.cls; it.ic = inner.ic } }
        is Vec -> box(n.inner, st.copy(vec = true))
        is Gap -> Blank(n.em * st.size)
        is CtrlSpace -> Blank(.166f * st.size).also { it.cls = Cls.Ord }
        is Sup -> scripts(n, st)
        is Frac -> frac(n, st)
        is Big -> bigOp(n, st)
        is Fence -> fence(n.l, box(n.inner, st), n.r, st)
        is Grid -> grid(n, st)
        is Ssub -> row(listOf(Atom("⊂", Cls.Rel, Kind.Sym), Atom("⊂", Cls.Rel, Kind.Sym)), st).also { it.cls = Cls.Rel }
        // amsmath \pmod: 8 mu (text) or 18 mu (display) before, roman "mod", 6 mu, the modulus
        is Mod -> row(listOf(Gap(if (st.sty == Sty.D) 1f else .444f), Atom("(", Cls.Open, Kind.Upright), Fn("mod"), Gap(.333f), n.arg, Atom(")", Cls.Close, Kind.Upright)), st)
        is UnderOver -> underOver(n, st)
        is Stackrel -> underOver(UnderOver(n.base, n.top, null, clsOf(n)), st)
        is Over -> over(n, st)
        is Hat -> accent(n, st)
        is Sqrt -> sqrt(n, st)
        // \\big( and friends: the first larger size
        is Tall -> delim(n.s, st.size * .85f, st.size * .35f, st).also { it.cls = Cls.Ord }
    }

    /** The one glyph a box is made of (a \\mathbb or \\mathbf letter, a coloured or tappable one), or null: MathJax treats it as a character. */
    private fun core(b: MBox): TexGlyph? = when (b) {
        is TexGlyph -> b
        is HBox -> b.parts.singleOrNull()?.takeIf { it.first == 0f }?.second?.let(::core)
        is MarkBox -> core(b.b)
        else -> null
    }

    private fun isBmod(n: N) = n is Row && n.cls == Cls.Bin && n.items.singleOrNull() == Fn("mod")

    private fun dbl(c: Char) = when (c) { 'R' -> "ℝ"; 'Z' -> "ℤ"; 'N' -> "ℕ"; 'Q' -> "ℚ"; 'C' -> "ℂ"; else -> JCharacter.toChars(0x1D538 + (c - 'A')).concatToString() }

    /** Rule 18: superscripts and subscripts. A character nucleus keeps its italic correction for the superscript only. */
    private fun scripts(n: Sup, st: St): MBox {
        val nuc = box(n.base, st)
        val c = core(nuc)
        val isChar = c != null
        val ic = c?.ic ?: nuc.ic
        val baseW = nuc.w - (if (n.sub != null) ic else 0f)
        val supSt = with(st, st.sty.sup()); val subSt = with(st, st.sty.sup(), cramped = true)
        val sup = n.sup?.let { box(it, supSt) }; val sub = n.sub?.let { box(it, subSt) }
        val xh = p.xHeight * st.size; val r = p.rule * st.size
        var u = if (isChar) 0f else nuc.asc - p.supDrop * supSt.size
        var v = if (isChar) 0f else nuc.desc + p.subDrop * subSt.size
        if (sup == null && sub != null) {
            v = maxOf(v, p.sub1 * st.size, sub.asc - .8f * xh)
        } else if (sup != null) {
            val pp = when { st.cramped -> p.sup3; st.sty == Sty.D -> p.sup1; else -> p.sup2 } * st.size
            u = maxOf(u, pp, sup.desc + xh / 4)
            if (sub != null) {
                v = maxOf(v, p.sub2 * st.size)
                if ((u - sup.desc) - (sub.asc - v) < 4 * r) {
                    v = 4 * r - u + sup.desc + sub.asc
                    val psi = .8f * xh - (u - sup.desc)
                    if (psi > 0) { u += psi; v -= psi }
                }
            }
        }
        val ss = p.scriptspace * st.size
        val items = ArrayList<Triple<Float, Float, MBox>>()
        items += Triple(0f, 0f, nuc)
        // a superscript after a character also clears its italic overhang (δ)
        // MathJax sets scripts p.extraIc further out than TeX (mjx-script's padding-left); the script space goes after the
        // script (padding-right), and only a nucleus with an italic overhang moves its superscript by .05 em more
        // (getAdjustedIc). Before every superscript it set "3^{-1}" with the exponent off the 3 (design review 18. 11.).
        val supX = baseW + (if (n.sub != null) ic else 0f) + (p.extraIc + if (ic > 0f) p.scriptspace else 0f) * st.size
        val subX = baseW + p.extraIc * st.size
        if (sup != null) items += Triple(supX, -u, sup)
        if (sub != null) items += Triple(subX, v, sub)
        val w = maxOf(baseW, if (sup != null) supX + sup.w + ss else 0f, if (sub != null) subX + sub.w + ss else 0f)
        val asc = maxOf(nuc.asc, if (sup != null) sup.asc + u else 0f)
        val desc = maxOf(nuc.desc, if (sub != null) sub.desc + v else 0f)
        return Stack(items, w, asc, desc).also { it.cls = nuc.cls }
    }

    /** Rule 15: fractions. \tfrac is a text-style fraction anywhere. */
    private fun frac(n: Frac, st0: St): MBox {
        val st = if (n.text && st0.sty == Sty.D) with(st0, Sty.T) else st0
        val numSt = with(st, st.sty.num()); val denSt = with(st, st.sty.num(), cramped = true)
        val num = box(n.top, numSt); val den = box(n.bottom, denSt)
        val s = st.size; val th = p.rule * s; val axis = p.axis * s
        var u: Float; var v: Float; val phi: Float
        if (st.sty == Sty.D) { u = p.num1 * s; v = p.denom1 * s; phi = 3 * th } else { u = p.num2 * s; v = p.denom2 * s; phi = th }
        val a = (u - num.desc) - (axis + th / 2); if (a < phi) u += phi - a
        val b = (axis - th / 2) - (den.asc - v); if (b < phi) v += phi - b
        val nd = p.nulldelim * s
        val inner = max(num.w, den.w) + 2 * p.fracExt * s
        val w = inner + 2 * nd
        val items = listOf(Triple(nd + (inner - num.w) / 2, -u, num), Triple(nd + (inner - den.w) / 2, v, den))
        return Stack(items, w, u + num.asc, v + den.desc, listOf(RectF(nd, -axis - th / 2, nd + inner, -axis + th / 2)), st.color).also { it.cls = Cls.Ord }
    }

    /** Rule 13: big operators. Display: the large glyph with limits over and under; text: the small one, limits as scripts. */
    private fun bigOp(n: Big, st: St): MBox {
        val cp = firstCp(n.s)
        val display = st.sty == Sty.D && !n.text
        val opRaw = ch(cp, st, Cls.Op, if (display) "largeop" else "smallop", addIc = false)
        // centred on the axis
        val shift = (opRaw.asc - opRaw.desc) / 2 - p.axis * st.size
        val op = Shifted(opRaw, shift).also { it.cls = Cls.Op }
        if (!display) {
            if (n.sub == null && n.sup == null) return op
            val sx = with(st, st.sty.sup()); val bx = with(st, st.sty.sup(), cramped = true)
            val sup = n.sup?.let { box(it, sx) }; val sub = n.sub?.let { box(it, bx) }
            var u = op.asc - p.supDrop * sx.size; var v = op.desc + p.subDrop * bx.size
            val xh = p.xHeight * st.size; val r = p.rule * st.size
            if (sup != null) u = maxOf(u, (if (st.cramped) p.sup3 else p.sup2) * st.size, sup.desc + xh / 4)
            if (sub != null) v = maxOf(v, if (sup != null) p.sub2 * st.size else maxOf(p.sub1 * st.size, sub.asc - .8f * xh))
            if (sup != null && sub != null && (u - sup.desc) - (sub.asc - v) < 4 * r) v = 4 * r - u + sup.desc + sub.asc
            val items = ArrayList<Triple<Float, Float, MBox>>(); items += Triple(0f, 0f, op)
            if (sup != null) items += Triple(op.w, -u, sup)
            if (sub != null) items += Triple(op.w, v, sub)
            val ss = p.scriptspace * st.size
            val w = op.w + max(sup?.w ?: 0f, sub?.w ?: 0f) + ss
            return Stack(items, w, maxOf(op.asc, if (sup != null) u + sup.asc else 0f), maxOf(op.desc, if (sub != null) v + sub.desc else 0f)).also { it.cls = Cls.Op }
        }
        return limits(op, n.sup?.let { box(it, with(st, st.sty.sup())) }, n.sub?.let { box(it, with(st, st.sty.sup(), cramped = true)) }, st, Cls.Op)
    }

    /** Limits over and under a nucleus, ξ9–ξ13. */
    private fun limits(nuc: MBox, over: MBox?, under: MBox?, st: St, cls: Cls): MBox {
        val s = st.size
        val w = maxOf(nuc.w, over?.w ?: 0f, under?.w ?: 0f)
        val items = ArrayList<Triple<Float, Float, MBox>>()
        items += Triple((w - nuc.w) / 2, 0f, nuc)
        val nh = max(nuc.asc, 0f); val nd = max(nuc.desc, 0f)
        var asc = nh; var desc = nd
        if (over != null) {
            val k = maxOf(p.bos1 * s, p.bos3 * s - over.desc)
            val y = -(nh + k + over.desc)
            items += Triple((w - over.w) / 2, y, over)
            asc = -y + over.asc + p.bos5 * s
        }
        if (under != null) {
            val k = maxOf(p.bos2 * s, p.bos4 * s - under.asc)
            val y = nd + k + under.asc
            items += Triple((w - under.w) / 2, y, under)
            desc = y + under.desc + p.bos5 * s
        }
        return Stack(items, w, asc, desc).also { it.cls = cls }
    }

    private fun underOver(n: UnderOver, st: St): MBox {
        val sx = with(st, st.sty.sup())
        return limits(box(n.base, st), n.over?.let { box(it, sx) }, n.under?.let { box(it, with(st, st.sty.sup(), cramped = true)) }, st, n.cls)
    }

    /** Rule 19: a delimiter at least 90.1 % of the height it encloses (and at most 0.3 em short), the smallest size of the font that does it. */
    private fun delim(d: String, h: Float, dp: Float, st: St): MBox {
        if (d.isEmpty()) return Blank(p.nulldelim * st.size)
        val axis = p.axis * st.size
        val half = max(h - axis, dp + axis)
        val need = max(2 * half * p.delimFactor, 2 * half - p.delimShortfall * st.size)
        val cp = when (d) { "|", "∣" -> 0x2223; else -> firstCp(d) }
        var pick: Pair<String, FloatArray>? = null
        for (v in listOf("normal", "smallop", "largeop", "size3", "size4")) {
            val m = f.m(v, cp) ?: continue
            pick = v to m
            if ((m[0] + m[1]) * st.size >= need) break
        }
        val (v, m) = pick ?: return Delim(d, st.size * .4f, axis + half, half - axis, st.size * .06f, st.color)
        val gh = m[0] * st.size; val gd = m[1] * st.size
        if (gh + gd < need) return Delim(d, m[2] * st.size, axis + need / 2, need / 2 - axis, st.size * .06f, st.color, true)
        // centred on the axis
        val up = axis - (gh - gd) / 2
        return TexGlyph(JCharacter.toChars(cp).concatToString(), f.face(v), st.size, m[2] * st.size, gh + up, gd - up, st.color, dy = -up, stroke = stroke)
    }

    private fun fence(l: String, inner: MBox, r: String, st: St): MBox {
        val lb = delim(l, inner.asc, inner.desc, st); val rb = delim(r, inner.asc, inner.desc, st)
        return HBox(listOf(0f to lb, lb.w to inner, lb.w + inner.w to rb), lb.w + inner.w + rb.w).also { it.cls = Cls.Ord }
    }

    /** \left( \begin{array}{cc|c} … \end{array} \right): cells centred, \arraycolsep on both sides of each column, rows at the textbook's pitch, the table centred on the axis. */
    private fun grid(n: Grid, st: St): MBox {
        // array cells are in text style, also in a display formula (TeX, MathJax); aligned keeps display style
        val cst = if (st.sty == Sty.D && !n.aligned) with(st, Sty.T) else st
        val cells = n.rows.map { r -> r.map { box(it, cst) } }
        val cols = cells.maxOf { it.size }
        val s = st.size
        val cw = FloatArray(cols); cells.forEach { r -> r.forEachIndexed { i, b -> cw[i] = max(cw[i], b.w) } }
        // aligned: no room inside an r l pair, a quad between pairs (amsmath); array and matrices: \arraycolsep on both sides
        val pad = if (n.aligned) 0f else p.colSep * s
        fun gapAfter(ci: Int) = if (n.aligned && ci % 2 == 1 && ci + 1 < cols) s else 0f
        fun place(ci: Int, b: MBox): Float {
            val a = when { n.aligned -> if (ci % 2 == 0) 'r' else 'l'; else -> n.align?.getOrNull(ci) ?: 'c' }
            return when (a) { 'l' -> 0f; 'r' -> cw[ci] - b.w; else -> (cw[ci] - b.w) / 2 }
        }
        // MathJax gives a column line a little room on both sides, and the table a little more at its end
        val hasBar = n.bar != null && n.bar in 1 until cols
        val lineRoom = if (hasBar) .0235f * s else 0f
        val rowH = cells.map { r -> max(r.maxOf { it.asc }, p.strutH * s) }; val rowD = cells.map { r -> max(r.maxOf { it.desc }, p.strutD * s) }
        val total = rowH.sum() + rowD.sum() + p.rowGap * s * (cells.size - 1) + 2 * p.tablePad * s
        val axis = p.axis * s
        val top = -axis - total / 2
        val items = ArrayList<Triple<Float, Float, MBox>>()
        var y = top + p.tablePad * s
        for ((ri, r) in cells.withIndex()) {
            val bl = y + rowH[ri]
            var x = 0f
            for ((ci, b) in r.withIndex()) {
                if (ci == n.bar) x += lineRoom
                items += Triple(x + pad + place(ci, b), bl, b); x += cw[ci] + 2 * pad + gapAfter(ci)
                if (ci + 1 == n.bar) x += lineRoom
            }
            y = bl + rowD[ri] + p.rowGap * s
        }
        val w = cw.sum() + 2 * pad * cols + (0 until cols).sumOf { gapAfter(it).toDouble() }.toFloat() + (if (hasBar) 2 * lineRoom + .023f * s else 0f)
        val rules = ArrayList<RectF>()
        if (n.bar != null && n.bar in 1 until cols) {
            val bx = (0 until n.bar).sumOf { (cw[it] + 2 * pad).toDouble() }.toFloat() + lineRoom
            val t = p.rule * s * 1.1f
            rules += RectF(bx - t / 2, top, bx + t / 2, top + total)
        }
        val table = Stack(items, w, -top, total + top, rules, st.color).also { it.cls = Cls.Ord }
        return fence(n.l, table, n.r, st)
    }

    private fun over(n: Over, st: St): MBox {
        val b = box(n.inner, st); val th = p.rule * st.size
        val y = -b.asc - 3 * th
        return Stack(listOf(Triple(0f, 0f, b)), b.w, b.asc + 4 * th, b.desc, listOf(RectF(0f, y - th, b.w, y)), st.color).also { it.cls = Cls.Ord }
    }

    /**
     * An accent over its base, as MathJax's mover places it: the accent's baseline 1.75 rule thicknesses over the
     * base (counted at least x-height tall) plus the accent's own depth (negative: its box floats where an x-height
     * letter's accent belongs), so a capital or a raised base lifts it. Centred, then moved right by the base glyph's
     * skew less the accent's, and by .75 of the base's italic correction.
     */
    private fun accent(n: Hat, st: St): MBox {
        val b = box(n.inner, st)
        val a = ch((if (n.accent == "ˆ") 0x2C6 else 0x2DC), st, Cls.Ord, addIc = false)
        val u = max(b.asc, p.xHeight * st.size) + p.rule * 1.75f * st.size + a.desc
        val c = core(b)
        val delta = (c?.sk ?: 0f) - (a as TexGlyph).sk + .75f * (c?.ic ?: b.ic)
        val w = max(b.w, a.w)
        var bx = (w - b.w) / 2; var ax = (w - a.w) / 2 + delta
        // nothing left of the box's start
        val lead = -minOf(0f, bx, ax); bx += lead; ax += lead
        // and its box .1 em over the accent (MathJax's big_op_spacing5)
        return Stack(listOf(Triple(bx, 0f, b), Triple(ax, -u, a)), max(bx + b.w, ax + a.w), max(b.asc, u + a.asc + p.bos5 * st.size), b.desc).also { it.cls = Cls.Ord }
    }

    /**
     * A radical as MathJax's msqrt sets it: the smallest √ of the font at least as tall as the cramped radicand plus a
     * rule, a surd-height gap and a quarter of φ (the x-height in display, a rule otherwise); the extra height of a
     * taller √ is split above and below; the bar runs from the √'s top over the radicand. \\sqrt[n]: the index in
     * scriptscript style over the √'s left part, lifted as MathJax's mroot lifts it.
     */
    private fun sqrt(n: Sqrt, st: St): MBox {
        val b = box(n.inner, st.copy(cramped = true))
        val s = st.size; val t = p.rule * s
        val phi = (if (st.sty == Sty.D) p.xHeight else p.rule) * s
        val need = b.asc + b.desc + 2 * t + phi / 4
        var pick: Pair<String, FloatArray> = "normal" to floatArrayOf(.8f, .2f, .833f, 0f, 0f)
        for (v in listOf("normal", "smallop", "largeop", "size3", "size4")) {
            val m = f.m(v, 0x221A) ?: continue
            pick = v to m
            if ((m[0] + m[1]) * s >= need) break
        }
        val (v, m) = pick
        val gh = m[0] * s; val g = (m[0] + m[1]) * s
        val q = if (g > need) (g - (b.asc + b.desc) + phi / 4) / 2 else t + phi / 4
        val top = b.asc + q + t
        // the √'s top meets the bar's top
        val surd = TexGlyph("√", f.face(v), s, m[2] * s, top, g - top, st.color, dy = gh - top, stroke = stroke)
        val ix = n.index?.let { box(it, with(st, Sty.SS)) }
        // MathJax's mroot: the index ends .6 of the √'s width into it, lifted .55 of the √'s size from its bottom
        val r = .6f * surd.w
        val iw = ix?.let { max(it.w, r) } ?: 0f
        val sx = if (ix != null) iw - r else 0f
        val lift = ix?.let { .55f * g - (g - top) + max(0f, it.desc) } ?: 0f
        val items = ArrayList<Triple<Float, Float, MBox>>()
        if (ix != null) items += Triple(iw - ix.w, -lift, ix)
        items += Triple(sx, 0f, surd); items += Triple(sx + surd.w, 0f, b)
        // the bar as thick as the √'s strokes, extra stroke included
        val sb = stroke * s / 2
        val bar = RectF(sx + surd.w - sb, -top - sb, sx + surd.w + b.w, -top + t + sb)
        // MathJax's box reaches a surd height (a rule) above the bar, as TeX's kern does
        val asc = maxOf(top + t, b.asc, if (ix != null) lift + ix.asc else 0f)
        return Stack(items, sx + surd.w + b.w, asc, max(g - top, b.desc), listOf(bar), st.color).also { it.cls = Cls.Ord }
    }
}

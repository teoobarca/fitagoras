package dev.hamper.tabula.la

/*
 * A table that grows row by row, shown by one component (screens/TableWalk.kt) the way GEM is: the layout is fixed
 * from the first step (every row and column reserved), each step reveals the next row with the rule that makes it written
 * above the table, the rows it comes from tinted, and numbers that move to the next row (Euclid: the divisor and the
 * remainder) sliding there. Euclid's divisions and the REA table (Poznámka 6.2) are tables like this; their builders
 * compute everything (data/dml/numbers), nothing is typed by hand (docs/CONTENT.md, "Vzorový príklad počíta engine").
 *
 * Teo (16. 11.): the equations of Euclid wrapped and the REA table was a flattened block; on a 320 dp phone nothing may
 * wrap: aligned rows under each other, the REA as a narrow n | r | α | β.
 */

/** Alignment of a column's cells. */
enum class Align { Start, Center, End }

/**
 * One step: the rule above the table ([label], TeX lines), the rows it uses, cells that move (from → to), and cells it
 * takes a number from ([marks], the REA's q in the row above).
 */
class TableStep(
    val label: List<String>, val sources: List<Int> = emptyList(), val slides: List<Pair<Pair<Int, Int>, Pair<Int, Int>>> = emptyList(),
    val marks: Set<Pair<Int, Int>> = emptySet(),
)

class TableWalk(
    /** Column heads (TeX), or null for no head row (Euclid's equations). */
    val header: List<String>?,
    /** Every row's cells as TeX; a row is revealed by its step. */
    val rows: List<List<String>>,
    val align: List<Align>,
    /** A vertical rule before these columns (the REA's n | r | α β). */
    val rules: Set<Int> = emptySet(),
    /** Rows shown before the first step (REA: the two given rows). */
    val start: Int,
    /** One step per further row; their count is rows.size - start. */
    val steps: List<TableStep>,
    /** What the last step ends with: cells framed and the label above the table. */
    val result: Set<Pair<Int, Int>> = emptySet(),
    val resultLabel: List<String> = emptyList(),
    /** Cells whose text is muted (the "=", "·" and "+" of an equation). */
    val quiet: Set<Int> = emptySet(),
    /** The line above the table before the first step: what every row means (REA), the first quotient (Euclid). */
    val startLabel: List<String> = emptyList(),
    /** Row numbers in the margin, small and grey, not a column (the REA's i, for the rule ř4=ř2-1·ř3). */
    val rowLabels: List<String>? = null,
) {
    init {
        require(rows.isNotEmpty() && rows.all { it.size == rows[0].size }) { "a table needs rows of one length" }
        require(start in 1..rows.size && steps.size == rows.size - start) { "one step per row after the first $start" }
        require(align.size == rows[0].size && (header == null || header.size == rows[0].size))
    }

    val size get() = steps.size
    val n get() = rows[0].size

    /** Rows visible on step [k]. */
    fun visible(k: Int) = start + k

    /** The table on paper, for the tests and wherever only text can go. */
    fun tex(): String {
        // what the formula engine draws (MathSourceTest): a matrix, the head as its first row, a bar after the first rule
        val bar = rules.minOrNull()?.let { "|$it" } ?: ""
        return "\\begin{matrix$bar}" + (listOfNotNull(header) + rows).joinToString("\\\\") { it.joinToString("&") } + "\\end{matrix}"
    }
}

package dev.hamper.tabula.data

/**
 * Reasons that recur in solution steps, written once (docs/CONTENT.md, "The reason in words, the theorem after it").
 * Each says why in words a student can write on the test; the theorem comes only after the reason, never instead of it.
 * Text uses § for math, like the rest of the content.
 */
object Reasons {
    /** Why GEM on columns tells which vectors are independent. */
    const val gemColumns = "Úpravy GEM nemenia riešenia sústavy §\\mathbf{A}\\mathbf{x}=\\theta§ s týmito stĺpcami, teda ani lineárne vzťahy medzi nimi."

    /** The chain from the main columns to a basis of the span (Veta 2.59); [pivots] are 0-based column indices. */
    fun mainColumns(pivots: List<Int>): String =
        if (pivots.size == 1) "Hlavný je len ${pivots[0] + 1}. stĺpec: v HST je nenulový a každý ostatný stĺpec je jeho násobkom. " +
            "GEM tieto vzťahy zachováva, preto pôvodný vektor pri ňom je nenulový a generuje celý obal. Tvorí teda jeho bázu (Veta 2.59)."
        else "Hlavné stĺpce sú ${pivots.joinToString(", ") { "${it + 1}." }}: v HST sú LN a každý ostatný stĺpec je ich kombináciou. " +
            "GEM tieto vzťahy zachováva, preto aj pôvodné vektory pri hlavných stĺpcoch sú LN a generujú celý obal. Tvoria teda jeho bázu (Veta 2.59)."

    /** Dimension from a basis that was found (Veta 2.57); [claim] is the math that follows, e.g. "\\dim M=2". */
    fun dim(claim: String) = "Dimenzia je počet vektorov ľubovoľnej bázy, preto §$claim§ (Veta 2.57)."

    /** A LN set as long as the dimension is a basis (Veta 2.55). */
    const val completeColumns = "Dané vektory a za ne celú štandardnú bázu zapíšem do stĺpcov a upravím GEM. Hlavné stĺpce tvoria bázu: dané vektory sú LN, takže ich stĺpce budú hlavné, a celý súbor generuje priestor (Klouda, 6. prednáška, postup na istotu)."
    const val pivotColumnsLN = "Matica len z hlavných stĺpcov má po tých istých úpravách všetky stĺpce hlavné, homogénna sústava má len triviálne riešenie, preto je súbor LN."
    const val lnOfDim = "Je to už báza, lebo vektor mimo obalu by s ním dal LN súbor dlhší, než dovolí dimenzia (Veta 2.55)."

    /** Coordinates exist and are unique (Veta 2.66). */
    const val coords = "Súradnice v báze existujú, lebo báza generuje, a sú jediné, lebo je LN (Veta 2.66)."

    /** Every subspace contains θ (Pozorovanie 2.8). */
    const val theta = "Každý podpriestor obsahuje §\\theta§, lebo s ľubovoľným §\\mathbf{x}§ obsahuje aj §0\\cdot\\mathbf{x}=\\theta§ (Pozorovanie 2.8)."

    /** A span is a subspace (Veta 2.38). */
    const val spanIsSubspace = "Lineárny obal je podpriestor vždy, lebo obsahuje §\\theta§ a súčet aj násobok lineárnych kombinácií je zase lineárna kombinácia (Veta 2.38)."

    /** GEM keeps the rank (Věta 3.6). */
    const val rankGem = "Hodnosť je dimenzia obalu riadkov a riadkové úpravy GEM tento obal nemenia, preto nemenia ani hodnosť (Věta 3.6)."

    /** The rank of a matrix in HST (Věta 3.7). */
    const val rankHst = "Nenulové riadky v HST sú LN, lebo pod prvým nenulovým prvkom každého riadku sú len nuly. " +
        "Generujú obal riadkov, preto je hodnosť ich počet (Věta 3.7)."

    /** |C×D| = |C|·|D| (Tvrzení 5.2). */
    const val productSize = "Pre konečné množiny je §|C\\times D|=|C|\\cdot|D|§, lebo ku každému prvku §C§ je §|D|§ dvojíc (Tvrzení 5.2)."
}

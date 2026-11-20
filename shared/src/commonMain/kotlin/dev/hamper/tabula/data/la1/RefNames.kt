package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.RefName
import dev.hamper.tabula.data.RemarkTitle
import dev.hamper.tabula.data.dsl.b
import dev.hamper.tabula.data.dsl.i
import dev.hamper.tabula.data.dsl.p
import dev.hamper.tabula.data.m

/** Numbered items of the LA1 textbook texts refer to that no card holds (see [RefName]). */
val La1RefNames: List<RefName> = listOf(
    RefName("poznamka", "2.36", "o lineárnom obale v známych priestoroch", "O lineárním obalu ve známých prostorech", listOf(
        p("""Lineární obaly souborů vektorů v prostorech §\mathbb{R}^2§ a §\mathbb{R}^3§ vypadají následovně:"""),
        i("""Lineární obal nulového vektoru (počátku) je množina pouze s počátkem."""),
        i("""Lineární obal nenulového vektoru z §\mathbb{R}^2§ (nebo z §\mathbb{R}^3§) je množina všech vektorů ležících ve společné přímce procházející počátkem."""),
        i("""Lineární obal LN souboru dvou vektorů z §\mathbb{R}^2§ je celé §\mathbb{R}^2§."""),
        i("""Lineární obal LN souboru dvou vektorů z §\mathbb{R}^3§ je množina všech vektorů ležících ve společné rovině obsahující počátek."""),
        i("""Lineární obal LN souboru tří vektorů z §\mathbb{R}^3§ je celé §\mathbb{R}^3§."""),
    )),
    RefName("postup", "2.69", "na nájdenie súradníc vektora v báze", "Nalezení souřadnic vektoru v bázi", listOf(
        p("""Pro zadaný vektor §\mathbf{v}\in P\ssub T^n§, kde §P§ má bázi §\mathcal{B}=(\mathbf{x}_1,\dots,\mathbf{x}_d)§, nalezněte souřadnice §\mathbf{v}§ vůči bázi §\mathcal{B}§."""),
        i("""Hledáme §d§-tici koeficientů §\alpha_1,\dots,\alpha_d\in T§ takovou, že příslušná lineární kombinace prvků báze je rovna vektoru §\mathbf{v}§."""),
        i("""Koeficienty §\alpha_1,\dots,\alpha_d\in T§ považujme za neznámé v rovnici §\alpha_1\mathbf{x}_1+\dots+\alpha_d\mathbf{x}_d=\mathbf{v}§."""),
        i("""Z definice vektorových operací rovnici výše převedeme na soustavu lineárních rovnic. Pravá strana soustavy bude určena vektorem §\mathbf{v}§."""),
        i("""Soustavu převedeme pomocí GEM do horního stupňovitého tvaru. Jelikož §\mathcal{B}=(\mathbf{x}_1,\dots,\mathbf{x}_d)§ je báze, musí existovat právě jedno řešení §(\alpha_1,\dots,\alpha_d)§ této soustavy, a to jsou hledané souřadnice vektoru §\mathbf{v}§ v bázi §\mathcal{B}§."""),
    )),
    RefName("poznamka", "3.2", "o potrebe transpozície v hodnosti", "O potřebě transpozice v hodnosti", listOf(
        p("""Ve vzorečku pro hodnost matice v Definici 3.1 se objevují transpozice (např. §(\mathbf{A}_{1:})^T§). Proč?"""),
        p("""Řádky matice nejsou vektory z §T^n§. Např. v následujícím Příkladu 3.3 je první řádek §\mathbf{A}_{1:}=\begin{pmatrix}1&3&1\end{pmatrix}§. Ten nepatří do žádného vektorového prostoru §T^n§, tedy se pak v našem kontextu nemá smysl bavit o lineárním obalu, natož o dimenzi."""),
        p("""Díky transpozici a ztotožnění z poznámky již vektory z §T^n§ získáme. Například v předchozím příkladu je"""),
        b("""\begin{pmatrix}1&3&1\end{pmatrix}^T=\begin{pmatrix}1\\3\\1\end{pmatrix}=(1,3,1)\in\mathbb{R}^3\,."""),
    )),
    RefName("poznamka", "4.22", "o koeficientoch rovnice, ktoré riešia špeciálnu sústavu", "O koeficientech rovnice řešících speciální soustavu", listOf(
        p("""Mějme jednu homogenní rovnici:"""),
        b("""\alpha_1\beta_1+\alpha_2\beta_2+\dots+\alpha_n\beta_n=0."""),
        p("""Tu si můžeme interpretovat jako, že §\mathbf{b}=(\beta_1,\dots,\beta_n)§ řeší soustavu"""),
        b("""\alpha_1x_1+\alpha_2x_2+\dots+\alpha_nx_n=0,"""),
        p("""ale také jako, že §\mathbf{a}=(\alpha_1,\dots,\alpha_n)§ řeší soustavu"""),
        b("""\beta_1x_1+\beta_2x_2+\dots+\beta_nx_n=x_1\beta_1+x_2\beta_2+\dots+x_n\beta_n=0."""),
        p("""Tedy máme-li nějaký vektor §\mathbf{x}=(x_1,\dots,x_n)§ z řešení soustavy"""), b("""\begin{pmatrix|3}\alpha_1&\cdots&\alpha_n&0\end{pmatrix}"""), p("""Tak potom vektor §\mathbf{a}=(\alpha_1,\dots,\alpha_n)§ obsahující koeficienty této lineární rovnice řeší soustavu"""), b("""\begin{pmatrix|3}x_1&\cdots&x_n&0\end{pmatrix}=\begin{pmatrix|1}\mathbf{x}^T&0\end{pmatrix}."""),
    )),
)

/**
 * The remarks (Poznámka) LA1 quotes whole, by number: the short Slovak name a reference gets in a source label and the
 * textbook's title (kurz/la1/cisla.tsv). A quoted remark a text refers to and missing here fails RefNamesTest.
 */
val La1RemarkTitles: Map<String, RemarkTitle> = mapOf(
    "2.4" to RemarkTitle("o vlastnostiach vektorového priestoru", m("O vlastnostech vektorového prostoru §T^n§")),
    "2.10" to RemarkTitle("o užitočnosti obmeny implikácie", "O užitečnosti obměny implikace"),
    "2.20" to RemarkTitle("o kolízii značenia súboru a vektora", "O kolizi značení souboru a vektoru"),
    "2.67" to RemarkTitle("o počte prvkov podpriestoru", "O počtu prvků podprostoru"),
    "3.9" to RemarkTitle("o aplikáciách hodnosti matice", "O aplikacích hodnosti matice"),
    "3.10" to RemarkTitle("o viacerých spôsoboch výpočtu", "O více možných způsobech výpočtu"),
    "3.29" to RemarkTitle("o matici úpravy (G1)", "O vlastnosti matice reprezentující (G1)"),
    "3.30" to RemarkTitle("o matici úpravy (G2)", "O vlastnosti matice reprezentující (G2)"),
    "3.31" to RemarkTitle("o matici úpravy (G3)", "O vlastnosti matice reprezentující (G3)"),
    "3.36" to RemarkTitle("o ďalších podmienkach regularity", "O dalších ekvivalentních podmínkách pro regularitu"),
    "4.3" to RemarkTitle("o popise množiny riešení homogénnej sústavy", m("O terminologii a reformulaci popisu §S_0§")),
    "4.17" to RemarkTitle("o názvoch pri varietách", "O terminologii u variet"),
    "4.19" to RemarkTitle("o názvoch špeciálnych variet", "O názvech některých speciálních variet"),
    "4.31" to RemarkTitle("o (ne)parametrickom vyjadrení variety", "O (ne)parametrickém vyjádření variety"),
)

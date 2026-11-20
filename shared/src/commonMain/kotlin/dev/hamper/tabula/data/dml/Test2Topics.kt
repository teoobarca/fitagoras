package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*

/**
 * What the 2nd test asks as the course page lists it (courses.fit.cvut.cz/BI-DML, Hodnocení, tab "2.zápočtovka",
 * "Konkrétně musíte:"; classification.adoc), each bullet word for word without the list's commas, shown on the page about
 * the test (data/TestPage.kt, TestTopic). The number theory bullets are the sub-items of "umět řešit úlohy z teorie čísel
 * (a znát tedy potřebné věty a tvrzení)".
 */
val DmlTest2Topics = listOf(
    TestTopic("Matematická indukcia", "znát principy indukce a z nich slabý a silný umět korektně aplikovat na řešení zadaného příkladu (správné komentování postupu a zdůvodňování, co z čeho dokazujeme je nutné)",
        listOf("dml.mi-principle", "dml.mi-matrix-power", "dml.mi-rec2", "dml.mi-nonlinear-seq")),
    TestTopic("Množiny a zobrazenia", "znát všechny definice z kapitoly Množiny a zobrazení a rozumět jim tak, abyste mohli odpovědět na jednoduchou doplňující otázku (typicky rozhodnutí o nějakém vztahu mezi pojmy, nalezení vhodného (proti)příkladu, atp.)",
        listOf("dml.mz-def-sets", "dml.mz-def-map", "dml.mz-def-power", "dml.mz-compose-exist", "dml.mz-sizes")),
    TestTopic("Množiny a zobrazenia", "pomocí znalostí z logiky (ne jen Vennových diagramů) rozhodnout o (ne)platnosti zadané množinové identity, k těm neplatným umět nalézt protipříklad",
        listOf("dml.mz-identity", "dml.mz-cartesian-identity")),
    TestTopic("Množiny a zobrazenia", "umět z definice rozhodnout o injektivitě a surjektivitě zadaného zobrazení, platnost korektně dokázat, k neplatnosti umět nalézt protipříklad",
        listOf("dml.mz-injsurj", "dml.mz-injsurj-nonlinear")),
    TestTopic("Teória čísel", "nalezení největšího společného dělitele/nejmenšího společného násobku a Bézoutových koeficientů", listOf("dml.tc-gcd-bezout")),
    TestTopic("Teória čísel", "řešení lineární diofantické rovnice", listOf("dml.tc-ldr")),
    TestTopic("Teória čísel", "nalezení multiplikativní inverze modulo m", listOf("dml.tc-inverse", "dml.tc-inverse-min-modulus", "dml.tc-inverse-euler")),
    TestTopic("Teória čísel", "redukce velkých mocnin modulo m", listOf("dml.tc-fermat-euler", "dml.tc-big-power", "dml.tc-tower")),
    TestTopic("Teória čísel", "řešení (bez použití hrubé síly) lineárních kongruencí", listOf("dml.tc-linear-congruence")),
    TestTopic("Teória čísel", "úlohy výše mohou být zkombinovány do jedné, podobně jako například ve Cvičení 6.28", listOf("dml.tc-crt", "dml.tc-crt-power-coef", "dml.tc-crt-word", "dml.tc-crt-interval", "dml.tc-crt-completeness")),
)

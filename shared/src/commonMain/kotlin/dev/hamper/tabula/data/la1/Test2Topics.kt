package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.*

/**
 * "Témata 2. zápočtové písemky" as the course page lists them (courses.fit.cvut.cz/BI-LA1, Zápočet a zkouška, "Co bude
 * v druhé zápočtové písemce?"; classification.adoc), shown on the page about the test (data/TestPage.kt, TestTopic).
 */
val La1Test2Topics = listOf(
    TestTopic("Vektorový priestor a podpriestor", "Měli byste vědět, jak je definován vektorový prostor §T^n§, kde §T§ je těleso, a jak se v něm počítá.", listOf("la1.def-vs", "la1.vec-ops")),
    TestTopic("Vektorový priestor a podpriestor", "Měli byste rozumět pojmu podprostor a znát jeho definici.", listOf("la1.def-subspace", "la1.subspace-decide")),
    TestTopic("Lineárna kombinácia", "Pojem lineární kombinace souboru a přilehlé názvosloví.", listOf("la1.def-lc", "la1.lc-compute")),
    TestTopic("Lineárna nezávislosť a závislosť", "Zvládnout dát do kupy definici a hlavně jí rozumět.", listOf("la1.def-ln")),
    TestTopic("Lineárna nezávislosť a závislosť", "Pochopení si ověřte na Pozorování 2.25, je Vám to jasné?", listOf("la1.prove-lz-basic")),
    TestTopic("Lineárna nezávislosť a závislosť", "Umět ověřit pomocí GEM, zda je daný soubor LN, či LZ.", listOf("la1.ln-decide", "la1.param-r")),
    TestTopic("Lineárny obal", "Chápat definici a jak vypadají lineární obaly v §\\mathbb{R}^2§ a v §\\mathbb{R}^3§.", listOf("la1.def-span", "la1.span-geom")),
    TestTopic("Lineárny obal", "Chápat, že lineární obal je vždy podprostor.", emptyList(), listOf("theorem:t2-38", "proof:p-obalpp")),
    TestTopic("Lineárny obal", "Umět ověřit, zda je nějaký vektor prvkem lineárního obalu daného souboru.", listOf("la1.span-member")),
    TestTopic("Lineárny obal", "Pochopit Větu 2.39 a Větu 2.42, které jsou strašně důležité. Umět je dokázat.", listOf("la1.prove-lz-lc", "la1.prove-ln-extend")),
    TestTopic("Báza a dimenzia", "Znát definice obojího.", listOf("la1.def-basis", "la1.def-dim")),
    TestTopic("Báza a dimenzia", "Umět ověřit, zda daný soubor je báze (tj. zda je LN a generuje daný podprostor).", listOf("la1.basis-decide")),
    TestTopic("Báza a dimenzia", "Znát standardní bázi prostoru §T^n§.", listOf("la1.standard-basis")),
    TestTopic("Báza a dimenzia", "Pochopit vztahy mezi dimenzí a bází.", listOf("la1.prove-dim-basis"), listOf("theorem:t2-56", "theorem:t2-57", "theorem:t2-58")),
    TestTopic("Báza a dimenzia", "Umět najít bázi podprostoru.", listOf("la1.span-basis-dim", "la1.basis-complete")),
    TestTopic("Súradnice v báze", "Chápat definici, umět dokázat, že souřadnice jsou určeny jednoznačně.", listOf("la1.def-coords", "la1.prove-coords-unique")),
    TestTopic("Súradnice v báze", "Pro zadanou bázi umět najít vektor, který je zadán souřadnicemi v této bázi. A naopak: pro vektor najít souřadnice v zadané bázi.", listOf("la1.coords")),
)

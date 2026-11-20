package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.RefName
import dev.hamper.tabula.data.dsl.b
import dev.hamper.tabula.data.dsl.i

/** Numbered items of the DML textbook texts refer to that no card holds: a definition no lesson teaches, quoted (see [RefName]). */
val DmlRefNames: List<RefName> = listOf(
    RefName("def", "4.9", "faktoriálu a Fibonacciho postupnosti", "Faktoriál a Fibonacciho postupnosť", listOf(
        i("""Funkce **faktoriál** je definována pro každé nezáporné celé číslo §n§ takto: §n!:=1§, pokud §n=0§, a §n!:=n\cdot(n-1)\cdot\cdots\cdot 3\cdot 2\cdot 1§, pokud §n\geq 1§. Ekvivalentně ji lze definovat (a následně i napočítávat) rekurzivně:"""),
        b("""\begin{aligned}0!&:=1,\\ n!&:=n\cdot(n-1)!\ \text{ pro každé } n\geq 1.\end{aligned}"""),
        i("""**Fibonacciho posloupnost** §(f_0, f_1, f_2, f_3, f_4, \dots)§ (jejíž §n§tý člen §f_n§ se také nazývá §n§té **Fibonacciho číslo**) je definována rekurzivně takto:"""),
        b("""\begin{aligned}f_0&:=0,\\ f_1&:=1,\\ f_n&:=f_{n-1}+f_{n-2}\ \text{ pro každé } n\geq 2.\end{aligned}"""),
    )),
)

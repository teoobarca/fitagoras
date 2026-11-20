package dev.hamper.tabula

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.data.*
import dev.hamper.tabula.math.*
import dev.hamper.tabula.screens.SolutionView
import dev.hamper.tabula.ui.*

/**
 * Debug only: the app's math on real content (lessons, skriptá cards, DML), one page at a time, to check typesetting.
 * adb shell am start -n dev.hamper.tabula/.SadzbaActivity --ei p <1..4> --ez dark <bool>
 */
class SadzbaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dev.hamper.tabula.platform.AndroidContext.install(this)
        val page = intent.getIntExtra("p", 1)
        val mode = if (intent.getBooleanExtra("dark", false)) ThemeMode.Dark else ThemeMode.Light
        setContent { TabulaTheme(mode) { Specimen(page) } }
    }
}

private fun explainNamed(t: String) = AllLessons.flatMap { it.steps }.filterIsInstance<Step.Explain>().first { it.title == t }
private fun workedNamed(t: String) = AllLessons.flatMap { it.steps }.filterIsInstance<Step.Worked>().first { it.title == t }

@Composable
internal fun Specimen(page: Int) {
    val c = T.c
    val body = MaterialTheme.typography.bodyLarge
    when (page) {
        1 -> {
            val s = explainNamed("Súbor alebo vektor?")
            StepFrame(14, 16, {}, footer = { Cta("Pokračovať", {}) }) {
                Spacer(Modifier.height(4.dp)); H1(s.title); Spacer(Modifier.height(10.dp))
                Rich(s.body, style = body, color = c.ink)
                Spacer(Modifier.height(14.dp)); SourceCard(s.src) { SourceLines(s.formal!!) }
                val d = ConceptById.getValue("trivialny")
                Spacer(Modifier.height(14.dp)); SourceCard(d.src) { SourceLines(d.czech) }
            }
        }
        2 -> {
            val w = workedNamed("Je to kombinácia?")
            StepFrame(10, 16, {}, footer = { Cta("Pokračovať", {}) }) {
                Spacer(Modifier.height(4.dp)); Label("Riešený príklad"); Spacer(Modifier.height(2.dp)); H1(w.title); Spacer(Modifier.height(10.dp))
                SourceCard(w.src ?: "Zadanie") { Rich(w.problem, style = body, color = c.ink) }
                Spacer(Modifier.height(12.dp)); SolutionView(w.solution)
            }
        }
        3 -> {
            val w = workedNamed("Príklad zo skrípt")
            val d = ConceptById.getValue("podpriestor")
            StepFrame(7, 16, {}, footer = { Cta("Pokračovať", {}) }) {
                Spacer(Modifier.height(4.dp)); Label("Riešený príklad"); Spacer(Modifier.height(2.dp)); H1(w.title); Spacer(Modifier.height(10.dp))
                SourceCard(w.src ?: "Zadanie") { Rich(w.problem, style = body, color = c.ink) }
                Spacer(Modifier.height(12.dp)); SolutionView(w.solution, shown = 2)
                Spacer(Modifier.height(14.dp)); SourceCard(d.src) { SourceLines(d.czech) }
            }
        }
        else -> {
            val w = workedNamed("Jednoduchá lineárna kongruencia")
            StepFrame(5, 12, {}, footer = { Cta("Pokračovať", {}) }) {
                Spacer(Modifier.height(4.dp)); Label("Riešený príklad"); Spacer(Modifier.height(2.dp)); H1(w.title); Spacer(Modifier.height(10.dp))
                SourceCard(w.src ?: "Zadanie") { Rich(w.problem, style = body, color = c.ink) }
                Spacer(Modifier.height(12.dp)); SolutionView(w.solution)
                Spacer(Modifier.height(14.dp))
                // SetsConcepts.kt (units of "zobrazenie") and Induction.kt:91, verbatim
                SourceCard("Množiny a indukcia") {
                    Rich("""Vzor §f^{-1}[N]=\{a\in A\mid f(a)\in N\}§ pre §N\subseteq B§""", style = body, color = c.ink)
                    Spacer(Modifier.height(6.dp))
                    Rich("""Dokazujem §V(n)§: §\sum_{k=1}^{n}(2k-1)=n^2§ pre všetky §n\in\mathbb{N}§, slabou indukciou.""", style = body, color = c.ink)
                    Spacer(Modifier.height(10.dp))
                    TexBlock("""\sum_{k=1}^{n+1}(2k-1)=\sum_{k=1}^{n}(2k-1)+(2n+1)""", size = 18.sp, color = c.ink)
                }
            }
        }
    }
}

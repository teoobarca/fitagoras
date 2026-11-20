package dev.hamper.tabula.snap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.data.AllLessons
import dev.hamper.tabula.data.Step
import dev.hamper.tabula.screens.GemWalkView
import dev.hamper.tabula.screens.LocalGemStep
import dev.hamper.tabula.ui.T
import org.junit.Test

/**
 * Every step of the GEM component on the real lesson screen ("Krokuj GEM", lesson LN step 11) and a GEM chain inside a
 * worked example: `./gradlew :app:snap -Psnap.test=GemShots -Psnap.theme=light,dark -Psnap.out=/tmp/gem`.
 */
class GemShots : Shots() {
    @Test fun krokuj() {
        for (k in 0..4) shoot("krokuj-gem-$k") { CompositionLocalProvider(LocalGemStep provides k) { AppAt("lesson:ln:10") } }
    }

    @Test fun workedChain() {
        val walk = AllLessons.first { it.id == "ln" }.steps.filterIsInstance<Step.Worked>().first { it.title.startsWith("Overenie LN") }
            .solution.steps.flatMap { it.math }.firstNotNullOf { it.gem }
        for (k in 0..walk.size) shoot("worked-ln-z5-$k") {
            CompositionLocalProvider(LocalGemStep provides k) {
                Box(Modifier.fillMaxWidth().background(T.c.bg).padding(16.dp)) { Box(Modifier.background(T.c.goodTint).padding(12.dp)) { GemWalkView(walk) } }
            }
        }
    }

    /** A walk too wide to sit beside its operations at 320 dp (Příklady 4.6 a 4.12): the operations above the matrix. */
    @Test fun workedWide() {
        val walk = AllLessons.first { it.id == "la1-k4-riesenie" }.steps.filterIsInstance<Step.Worked>()
            .flatMap { w -> w.solution.steps.flatMap { it.math } }.firstNotNullOf { it.gem }
        for (k in 0..walk.size) shoot("worked-wide-$k") {
            CompositionLocalProvider(LocalGemStep provides k) {
                Box(Modifier.fillMaxWidth().background(T.c.bg).padding(16.dp)) { Box(Modifier.background(T.c.goodTint).padding(12.dp)) { GemWalkView(walk) } }
            }
        }
    }
}

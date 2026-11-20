package dev.hamper.tabula.snap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.data.dml.numbers.euclidWalk
import dev.hamper.tabula.data.dml.numbers.rea
import dev.hamper.tabula.data.dml.numbers.reaWalk
import dev.hamper.tabula.la.TableWalk
import dev.hamper.tabula.screens.LocalGemStep
import dev.hamper.tabula.screens.TableWalkView
import dev.hamper.tabula.screens.tableStageSteps
import dev.hamper.tabula.ui.T
import org.junit.Test

/**
 * Euclid and the REA step by step, as in a goal card of a worked example:
 * `./gradlew :app:snap -Psnap.test=TableShots -Psnap.width=320 -Psnap.theme=light,dark -Psnap.out=/tmp/t`.
 */
class TableShots : Shots() {
    private fun walk(name: String, t: TableWalk, ks: List<Int>) {
        for (k in ks) shoot("$name-$k") {
            CompositionLocalProvider(LocalGemStep provides k) {
                Box(Modifier.fillMaxWidth().background(T.c.bg).padding(16.dp)) {
                    Box(Modifier.fillMaxWidth().background(T.c.goodTint, RoundedCornerShape(16.dp)).padding(12.dp)) { TableWalkView(t) }
                }
            }
        }
    }

    @Test fun euclid() { val t = euclidWalk(rea(254, 158)); walk("euklid-254-158", t, listOf(0, 1, 4, tableStageSteps(t))) }
    @Test fun rea254() { val t = reaWalk(rea(254, 158)); walk("rea-254-158", t, listOf(0, 1, 6, tableStageSteps(t))) }
    /** The generated problem of Teo's screenshot (-116 and 32): the table works with |a|, the result ends in the problem's signs. */
    @Test fun rea116() {
        val t = dev.hamper.tabula.data.dml.tcRea(dev.hamper.tabula.data.dml.TcReaTask(-116, 32)).solution(emptyList()).steps.flatMap { it.math }.firstNotNullOf { it.table }
        walk("rea-116-32", t, listOf(0, 2, tableStageSteps(t)))
    }
}

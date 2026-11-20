package dev.hamper.tabula.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Display formulas never get smaller than the floor: they wrap, and a row still too wide scrolls in its own box. */
class DisplayPlanTest {
    @Test fun aLineThatFitsAtTheFloorIsShrunkAtMostThatMuch() {
        val plan = displayPlan(listOf("a"), 100f, 10f, { 120f }) { listOf(120f) }
        assertEquals(listOf(DisplayRow(0, null, 100f / 120f, false)), plan)
    }

    @Test fun aWiderLineWrapsIntoItsPieces() {
        val plan = displayPlan(listOf("a"), 100f, 10f, { 200f }) { listOf(60f, 60f, 60f) }
        assertEquals(listOf(listOf(0), listOf(1), listOf(2)), plan.map { it.pieces })
        assertTrue(plan.all { it.scale == 1f && !it.scroll })
    }

    @Test fun aPieceTooWideEvenAtTheFloorKeepsTheFloorAndScrolls() {
        // a matrix that cannot break: 4 times the room
        val plan = displayPlan(listOf("a"), 100f, 10f, { 400f }) { listOf(400f) }
        assertEquals(listOf(DisplayRow(0, listOf(0), displayMinScale, true)), plan)
    }

    @Test fun theFloorIsThreeQuarters() = assertEquals(.75f, displayMinScale)

    @Test fun noRowIsEverBelowTheFloor() {
        for (w in listOf(10f, 99f, 130f, 134f, 140f, 500f, 5000f)) for (pieces in listOf(listOf(w), listOf(w / 2, w / 2), listOf(w / 5, w / 5, w / 5, w / 5, w / 5))) {
            val plan = displayPlan(listOf("a", "b"), 100f, 12f, { w }) { pieces }
            assertTrue("$w $pieces: $plan", plan.all { it.scale >= displayMinScale && it.scale <= 1f })
            assertFalse(plan.any { it.scroll && it.scale != displayMinScale })
        }
    }
}

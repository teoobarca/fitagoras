package dev.hamper.tabula.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Every assessment sits right after the last lesson it covers and before the first lesson after it that it doesn't. */
class PathTest {
    @Test fun each_milestone_follows_exactly_its_scope() {
        for (c in Courses) {
            val items = pathItems(c)
            val milestones = items.withIndex().filter { it.value is PathItem.Milestone }
            assertEquals("${c.id}: one milestone per exam the app prepares for", c.exams.count { it.prepares }, milestones.size)
            for ((at, m) in milestones) {
                val e = (m as PathItem.Milestone).exam
                val lessons = items.withIndex().filter { it.value is PathItem.Lesson }.map { it.index to (it.value as PathItem.Lesson).lesson }
                val scope = lessons.filter { c.forExam(it.second, e) }
                if (scope.isEmpty()) { assertEquals("${e.id} without lessons goes last", items.lastIndex, items.indexOfLast { it is PathItem.Milestone }.coerceAtLeast(at)); continue }
                assertTrue("${e.id}: every lesson of its scope comes before it", scope.all { it.first < at })
                // between its last scope lesson and the milestone: only that lesson's checkpoint or another milestone
                val last = scope.last().first
                assertTrue("${e.id}: right after its last lesson", items.subList(last + 1, at).all { it is PathItem.Check || it is PathItem.Milestone })
                // the next lesson after the milestone is outside its scope
                lessons.firstOrNull { it.first > at }?.let { assertTrue("${e.id}: ${it.second.id} after the milestone belongs to it", !c.forExam(it.second, e)) }
            }
        }
    }

    @Test fun la1_has_the_2nd_test_after_chapter_2_and_the_exam_after_chapter_4() {
        val items = pathItems(La1)
        val test2 = items.indexOfFirst { it is PathItem.Milestone && it.exam.id == "la1-test2" }
        val exam = items.indexOfFirst { it is PathItem.Milestone && it.exam.id == "la1-skuska" }
        val lastCh2 = items.indexOfLast { it is PathItem.Lesson && it.lesson.id in La1Lessons.map { l -> l.id } }
        val firstCh3 = items.indexOfFirst { it is PathItem.Lesson && it.lesson.id.startsWith("la1-k3") }
        assertTrue("2. test after chapter 2 ($lastCh2 < $test2 < $firstCh3)", test2 in (lastCh2 + 1) until firstCh3)
        assertTrue("the exam after chapter 4", exam > items.indexOfLast { it is PathItem.Lesson && it.lesson.id.startsWith("la1-k4") })
    }
}

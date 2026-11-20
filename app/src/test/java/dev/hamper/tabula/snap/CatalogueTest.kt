package dev.hamper.tabula.snap

import dev.hamper.tabula.data.Courses
import org.junit.After
import org.junit.Test
import java.io.File

/**
 * `./gradlew :app:snapCatalogue [-Psnap.out=<folder>] [-Psnap.only=dml-mz] [-Psnap.steps=2]`: for a look over everything
 * at once. `types/`: the first step of each step type; `<course>/<lesson>-<step>`: the first steps of every lesson
 * (before its lecture too). Light and dark at the Pixel's width unless -Psnap.theme/-Psnap.width/-Psnap.font say otherwise.
 */
class CatalogueTest : Shots() {
    private val looks = Snap.looks(theme = "light,dark")
    private val out = File(Snap.get("out") ?: "build/snap/catalogue").absoluteFile
    private val only = Snap.get("only")
    private val steps = Snap.get("steps")?.toInt() ?: 2
    private val failed = mutableListOf<String>()

    /** One broken screen must not hide the rest: render everything, then fail with the list. */
    private fun safely(name: String, dir: File, route: String) =
        try { shoot(name, looks, dir) { AppAt(route) } } catch (e: Throwable) { failed += "$name: $e"; System.err.println("snapCatalogue: $name failed: $e") }

    @After
    fun report() { if (failed.isNotEmpty()) throw AssertionError("${failed.size} screens failed:\n" + failed.joinToString("\n")) }

    @Test
    fun stepTypes() {
        val seen = mutableSetOf<String>()
        for (c in Courses) for (l in c.lessons) l.steps.forEachIndexed { i, s ->
            val type = s::class.simpleName!!
            if ((only == null || l.id.contains(only)) && seen.add(type)) safely("$type-${l.id}-$i", File(out, "types"), "lesson:${l.id}:$i")
        }
        println("snapCatalogue: step types ${seen.sorted()} in ${File(out, "types")}")
    }

    @Test
    fun lessonStarts() {
        for (c in Courses) for (l in c.lessons) if (only == null || l.id.contains(only))
            for (i in 0 until minOf(steps, l.steps.size)) safely("${l.id}-$i", File(out, c.id), "lesson:${l.id}:$i")
        println("snapCatalogue: lesson starts in $out")
    }
}

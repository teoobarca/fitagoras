package dev.hamper.tabula.repo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Things that name the package by hand follow the applicationId (launcher shortcuts break silently otherwise). */
class AppIdTest {
    private fun file(p: String) = listOf(File(p), File("app/$p"), File("../$p"), File("../app/$p")).first { it.exists() }

    @Test fun shortcuts_target_the_app() {
        val gradle = file("build.gradle.kts").readText().let { if ("applicationId" in it) it else file("app/build.gradle.kts").readText() }
        val id = Regex("""applicationId = "([^"]+)"""").find(gradle)!!.groupValues[1]
        assertEquals("dev.hamper.fitest", id)
        val targets = Regex("""targetPackage="([^"]+)"""").findAll(file("src/main/res/xml/shortcuts.xml").readText()).map { it.groupValues[1] }.toList()
        assertTrue(targets.isNotEmpty())
        targets.forEach { assertEquals("shortcut targetPackage", id, it) }
    }
}

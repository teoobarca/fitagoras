package dev.hamper.tabula.snap

import org.junit.Test

/**
 * `./gradlew :app:snap -Psnap.route=<route>[,<route>...] [-Psnap.course=dml] [-Psnap.theme=light,dark]
 * [-Psnap.width=pixel,360] [-Psnap.font=1.0,1.3] [-Psnap.height=2000] [-Psnap.out=<folder>]`
 */
class SnapTest : Shots() {
    @Test
    fun route() {
        val routes = Snap.get("route")?.split(",")?.map { it.trim() } ?: throw AssertionError("-Psnap.route=<route> is missing (docs/SYSTEM.md)")
        for (r in routes) shoot(r.replace(Regex("[^A-Za-z0-9._-]+"), "_")) { AppAt(r, Snap.get("course")) }
    }
}

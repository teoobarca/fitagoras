package dev.hamper.tabula.snap

import dev.hamper.tabula.SkriptaItem
import dev.hamper.tabula.SkriptaItems
import org.junit.Test

/**
 * The specimen set as the app sets it, next to the MathJax goldens (sadzba/zlate): `./gradlew :app:snap
 * -Psnap.test=SkriptaShots [-Psnap.theme=light,dark] -Psnap.out=<folder>`; one PNG per item.
 */
class SkriptaShots : Shots() {
    @Test fun items() {
        for ((name, _) in SkriptaItems) steady("app-$name") { SkriptaItem(name) }
    }
}

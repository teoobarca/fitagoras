package dev.hamper.tabula.snap

import dev.hamper.tabula.Specimen
import org.junit.Test

/**
 * The app's math on the four specimen pages of the debug SadzbaActivity (lessons, skriptá cards, DML):
 * `./gradlew :app:snap -Psnap.test=SadzbaShots [-Psnap.theme=light,dark] [-Psnap.font=1.0,2.0] -Psnap.out=<folder>`
 */
class SadzbaShots : Shots() {
    @Test fun specimen() {
        for (p in 1..4) shoot("p$p") { Specimen(p) }
    }
}

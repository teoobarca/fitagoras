package dev.hamper.tabula.snap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.data.LessonById
import dev.hamper.tabula.screens.HeroArt
import dev.hamper.tabula.screens.Motif
import dev.hamper.tabula.screens.MotifTile
import dev.hamper.tabula.ui.Mood
import dev.hamper.tabula.ui.Pytagoras
import dev.hamper.tabula.ui.T
import org.junit.Test

/**
 * Sheets of the pictures, for a look at them side by side: `-Psnap.test=ArtShots`. The character sheet of Pytagoras
 * (every pose, on both sheets, large and small) and every motif of the small pictures with three hero cards.
 */
class ArtShots : Shots() {
    private val names = mapOf(Mood.Pleased to "správne", Mood.Thinking to "nesprávne", Mood.Pondering to "premýšľa", Mood.Celebrating to "koniec lekcie", Mood.Greeting to "prázdny stav")

    @Test fun pytagoras() = shoot("pytagoras", Snap.looks(width = "640").map { it.copy(heightDp = 560) }) {
        val c = T.c
        Column(Modifier.fillMaxSize().background(c.bg).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Pytagoras · list postavy", style = MaterialTheme.typography.titleLarge, color = c.ink)
            for ((label, bg, sheet) in listOf(Triple("na liste „Správne.“", c.art.mascotGood, c.goodTint), Triple("na liste „Tentoraz nie.“", c.art.mascotBad, c.badTint))) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = c.ink2)
                Row(Modifier.fillMaxWidth().background(sheet).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (m in Mood.entries) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // the sizes he is used at: 64 dp on a feedback sheet, 48 and 56 in small places
                        Pytagoras(m, bg = bg, size = 64.dp)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) { Pytagoras(m, bg = bg, size = 48.dp); Spacer(Modifier.width(6.dp)); Pytagoras(m, bg = bg, size = 32.dp) }
                        Text(names.getValue(m), style = MaterialTheme.typography.labelMedium, color = c.ink2)
                    }
                }
            }
        }
    }

    @Test fun motifs() = shoot("ilustracie", Snap.looks(width = "640").map { it.copy(heightDp = 980) }) {
        val c = T.c
        Column(Modifier.fillMaxSize().background(c.bg).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Malé obrázky (cvičenia, cesta)", style = MaterialTheme.typography.titleLarge, color = c.ink)
            Motif.entries.chunked(7).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (m in row) Column(Modifier.width(76.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        MotifTile(m, size = 60.dp)
                        Text(m.name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = c.ink2)
                    }
                }
            }
            Text("Hlavná karta Dnes", style = MaterialTheme.typography.titleLarge, color = c.ink)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (id in listOf("dml-tc-kongruencie", "dml-mi-silna", "dml-mz-injsurj")) HeroArt(LessonById.getValue(id), Modifier.weight(1f).height(128.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (id in listOf("la1-k3-hodnost", "obal", "la1-k4-variety")) HeroArt(LessonById.getValue(id), Modifier.weight(1f).height(128.dp))
            }
        }
    }
}

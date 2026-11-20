package dev.hamper.tabula.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import dev.hamper.tabula.ui.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.system.Release
import dev.hamper.tabula.system.Telemetry
import dev.hamper.tabula.system.UpdateCore
import dev.hamper.tabula.system.Updater
import dev.hamper.tabula.system.UpdatePhase as Phase
import dev.hamper.tabula.platform.LocalPlatform
import dev.hamper.tabula.ui.*
import dev.hamper.tabula.res.*

/**
 * "Nová verzia 1.3.0": what is new, Aktualizovať / Neskôr. Shown on the home screens only (App.kt), never in a lesson
 * or a running mock test. While downloading it can't be swiped away; Zrušiť stops the download.
 */
@Composable
fun UpdateSheet(store: Store, r: Release, onClose: () -> Unit) {
    // statistics: offered once a version (Tracker remembers which)
    androidx.compose.runtime.LaunchedEffect(r.versionName) { Telemetry.update("offered", r.versionName) }
    val c = T.c
    val platform = LocalPlatform.current
    val phase = Updater.phase
    val busy = phase is Phase.Downloading
    val later = { Updater.later(store); onClose() }
    androidx.compose.runtime.DisposableEffect(Unit) { Updater.shown = true; onDispose { Updater.shown = false } }
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { it != SheetValue.Hidden || Updater.phase !is Phase.Downloading })
    ModalBottomSheet({ if (!busy) later() }, sheetState = state, containerColor = c.card, contentColor = c.ink, sheetMaxWidth = AppWidth) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp).navigationBarsPadding().animateContentSize()) {
            Label(listOf("Aktualizácia", UpdateCore.day(r.date), UpdateCore.megabytes(r.size)).filter { it.isNotEmpty() }.joinToString(", "))
            Spacer(Modifier.height(2.dp))
            Text("Nová verzia ${r.versionName}", style = MaterialTheme.typography.headlineMedium, color = c.ink, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(8.dp))
            // what changed since this app's version: every newer version's notes, newest first (UpdateCore.changes)
            val changes = androidx.compose.runtime.remember(r) { UpdateCore.changes(r, Updater.current()) }
            if (changes.isNotEmpty()) {
                Column(Modifier.heightIn(max = 260.dp).verticalScroll(rememberScrollState())) {
                    changes.forEachIndexed { i, v ->
                        // the newest version alone needs no heading (the title names it); otherwise version and day
                        // also one older version alone, so its notes never read as the newest one's
                        if (changes.size > 1 || v.versionCode != r.versionCode) {
                            if (i > 0) Spacer(Modifier.height(8.dp))
                            Text(listOf(v.versionName, UpdateCore.day(v.date)).filter { it.isNotEmpty() }.joinToString(" · "), style = MaterialTheme.typography.titleSmall, color = c.ink2, modifier = Modifier.padding(bottom = 4.dp).semantics { heading() })
                        }
                        v.notes.lines().filter { it.isNotBlank() }.forEach { line ->
                            Row(Modifier.padding(bottom = 4.dp)) {
                                Text("•", style = MaterialTheme.typography.bodyLarge, color = c.ink3, modifier = Modifier.width(16.dp))
                                dev.hamper.tabula.math.Rich(line.trim().removePrefix("- ").removePrefix("• "), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = c.ink)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
            Text("Tvoj postup v appke ostane.", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
            Spacer(Modifier.height(16.dp))
            when (phase) {
                is Phase.Downloading -> {
                    val f = if (phase.total > 0) (phase.done.toFloat() / phase.total).coerceIn(0f, 1f) else 0f
                    val shown by animateFloatAsState(f, tween(200), label = "download")
                    Text("Sťahujem, ${(f * 100).toInt()} %", style = MaterialTheme.typography.titleSmall, color = c.ink)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(c.line)
                            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(f, 0f..1f) },
                    ) { Box(Modifier.fillMaxHeight().fillMaxWidth(shown).clip(RoundedCornerShape(4.dp)).background(c.lesson.main)) }
                    Spacer(Modifier.height(16.dp))
                    Second("Zrušiť", { Updater.cancel() })
                }
                is Phase.NeedsPermission -> {
                    Text(
                        "Android musí FITagorasu raz dovoliť inštalovať aplikácie: zapni to na ďalšej obrazovke a vráť sa, inštalácia pokračuje sama.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = c.ink,
                    )
                    Spacer(Modifier.height(14.dp))
                    Cta("Otvoriť nastavenia", { platform.openInstallPermission() }, hue = c.lesson)
                    Spacer(Modifier.height(10.dp))
                    Second("Neskôr", later)
                }
                else -> {
                    if (phase is Phase.Failed) {
                        Text(phase.message, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = c.bad)
                        Spacer(Modifier.height(12.dp))
                    }
                    ButtonRow {
                        Second("Neskôr", later)
                        Cta(if (phase is Phase.Failed) "Skúsiť znova" else "Aktualizovať", { Telemetry.update("accepted", r.versionName); platform.installUpdate(r) }, icon = Res.drawable.ic_download, hue = c.lesson)
                    }
                }
            }
        }
    }
}

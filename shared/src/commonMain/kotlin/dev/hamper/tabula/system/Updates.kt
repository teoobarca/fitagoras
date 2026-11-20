package dev.hamper.tabula.system

import dev.hamper.tabula.compat.File
import dev.hamper.tabula.state.Store

/** Where the in-app update is (the screens show it; Android's Updater moves it along). */
sealed interface UpdatePhase {
    data object Idle : UpdatePhase
    data object Checking : UpdatePhase
    data object UpToDate : UpdatePhase
    data class Downloading(val done: Long, val total: Long) : UpdatePhase
    /** Downloaded and verified, waiting for the installer (or for the permission to use it). */
    data class Ready(val file: File) : UpdatePhase
    /** Android has not allowed this app to install apps yet: the student turns it on in Settings. */
    data class NeedsPermission(val file: File) : UpdatePhase
    /** Handed to the system installer; a second tap opens it again (a dismissed dialog may not answer). */
    data class Installing(val file: File) : UpdatePhase
    data class Failed(val message: String) : UpdatePhase
}

/**
 * The in-app update as the screens see it (Android: shared/src/androidMain/.../system/Updater.kt; checking, downloading
 * and installing go through Platform). The desktop and the web have none.
 */
expect object Updater {
    var phase: UpdatePhase
    /** The sheet asked for from Settings ("Skontrolovať teraz" found a newer build). */
    var asked: Boolean
    /** The sheet is on screen. */
    var shown: Boolean
    fun current(): Int
    /** "Neskôr": that version stays hidden for three days. */
    fun later(store: Store)
    fun cancel()
}

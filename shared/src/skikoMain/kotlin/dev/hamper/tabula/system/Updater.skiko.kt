package dev.hamper.tabula.system

import dev.hamper.tabula.state.Store

/** No in-app update on the desktop or the web. */
actual object Updater {
    actual var phase: UpdatePhase = UpdatePhase.Idle
    actual var asked: Boolean = false
    actual var shown: Boolean = false
    actual fun current(): Int = 0
    actual fun later(store: Store) { asked = false }
    actual fun cancel() { phase = UpdatePhase.Idle }
}

package dev.hamper.tabula.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.system.Release

/**
 * What the common screens ask of the device beyond drawing: the home-screen widget and the daily reminder (Android:
 * app/src/main/java/dev/hamper/tabula/AndroidPlatform.kt). The defaults do nothing, for the desktop and the web.
 */
interface Platform {
    /**
     * The browser (wasmJsMain/web): no vibration, reminder, widget, in-app update, statistics, error reports or lecture
     * clips, so Settings and the screens leave them out; what the web has instead comes from [SettingsRows].
     */
    val isWeb: Boolean get() = false

    /** Settings rows only this device has (the web: where progress lives, its backup, "Pridať na plochu"). */
    @Composable
    fun SettingsRows(store: Store) {}

    /** Redraws the home-screen widget with the current progress. */
    fun refreshWidget() {}

    /** Asks the launcher to pin the widget (where the launcher supports it). */
    fun pinWidget() {}

    /** Whether the reminder notification may be shown (the permission is granted). */
    fun remindersAllowed(): Boolean = false

    /** Schedules the daily reminder. */
    fun scheduleReminder() {}

    /** A launcher for the notification permission; [onResult] gets the answer. */
    @Composable
    fun rememberReminderPermission(onResult: (Boolean) -> Unit): () -> Unit = { onResult(false) }

    /** Opens a web page in the browser. */
    fun openUrl(url: String) {}

    /** Asks the server for a newer release (Android's in-app update; [manual]: "Skontrolovať teraz"). */
    fun checkUpdate(store: Store, manual: Boolean) {}

    /** "Aktualizovať": downloads [release] and hands it to the installer. */
    fun installUpdate(release: Release) {}

    /** Opens the system setting that lets this app install updates. */
    fun openInstallPermission() {}
}

val LocalPlatform = staticCompositionLocalOf<Platform> { object : Platform {} }

/** Files shipped with the app (composeResources/files): clips and the coverage register. */
object Assets {
    /** Reads a file by its path under files/, e.g. "clips.json" or "coverage/skills.json". */
    var read: (String) -> String = { error("Assets.read not installed") }
}

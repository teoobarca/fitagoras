package dev.hamper.tabula

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import dev.hamper.tabula.platform.Platform
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.system.Notifications
import dev.hamper.tabula.system.TabulaWidgetReceiver
import dev.hamper.tabula.system.Telemetry
import dev.hamper.tabula.system.Release
import dev.hamper.tabula.system.Updater

/** The widget, the reminder, links and the in-app update for the common screens. [ctx]: MainActivity. */
class AndroidPlatform(private val ctx: Context) : Platform {
    override fun refreshWidget() = TabulaWidgetReceiver.refresh(ctx)

    override fun pinWidget() {
        val m = AppWidgetManager.getInstance(ctx)
        if (m.isRequestPinAppWidgetSupported) m.requestPinAppWidget(ComponentName(ctx, TabulaWidgetReceiver::class.java), null, null)
    }

    override fun remindersAllowed(): Boolean =
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    override fun scheduleReminder() = Notifications.reminder(ctx)

    @Composable
    override fun rememberReminderPermission(onResult: (Boolean) -> Unit): () -> Unit {
        val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onResult)
        return { ask.launch(Manifest.permission.POST_NOTIFICATIONS) }
    }

    override fun openUrl(url: String) { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } }

    override fun checkUpdate(store: Store, manual: Boolean) = Updater.check(ctx, store, manual)

    override fun installUpdate(release: Release) { (ctx as? Activity)?.let { Updater.update(it, release) } }

    override fun openInstallPermission() = ctx.startActivity(Updater.permissionIntent(ctx))
}

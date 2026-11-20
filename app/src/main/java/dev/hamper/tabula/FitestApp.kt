package dev.hamper.tabula

import android.app.Application
import dev.hamper.tabula.platform.AndroidContext

/**
 * Runs first in every process start (activity, widget, notification, WorkManager): hands the shared code the build
 * values and the context it reads assets and fonts through.
 */
class FitestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Env.install(
            debug = BuildConfig.DEBUG, clipsGuestToken = BuildConfig.CLIPS_GUEST_TOKEN, versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE, applicationId = BuildConfig.APPLICATION_ID, updateUrl = BuildConfig.UPDATE_URL,
        )
        AndroidContext.install(this)
    }
}

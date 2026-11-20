package dev.hamper.tabula

/**
 * Build values common code reads (the Android app's BuildConfig belongs to the app module). The app sets them in
 * FitestApp.onCreate, before any activity, widget, worker or receiver runs; the desktop and the web at start.
 */
object Env {
    var DEBUG: Boolean = false
        private set
    /** The guest key for lecture clips (secrets.properties at build time); empty: clips are read as captions. */
    var CLIPS_GUEST_TOKEN: String = ""
        private set
    var VERSION_NAME: String = ""
        private set
    var VERSION_CODE: Int = 0
        private set
    var APPLICATION_ID: String = ""
        private set
    /** Where the in-app update asks for the latest release (Android). */
    var UPDATE_URL: String = ""
        private set

    fun install(debug: Boolean, clipsGuestToken: String, versionName: String, versionCode: Int = 0, applicationId: String = "", updateUrl: String = "") {
        DEBUG = debug; CLIPS_GUEST_TOKEN = clipsGuestToken; VERSION_NAME = versionName
        VERSION_CODE = versionCode; APPLICATION_ID = applicationId; UPDATE_URL = updateUrl
    }
}

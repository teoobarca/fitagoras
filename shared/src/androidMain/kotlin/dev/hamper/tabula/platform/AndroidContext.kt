package dev.hamper.tabula.platform

import android.content.Context
import android.content.res.AssetManager
import dev.hamper.tabula.res.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** The application context, installed by FitestApp.onCreate before anything else runs. */
object AndroidContext {
    lateinit var app: Context
        private set

    fun install(c: Context) {
        if (!::app.isInitialized) app = c.applicationContext
        Assets.read = { path -> app.assets.open(assetPath("files/$path")).bufferedReader().use { it.readText() } }
    }

    val assets: AssetManager get() = app.assets

    /** Where Compose resources sit among the APK's assets ("composeResources/<package>/" + [path]). */
    @OptIn(ExperimentalResourceApi::class)
    fun assetPath(path: String): String = Res.getUri(path).removePrefix("file:///android_asset/")
}

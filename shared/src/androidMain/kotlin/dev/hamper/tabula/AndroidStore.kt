package dev.hamper.tabula

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.hamper.tabula.platform.AndroidContext
import dev.hamper.tabula.state.StateFile
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.system.Telemetry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// The progress document: the DataStore file and key every FITest release has used, so an update keeps everything.
private val Context.data by preferencesDataStore("tabula")
private val KEY = stringPreferencesKey("state")

private class DataStoreStateFile(private val ctx: Context) : StateFile {
    override fun read(): String? = runBlocking { ctx.data.data.first()[KEY] }
    override suspend fun write(doc: String) { ctx.data.edit { it[KEY] = doc } }
}

/** The store of the process, as before: the activity, the widget and notifications read the same state. */
fun Tabula.store(ctx: Context): Store {
    AndroidContext.install(ctx)
    return store { DataStoreStateFile(ctx.applicationContext) }.also { Telemetry.init(ctx) }
}

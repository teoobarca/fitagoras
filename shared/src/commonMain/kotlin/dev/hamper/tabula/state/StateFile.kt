package dev.hamper.tabula.state

/**
 * Where the progress document lives. Android: the DataStore file "tabula", key "state", exactly as before, so an
 * update keeps everything (app/src/main/java/dev/hamper/tabula/AndroidPlatform.kt); the desktop: a file; the web:
 * localStorage.
 */
interface StateFile {
    /** The stored document, or null; called once, at start, and may block briefly. */
    fun read(): String?
    suspend fun write(doc: String)
}

/** Keeps the document in memory: tests, screenshots. */
class MemoryStateFile(private var doc: String? = null) : StateFile {
    override fun read() = doc
    override suspend fun write(doc: String) { this.doc = doc }
}

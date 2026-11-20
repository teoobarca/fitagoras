package dev.hamper.tabula.tutor.harness

import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.tutor.siwc.Trezor
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions

/**
 * The harness's credentials on the Mac: one JSON file readable only by its owner (0600) in a 0700 folder outside any
 * repo (default ~/.fitagoras). Written to a 0600 temp file first and moved over atomically, so a crash never
 * leaves half a record and the token is never world-readable even for a moment.
 */
class SuborovyTrezor(private val dir: File = File(System.getProperty("user.home"), ".fitagoras")) : Trezor {
    private val file = File(dir, "trezor.json")
    private val lock = Any()

    private fun pripravPriecinok() {
        if (!dir.exists()) Files.createDirectories(dir.toPath(), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")))
        Files.setPosixFilePermissions(dir.toPath(), PosixFilePermissions.fromString("rwx------"))
    }

    override fun nacitaj(): JSONObject = synchronized(lock) {
        if (!file.exists()) JSONObject() else JSONObject(file.readText())
    }

    override fun uloz(data: JSONObject) = synchronized(lock) {
        pripravPriecinok()
        val tmp = Files.createTempFile(dir.toPath(), "trezor", ".tmp", PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")))
        Files.writeString(tmp, data.toString())
        Files.move(tmp, file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        Unit
    }

    override fun uprav(zmena: (JSONObject) -> Unit) = vylucne { synchronized(lock) { val d = nacitaj(); zmena(d); uloz(d) } }

    override fun zmaz() = vylucne { synchronized(lock) { file.delete(); Unit } }

    /** This thread holds the file lock already (refresh calls uprav inside it): don't take it twice. */
    private val drzi = ThreadLocal.withInitial { false }

    /**
     * Every read-change-write across processes: two harness runs at once must not refresh with the same token (the
     * second would be rejected as reused) nor write back an old record over a rotated one.
     */
    override fun <T> vylucne(f: () -> T): T {
        if (drzi.get()) return f()
        pripravPriecinok()
        java.io.RandomAccessFile(File(dir, "trezor.lock"), "rw").use { raf ->
            raf.channel.lock().use {
                drzi.set(true)
                try { return f() } finally { drzi.set(false) }
            }
        }
    }
}

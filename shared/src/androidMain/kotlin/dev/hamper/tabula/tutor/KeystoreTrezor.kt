package dev.hamper.tabula.tutor

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.tutor.siwc.Trezor
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * The phone's ChatGPT credentials: one JSON record, AES-256-GCM under a non-exportable Android Keystore key, in
 * noBackupFilesDir (never in a backup or a cloud restore), written atomically. From the prototype that passed on
 * Teo's Pixel. Nothing read here is logged.
 */
class KeystoreTrezor(context: Context) : Trezor {
    private val file = File(context.noBackupFilesDir, "tutor-siwc.bin")
    private val alias = "fitagoras-siwc"

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build()
        )
        return gen.generateKey()
    }

    @Synchronized
    override fun nacitaj(): JSONObject {
        if (!file.exists()) return JSONObject()
        return try {
            val bytes = file.readBytes()
            val c = Cipher.getInstance("AES/GCM/NoPadding")
            c.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            JSONObject(String(c.doFinal(bytes.copyOfRange(12, bytes.size))))
        } catch (e: javax.crypto.AEADBadTagException) { gone() } catch (e: android.security.keystore.KeyPermanentlyInvalidatedException) { gone() }
        catch (e: java.security.UnrecoverableKeyException) { gone() }
    }

    /** The key is gone (a restore to another phone, a cleared Keystore): the record can never be read again. */
    private fun gone(): JSONObject {
        file.delete(); return JSONObject()
    }

    @Synchronized
    override fun uloz(data: JSONObject) {
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, key())
        val tmp = File(file.parentFile, "tutor-siwc.bin.tmp")
        tmp.writeBytes(c.iv + c.doFinal(data.toString().toByteArray()))
        if (!tmp.renameTo(file)) error("atomic rename failed")
    }

    @Synchronized
    override fun uprav(zmena: (JSONObject) -> Unit) { val d = nacitaj(); zmena(d); uloz(d) }

    @Synchronized
    override fun zmaz() { file.delete() }
}

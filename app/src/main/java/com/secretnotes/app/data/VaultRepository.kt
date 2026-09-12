package com.secretnotes.app.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class VaultRepository(context: Context) {
    private val filesDir = context.filesDir
    private val metaFile = File(filesDir, "vault.meta")
    private val dataFile = File(filesDir, "vault.bin")
    private val prefs = context.getSharedPreferences("vault_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    val isInitialized: Boolean
        get() = metaFile.exists() && dataFile.exists()

    var autoLockSeconds: Int
        get() = prefs.getInt(KEY_AUTO_LOCK, 15)
        set(value) {
            prefs.edit().putInt(KEY_AUTO_LOCK, value).apply()
        }

    fun createVault(pin: CharArray): SecretKey {
        val salt = VaultCrypto.randomBytes(VaultCrypto.SALT_BYTES)
        val kek = VaultCrypto.deriveKey(pin, salt)
        val master = SecretKeySpec(VaultCrypto.randomBytes(32), "AES")
        val wrapped = VaultCrypto.encrypt(kek, master.encoded)
        metaFile.writeBytes(salt + wrapped)
        saveNotes(master, emptyList())
        return master
    }

    fun unlock(pin: CharArray): SecretKey? {
        if (!isInitialized) return null
        return try {
            val meta = metaFile.readBytes()
            if (meta.size <= VaultCrypto.SALT_BYTES) return null
            val salt = meta.copyOfRange(0, VaultCrypto.SALT_BYTES)
            val wrapped = meta.copyOfRange(VaultCrypto.SALT_BYTES, meta.size)
            val kek = VaultCrypto.deriveKey(pin, salt)
            val rawMaster = VaultCrypto.decrypt(kek, wrapped)
            SecretKeySpec(rawMaster, "AES")
        } catch (_: Exception) {
            null
        }
    }

    fun changePin(oldPin: CharArray, newPin: CharArray, currentMaster: SecretKey): Boolean {
        val check = unlock(oldPin) ?: return false
        VaultCrypto.wipe(check.encoded)
        val salt = VaultCrypto.randomBytes(VaultCrypto.SALT_BYTES)
        val kek = VaultCrypto.deriveKey(newPin, salt)
        val wrapped = VaultCrypto.encrypt(kek, currentMaster.encoded)
        metaFile.writeBytes(salt + wrapped)
        return true
    }

    fun loadNotes(master: SecretKey): List<Note> {
        val blob = dataFile.readBytes()
        val plain = VaultCrypto.decrypt(master, blob)
        return try {
            json.decodeFromString(VaultPayload.serializer(), plain.decodeToString()).notes
        } finally {
            VaultCrypto.wipe(plain)
        }
    }

    fun saveNotes(master: SecretKey, notes: List<Note>) {
        val payload = json.encodeToString(VaultPayload.serializer(), VaultPayload(notes)).toByteArray()
        try {
            val blob = VaultCrypto.encrypt(master, payload)
            val tmp = File(filesDir, "vault.bin.tmp")
            tmp.writeBytes(blob)
            if (!tmp.renameTo(dataFile)) {
                tmp.copyTo(dataFile, overwrite = true)
                tmp.delete()
            }
        } finally {
            VaultCrypto.wipe(payload)
        }
    }

    fun destroyVault() {
        metaFile.delete()
        dataFile.delete()
        File(filesDir, "vault.bin.tmp").delete()
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_AUTO_LOCK = "auto_lock_seconds"
    }
}

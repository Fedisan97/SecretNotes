package com.secretnotes.app.vault

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.secretnotes.app.data.Note
import com.secretnotes.app.data.VaultCrypto
import com.secretnotes.app.data.VaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.crypto.SecretKey

enum class GateMode { Setup, Confirm, Unlock }

data class VaultUiState(
    val ready: Boolean = false,
    val initialized: Boolean = false,
    val unlocked: Boolean = false,
    val gateMode: GateMode = GateMode.Setup,
    val pinLength: Int = 0,
    val pinError: String? = null,
    val busy: Boolean = false,
    val notes: List<Note> = emptyList(),
    val autoLockSeconds: Int = 15,
    val toast: String? = null
)

class VaultViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = VaultRepository(application)
    private var masterKey: SecretKey? = null
    private var pendingSetupPin: CharArray? = null
    private var entered = CharArray(0)
    private var idleJob: Job? = null

    private val _state = MutableStateFlow(VaultUiState())
    val state: StateFlow<VaultUiState> = _state

    init {
        val initialized = repo.isInitialized
        _state.value = VaultUiState(
            ready = true,
            initialized = initialized,
            gateMode = if (initialized) GateMode.Unlock else GateMode.Setup,
            autoLockSeconds = repo.autoLockSeconds
        )
    }

    fun onDigit(digit: Char) {
        if (_state.value.busy) return
        if (entered.size >= PIN_LEN) return
        entered += digit
        _state.update { it.copy(pinLength = entered.size, pinError = null) }
        if (entered.size == PIN_LEN) {
            submitPin()
        }
    }

    fun onBackspace() {
        if (entered.isEmpty()) return
        entered = entered.copyOfRange(0, entered.size - 1)
        _state.update { it.copy(pinLength = entered.size, pinError = null) }
    }

    fun consumeToast() {
        _state.update { it.copy(toast = null) }
    }

    fun lock() {
        idleJob?.cancel()
        masterKey = null
        VaultCrypto.wipe(entered)
        entered = CharArray(0)
        VaultCrypto.wipe(pendingSetupPin)
        pendingSetupPin = null
        _state.update {
            it.copy(
                unlocked = false,
                notes = emptyList(),
                pinLength = 0,
                pinError = null,
                gateMode = if (it.initialized) GateMode.Unlock else GateMode.Setup,
                busy = false
            )
        }
    }

    fun onAppBackgrounded() {
        if (_state.value.unlocked) lock()
    }

    fun bumpIdle() {
        if (!_state.value.unlocked) return
        idleJob?.cancel()
        val seconds = _state.value.autoLockSeconds
        if (seconds <= 0) return
        idleJob = viewModelScope.launch {
            delay(seconds * 1000L)
            lock()
        }
    }

    fun setAutoLockSeconds(seconds: Int) {
        repo.autoLockSeconds = seconds
        _state.update { it.copy(autoLockSeconds = seconds) }
        bumpIdle()
    }

    fun saveNote(id: String, title: String, body: String) {
        val key = masterKey ?: return
        if (title.isBlank() && body.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val current = _state.value.notes.toMutableList()
            val index = current.indexOfFirst { it.id == id }
            if (index >= 0) {
                current[index] = current[index].copy(
                    title = title.trim(),
                    body = body,
                    updatedAt = now
                )
            } else {
                current.add(
                    0,
                    Note(id = id, title = title.trim(), body = body, createdAt = now, updatedAt = now)
                )
            }
            persist(key, current)
        }
    }

    fun deleteNote(id: String) {
        val key = masterKey ?: return
        viewModelScope.launch {
            persist(key, _state.value.notes.filterNot { it.id == id })
        }
    }

    fun changePin(oldPin: String, newPin: String, onDone: (Boolean) -> Unit) {
        val key = masterKey ?: return onDone(false)
        viewModelScope.launch {
            val ok = withContext(Dispatchers.Default) {
                val old = oldPin.toCharArray()
                val neu = newPin.toCharArray()
                try {
                    repo.changePin(old, neu, key)
                } finally {
                    VaultCrypto.wipe(old)
                    VaultCrypto.wipe(neu)
                }
            }
            onDone(ok)
            if (ok) _state.update { it.copy(toast = "PIN обновлён") }
        }
    }

    fun destroyVault() {
        repo.destroyVault()
        lock()
        _state.update {
            it.copy(
                initialized = false,
                gateMode = GateMode.Setup,
                toast = "Сейф уничтожен"
            )
        }
    }

    private fun submitPin() {
        when (_state.value.gateMode) {
            GateMode.Setup -> {
                pendingSetupPin = entered.copyOf()
                VaultCrypto.wipe(entered)
                entered = CharArray(0)
                _state.update {
                    it.copy(
                        gateMode = GateMode.Confirm,
                        pinLength = 0,
                        pinError = null
                    )
                }
            }
            GateMode.Confirm -> {
                val first = pendingSetupPin
                if (first == null || !first.contentEquals(entered)) {
                    VaultCrypto.wipe(entered)
                    VaultCrypto.wipe(pendingSetupPin)
                    entered = CharArray(0)
                    pendingSetupPin = null
                    _state.update {
                        it.copy(
                            gateMode = GateMode.Setup,
                            pinLength = 0,
                            pinError = "PIN не совпал. Задайте заново."
                        )
                    }
                } else {
                    _state.update { it.copy(busy = true) }
                    viewModelScope.launch {
                        val key = withContext(Dispatchers.Default) {
                            repo.createVault(entered)
                        }
                        masterKey = key
                        VaultCrypto.wipe(entered)
                        VaultCrypto.wipe(pendingSetupPin)
                        entered = CharArray(0)
                        pendingSetupPin = null
                        _state.update {
                            it.copy(
                                busy = false,
                                initialized = true,
                                unlocked = true,
                                notes = emptyList(),
                                pinLength = 0,
                                pinError = null
                            )
                        }
                        bumpIdle()
                    }
                }
            }
            GateMode.Unlock -> {
                _state.update { it.copy(busy = true, pinError = null) }
                val attempt = entered.copyOf()
                VaultCrypto.wipe(entered)
                entered = CharArray(0)
                viewModelScope.launch {
                    val key = withContext(Dispatchers.Default) { repo.unlock(attempt) }
                    VaultCrypto.wipe(attempt)
                    if (key == null) {
                        _state.update {
                            it.copy(
                                busy = false,
                                pinLength = 0,
                                pinError = "Неверный PIN"
                            )
                        }
                    } else {
                        val notes = withContext(Dispatchers.Default) { repo.loadNotes(key) }
                        masterKey = key
                        _state.update {
                            it.copy(
                                busy = false,
                                unlocked = true,
                                notes = notes.sortedByDescending { n -> n.updatedAt },
                                pinLength = 0,
                                pinError = null
                            )
                        }
                        bumpIdle()
                    }
                }
            }
        }
    }

    private suspend fun persist(key: SecretKey, notes: List<Note>) {
        val ordered = notes.sortedByDescending { it.updatedAt }
        withContext(Dispatchers.Default) { repo.saveNotes(key, ordered) }
        _state.update { it.copy(notes = ordered) }
    }

    override fun onCleared() {
        lock()
        super.onCleared()
    }

    companion object {
        const val PIN_LEN = 6
    }
}

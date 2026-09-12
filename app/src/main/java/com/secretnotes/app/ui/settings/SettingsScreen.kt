package com.secretnotes.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secretnotes.app.ui.components.VaultStage
import com.secretnotes.app.ui.theme.Gold
import com.secretnotes.app.ui.theme.InkSoft
import com.secretnotes.app.ui.theme.Parchment
import com.secretnotes.app.ui.theme.ParchmentMuted
import com.secretnotes.app.vault.VaultViewModel

@Composable
fun SettingsScreen(
    autoLockSeconds: Int,
    onAutoLock: (Int) -> Unit,
    onChangePin: (old: String, new: String, done: (Boolean) -> Unit) -> Unit,
    onDestroy: () -> Unit,
    onBack: () -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<String?>(null) }
    var destroy by remember { mutableStateOf(false) }
    val options = listOf(0 to "Сразу", 15 to "15 с", 30 to "30 с", 60 to "1 мин", 120 to "2 мин")
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Gold,
        unfocusedBorderColor = Gold.copy(alpha = 0.35f),
        focusedTextColor = Parchment,
        unfocusedTextColor = Parchment,
        cursorColor = Gold
    )

    VaultStage {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 28.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Назад", tint = Gold)
                }
                Text("ПРОТОКОЛ", color = Gold, letterSpacing = 4.sp, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("Автоблокировка", color = Parchment, fontSize = 16.sp)
            Text(
                "Сейф закрывается в фоне сразу. Таймер — ещё и при бездействии на экране.",
                color = ParchmentMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )
            options.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    row.forEach { (sec, label) ->
                        FilterChip(
                            selected = autoLockSeconds == sec,
                            onClick = { onAutoLock(sec) },
                            label = { Text(label) },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold,
                                selectedLabelColor = InkSoft,
                                containerColor = InkSoft,
                                labelColor = Parchment
                            )
                        )
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            Text("Сменить PIN", color = Parchment, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = oldPin,
                onValueChange = { if (it.length <= VaultViewModel.PIN_LEN && it.all(Char::isDigit)) oldPin = it },
                label = { Text("Старый PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newPin,
                onValueChange = { if (it.length <= VaultViewModel.PIN_LEN && it.all(Char::isDigit)) newPin = it },
                label = { Text("Новый PIN, 6 цифр") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(
                onClick = {
                    if (oldPin.length != 6 || newPin.length != 6) {
                        pinMessage = "Нужно по 6 цифр"
                    } else {
                        onChangePin(oldPin, newPin) { ok ->
                            pinMessage = if (ok) "PIN обновлён" else "Старый PIN неверный"
                            if (ok) {
                                oldPin = ""
                                newPin = ""
                            }
                        }
                    }
                }
            ) { Text("Сохранить PIN", color = Gold) }
            if (pinMessage != null) {
                Text(pinMessage!!, color = ParchmentMuted, fontSize = 13.sp)
            }
            Spacer(Modifier.height(32.dp))
            Text("Данные только на устройстве. Нет аккаунта и облака.", color = ParchmentMuted, fontSize = 13.sp)
            TextButton(onClick = { destroy = true }) {
                Text("Уничтожить сейф", color = androidx.compose.ui.graphics.Color(0xFFE07A73))
            }
        }
    }

    if (destroy) {
        AlertDialog(
            onDismissRequest = { destroy = false },
            title = { Text("Уничтожить всё?") },
            text = { Text("PIN и все заметки будут стёрты с телефона.") },
            confirmButton = {
                TextButton(onClick = {
                    destroy = false
                    onDestroy()
                }) { Text("Стереть", color = androidx.compose.ui.graphics.Color(0xFFE07A73)) }
            },
            dismissButton = {
                TextButton(onClick = { destroy = false }) { Text("Отмена") }
            },
            containerColor = androidx.compose.ui.graphics.Color(0xFF120E0B)
        )
    }
}

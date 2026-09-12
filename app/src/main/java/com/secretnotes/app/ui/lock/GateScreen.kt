package com.secretnotes.app.ui.lock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secretnotes.app.ui.components.PinPad
import com.secretnotes.app.ui.components.VaultStage
import com.secretnotes.app.ui.theme.Gold
import com.secretnotes.app.ui.theme.GoldDim
import com.secretnotes.app.ui.theme.Parchment
import com.secretnotes.app.ui.theme.ParchmentMuted
import com.secretnotes.app.vault.GateMode
import com.secretnotes.app.vault.VaultUiState
import com.secretnotes.app.vault.VaultViewModel

@Composable
fun GateScreen(
    state: VaultUiState,
    viewModel: VaultViewModel
) {
    val subtitle = when (state.gateMode) {
        GateMode.Setup -> "ЗАДАЙТЕ PIN — ШЕСТЬ ЦИФР"
        GateMode.Confirm -> "ПОВТОРИТЕ PIN"
        GateMode.Unlock -> "ВВЕДИТЕ PIN"
    }
    VaultStage {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(36.dp))
            Text("CLASSIFIED", color = GoldDim, letterSpacing = 8.sp, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "SECRET NOTES",
                color = Gold,
                letterSpacing = 8.sp,
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "ЛОКАЛЬНЫЙ СЕЙФ · БЕЗ СЕРВЕРА",
                color = ParchmentMuted,
                letterSpacing = 3.sp,
                fontSize = 10.sp
            )
            Spacer(Modifier.height(28.dp))
            Text(subtitle, color = Parchment, letterSpacing = 3.sp, fontSize = 12.sp)
            Spacer(Modifier.height(18.dp))
            if (state.busy) {
                CircularProgressIndicator(color = Gold, strokeWidth = 2.dp)
            } else {
                PinPad(
                    filled = state.pinLength,
                    error = state.pinError,
                    enabled = !state.busy,
                    onDigit = viewModel::onDigit,
                    onBackspace = viewModel::onBackspace
                )
            }
        }
    }
}

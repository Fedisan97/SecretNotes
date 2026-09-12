package com.secretnotes.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secretnotes.app.ui.theme.Gold
import com.secretnotes.app.ui.theme.InkSoft
import com.secretnotes.app.ui.theme.Parchment
import com.secretnotes.app.vault.VaultViewModel
import kotlin.math.roundToInt

@Composable
fun PinPad(
    filled: Int,
    error: String?,
    enabled: Boolean,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit
) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(error) {
        if (error != null) {
            shake.snapTo(0f)
            shake.animateTo(0f, spring(dampingRatio = 0.2f, stiffness = 600f))
            repeat(4) { i ->
                shake.animateTo(if (i % 2 == 0) 18f else -18f)
            }
            shake.animateTo(0f)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.offset { IntOffset(shake.value.roundToInt(), 0) }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(VaultViewModel.PIN_LEN) { index ->
                val on = index < filled
                Box(
                    Modifier
                        .size(14.dp)
                        .border(1.dp, Gold.copy(alpha = if (on) 1f else 0.45f), CircleShape)
                        .background(if (on) Gold else Color.Transparent, CircleShape)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = error.orEmpty(),
            color = Color(0xFFE07A73),
            fontSize = 13.sp,
            modifier = Modifier.height(20.dp)
        )
        Spacer(Modifier.height(12.dp))
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫")
        )
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(72.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (key) {
                            "" -> {}
                            "⌫" -> KeyFace(
                                enabled = enabled,
                                onClick = onBackspace
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Backspace,
                                    contentDescription = "Стереть",
                                    tint = Parchment
                                )
                            }
                            else -> KeyFace(
                                enabled = enabled,
                                onClick = { onDigit(key[0]) }
                            ) {
                                Text(
                                    key,
                                    color = Parchment,
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyFace(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .border(1.dp, Gold.copy(alpha = 0.35f), CircleShape)
            .background(InkSoft.copy(alpha = 0.7f))
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

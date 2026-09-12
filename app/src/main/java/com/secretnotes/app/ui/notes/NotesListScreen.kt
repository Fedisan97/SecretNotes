package com.secretnotes.app.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secretnotes.app.data.Note
import com.secretnotes.app.ui.components.VaultStage
import com.secretnotes.app.ui.theme.Gold
import com.secretnotes.app.ui.theme.GoldDim
import com.secretnotes.app.ui.theme.InkSoft
import com.secretnotes.app.ui.theme.Parchment
import com.secretnotes.app.ui.theme.ParchmentMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesListScreen(
    notes: List<Note>,
    onOpen: (Note) -> Unit,
    onCreate: () -> Unit,
    onLock: () -> Unit,
    onSettings: () -> Unit
) {
    val stamp = SimpleDateFormat("dd MMM · HH:mm", Locale.getDefault())
    VaultStage {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLock) {
                    Icon(Icons.Outlined.Lock, contentDescription = "Заблокировать", tint = Gold)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DOSSIER", color = GoldDim, letterSpacing = 6.sp, fontSize = 10.sp)
                    Text("Заметки", color = Gold, fontFamily = FontFamily.Serif, fontSize = 22.sp)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Настройки", tint = Gold)
                }
            }
            if (notes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ПУСТО", color = GoldDim, letterSpacing = 8.sp, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Сейф открыт.\nЗдесь будут только ваши записи.",
                            color = ParchmentMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Gold.copy(alpha = 0.28f), RoundedCornerShape(6.dp))
                                .background(InkSoft.copy(alpha = 0.85f))
                                .clickable { onOpen(note) }
                                .padding(16.dp)
                        ) {
                            Text(
                                note.title.ifBlank { "Без названия" },
                                color = Parchment,
                                fontFamily = FontFamily.Serif,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                note.body.ifBlank { "—" },
                                color = ParchmentMuted,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stamp.format(Date(note.updatedAt)),
                                color = GoldDim,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onCreate,
            containerColor = Gold,
            contentColor = androidx.compose.ui.graphics.Color(0xFF070504),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Новая заметка")
        }
    }
}

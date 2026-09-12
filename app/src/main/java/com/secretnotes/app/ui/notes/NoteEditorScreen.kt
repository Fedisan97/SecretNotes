package com.secretnotes.app.ui.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secretnotes.app.data.Note
import com.secretnotes.app.ui.components.VaultStage
import com.secretnotes.app.ui.theme.Gold
import com.secretnotes.app.ui.theme.Parchment
import com.secretnotes.app.ui.theme.ParchmentMuted
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun NoteEditorScreen(
    note: Note?,
    onSave: (id: String, title: String, body: String) -> Unit,
    onDelete: (id: String) -> Unit,
    onBack: () -> Unit
) {
    val noteId = rememberSaveable(note?.id) { note?.id ?: UUID.randomUUID().toString() }
    var title by rememberSaveable(noteId) { mutableStateOf(note?.title.orEmpty()) }
    var body by rememberSaveable(noteId) { mutableStateOf(note?.body.orEmpty()) }
    var confirmDelete by remember { mutableStateOf(false) }
    val exists = note != null

    LaunchedEffect(title, body) {
        delay(350)
        onSave(noteId, title, body)
    }

    VaultStage {
        Column(Modifier.fillMaxSize().padding(top = 28.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    onSave(noteId, title, body)
                    onBack()
                }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Назад", tint = Gold)
                }
                Text(
                    if (exists) "ДОСЬЕ" else "НОВАЯ ЗАПИСЬ",
                    color = Gold,
                    letterSpacing = 4.sp,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Удалить", tint = Gold)
                }
            }
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                cursorBrush = SolidColor(Gold),
                textStyle = TextStyle(
                    color = Parchment,
                    fontSize = 26.sp,
                    fontFamily = FontFamily.Serif
                ),
                decorationBox = { inner ->
                    if (title.isEmpty()) {
                        Text("Заголовок", color = ParchmentMuted, fontSize = 26.sp, fontFamily = FontFamily.Serif)
                    }
                    inner()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
            BasicTextField(
                value = body,
                onValueChange = { body = it },
                cursorBrush = SolidColor(Gold),
                textStyle = TextStyle(color = Parchment, fontSize = 16.sp, lineHeight = 24.sp),
                decorationBox = { inner ->
                    if (body.isEmpty()) {
                        Text("Только на этом устройстве…", color = ParchmentMuted, fontSize = 16.sp)
                    }
                    inner()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Сжечь запись?") },
            text = { Text("Заметка будет удалена с устройства без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete(noteId)
                    onBack()
                }) { Text("Удалить", color = Gold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Отмена") }
            },
            containerColor = Color(0xFF120E0B)
        )
    }
}

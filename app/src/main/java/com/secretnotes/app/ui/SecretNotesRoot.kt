package com.secretnotes.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.secretnotes.app.ui.lock.GateScreen
import com.secretnotes.app.ui.notes.NoteEditorScreen
import com.secretnotes.app.ui.notes.NotesListScreen
import com.secretnotes.app.ui.settings.SettingsScreen
import com.secretnotes.app.vault.VaultViewModel

@Composable
fun SecretNotesRoot(viewModel: VaultViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val processLifecycle = ProcessLifecycleOwner.get()

    DisposableEffect(processLifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppBackgrounded()
            }
        }
        processLifecycle.lifecycle.addObserver(observer)
        onDispose { processLifecycle.lifecycle.removeObserver(observer) }
    }

    val touchLock = Modifier
        .fillMaxSize()
        .pointerInput(state.unlocked) {
            awaitPointerEventScope {
                while (true) {
                    awaitPointerEvent(PointerEventPass.Initial)
                    viewModel.bumpIdle()
                }
            }
        }

    if (!state.ready) return

    if (!state.unlocked) {
        GateScreen(state, viewModel)
        return
    }

    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = "notes",
        modifier = touchLock
    ) {
        composable("notes") {
            NotesListScreen(
                notes = state.notes,
                onOpen = { nav.navigate("editor/${it.id}") },
                onCreate = { nav.navigate("editor/new") },
                onLock = viewModel::lock,
                onSettings = { nav.navigate("settings") }
            )
        }
        composable(
            "editor/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("noteId")
            val note = state.notes.firstOrNull { it.id == id }
            NoteEditorScreen(
                note = if (id == "new") null else note,
                onSave = viewModel::saveNote,
                onDelete = viewModel::deleteNote,
                onBack = { nav.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                autoLockSeconds = state.autoLockSeconds,
                onAutoLock = viewModel::setAutoLockSeconds,
                onChangePin = viewModel::changePin,
                onDestroy = {
                    viewModel.destroyVault()
                },
                onBack = { nav.popBackStack() }
            )
        }
    }
}

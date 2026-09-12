package com.secretnotes.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.secretnotes.app.ui.SecretNotesRoot
import com.secretnotes.app.ui.theme.SecretNotesTheme
import com.secretnotes.app.vault.VaultViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge()
        setContent {
            SecretNotesTheme {
                val viewModel: VaultViewModel = viewModel()
                SecretNotesRoot(viewModel)
            }
        }
    }
}

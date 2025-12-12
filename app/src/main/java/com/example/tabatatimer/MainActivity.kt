package com.example.tabatatimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Audio
        soundManager = SoundManager(this)

        // Initialize ViewModel with SoundManager
        val viewModel = ViewModelProvider(this)[TabataViewModel::class.java]
        viewModel.setSoundManager(soundManager)

        // Anonymous Login for simplicity (Or use UI to login)
        if (Firebase.auth.currentUser == null) {
            Firebase.auth.signInAnonymously()
        }

        setContent {
            TabataApp(viewModel = viewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
package com.example.kotlinmusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.kotlinmusic.ui.theme.KotlinMusicTheme
import com.google.android.exoplayer2.ExoPlayer

class DisplayMusicsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playlist = intent.getStringExtra("selected_playlist") ?: return
        val player = ExoPlayer.Builder(this).build()

        setContent {
            KotlinMusicTheme {
                DisplayMusics(player, playlist) {
                    finish() // This will navigate back to the previous screen
                }
            }
        }
    }
}
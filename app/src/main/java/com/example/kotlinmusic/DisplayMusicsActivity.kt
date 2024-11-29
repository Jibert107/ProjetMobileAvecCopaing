// DisplayMusicsActivity.kt
package com.example.kotlinmusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.kotlinmusic.ui.theme.KotlinMusicTheme
import com.google.android.exoplayer2.ExoPlayer

class DisplayMusicsActivity : ComponentActivity() {
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        val playlist = intent.getStringExtra("selected_playlist") ?: "Playlist 1"

        setContent {
            KotlinMusicTheme {
                DisplayMusics(player, playlist)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
package com.example.kotlinmusic

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun PlaylistScreen() {
    val context = LocalContext.current
    val playlists = listOf("Playlist 1", "Playlist 2", "Playlist 3")

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(playlists) { playlist ->
                    Text(
                        text = playlist,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(context, MainActivity::class.java).apply {
                                    putExtra("selected_playlist", playlist)
                                }
                                context.startActivity(intent)
                            }
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
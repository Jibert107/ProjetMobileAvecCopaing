package com.example.kotlinmusic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlaylistsScreen(onPlaylistClick: (String) -> Unit) {
    val playlists = listOf("Playlist 1", "Playlist 2", "Playlist 3")
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(playlists) { playlist ->
            Text(
                text = playlist,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlaylistClick(playlist) }
                    .padding(16.dp)
            )
        }
    }
}
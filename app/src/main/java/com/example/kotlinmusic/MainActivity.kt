package com.example.kotlinmusic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.kotlinmusic.ui.theme.KotlinMusicTheme
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.upstream.RawResourceDataSource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KotlinMusicTheme {
                MainScreen()
            }
        }
    }
}
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf(0) }
    val player = remember { ExoPlayer.Builder(context).build() }
    var currentTrack by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column {
                if (currentTrack.isNotEmpty()) {
                    MusicPlayerBar(player, currentTrack)
                }
                BottomNavigation {
                    BottomNavigationItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = selectedIndex == 0,
                        onClick = { selectedIndex = 0 }
                    )
                    BottomNavigationItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        selected = selectedIndex == 1,
                        onClick = { selectedIndex = 1 }
                    )
                    BottomNavigationItem(
                        icon = { Icon(Icons.Default.Menu, contentDescription = "Playlist") },
                        label = { Text("Playlist") },
                        selected = selectedIndex == 2,
                        onClick = { selectedIndex = 2 }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            when (selectedIndex) {
                0 -> HomePage()
                1 -> SearchScreen { track ->
                    currentTrack = track
                    val resourceName = track.lowercase().replace(' ', '_')
                    val uri = RawResourceDataSource.buildRawResourceUri(
                        context.resources.getIdentifier(resourceName, "raw", context.packageName)
                    )
                    player.setMediaItem(MediaItem.fromUri(uri))
                    player.prepare()
                    player.play()
                }
                2 -> PlaylistScreen { track ->
                    currentTrack = track
                    val resourceName = track.lowercase().replace(' ', '_')
                    val uri = RawResourceDataSource.buildRawResourceUri(
                        context.resources.getIdentifier(resourceName, "raw", context.packageName)
                    )
                    player.setMediaItem(MediaItem.fromUri(uri))
                    player.prepare()
                    player.play()
                }
            }
        }
    }
}

@Composable
fun HomePage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Home Page")
    }
}

@Composable
fun SearchScreen(onTrackSelected: (String) -> Unit) {
    val context = LocalContext.current
    val allRawResources = getAllResourceFileNames(context)
    var searchQuery by remember { mutableStateOf("") }
    val filteredResources = allRawResources.filter { it.contains(searchQuery.lowercase()) }

    Column {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(filteredResources) { file ->
                val displayName = file.replaceFirst("playlist1_", "").replaceFirst("playlist2_", "").replace('_', ' ').replaceFirstChar { it.uppercase() }
                Text(
                    text = displayName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTrackSelected(file) }
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun PlaylistScreen(onTrackSelected: (String) -> Unit) {
    val context = LocalContext.current
    val playlists = listOf("Playlist 1", "Playlist 2", "Playlist 3")

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(playlists) { playlist ->
            Text(
                text = playlist,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(context, DisplayMusicsActivity::class.java).apply {
                            putExtra("selected_playlist", playlist)
                        }
                        context.startActivity(intent)
                    }
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun MusicPlayerBar(player: ExoPlayer, currentTrack: String) {
    val isPlaying by remember { mutableStateOf(player.isPlaying) }
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentTrack,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }) {
            Icon(
                imageVector = if (player.isPlaying) Icons.Default.Add else Icons.Default.PlayArrow,
                contentDescription = if (player.isPlaying) "Pause" else "Play"
            )
        }
    }
}
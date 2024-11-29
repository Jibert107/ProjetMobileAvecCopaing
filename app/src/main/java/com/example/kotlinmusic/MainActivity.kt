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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
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
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            when (selectedIndex) {
                0 -> HomePage()
                1 -> SearchScreen()
                2 -> PlaylistScreen()
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
fun PlaylistScreen() {
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
fun SearchScreen() {
    val context = LocalContext.current
    val allRawResources = getAllResourceFileNames(context)
    var searchQuery by remember { mutableStateOf("") }
    val filteredResources = allRawResources.filter { it.contains(searchQuery.lowercase()) }
    var selectedFile by remember { mutableStateOf("Select a file") }
    val player = remember { ExoPlayer.Builder(context).build() }

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
                        .clickable {
                            selectedFile = file
                            val resourceName = file.lowercase().replace(' ', '_')
                            val uri = RawResourceDataSource.buildRawResourceUri(
                                context.resources.getIdentifier(resourceName, "raw", context.packageName)
                            )
                            player.setMediaItem(MediaItem.fromUri(uri))
                            player.prepare()
                            player.play()
                        }
                        .padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PlayerViewComposable(player)
    }
}
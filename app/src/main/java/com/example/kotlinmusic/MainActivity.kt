package com.example.kotlinmusic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

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
    var displayMusics by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf("") }
    var showFullPlayer by remember { mutableStateOf(false) }
    val player = remember { ExoPlayer.Builder(context).build() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column {
                MusicPlayerBar(player, modifier = Modifier.fillMaxWidth()) {
                    showFullPlayer = true
                }
                BottomNavigation {
                    BottomNavigationItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = selectedIndex == 0,
                        onClick = {
                            selectedIndex = 0
                            displayMusics = false
                        }
                    )
                    BottomNavigationItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        selected = selectedIndex == 1,
                        onClick = {
                            selectedIndex = 1
                            displayMusics = false
                        }
                    )
                    BottomNavigationItem(
                        icon = { Icon(Icons.Default.Menu, contentDescription = "Playlist") },
                        label = { Text("Playlist") },
                        selected = selectedIndex == 2,
                        onClick = {
                            selectedIndex = 2
                            displayMusics = false
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (showFullPlayer) {
                FullPlayerView(player) {
                    showFullPlayer = false
                }
            } else if (displayMusics) {
                DisplayMusics(player, selectedPlaylist) {
                    displayMusics = false
                }
            } else {
                when (selectedIndex) {
                    0 -> HomePage()
                    1 -> SearchScreen(player)
                    2 -> PlaylistScreen(player) { playlist ->
                        selectedPlaylist = playlist
                        displayMusics = true
                    }
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
fun SearchScreen(player: ExoPlayer) {
    val context = LocalContext.current
    val allRawResources = getAllResourceFileNames(context)
    var searchQuery by remember { mutableStateOf("") }
    val filteredResources = allRawResources.filter { it.contains(searchQuery.lowercase()) }
    var selectedFile by remember { mutableStateOf("Select a file") }

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
    }
}
@Composable
fun MusicPlayerBar(player: ExoPlayer, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("No music playing") }
    val albumArt: Painter = painterResource(id = R.drawable.default_album_art) // Replace with your default album art resource
    val duration = player.duration.takeIf { it > 0 } ?: 0L
    var position by remember { mutableStateOf(player.currentPosition) }

    LaunchedEffect(player) {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                title = mediaItem?.mediaMetadata?.title?.toString() ?: "No music playing"
            }
        })
        while (true) {
            position = player.currentPosition
            delay(1000L)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colors.surface)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = albumArt,
            contentDescription = "Album Art",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (duration > 0) {
                Slider(
                    value = position.toFloat(),
                    valueRange = 0f..duration.toFloat(),
                    onValueChange = { newPosition ->
                        player.seekTo(newPosition.toLong())
                        position = newPosition.toLong()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
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
        IconButton(onClick = {
            player.seekToNext()
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next"
            )
        }
    }
}

@Composable
fun FullPlayerView(player: ExoPlayer, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Now Playing") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Add your full player UI components here
            Text(text = "Full Player View")
            // Example: Display the PlayerView
            PlayerViewComposable(player, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun PlaylistScreen(player: ExoPlayer, onPlaylistSelected: (String) -> Unit) {
    val playlists = listOf("Playlist 1", "Playlist 2", "Playlist 3")

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(playlists) { playlist ->
            Text(
                text = playlist,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onPlaylistSelected(playlist)
                    }
                    .padding(16.dp)
            )
        }
    }
}
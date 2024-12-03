// app/src/main/java/com/example/kotlinmusic/MainActivity.kt
package com.example.kotlinmusic

import DeezerTrack
import DeezerWorker
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinmusic.ui.theme.KotlinMusicTheme
import com.example.kotlinmusic.ui.theme.SpotifyBlack
import com.example.kotlinmusic.ui.theme.SpotifyGray
import com.example.kotlinmusic.ui.theme.SpotifyGreen
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Log
import com.google.gson.Gson
import coil.compose.AsyncImage

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
    var currentTrack by remember { mutableStateOf<DeezerTrack?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column {
                currentTrack?.let {
                    MusicPlayerBar(player, it)
                }
                BottomNavigation(backgroundColor = SpotifyGreen) {
                    BottomNavigationItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        selected = selectedIndex == 0,
                        onClick = { selectedIndex = 0 }
                    )
                    BottomNavigationItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        selected = selectedIndex == 1,
                        onClick = { selectedIndex = 1 }
                    )
                    BottomNavigationItem(
                        icon = { Icon(Icons.Default.Build, contentDescription = "Settings") },
                        selected = selectedIndex == 2,
                        onClick = {
                            val intent = Intent(context, SettingsActivity::class.java)
                            context.startActivity(intent)
                        }
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
                    val uri = Uri.parse(track?.preview ?: "")
                    player.setMediaItem(MediaItem.fromUri(uri))
                    player.prepare()
                    player.play()
                }
                2 -> PlaylistScreen()
            }
        }
    }
}

@Composable
fun HomePage() {
    Box(modifier = Modifier.fillMaxSize().background(SpotifyBlack), contentAlignment = Alignment.Center) {
        Row(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.weight(3f).padding(16.dp)) { // Right part (3/4 of the screen)
                TaskItem("Mobile Android application in Kotlin (API > 35)", true)
                TaskItem("Request a permission at runtime (location, camera, . . . )", true)
                TaskItem("Usage of the WorkManager", true)
                TaskItem("UI using XML or Jetpack Compose", true)
                TaskItem("Activity navigation inside the app", true)
            }
            Spacer(modifier = Modifier.weight(1f))

        }
    }
}

@Composable
fun TaskItem(task: String, isCompleted: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = task,
            color = Color.White,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (isCompleted) "Completed" else "Not Completed",
            tint = if (isCompleted) Color.Green else Color.Red
        )
    }
}

@Composable
fun SearchScreen(onTrackSelected: (DeezerTrack) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<DeezerTrack?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(SpotifyBlack)) {
        TextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                if (query.isNotEmpty()) {
                    val workRequest = OneTimeWorkRequestBuilder<DeezerWorker>()
                        .setInputData(workDataOf("query" to query))
                        .build()
                    WorkManager.getInstance(context).enqueue(workRequest)
                    WorkManager.getInstance(context).getWorkInfoByIdLiveData(workRequest.id)
                        .observe(lifecycleOwner) { workInfo ->
                            if (workInfo != null && workInfo.state.isFinished) {
                                val trackJson = workInfo.outputData.getString("track")
                                searchResult = Gson().fromJson(trackJson, DeezerTrack::class.java)
                            }
                        }
                }
            },
            label = { Text("Search", color = Color.Black, fontWeight = FontWeight.Bold) },
            textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold),
            colors = TextFieldDefaults.textFieldColors(
                textColor = Color.Black,
                cursorColor = Color.Black,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                backgroundColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        searchResult?.let { track ->
            Text(
                text = "${track.title} by ${track.artist.name}",
                color = Color.White,
                modifier = Modifier.fillMaxWidth().clickable {
                    onTrackSelected(track)
                }.padding(16.dp)
            )
        }
    }
}

@Composable
fun PlaylistScreen() {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(SpotifyBlack), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Button(onClick = {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(text = "Go to Settings")
        }
    }
}

@Composable
fun MusicPlayerBar(player: ExoPlayer, currentTrack: DeezerTrack) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpotifyGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image placeholder for the music
            AsyncImage(
                model = currentTrack.album.cover,
                contentDescription = "Album cover",
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Title and artist
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentTrack.title,
                    style = MaterialTheme.typography.body1,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = currentTrack.artist.name,
                    style = MaterialTheme.typography.caption,
                    color = Color.White,
                    maxLines = 1
                )
            }

            // Play/Pause button
            IconButton(onClick = {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }) {
                Icon(
                    imageVector = if (player.isPlaying) Icons.Default.Add else Icons.Default.PlayArrow,
                    contentDescription = if (player.isPlaying) "Pause" else "Play",
                    tint = Color.White
                )
            }
        }

        // Progress bar (dummy or real)
        LinearProgressIndicator(
            progress = if (player.isPlaying) 0.5f else 0f, // Add real logic here
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )
    }

    // Add error listener
    player.addListener(object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            Log.e("PlaybackError", "ExoPlayer error: ${error.message}")
        }
    })
}
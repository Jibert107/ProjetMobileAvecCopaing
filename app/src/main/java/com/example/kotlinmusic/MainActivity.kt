package com.example.kotlinmusic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
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
import com.google.android.exoplayer2.upstream.RawResourceDataSource

import android.util.Size
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. You can perform the camera-related task.
            } else {
                // Permission is denied. Inform the user that the feature is unavailable.
            }
        }
        setContent {
            KotlinMusicTheme {
                var hasCameraPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                if (!hasCameraPermission) {
                    LaunchedEffect(Unit) {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }

                if (hasCameraPermission) {
                    MainScreen()
                } else {
                    // Show a message or a different screen if permission is denied
                    Text("Camera permission is required to use this feature.")
                }
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
                BottomNavigation (backgroundColor = SpotifyGreen){
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
    Box(modifier = Modifier.fillMaxSize().background(SpotifyBlack), contentAlignment = Alignment.Center) {
        Row(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.weight(3f).padding(16.dp)) { // Right part (3/4 of the screen)
                TaskItem("Mobile Android application in Kotlin (API > 35)", false)
                TaskItem("Request a permission at runtime (location, camera, . . . )", true)
                TaskItem("Usage of the WorkManager", false)
                TaskItem("UI using XML or Jetpack Compose", true)
                TaskItem("Activity navigation inside the app", false)
            }
            Spacer(modifier = Modifier.weight(1f))

        }
        CameraPreview()
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
fun SearchScreen(onTrackSelected: (String) -> Unit) {
    val context = LocalContext.current
    val allRawResources = getAllResourceFileNames(context)
    var searchQuery by remember { mutableStateOf("") }
    val filteredResources = allRawResources.filter { it.contains(searchQuery.lowercase()) }

    Column (modifier = Modifier
        .fillMaxSize()
        .background(SpotifyBlack)) {

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search", color = Color.Black, fontWeight = FontWeight.Bold) },
            textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold),
            colors = TextFieldDefaults.textFieldColors(
                textColor = Color.Black,
                cursorColor = Color.Black,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                backgroundColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(filteredResources) { file ->
                val displayName = file.replaceFirst("playlist1_", "").replaceFirst("playlist2_", "").replace('_', ' ').replaceFirstChar { it.uppercase() }
                Text(
                    text = displayName,
                    color = Color.White,
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
                    .background(SpotifyBlack)
            )
        }
    }
}

@Composable
fun MusicPlayerBar(player: ExoPlayer, currentTrack: String) {
    val isPlaying by remember { mutableStateOf(player.isPlaying) }


    val trackTitle = currentTrack
        .replaceFirst("playlist1", "")
        .replaceFirst("playlist2", "")
        .replaceFirst("playlist3", "")
        .replace('_', ' ')
        .replaceFirstChar { it.uppercase() }
    val trackArtist = "Unknown Artist"

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
            // Image placeholder pour la musique
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
            ) {
                Text(
                    text = "🎵", // Icône par défaut
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Titre et artiste
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trackTitle,
                    color = Color.White,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1
                )
                Text(
                    text = trackArtist,
                    style = MaterialTheme.typography.caption,
                    color = Color.White,
                    maxLines = 1
                )
            }

            // Bouton Play/Pause
            IconButton(onClick = {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }
        }

        // Barre de progression (dummy ou réelle)
        LinearProgressIndicator(
            progress = if (player.isPlaying) 0.5f else 0f, // Mettre une vraie logique ici
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )
    }
}


@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder()
            .setTargetResolution(Size(640, 360))
            .build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        preview.setSurfaceProvider(previewView.surfaceProvider)

        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview
        )

        onDispose {
            cameraProvider.unbindAll()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
            .width(200.dp)  // Set the desired width
            .height(150.dp) // Set the desired height
    )
}
// DisplayMusics.kt
package com.example.kotlinmusic

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.RawResourceDataSource

import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.BottomNavigation
import androidx.compose.ui.Alignment

@Composable
fun DisplayMusics(player: ExoPlayer, playlist: String) {
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
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { (context as? ComponentActivity)?.finish() }
            )

            when (selectedIndex) {
                0 -> HomePage()
                1 -> SearchPage()
                2 -> PlaylistPage(player, playlist)
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
fun SearchPage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Search Page")
    }
}

@Composable
fun PlaylistPage(player: ExoPlayer, playlist: String) {
    val context = LocalContext.current
    val rawResources = getRawResourceFileNames(context, playlist)
    var selectedFile by remember { mutableStateOf("Select a file") }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(rawResources) { file ->
            Text(
                text = file,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedFile = file
                        val uri = RawResourceDataSource.buildRawResourceUri(
                            context.resources.getIdentifier(file, "raw", context.packageName)
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

fun getRawResourceFileNames(context: Context, playlist: String): List<String> {
    val rawResources = mutableListOf<String>()
    val fields = R.raw::class.java.fields
    for (field in fields) {
        if (playlist == "Playlist 1") {
            rawResources.add(field.name)
        }
    }
    return rawResources
}

@Composable
fun PlayerViewComposable(player: ExoPlayer, modifier: Modifier = Modifier) {
    AndroidView(factory = { context ->
        PlayerView(context).apply {
            this.player = player
        }
    }, modifier = modifier)
}
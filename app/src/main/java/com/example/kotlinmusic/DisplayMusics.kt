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
import androidx.compose.ui.Alignment

@Composable
fun DisplayMusics(player: ExoPlayer, playlist: String) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { (context as? ComponentActivity)?.finish() }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            PlaylistPage(player, playlist)
        }
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
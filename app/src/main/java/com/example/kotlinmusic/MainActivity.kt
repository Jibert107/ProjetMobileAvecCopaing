package com.example.kotlinmusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.kotlinmusic.ui.theme.KotlinMusicTheme
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import android.content.Context

class MainActivity : ComponentActivity() {
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()

        setContent {
            KotlinMusicTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        var selectedFile by remember { mutableStateOf("Select a file") }
                        val context = LocalContext.current
                        val rawResources = getRawResourceFileNames(context)

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
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}

fun getRawResourceFileNames(context: Context): List<String> {
    val rawResources = mutableListOf<String>()
    val fields = R.raw::class.java.fields
    for (field in fields) {
        rawResources.add(field.name)
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

@Preview(showBackground = true)
@Composable
fun PlayerViewPreview() {
    val mockPlayer = ExoPlayer.Builder(LocalContext.current).build()
    KotlinMusicTheme {
        PlayerViewComposable(player = mockPlayer)
    }
}
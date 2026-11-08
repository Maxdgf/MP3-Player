package com.maxdgf.impulse.presentation.main_screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.maxdgf.impulse.R
import com.maxdgf.impulse.domain.model.Audio


@Composable
private fun AudioCard(
    title: String,
    artist: String,
    albumArtUri: Uri,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(75.dp)
            .fillMaxWidth()
            .background(color = if (isSelected) Color.Gray.copy(alpha = 0.3f) else Color.Unspecified)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val context = LocalContext.current
        val imageRequest = remember(albumArtUri) {
            return@remember ImageRequest.Builder(context)
                .size(150, 150)
                .data(albumArtUri)
                .build()
        }

        AsyncImage(
            model = imageRequest,
            modifier = Modifier
                .size(55.dp)
                .clip(shape = RoundedCornerShape(15.dp)),
            contentDescription = null,
            error = painterResource(R.drawable.outline_music_note_24),
            contentScale = ContentScale.Crop
        )

        Column {
            Text(
                text = title,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = artist,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun MiniPlayer(
    currentAudio: Audio,
    isPlaying: Boolean,
    isLoading: Boolean,
    onTogglePlayback: () -> Unit,
    onSeekToNext: () -> Unit,
    onSeekToPrevious: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val context = LocalContext.current
        val imageRequest = remember(currentAudio.albumArtUri) {
            return@remember ImageRequest.Builder(context)
                .size(100, 100)
                .data(currentAudio.albumArtUri)
                .build()
        }

        AsyncImage(
            model = imageRequest,
            modifier = Modifier
                .size(50.dp)
                .clip(shape = RoundedCornerShape(15.dp)),
            contentDescription = null,
            error = painterResource(R.drawable.outline_music_note_24),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = currentAudio.title,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = currentAudio.artist,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp
            )
        }

        IconButton(onClick = onSeekToPrevious) {
            Icon(
                painter = painterResource(R.drawable.baseline_skip_previous_24),
                contentDescription = null
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                strokeWidth = 2.dp
            )
        } else {
            IconButton(onClick = onTogglePlayback) {
                Icon(
                    painter = painterResource(
                        if (isPlaying) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24
                    ),
                    contentDescription = null
                )
            }
        }

        IconButton(onClick = onSeekToNext) {
            Icon(
                painter = painterResource(R.drawable.baseline_skip_next_24),
                contentDescription = null
            )
        }
    }
}

/** Creates main app screen. */
@Composable
fun MainScreen() {
    val mediaControllerViewModel = hiltViewModel<MediaControllerViewModel>()
    val currentAudio by mediaControllerViewModel.currentMediaItem.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar {
                currentAudio?.let { audio ->
                    val isPlaying by mediaControllerViewModel.isPlaying.collectAsState()
                    val isLoading by mediaControllerViewModel.isLoading.collectAsState()

                    MiniPlayer(
                        currentAudio = audio,
                        isPlaying = isPlaying,
                        isLoading = isLoading,
                        onTogglePlayback = mediaControllerViewModel::togglePlayback,
                        onSeekToNext = mediaControllerViewModel::seekToNext,
                        onSeekToPrevious = mediaControllerViewModel::seekToPrevious
                    )
                }
            }
        }
    ) { innerPadding ->
        val mainViewModel = hiltViewModel<MainViewModel>()
        val allAudios by mainViewModel.allAudiosList.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val audiosState = allAudios) {
                is AudiosListResult.Content -> {
                    LaunchedEffect(audiosState.list) {
                        mediaControllerViewModel.updateMediaItemsList(audiosState.list)
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(
                            items = audiosState.list,
                            key = { _, audio -> audio.id }
                        ) { index, audio ->
                            AudioCard(
                                title = audio.title,
                                artist = audio.artist,
                                albumArtUri = audio.albumArtUri,
                                isSelected = audio == currentAudio,
                                onClick = { mediaControllerViewModel.seekToMediaItemById(audio.id) }
                            )

                            if (index < audiosState.list.lastIndex)
                                HorizontalDivider()
                        }
                    }
                }
                AudiosListResult.Empty -> Text(text = "It looks like you don't have any audio files on your device yet :(")
                is AudiosListResult.Error -> Text(text = audiosState.message ?: "Unknown error")
                AudiosListResult.Loading -> CircularProgressIndicator()
            }
        }
    }
}
package com.maxdgf.impulse.presentation.main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.maxdgf.impulse.domain.model.Audio
import com.maxdgf.impulse.domain.repository.MediaControllerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MediaControllerViewModel @Inject constructor(private val mediaControllerRepository: MediaControllerRepository) : ViewModel() {
    val isPlaying = mediaControllerRepository.isPlaying
    val isLoading = mediaControllerRepository.isLoading

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMediaItem = mediaControllerRepository.currentMediaItem
        .filterNotNull()
        .mapLatest { mediaItem ->
            return@mapLatest Audio(
                id = mediaItem.mediaId,
                title = mediaItem.mediaMetadata.title!!.toString(),
                artist = mediaItem.mediaMetadata.artist!!.toString(),
                duration = mediaItem.mediaMetadata.durationMs!!,
                uri = mediaItem.localConfiguration?.uri!!,
                albumArtUri = mediaItem.mediaMetadata.artworkUri!!
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun togglePlayback() =
        mediaControllerRepository.togglePlayback()

    fun seekToNext() =
        mediaControllerRepository.seekToNext()

    fun seekToPrevious() =
        mediaControllerRepository.seekToPrevious()

    fun seekToMediaItemById(id: String) =
        mediaControllerRepository.seekToMediaItemById(id)

    fun updateMediaItemsList(audios: List<Audio>) = viewModelScope.launch {
        val mediaItemsList = withContext(Dispatchers.Default) {
            return@withContext audios.map { audio ->
                return@map audio.run {
                    return@run MediaItem.Builder()
                        .setMediaId(id)
                        .setUri(uri)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(title)
                                .setArtist(artist)
                                .setDurationMs(duration)
                                .setArtworkUri(albumArtUri)
                                .build()
                        )
                        .build()
                }
            }
        }

        mediaControllerRepository.updateMediaItems(mediaItemsList)
    }
}
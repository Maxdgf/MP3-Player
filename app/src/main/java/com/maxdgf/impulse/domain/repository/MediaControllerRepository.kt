package com.maxdgf.impulse.domain.repository

import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.StateFlow

interface MediaControllerRepository {
    val isPlaying: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>
    val currentMediaItem: StateFlow<MediaItem?>

    fun togglePlayback()
    fun seekToMediaItemById(id: String)
    fun updateMediaItems(items: List<MediaItem>)
    fun seekToNext()
    fun seekToPrevious()
}
package com.maxdgf.impulse.data.impl

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.maxdgf.impulse.data.MediaPlayerService
import com.maxdgf.impulse.domain.repository.MediaControllerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class MediaControllerRepositoryImpl @Inject constructor(@param:ApplicationContext private val context: Context) : MediaControllerRepository {
    private var _mediaController: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)

    private inner class PlayerEventsListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            _isLoading.value = isLoading
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentMediaItem.value = mediaItem
        }
    }

    init {
        val sessionToken = SessionToken(context, ComponentName(context, MediaPlayerService::class.java))
        val controllerFeature = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFeature.addListener(
            {
                _mediaController = controllerFeature.get().apply {
                    addListener(PlayerEventsListener())
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private var _mediaItemsIndicesMap: Map<String, Int> = emptyMap()
    private var _lastMediaItemsListHash = 0


    override val isPlaying: StateFlow<Boolean>
        get() = _isPlaying.asStateFlow()

    override val isLoading: StateFlow<Boolean>
        get() = _isLoading.asStateFlow()

    override val currentMediaItem: StateFlow<MediaItem?>
        get() = _currentMediaItem.asStateFlow()

    override fun togglePlayback() {
        _mediaController?.run {
            if (!_isPlaying.value) play() else pause() // play or pause
        }
    }

    override fun seekToMediaItemById(id: String) {
        _mediaItemsIndicesMap[id]?.let { index ->
            _mediaController?.seekTo(index, 0L)
        }
    }

    override fun updateMediaItems(items: List<MediaItem>) {
        val itemsHash = items.fold(1) { acc, item -> acc * 31 + item.mediaId.hashCode() }
        if (itemsHash != _lastMediaItemsListHash) _mediaController?.let { controller ->
            _mediaItemsIndicesMap = items
                .mapIndexed { index, item ->
                    return@mapIndexed item.mediaId to index
                }
                .toMap()

            val currentMediaItemId = controller.currentMediaItem?.mediaId
            if (currentMediaItemId != null) {
                val isPlaying = controller.isPlaying
                val positionMs = controller.currentPosition
                val newCurrentMediaItemIndex = items.indexOfFirst { it.mediaId == currentMediaItemId }

                if (newCurrentMediaItemIndex >= 0) controller.apply {
                    setMediaItems(items, newCurrentMediaItemIndex, positionMs)
                    if (isPlaying) play()
                } else controller.setMediaItems(items)
            } else controller.setMediaItems(items)

            _lastMediaItemsListHash = itemsHash
        }
    }

    override fun seekToNext() {
        _mediaController?.seekToNext()
    }

    override fun seekToPrevious() {
        _mediaController?.seekToPrevious()
    }
}
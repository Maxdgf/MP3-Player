package com.maxdgf.impulse.data

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MediaPlayerService : MediaSessionService() {
    private var _mediaSession: MediaSession? = null

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        _mediaSession

    override fun onCreate() {
        super.onCreate()

        val exoPlayer = ExoPlayer.Builder(this).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
        }

        _mediaSession = MediaSession.Builder(this, exoPlayer).build()
    }

    override fun onDestroy() {
        _mediaSession?.run {
            player.release()
            release()
            _mediaSession = null
        }
        super.onDestroy()
    }
}
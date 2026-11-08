package com.maxdgf.impulse.domain.model

import android.net.Uri

data class Audio(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: Uri,
    val albumArtUri: Uri
)
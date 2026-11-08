package com.maxdgf.impulse.presentation.main_screen

import com.maxdgf.impulse.domain.model.Audio

sealed class AudiosListResult {
    data class Content(val list: List<Audio>) : AudiosListResult()
    data class Error(val message: String?) : AudiosListResult()
    object Empty : AudiosListResult()
    object Loading : AudiosListResult()
}
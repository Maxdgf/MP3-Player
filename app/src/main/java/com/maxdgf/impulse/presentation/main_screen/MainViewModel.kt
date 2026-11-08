package com.maxdgf.impulse.presentation.main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxdgf.impulse.domain.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(audioRepository: AudioRepository) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val allAudiosList = audioRepository.allAudiosFromStorage
        .mapLatest { audios ->
            return@mapLatest if (audios.isNotEmpty())
                AudiosListResult.Content(audios)
            else
                AudiosListResult.Empty
        }
        .catch { exception ->
            emit(AudiosListResult.Error(exception.message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AudiosListResult.Loading
        )
}
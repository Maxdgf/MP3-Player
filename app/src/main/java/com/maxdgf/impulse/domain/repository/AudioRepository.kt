package com.maxdgf.impulse.domain.repository

import com.maxdgf.impulse.domain.model.Audio
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    val allAudiosFromStorage: Flow<List<Audio>>
}
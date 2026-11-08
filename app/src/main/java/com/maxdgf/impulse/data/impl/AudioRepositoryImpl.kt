package com.maxdgf.impulse.data.impl

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.maxdgf.impulse.domain.model.Audio
import com.maxdgf.impulse.domain.repository.AudioRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AudioRepositoryImpl @Inject constructor(@param:ApplicationContext private val context: Context) : AudioRepository {
    private companion object {
        const val EXTERNAL_AUDIO_PATH = "content://media/external/audio"
    }

    private val _contentResolver = context.contentResolver

    private fun getAllMusicFromStorage(): List<Audio> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val cursor = _contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        val allAudios = mutableListOf<Audio>()

        cursor?.use { cur ->
            if (cur.moveToFirst()) {
                val idIndex = cur.getColumnIndex(MediaStore.Audio.Media._ID)
                val displayNameIndex = cur.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val durationIndex = cur.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val artistIndex = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val titleIndex = cur.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val albumIdIndex = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                do {
                    val id = cur.getString(idIndex)
                    val displayName = cur.getString(displayNameIndex)
                    val duration = cur.getLong(durationIndex)
                    val artist = cur.getStringOrNull(artistIndex)
                    val title = cur.getStringOrNull(titleIndex)
                    val albumId = cur.getString(albumIdIndex)

                    val uri = Uri.withAppendedPath("$EXTERNAL_AUDIO_PATH/media".toUri(), id)
                    val albumArtUri = Uri.withAppendedPath("$EXTERNAL_AUDIO_PATH/albumart".toUri(), albumId)

                    allAudios.add(
                        Audio(
                            id = id,
                            title = title ?: displayName,
                            artist = artist ?: "unknown artist :(",
                            duration = duration,
                            uri = uri,
                            albumArtUri = albumArtUri
                        )
                    )
                } while (cur.moveToNext())
            }
        }

        return allAudios
    }

    private val _allAudiosFlow =
        callbackFlow {
            val contentObserver = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    trySend(getAllMusicFromStorage())
                }
            }

            _contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver
            )

            trySend(getAllMusicFromStorage())

            awaitClose {
                _contentResolver.unregisterContentObserver(contentObserver)
            }
        }
        .flowOn(Dispatchers.IO)

    override val allAudiosFromStorage: Flow<List<Audio>>
        get() = _allAudiosFlow
}
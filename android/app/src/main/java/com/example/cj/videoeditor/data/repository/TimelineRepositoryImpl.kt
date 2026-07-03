package com.example.cj.videoeditor.data.repository

import com.example.cj.videoeditor.domain.model.Clip
import com.example.cj.videoeditor.domain.model.Timeline
import com.example.cj.videoeditor.domain.model.Track
import com.example.cj.videoeditor.domain.repository.TimelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 时间线数据仓库实现（内存版本）
 *
 * MVP 阶段使用内存存储，后续可扩展为 Room + JSON 持久化。
 */
@Singleton
class TimelineRepositoryImpl @Inject constructor() : TimelineRepository {

    private val _timeline = MutableStateFlow(Timeline())

    override fun getTimeline(): Flow<Timeline> = _timeline.asStateFlow()

    override fun getCurrentTimeline(): Timeline = _timeline.value

    override suspend fun setTimeline(timeline: Timeline) {
        _timeline.value = timeline
    }

    override suspend fun addTrack(track: Track) {
        _timeline.update { current ->
            if (current.tracks.any { it.id == track.id }) current
            else current.copy(tracks = current.tracks + track)
        }
    }

    override suspend fun removeTrack(trackId: String) {
        _timeline.update { current ->
            current.copy(tracks = current.tracks.filter { it.id != trackId })
        }
    }

    override suspend fun addClip(clip: Clip) {
        _timeline.update { current ->
            val updatedTracks = current.tracks.map { track ->
                if (track.id == clip.trackId) {
                    updateTrackClips(track, track.clips + clip)
                } else track
            }
            current.copy(tracks = updatedTracks)
        }
    }

    override suspend fun updateClip(clip: Clip) {
        _timeline.update { current ->
            val updatedTracks = current.tracks.map { track ->
                if (track.clips.any { it.id == clip.id }) {
                    updateTrackClips(track, track.clips.map { if (it.id == clip.id) clip else it })
                } else track
            }
            current.copy(tracks = updatedTracks)
        }
    }

    override suspend fun removeClip(clipId: String) {
        _timeline.update { current ->
            val updatedTracks = current.tracks.map { track ->
                if (track.clips.any { it.id == clipId }) {
                    updateTrackClips(track, track.clips.filter { it.id != clipId })
                } else track
            }
            current.copy(tracks = updatedTracks)
        }
    }

    override suspend fun clear() {
        _timeline.value = Timeline()
    }

    private fun updateTrackClips(track: Track, newClips: List<Clip>): Track {
        return when (track) {
            is com.example.cj.videoeditor.domain.model.VideoTrack -> track.copy(clips = newClips)
            is com.example.cj.videoeditor.domain.model.PipTrack -> track.copy(clips = newClips)
            is com.example.cj.videoeditor.domain.model.AudioTrack -> track.copy(clips = newClips)
            is com.example.cj.videoeditor.domain.model.TextTrack -> track.copy(clips = newClips)
            is com.example.cj.videoeditor.domain.model.StickerTrack -> track.copy(clips = newClips)
        }
    }
}

package com.example.cj.videoeditor.domain.model

/**
 * 时间线模型
 *
 * 包含多条轨道，统一编排所有素材片段。
 */
data class Timeline(
    val tracks: List<Track> = emptyList(),
    val currentPositionUs: Long = 0L
) {
    /**
     * 时间线总时长（所有轨道最晚结束时间）
     */
    val durationUs: Long
        get() = tracks.maxOfOrNull { it.endTimeUs } ?: 0L

    /**
     * 获取指定类型的所有轨道
     */
    fun tracksByType(type: TrackType): List<Track> {
        return tracks.filter { it.type == type }
    }

    /**
     * 获取指定轨道的片段
     */
    fun clipsOnTrack(trackId: String): List<Clip> {
        return tracks.find { it.id == trackId }?.clips ?: emptyList()
    }

    /**
     * 查找指定时间点命中的所有片段
     */
    fun clipsAtTime(positionUs: Long): List<Clip> {
        return tracks.flatMap { track ->
            track.clips.filter { positionUs in it.startTimeUs until it.endTimeUs }
        }
    }

    /**
     * 获取视频主轨
     */
    fun mainVideoTrack(): VideoTrack? {
        return tracks.filterIsInstance<VideoTrack>().firstOrNull()
    }

    /**
     * 替换某条轨道
     */
    fun updateTrack(updatedTrack: Track): Timeline {
        return copy(tracks = tracks.map { if (it.id == updatedTrack.id) updatedTrack else it })
    }

    /**
     * 更新当前播放位置
     */
    fun seekTo(positionUs: Long): Timeline {
        val clamped = positionUs.coerceIn(0L, durationUs)
        return copy(currentPositionUs = clamped)
    }
}

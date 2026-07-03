package com.example.cj.videoeditor.domain.repository

import com.example.cj.videoeditor.domain.model.Clip
import com.example.cj.videoeditor.domain.model.Timeline
import com.example.cj.videoeditor.domain.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * 时间线数据仓库接口
 */
interface TimelineRepository {

    /**
     * 获取当前时间线（Flow）
     */
    fun getTimeline(): Flow<Timeline>

    /**
     * 获取当前时间线快照
     */
    fun getCurrentTimeline(): Timeline

    /**
     * 替换整个时间线
     */
    suspend fun setTimeline(timeline: Timeline)

    /**
     * 添加轨道
     */
    suspend fun addTrack(track: Track)

    /**
     * 删除轨道
     */
    suspend fun removeTrack(trackId: String)

    /**
     * 添加片段
     */
    suspend fun addClip(clip: Clip)

    /**
     * 更新片段
     */
    suspend fun updateClip(clip: Clip)

    /**
     * 删除片段
     */
    suspend fun removeClip(clipId: String)

    /**
     * 清空时间线
     */
    suspend fun clear()
}

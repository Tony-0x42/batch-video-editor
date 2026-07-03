package com.example.cj.videoeditor.domain.repository

import com.example.cj.videoeditor.ui.media.MediaItem
import com.example.cj.videoeditor.ui.media.MediaType

/**
 * 媒体资源仓库接口
 */
interface MediaRepository {

    /**
     * 加载设备上的视频列表
     */
    suspend fun loadVideos(): List<MediaItem>

    /**
     * 加载设备上的图片列表（预留）
     */
    suspend fun loadPhotos(): List<MediaItem>

    /**
     * 加载设备上的音频列表（预留）
     */
    suspend fun loadAudios(): List<MediaItem>

    /**
     * 按类型加载媒体
     */
    suspend fun loadByType(type: MediaType): List<MediaItem> {
        return when (type) {
            MediaType.VIDEO -> loadVideos()
            MediaType.PHOTO -> loadPhotos()
            MediaType.AUDIO -> loadAudios()
        }
    }
}

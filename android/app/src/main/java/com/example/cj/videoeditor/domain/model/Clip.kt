package com.example.cj.videoeditor.domain.model

import android.net.Uri

/**
 * 时间线片段基类
 *
 * 所有素材片段（视频、图片、音频、文字）都继承此类。
 * 时间统一使用微秒（us），避免浮点精度问题。
 */
sealed class Clip(
    open val id: String,
    open val trackId: String,
    /** 片段在时间线上的起始时间（微秒） */
    open val startTimeUs: Long,
    /** 片段在时间线上的结束时间（微秒） */
    open val endTimeUs: Long,
    /** 源素材起始时间（微秒） */
    open val sourceStartUs: Long,
    /** 源素材结束时间（微秒） */
    open val sourceEndUs: Long
) {
    /**
     * 片段在时间线上的时长
     */
    val timelineDurationUs: Long
        get() = endTimeUs - startTimeUs

    /**
     * 源素材有效时长
     */
    val sourceDurationUs: Long
        get() = sourceEndUs - sourceStartUs

    /**
     * 是否需要重新计算结束时间（子类可覆盖）
     */
    open fun recalculateEndTime(): Long = startTimeUs + sourceDurationUs
}

/**
 * 视频片段
 */
data class VideoClip(
    override val id: String,
    override val trackId: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    override val sourceStartUs: Long,
    override val sourceEndUs: Long,
    val uri: Uri,
    val speed: Float = 1.0f,
    val isReversed: Boolean = false,
    val transform: VideoTransform = VideoTransform(),
    val filterEffects: List<FilterEffectRef> = emptyList()
) : Clip(id, trackId, startTimeUs, endTimeUs, sourceStartUs, sourceEndUs) {

    override fun recalculateEndTime(): Long {
        return if (speed > 0) {
            startTimeUs + (sourceDurationUs / speed).toLong()
        } else {
            startTimeUs + sourceDurationUs
        }
    }
}

/**
 * 图片片段
 */
data class ImageClip(
    override val id: String,
    override val trackId: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    val uri: Uri,
    val transform: VideoTransform = VideoTransform()
) : Clip(id, trackId, startTimeUs, endTimeUs, 0L, endTimeUs - startTimeUs)

/**
 * 音频片段
 */
data class AudioClip(
    override val id: String,
    override val trackId: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    override val sourceStartUs: Long,
    override val sourceEndUs: Long,
    val uri: Uri,
    val volume: Float = 1.0f,
    val fadeInDurationUs: Long = 0L,
    val fadeOutDurationUs: Long = 0L
) : Clip(id, trackId, startTimeUs, endTimeUs, sourceStartUs, sourceEndUs)

/**
 * 文字片段
 */
data class TextClip(
    override val id: String,
    override val trackId: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    val text: String,
    val style: TextStyle = TextStyle(),
    val transform: VideoTransform = VideoTransform()
) : Clip(id, trackId, startTimeUs, endTimeUs, 0L, endTimeUs - startTimeUs)

/**
 * 视频变换参数
 */
data class VideoTransform(
    val x: Float = 0f,
    val y: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotation: Float = 0f,
    val alpha: Float = 1f
)

/**
 * 文字样式
 */
data class TextStyle(
    val fontFamily: String? = null,
    val fontSizeSp: Float = 16f,
    val textColor: Int = 0xFFFFFFFF.toInt(),
    val strokeColor: Int? = null,
    val strokeWidth: Float = 0f,
    val backgroundColor: Int? = null
)

/**
 * 滤镜效果引用
 */
data class FilterEffectRef(
    val filterId: String,
    val intensity: Float = 1.0f
)

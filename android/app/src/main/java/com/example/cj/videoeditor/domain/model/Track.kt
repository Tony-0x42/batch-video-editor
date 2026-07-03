package com.example.cj.videoeditor.domain.model

/**
 * 轨道类型
 */
enum class TrackType {
    VIDEO,      // 主视频轨
    PIP,        // 画中画轨
    AUDIO,      // 音频轨
    TEXT,       // 文字轨
    STICKER     // 贴纸轨
}

/**
 * 轨道抽象
 */
sealed class Track(
    open val id: String,
    open val type: TrackType,
    open val clips: List<Clip>,
    open val isLocked: Boolean = false,
    open val isMuted: Boolean = false,
    open val isVisible: Boolean = true
) {
    /**
     * 轨道结束时间（所有片段最晚结束）
     */
    val endTimeUs: Long
        get() = clips.maxOfOrNull { it.endTimeUs } ?: 0L
}

/**
 * 视频轨道
 */
data class VideoTrack(
    override val id: String,
    override val clips: List<Clip> = emptyList(),
    override val isLocked: Boolean = false,
    override val isMuted: Boolean = false,
    override val isVisible: Boolean = true
) : Track(id, TrackType.VIDEO, clips, isLocked, isMuted, isVisible)

/**
 * 画中画轨道
 */
data class PipTrack(
    override val id: String,
    override val clips: List<Clip> = emptyList(),
    override val isLocked: Boolean = false,
    override val isMuted: Boolean = false,
    override val isVisible: Boolean = true
) : Track(id, TrackType.PIP, clips, isLocked, isMuted, isVisible)

/**
 * 音频轨道
 */
data class AudioTrack(
    override val id: String,
    override val clips: List<Clip> = emptyList(),
    override val isLocked: Boolean = false,
    override val isMuted: Boolean = false,
    override val isVisible: Boolean = true
) : Track(id, TrackType.AUDIO, clips, isLocked, isMuted, isVisible)

/**
 * 文字轨道
 */
data class TextTrack(
    override val id: String,
    override val clips: List<Clip> = emptyList(),
    override val isLocked: Boolean = false,
    override val isMuted: Boolean = false,
    override val isVisible: Boolean = true
) : Track(id, TrackType.TEXT, clips, isLocked, isMuted, isVisible)

/**
 * 贴纸轨道
 */
data class StickerTrack(
    override val id: String,
    override val clips: List<Clip> = emptyList(),
    override val isLocked: Boolean = false,
    override val isMuted: Boolean = false,
    override val isVisible: Boolean = true
) : Track(id, TrackType.STICKER, clips, isLocked, isMuted, isVisible)

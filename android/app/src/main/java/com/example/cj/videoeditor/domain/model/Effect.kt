package com.example.cj.videoeditor.domain.model

/**
 * 效果基类
 *
 * 所有可视化效果（滤镜、转场、动画）都继承此类。
 */
sealed class Effect(
    open val id: String,
    open val startTimeUs: Long,
    open val endTimeUs: Long
)

/**
 * 滤镜效果
 */
data class FilterEffect(
    override val id: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    val filterId: String,
    val intensity: Float = 1.0f
) : Effect(id, startTimeUs, endTimeUs)

/**
 * 转场效果
 */
data class TransitionEffect(
    override val id: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    val transitionId: String,
    val fromClipId: String,
    val toClipId: String
) : Effect(id, startTimeUs, endTimeUs)

/**
 * 关键帧
 */
data class Keyframe<T>(
    val timeUs: Long,
    val value: T,
    val interpolator: InterpolatorType = InterpolatorType.LINEAR
)

enum class InterpolatorType {
    LINEAR,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT
}

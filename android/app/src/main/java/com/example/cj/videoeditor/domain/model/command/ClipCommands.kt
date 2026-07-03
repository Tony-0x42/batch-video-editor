package com.example.cj.videoeditor.domain.model.command

import com.example.cj.videoeditor.domain.model.AudioClip
import com.example.cj.videoeditor.domain.model.Clip
import com.example.cj.videoeditor.domain.model.ImageClip
import com.example.cj.videoeditor.domain.model.TextClip
import com.example.cj.videoeditor.domain.model.Timeline
import com.example.cj.videoeditor.domain.model.Track
import com.example.cj.videoeditor.domain.model.VideoClip
import java.util.UUID

/**
 * 分割片段命令
 */
class SplitClipCommand(
    private val clipId: String,
    private val splitTimeUs: Long
) : EditorCommand {

    private var firstClipId: String? = null
    private var secondClipId: String? = null

    override fun execute(timeline: Timeline): Timeline {
        val track = timeline.tracks.find { it.clips.any { clip -> clip.id == clipId } }
            ?: return timeline
        val clip = track.clips.find { it.id == clipId } ?: return timeline

        if (splitTimeUs <= clip.startTimeUs || splitTimeUs >= clip.endTimeUs) {
            return timeline
        }

        val splitRatio = (splitTimeUs - clip.startTimeUs).toFloat() / clip.timelineDurationUs
        val sourceSplitUs = clip.sourceStartUs + (clip.sourceDurationUs * splitRatio).toLong()

        val newFirstId = UUID.randomUUID().toString()
        val newSecondId = UUID.randomUUID().toString()
        firstClipId = newFirstId
        secondClipId = newSecondId

        val firstClip = createSplitClip(clip, newFirstId, clip.startTimeUs, splitTimeUs, clip.sourceStartUs, sourceSplitUs)
        val secondClip = createSplitClip(clip, newSecondId, splitTimeUs, clip.endTimeUs, sourceSplitUs, clip.sourceEndUs)

        return updateTimelineWithClips(timeline, track, clip, listOf(firstClip, secondClip))
    }

    override fun undo(timeline: Timeline): Timeline {
        val track = timeline.tracks.find { it.clips.any { clip -> clip.id == firstClipId || clip.id == secondClipId } }
            ?: return timeline

        val first = track.clips.find { it.id == firstClipId }
        val second = track.clips.find { it.id == secondClipId }
        if (first == null || second == null) return timeline

        val restoredClip = createSplitClip(
            first,
            clipId,
            first.startTimeUs,
            second.endTimeUs,
            first.sourceStartUs,
            second.sourceEndUs
        )

        val newClips = track.clips.toMutableList()
        val firstIndex = newClips.indexOfFirst { it.id == firstClipId }
        if (firstIndex >= 0) {
            newClips[firstIndex] = restoredClip
            newClips.removeAll { it.id == secondClipId }
        }

        return timeline.updateTrack(updateTrackClips(track, newClips))
    }

    private fun createSplitClip(
        original: Clip,
        newId: String,
        startTimeUs: Long,
        endTimeUs: Long,
        sourceStartUs: Long,
        sourceEndUs: Long
    ): Clip {
        return when (original) {
            is VideoClip -> original.copy(
                id = newId,
                startTimeUs = startTimeUs,
                endTimeUs = endTimeUs,
                sourceStartUs = sourceStartUs,
                sourceEndUs = sourceEndUs
            )
            is AudioClip -> original.copy(
                id = newId,
                startTimeUs = startTimeUs,
                endTimeUs = endTimeUs,
                sourceStartUs = sourceStartUs,
                sourceEndUs = sourceEndUs
            )
            is ImageClip -> original.copy(
                id = newId,
                startTimeUs = startTimeUs,
                endTimeUs = endTimeUs
            )
            is TextClip -> original.copy(
                id = newId,
                startTimeUs = startTimeUs,
                endTimeUs = endTimeUs
            )
        }
    }
}

/**
 * 裁剪片段命令（调整入点/出点）
 */
class TrimClipCommand(
    private val clipId: String,
    private val newStartTimeUs: Long,
    private val newEndTimeUs: Long,
    private val newSourceStartUs: Long,
    private val newSourceEndUs: Long
) : EditorCommand {

    private var oldClip: Clip? = null

    override fun execute(timeline: Timeline): Timeline {
        val track = timeline.tracks.find { it.clips.any { clip -> clip.id == clipId } }
            ?: return timeline
        val clip = track.clips.find { it.id == clipId } ?: return timeline

        oldClip = clip

        val trimmed = createUpdatedClip(
            clip,
            clip.id,
            newStartTimeUs,
            newEndTimeUs,
            newSourceStartUs,
            newSourceEndUs
        )

        return updateTimelineWithClips(timeline, track, clip, listOf(trimmed))
    }

    override fun undo(timeline: Timeline): Timeline {
        val original = oldClip ?: return timeline
        val track = timeline.tracks.find { it.clips.any { clip -> clip.id == clipId } }
            ?: return timeline
        val clip = track.clips.find { it.id == clipId } ?: return timeline

        return updateTimelineWithClips(timeline, track, clip, listOf(original))
    }
}

/**
 * 删除片段命令
 */
class DeleteClipCommand(
    private val clipId: String,
    private val rippleDelete: Boolean = true
) : EditorCommand {

    private var deletedClip: Clip? = null
    private var deletedTrackId: String? = null
    private var followingClips: List<Clip>? = null

    override fun execute(timeline: Timeline): Timeline {
        val track = timeline.tracks.find { it.clips.any { clip -> clip.id == clipId } }
            ?: return timeline
        val clip = track.clips.find { it.id == clipId } ?: return timeline

        deletedClip = clip
        deletedTrackId = track.id

        val newClips = track.clips.filter { it.id != clipId }.toMutableList()

        if (rippleDelete) {
            val removedDuration = clip.timelineDurationUs
            followingClips = newClips.filter { it.startTimeUs >= clip.endTimeUs }
            newClips.replaceAll { currentClip ->
                if (currentClip.startTimeUs >= clip.endTimeUs) {
                    shiftClip(currentClip, -removedDuration)
                } else {
                    currentClip
                }
            }
        }

        return timeline.updateTrack(updateTrackClips(track, newClips))
    }

    override fun undo(timeline: Timeline): Timeline {
        val originalClip = deletedClip ?: return timeline
        val trackId = deletedTrackId ?: return timeline
        val track = timeline.tracks.find { it.id == trackId } ?: return timeline

        val restoredClip = if (rippleDelete && followingClips != null) {
            val removedDuration = originalClip.timelineDurationUs
            val newClips = track.clips.toMutableList()
            newClips.replaceAll { currentClip ->
                if (followingClips!!.any { it.id == currentClip.id }) {
                    shiftClip(currentClip, removedDuration)
                } else {
                    currentClip
                }
            }
            val insertIndex = newClips.indexOfFirst { it.startTimeUs >= originalClip.startTimeUs }
                .coerceAtLeast(0)
            newClips.add(insertIndex, originalClip)
            timeline.updateTrack(updateTrackClips(track, newClips))
        } else {
            val newClips = track.clips.toMutableList()
            val insertIndex = newClips.indexOfFirst { it.startTimeUs >= originalClip.startTimeUs }
                .coerceAtLeast(0)
            newClips.add(insertIndex, originalClip)
            timeline.updateTrack(updateTrackClips(track, newClips))
        }

        return restoredClip
    }
}

/**
 * 复制片段命令
 */
class DuplicateClipCommand(
    private val clipId: String
) : EditorCommand {

    private var newClipId: String? = null

    override fun execute(timeline: Timeline): Timeline {
        val track = timeline.tracks.find { it.clips.any { clip -> clip.id == clipId } }
            ?: return timeline
        val clip = track.clips.find { it.id == clipId } ?: return timeline

        val duplicateId = UUID.randomUUID().toString()
        newClipId = duplicateId

        val duplicated = createUpdatedClip(
            clip,
            duplicateId,
            clip.endTimeUs,
            clip.endTimeUs + clip.timelineDurationUs,
            clip.sourceStartUs,
            clip.sourceEndUs
        )

        val insertIndex = track.clips.indexOfFirst { it.id == clipId } + 1
        val newClips = track.clips.toMutableList()
        newClips.add(insertIndex, duplicated)

        // 后续片段后移
        newClips.replaceAll { currentClip ->
            if (currentClip.startTimeUs >= duplicated.startTimeUs && currentClip.id != duplicateId) {
                shiftClip(currentClip, duplicated.timelineDurationUs)
            } else {
                currentClip
            }
        }

        // 重新对齐，避免重复片段被后移
        val finalClips = newClips.map { currentClip ->
            when (currentClip.id) {
                duplicateId -> duplicated
                else -> currentClip
            }
        }

        return timeline.updateTrack(updateTrackClips(track, finalClips))
    }

    override fun undo(timeline: Timeline): Timeline {
        val duplicateId = newClipId ?: return timeline
        val track = timeline.tracks.find { it.clips.any { clip -> clip.id == duplicateId } }
            ?: return timeline
        val duplicated = track.clips.find { it.id == duplicateId } ?: return timeline

        val newClips = track.clips.filter { it.id != duplicateId }.toMutableList()
        newClips.replaceAll { currentClip ->
            if (currentClip.startTimeUs >= duplicated.endTimeUs) {
                shiftClip(currentClip, -duplicated.timelineDurationUs)
            } else {
                currentClip
            }
        }

        return timeline.updateTrack(updateTrackClips(track, newClips))
    }
}

/**
 * 移动片段命令（同轨道内改变顺序）
 */
class MoveClipCommand(
    private val clipId: String,
    private val targetIndex: Int
) : EditorCommand {

    private var originalIndex: Int = -1

    override fun execute(timeline: Timeline): Timeline {
        val track = timeline.tracks.find { it.clips.any { clip -> clip.id == clipId } }
            ?: return timeline
        val clips = track.clips.toMutableList()
        originalIndex = clips.indexOfFirst { it.id == clipId }
        if (originalIndex < 0 || originalIndex == targetIndex) return timeline

        val movedClip = clips.removeAt(originalIndex)
        val safeIndex = targetIndex.coerceIn(0, clips.size)
        clips.add(safeIndex, movedClip)

        // 重新计算时间位置
        val reorderedClips = reorderClips(clips)
        return timeline.updateTrack(updateTrackClips(track, reorderedClips))
    }

    override fun undo(timeline: Timeline): Timeline {
        val track = timeline.tracks.find { it.clips.any { clip -> clip.id == clipId } }
            ?: return timeline
        val clips = track.clips.toMutableList()
        val currentIndex = clips.indexOfFirst { it.id == clipId }
        if (currentIndex < 0) return timeline

        val movedClip = clips.removeAt(currentIndex)
        val safeOriginalIndex = originalIndex.coerceIn(0, clips.size)
        clips.add(safeOriginalIndex, movedClip)

        val reorderedClips = reorderClips(clips)
        return timeline.updateTrack(updateTrackClips(track, reorderedClips))
    }
}

// ==================== 私有工具函数 ====================

private fun updateTimelineWithClips(
    timeline: Timeline,
    track: Track,
    oldClip: Clip,
    newClips: List<Clip>
): Timeline {
    val newTrackClips = track.clips.toMutableList()
    val index = newTrackClips.indexOfFirst { it.id == oldClip.id }
    if (index >= 0) {
        newTrackClips.removeAt(index)
        newTrackClips.addAll(index, newClips)
    }
    return timeline.updateTrack(updateTrackClips(track, newTrackClips))
}

private fun shiftClip(clip: Clip, deltaUs: Long): Clip {
    return createUpdatedClip(
        clip,
        clip.id,
        clip.startTimeUs + deltaUs,
        clip.endTimeUs + deltaUs,
        clip.sourceStartUs,
        clip.sourceEndUs
    )
}

private fun reorderClips(clips: List<Clip>): List<Clip> {
    var currentTimeUs = 0L
    return clips.map { clip ->
        val duration = clip.timelineDurationUs
        val newClip = createUpdatedClip(
            clip,
            clip.id,
            currentTimeUs,
            currentTimeUs + duration,
            clip.sourceStartUs,
            clip.sourceEndUs
        )
        currentTimeUs += duration
        newClip
    }
}

private fun createUpdatedClip(
    clip: Clip,
    newId: String,
    startTimeUs: Long,
    endTimeUs: Long,
    sourceStartUs: Long,
    sourceEndUs: Long
): Clip {
    return when (clip) {
        is VideoClip -> clip.copy(
            id = newId,
            startTimeUs = startTimeUs,
            endTimeUs = endTimeUs,
            sourceStartUs = sourceStartUs,
            sourceEndUs = sourceEndUs
        )
        is AudioClip -> clip.copy(
            id = newId,
            startTimeUs = startTimeUs,
            endTimeUs = endTimeUs,
            sourceStartUs = sourceStartUs,
            sourceEndUs = sourceEndUs
        )
        is ImageClip -> clip.copy(
            id = newId,
            startTimeUs = startTimeUs,
            endTimeUs = endTimeUs
        )
        is TextClip -> clip.copy(
            id = newId,
            startTimeUs = startTimeUs,
            endTimeUs = endTimeUs
        )
    }
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

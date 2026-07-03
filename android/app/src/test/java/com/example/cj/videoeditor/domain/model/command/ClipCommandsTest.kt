package com.example.cj.videoeditor.domain.model.command

import android.net.Uri
import com.example.cj.videoeditor.domain.model.AudioClip
import com.example.cj.videoeditor.domain.model.TextClip
import com.example.cj.videoeditor.domain.model.Timeline
import com.example.cj.videoeditor.domain.model.VideoClip
import com.example.cj.videoeditor.domain.model.VideoTrack
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ClipCommandsTest {

    private val testUri: Uri = Uri.parse("file:///test.mp4")

    private fun createVideoClip(
        id: String,
        trackId: String = "video-1",
        startTimeUs: Long,
        endTimeUs: Long,
        sourceStartUs: Long = startTimeUs,
        sourceEndUs: Long = endTimeUs
    ): VideoClip {
        return VideoClip(
            id = id,
            trackId = trackId,
            startTimeUs = startTimeUs,
            endTimeUs = endTimeUs,
            sourceStartUs = sourceStartUs,
            sourceEndUs = sourceEndUs,
            uri = testUri
        )
    }

    private fun createTimeline(vararg clips: VideoClip): Timeline {
        return Timeline(tracks = listOf(VideoTrack(id = "video-1", clips = clips.toList())))
    }

    @Test
    fun `split command divides clip into two parts`() {
        val timeline = createTimeline(createVideoClip("c1", startTimeUs = 0L, endTimeUs = 10_000_000L))
        val command = SplitClipCommand("c1", splitTimeUs = 4_000_000L)

        val result = command.execute(timeline)
        val clips = result.clipsOnTrack("video-1")

        assertThat(clips).hasSize(2)
        assertThat(clips[0].startTimeUs).isEqualTo(0L)
        assertThat(clips[0].endTimeUs).isEqualTo(4_000_000L)
        assertThat(clips[1].startTimeUs).isEqualTo(4_000_000L)
        assertThat(clips[1].endTimeUs).isEqualTo(10_000_000L)
    }

    @Test
    fun `split at boundary returns unchanged timeline`() {
        val timeline = createTimeline(createVideoClip("c1", startTimeUs = 0L, endTimeUs = 10_000_000L))
        val command = SplitClipCommand("c1", splitTimeUs = 0L)

        val result = command.execute(timeline)

        assertThat(result.clipsOnTrack("video-1")).hasSize(1)
    }

    @Test
    fun `split undo restores original clip`() {
        val original = createVideoClip("c1", startTimeUs = 0L, endTimeUs = 10_000_000L)
        val timeline = createTimeline(original)
        val command = SplitClipCommand("c1", splitTimeUs = 4_000_000L)

        val afterSplit = command.execute(timeline)
        val afterUndo = command.undo(afterSplit)

        assertThat(afterUndo.clipsOnTrack("video-1")).hasSize(1)
        assertThat(afterUndo.clipsOnTrack("video-1")[0].startTimeUs).isEqualTo(original.startTimeUs)
        assertThat(afterUndo.clipsOnTrack("video-1")[0].endTimeUs).isEqualTo(original.endTimeUs)
    }

    @Test
    fun `trim command updates clip boundaries`() {
        val timeline = createTimeline(
            createVideoClip("c1", startTimeUs = 0L, endTimeUs = 10_000_000L)
        )
        val command = TrimClipCommand(
            clipId = "c1",
            newStartTimeUs = 2_000_000L,
            newEndTimeUs = 8_000_000L,
            newSourceStartUs = 2_000_000L,
            newSourceEndUs = 8_000_000L
        )

        val result = command.execute(timeline)
        val clip = result.clipsOnTrack("video-1")[0]

        assertThat(clip.startTimeUs).isEqualTo(2_000_000L)
        assertThat(clip.endTimeUs).isEqualTo(8_000_000L)
    }

    @Test
    fun `trim undo restores original clip`() {
        val timeline = createTimeline(
            createVideoClip("c1", startTimeUs = 0L, endTimeUs = 10_000_000L)
        )
        val command = TrimClipCommand(
            clipId = "c1",
            newStartTimeUs = 2_000_000L,
            newEndTimeUs = 8_000_000L,
            newSourceStartUs = 2_000_000L,
            newSourceEndUs = 8_000_000L
        )

        val afterTrim = command.execute(timeline)
        val afterUndo = command.undo(afterTrim)
        val clip = afterUndo.clipsOnTrack("video-1")[0]

        assertThat(clip.startTimeUs).isEqualTo(0L)
        assertThat(clip.endTimeUs).isEqualTo(10_000_000L)
    }

    @Test
    fun `delete command removes clip and ripple shifts following clips`() {
        val timeline = createTimeline(
            createVideoClip("c1", startTimeUs = 0L, endTimeUs = 3_000_000L),
            createVideoClip("c2", startTimeUs = 3_000_000L, endTimeUs = 6_000_000L),
            createVideoClip("c3", startTimeUs = 6_000_000L, endTimeUs = 10_000_000L)
        )
        val command = DeleteClipCommand("c2", rippleDelete = true)

        val result = command.execute(timeline)
        val clips = result.clipsOnTrack("video-1")

        assertThat(clips).hasSize(2)
        assertThat(clips[0].id).isEqualTo("c1")
        assertThat(clips[1].id).isEqualTo("c3")
        assertThat(clips[1].startTimeUs).isEqualTo(3_000_000L)
        assertThat(clips[1].endTimeUs).isEqualTo(7_000_000L)
    }

    @Test
    fun `delete without ripple does not shift other clips`() {
        val timeline = createTimeline(
            createVideoClip("c1", startTimeUs = 0L, endTimeUs = 3_000_000L),
            createVideoClip("c2", startTimeUs = 3_000_000L, endTimeUs = 6_000_000L)
        )
        val command = DeleteClipCommand("c1", rippleDelete = false)

        val result = command.execute(timeline)
        val clips = result.clipsOnTrack("video-1")

        assertThat(clips).hasSize(1)
        assertThat(clips[0].startTimeUs).isEqualTo(3_000_000L)
        assertThat(clips[0].endTimeUs).isEqualTo(6_000_000L)
    }

    @Test
    fun `delete undo restores clip and shifts following clips back`() {
        val timeline = createTimeline(
            createVideoClip("c1", startTimeUs = 0L, endTimeUs = 3_000_000L),
            createVideoClip("c2", startTimeUs = 3_000_000L, endTimeUs = 6_000_000L)
        )
        val command = DeleteClipCommand("c1", rippleDelete = true)

        val afterDelete = command.execute(timeline)
        val afterUndo = command.undo(afterDelete)
        val clips = afterUndo.clipsOnTrack("video-1")

        assertThat(clips).hasSize(2)
        assertThat(clips[0].id).isEqualTo("c1")
        assertThat(clips[1].id).isEqualTo("c2")
        assertThat(clips[1].startTimeUs).isEqualTo(3_000_000L)
        assertThat(clips[1].endTimeUs).isEqualTo(6_000_000L)
    }

    @Test
    fun `duplicate command creates copy after original`() {
        val timeline = createTimeline(
            createVideoClip("c1", startTimeUs = 0L, endTimeUs = 3_000_000L)
        )
        val command = DuplicateClipCommand("c1")

        val result = command.execute(timeline)
        val clips = result.clipsOnTrack("video-1")

        assertThat(clips).hasSize(2)
        assertThat(clips[0].id).isEqualTo("c1")
        assertThat(clips[0].startTimeUs).isEqualTo(0L)
        assertThat(clips[0].endTimeUs).isEqualTo(3_000_000L)
        assertThat(clips[1].startTimeUs).isEqualTo(3_000_000L)
        assertThat(clips[1].endTimeUs).isEqualTo(6_000_000L)
    }

    @Test
    fun `duplicate undo removes copy and shifts following clips back`() {
        val timeline = createTimeline(
            createVideoClip("c1", startTimeUs = 0L, endTimeUs = 3_000_000L),
            createVideoClip("c2", startTimeUs = 3_000_000L, endTimeUs = 6_000_000L)
        )
        val command = DuplicateClipCommand("c1")

        val afterDuplicate = command.execute(timeline)
        val afterUndo = command.undo(afterDuplicate)
        val clips = afterUndo.clipsOnTrack("video-1")

        assertThat(clips).hasSize(2)
        assertThat(clips[0].id).isEqualTo("c1")
        assertThat(clips[1].id).isEqualTo("c2")
        assertThat(clips[1].startTimeUs).isEqualTo(3_000_000L)
        assertThat(clips[1].endTimeUs).isEqualTo(6_000_000L)
    }

    @Test
    fun `move command reorders clips and recalculates time`() {
        val timeline = createTimeline(
            createVideoClip("c1", startTimeUs = 0L, endTimeUs = 2_000_000L),
            createVideoClip("c2", startTimeUs = 2_000_000L, endTimeUs = 5_000_000L)
        )
        val command = MoveClipCommand("c1", targetIndex = 1)

        val result = command.execute(timeline)
        val clips = result.clipsOnTrack("video-1")

        assertThat(clips[0].id).isEqualTo("c2")
        assertThat(clips[0].startTimeUs).isEqualTo(0L)
        assertThat(clips[0].endTimeUs).isEqualTo(3_000_000L)
        assertThat(clips[1].id).isEqualTo("c1")
        assertThat(clips[1].startTimeUs).isEqualTo(3_000_000L)
        assertThat(clips[1].endTimeUs).isEqualTo(5_000_000L)
    }

    @Test
    fun `move undo restores original order`() {
        val timeline = createTimeline(
            createVideoClip("c1", startTimeUs = 0L, endTimeUs = 2_000_000L),
            createVideoClip("c2", startTimeUs = 2_000_000L, endTimeUs = 5_000_000L)
        )
        val command = MoveClipCommand("c1", targetIndex = 1)

        val afterMove = command.execute(timeline)
        val afterUndo = command.undo(afterMove)
        val clips = afterUndo.clipsOnTrack("video-1")

        assertThat(clips[0].id).isEqualTo("c1")
        assertThat(clips[0].startTimeUs).isEqualTo(0L)
        assertThat(clips[0].endTimeUs).isEqualTo(2_000_000L)
        assertThat(clips[1].id).isEqualTo("c2")
        assertThat(clips[1].startTimeUs).isEqualTo(2_000_000L)
        assertThat(clips[1].endTimeUs).isEqualTo(5_000_000L)
    }
}

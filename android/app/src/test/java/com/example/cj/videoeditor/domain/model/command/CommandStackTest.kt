package com.example.cj.videoeditor.domain.model.command

import com.example.cj.videoeditor.domain.model.TextClip
import com.example.cj.videoeditor.domain.model.Timeline
import com.example.cj.videoeditor.domain.model.VideoTrack
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CommandStackTest {

    private fun createTimeline(vararg durationsUs: Long): Timeline {
        var start = 0L
        val clips = durationsUs.mapIndexed { index, duration ->
            val clip = TextClip(
                id = "clip-$index",
                trackId = "video-1",
                startTimeUs = start,
                endTimeUs = start + duration,
                text = "Clip $index"
            )
            start += duration
            clip
        }
        return Timeline(tracks = listOf(VideoTrack(id = "video-1", clips = clips)))
    }

    private class AddClipCommand(private val clipId: String) : EditorCommand {
        override fun execute(timeline: Timeline): Timeline {
            val track = timeline.tracks.first() as VideoTrack
            val newClip = TextClip(
                id = clipId,
                trackId = track.id,
                startTimeUs = 0L,
                endTimeUs = 1_000_000L,
                text = "Added"
            )
            return timeline.updateTrack(track.copy(clips = track.clips + newClip))
        }

        override fun undo(timeline: Timeline): Timeline {
            val track = timeline.tracks.first() as VideoTrack
            return timeline.updateTrack(track.copy(clips = track.clips.filter { it.id != clipId }))
        }
    }

    @Test
    fun `execute adds command to undo stack and clears redo stack`() {
        val stack = CommandStack()
        val timeline = createTimeline(1_000_000L)

        val result = stack.execute(AddClipCommand("added-1"), timeline)

        assertThat(stack.canUndo).isTrue()
        assertThat(stack.canRedo).isFalse()
        assertThat(result.clipsOnTrack("video-1")).hasSize(2)
    }

    @Test
    fun `undo reverts last command`() {
        val stack = CommandStack()
        val timeline = createTimeline(1_000_000L)

        val afterExecute = stack.execute(AddClipCommand("added-1"), timeline)
        val undone = stack.undo(afterExecute)

        assertThat(stack.canUndo).isFalse()
        assertThat(stack.canRedo).isTrue()
        assertThat(undone.clipsOnTrack("video-1")).hasSize(1)
    }

    @Test
    fun `redo re-applies undone command`() {
        val stack = CommandStack()
        val timeline = createTimeline(1_000_000L)

        val afterExecute = stack.execute(AddClipCommand("added-1"), timeline)
        val afterUndo = stack.undo(afterExecute)
        val afterRedo = stack.redo(afterUndo)

        assertThat(stack.canUndo).isTrue()
        assertThat(stack.canRedo).isFalse()
        assertThat(afterRedo.clipsOnTrack("video-1")).hasSize(2)
    }

    @Test
    fun `new execute after undo clears redo stack`() {
        val stack = CommandStack()
        val timeline = createTimeline(1_000_000L)

        val afterFirst = stack.execute(AddClipCommand("added-1"), timeline)
        val afterUndo = stack.undo(afterFirst)
        stack.execute(AddClipCommand("added-2"), afterUndo)

        assertThat(stack.canRedo).isFalse()
    }

    @Test
    fun `undo on empty stack returns same timeline`() {
        val stack = CommandStack()
        val timeline = createTimeline(1_000_000L)

        val result = stack.undo(timeline)

        assertThat(result).isSameInstanceAs(timeline)
    }

    @Test
    fun `redo on empty stack returns same timeline`() {
        val stack = CommandStack()
        val timeline = createTimeline(1_000_000L)

        val result = stack.redo(timeline)

        assertThat(result).isSameInstanceAs(timeline)
    }

    @Test
    fun `max size drops oldest commands`() {
        val stack = CommandStack(maxSize = 2)
        val timeline = createTimeline(1_000_000L)

        stack.execute(AddClipCommand("added-1"), timeline)
        stack.execute(AddClipCommand("added-2"), timeline)
        stack.execute(AddClipCommand("added-3"), timeline)

        assertThat(stack.canUndo).isTrue()
        // First command should have been evicted, so undoing only removes the newest two
        val afterUndoAll = stack.undo(stack.undo(stack.execute(AddClipCommand("added-3"), timeline)))
        assertThat(afterUndoAll.clipsOnTrack("video-1")).hasSize(1)
    }
}

package com.example.cj.videoeditor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TimelineTest {

    @Test
    fun `empty timeline has zero duration`() {
        val timeline = Timeline()

        assertThat(timeline.durationUs).isEqualTo(0L)
        assertThat(timeline.currentPositionUs).isEqualTo(0L)
    }

    @Test
    fun `duration equals latest track end time`() {
        val videoTrack = VideoTrack(
            id = "video-1",
            clips = listOf(
                TextClip(
                    id = "clip-1",
                    trackId = "video-1",
                    startTimeUs = 0L,
                    endTimeUs = 5_000_000L,
                    text = "Hello"
                )
            )
        )
        val audioTrack = AudioTrack(
            id = "audio-1",
            clips = listOf(
                AudioClip(
                    id = "clip-2",
                    trackId = "audio-1",
                    startTimeUs = 0L,
                    endTimeUs = 10_000_000L,
                    sourceStartUs = 0L,
                    sourceEndUs = 10_000_000L,
                    uri = android.net.Uri.parse("file:///audio.mp3")
                )
            )
        )
        val timeline = Timeline(tracks = listOf(videoTrack, audioTrack))

        assertThat(timeline.durationUs).isEqualTo(10_000_000L)
    }

    @Test
    fun `tracksByType filters by track type`() {
        val videoTrack = VideoTrack(id = "video-1")
        val audioTrack = AudioTrack(id = "audio-1")
        val timeline = Timeline(tracks = listOf(videoTrack, audioTrack))

        assertThat(timeline.tracksByType(TrackType.VIDEO)).containsExactly(videoTrack)
        assertThat(timeline.tracksByType(TrackType.AUDIO)).containsExactly(audioTrack)
        assertThat(timeline.tracksByType(TrackType.TEXT)).isEmpty()
    }

    @Test
    fun `clipsOnTrack returns clips for given track`() {
        val clip = TextClip(
            id = "clip-1",
            trackId = "text-1",
            startTimeUs = 0L,
            endTimeUs = 3_000_000L,
            text = "Subtitle"
        )
        val track = TextTrack(id = "text-1", clips = listOf(clip))
        val timeline = Timeline(tracks = listOf(track))

        assertThat(timeline.clipsOnTrack("text-1")).containsExactly(clip)
        assertThat(timeline.clipsOnTrack("missing")).isEmpty()
    }

    @Test
    fun `clipsAtTime returns clips overlapping position`() {
        val clip1 = TextClip(
            id = "clip-1",
            trackId = "video-1",
            startTimeUs = 0L,
            endTimeUs = 5_000_000L,
            text = "First"
        )
        val clip2 = TextClip(
            id = "clip-2",
            trackId = "video-1",
            startTimeUs = 5_000_000L,
            endTimeUs = 10_000_000L,
            text = "Second"
        )
        val track = VideoTrack(id = "video-1", clips = listOf(clip1, clip2))
        val timeline = Timeline(tracks = listOf(track))

        assertThat(timeline.clipsAtTime(2_000_000L)).containsExactly(clip1)
        assertThat(timeline.clipsAtTime(5_000_000L)).containsExactly(clip2)
        assertThat(timeline.clipsAtTime(10_000_000L)).isEmpty()
    }

    @Test
    fun `mainVideoTrack returns first video track`() {
        val videoTrack = VideoTrack(id = "video-1")
        val timeline = Timeline(tracks = listOf(videoTrack))

        assertThat(timeline.mainVideoTrack()).isEqualTo(videoTrack)
    }

    @Test
    fun `updateTrack replaces matching track`() {
        val original = VideoTrack(id = "video-1")
        val timeline = Timeline(tracks = listOf(original))
        val updated = VideoTrack(id = "video-1", clips = listOf())

        val result = timeline.updateTrack(updated)

        assertThat(result.tracks).containsExactly(updated)
    }

    @Test
    fun `seekTo clamps position within duration`() {
        val clip = TextClip(
            id = "clip-1",
            trackId = "video-1",
            startTimeUs = 0L,
            endTimeUs = 5_000_000L,
            text = "Hello"
        )
        val timeline = Timeline(tracks = listOf(VideoTrack(id = "video-1", clips = listOf(clip))))

        assertThat(timeline.seekTo(2_000_000L).currentPositionUs).isEqualTo(2_000_000L)
        assertThat(timeline.seekTo(-1_000_000L).currentPositionUs).isEqualTo(0L)
        assertThat(timeline.seekTo(10_000_000L).currentPositionUs).isEqualTo(5_000_000L)
    }
}

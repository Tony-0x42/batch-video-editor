package com.example.cj.videoeditor.domain.model

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ClipTest {

    private val testUri: Uri = Uri.parse("file:///test.mp4")

    @Test
    fun `timelineDurationUs returns end minus start`() {
        val clip = VideoClip(
            id = "v1",
            trackId = "t1",
            startTimeUs = 1_000_000L,
            endTimeUs = 5_000_000L,
            sourceStartUs = 0L,
            sourceEndUs = 4_000_000L,
            uri = testUri
        )

        assertThat(clip.timelineDurationUs).isEqualTo(4_000_000L)
    }

    @Test
    fun `sourceDurationUs returns source end minus source start`() {
        val clip = VideoClip(
            id = "v1",
            trackId = "t1",
            startTimeUs = 0L,
            endTimeUs = 4_000_000L,
            sourceStartUs = 1_000_000L,
            sourceEndUs = 5_000_000L,
            uri = testUri
        )

        assertThat(clip.sourceDurationUs).isEqualTo(4_000_000L)
    }

    @Test
    fun `video clip recalculateEndTime applies speed`() {
        val clip = VideoClip(
            id = "v1",
            trackId = "t1",
            startTimeUs = 0L,
            endTimeUs = 4_000_000L,
            sourceStartUs = 0L,
            sourceEndUs = 4_000_000L,
            uri = testUri,
            speed = 2.0f
        )

        assertThat(clip.recalculateEndTime()).isEqualTo(2_000_000L)
    }

    @Test
    fun `video clip recalculateEndTime falls back when speed is zero`() {
        val clip = VideoClip(
            id = "v1",
            trackId = "t1",
            startTimeUs = 0L,
            endTimeUs = 4_000_000L,
            sourceStartUs = 0L,
            sourceEndUs = 4_000_000L,
            uri = testUri,
            speed = 0f
        )

        assertThat(clip.recalculateEndTime()).isEqualTo(4_000_000L)
    }

    @Test
    fun `image clip source duration equals timeline duration`() {
        val clip = ImageClip(
            id = "i1",
            trackId = "t1",
            startTimeUs = 0L,
            endTimeUs = 3_000_000L,
            uri = testUri
        )

        assertThat(clip.sourceDurationUs).isEqualTo(3_000_000L)
        assertThat(clip.timelineDurationUs).isEqualTo(3_000_000L)
    }

    @Test
    fun `text clip source duration equals timeline duration`() {
        val clip = TextClip(
            id = "x1",
            trackId = "t1",
            startTimeUs = 1_000_000L,
            endTimeUs = 4_000_000L,
            text = "Hello"
        )

        assertThat(clip.sourceDurationUs).isEqualTo(3_000_000L)
        assertThat(clip.timelineDurationUs).isEqualTo(3_000_000L)
    }
}

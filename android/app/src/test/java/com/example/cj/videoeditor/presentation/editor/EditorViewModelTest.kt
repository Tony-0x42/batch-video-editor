package com.example.cj.videoeditor.presentation.editor

import android.net.Uri
import app.cash.turbine.test
import com.example.cj.videoeditor.data.repository.TimelineRepositoryImpl
import com.example.cj.videoeditor.domain.model.AudioClip
import com.example.cj.videoeditor.domain.model.AudioTrack
import com.example.cj.videoeditor.domain.model.TextClip
import com.example.cj.videoeditor.domain.model.TextTrack
import com.example.cj.videoeditor.domain.model.Timeline
import com.example.cj.videoeditor.domain.model.VideoClip
import com.example.cj.videoeditor.domain.model.VideoTrack
import com.example.cj.videoeditor.domain.usecase.editor.EditorUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var timelineRepository: TimelineRepositoryImpl
    private lateinit var editorUseCase: EditorUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        timelineRepository = TimelineRepositoryImpl()
        editorUseCase = EditorUseCase(timelineRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun createViewModel(): EditorViewModel {
        timelineRepository.setTimeline(createTestTimeline())
        return EditorViewModel(editorUseCase, timelineRepository)
    }

    @Test
    fun `initial state loads timeline from repository`() = runTest(testDispatcher) {
        val expectedTimeline = Timeline(
            tracks = listOf(
                VideoTrack(
                    id = "track_video_1",
                    clips = listOf(
                        VideoClip(
                            id = "clip_video_1",
                            trackId = "track_video_1",
                            startTimeUs = 0L,
                            endTimeUs = 3_000_000L,
                            sourceStartUs = 0L,
                            sourceEndUs = 3_000_000L,
                            uri = Uri.parse("content://real/video1")
                        )
                    )
                )
            )
        )
        timelineRepository.setTimeline(expectedTimeline)

        val viewModel = EditorViewModel(editorUseCase, timelineRepository)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.timeline.tracks).hasSize(1)
            assertThat(state.timeline.clipsOnTrack("track_video_1")).hasSize(1)
            assertThat(state.timeline.clipsOnTrack("track_video_1")[0].id).isEqualTo("clip_video_1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `splitClip divides video clip`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.splitClip("clip_video_1", splitTimeUs = 2_000_000L)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val videoClips = state.timeline.clipsOnTrack("track_video_1")
            assertThat(videoClips).hasSize(3)
            assertThat(state.canUndo).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteClip removes clip and enables undo`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteClip("clip_video_1")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val videoClips = state.timeline.clipsOnTrack("track_video_1")
            assertThat(videoClips).hasSize(1)
            assertThat(videoClips[0].id).isEqualTo("clip_video_2")
            assertThat(state.canUndo).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `undo restores deleted clip`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteClip("clip_video_1")
        advanceUntilIdle()
        viewModel.undo()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val videoClips = state.timeline.clipsOnTrack("track_video_1")
            assertThat(videoClips).hasSize(2)
            assertThat(videoClips[0].id).isEqualTo("clip_video_1")
            assertThat(state.canUndo).isFalse()
            assertThat(state.canRedo).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `seekTo clamps position within duration`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.seekTo(5_000_000L)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.timeline.currentPositionUs).isEqualTo(5_000_000L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createTestTimeline(): Timeline {
        val videoTrack = VideoTrack(
            id = "track_video_1",
            clips = listOf(
                VideoClip(
                    id = "clip_video_1",
                    trackId = "track_video_1",
                    startTimeUs = 0L,
                    endTimeUs = 5_000_000L,
                    sourceStartUs = 0L,
                    sourceEndUs = 5_000_000L,
                    uri = Uri.parse("content://test/video1")
                ),
                VideoClip(
                    id = "clip_video_2",
                    trackId = "track_video_1",
                    startTimeUs = 5_000_000L,
                    endTimeUs = 10_000_000L,
                    sourceStartUs = 0L,
                    sourceEndUs = 5_000_000L,
                    uri = Uri.parse("content://test/video2")
                )
            )
        )

        val audioTrack = AudioTrack(
            id = "track_audio_1",
            clips = listOf(
                AudioClip(
                    id = "clip_audio_1",
                    trackId = "track_audio_1",
                    startTimeUs = 0L,
                    endTimeUs = 10_000_000L,
                    sourceStartUs = 0L,
                    sourceEndUs = 10_000_000L,
                    uri = Uri.parse("content://test/audio1")
                )
            )
        )

        val textTrack = TextTrack(
            id = "track_text_1",
            clips = listOf(
                TextClip(
                    id = "clip_text_1",
                    trackId = "track_text_1",
                    startTimeUs = 2_000_000L,
                    endTimeUs = 6_000_000L,
                    text = "片头标题"
                )
            )
        )

        return Timeline(tracks = listOf(videoTrack, audioTrack, textTrack))
    }
}

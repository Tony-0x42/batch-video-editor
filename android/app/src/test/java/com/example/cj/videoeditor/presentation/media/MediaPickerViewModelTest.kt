package com.example.cj.videoeditor.presentation.media

import android.content.Context
import app.cash.turbine.test
import com.example.cj.videoeditor.domain.repository.MediaRepository
import com.example.cj.videoeditor.domain.repository.TimelineRepository
import com.example.cj.videoeditor.ui.media.MediaItem
import com.example.cj.videoeditor.ui.media.MediaType
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
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

@OptIn(ExperimentalCoroutinesApi::class)
class MediaPickerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mediaRepository: MediaRepository
    private lateinit var timelineRepository: TimelineRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mediaRepository = mockk()
        timelineRepository = mockk(relaxed = true)
        context = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MediaPickerViewModel {
        return MediaPickerViewModel(mediaRepository, timelineRepository, context)
    }

    @Test
    fun `toggleSelection adds and removes media id`() = runTest(testDispatcher) {
        coEvery { mediaRepository.loadByType(any()) } returns emptyList()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleSelection("media-1")
        viewModel.toggleSelection("media-2")

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedIds).containsExactly("media-1", "media-2")

            viewModel.toggleSelection("media-1")
            assertThat(awaitItem().selectedIds).containsExactly("media-2")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMedia updates selected tab and loads items`() = runTest(testDispatcher) {
        coEvery { mediaRepository.loadByType(MediaType.VIDEO) } returns emptyList()
        coEvery { mediaRepository.loadByType(MediaType.PHOTO) } returns listOf(
            MediaItem(id = "photo-1", uri = "content://photo/1", type = MediaType.PHOTO)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMedia(MediaType.PHOTO)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedTab).isEqualTo(MediaType.PHOTO)
            assertThat(state.mediaList).hasSize(1)
            assertThat(state.mediaList[0].id).isEqualTo("photo-1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMedia sets error when permission denied`() = runTest(testDispatcher) {
        coEvery { mediaRepository.loadByType(any()) } throws SecurityException("Permission denied")

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.errorMessage).isNotNull()
            assertThat(state.errorMessage).contains("存储权限")
            cancelAndIgnoreRemainingEvents()
        }
    }
}

package com.example.cj.videoeditor.presentation.main

import androidx.compose.ui.graphics.Color
import app.cash.turbine.test
import com.example.cj.videoeditor.domain.model.Draft
import com.example.cj.videoeditor.domain.repository.DraftRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var draftRepository: FakeDraftRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        draftRepository = FakeDraftRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state finishes loading with empty drafts`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(draftRepository)

        viewModel.uiState.test {
            // Initial emission
            assertThat(awaitItem().isLoading).isFalse()

            // Loading starts
            assertThat(awaitItem().isLoading).isTrue()

            // Loading completes with empty drafts
            val finalState = awaitItem()
            assertThat(finalState.isLoading).isFalse()
            assertThat(finalState.drafts).isEmpty()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saving a draft updates the ui state`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(draftRepository)
        advanceUntilIdle()

        val now = System.currentTimeMillis()
        draftRepository.saveDraft(
            Draft(
                id = "draft-1",
                title = "旅行 vlog",
                thumbnailColorHex = "#FF6B6B",
                durationUs = 75_000_000,
                updatedAtMillis = now - 2 * 60 * 1000
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.drafts).hasSize(1)

        val draftItem = state.drafts.first()
        assertThat(draftItem.id).isEqualTo("draft-1")
        assertThat(draftItem.title).isEqualTo("旅行 vlog")
        assertThat(draftItem.thumbnailColor).isEqualTo(Color(0xFFFF6B6B))
        assertThat(draftItem.durationText).isEqualTo("01:15")
        assertThat(draftItem.updateTimeText).isEqualTo("2分钟前")
    }

    private class FakeDraftRepository : DraftRepository {

        private val drafts = MutableStateFlow<List<Draft>>(emptyList())

        override fun getDrafts(): Flow<List<Draft>> = drafts

        override suspend fun saveDraft(draft: Draft) {
            drafts.update { list ->
                val index = list.indexOfFirst { it.id == draft.id }
                if (index >= 0) {
                    list.toMutableList().apply { set(index, draft) }
                } else {
                    list + draft
                }
            }
        }

        override suspend fun deleteDraft(draftId: String) {
            drafts.update { list -> list.filter { it.id != draftId } }
        }
    }
}

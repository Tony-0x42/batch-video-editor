package com.example.cj.videoeditor.data.repository

import com.example.cj.videoeditor.domain.model.Draft
import com.example.cj.videoeditor.domain.repository.DraftRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * 草稿仓库内存实现
 *
 * 当前以内存数据提供草稿列表，后续可替换为 Room/DataStore 持久化实现。
 */
@Singleton
class DraftRepositoryImpl @Inject constructor() : DraftRepository {

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

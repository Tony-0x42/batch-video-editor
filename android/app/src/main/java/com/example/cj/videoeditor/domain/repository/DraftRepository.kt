package com.example.cj.videoeditor.domain.repository

import com.example.cj.videoeditor.domain.model.Draft
import kotlinx.coroutines.flow.Flow

/**
 * 草稿仓库接口
 */
interface DraftRepository {
    fun getDrafts(): Flow<List<Draft>>
    suspend fun saveDraft(draft: Draft)
    suspend fun deleteDraft(draftId: String)
}

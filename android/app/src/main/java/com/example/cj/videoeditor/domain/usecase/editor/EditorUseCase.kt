package com.example.cj.videoeditor.domain.usecase.editor

import com.example.cj.videoeditor.domain.model.Clip
import com.example.cj.videoeditor.domain.model.Timeline
import com.example.cj.videoeditor.domain.model.command.CommandStack
import com.example.cj.videoeditor.domain.model.command.DeleteClipCommand
import com.example.cj.videoeditor.domain.model.command.DuplicateClipCommand
import com.example.cj.videoeditor.domain.model.command.MoveClipCommand
import com.example.cj.videoeditor.domain.model.command.SplitClipCommand
import com.example.cj.videoeditor.domain.model.command.TrimClipCommand
import com.example.cj.videoeditor.domain.repository.TimelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 编辑器核心用例
 *
 * 封装所有剪辑操作与撤销重做管理。
 */
@Singleton
class EditorUseCase @Inject constructor(
    private val timelineRepository: TimelineRepository
) {

    private val commandStack = CommandStack()

    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    fun getTimeline(): Flow<Timeline> = timelineRepository.getTimeline()

    suspend fun splitClip(clipId: String, splitTimeUs: Long) {
        executeCommand(SplitClipCommand(clipId, splitTimeUs))
    }

    suspend fun trimClip(
        clipId: String,
        newStartTimeUs: Long,
        newEndTimeUs: Long,
        newSourceStartUs: Long,
        newSourceEndUs: Long
    ) {
        executeCommand(
            TrimClipCommand(
                clipId,
                newStartTimeUs,
                newEndTimeUs,
                newSourceStartUs,
                newSourceEndUs
            )
        )
    }

    suspend fun deleteClip(clipId: String, rippleDelete: Boolean = true) {
        executeCommand(DeleteClipCommand(clipId, rippleDelete))
    }

    suspend fun duplicateClip(clipId: String) {
        executeCommand(DuplicateClipCommand(clipId))
    }

    suspend fun moveClip(clipId: String, targetIndex: Int) {
        executeCommand(MoveClipCommand(clipId, targetIndex))
    }

    suspend fun undo() {
        val currentTimeline = timelineRepository.getCurrentTimeline()
        val newTimeline = commandStack.undo(currentTimeline)
        timelineRepository.setTimeline(newTimeline)
        updateEditorState()
    }

    suspend fun redo() {
        val currentTimeline = timelineRepository.getCurrentTimeline()
        val newTimeline = commandStack.redo(currentTimeline)
        timelineRepository.setTimeline(newTimeline)
        updateEditorState()
    }

    /**
     * 清空命令栈，用于新的剪辑会话开始时重置撤销/重做状态。
     */
    fun clearCommandStack() {
        commandStack.clear()
        updateEditorState()
    }

    private suspend fun executeCommand(command: com.example.cj.videoeditor.domain.model.command.EditorCommand) {
        val currentTimeline = timelineRepository.getCurrentTimeline()
        val newTimeline = commandStack.execute(command, currentTimeline)
        timelineRepository.setTimeline(newTimeline)
        updateEditorState()
    }

    private fun updateEditorState() {
        _editorState.value = EditorState(
            canUndo = commandStack.canUndo,
            canRedo = commandStack.canRedo
        )
    }

    data class EditorState(
        val canUndo: Boolean = false,
        val canRedo: Boolean = false,
        val isLoading: Boolean = false
    )
}

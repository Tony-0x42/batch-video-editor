package com.example.cj.videoeditor.domain.model.command

import com.example.cj.videoeditor.domain.model.Timeline

/**
 * 可撤销的编辑器命令接口
 */
interface EditorCommand {

    /**
     * 执行命令，返回新的 Timeline
     */
    fun execute(timeline: Timeline): Timeline

    /**
     * 撤销命令，返回撤销后的 Timeline
     */
    fun undo(timeline: Timeline): Timeline
}

/**
 * 命令栈
 */
class CommandStack(private val maxSize: Int = 50) {

    private val undoStack = ArrayDeque<EditorCommand>()
    private val redoStack = ArrayDeque<EditorCommand>()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    /**
     * 执行新命令
     */
    fun execute(command: EditorCommand, timeline: Timeline): Timeline {
        val result = command.execute(timeline)
        undoStack.addLast(command)
        redoStack.clear()
        if (undoStack.size > maxSize) {
            undoStack.removeFirst()
        }
        return result
    }

    /**
     * 撤销
     */
    fun undo(timeline: Timeline): Timeline {
        val command = undoStack.removeLastOrNull() ?: return timeline
        val result = command.undo(timeline)
        redoStack.addLast(command)
        return result
    }

    /**
     * 重做
     */
    fun redo(timeline: Timeline): Timeline {
        val command = redoStack.removeLastOrNull() ?: return timeline
        val result = command.execute(timeline)
        undoStack.addLast(command)
        return result
    }

    /**
     * 清空栈
     */
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}

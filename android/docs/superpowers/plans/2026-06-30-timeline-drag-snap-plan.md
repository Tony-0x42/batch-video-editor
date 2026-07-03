# 时间轴拖拽、重叠自动换轨与边缘吸附实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 Android 视频编辑器实现时间轴片段拖拽、重叠自动提升到新视频轨道、以及 12 dp 边缘吸附功能。

**Architecture:** 在 `Track` 中增加 `index` 字段以支持同类型多轨道；由 `MoveClipCommand` 内部根据目标起始时间和重叠检测决定最终轨道；UI 层仅负责手势识别、实时吸附反馈和按轨道列表渲染。

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, JUnit 4, MockK, Gradle

## Global Constraints

- 所有公共 API 必须有 KDoc。
- 新模块/文件优先使用 Kotlin。
- 时间单位统一为微秒（`Long`，us）。
- `Clip`、`Track`、`Timeline` 均为不可变 data class，修改返回新对象。
- 所有会改变 Timeline 状态的操作必须封装为 `EditorCommand`。
- 禁止在主线程执行耗时操作。
- 遵循 `spec/project-guidelines.md` 中的命名与格式规范。
- 时间线操作后 UI 反馈 ≤ 100ms。

---

## File Structure

| 文件 | 动作 | 职责 |
|------|------|------|
| `app/src/main/java/com/example/cj/videoeditor/domain/model/Track.kt` | 修改 | 给 `Track` 密封类及所有子类增加 `index: Int` 字段 |
| `app/src/main/java/com/example/cj/videoeditor/domain/model/Timeline.kt` | 修改 | 增加轨道查找、lane 管理、重叠检测、吸附计算辅助方法 |
| `app/src/main/java/com/example/cj/videoeditor/domain/model/TimelineConstants.kt` | 新建 | 吸附阈值、时间轴宽度等常量 |
| `app/src/main/java/com/example/cj/videoeditor/domain/model/command/ClipCommands.kt` | 修改 | 重写 `MoveClipCommand`：按目标时间移动并自动提升轨道 |
| `app/src/main/java/com/example/cj/videoeditor/domain/usecase/editor/EditorUseCase.kt` | 修改 | 更新 `moveClip` 方法签名 |
| `app/src/main/java/com/example/cj/videoeditor/presentation/editor/EditorViewModel.kt` | 修改 | 更新 `moveClip` 方法签名 |
| `app/src/main/java/com/example/cj/videoeditor/presentation/media/MediaPickerViewModel.kt` | 修改 | 导入时显式使用 `index = 0` |
| `app/src/main/java/com/example/cj/videoeditor/ui/editor/EditorScreen.kt` | 修改 | 添加 Clip 拖拽、吸附线、多轨道渲染 |
| `app/src/test/java/com/example/cj/videoeditor/domain/model/TimelineOverlapTest.kt` | 新建 | 重叠检测与吸附计算单元测试 |
| `app/src/test/java/com/example/cj/videoeditor/domain/model/command/MoveClipCommandTest.kt` | 新建 | MoveClipCommand 单元测试 |
| `app/src/test/java/com/example/cj/videoeditor/domain/model/ClipTest.kt` | 修改 | 如有必要，更新 Clip 相关测试 |
| `app/src/test/java/com/example/cj/videoeditor/domain/model/TimelineTest.kt` | 修改 | 如有必要，更新 Timeline 相关测试 |
| `app/src/test/java/com/example/cj/videoeditor/domain/model/command/ClipCommandsTest.kt` | 修改 | 更新现有 MoveClipCommand 测试 |
| `app/src/test/java/com/example/cj/videoeditor/presentation/editor/EditorViewModelTest.kt` | 修改 | 更新 ViewModel moveClip 测试 |

---

## Task 1: 给 `Track` 增加 `index` 字段

**Files:**
- Modify: `app/src/main/java/com/example/cj/videoeditor/domain/model/Track.kt`
- Test: `app/src/test/java/com/example/cj/videoeditor/domain/model/TimelineTest.kt`

**Interfaces:**
- Consumes: 无
- Produces: `Track` 密封类及子类均携带 `index: Int`，默认值为 `0`。

- [ ] **Step 1: 修改 `Track.kt`**

将 `Track` 改为：

```kotlin
sealed class Track(
    open val id: String,
    open val type: TrackType,
    open val index: Int,
    open val clips: List<Clip>,
    open val isLocked: Boolean = false,
    open val isMuted: Boolean = false,
    open val isVisible: Boolean = true
) {
    /**
     * 轨道结束时间（所有片段最晚结束）
     */
    val endTimeUs: Long
        get() = clips.maxOfOrNull { it.endTimeUs } ?: 0L
}
```

给所有子类增加 `index` 参数：

```kotlin
data class VideoTrack(
    override val id: String,
    override val index: Int = 0,
    override val clips: List<Clip> = emptyList(),
    override val isLocked: Boolean = false,
    override val isMuted: Boolean = false,
    override val isVisible: Boolean = true
) : Track(id, TrackType.VIDEO, index, clips, isLocked, isMuted, isVisible)

data class PipTrack(
    override val id: String,
    override val index: Int = 0,
    override val clips: List<Clip> = emptyList(),
    override val isLocked: Boolean = false,
    override val isMuted: Boolean = false,
    override val isVisible: Boolean = true
) : Track(id, TrackType.PIP, index, clips, isLocked, isMuted, isVisible)

data class AudioTrack(
    override val id: String,
    override val index: Int = 0,
    override val clips: List<Clip> = emptyList(),
    override val isLocked: Boolean = false,
    override val isMuted: Boolean = false,
    override val isVisible: Boolean = true
) : Track(id, TrackType.AUDIO, index, clips, isLocked, isMuted, isVisible)

data class TextTrack(
    override val id: String,
    override val index: Int = 0,
    override val clips: List<Clip> = emptyList(),
    override val isLocked: Boolean = false,
    override val isMuted: Boolean = false,
    override val isVisible: Boolean = true
) : Track(id, TrackType.TEXT, index, clips, isLocked, isMuted, isVisible)

data class StickerTrack(
    override val id: String,
    override val index: Int = 0,
    override val clips: List<Clip> = emptyList(),
    override val isLocked: Boolean = false,
    override val isMuted: Boolean = false,
    override val isVisible: Boolean = true
) : Track(id, TrackType.STICKER, index, clips, isLocked, isMuted, isVisible)
```

- [ ] **Step 2: 运行单元测试，修复因构造函数签名变化导致的编译错误**

Run: `./gradlew :app:testDebugUnitTest`

Expected: 编译通过；如 `TimelineTest.kt` 中显式构造 `Track` 子类，需补 `index = 0`。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/cj/videoeditor/domain/model/Track.kt
git commit -m "feat(timeline): add index field to Track and all subclasses"
```

---

## Task 2: 新增时间轴常量文件

**Files:**
- Create: `app/src/main/java/com/example/cj/videoeditor/domain/model/TimelineConstants.kt`
- Test: 无

**Interfaces:**
- Consumes: 无
- Produces: `TimelineConstants.SNAP_THRESHOLD_DP`, `TimelineConstants.TIMELINE_WIDTH_DP`。

- [ ] **Step 1: 创建常量文件**

```kotlin
package com.example.cj.videoeditor.domain.model

/**
 * 时间轴相关常量。
 */
object TimelineConstants {
    /**
     * 边缘吸附阈值，单位 dp。
     */
    const val SNAP_THRESHOLD_DP = 12

    /**
     * 时间轴可视宽度，单位 dp。
     */
    const val TIMELINE_WIDTH_DP = 300
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/cj/videoeditor/domain/model/TimelineConstants.kt
git commit -m "feat(timeline): add TimelineConstants for snap threshold and width"
```

---

## Task 3: 在 `Timeline.kt` 增加辅助方法

**Files:**
- Modify: `app/src/main/java/com/example/cj/videoeditor/domain/model/Timeline.kt`
- Test: `app/src/test/java/com/example/cj/videoeditor/domain/model/TimelineOverlapTest.kt`

**Interfaces:**
- Consumes: `Track.index` 已存在
- Produces:
  - `Timeline.videoTracks(): List<VideoTrack>`
  - `Timeline.findClipTrack(clipId: String): Track?`
  - `Timeline.ensureVideoLane(index: Int): Timeline`
  - `Timeline.findLowestNonOverlappingLane(clip: Clip, startLane: Int = 0): Int`
  - `Timeline.resolveTargetTrack(clip: Clip, desiredStartTimeUs: Long): Pair<Timeline, String>`
  - `Clip.overlapsWith(other: Clip): Boolean`
  - `Timeline.snapTargetStartTimeUs(clip: Clip, rawStartTimeUs: Long, thresholdUs: Long): Long`
  - `Timeline.activeSnapTargetsFor(clip: Clip): List<Long>`

- [ ] **Step 1: 在 `Timeline.kt` 中追加扩展/成员方法**

```kotlin
/**
 * 返回所有视频轨道，按 index 升序。
 */
fun Timeline.videoTracks(): List<VideoTrack> =
    tracks.filterIsInstance<VideoTrack>().sortedBy { it.index }

/**
 * 根据片段 ID 查找所在轨道。
 */
fun Timeline.findClipTrack(clipId: String): Track? =
    tracks.find { track -> track.clips.any { it.id == clipId } }

/**
 * 如果指定 index 的视频轨道不存在，则创建一条新的。
 */
fun Timeline.ensureVideoLane(index: Int): Timeline {
    val existing = videoTracks().any { it.index == index }
    if (existing) return this
    val newTrack = VideoTrack(
        id = "track_video_lane_${index}",
        index = index,
        clips = emptyList()
    )
    return copy(tracks = tracks + newTrack)
}

/**
 * 找到不会与目标片段重叠的最低视频 lane index。
 */
fun Timeline.findLowestNonOverlappingLane(
    clip: Clip,
    startLane: Int = 0
): Int {
    val laneClips = videoTracks()
        .firstOrNull { it.index == startLane }
        ?.clips
        ?.filter { it.id != clip.id }
        ?: emptyList()

    val hasOverlap = laneClips.any { it.overlapsWith(clip) }
    return if (!hasOverlap) {
        startLane
    } else {
        findLowestNonOverlappingLane(clip, startLane + 1)
    }
}

/**
 * 解析目标片段应落入的轨道，并返回已确保该轨道存在的新 Timeline 与轨道 ID。
 */
fun Timeline.resolveTargetTrack(
    clip: Clip,
    desiredStartTimeUs: Long
): Pair<Timeline, String> {
    val shiftedClip = clip.copyWithTime(startTimeUs = desiredStartTimeUs)
    val targetIndex = findLowestNonOverlappingLane(shiftedClip, 0)
    val updated = ensureVideoLane(targetIndex)
    val targetTrack = updated.videoTracks().first { it.index == targetIndex }
    return updated to targetTrack.id
}

/**
 * 判断两个片段时间范围是否重叠。首尾相接不算重叠。
 */
fun Clip.overlapsWith(other: Clip): Boolean =
    this.startTimeUs < other.endTimeUs && this.endTimeUs > other.startTimeUs

/**
 * 计算目标片段起始时间的吸附目标列表（P0：同轨道前后片段边界 + 时间轴起点）。
 */
fun Timeline.activeSnapTargetsFor(clip: Clip): List<Long> {
    val track = findClipTrack(clip.id) ?: return listOf(0L)
    val targets = mutableListOf<Long>()
    targets.add(0L)
    track.clips.filter { it.id != clip.id }.forEach {
        targets.add(it.startTimeUs)
        targets.add(it.endTimeUs)
    }
    return targets.distinct()
}

/**
 * 根据吸附目标，返回吸附后的起始时间。若未命中任何目标则返回 rawStartTimeUs。
 */
fun Timeline.snapTargetStartTimeUs(
    clip: Clip,
    rawStartTimeUs: Long,
    thresholdUs: Long
): Long {
    val targets = activeSnapTargetsFor(clip)
    val shiftedClip = clip.copyWithTime(startTimeUs = rawStartTimeUs)
    return targets
        .map { target -> target to kotlin.math.abs(target - shiftedClip.startTimeUs) }
        .filter { it.second <= thresholdUs }
        .minByOrNull { it.second }
        ?.let { targetDelta ->
            // 吸附后保持原有时长
            val duration = clip.timelineDurationUs
            clip.copyWithTime(
                startTimeUs = targetDelta.first,
                endTimeUs = targetDelta.first + duration
            ).startTimeUs
        }
        ?: rawStartTimeUs
}
```

- [ ] **Step 2: 新建测试文件 `TimelineOverlapTest.kt`**

```kotlin
package com.example.cj.videoeditor.domain.model

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TimelineOverlapTest {

    private val uri: Uri = Uri.parse("content://test/video")

    private fun videoClip(
        id: String,
        trackId: String,
        startTimeUs: Long,
        endTimeUs: Long
    ): VideoClip = VideoClip(
        id = id,
        trackId = trackId,
        startTimeUs = startTimeUs,
        endTimeUs = endTimeUs,
        sourceStartUs = 0L,
        sourceEndUs = endTimeUs - startTimeUs,
        uri = uri
    )

    private fun baseTimeline(): Timeline = Timeline(
        tracks = listOf(
            VideoTrack(
                id = "track_video_1",
                index = 0,
                clips = listOf(
                    videoClip("c1", "track_video_1", 0L, 3_000_000L),
                    videoClip("c2", "track_video_1", 3_000_000L, 6_000_000L)
                )
            )
        )
    )

    @Test
    fun `overlapsWith returns false for back-to-back clips`() {
        val a = videoClip("a", "t", 0L, 3_000_000L)
        val b = videoClip("b", "t", 3_000_000L, 6_000_000L)
        assertFalse(a.overlapsWith(b))
    }

    @Test
    fun `overlapsWith returns true when clips intersect`() {
        val a = videoClip("a", "t", 0L, 3_000_000L)
        val b = videoClip("b", "t", 2_000_000L, 5_000_000L)
        assertTrue(a.overlapsWith(b))
    }

    @Test
    fun `findLowestNonOverlappingLane returns 0 when no overlap`() {
        val timeline = baseTimeline()
        val clip = videoClip("c3", "track_video_1", 6_000_000L, 9_000_000L)
        assertEquals(0, timeline.findLowestNonOverlappingLane(clip, 0))
    }

    @Test
    fun `findLowestNonOverlappingLane returns 1 when overlaps main lane`() {
        val timeline = baseTimeline()
        val clip = videoClip("c3", "track_video_1", 1_000_000L, 4_000_000L)
        assertEquals(1, timeline.findLowestNonOverlappingLane(clip, 0))
    }

    @Test
    fun `ensureVideoLane creates new track when index missing`() {
        val timeline = baseTimeline()
        val updated = timeline.ensureVideoLane(1)
        assertEquals(2, updated.videoTracks().size)
        assertEquals(1, updated.videoTracks().first { it.index == 1 }.index)
    }

    @Test
    fun `snapTargetStartTimeUs snaps to previous clip end`() {
        val timeline = baseTimeline()
        val clip = videoClip("c2", "track_video_1", 5_900_000L, 8_900_000L)
        val snapped = timeline.snapTargetStartTimeUs(clip, 5_900_000L, 200_000L)
        assertEquals(3_000_000L, snapped)
    }

    @Test
    fun `snapTargetStartTimeUs keeps raw when out of threshold`() {
        val timeline = baseTimeline()
        val clip = videoClip("c2", "track_video_1", 5_000_000L, 8_000_000L)
        val snapped = timeline.snapTargetStartTimeUs(clip, 5_000_000L, 100_000L)
        assertEquals(5_000_000L, snapped)
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.cj.videoeditor.domain.model.TimelineOverlapTest"`

Expected: 6 tests PASS。

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/cj/videoeditor/domain/model/Timeline.kt

git add app/src/test/java/com/example/cj/videoeditor/domain/model/TimelineOverlapTest.kt
git commit -m "feat(timeline): add lane helpers, overlap detection and snap logic"
```

---

## Task 4: 重写 `MoveClipCommand`

**Files:**
- Modify: `app/src/main/java/com/example/cj/videoeditor/domain/model/command/ClipCommands.kt`
- Test: `app/src/test/java/com/example/cj/videoeditor/domain/model/command/MoveClipCommandTest.kt`

**Interfaces:**
- Consumes: `Timeline.resolveTargetTrack`, `findClipTrack`, `ensureVideoLane`, `Clip.copyWithTime`
- Produces: `MoveClipCommand(clipId: String, targetStartTimeUs: Long)` 执行后会将片段移动到无重叠的最低 lane。

- [ ] **Step 1: 重写 `MoveClipCommand`**

替换现有 `MoveClipCommand` 为：

```kotlin
/**
 * 移动片段命令。
 *
 * 将指定片段移动到目标起始时间。若目标位置与同轨道其他片段重叠，
 * 则自动将其提升到新的视频轨道（画中画层）。
 */
class MoveClipCommand(
    private val clipId: String,
    private val targetStartTimeUs: Long
) : EditorCommand {

    override val name: String = "移动片段"

    private var beforeTimeline: Timeline? = null

    override fun execute(timeline: Timeline): Timeline {
        beforeTimeline = timeline
        val sourceTrack = timeline.findClipTrack(clipId) ?: return timeline
        val clip = sourceTrack.clips.find { it.id == clipId } ?: return timeline

        // 从原轨道移除
        val sourceClips = sourceTrack.clips.filter { it.id != clipId }
        val timelineWithoutClip = timeline.updateTrack(
            updateTrackClips(sourceTrack, sourceClips)
        ).let { cleanEmptyVideoLanes(it) }

        // 确定目标轨道（自动提升）
        val (timelineWithLane, targetTrackId) = timelineWithoutClip.resolveTargetTrack(
            clip,
            targetStartTimeUs.coerceAtLeast(0L)
        )
        val targetTrack = timelineWithLane.tracks.find { it.id == targetTrackId }
            ?: return timeline

        // 将片段放入目标轨道并保持按 startTimeUs 升序
        val movedClip = clip.copyWithTime(
            startTimeUs = targetStartTimeUs.coerceAtLeast(0L),
            endTimeUs = targetStartTimeUs.coerceAtLeast(0L) + clip.timelineDurationUs
        ).let { updateClipTrackId(it, targetTrackId) }

        val targetClips = (targetTrack.clips + movedClip)
            .sortedBy { it.startTimeUs }

        val finalTimeline = timelineWithLane
            .updateTrack(updateTrackClips(targetTrack, targetClips))
            .let { cleanEmptyVideoLanes(it) }

        return finalTimeline
    }

    override fun undo(timeline: Timeline): Timeline {
        return beforeTimeline ?: timeline
    }
}

/**
 * 清理空的非主视频轨道。
 */
private fun cleanEmptyVideoLanes(timeline: Timeline): Timeline {
    val cleanedTracks = timeline.tracks.filter { track ->
        if (track !is VideoTrack) return@filter true
        if (track.index == 0) return@filter true
        track.clips.isNotEmpty()
    }
    return if (cleanedTracks.size == timeline.tracks.size) {
        timeline
    } else {
        timeline.copy(tracks = cleanedTracks)
    }
}

/**
 * 更新 Clip 的 trackId。由于 Clip 是密封类，需要为每种类型处理。
 */
private fun updateClipTrackId(clip: Clip, newTrackId: String): Clip {
    return when (clip) {
        is VideoClip -> clip.copy(trackId = newTrackId)
        is AudioClip -> clip.copy(trackId = newTrackId)
        is ImageClip -> clip.copy(trackId = newTrackId)
        is TextClip -> clip.copy(trackId = newTrackId)
    }
}
```

- [ ] **Step 2: 新建 `MoveClipCommandTest.kt`**

```kotlin
package com.example.cj.videoeditor.domain.model.command

import android.net.Uri
import com.example.cj.videoeditor.domain.model.Timeline
import com.example.cj.videoeditor.domain.model.VideoClip
import com.example.cj.videoeditor.domain.model.VideoTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MoveClipCommandTest {

    private val uri: Uri = Uri.parse("content://test/video")

    private fun videoClip(
        id: String,
        trackId: String,
        startTimeUs: Long,
        endTimeUs: Long
    ): VideoClip = VideoClip(
        id = id,
        trackId = trackId,
        startTimeUs = startTimeUs,
        endTimeUs = endTimeUs,
        sourceStartUs = 0L,
        sourceEndUs = endTimeUs - startTimeUs,
        uri = uri
    )

    private fun baseTimeline(): Timeline = Timeline(
        tracks = listOf(
            VideoTrack(
                id = "track_video_1",
                index = 0,
                clips = listOf(
                    videoClip("c1", "track_video_1", 0L, 3_000_000L),
                    videoClip("c2", "track_video_1", 3_000_000L, 6_000_000L)
                )
            )
        )
    )

    @Test
    fun `move clip within main lane keeps same track`() {
        val timeline = baseTimeline()
        val command = MoveClipCommand("c2", 8_000_000L)
        val result = command.execute(timeline)

        val mainTrack = result.videoTracks().first { it.index == 0 }
        assertEquals(1, result.videoTracks().size)
        val movedClip = mainTrack.clips.first { it.id == "c2" }
        assertEquals(8_000_000L, movedClip.startTimeUs)
        assertEquals(11_000_000L, movedClip.endTimeUs)
    }

    @Test
    fun `move clip to overlap creates new video lane`() {
        val timeline = baseTimeline()
        val command = MoveClipCommand("c2", 1_000_000L)
        val result = command.execute(timeline)

        assertEquals(2, result.videoTracks().size)
        val laneOne = result.videoTracks().first { it.index == 1 }
        assertEquals(1, laneOne.clips.size)
        assertEquals("c2", laneOne.clips.first().id)
        assertEquals("track_video_lane_1", laneOne.clips.first().trackId)
    }

    @Test
    fun `undo restores original timeline`() {
        val timeline = baseTimeline()
        val command = MoveClipCommand("c2", 1_000_000L)
        val after = command.execute(timeline)
        val undone = command.undo(after)

        assertEquals(timeline, undone)
    }

    @Test
    fun `move clip back to main lane removes empty pip lane`() {
        val timeline = baseTimeline()
        val moveToPip = MoveClipCommand("c2", 1_000_000L)
        val pipTimeline = moveToPip.execute(timeline)

        val moveBack = MoveClipCommand("c2", 8_000_000L)
        val finalTimeline = moveBack.execute(pipTimeline)

        assertEquals(1, finalTimeline.videoTracks().size)
        assertNull(finalTimeline.videoTracks().firstOrNull { it.index == 1 })
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.cj.videoeditor.domain.model.command.MoveClipCommandTest"`

Expected: 4 tests PASS。

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/cj/videoeditor/domain/model/command/ClipCommands.kt

git add app/src/test/java/com/example/cj/videoeditor/domain/model/command/MoveClipCommandTest.kt
git commit -m "feat(timeline): rewrite MoveClipCommand with auto lane promotion and undo snapshot"
```

---

## Task 5: 更新 UseCase 与 ViewModel 接口

**Files:**
- Modify: `app/src/main/java/com/example/cj/videoeditor/domain/usecase/editor/EditorUseCase.kt`
- Modify: `app/src/main/java/com/example/cj/videoeditor/presentation/editor/EditorViewModel.kt`
- Test: `app/src/test/java/com/example/cj/videoeditor/presentation/editor/EditorViewModelTest.kt`

**Interfaces:**
- Consumes: `MoveClipCommand(clipId, targetStartTimeUs)`
- Produces:
  - `EditorUseCase.moveClip(clipId: String, targetStartTimeUs: Long)`
  - `EditorViewModel.moveClip(clipId: String, targetStartTimeUs: Long)`

- [ ] **Step 1: 修改 `EditorUseCase.kt`**

将 `moveClip` 改为：

```kotlin
suspend fun moveClip(clipId: String, targetStartTimeUs: Long) {
    executeCommand(MoveClipCommand(clipId, targetStartTimeUs))
}
```

- [ ] **Step 2: 修改 `EditorViewModel.kt`**

将 `moveClip` 改为：

```kotlin
fun moveClip(clipId: String, targetStartTimeUs: Long) {
    viewModelScope.launch {
        editorUseCase.moveClip(clipId, targetStartTimeUs)
    }
}
```

- [ ] **Step 3: 更新 `EditorViewModelTest.kt` 中的 moveClip 调用**

查找现有测试：

Run: `grep -n "moveClip" app/src/test/java/com/example/cj/videoeditor/presentation/editor/EditorViewModelTest.kt`

若存在对 `moveClip(clipId, targetIndex)` 的调用，改为 `moveClip(clipId, targetStartTimeUs)`。若不存在，新增一个测试：

```kotlin
@Test
fun `moveClip forwards target start time to use case`() = runTest {
    // Arrange
    val clipId = "clip_1"
    val targetStartUs = 5_000_000L

    // Act
    viewModel.moveClip(clipId, targetStartUs)
    advanceUntilIdle()

    // Assert
    coVerify { editorUseCase.moveClip(clipId, targetStartUs) }
}
```

- [ ] **Step 4: 运行测试**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.cj.videoeditor.presentation.editor.EditorViewModelTest"`

Expected: 测试 PASS。

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/cj/videoeditor/domain/usecase/editor/EditorUseCase.kt

git add app/src/main/java/com/example/cj/videoeditor/presentation/editor/EditorViewModel.kt

git add app/src/test/java/com/example/cj/videoeditor/presentation/editor/EditorViewModelTest.kt
git commit -m "feat(timeline): update moveClip signatures in use case and view model"
```

---

## Task 6: 更新导入逻辑以显式使用 `index = 0`

**Files:**
- Modify: `app/src/main/java/com/example/cj/videoeditor/presentation/media/MediaPickerViewModel.kt`
- Test: `app/src/test/java/com/example/cj/videoeditor/presentation/media/MediaPickerViewModelTest.kt`

**Interfaces:**
- Consumes: `VideoTrack` 和 `AudioTrack` 的 `index` 构造函数参数
- Produces: 导入时创建的轨道均显式 `index = 0`

- [ ] **Step 1: 修改 `MediaPickerViewModel.kt`**

在 `createTimelineFromUris` 中：

```kotlin
return Timeline(
    tracks = listOf(
        VideoTrack(
            id = "track_video_1",
            index = 0,
            clips = videoClips
        ),
        AudioTrack(
            id = "track_audio_1",
            index = 0,
            clips = audioClips
        )
    )
)
```

- [ ] **Step 2: 运行相关测试**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.cj.videoeditor.presentation.media.MediaPickerViewModelTest"`

Expected: PASS。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/cj/videoeditor/presentation/media/MediaPickerViewModel.kt

git add app/src/test/java/com/example/cj/videoeditor/presentation/media/MediaPickerViewModelTest.kt
git commit -m "feat(timeline): explicitly set track index=0 during media import"
```

---

## Task 7: 在 UI 实现 Clip 拖拽、吸附线与多轨道渲染

**Files:**
- Modify: `app/src/main/java/com/example/cj/videoeditor/ui/editor/EditorScreen.kt`
- Test: 手动运行 App 验证

**Interfaces:**
- Consumes:
  - `EditorViewModel.moveClip(clipId, targetStartTimeUs)`
  - `TimelineConstants.SNAP_THRESHOLD_DP`
  - `TimelineConstants.TIMELINE_WIDTH_DP`
  - `Timeline.snapTargetStartTimeUs`
- Produces:
  - `ClipItem` 支持水平拖拽手势
  - `TrackRow` 显示轨道标签与吸附线
  - `TimelinePanel` 处理 `onClipMoveStart/End` 回调

- [ ] **Step 1: 修改 `ClipItem` 支持拖拽**

将 `ClipItem` 改为：

```kotlin
@Composable
private fun ClipItem(
    clip: Clip,
    isSelected: Boolean,
    trackDurationUs: Long,
    zoomLevel: Float,
    onClick: () -> Unit,
    onMoveStart: () -> Unit,
    onMoveEnd: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val trackWidthPx = with(density) { (TimelineConstants.TIMELINE_WIDTH_DP * zoomLevel).dp.toPx() }
    val startRatio = clip.startTimeUs.toFloat() / trackDurationUs
    val durationRatio = clip.timelineDurationUs.toFloat() / trackDurationUs
    val clipColor = when (clip) {
        is VideoClip -> VideoEditorColors.TimelineTrackVideo
        is AudioClip -> VideoEditorColors.TimelineTrackAudio
        is TextClip -> VideoEditorColors.TimelineTrackText
        is ImageClip -> VideoEditorColors.TimelineTrackPip
    }

    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(start = (startRatio * TimelineConstants.TIMELINE_WIDTH_DP * zoomLevel).dp)
            .width((durationRatio * TimelineConstants.TIMELINE_WIDTH_DP * zoomLevel).dp.coerceAtLeast(40.dp))
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(clipColor.copy(alpha = if (isSelected || isDragging) 1f else 0.8f))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isDragging) VideoEditorColors.Primary else Color.White,
                shape = RoundedCornerShape(6.dp)
            )
            .pointerInput(trackWidthPx, trackDurationUs) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragOffsetX = 0f
                        onMoveStart()
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount
                    },
                    onDragEnd = {
                        isDragging = false
                        val deltaUs = (dragOffsetX / trackWidthPx * trackDurationUs).toLong()
                        val rawTargetStartUs = (clip.startTimeUs + deltaUs).coerceAtLeast(0L)
                        onMoveEnd(rawTargetStartUs)
                        dragOffsetX = 0f
                    }
                )
            }
            .clickable {
                Log.e("ClipItem", "Clicked clip ${clip.id} (${clip::class.simpleName})")
                onClick()
            }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = clipLabel(clip),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            maxLines = 1
        )
    }
}
```

- [ ] **Step 2: 修改 `TrackRow` 增加轨道标签、吸附线与拖拽回调**

```kotlin
@Composable
private fun TrackRow(
    track: Track,
    selectedClipId: String?,
    onClipSelected: (String) -> Unit,
    onClipMoveStart: (String) -> Unit,
    onClipMoveEnd: (String, Long) -> Unit,
    onSeek: (Long) -> Unit,
    zoomLevel: Float,
    currentPositionUs: Long,
    totalDurationUs: Long,
    snapIndicatorPositionX: Float?,     // null 表示不显示吸附线
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val virtualWidthPx = with(density) { (TimelineConstants.TIMELINE_WIDTH_DP * zoomLevel).dp.toPx() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = trackLabel(track),
            style = MaterialTheme.typography.labelSmall,
            color = VideoEditorColors.OnSurfaceVariant,
            modifier = Modifier.width(64.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(VideoEditorColors.Surface)
        ) {
            track.clips.forEach { clip ->
                ClipItem(
                    clip = clip,
                    isSelected = clip.id == selectedClipId,
                    trackDurationUs = track.endTimeUs.coerceAtLeast(1L),
                    zoomLevel = zoomLevel,
                    onClick = { onClipSelected(clip.id) },
                    onMoveStart = { onClipMoveStart(clip.id) },
                    onMoveEnd = { targetStartUs -> onClipMoveEnd(clip.id, targetStartUs) },
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            // 吸附线
            if (snapIndicatorPositionX != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = with(density) { snapIndicatorPositionX.toDp() })
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.8f))
                )
            }

            // 播放头
            if (totalDurationUs > 0) {
                val playheadRatio = (currentPositionUs.toFloat() / totalDurationUs.toFloat())
                    .coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = (playheadRatio * TimelineConstants.TIMELINE_WIDTH_DP * zoomLevel).dp)
                        .width(20.dp)
                        .fillMaxHeight()
                        .pointerInput(virtualWidthPx, totalDurationUs) {
                            var currentRatio = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { currentRatio = playheadRatio },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    currentRatio = (currentRatio + dragAmount / virtualWidthPx)
                                        .coerceIn(0f, 1f)
                                    val newPositionUs = (currentRatio * totalDurationUs).toLong()
                                    onSeek(newPositionUs)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(Color.White)
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: 更新 `trackLabel` 支持画中画标签**

```kotlin
private fun trackLabel(track: Track): String {
    return when (track) {
        is VideoTrack -> if (track.index == 0) "主视频" else "画中画 ${track.index}"
        is PipTrack -> "画中画"
        is AudioTrack -> "音频"
        is TextTrack -> "文字"
        is StickerTrack -> "贴纸"
    }
}
```

- [ ] **Step 4: 修改 `TimelinePanel` 管理拖拽状态与吸附线**

```kotlin
@Composable
private fun TimelinePanel(
    timeline: Timeline,
    playbackPositionUs: Long,
    selectedClipId: String?,
    onClipSelected: (String) -> Unit,
    onClipMoveEnd: (String, Long) -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var zoomLevel by remember { mutableFloatStateOf(1f) }
    var draggingClipId by remember { mutableStateOf<String?>(null) }
    var snapPositionX by remember { mutableStateOf<Float?>(null) }

    val density = LocalDensity.current
    val trackWidthPx = with(density) { (TimelineConstants.TIMELINE_WIDTH_DP * zoomLevel).dp.toPx() }
    val totalDurationUs = timeline.durationUs.coerceAtLeast(1L)
    val snapThresholdPx = with(density) { TimelineConstants.SNAP_THRESHOLD_DP.dp.toPx() }
    val snapThresholdUs = (snapThresholdPx / trackWidthPx * totalDurationUs).toLong()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(VideoEditorColors.TimelineBackground)
            .padding(vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = "时间轴",
                style = MaterialTheme.typography.labelLarge,
                color = VideoEditorColors.OnSurface
            )
            Text(
                text = "总时长: ${formatTimeUs(timeline.durationUs)}",
                style = MaterialTheme.typography.labelMedium,
                color = VideoEditorColors.OnSurfaceVariant
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(timeline.tracks) { track ->
                TrackRow(
                    track = track,
                    selectedClipId = selectedClipId,
                    onClipSelected = onClipSelected,
                    onClipMoveStart = { clipId ->
                        draggingClipId = clipId
                        snapPositionX = null
                    },
                    onClipMoveEnd = { clipId, rawStartUs ->
                        draggingClipId = null
                        snapPositionX = null
                        val clip = timeline.tracks
                            .flatMap { it.clips }
                            .firstOrNull { it.id == clipId }
                            ?: return@TrackRow
                        val snappedStartUs = timeline.snapTargetStartTimeUs(
                            clip,
                            rawStartUs,
                            snapThresholdUs
                        )
                        onClipMoveEnd(clipId, snappedStartUs)
                    },
                    onSeek = onSeek,
                    zoomLevel = zoomLevel,
                    currentPositionUs = playbackPositionUs,
                    totalDurationUs = totalDurationUs,
                    snapIndicatorPositionX = snapPositionX
                )
            }

            if (timeline.tracks.isEmpty()) {
                item { EmptyTimelinePlaceholder() }
            }
        }
    }
}
```

> 注：P0 阶段吸附线位置计算可留待 Task 8 细化；上述代码中 `snapPositionX` 由外部管理，后续可基于实时拖拽偏移计算。

- [ ] **Step 5: 更新 `EditorScreen` 中的 `TimelinePanel` 调用**

将：

```kotlin
TimelinePanel(
    timeline = uiState.timeline,
    playbackPositionUs = playbackPositionUs,
    selectedClipId = uiState.selectedClipId,
    onClipSelected = { clipId -> viewModel.selectClip(clipId) },
    onSeek = { positionUs -> ... },
    ...
)
```

改为：

```kotlin
TimelinePanel(
    timeline = uiState.timeline,
    playbackPositionUs = playbackPositionUs,
    selectedClipId = uiState.selectedClipId,
    onClipSelected = { clipId -> viewModel.selectClip(clipId) },
    onClipMoveEnd = { clipId, targetStartUs ->
        viewModel.moveClip(clipId, targetStartUs)
    },
    onSeek = { positionUs ->
        playbackPositionUs = positionUs
        viewModel.seekTo(positionUs)
    },
    modifier = Modifier
        .fillMaxWidth()
        .weight(1.2f)
)
```

- [ ] **Step 6: 运行编译**

Run: `./gradlew :app:compileDebugKotlin`

Expected: 编译通过。

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/cj/videoeditor/ui/editor/EditorScreen.kt
git commit -m "feat(editor): add clip drag, snap indicator and multi-lane track rendering"
```

---

## Task 8: 实时吸附反馈优化（可选，P0 完成后）

**Files:**
- Modify: `app/src/main/java/com/example/cj/videoeditor/ui/editor/EditorScreen.kt`
- Test: 手动运行 App 验证

**Interfaces:**
- Consumes: `Timeline.snapTargetStartTimeUs`
- Produces: 拖拽过程中实时显示吸附线位置

- [ ] **Step 1: 在 `ClipItem` 的 `onHorizontalDrag` 中实时计算吸附位置**

将 `onHorizontalDrag` 改为：

```kotlin
onHorizontalDrag = { change, dragAmount ->
    change.consume()
    dragOffsetX += dragAmount
    val deltaUs = (dragOffsetX / trackWidthPx * trackDurationUs).toLong()
    val rawTargetStartUs = (clip.startTimeUs + deltaUs).coerceAtLeast(0L)
    val snappedStartUs = timeline.snapTargetStartTimeUs(
        clip,
        rawTargetStartUs,
        snapThresholdUs
    )
    // 将吸附时间转回像素位置，通过回调通知 TrackRow/TimelinePanel
    val snapRatio = snappedStartUs.toFloat() / trackDurationUs
    onSnapPositionChanged(snapRatio * trackWidthPx)
}
```

- [ ] **Step 2: 通过回调链将吸附位置上传到 `TimelinePanel`**

在 `ClipItem` 增加 `onSnapPositionChanged: (Float?) -> Unit`，`TrackRow` 透传，`TimelinePanel` 汇总并显示。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/cj/videoeditor/ui/editor/EditorScreen.kt
git commit -m "feat(editor): real-time snap indicator during clip drag"
```

---

## Task 9: 全量测试与手动验证

**Files:**
- All modified files
- Test: 单元测试 + 手动运行

- [ ] **Step 1: 运行全量单元测试**

Run: `./gradlew :app:testDebugUnitTest`

Expected: 所有测试 PASS。

- [ ] **Step 2: 手动运行 App 验证以下场景**

1. 选择 2 段视频进入编辑器，时间轴上两个片段首尾相接、不重叠。
2. 长按/拖拽第二个片段向左越过第一个片段结尾，释放后下方出现「画中画 1」轨道。
3. 缓慢拖动第二个片段靠近第一个结尾，距 12 dp 内时自动吸附拼接。
4. 点击「撤销」，时间轴恢复到拖拽前状态。
5. 把画中画层片段拖回主轨空位，画中画轨道消失。

- [ ] **Step 3: 修复发现的问题并重新运行测试**

- [ ] **Step 4: Commit**

```bash
git commit -m "test(timeline): verify drag, snap and auto lane promotion"
```

---

## Self-Review Checklist

### Spec coverage

| 设计文档章节 | 对应 Task |
|-------------|----------|
| 4.1 `Track.index` 字段 | Task 1 |
| 4.2 Timeline 辅助方法 | Task 3 |
| 4.3 导入时首尾相接 | Task 6 |
| 5 拖拽交互 | Task 7 |
| 6 边缘吸附 | Task 3, Task 7, Task 8 |
| 7 重叠检测与自动换轨 | Task 3, Task 4 |
| 8 UI 渲染调整 | Task 7 |
| 9 命令封装 | Task 4 |
| 10 ViewModel/UseCase | Task 5 |

### Placeholder scan

- 无 TBD/TODO。
- 每个 Task 均包含具体代码与测试命令。
- 方法签名在 Task 间保持一致。

### Type consistency

- `MoveClipCommand(clipId, targetStartTimeUs)` 在 Task 4、5、7 中一致。
- `Track.index` 在 Task 1、3、4 中一致。
- `TimelineConstants` 在 Task 2、7 中一致。

---

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-06-30-timeline-drag-snap-plan.md`.**

Two execution options:

**1. Subagent-Driven (recommended)** - Dispatch a fresh subagent per task, review between tasks, fast iteration.

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints.

Which approach?

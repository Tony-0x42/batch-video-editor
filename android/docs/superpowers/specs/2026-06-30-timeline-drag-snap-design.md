# 时间轴拖拽、重叠自动换轨与边缘吸附设计

> 本设计文档解决多视频导入后时间轴片段重叠、拖拽 rearrange、自动提升到新轨道、以及边缘磁吸吸附的问题。

---

## 1. 背景与问题

当前代码在 `MediaPickerViewModel.createTimelineFromUris()` 中已经会把选中的视频按导入顺序首尾相接放入同一条 `VideoTrack`。但在 `EditorScreen` 的 `ClipItem` 中：

- 片段目前只响应 `clickable`，**没有拖拽手势**。
- 没有**重叠检测**与自动换轨道逻辑。
- 没有**边缘吸附（Snap）**。

因此用户拖拽片段时无法 rearrange，也无法自然实现「一个视频叠在另一个视频上时自动变成画中画层」的交互。

---

## 2. 设计目标

1. **初始导入不重叠**：多个视频默认按导入顺序在主视频轨首尾相接。
2. **拖拽重排**：用户可左右拖动片段改变其在时间线上的起始位置。
3. **重叠自动换轨**：当拖拽释放后，若片段与同轨道其他片段重叠，则自动将其提升到新的视频轨道（画中画层）。
4. **边缘吸附**：拖拽时当片段起始时间接近前一段结尾（或其他可吸附边界）时，按像素距离自动吸附，方便精确拼接。
5. **可撤销**：所有拖拽与轨道调整操作通过 `TimelineCommand` 入栈，支持 Undo/Redo。

---

## 3. 方案选择

| 方案 | 简述 | 优点 | 缺点 | 结论 |
|------|------|------|------|------|
| A：给 `Track` 增加 `index` 字段，允许同类型多轨道 | `VideoTrack` 可存在多个实例，`index=0` 为主轨，`index=1,2…` 为画中画层。 | 与 `spec/modules/timeline/README.md` 模型对齐；自然支持多层画中画；UI 按轨道列表直接渲染。 | 需要给 `Track` 加字段并更新序列化/比较逻辑。 | **推荐采用** |
| B：复用 `VideoTrack` + `PipTrack` | 主轨固定一条，重叠时移入 `PipTrack`。 | 与现有密封类语义一致。 | 多个画中画层扩展困难；层级语义不够灵活。 | 不采用 |
| C：给 `Clip` 加 `laneIndex` | 轨道只作类型容器，渲染时二次分组。 | 不改 `Track` 结构。 | 破坏「轨道是 Clip 容器」的语义；查询/排序/渲染更复杂。 | 不采用 |

**采用方案 A**。

---

## 4. 数据模型变更

### 4.1 `Track` 增加 `index` 字段

与模块规范对齐，给轨道增加层级字段：

```kotlin
sealed class Track(
    open val id: String,
    open val type: TrackType,
    open val index: Int,                // 新增：轨道层级。UI 渲染时数值小的在上，大的在下；视频合成时数值大的覆盖在小的之上
    open val clips: List<Clip>,
    open val isLocked: Boolean = false,
    open val isMuted: Boolean = false,
    open val isVisible: Boolean = true
) {
    val endTimeUs: Long
        get() = clips.maxOfOrNull { it.endTimeUs } ?: 0L
}
```

所有子类同步增加 `index` 参数，默认值为 `0`：

```kotlin
data class VideoTrack(
    override val id: String,
    override val index: Int = 0,
    override val clips: List<Clip> = emptyList(),
    override val isLocked: Boolean = false,
    override val isMuted: Boolean = false,
    override val isVisible: Boolean = true
) : Track(id, TrackType.VIDEO, index, clips, isLocked, isMuted, isVisible)
```

> `AudioTrack`、`TextTrack`、`PipTrack`、`StickerTrack` 同样增加 `index` 字段，本次改动以视频轨道为主，其他类型轨道先保留默认 `index=0`，为后续扩展预留。

### 4.2 `Timeline` 辅助方法

新增以下辅助方法（实现时补充到 `Timeline.kt`）：

- `fun videoTracks(): List<VideoTrack>`：返回所有视频轨道，按 `index` 升序。
- `fun nextVideoLaneIndex(): Int`：返回当前最大视频轨道 `index + 1`。
- `fun ensureVideoLane(index: Int): Timeline`：如果指定 `index` 的视频轨道不存在，则创建新的 `VideoTrack`。
- `fun findClipTrack(clipId: String): Track?`：根据片段 ID 查找所在轨道。

### 4.3 导入时默认布局

保持 `MediaPickerViewModel.createTimelineFromUris()` 现有逻辑：

- 所有视频片段放入 `VideoTrack(id = "track_video_1", index = 0)`。
- 对应音频片段放入 `AudioTrack(id = "track_audio_1", index = 0)`。
- 片段 `startTimeUs` 依次递增，保证首尾相接。

边界处理：
- 若某视频 duration 为 0，跳过该视频并记录警告日志。
- 导入后调用 `Timeline.recalculateDuration()`（如需要）。

---

## 5. 拖拽交互

### 5.1 手势实现

在 `ClipItem` 上增加 `pointerInput` + `detectHorizontalDragGestures`：

```kotlin
.detectHorizontalDragGestures(
    onDragStart = { /* 记录起始位置，提升视觉层级 */ },
    onHorizontalDrag = { change, dragAmount ->
        change.consume()
        // 计算临时偏移并更新本地状态
    },
    onDragEnd = {
        // 提交最终位置
    }
)
```

交互状态：
- 拖拽中：Clip 半透明 + 轻微阴影抬高，提示用户正在移动。
- 释放后：根据吸附和重叠检测结果更新 Timeline。

### 5.2 像素 ↔ 时间换算

时间轴宽度固定为 `300 * zoomLevel dp`，总时间 `totalDurationUs`：

```kotlin
fun pxToUs(px: Float, totalDurationUs: Long, trackWidthPx: Float): Long =
    (px / trackWidthPx * totalDurationUs).toLong()

fun usToPx(us: Long, totalDurationUs: Long, trackWidthPx: Float): Float =
    us.toFloat() / totalDurationUs * trackWidthPx
```

---

## 6. 边缘吸附（Snap）

### 6.1 吸附阈值

**默认吸附阈值：12 dp。**

选择理由：
- 移动端手指操作精度有限，12 dp 能在「容易命中」与「避免误吸附」之间取得平衡。
- 与常见剪辑应用（CapCut/VN/剪映）的磁吸手感接近。
- 该值作为 `TimelineConstants.SNAP_THRESHOLD_DP`，后续可在设置中暴露给用户调整。

将 12 dp 按当前缩放转换为时间：

```kotlin
val snapThresholdUs = pxToUs(
    px = with(density) { SNAP_THRESHOLD_DP.dp.toPx() },
    totalDurationUs = timeline.durationUs.coerceAtLeast(1L),
    trackWidthPx = with(density) { (300 * zoomLevel).dp.toPx() }
)
```

### 6.2 吸附目标

P0 阶段吸附目标：

1. **同轨道前一个片段的结束时间**（`prevClip.endTimeUs`）。
2. **同轨道后一个片段的开始时间**（`nextClip.startTimeUs`）。
3. **时间轴起点** `0`。

P1 可扩展目标：
- 其他视频轨道片段的 start/end。
- 播放头当前位置。
- 固定时间网格（如 1 秒刻度）。

### 6.3 吸附策略

- 拖拽过程中实时计算候选吸附点。
- 对每个候选点，计算片段起始时间到该点的绝对时间差。
- 若最小差值 ≤ `snapThresholdUs`，则吸附到该点。
- 若多个候选点同时满足，选择时间差最小的一个。
- 吸附反馈：显示垂直白色虚线吸附线，并给片段边框一个短暂高亮。

### 6.4 吸附示例

用户把第二个视频向第一个视频结尾拖动：
- 当两者间距在 12 dp 以内时，第二个片段的起始时间自动跳到第一个片段的 `endTimeUs`。
- 释放后片段停留在吸附位置，实现无缝拼接。

---

## 7. 重叠检测与自动换轨

### 7.1 重叠判定

两个片段重叠定义为时间范围存在交集：

```kotlin
fun Clip.overlapsWith(other: Clip): Boolean =
    this.startTimeUs < other.endTimeUs && this.endTimeUs > other.startTimeUs
```

注意：两个片段首尾相接（`endTimeUs == startTimeUs`）**不算重叠**。

### 7.2 自动提升算法

释放后，对目标片段执行以下逻辑：

```kotlin
fun resolveOverlap(
    timeline: Timeline,
    clip: Clip,
    desiredStartTimeUs: Long
): Pair<Timeline, String> {
    val targetIndex = findLowestNonOverlappingLane(
        timeline = timeline,
        clip = clip.copyWithTime(startTimeUs = desiredStartTimeUs),
        startLane = 0
    )
    val updated = timeline.ensureVideoLane(targetIndex)
    val targetTrack = updated.videoTracks().first { it.index == targetIndex }
    return updated to targetTrack.id
}

fun findLowestNonOverlappingLane(
    timeline: Timeline,
    clip: Clip,
    startLane: Int
): Int {
    val laneClips = timeline.videoTracks()
        .firstOrNull { it.index == startLane }
        ?.clips
        ?.filter { it.id != clip.id }
        ?: emptyList()

    val hasOverlap = laneClips.any { it.overlapsWith(clip) }
    return if (!hasOverlap) {
        startLane
    } else {
        findLowestNonOverlappingLane(timeline, clip, startLane + 1)
    }
}
```

算法说明：
1. 从目标 lane（通常为 0）开始检查。
2. 若该 lane 无重叠，片段停留/回到该 lane。
3. 若有重叠，尝试 `index + 1` 的 lane。
4. 递归直到找到无重叠 lane；若不存在则创建新 `VideoTrack`。

### 7.3 轨道降级

若用户把原本在画中画层的片段拖回主轨空位（无重叠），则将其移回 `index=0` 的 `VideoTrack`。

### 7.4 空轨道清理

当某条非主轨（`index > 0`）的 `VideoTrack` 变空时：
- 立即删除该空轨道，避免时间轴出现无意义的空行。
- 更高 index 的轨道是否需要重新编号？**不需要**，轨道 ID 保持不变，仅 `index` 用于排序显示。
- Undo 时需要能恢复被删除的空轨道。

---

## 8. UI 渲染调整

### 8.1 轨道行渲染

`TimelinePanel` 继续按 `timeline.tracks` 用 `LazyColumn` 渲染，`TrackRow` 增加轨道标签显示：

- `index=0` 的视频轨道标签为「主视频轨」。
- `index>0` 的视频轨道标签为「画中画 ${index}」。

```kotlin
private fun videoTrackLabel(track: VideoTrack): String =
    if (track.index == 0) "主视频轨" else "画中画 ${track.index}"
```

### 8.2 吸附线

在 `TrackRow` 或 `TimelinePanel` 顶层增加 `SnapIndicator`：

- 当拖拽中发生吸附时，在所有轨道行的同一 x 位置显示一条垂直白色虚线。
- 释放或拖拽离开吸附范围后隐藏。

### 8.3 拖拽视觉反馈

- 拖拽中 Clip：`alpha = 0.85f`，添加阴影/边框高亮。
- 被吸附时：边框颜色短暂变为 `VideoEditorColors.Primary`。
- 释放时：使用 `animateContentHeight` 让新轨道出现更自然。

---

## 9. 命令封装与 Undo/Redo

### 9.1 命令设计

新增/改造 `MoveClipCommand`：

```kotlin
class MoveClipCommand(
    private val clipId: String,
    private val targetStartTimeUs: Long
) : TimelineCommand {
    override val name: String = "移动片段"

    private var beforeTimeline: Timeline? = null
    private var afterTimeline: Timeline? = null

    override fun execute(timeline: Timeline): Timeline {
        beforeTimeline = timeline
        afterTimeline = computeMovedTimeline(timeline)
        return afterTimeline!!
    }

    override fun undo(timeline: Timeline): Timeline {
        return beforeTimeline ?: timeline
    }
}
```

> 采用「快照式」撤销：在 `execute` 时保存执行前的完整 `Timeline`，`undo` 直接恢复。实现简单、正确性高，且 `Timeline` 为不可变 data class，易于保存。

### 9.2 执行逻辑

`computeMovedTimeline(timeline)` 内部流程：

1. 找到 `clipId` 所在原轨道。
2. 从原轨道移除该 clip。
3. 根据目标 `targetStartTimeUs` 计算是否需要提升 lane（调用 `resolveOverlap`）。
4. 确保目标 lane 的 `VideoTrack` 存在。
5. 将 clip 以新的 `trackId` 和 `startTimeUs` 插入目标轨道，并保持轨道内按 `startTimeUs` 升序。
6. 清理空轨道。
7. 返回新的 `Timeline`。

### 9.3 撤销逻辑

直接返回 `beforeTimeline`，完整恢复到移动前状态（包括轨道增删、片段位置）。

---

## 10. ViewModel 与 UseCase 接口

### 10.1 `EditorViewModel` 新增方法

```kotlin
fun moveClip(clipId: String, targetStartTimeUs: Long)
```

内部通过 `EditorUseCase` 创建并执行 `MoveClipCommand(clipId, targetStartTimeUs)`，由命令层自动决定最终落入哪个轨道。

### 10.2 UI 回调

`TimelinePanel` / `TrackRow` / `ClipItem` 新增回调：

```kotlin
onClipMoveStart: (String) -> Unit,
onClipMove: (String, Long) -> Unit,        // 实时预览，可暂不持久化
onClipMoveEnd: (String, Long) -> Unit      // 释放后提交（只传目标起始时间，轨道由命令层计算）
```

为降低复杂度，P0 阶段：
- 拖拽过程中**只更新本地临时偏移**，不修改 `Timeline`。
- 释放后通过 `onClipMoveEnd` 调用 `viewModel.moveClip(...)` 一次性提交。

---

## 11. 边界与异常处理

| 场景 | 处理方式 |
|------|---------|
| 拖拽到时间轴负数区域 | Clamp 到 `0`，不允许超出起点。 |
| 片段被拖到总时长之外 | 允许，总时长自动重新计算。 |
| 主轨被锁定 | 如果原轨道或目标轨道被锁定，`Command.execute` 返回失败，UI Toast 提示。 |
| 吸附时同时命中多个目标 | 选择时间差最小的目标；若时间差相同，优先前一片段结尾。 |
| 空轨道清理后的 Undo | 通过快照式撤销完整恢复。 |
| 视频 duration 为 0 | 导入时跳过并记录日志。 |

---

## 12. 测试与验收要点

### 12.1 单元测试

- `Track.index` 字段更新后序列化/反序列化正确。
- `findLowestNonOverlappingLane()` 在单重叠、多层重叠、无重叠场景下返回正确 index。
- `Clip.overlapsWith()` 正确识别相交与首尾相接。
- 吸附函数在 12 dp 阈值内外行为正确。
- `MoveClipCommand.execute/undo` 能正确恢复原始 Timeline（含轨道增删）。

### 12.2 UI/集成测试

- 导入 2 段视频，时间轴上两片段首尾相接，不重叠。
- 拖拽第二段向左越过第一段结尾，释放后自动出现「画中画 1」轨道。
- 拖拽第二段接近第一段结尾（12 dp 内），自动吸附拼接。
- 点击撤销，恢复到拖拽前状态。
- 拖拽画中画层片段回到主轨空位，轨道自动合并回主轨。

### 12.3 性能目标

- 拖拽释放后 UI 反馈 ≤ 100ms（与 `spec/modules/timeline/README.md` 一致）。
- 吸附计算在拖拽过程中每帧耗时 ≤ 1ms。

---

## 13. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | 2026-06-30 | 初始设计：拖拽、重叠自动换轨、边缘吸附、数据模型变更 |

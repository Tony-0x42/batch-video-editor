# editor 模块规范

> 本文件定义 VideoEditor-For-Android 项目「剪辑核心」模块的职责、功能、数据模型、交互流程、实现要点与质量要求。
> 本模块聚焦视频/图片片段的**精确剪辑操作**，包括分割、裁剪、删除、复制、排序、变速、倒放等核心命令与领域模型。

---

## 1. 模块概述

`editor` 模块位于整体架构的 **Presentation / UseCase 层与 Domain 层交界处**，是用户与时间线数据之间的「操作转换器」。它负责把用户在时间线上的手势/菜单操作（如点击分割、拖动裁剪、拖拽排序）封装为可撤销的命令，安全地修改 `timeline` 模块定义的多轨道时间线模型，并通过 `player` 模块触发实时预览刷新。

本模块需要解决的核心问题是：**如何用命令模式表达所有剪辑操作，保证操作的原子性、可撤销性、线程安全性，同时与现有 Demo 的 MediaCodec/OpenGL 渲染链路平滑衔接**。其边界清晰：只负责「片段级别的剪辑逻辑」，不实现时间线渲染、滤镜特效、音频处理、工程导出等能力，但这些能力需要能通过本模块产生的结果被正确驱动。

---

## 2. 功能清单

### P0 — 首个 MVP 必须实现

| 名称 | 描述 | 验收标准 |
|------|------|----------|
| **片段分割（Split）** | 在当前播放头或指定时间处，将一个 Clip 切分为两个相邻 Clip，两个新 Clip 保持原 Clip 的源素材引用，源时间范围拼接后等于原范围。 | 1. 分割后两段总时长等于原 Clip 时长；2. 源素材 URI 不变；3. 支持撤销重做；4. UI 反馈 ≤ 100ms。 |
| **头尾裁剪（Trim）** | 拖动 Clip 左右边缘，调整其在时间线上的 `startTimeUs` / `endTimeUs` 以及对应的 `sourceStartUs` / `sourceEndUs`，不改变相邻 Clip。 | 1. 裁剪后 Clip 时长 ≥ 100ms；2. 源时间范围合法（sourceStart < sourceEnd）；3. 预览实时同步。 |
| **片段删除（Delete）** | 删除选中的 Clip；若是主视频轨片段，删除后后续片段可配置「自动前移填补空隙」或「保留空白间隙」。 | 1. 删除后轨道内 Clip ID 唯一；2. 至少支持 Ripple Delete（自动前移）与 Delete and Leave Gap 两种模式；3. 撤销重做正确。 |
| **片段复制（Duplicate）** | 在相邻位置生成一个与原 Clip 参数完全一致的新 Clip（新 ID），源素材引用共享。 | 1. 复制后 Clip 紧接原 Clip；2. 不复制源文件；3. 支持一次复制一个或多个选中的 Clip。 |
| **拖拽排序（Reorder）** | 在同一轨道内拖动 Clip 改变前后顺序，或在支持多轨道时拖放到其他轨道。 | 1. 排序后 Clip 不重叠；2. 跨轨道拖拽时类型必须兼容（视频 Clip 不能放入纯音频轨）；3. 撤销重做可还原原顺序。 |
| **播放头定位（Seek）** | 将当前播放头移动到指定时间点，并通知 Player 刷新画面；支持吸附到 Clip 边缘或关键帧。 | 1. Seek 精度 ≤ 1 帧（约 33ms@30fps）；2. 拖动 Seekbar 时预览帧率 ≥ 24fps；3. 支持吸附阈值 100ms。 |
| **撤销/重做（Undo/Redo）** | 所有 P0 剪辑操作必须通过 Command 模式封装，支持多步命令栈。 | 1. 命令栈深度默认 50 步；2. 工程保存前命令栈可持久化；3. 保存/恢复工程后命令状态一致。 |

### P1 — 重要增值（MVP 后 1~2 个迭代）

| 名称 | 描述 | 验收标准 |
|------|------|----------|
| **变速（Speed）** | 对单个视频/音频 Clip 设置播放速度 0.25x ~ 4x，并可选「保持音调」或「音调随速度变化」。 | 1. 变速后 Clip 在时间线上的显示时长正确（timelineDuration = sourceDuration / speed）；2. 音画同步误差 < 40ms；3. 导出时通过 `audio` 模块 Sonic 处理变速。 |
| **倒放（Reverse）** | 将视频 Clip 按帧倒序播放；音频同步倒放。 | 1. 倒放后首帧为原视频末帧；2. 导出时通过预先生成倒放缓存文件或实时倒放渲染实现；3. 对 >30s 片段提供「先生成缓存再编辑」的进度提示。 |
| **定格（Freeze）** | 在指定时间点提取一帧，生成一张持续指定时长的图片 Clip 插入时间线。 | 1. 定格帧画质不低于源视频；2. 定格时长 ≥ 100ms；3. 原 Clip 自动分割并在定格后恢复。 |
| **批量选择编辑** | 支持多选 Clip 后统一删除、复制、移动。 | 1. 选中态视觉反馈清晰；2. 批量操作后 Clip 时间不重叠；3. 撤销重做为单次命令。 |
| **Clip 旋转/翻转/缩放** | 对视频 Clip 应用基础几何变换（旋转 90°/180°/270°、水平/垂直翻转、画面缩放/位移）。 | 1. 变换参数持久化；2. 实时预览无黑边；3. 导出结果与预览一致。 |
| **吸附与对齐** | 拖拽 Clip 或裁剪边缘时，自动吸附到其他 Clip 边缘、播放头、关键帧或网格。 | 1. 吸附阈值可调（默认 100ms）；2. 吸附时提供触觉反馈；3. 不影响手动精确微调。 |

### P2 — 长期规划

| 名称 | 描述 | 验收标准 |
|------|------|----------|
| **曲线变速（Curve Speed）** | 对 Clip 内不同时间段设置非线性变速曲线。 | 1. 支持至少 5 个控制点；2. 时间重映射后 Clip 总时长计算准确；3. 导出精度误差 < 40ms。 |
| **智能剪辑建议** | 基于画面/音频自动标记精彩片段、静音段、抖动段，供用户一键删除或保留。 | 1. 分析在后台线程完成；2. 标记可人工调整；3. 不出错删用户素材。 |
| **多工程片段复用** | 跨工程复制/导入 Clip 及其编辑参数。 | 1. 源素材路径在目标工程中可解析；2. 缺失素材提示用户重新链接。 |

---

## 3. 数据模型与接口

### 3.1 核心领域模型

本模块直接使用 `timeline` 模块定义的 `Timeline`、`Track`、`Clip` 领域模型，并在本模块内补充与「剪辑操作」强相关的命令、状态与变换对象。

```kotlin
/**
 * 剪辑操作的最小原子单元，支持 execute / undo。
 * 所有会改变 Timeline 状态的操作都必须实现此接口。
 */
interface EditorCommand {
    val commandId: String
    val createTime: Long
    /** 执行命令，返回操作后的 Timeline 或错误 */
    suspend fun execute(timeline: Timeline): Result<Timeline>
    /** 撤销命令，返回操作前的 Timeline 或错误 */
    suspend fun undo(timeline: Timeline): Result<Timeline>
    /** 命令的友好描述，用于撤销菜单或日志 */
    fun description(): String
}

/**
 * 命令栈管理器，负责记录、执行、撤销、重做命令。
 */
interface CommandStack {
    val canUndo: Boolean
    val canRedo: Boolean
    val undoDescription: String?
    val redoDescription: String?
    /**
     * 执行一条新命令。执行成功后，redo 栈清空。
     * 必须在非主线程调用（Dispatchers.Default/IO）。
     */
    suspend fun execute(command: EditorCommand): Result<Timeline>
    suspend fun undo(): Result<Timeline>
    suspend fun redo(): Result<Timeline>
    fun clear()
}

/**
 * 编辑器状态，暴露给 ViewModel 与 UI。
 */
data class EditorState(
    val timeline: Timeline,
    val currentPositionUs: Long = 0L,
    val selectedClipIds: Set<String> = emptySet(),
    val playing: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val snapEnabled: Boolean = true,
    val commandError: EditorError? = null
)

sealed class EditorError(val message: String) {
    class InvalidTime(message: String) : EditorError(message)
    class ClipNotFound(clipId: String) : EditorError("Clip not found: $clipId")
    class TrackTypeMismatch(message: String) : EditorError(message)
    class OperationNotAllowed(message: String) : EditorError(message)
}
```

### 3.2 Clip 变换参数

```kotlin
/**
 * 单个 Clip 的画面几何变换，由 editor 模块生成并维护，
 * 最终由 renderer 模块在渲染时应用。
 */
data class VideoTransform(
    val scaleX: Float = 1.0f,
    val scaleY: Float = 1.0f,
    val translateX: Float = 0.0f,
    val translateY: Float = 0.0f,
    val rotationDegrees: Float = 0.0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false
)

/**
 * 单个 Clip 的播放速度配置。
 */
data class PlaybackSpeed(
    val speed: Float = 1.0f,
    /** 变速时是否保持原始音调 */
    val keepPitch: Boolean = true
)
```

### 3.3 核心命令接口示例

```kotlin
/**
 * 在指定时间处分割一个 Clip。
 */
data class SplitClipCommand(
    val clipId: String,
    val splitTimeUs: Long
) : EditorCommand {
    override suspend fun execute(timeline: Timeline): Result<Timeline> {
        // 1. 查找 Clip
        // 2. 校验 splitTimeUs 在 (startTimeUs, endTimeUs) 内
        // 3. 按时间比例计算 sourceSplitUs
        // 4. 生成两个新 Clip（新 ID）
        // 5. 替换原 Clip，保持轨道顺序
        // 6. 返回新 Timeline
    }

    override suspend fun undo(timeline: Timeline): Result<Timeline> {
        // 1. 查找由本次分割生成的两个 Clip
        // 2. 合并为原 Clip（原 ID）
        // 3. 替换回轨道
    }
}

/**
 * 裁剪 Clip 头尾。
 */
data class TrimClipCommand(
    val clipId: String,
    val newStartTimeUs: Long? = null,
    val newEndTimeUs: Long? = null
) : EditorCommand

/**
 * 删除 Clip。
 */
data class DeleteClipCommand(
    val clipId: String,
    val rippleDelete: Boolean = true
) : EditorCommand

/**
 * 复制 Clip。
 */
data class DuplicateClipCommand(
    val clipId: String
) : EditorCommand

/**
 * 在同一轨道内移动 Clip 到新位置。
 */
data class MoveClipCommand(
    val clipId: String,
    val targetTrackId: String,
    val targetStartTimeUs: Long
) : EditorCommand

/**
 * 修改 Clip 的播放速度。
 */
data class ChangeSpeedCommand(
    val clipId: String,
    val speed: Float,
    val keepPitch: Boolean
) : EditorCommand

/**
 * 设置 Clip 倒放状态。
 */
data class ReverseClipCommand(
    val clipId: String,
    val reversed: Boolean
) : EditorCommand
```

### 3.4 Repository / UseCase 接口

```kotlin
/**
 * 剪辑操作的入口，由 UI 层的 ViewModel 调用。
 */
interface EditorUseCase {
    val editorState: StateFlow<EditorState>

    suspend fun splitClipAt(clipId: String, timeUs: Long): Result<Unit>
    suspend fun trimClip(clipId: String, startUs: Long?, endUs: Long?): Result<Unit>
    suspend fun deleteClip(clipId: String, rippleDelete: Boolean = true): Result<Unit>
    suspend fun duplicateClip(clipId: String): Result<Unit>
    suspend fun moveClip(clipId: String, targetTrackId: String, targetStartUs: Long): Result<Unit>
    suspend fun setClipSpeed(clipId: String, speed: Float, keepPitch: Boolean = true): Result<Unit>
    suspend fun setClipReverse(clipId: String, reversed: Boolean): Result<Unit>
    suspend fun freezeFrame(clipId: String, timeUs: Long, durationUs: Long): Result<Unit>
    suspend fun undo(): Result<Unit>
    suspend fun redo(): Result<Unit>
    suspend fun seekTo(positionUs: Long): Result<Unit>
    suspend fun selectClips(clipIds: Set<String>): Result<Unit>
}

/**
 * 由 data 层实现，负责把 CommandStack 与 Timeline 持久化到 Room。
 */
interface EditorRepository {
    suspend fun saveCommandHistory(projectId: String, stack: CommandStack)
    suspend fun loadCommandHistory(projectId: String): CommandStack?
}
```

### 3.5 输入输出与状态管理

- **输入**：用户手势/工具栏事件、Seekbar 位置、当前 `Timeline` 对象、工程 ID。
- **输出**：新的 `Timeline` 对象、`EditorState` Flow、命令执行结果（成功/失败）。
- **状态管理**：`EditorViewModel` 持有 `EditorUseCase`，通过 `StateFlow<EditorState>` 向 Compose UI 提供单向数据流。所有会修改 Timeline 的命令都必须在 `viewModelScope` + `Dispatchers.Default` 中执行，执行完成后通过 `TimelineRepository` 触发自动保存。

### 3.6 与其他模块的依赖接口

| 依赖模块 | 接口/能力 | 用途 |
|----------|----------|------|
| `timeline` | `Timeline`、`Track`、`Clip`、`TimelineRepository` | 读取和修改多轨道时间线模型 |
| `player` | `PlayerEngine.seekTo(timeline, positionUs)`、`refresh()` | 命令执行后刷新实时预览 |
| `project` | `ProjectRepository.saveProject(project)` | 命令执行成功后自动保存工程 |
| `media` | `MediaInfoProvider.getMediaInfo(uri)` | 校验源素材时长、分辨率、旋转角 |
| `audio` | `AudioProcessor.calcDurationAfterSpeed(...)` | 变速后音频时长与音调处理 |
| `export` | `ExportSettings` | 不直接依赖，但 editor 产生的时间线结构决定导出输入 |

---

## 4. 交互与流程

### 4.1 关键用户操作流程

#### 4.1.1 分割片段

1. 用户在时间轴上拖动播放头到目标位置。
2. 点击工具栏「分割」按钮或双击 Clip。
3. `EditorViewModel` 调用 `splitClipAt(clipId, currentPositionUs)`。
4. `SplitClipCommand.execute()` 校验时间点合法后，生成两个新 Clip。
5. 新 Timeline 通过 `editorState` 通知 UI 刷新。
6. `PlayerEngine` 收到 Timeline 变化后刷新渲染图。
7. `ProjectRepository` 在后台自动保存工程与命令栈。

#### 4.1.2 裁剪头尾

1. 用户选中 Clip，拖动左/右裁剪手柄。
2. 手柄释放后，`EditorViewModel` 计算新的 `startTimeUs` / `endTimeUs`。
3. 调用 `trimClip(...)`，封装为 `TrimClipCommand`。
4. 校验新的源时间范围合法（不越界、不反向、不短于 100ms）。
5. 执行命令，刷新预览与保存工程。

#### 4.1.3 变速

1. 用户选中 Clip，打开变速面板，设置速度 2.0x。
2. `EditorViewModel` 调用 `setClipSpeed(clipId, 2.0f, keepPitch = true)`。
3. `ChangeSpeedCommand` 修改 Clip 的 `speed` 字段，并重新计算 `endTimeUs`（主视频轨需保证不与相邻 Clip 重叠）。
4. 若启用保持音调，`audio` 模块在导出/预览时介入 Sonic 处理。
5. 若速度导致 Clip 跨越下一个 Clip，`EditorUseCase` 可选择自动后移相邻 Clip 或报错。

### 4.2 模块内部数据流

```
用户操作（分割/裁剪/删除/变速）
    ↓
EditorViewModel 验证参数合法性
    ↓
构建对应的 EditorCommand 对象
    ↓
CommandStack.execute(command) 在 Dispatchers.Default 执行
    ↓
修改 Timeline 领域模型（纯内存、无副作用）
    ↓
新的 Timeline 写入 editorState
    ↓
PlayerEngine.onTimelineChanged(timeline) 刷新预览
    ↓
ProjectRepository.saveProject(project) 异步持久化
```

### 4.3 撤销重做时序

```
用户点击撤销
    ↓
CommandStack.undo() 调用最近命令的 undo()
    ↓
恢复上一个 Timeline 状态
    ↓
通知 Player 刷新
    ↓
自动保存工程（命令栈本身也被保存）
```

---

## 5. 实现要点

### 5.1 技术方案选择

1. **命令模式（Command Pattern）**：所有会改变 Timeline 的操作都封装为 `EditorCommand`。每个命令自行实现 `execute` 与 `undo`，保证撤销重做逻辑内聚。
2. **不可变 Timeline**：每次命令执行后返回新的 `Timeline` 对象，配合 `StateFlow` 实现单向数据流，避免并发修改问题。
3. **Clip ID 全局唯一**：每个 Clip 拥有工程级唯一 ID（UUID / 雪花算法），分割、复制时生成新 ID，避免撤销重做期间 ID 冲突。
4. **源时间与时间线时间分离**：`sourceStartUs` / `sourceEndUs` 表示在源素材中的起止；`startTimeUs` / `endTimeUs` 表示在工程时间线上的起止。变速/倒放时只改时间线时间相关的计算，不改变源素材。
5. **Ripple Delete 与 Leave Gap 策略**：P0 必须支持两种删除模式，默认 Ripple Delete。实现时通过配置 `DeleteBehavior` 枚举控制。

### 5.2 需要特别注意的难点

1. **变速后的音画同步**：
   - 视频变速后，`endTimeUs = (sourceEndUs - sourceStartUs) / speed`。
   - 若 Clip 同时包含音频，音频需要通过 `audio` 模块变速处理，并保证导出时 PTS 连续。
   - 建议 `editor` 模块只负责计算时间，实际音频重采样交给 `audio` 模块。

2. **倒放的实现复杂度**：
   - 实时倒放对 Player 和 Decoder 压力大，建议对短视频片段（<30s）采用「生成倒放缓存文件」方案。
   - 倒放片段的缩略图、时长、帧边界需单独处理，时间线显示需明显标识。

3. **撤销重做与自动保存的时序**：
   - 命令执行失败时不得保存 Timeline，也不得入栈。
   - 自动保存应保存「命令栈 + 当前 Timeline 快照」双份数据，确保工程恢复后 undo/redo 状态一致。

4. **跨轨道拖拽的类型兼容性**：
   - 视频 Clip 可放入视频轨、画中画轨；音频 Clip 只能放入音频轨；图片 Clip 可放入视频轨但不能放入纯音频轨。
   - 跨轨道移动时需校验 `TrackType`，否则返回 `TrackTypeMismatch` 错误。

5. **裁剪的最小片段保护**：
   - 任何 Clip 经裁剪后时长不得低于 100ms，防止生成空片段或异常解码。

### 5.3 与现有代码的衔接建议

1. **复用现有 JNI 混音能力**：`editor` 模块不直接处理音频，但在执行「删除带音频的视频片段」或「变速」命令后，需要通知 `audio` 模块重新计算混音时间线。
2. **与旧版 OpenGL 录制解耦**：旧录制模块产生的原始视频文件作为普通源素材导入即可，editor 不需要感知录制细节。
3. **MediaCodec 硬编码链路**：导出阶段由 `export` 模块读取 editor 修改后的 Timeline，`editor` 只需保证 Timeline 模型完整、源时间范围合法。
4. **目录建议**：
   - `app/src/main/java/com/example/cj/videoeditor/domain/editor/`：命令接口、EditorState、EditorError。
   - `app/src/main/java/com/example/cj/videoeditor/presentation/editor/`：EditorUseCase、EditorViewModel、CommandStack 实现。
   - `app/src/main/java/com/example/cj/videoeditor/data/editor/`：EditorRepository 实现。

---

## 6. 性能与质量要求

### 6.1 性能指标

| 场景 | 目标 | 说明 |
|------|------|------|
| 分割操作 UI 反馈 | ≤ 100ms | 从用户点击到时间轴重绘完成 |
| 裁剪操作 UI 反馈 | ≤ 80ms | 手柄释放到预览刷新 |
| 删除操作 UI 反馈 | ≤ 80ms | 包括 Ripple Delete 重新排布 |
| 拖拽排序反馈 | ≤ 100ms | 拖放到新位置后 UI 更新 |
| 变速/倒放命令执行 | ≤ 200ms | 仅修改模型，不含导出缓存生成 |
| 命令栈深度 | ≥ 50 步 | 默认 50 步，可配置 |
| 撤销重做执行 | ≤ 80ms | 单步 undo/redo 的模型恢复 |

### 6.2 常见异常与处理

| 异常场景 | 原因 | 处理策略 |
|----------|------|----------|
| **裁剪时间越界** | 用户拖动裁剪手柄超出源素材范围 | 自动钳制到合法范围，并给出 Snackbar 提示 |
| **分割点位于 Clip 边缘** | splitTimeUs 等于 startTimeUs 或 endTimeUs | 拒绝执行，提示用户「请选择片段中间位置分割」 |
| **删除后轨道为空** | 删除最后一段主视频轨 Clip | 允许删除，但 Player 显示黑帧占位，导出时提示用户添加素材 |
| **跨轨道类型不匹配** | 把音频 Clip 拖入视频轨 | 拒绝 drop，给出触觉反馈与视觉提示 |
| **变速导致重叠** | 2x 变速后 Clip 跨越下一个 Clip | 默认自动后移相邻 Clip；若无法后移则报错 |
| **源素材不可用** | 用户删除或移动了原文件 | 命令执行前通过 `media` 模块校验 URI 可访问，失败时标记为「素材缺失」并提示重新选择 |
| **命令栈溢出** | 操作步数超过最大深度 | 移除最早命令，保留最近 50 步，必要时提示用户 |
| **Undo 后播放头越界** | 撤销导致 Timeline 总时长变短，当前播放头超出终点 | 自动将播放头 seek 到新的终点位置 |

### 6.3 日志与监控

- 所有命令执行/撤销/重做必须记录日志，Tag 统一为 `VE:EditorCommand`。
- 命令执行耗时超过阈值（如 500ms）需打印警告日志。
- 崩溃率目标 < 0.5%，editor 模块相关崩溃需单独分类到 Firebase Crashlytics。

---

## 7. 依赖模块

| 模块 | 路径 | 依赖方式 | 说明 |
|------|------|----------|------|
| **timeline** | `spec/modules/timeline/README.md` | 强依赖 | 使用其 `Timeline`、`Track`、`Clip` 模型与 `TimelineRepository` 接口 |
| **player** | `spec/modules/player/README.md` | 强依赖 | 命令执行后调用 Player 刷新实时预览 |
| **project** | `spec/modules/project/README.md` | 强依赖 | 通过 `ProjectRepository` 自动保存工程与命令栈 |
| **media** | `spec/modules/media/README.md` | 中等依赖 | 校验源素材信息、生成缩略图、解析媒体元数据 |
| **audio** | `spec/modules/audio/README.md` | 中等依赖 | 变速/倒放/音量相关计算与处理 |
| **draft** | `spec/modules/draft/README.md` | 弱依赖 | 自动保存机制与草稿恢复需要参考其规范 |
| **uiux** | `spec/modules/uiux/README.md` | 弱依赖 | 剪辑页工具栏、时间轴手势、选中态等 UI 规范 |
| **export** | `spec/modules/export/README.md` | 弱依赖 | editor 输出 Timeline 作为 export 的输入，不直接调用 |

---

## 8. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | 2025-06-28 | 初始 editor 模块规范，覆盖分割、裁剪、删除、复制、排序、变速、倒放等核心能力 |

# 时间线（Timeline）模块规范

> 本文件定义 VideoEditor-For-Android 中时间线模块的职责边界、数据模型、核心流程、实现要点与质量要求。所有涉及时间线、轨道、片段、剪辑命令与撤销重做的实现，必须以本规范为首要参考。

---

## 1. 模块概述

时间线模块是视频剪辑应用的**核心编排层**，负责将离散的素材片段（视频、图片、音频、文字、特效）组织为有序、可编辑、可渲染的时序结构。它向上为剪辑主界面提供轨道数据、播放位置、片段选择与操作接口；向下为播放器（player）、导出器（export）、音频处理器（audio）提供按时间精确索引的渲染图。

本模块要解决的问题包括：如何在多轨道之间保证时间对齐；如何以统一的 `Clip` 抽象表达视频、图片、音频等不同素材；如何让分割、裁剪、删除、复制、排序、拖拽等操作既响应迅速又可撤销；如何支撑后续变速、倒放、转场、关键帧等高级能力的扩展。时间线模块不直接负责解码、渲染、编码，但必须定义清晰的数据契约，使渲染管线能够根据当前时间快速定位需要合成的内容。

---

## 2. 功能清单

### P0 — 首个 MVP 必须实现

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 多轨道时间轴模型 | 支持主视频轨、画中画轨、音频轨、文字轨四类轨道，每种轨道按时间顺序排列 Clip | `Timeline` 对象包含 `List<Track>`，轨道类型通过 `TrackType` 区分，序列化后可在 Room 中完整恢复 |
| 片段抽象与基础属性 | 定义统一的 `Clip` 基类，包含在时间线上的起止时间、在源素材上的起止时间、轨道归属 | 所有 Clip 派生类均可计算 `timelineDurationUs`、`sourceDurationUs`，且时间单位统一为微秒（us） |
| 视频片段导入 | 将导入的视频、图片素材封装为 `VideoClip` / `ImageClip` 并插入主视频轨 | 导入后时间线总时长自动更新，片段默认按导入顺序首尾相接 |
| 片段分割 | 在指定时间点将单个 Clip 切分为前后两个 Clip，保持源素材引用不变 | 分割后两个新 Clip 的 `sourceStartUs` / `sourceEndUs` 之和等于原 Clip，Undo 后可恢复 |
| 片段裁剪 | 调整 Clip 的入点/出点，改变其在时间线上的有效时长 | 裁剪后 Clip 的 `sourceStartUs` / `sourceEndUs` 更新，且不超出源素材实际时长 |
| 片段删除 | 从轨道中移除指定 Clip，可选是否保留后续片段位置 | 删除后轨道内 Clip 索引连续，无空引用；支持 Undo 恢复 |
| 片段复制 | 在相同轨道生成与原 Clip 内容一致的副本 | 副本生成新 ID，源时间范围与原 Clip 相同，插入位置紧随原 Clip 之后 |
| 拖拽排序 | 在同轨道内改变 Clip 顺序，自动重新计算时间线位置 | 拖拽释放后所有受影响 Clip 的 `startTimeUs` / `endTimeUs` 重新排列，无时间重叠（同轨道） |
| 撤销/重做 | 基于 Command 模式维护操作栈，支持撤销与重做 | 栈深度默认 50 步；Undo/Redo 后 Timeline 状态与操作前完全一致，并通知 UI 刷新 |
| 当前播放位置管理 | 维护 `currentPositionUs`，支持 Seek 与播放进度同步 | Seek 后能够返回当前时间点命中的 Clip 列表，精度误差 ≤ 1ms |
| 时间线总时长计算 | 根据所有轨道的最晚结束时间自动计算 `durationUs` | 新增/删除/裁剪 Clip 后总时长实时更新，Player 与导出器使用同一数值 |

### P1 — 重要增值

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 轨道锁定/静音/隐藏 | 对单条轨道设置锁定（不可编辑）、静音（音频轨）、隐藏（视频轨）状态 | 状态变更后轨道 Clip 仍参与总时长计算，但被锁定时 UI 禁止编辑操作 |
| 片段变速 | 调整视频 Clip 的播放速度（如 0.25x ~ 4x） | 变速后 Clip 在时间线上的长度按速度比例变化，Player/Exporter 读取时获得 `speed` 参数 |
| 片段倒放 | 标记视频 Clip 以倒序方式播放 | `VideoClip.isReversed` 为 true 时，渲染管线按逆序读取帧 |
| 片段定格 | 在 Clip 内某一帧生成一张持续指定时长的图片片段 | 定格帧生成独立的 `ImageClip`，源时间点指向原视频帧 |
| 跨轨道拖拽 | 允许将 Clip 从一条视频轨拖到另一条视频轨（如画中画） | 拖拽后 Clip 的 `trackId` 更新，时间范围保持不变 |
| 空镜/背景填充片段 | 在视频片段之间插入纯色或图片背景片段 | 作为 `ImageClip` 或 `SolidColorClip` 实现，与相邻片段无转场 |
| 多选批量操作 | 支持同时选中多个 Clip 进行删除、复制、移动 | 批量操作作为一个原子 Command 入栈，Undo 时整体恢复 |
| 操作历史快照 | 在关键操作点自动保存 Timeline 快照，支持快速回退 | 快照存储为 JSON 差异，最多保留 5 个最近的自动快照 |

### P2 — 长期规划

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 嵌套序列 | 支持将一个工程的时间线作为片段嵌入另一工程 | 嵌套序列以 `NestedSequenceClip` 表达，导出时递归展开 |
| 智能节拍对齐 | 根据音频节拍自动吸附片段边界 | 提供吸附建议点列表，用户确认后自动调整 Clip 边界 |
| 版本化时间线 | 保存时间线多个历史版本，支持分支对比 | 每次导出前自动创建版本节点，可命名与回滚 |
| 协作标记 | 在时间线上添加评论标记与范围标注 | 标记不影响渲染，持久化到 Project 元数据 |

---

## 3. 数据模型与接口

### 3.1 核心领域模型

```kotlin
/**
 * 时间线根对象，承载所有轨道与总时长。
 */
data class Timeline(
    val id: String,
    val durationUs: Long,
    val tracks: List<Track>
) {
    /**
     * 重新计算总时长，应在每次 Clip 增删改后调用。
     */
    fun recalculateDuration(): Timeline = copy(
        durationUs = tracks.maxOfOrNull { it.clips.maxOfOrNull { clip -> clip.endTimeUs } ?: 0L } ?: 0L
    )
}

/**
 * 轨道类型。
 */
enum class TrackType {
    VIDEO,      // 主视频/画中画轨道，视觉层
    AUDIO,      // 音乐/音效/录音轨道
    TEXT,       // 文字/字幕轨道
    EFFECT      // 特效/滤镜包络轨道（预留）
}

/**
 * 轨道基类。轨道负责维护一组按时间升序排列的 Clip。
 */
sealed class Track(
    open val id: String,
    open val type: TrackType,
    open val index: Int,                // 轨道层级，数值越大越靠前
    open val clips: List<Clip>,
    open val isLocked: Boolean = false,
    open val isMuted: Boolean = false,
    open val isVisible: Boolean = true
) {
    abstract fun withClips(newClips: List<Clip>): Track
}

data class VideoTrack(
    override val id: String,
    override val index: Int,
    override val clips: List<Clip>,
    override val isLocked: Boolean = false,
    override val isVisible: Boolean = true
) : Track(id, TrackType.VIDEO, index, clips, isLocked, false, isVisible) {
    override fun withClips(newClips: List<Clip>): Track = copy(clips = newClips)
}

data class AudioTrack(
    override val id: String,
    override val index: Int,
    override val clips: List<Clip>,
    override val isLocked: Boolean = false,
    override val isMuted: Boolean = false
) : Track(id, TrackType.AUDIO, index, clips, isLocked, isMuted, true) {
    override fun withClips(newClips: List<Clip>): Track = copy(clips = newClips)
}

/**
 * 片段基类。所有时间线素材共享相同的时序属性。
 */
sealed class Clip(
    open val id: String,
    open val trackId: String,
    open val startTimeUs: Long,
    open val endTimeUs: Long,
    open val sourceStartUs: Long,
    open val sourceEndUs: Long
) {
    val timelineDurationUs: Long get() = endTimeUs - startTimeUs
    val sourceDurationUs: Long get() = sourceEndUs - sourceStartUs

    abstract fun copyWithTime(
        startTimeUs: Long = this.startTimeUs,
        endTimeUs: Long = this.endTimeUs,
        sourceStartUs: Long = this.sourceStartUs,
        sourceEndUs: Long = this.sourceEndUs
    ): Clip
}

/**
 * 视频片段。
 */
data class VideoClip(
    override val id: String,
    override val trackId: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    override val sourceStartUs: Long,
    override val sourceEndUs: Long,
    val uri: Uri,
    val speed: Float = 1.0f,
    val isReversed: Boolean = false,
    val transformations: VideoTransform = VideoTransform()
) : Clip(id, trackId, startTimeUs, endTimeUs, sourceStartUs, sourceEndUs) {
    override fun copyWithTime(
        startTimeUs: Long,
        endTimeUs: Long,
        sourceStartUs: Long,
        sourceEndUs: Long
    ): Clip = copy(
        startTimeUs = startTimeUs,
        endTimeUs = endTimeUs,
        sourceStartUs = sourceStartUs,
        sourceEndUs = sourceEndUs
    )
}

/**
 * 图片片段（静态帧）。
 */
data class ImageClip(
    override val id: String,
    override val trackId: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    val uri: Uri,
    val transformations: VideoTransform = VideoTransform()
) : Clip(id, trackId, startTimeUs, endTimeUs, 0L, endTimeUs - startTimeUs) {
    override fun copyWithTime(
        startTimeUs: Long,
        endTimeUs: Long,
        sourceStartUs: Long,
        sourceEndUs: Long
    ): Clip = copy(startTimeUs = startTimeUs, endTimeUs = endTimeUs)
}

/**
 * 音频片段。
 */
data class AudioClip(
    override val id: String,
    override val trackId: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    override val sourceStartUs: Long,
    override val sourceEndUs: Long,
    val uri: Uri,
    val volume: Float = 1.0f,
    val fadeInUs: Long = 0L,
    val fadeOutUs: Long = 0L
) : Clip(id, trackId, startTimeUs, endTimeUs, sourceStartUs, sourceEndUs) {
    override fun copyWithTime(
        startTimeUs: Long,
        endTimeUs: Long,
        sourceStartUs: Long,
        sourceEndUs: Long
    ): Clip = copy(
        startTimeUs = startTimeUs,
        endTimeUs = endTimeUs,
        sourceStartUs = sourceStartUs,
        sourceEndUs = sourceEndUs
    )
}

/**
 * 文字/字幕片段。
 */
data class TextClip(
    override val id: String,
    override val trackId: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    val text: String,
    val style: TextStyle = TextStyle(),
    val animationIn: TextAnimation = TextAnimation.NONE,
    val animationOut: TextAnimation = TextAnimation.NONE
) : Clip(id, trackId, startTimeUs, endTimeUs, 0L, endTimeUs - startTimeUs) {
    override fun copyWithTime(
        startTimeUs: Long,
        endTimeUs: Long,
        sourceStartUs: Long,
        sourceEndUs: Long
    ): Clip = copy(startTimeUs = startTimeUs, endTimeUs = endTimeUs)
}

/**
 * 视频画面变换。
 */
data class VideoTransform(
    val scaleX: Float = 1.0f,
    val scaleY: Float = 1.0f,
    val rotation: Float = 0.0f,
    val translateX: Float = 0.0f,
    val translateY: Float = 0.0f
)
```

### 3.2 时间线仓储接口

```kotlin
/**
 * 时间线数据访问接口，由 Data 层实现。
 */
interface TimelineRepository {
    /**
     * 获取指定工程的时间线。
     */
    suspend fun getTimeline(projectId: String): Result<Timeline>

    /**
     * 保存时间线。调用方应保证 Timeline 已通过 [recalculateDuration] 更新。
     */
    suspend fun saveTimeline(projectId: String, timeline: Timeline): Result<Unit>

    /**
     * 监听时间线变化。
     */
    fun observeTimeline(projectId: String): Flow<Timeline>
}
```

### 3.3 剪辑命令接口

```kotlin
/**
 * 可撤销的剪辑命令。
 */
interface TimelineCommand {
    val name: String
    val createdAt: Long

    /**
     * 执行命令，返回新的 Timeline。
     */
    fun execute(timeline: Timeline): Timeline

    /**
     * 撤销命令，返回命令执行前的 Timeline。
     */
    fun undo(timeline: Timeline): Timeline
}

/**
 * 命令栈管理器。
 */
interface CommandStack {
    val canUndo: Boolean
    val canRedo: Boolean
    val undoCount: Int
    val redoCount: Int

    fun push(command: TimelineCommand)
    fun undo(): TimelineCommand?
    fun redo(): TimelineCommand?
    fun clear()
}
```

### 3.4 播放器与渲染依赖接口

```kotlin
/**
 * 时间线渲染图提供者。Player / Exporter 通过此接口查询当前时间需要渲染的内容。
 */
interface TimelineRenderGraph {
    /**
     * 查询指定时间点所有激活的 Clip，按轨道层级升序返回。
     */
    fun getActiveClipsAt(positionUs: Long): List<ClipRenderItem>

    /**
     * 查询指定时间点的背景、滤镜、文字等覆盖层。
     */
    fun getOverlaysAt(positionUs: Long): List<OverlayRenderItem>
}

data class ClipRenderItem(
    val clip: Clip,
    val trackIndex: Int,
    val localTimeUs: Long          // 当前时间点在该 Clip 内的本地时间
)
```

---

## 4. 交互与流程

### 4.1 用户操作流程：导入素材并排列

1. 用户在素材选择页选择 3 段视频。
2. `MediaUseCase` 解析每段视频的时长、分辨率、旋转角度，返回 `MediaInfo` 列表。
3. `TimelineUseCase` 为每段视频创建 `VideoClip`，`trackId` 指向主视频轨，`startTimeUs` 依次递增，`sourceStartUs = 0`，`sourceEndUs = 实际时长`。
4. 新的 `Timeline` 通过 `TimelineRepository.saveTimeline()` 持久化。
5. `EditorViewModel` 通知 `Player` 刷新渲染图并定位到 `currentPositionUs = 0`。

### 4.2 用户操作流程：分割片段

1. 用户在时间轴上选中 Clip A，移动播放指针到 00:05.000。
2. 点击「分割」按钮，UI 层调用 `SplitClipCommand(clipId = A, splitTimeUs = 5_000_000)`。
3. `TimelineUseCase` 执行命令：
   - 在 Command 中根据 `splitTimeUs` 与 Clip A 的 `startTimeUs` 计算出本地分割点 `localUs`。
   - 生成 Clip A1 与 Clip A2，保持相同的 `sourceStartUs` / `sourceEndUs` 比例关系（考虑 speed 时进行换算）。
   - 用 A1、A2 替换原轨道中的 A。
4. 命令入栈，UI 刷新，自动保存。

### 4.3 模块内部数据流

```
用户操作（点击/拖拽/手势）
        ↓
EditorViewModel 将操作封装为 TimelineCommand
        ↓
TimelineUseCase 在 Domain 层修改 Timeline 模型
        ↓
CommandStack 记录命令（支持 Undo/Redo）
        ↓
TimelineRepository 将 Timeline 持久化到 Room
        ↓
Flow 通知 Player / Exporter 渲染图变更
        ↓
UI 层根据新的 Timeline 状态刷新时间轴视图
```

### 4.4 伪时序：Seek 时查询当前激活片段

```
Player.renderFrame(positionUs)
    → TimelineRenderGraph.getActiveClipsAt(positionUs)
        → 遍历所有非隐藏轨道
            → 对每条轨道的 clips 二分查找包含 positionUs 的 Clip
            → 计算 localTimeUs = positionUs - clip.startTimeUs
            → 若 VideoClip.speed != 1.0，localTimeUs *= speed
            → 若 VideoClip.isReversed，localTimeUs = clip.timelineDurationUs - localTimeUs
        → 返回 List<ClipRenderItem>
    → Renderer 按 trackIndex 从小到大合成帧
```

---

## 5. 实现要点

### 5.1 技术方案选择

- **时间单位**：统一使用微秒（`Long`，单位 us），避免浮点误差。所有对外显示的时间字符串在 UI 层进行格式化。
- **Clip 不可变性**：`Clip`、`Track`、`Timeline` 均设计为 `data class`，修改操作返回新对象。这能天然支持 Undo/Redo 与 Flow 差异比较。
- **命令模式**：所有会改变 Timeline 状态的操作都实现 `TimelineCommand`，由 `CommandStack` 统一管理。Command 内部持有执行所需的最小参数，不直接引用旧 Timeline，以保证序列化与单元测试的可行性。
- **轨道 Clip 有序性**：每条轨道的 `clips` 列表必须按 `startTimeUs` 升序维护。插入、分割、拖拽后需重新排序并重新计算首尾时间。
- **轨道层级合成**：视频轨按 `index` 从低到高合成，index 最大的在最上层；音频轨全部混音输出；文字轨作为覆盖层渲染在视频之上。

### 5.2 需要特别注意的难点

- **变速与倒放的时间映射**：`VideoClip.speed` 会改变时间线长度与本地时间的对应关系。分割、裁剪时必须先将时间线本地时间换算回源素材时间，再生成新 Clip。倒放需要标记 `isReversed`，渲染器在采样时反向 Seek。
- **图片片段的源时间**：`ImageClip` 没有源素材时长，其 `sourceDurationUs` 等于时间线长度。定格功能通过从视频帧截图生成新的 `ImageClip` 实现。
- **同轨道 Clip 不重叠**：P0 阶段同一条轨道内不允许 Clip 时间重叠。跨轨道（主视频轨与画中画轨）允许重叠，用于实现画中画。
- **Undo/Redo 与自动保存的协调**：自动保存应监听 `CommandStack` 的变化，但在连续拖拽或 scrub 时不应每次中间状态都触发数据库写入。建议采用 500ms 防抖 + 操作完成事件触发保存。
- **与旧代码的衔接**：旧工程中存在基于两段视频拼接的逻辑，可迁移为创建一个包含两段 `VideoClip` 的 `Timeline`，再调用导出器。避免直接在旧拼接代码中维护状态，而是统一通过 Timeline 模型驱动。

### 5.3 与现有代码的衔接建议

- 将旧 `android.hardware.Camera` 录制的视频文件以 `MediaInfo` 形式交给 `TimelineUseCase`，由本模块统一创建 `VideoClip`。
- 现有 JNI 混音逻辑保留在 audio 模块，Timeline 只负责描述音频片段的排列、音量、淡入淡出参数。
- 现有 OpenGL 滤镜链由 effects/filters 模块实现，Timeline 不存储 Shader 代码，只保存 `FilterEffect` 的 ID、强度、起止时间等元数据。

---

## 6. 性能与质量要求

### 6.1 性能指标

| 场景 | 目标 | 测试方法 |
|------|------|---------|
| 时间线操作反馈 | 分割/删除/复制/拖拽排序后 UI 刷新 ≤ 100ms | 在小米 12/荣耀 80 档设备上连续操作 100 次取 95 分位 |
| 大时间线加载 | 100 个 Clip 的工程从 Room 加载并渲染时间轴 ≤ 500ms | 使用 Room + JSON 反序列化测试 |
| Seek 查询耗时 | 查询当前时间激活 Clip 耗时 ≤ 1ms | 使用 `TimelineRenderGraph.getActiveClipsAt()` 单元测试 |
| 内存占用 | 编辑 10 段 1080p 视频时间线，Timeline 领域对象占用内存 ≤ 20MB | Android Studio Profiler Heap Dump |
| Undo/Redo 栈 | 50 步历史不引起明显内存增长，单步快照 ≤ 200KB | 序列化后统计 JSON 大小 |

### 6.2 常见异常与处理

| 异常场景 | 处理方式 |
|---------|---------|
| 分割点落在 Clip 边界外 | 返回 `Result.failure(TimelineException.InvalidSplitPoint)`，UI 提示「请选择有效分割位置」 |
| 裁剪后源时间越界 | 自动 clamp 到 `[0, sourceDurationUs]`，并记录警告日志 |
| 拖拽导致同轨道重叠 | P0 阶段禁止释放，显示红色冲突提示；P1 可考虑自动后移后续片段 |
| 轨道被锁定仍收到编辑命令 | `Command.execute()` 前置检查 `isLocked`，返回失败并提示轨道已锁定 |
| 源文件被删除 | `MediaUseCase` 校验 URI 有效性，失效 Clip 标记为缺失，UI 显示红色警告并禁止导出 |
| CommandStack 为空时 Undo/Redo | UI 层根据 `canUndo` / `canRedo` 禁用按钮，避免无效调用 |
| 时间线总时长为 0 时播放 | Player 进入空状态，显示空白画布或默认背景 |

### 6.3 单元测试要求

- 必须覆盖：Clip 时间计算、分割、裁剪、删除、复制、拖拽排序、Undo/Redo。
- 必须覆盖：总时长重新计算、轨道层级排序、变速 Clip 的时间映射。
- 推荐使用 MockK 模拟 `TimelineRepository`，确保 Domain 层不依赖 Android Framework。

---

## 7. 依赖模块

本模块依赖以下 `spec/modules` 下的模块：

| 模块 | 依赖说明 |
|------|---------|
| [media](../media/) | 导入素材时解析 `MediaInfo`（时长、分辨率、旋转角），并校验源文件 URI 有效性 |
| [player](../player/) | 向 Player 提供 `TimelineRenderGraph` 接口，Player 按时间线查询当前激活的 Clip 与覆盖层 |
| [export](../export/) | 导出器读取 Timeline 模型与渲染图，按轨道顺序合成并编码 |
| [audio](../audio/) | 音频轨道的 Clip 元数据（音量、淡入淡出）由 Timeline 维护，具体解码/混音由 audio 模块执行 |
| [effects](../effects/) | 文字轨、特效轨的渲染参数由 Timeline 保存，effects 模块负责实际的 Shader/动画渲染 |
| [filters](../filters/) | 视频片段应用的滤镜 ID 与强度由 Timeline 保存，filters 模块负责滤镜管线 |
| [project](../project/) | Timeline 是 `VideoProject` 的核心成员，project 模块负责工程版本与元数据持久化 |
| [uiux](../uiux/) | 时间轴 UI 组件、手势、主题、动画遵循 uiux 模块的设计系统 |

---

## 8. 边界说明

- **本模块不做**：视频解码、OpenGL 渲染、音频混音、硬编码导出、媒体扫描、相机采集。这些由对应模块实现。
- **本模块负责**：时间线结构、Clip 模型、剪辑命令、撤销重做、播放位置、总时长计算、渲染图索引。
- 若开发过程中需要新增 Clip 类型（如 `StickerClip`、`TransitionClip`），必须在本规范中补充定义，并评估对现有命令与渲染图接口的影响。

---

## 9. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | 2025-06-28 | 初始版本，定义 Timeline 模块的职责、模型、命令与性能要求 |

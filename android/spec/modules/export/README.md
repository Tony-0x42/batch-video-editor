# export 模块规范

> 本文件定义 VideoEditor-For-Android 导出模块的职责、功能、数据模型、交互流程、实现要点与性能要求。所有涉及视频导出的实现必须遵循本规范。

---

## 1. 模块概述

`export` 模块是剪辑闭环的最后一个环节，负责将经过时间轴编辑后的多轨道工程（`VideoProject`）渲染并编码为最终可发布、可分享的 MP4 视频文件。模块位于 Engine Layer 的最末端，向上通过 UseCase / ViewModel 响应 UI 层的导出请求，向下调用 `player`、`renderer`、`audio` 等引擎能力完成逐帧合成，最终利用 Android `MediaCodec` 硬编码与 `MediaMuxer` 封装输出。

该模块需要解决的核心问题包括：
- 如何将时间轴多轨道（主视频轨、画中画轨、音频轨、文字轨）按正确时序与层级合成为一帧图像和一段混音；
- 如何在保持音视频同步（AV Sync）的前提下，把解码后的原始帧送入 OpenGL 渲染管线并回读到编码器；
- 如何合理配置编码参数（分辨率、帧率、码率、Profile、Level），兼顾画质、体积与导出速度；
- 如何在后台稳定执行导出任务，提供可感知的进度、取消与失败恢复能力；
- 如何适配 Android 10+ 的 Scoped Storage，将输出文件保存到相册或应用沙盒。

---

## 2. 功能清单

### P0 — 首个 MVP 必须实现

| 功能名称 | 功能描述 | 验收标准 |
|----------|----------|----------|
| 基础 MP4 导出 | 将当前工程时间线导出为单文件 MP4（H.264 + AAC） | 任意 720p/1080p 工程可正常导出，输出文件可被系统相册识别播放 |
| 分辨率选择 | 支持 480p / 720p / 1080p 三档输出 | 选择后输出视频短边或长边符合对应档位，UI 实时显示目标尺寸 |
| 帧率选择 | 支持 24fps / 30fps | 输出视频帧率与选择一致，码率按帧率线性折算 |
| 码率配置 | 提供高/中/低三档码率预设 | 导出文件平均码率落在预设区间内，画面无明显块效应 |
| 水印开关 | 导出前可开启或关闭应用水印 | 开启时右下角叠加指定 PNG 水印；关闭时无水印 |
| 导出进度反馈 | 实时返回当前导出进度（0-100%） | 进度平滑递增，每秒至少刷新 2 次；暂停/卡住时 UI 需有明确状态 |
| 取消导出 | 用户可随时终止导出并清理临时文件 | 取消后编码器、Muxer 正确释放，不残留损坏文件 |
| 保存到相册 | 导出完成后通过 MediaStore 写入公共相册 | Android 10+ 使用 `MediaStore.Video.Media.RELATIVE_PATH`，无需额外权限 |

### P1 — MVP 后 1~2 个迭代

| 功能名称 | 功能描述 | 验收标准 |
|----------|----------|----------|
| H.265/HEVC 编码 | 在支持设备上提供 H.265 导出选项 | 检测到硬编码器可用时才显示选项；同画质下文件体积较 H.264 减少 ≥ 30% |
| 导出预设 | 提供「高清」「标清」「省空间」「抖音 9:16」等一键预设 | 选择预设后自动联动分辨率/帧率/码率；用户仍可手动微调 |
| 后台导出服务 | 导出在 Foreground Service 中运行并带通知进度 | 退到后台不中断；通知显示进度条、取消按钮；完成/失败点击可回到 App |
| 码率自定义 | 允许用户手动输入目标码率（Mbps） | 输入范围限制在 1~50 Mbps；导出信息面板显示实际平均码率 |
| 快速导出模式 | 降低预览分辨率，优先速度 | 导出时间较普通模式减少 ≥ 30%，画质下降在可接受范围内 |
| 导出封面帧 | 允许用户从时间轴指定一帧作为视频封面 | 封面保存为 JPEG 并关联到 MediaStore；可在导出结果页预览 |
| 失败重试与诊断 | 导出失败后给出原因并允许一键重试 | 错误类型至少覆盖：编码器异常、存储不足、源文件丢失、不支持的格式 |

### P2 — 长期规划

| 功能名称 | 功能描述 | 验收标准 |
|----------|----------|----------|
| GIF/WebM 输出 | 支持导出 GIF 动图或 WebM 视频 | GIF 支持自定义分辨率与帧率；WebM 使用 VP9 编码预留接口 |
| 仅导出音频 | 将工程混音导出为 AAC/MP3 | 输出文件时长与工程音频轨道一致，采样率 44.1kHz/48kHz 可选 |
| 分段导出 | 按分割点一次性导出多个片段 | 每个片段独立文件命名；进度按片段数量聚合显示 |
| 多分辨率批量导出 | 一次导出多种分辨率版本 | 提供后台队列；每个任务独立进度与结果 |
| 云端导出预留 | 定义云导出的任务投递与状态查询接口 | 本地实现占位，网络层通过 DI 切换 |

---

## 3. 数据模型与接口

### 3.1 核心领域模型

```kotlin
/**
 * 导出配置，由用户在导出页设置并持久化到 Project。
 */
data class ExportSettings(
    val resolution: ExportResolution,
    val frameRate: Int,
    val videoBitrateMbps: Float,
    val videoCodec: VideoCodec,
    val audioBitrateKbps: Int,
    val audioSampleRate: Int,
    val includeWatermark: Boolean,
    val outputFormat: OutputFormat = OutputFormat.MP4
)

enum class ExportResolution {
    P480, P720, P1080
}

enum class VideoCodec {
    H264, H265
}

enum class OutputFormat {
    MP4, GIF, WEBM, AUDIO_ONLY
}

/**
 * 导出任务的运行时参数，包含输出路径与封面位置。
 */
data class ExportParams(
    val projectId: String,
    val settings: ExportSettings,
    val outputUri: Uri,
    val coverFrameUs: Long? = null,
    val quickMode: Boolean = false
)

/**
 * 导出会话，承载一次完整的导出生命周期。
 */
sealed class ExportState {
    data object Idle : ExportState()
    data class Preparing(val message: String) : ExportState()
    data class Progress(
        val percent: Int,
        val currentFrame: Long,
        val totalFrames: Long,
        val estimatedRemainingMs: Long
    ) : ExportState()
    data class Success(val outputUri: Uri, val coverUri: Uri?) : ExportState()
    data class Failed(val error: ExportError) : ExportState()
    data object Cancelled : ExportState()
}

sealed class ExportError {
    abstract val message: String
    data class EncodeError(override val message: String, val codecInfo: String) : ExportError()
    data class DecodeError(override val message: String, val sourceUri: Uri) : ExportError()
    data class StorageError(override val message: String, val availableBytes: Long) : ExportError()
    data class RenderError(override val message: String) : ExportError()
    data class CancelledByUser(override val message: String = "用户取消") : ExportError()
}

/**
 * 导出结果，供上层跳转结果页或分享。
 */
data class ExportResult(
    val success: Boolean,
    val outputUri: Uri?,
    val coverUri: Uri?,
    val fileSizeBytes: Long,
    val durationMs: Long,
    val actualResolution: Size,
    val actualBitrate: Long,
    val error: ExportError? = null
)
```

### 3.2 核心接口

```kotlin
/**
 * 导出器对外暴露的入口，由 Engine Layer 实现。
 */
interface VideoExporter {
    /**
     * 准备导出环境，校验参数与可用资源。
     */
    suspend fun prepare(params: ExportParams): Result<Unit>

    /**
     * 开始导出，进度通过 [progress] 回调返回。
     */
    suspend fun start(
        params: ExportParams,
        progress: (ExportState.Progress) -> Unit
    ): Result<ExportResult>

    /**
     * 取消当前导出。
     */
    fun cancel()

    /**
     * 释放编码器、Muxer、Surface 等资源。
     */
    fun release()
}

/**
 * 渲染管线适配器：将预览渲染器复用于导出，输出到编码器 Surface。
 */
interface ExportRenderer {
    fun attachOutputSurface(surface: Surface, width: Int, height: Int)
    fun renderFrame(positionUs: Long)
    fun release()
}

/**
 * 音频处理接口，由 audio 模块提供实现。
 */
interface ExportAudioMixer {
    suspend fun prepare(timeline: Timeline, sampleRate: Int, channelCount: Int)
    fun readMixedAudio(buffer: ByteBuffer, presentationTimeUs: Long): Int
    fun release()
}

/**
 * 导出任务仓库，负责任务排队、持久化与状态恢复。
 */
interface ExportTaskRepository {
    suspend fun enqueue(params: ExportParams): String
    suspend fun updateState(taskId: String, state: ExportState)
    fun observeState(taskId: String): Flow<ExportState>
    suspend fun getPendingTasks(): List<ExportTask>
}
```

### 3.3 状态管理

- UI Layer 通过 `ExportViewModel` 持有 `ExportState` 的 `StateFlow`，驱动导出页进度条、按钮、结果页跳转。
- `ExportUseCase` 调用 `VideoExporter` 并转换底层事件为 `ExportState`。
- 后台 Service 持有独立 `ExportJobManager`，通过 `ExportTaskRepository` 持久化任务状态，App 进程被杀死后重新进入可恢复显示。
- 所有 `ExportState` 必须保证在主线程安全发布；耗时操作下沉到 `Dispatchers.Default` 或引擎专用线程。

---

## 4. 交互与流程

### 4.1 用户导出操作流程

```
用户点击「导出」按钮
    ↓
进入导出设置页（分辨率/帧率/码率/水印开关）
    ↓
用户点击「开始导出」
    ↓
系统校验存储空间与编码器支持
    ↓
弹出进度页 / 切换到后台通知
    ↓
逐帧渲染 → 编码 → 混流
    ↓
导出完成：写入相册 → 跳转结果页
    ↓
用户可选择「保存并分享」或「返回编辑」
```

### 4.2 模块内部数据流

```
ExportUseCase.startExport(params)
    ↓
VideoExporter.prepare(params)
    ├─→ 检查输出目录与可用空间
    ├─→ 查询 MediaCodec 编码器（H.264/H.265）
    └─→ 创建 MediaMuxer，绑定输出文件
    ↓
打开视频编码器输入 Surface
    ↓
ExportRenderer.attachOutputSurface(surface, w, h)
    ↓
按时间线逐帧推进 positionUs
    ├─→ 解码器读取当前激活 VideoClip 的帧
    ├─→ OpenGL 合成滤镜/特效/画中画/字幕
    ├─→ 如开启水印，最后叠加水印层
    └─→ eglSwapBuffers 输出到编码器 Surface
    ↓
音频轨道同步：ExportAudioMixer 读取混音 PCM
    ↓
MediaCodec 分别输出视频/音频 ES 数据
    ↓
MediaMuxer.writeSampleData 写入 MP4
    ↓
发送 ExportState.Progress
    ↓
到达时间线结尾 → 发送 end-of-stream
    ↓
MediaMuxer.stop / release
    ↓
MediaStore 插入输出文件并刷新缩略图
    ↓
返回 ExportResult
```

### 4.3 取消与异常流程

```
用户点击取消 / 系统回收资源
    ↓
设置 isCancelled = true
    ↓
停止向编码器喂帧
    ↓
MediaCodec.signalEndOfInputStream
    ↓
等待编码器输出完成或超时 3s
    ↓
释放 MediaMuxer、编码器、Surface、解码器
    ↓
删除未完成输出文件
    ↓
发布 ExportState.Cancelled / ExportState.Failed
```

---

## 5. 实现要点

### 5.1 技术方案选择

1. **渲染管线复用**：导出渲染器应尽量复用 `player` 模块的 OpenGL 渲染链，仅在输出目标上将预览 Surface 替换为编码器输入 Surface。通过 `ExportRenderer` 接口屏蔽差异，保证预览与导出效果一致。
2. **视频编码**：优先使用 `MediaCodec` 异步模式创建 H.264 编码器，配置 `COLOR_FormatSurface` 以支持零拷贝输入。分辨率与码率按档位表映射，帧率使用 `MediaFormat.KEY_FRAME_RATE` 控制。
3. **音频编码与混音**：由 `audio` 模块提供 `ExportAudioMixer`，按时间线生成 48kHz 或 44.1kHz 双声道 PCM；再由 `MediaCodec` AAC 编码器编码；混音过程必须在独立线程执行，避免阻塞视频渲染。
4. **封装输出**：使用 `MediaMuxer` 混合视频轨与音频轨。启动 Muxer 前必须等待视频/音频格式均可用；音视频时间戳必须单调递增，防止 Muxer 异常。
5. **后台执行**：导出任务封装为 `ExportForegroundService`，通过 `WorkManager` 或自启动 Service 调度。Service 持有 `ExportJobManager`，任务进度通过 `ExportTaskRepository` 持久化并发布通知。

### 5.2 需要特别注意的难点

- **OpenGL 上下文共享**：导出通常在独立线程执行，需与播放器共享 EGLContext 或使用纹理副本，避免频繁上传 GPU 纹理造成内存抖动。
- **音视频同步**：视频帧解码和音频混音速度不同，需以 `positionUs` 为基准统一推进。若某帧解码失败，应能跳过并保持时间戳连续。
- **编码器兼容性**：部分设备对高分辨率 H.265 支持不完善，必须查询 `MediaCodecList` 并准备降级到 H.264 的策略。
- **存储空间预估**：导出前根据目标码率、时长估算所需空间，预留 20% 缓冲；不足时提前提示用户。
- **Surface 生命周期**：编码器 Surface 必须在 Muxer 启动后才能有效写入，释放顺序错误会导致 native crash。
- **文字/贴纸渲染到视频**：字幕和贴纸层必须参与导出渲染，不能仅作为预览 Overlay；需确保字体资源在导出线程可访问。

### 5.3 与现有代码的衔接建议

- 旧版导出逻辑若存在，应整体迁移到 `engine/exporter/` 包，替换为 `VideoExporter` 接口实现。
- 现有 JNI 混音能力通过 `ExportAudioMixer` 接口包装，保持 Native 代码不变，仅新增 Kotlin 适配层。
- 现有 OpenGL 滤镜链应拆分为独立的 `FrameRenderer`，`player` 与 `exporter` 共用同一实例，仅在初始化时传入不同 EGL Surface。
- `ExportSettings` 应作为 `VideoProject` 的子对象持久化到 Room，避免每次导出都重置用户选择。

---

## 6. 性能与质量要求

### 6.1 性能指标

| 指标 | 目标 | 测试方法 |
|------|------|----------|
| 导出速度 | 1 分钟 1080p 30fps 视频在中档设备导出 ≤ 45 秒 | 使用 3 段素材拼接、含滤镜+BGM 的工程 |
| 内存占用 | 导出 1080p 视频时 App 内存 ≤ 400MB | Android Studio Profiler 峰值监控 |
| 后台稳定性 | 导出期间退后台 5 分钟不崩溃 | 真机测试 + Firebase Crashlytics |
| 进度刷新 | 进度回调间隔 ≤ 500ms | 日志打点 |
| 输出文件大小 | 1080p 30fps 默认码率下 1 分钟视频 ≤ 80MB | 文件管理器/Exporter 元数据 |

### 6.2 常见异常与处理

| 异常场景 | 处理策略 |
|----------|----------|
| 编码器配置失败 | 降级到 H.264；仍失败则提示「当前设备不支持该分辨率」 |
| 存储空间不足 | 导出前预估并拦截；运行中监控，不足时保存已导出部分并提示 |
| 源文件被删除或损坏 | 跳过该 Clip 或终止导出，返回 `ExportError.DecodeError` |
| 导出过程中切换分辨率 | 禁止切换；必须取消当前任务后重新配置 |
| 音频解码速度落后 | 允许丢帧或拉伸音频，保持视频时间戳领先 |
| Muxer 写入失败 | 捕获 `IllegalStateException`，释放资源后删除损坏文件 |
| 用户强制杀进程 | Service 被回收，下次启动检测到未完成状态提示「是否继续」或「删除」 |

---

## 7. 依赖模块

| 模块 | 依赖说明 |
|------|----------|
| [timeline](../timeline/README.md) | 读取多轨道时间线、Clip 时序、轨道层级，作为导出的核心输入 |
| [editor](../editor/README.md) | 获取分割、裁剪、变速、排序等编辑结果，确保导出与预览一致 |
| [audio](../audio/README.md) | 提供 BGM、原声、录音的混音与 AAC 编码能力 |
| [filters](../filters/README.md) | 提供滤镜、美颜、LUT（预留）的 GPU Shader |
| [effects](../effects/README.md) | 提供文字、字幕、贴纸、转场、关键帧的渲染数据 |
| [player](../player/README.md) | 复用 OpenGL 渲染管线、解码器管理、Surface 管理机制 |
| [media](../media/README.md) | 素材元数据、缩略图、文件 URI 解析与 MediaStore 写入 |
| [project](../project/README.md) | 读取 `VideoProject` 与 `ExportSettings`，导出完成后更新工程状态 |
| [draft](../draft/README.md) | 导出前触发草稿持久化，确保崩溃后可恢复编辑状态 |
| [uiux](../uiux/README.md) | 遵循导出页、进度页、结果页的 UI 规范与动效 |

---

## 8. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | 2025-06-28 | 初始 export 模块规范，覆盖 MVP 导出能力与后续扩展方向 |

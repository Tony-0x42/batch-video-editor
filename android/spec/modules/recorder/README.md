# recorder 模块规范

> 本文档定义 VideoEditor-For-Android 的「相机录制」模块，覆盖相机预览、分段录制、参数配置以及从旧版 `android.hardware.Camera` 到 Camera2/CameraX 的迁移路径。

---

## 1. 模块概述

`recorder` 模块负责在 App 内部提供**短视频拍摄与分段录制能力**，是用户从「拍摄」到「进入剪辑」的首个入口。模块在整体架构中位于 **Engine Layer**（参考 `technical-spec.md` 第 2 节），向上通过 Domain/UseCase 接口为 `editor`、`project`、`draft` 等模块提供原始素材；向下直接调用 Android Camera2/CameraX API 与 MediaCodec 硬件编码器完成视频采集、滤镜渲染、音画同步和 MP4 落盘。

本模块需要解决的核心问题包括：旧版 Camera API 在新设备上的兼容性与能力受限、预览帧率与录制质量的平衡、前后摄像头切换的平滑性、分段录制的时间线拼接、麦克风采集与视频编码的时间同步，以及弱光、对焦、变焦等商用级拍摄体验。所有实现必须遵循 `project-guidelines.md` 的架构分层、线程模型与 Kotlin 优先原则，并为后续 BGM、特效、美颜滤镜预留扩展点。

---

## 2. 功能清单

### P0 — 首个 MVP 必须实现

| 功能 | 描述 | 验收标准 |
|------|------|----------|
| 相机预览 | 使用 Camera2/CameraX 渲染预览流到 Surface，支持全屏/9:16/16:9 裁切 | 预览帧率 ≥ 24fps（中档设备 720p），前后摄切换 ≤ 1s |
| 普通录制 | 单次按下录制，视频 + 麦克风音频同时编码为 MP4 | 输出 H.264/AAC、720p/30fps、无音画不同步 |
| 分段录制 | 支持多次暂停/继续，每次生成独立片段文件，最终可进入编辑页拼接 | 片段切换 ≤ 300ms，片段文件列表在 UI 实时更新 |
| 前后摄像头切换 | 录制前后均可切换前后摄 | 切换过程预览不黑屏超过 500ms，录制状态可保留或友好中断 |
| 分辨率/帧率配置 | 提供 720p@30fps、1080p@30fps 两种档位 | 用户在设置页切换后，下次录制生效；不支持档位给出提示 |
| 闪光灯开关 | 自动/打开/关闭三档 | 切换即时生效，录制开始后仍可调 |
| 对焦与曝光 | 点击屏幕区域触发对焦 + 曝光测光 | 对焦框显示 1.5s，对焦成功率 ≥ 90%（有 AF 设备） |
| 美颜/滤镜实时预览 | 复用现有 GPU 滤镜链，在拍摄预览时叠加 | 滤镜切换 ≤ 200ms，美颜等级 0~5 可调 |
| 录制进度与最长时间 | 顶部显示录制进度条，最长 60s（默认） | 到达最大时长自动停止，并给出提示 |
| 保存并进入编辑 | 录制完成后将片段写入草稿或直接进入时间线 | 文件保存到应用私有目录，可被 `media` 模块索引 |

### P1 — MVP 后 1~2 个迭代

| 功能 | 描述 | 验收标准 |
|------|------|----------|
| 变焦（双指/滑杆） | 支持 smooth zoom 与 digital zoom | 变焦范围 1x~5x，帧率波动 ≤ 5fps |
| 倒计时拍摄 | 3s/10s 倒计时后自动开始录制 | 倒计时期间预览稳定，UI 显示同步 |
| 录制水印开关 | 拍摄时是否叠加工程水印 | 与 `export` 模块水印策略一致 |
| 录制画幅选择 | 1:1 / 4:3 / 9:16 / 16:9 | 与 `editor` 画布比例对齐 |
| 录制参数记忆 | 记住用户上次选择的分辨率、滤镜、美颜 | 使用 DataStore 持久化 |

### P2 — 长期规划

| 功能 | 描述 | 验收标准 |
|------|------|----------|
| 慢动作/延时摄影 | 通过控制帧率与编码时间戳实现 | 输出文件播放效果正确 |
| 实时特效/贴纸 | 在录制预览叠加动态贴纸或特效 | 帧率下降 ≤ 20% |
| 提词器 | 录制界面半透明悬浮提词 | 可滚动、可调整字号 |
| 直播推流 | RTMP/RTC 实时推流 | 不在本模块直接实现，仅预留编码器输出接口 |

---

## 3. 数据模型与接口

### 3.1 核心领域模型

```kotlin
/**
 * 录制会话配置，由 UI/ViewModel 组装后传入 RecorderEngine。
 */
data class RecordSessionConfig(
    val outputDir: File,                       // 片段输出目录
    val resolution: RecordResolution,          // 分辨率档位
    val frameRate: Int,                        // 目标帧率
    val videoBitRate: Int,                     // 视频码率 bps
    val audioSampleRate: Int = 48_000,         // 音频采样率
    val audioChannelCount: Int = 2,            // 音频通道数
    val maxDurationMs: Long = 60_000,          // 最大录制时长
    val lensFacing: LensFacing = LensFacing.BACK,
    val flashMode: FlashMode = FlashMode.OFF,
    val beautyLevel: Int = 0,                  // 0~5
    val filterId: String? = null
)

enum class RecordResolution { P720, P1080 }
enum class LensFacing { FRONT, BACK }
enum class FlashMode { OFF, ON, AUTO }

/**
 * 单个录制片段，作为时间线 Clip 的原始素材来源。
 */
data class RecordClip(
    val id: String,
    val file: File,
    val durationMs: Long,
    val creationTime: Long,
    val cameraFacing: LensFacing,
    val resolution: RecordResolution
)

/**
 * 录制会话状态，供 UI 订阅。
 */
sealed class RecordState {
    object Idle : RecordState()
    object Previewing : RecordState()
    data class Recording(
        val elapsedMs: Long,
        val progress: Float,
        val currentClipIndex: Int
    ) : RecordState()
    data class Paused(val totalRecordedMs: Long) : RecordState()
    data class Finalizing(val clipCount: Int) : RecordState()
    data class Error(val exception: VideoEditorException) : RecordState()
}
```

### 3.2 Recorder 引擎接口

```kotlin
/**
 * 录制引擎接口。Domain 层定义，由 Engine Layer 实现。
 */
interface RecorderEngine {
    /**
     * 初始化相机与预览管道。
     * @param previewSurface 用于 Camera2/CameraX 输出的 Surface，通常来自 TextureView/SurfaceView。
     */
    suspend fun prepare(config: RecordSessionConfig, previewSurface: Surface): Result<Unit>

    /** 开始预览。 */
    suspend fun startPreview(): Result<Unit>

    /** 停止预览并释放相机会话。 */
    suspend fun stopPreview(): Result<Unit>

    /** 开始一段新的录制。 */
    suspend fun startRecording(): Result<Unit>

    /** 暂停当前录制，生成一个片段文件。 */
    suspend fun pauseRecording(): Result<RecordClip>

    /** 从暂停恢复，继续录制下一段。 */
    suspend fun resumeRecording(): Result<Unit>

    /** 停止全部录制并返回所有片段。 */
    suspend fun stopRecording(): Result<List<RecordClip>>

    /** 切换前后摄像头。 */
    suspend fun switchCamera(): Result<Unit>

    /** 设置闪光灯模式。 */
    suspend fun setFlashMode(mode: FlashMode): Result<Unit>

    /** 设置对焦与测光区域，坐标为预览 Surface 的相对坐标 [0,1]。 */
    suspend fun setFocusAndMetering(x: Float, y: Float): Result<Unit>

    /** 设置数字变焦倍数。 */
    suspend fun setZoom(zoomRatio: Float): Result<Unit>

    /** 设置滤镜与美颜。 */
    suspend fun setFilter(filterId: String?, beautyLevel: Int): Result<Unit>

    /** 实时状态流。 */
    val state: StateFlow<RecordState>
}
```

### 3.3 与现有代码的衔接接口

```kotlin
/**
 * 将 recorder 产生的 RecordClip 转换为 timeline 可识别的 VideoClip。
 * 由 editor/media 模块消费。
 */
interface RecordClipImporter {
    suspend fun import(recordClips: List<RecordClip>, project: VideoProject): Result<List<VideoClip>>
}
```

---

## 4. 交互与流程

### 4.1 关键用户操作流程

```
用户进入拍摄页
    ↓
申请相机/麦克风权限（动态申请）
    ↓
Engine.prepare() 初始化 Camera2 Session + OpenGL 预览管道
    ↓
用户点击录制 → startRecording()
    ↓
用户点击暂停 → pauseRecording()，生成 RecordClip #1
    ↓
用户继续录制 → resumeRecording()
    ↓
用户点击完成 → stopRecording()，返回 List<RecordClip>
    ↓
选择「进入编辑」→ RecordClipImporter 转为 VideoClip 加入 Timeline
    ↓
或选择「保存草稿」→ 片段写入 draft 私有目录，更新 Project 元数据
```

### 4.2 模块内部数据流

```
Camera2/CameraX 输出帧
    ↓
SurfaceTexture → OpenGL 外部纹理
    ↓
CameraDrawer 应用滤镜/美颜/变换
    ↓
┌─────────────────────┐
│ 预览分支：渲染到预览 Surface │
└─────────────────────┘
           ↓
┌─────────────────────┐
│ 录制分支：渲染到 MediaCodec 输入 Surface │
└─────────────────────┘
           ↓
VideoEncoderCore（H.264） + AudioRecord（AAC）
           ↓
MediaMuxer 混流为当前片段 MP4
           ↓
生成 RecordClip 并通知状态流
```

### 4.3 时序伪代码

```text
UI Thread:          RecorderEngine Thread:          GL/Encoder Thread:
    │                         │                              │
    │ prepare(config, surface)│                              │
    ├────────────────────────►│                              │
    │                         │ openCamera()                 │
    │                         │ createCaptureSession()       │
    │                         ├─────────────────────────────►│
    │                         │                              │ init EglCore + shaders
    │                         │ startPreview()               │
    │                         ├─────────────────────────────►│
    │                         │                              │ requestRender loop
    │ startRecording()        │                              │
    ├────────────────────────►│                              │
    │                         │ startMediaCodecEncoder()     │
    │                         ├─────────────────────────────►│
    │                         │                              │ drain video + audio
    │ pauseRecording()        │                              │
    ├────────────────────────►│                              │
    │                         │ finalize current segment     │
    │                         │ emit RecordClip              │
    │◄────────────────────────┤                              │
```

---

## 5. 实现要点

### 5.1 技术方案选择

1. **相机 API 迁移策略**
   - 新模块统一使用 **Camera2 API**（`android.hardware.camera2`），最低 SDK 24，可完整控制 ISO、曝光、对焦、变焦。
   - 在 Camera2 兼容性极差的少数旧设备上，可保留旧 `CameraController` 作为降级方案，但需在 MVP 后统一移除。
   - 不建议在 MVP 阶段直接引入 CameraX 作为唯一方案，因其对 MediaCodec 输入 Surface 的自定义控制粒度不足；可将 CameraX 作为 P1 的可选封装。

2. **预览与录制复用同一条 OpenGL 管线**
   - 复用现有 `CameraDrawer` / `MagicCameraInputFilter` 的 OES 纹理处理逻辑。
   - 通过 `Framebuffer Object（FBO）` 一次绘制，同时输出到屏幕预览 Surface 与编码器输入 Surface，避免重复 GPU 计算。

3. **分段录制的时间戳连续性**
   - 每个片段独立编码为 MP4，片段间时间戳不连续；后续由 `timeline` / `editor` 模块按顺序拼接。
   - 录制暂停时立即调用 `signalEndOfInputStream()` 并 `drainEncoder(true)`，确保文件完整可播放。

4. **音视频同步**
   - 视频时间戳以 `SurfaceTexture.getTimestamp()` 或系统 `nanoTime` 为基准；音频时间戳以 `AudioRecord` 读取回调累计采样数计算。
   - 两者统一换算为微秒（µs）后写入 MediaMuxer；暂停期间不计入时间戳。

### 5.2 需要特别注意的难点

- **前后摄切换的黑屏问题**：Camera2 切换需关闭旧 Session 再创建新 Session，期间应显示最后一帧占位图或加载动画，避免纯白/黑屏。
- **闪光灯与自动曝光冲突**：打开闪光灯时，需将 AE 模式设为 `CONTROL_AE_MODE_ON_ALWAYS_FLASH` 或 `CONTROL_AE_MODE_ON_AUTO_FLASH`，避免画面过曝。
- **前置摄像头镜像**：预览阶段保持镜像，录制文件需通过 OpenGL 矩阵做水平翻转，避免导出后文字反向。
- **录音权限与占用**：Android 10+ 对麦克风后台使用有严格限制；录制页必须处于前台，且 onPause 时自动暂停录制。
- **Surface 生命周期**：Activity/Fragment 生命周期与 GL 线程解耦，需在 `onPause`/`onResume` 时正确释放/重建 Camera Session，防止泄漏与 ANR。

### 5.3 与现有代码的衔接建议

- 将现有 `CameraController` 的实现逻辑迁移到 `engine/recorder/Camera2Device.kt`，保留 `ICamera` 中「尺寸选择、对焦」等算法作为参考，但废弃 `android.hardware.Camera` 的直接调用。
- `TextureMovieEncoder` 的 HandlerThread + EGL 录制模型值得保留，但需改造为 Kotlin Coroutines + `StateFlow` 状态管理，并拆分视频编码、音频编码、Muxer 为独立类。
- `VideoEncoderCore` 中硬编码的 `FRAME_RATE`、`IFRAME_INTERVAL`、`MIME_TYPE` 等常量应迁移到 `RecordSessionConfig` 中作为可配置项。
- 现有美颜/滤镜类（`MagicBeautyFilter`、`SlideGpuFilterGroup`）可直接接入新的 Recorder OpenGL 管线，但需通过接口 `RecorderFilter` 抽象，避免 UI 层直接依赖具体滤镜实现。

---

## 6. 性能与质量要求

### 6.1 性能指标

| 场景 | 目标 | 测试方法 |
|------|------|----------|
| 预览启动时间 | 从页面可见到首帧预览 ≤ 1.5s | 手动计时 + Systrace |
| 录制帧率 | 720p/1080p 预览与录制均 ≥ 24fps | 自定义 FPS 计数 |
| 分段录制切换延迟 | 暂停/恢复操作到状态变更 ≤ 300ms | 日志打点 |
| 前后摄切换 | ≤ 1s | 手动计时 |
| 内存占用 | 录制 1080p 时 ≤ 250MB | Android Studio Profiler |
| 发热控制 | 连续录制 3 分钟无明显掉帧 | 真机测试 |

### 6.2 常见异常与处理

| 异常 | 原因 | 处理策略 |
|------|------|----------|
| CameraAccessException | 相机被占用、权限未授予、设备无相机 | 提示用户检查权限/关闭其他应用；返回 `RecordState.Error` |
| EncoderConfigurationException | 设备不支持目标分辨率/码率 | 降级到 720p 或提示用户更换参数 |
| AudioRecord initialization failed | 麦克风被占用或权限问题 | 仅录制无声视频，并提示用户 |
| Muxer stop failed (no data) | 未写入任何帧即停止 | 捕获异常，删除空文件，状态回退到 Previewing |
| OutOfMemory during recording | 长视频或高分辨率导致 | 限制单段最大时长，分段录制，监控内存 |
| Surface abandoned | Activity 销毁或后台切换 | 在 `onPause` 安全释放，避免崩溃 |

---

## 7. 依赖模块

本模块依赖以下 `spec/modules` 下的模块，并与其保持接口解耦：

| 模块 | 依赖说明 |
|------|----------|
| [filters](../filters/) | 复用滤镜/美颜的 GPU Shader 与参数定义，录制预览与导出效果一致 |
| [media](../media/) | 录制完成后由 media 模块扫描、生成缩略图，并提供给时间线 |
| [project](../project/) | 录制配置与片段元数据需写入 Project 模型 |
| [draft](../draft/) | 支持「保存草稿」流程，自动保存未进入编辑的录制片段 |
| [editor](../editor/) | 录制完成后可进入 editor，RecordClip 需转换为 VideoClip |
| [timeline](../timeline/) | 多轨道时间线模型接收 RecordClip 生成的 Clip 列表 |
| [uiux](../uiux/) | 拍摄页按钮布局、录制进度条、对焦动画等遵循 UI/UX 设计系统 |

---

## 8. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | 2025-06-28 | 初始 recorder 模块规范，明确 Camera2 迁移、分段录制、接口与性能指标 |

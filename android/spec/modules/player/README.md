# player 模块规范

> 本规范定义 VideoEditor-For-Android 实时预览播放器（Player）模块的职责、接口、数据流与性能要求。Player 是剪辑主界面的核心预览引擎，负责将 timeline 上的多轨道片段、滤镜、特效、音频实时合成为用户可见可听的预览输出。

---

## 1. 模块概述

Player 模块承担**编辑期实时预览**职责，位于 Engine Layer 最上游，直接面向 UI 层的 `EditorPreview` 组件。它以 `Timeline` 当前播放位置为驱动，按时间线顺序激活对应视频/图片片段、音频片段和特效，调用 Renderer 完成一帧图像合成，并同步输出到 Surface；音频部分则通过 AudioTrack 或 OpenSL ES 实时混音播放。

Player 解决的核心问题包括：

1. **所见即所得**：用户在时间线上做任何剪辑、滤镜、字幕调整后，必须在预览区立即看到接近最终导出的效果。
2. **精准 Seek**：支持毫秒级拖动定位、逐帧进退，方便用户精确找到剪辑点。
3. **多轨道同步**：主视频轨、画中画轨、音频轨、文字轨在同一时间基准下同步渲染与播放。
4. **性能与功耗平衡**：编辑预览不是全精度导出，需在帧率、画质、耗电之间取得平衡，保证中档设备 1080p 时间线 ≥ 24fps。

Player 不直接处理文件持久化、工程保存、导出编码，也不负责时间线模型的增删改，这些分别由 `project`、`draft`、`export`、`timeline`、`editor` 模块负责。

---

## 2. 功能清单

### P0 - 首个 MVP 必须实现

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 实时预览播放 | 根据 Timeline 当前位置连续渲染视频帧到预览 Surface，并同步播放音频 | 1080p 时间线在中档设备预览帧率 ≥ 24fps；音视频同步误差 ≤ 40ms |
| 播放/暂停控制 | 提供 `play()`、`pause()`、`toggle()` 接口，受 UI 播放按钮控制 | 点击播放按钮后 100ms 内开始渲染；暂停后立即定格当前帧 |
| Seek 定位 | 支持拖动进度条、点击时间线、输入时间三种方式定位到任意时间点 | Seek 到 1 分钟视频任意位置耗时 ≤ 200ms；定位后画面立即刷新 |
| 逐帧进退 | 提供 `nextFrame()` / `previousFrame()`，用于精确找剪辑点 | 每次调用前进/后退一帧（按项目帧率），画面刷新延迟 ≤ 100ms |
| Surface 生命周期管理 | 适配 `SurfaceView` / `TextureView`，处理 surfaceCreated / changed / destroyed | Surface 重建后 200ms 内恢复预览；不泄漏 Surface / EGLContext |
| 播放范围限制 | 支持仅预览选中片段或时间线入点/出点范围 | 循环播放范围内内容，范围边界切换无黑帧、无跳音 |
| 播放状态回调 | 通过 Flow 向外暴露 `IDLE`、`PREPARING`、`PLAYING`、`PAUSED`、`SEEKING`、`ERROR` 状态 | UI 能及时响应状态变化；状态切换有明确日志 Tag `VE:Player` |
| 基础音视频同步 | 以音频时钟或系统时钟为基准，校正视频渲染节奏 | 连续播放 1 分钟以上无明显音画不同步 |

### P1 - 重要增值

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 变速预览 | 支持以 0.25x ~ 4x 速度预览时间线，反映片段速度调整 | 变速播放时音频同步不失真；画面帧率保持 ≥ 20fps |
| 倒放预览 | 支持片段倒放预览，用于倒放效果编辑 | 倒放 5 秒片段无卡顿；不支持音频时给出静音提示 |
| 定格/静帧预览 | 在定格点显示单帧画面，音频继续或停止按配置执行 | 定格起止时间精确到帧；切换无闪烁 |
| 画中画实时合成 | 主视频轨 + 画中画轨在同一帧中按层级合成 | 画中画缩放/旋转/位置调整后实时可见 |
| 字幕/贴纸叠加 | 在预览上叠加文字、贴纸轨道内容 | 字体、颜色、描边、阴影、位置、旋转实时生效 |
| 预缓冲机制 | 在 Seek 或暂停时预加载后续若干帧，减少播放启动延迟 | 预缓冲 10 帧内，播放启动延迟 ≤ 50ms |
| 后台音频继续 | 应用切到后台后可选仅播放音频（如录音监听场景） | 后台播放不中断；返回前台后 100ms 内恢复画面 |

### P2 - 长期规划

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 关键帧动画预览 | 实时渲染滤镜/位置/缩放的关键帧插值结果 | 关键帧曲线播放平滑，无抖动 |
| 复杂转场实时预览 | 两段视频间转场在预览中实时呈现 | 转场效果与导出效果差异 ≤ 5% |
| LUT/高级调色预览 | 实时应用 LUT 与调色参数 | LUT 预览帧率 ≥ 20fps |
| 多机位/多分辨率适配 | 自动适配不同分辨率素材的缩放与裁剪 | 预览画面与导出画面像素级一致 |
| 外部显示器输出 | 支持 HDMI/USB-C 投屏预览 | 投屏画面与手机屏幕同步，延迟 ≤ 100ms |

---

## 3. 数据模型与接口

### 3.1 核心状态

```kotlin
/**
 * 播放器状态机
 */
enum class PlayerState {
    IDLE,           // 初始或释放后
    PREPARING,      // 加载 Timeline、初始化解码器
    READY,          // 可播放
    PLAYING,        // 正在播放
    PAUSED,         // 暂停
    SEEKING,        // 正在 Seek
    ERROR           // 发生错误
}

/**
 * 播放器配置
 */
data class PlayerConfig(
    val enableAudio: Boolean = true,
    val enableVideo: Boolean = true,
    val loopRange: ClosedRange<Long>? = null,   // 微秒
    val playbackSpeed: Float = 1.0f,
    val targetFps: Int = 30,
    val lowResolutionPreview: Boolean = false    // P1：降低预览分辨率以提升帧率
)

/**
 * 播放进度与元数据
 */
data class PlayerProgress(
    val positionUs: Long,
    val durationUs: Long,
    val state: PlayerState,
    val isPlaying: Boolean,
    val currentClipId: String? = null,
    val error: PlayerError? = null
)

sealed class PlayerError(val message: String) {
    class DecodeError(message: String) : PlayerError(message)
    class RenderError(message: String) : PlayerError(message)
    class SurfaceError(message: String) : PlayerError(message)
    class AudioError(message: String) : PlayerError(message)
}
```

### 3.2 Player 对外接口

```kotlin
/**
 * 实时预览播放器对外暴露的接口
 */
interface VideoPlayer {

    /** 播放器状态流 */
    val stateFlow: StateFlow<PlayerState>

    /** 播放进度流，UI 据此更新进度条与当前时间显示 */
    val progressFlow: StateFlow<PlayerProgress>

    /**
     * 绑定用于渲染的 Surface
     * @param surface 由 SurfaceView/TextureView 提供
     */
    fun setSurface(surface: Surface)

    /**
     * 设置待预览的时间线
     */
    fun setTimeline(timeline: Timeline)

    /**
     * 设置播放器配置
     */
    fun setConfig(config: PlayerConfig)

    /** 准备资源，异步完成 */
    suspend fun prepare()

    /** 开始播放 */
    fun play()

    /** 暂停播放 */
    fun pause()

    /** 播放/暂停切换 */
    fun toggle()

    /**
     * Seek 到指定时间
     * @param positionUs 目标时间，微秒
     * @param accurate 是否精确 Seek（解码到关键帧后再精确定位）
     */
    fun seekTo(positionUs: Long, accurate: Boolean = false)

    /** 前进一帧 */
    fun nextFrame()

    /** 后退一帧 */
    fun previousFrame()

    /** 释放所有资源 */
    fun release()
}
```

### 3.3 内部核心组件

```kotlin
/**
 * 负责按时间线当前位置调度 Clip 与 Effect
 */
interface TimelineScheduler {
    fun setTimeline(timeline: Timeline)
    fun getActiveClipsAt(positionUs: Long): List<Clip>
    fun getActiveEffectsAt(positionUs: Long): List<Effect>
    fun getCurrentClipAt(positionUs: Long): Clip?
}

/**
 * 视频帧渲染器抽象，Player 将当前应显示的帧交给 Renderer 处理
 */
interface FrameRenderer {
    fun init(eglContext: EglContext, outputSurface: Surface)
    fun renderFrame(positionUs: Long, clips: List<Clip>, effects: List<Effect>)
    fun release()
}

/**
 * 音频播放器抽象
 */
interface AudioPlayer {
    fun prepare(audioClips: List<AudioClip>, positionUs: Long)
    fun play()
    fun pause()
    fun seekTo(positionUs: Long)
    fun setSpeed(speed: Float)
    fun release()
    val currentPositionUs: Long
}
```

### 3.4 与其他模块的依赖接口

| 依赖模块 | 接口/数据 | 说明 |
|---------|----------|------|
| timeline | `Timeline`、`Track`、`Clip` | Player 只读访问时间线模型，不修改 |
| renderer | `FrameRenderer` | 将 OpenGL 合成交给 renderer 模块，Player 负责调度 |
| audio | `AudioPlayer`、`AudioMixer` | 音频解码、混音、播放由 audio 模块实现 |
| filters | `FilterEffect` 参数 | Player 读取当前生效的滤镜参数传给 Renderer |
| effects | `TextEffect`、`StickerEffect` | 文字、贴纸轨道信息传给 Renderer 叠加 |
| project | `VideoProject` | 从工程获取 CanvasConfig、导出设置等预览上下文 |

---

## 4. 交互与流程

### 4.1 用户操作流程

#### 4.1.1 进入剪辑页并自动播放

1. 用户从素材选择页进入 `EditorActivity`。
2. `EditorViewModel` 构建默认 `Timeline` 并调用 `VideoPlayer.setTimeline(timeline)`。
3. `EditorPreview` 组件创建 Surface 并调用 `VideoPlayer.setSurface(surface)`。
4. Player 进入 `PREPARING` 状态，初始化解码器与 OpenGL 上下文。
5. 准备完成后状态变为 `READY`，自动从 0 开始播放或保持暂停（按产品定义）。

#### 4.1.2 用户拖动时间线 Seek

1. 用户在时间轴上拖动游标。
2. `EditorViewModel` 持续调用 `VideoPlayer.seekTo(positionUs, accurate = false)`。
3. Player 解码到最近关键帧并渲染，保持 `PAUSED` 或 `SEEKING` 状态。
4. 用户松手后，若需精确帧可再调用一次 `seekTo(positionUs, accurate = true)`。
5. 画面更新后 Player 回到 `PAUSED` 状态。

#### 4.1.3 用户调整滤镜强度

1. 用户在滤镜面板调整强度滑块。
2. `EditorViewModel` 通过 `UpdateFilterCommand` 修改 `Timeline` 模型。
3. `TimelineRepository` 通知 Player：`onTimelineChanged(timeline)`。
4. Player 刷新当前激活的 `FilterEffect` 列表，下一帧渲染使用新参数。
5. 画面实时变化，无需重新解码。

### 4.2 模块内部数据流

```
Timeline（只读）
    ↓
TimelineScheduler 计算当前 positionUs 下激活的 Clip / Effect
    ↓
VideoDecoder（MediaCodec 硬解）读取源素材帧
    ↓
FrameRenderer（OpenGL）执行滤镜、画中画、字幕合成
    ↓
EGL Surface → 手机屏幕预览

AudioDecoder / AudioMixer（独立线程）
    ↓
AudioTrack 播放混合音频

播放调度线程（主控）
    ↓
维护系统时钟或音频时钟，循环驱动 positionUs 前进
    ↓
每帧触发 Scheduler + Decoder + Renderer + 进度上报
```

### 4.3 关键时序（初始化到播放）

```
UI Thread                Player Thread                GL Thread                Audio Thread
  |                            |                            |                        |
  |-- setTimeline(timeline) -->|                            |                        |
  |-- setSurface(surface) ---->|                            |                        |
  |-- prepare() -------------->|                            |                        |
  |                            |-- init scheduler --------->|                        |
  |                            |-- init decoder/egl ------->|-- create EGLContext    |
  |                            |-- init audio player --------------------------------->|-- prepare
  |                            |                            |                        |
  |<-- state=READY ------------|                            |                        |
  |-- play() ----------------->|                            |                        |
  |                            |-- start render loop        |                        |
  |                            |-- positionUs += delta      |                        |
  |                            |-- request frame ---------->|-- renderFrame()        |
  |                            |-- update progress -------->|                        |
  |                            |                            |                        |
  |<-- progressFlow ---------->|                            |                        |
```

---

## 5. 实现要点

### 5.1 技术方案选择

1. **视频渲染**：基于 OpenGL ES 3.0 / 2.0，使用自定义 `GLSurfaceView` 或 `TextureView` + 独立 EGL 上下文。优先使用 `SurfaceView` 以获得更高渲染效率，画中画/字幕通过 FBO 离屏合成后再上屏。
2. **视频解码**：使用 `MediaCodec` 异步模式硬解码，按 Clip 源时间范围精确 `seekTo` 并 `queueInputBuffer`。多 Clip 场景下维护一个解码器池或单解码器复用，避免频繁创建释放。
3. **音频播放**：解码后 PCM 数据通过 `AudioTrack` 或 `OpenSL ES` 输出。变速/倒放通过 audio 模块的 Sonic/自研 JNI 处理，Player 只负责同步位置。
4. **时间基准**：优先以音频时钟为基准校正视频帧显示时间；当静音或纯视频场景时退化为系统时钟。
5. **线程模型**：
   - 主线程：UI 调用与状态回调。
   - Player 线程：播放循环、Seek 调度、进度计算。
   - GL 线程：OpenGL 渲染与纹理上传。
   - Audio 线程：音频解码与播放。

### 5.2 需要特别注意的难点

1. **多 Clip 无缝衔接**：两个相邻 VideoClip 切换时，必须保证解码器在上一 Clip 结束前一帧已准备好下一 Clip 的首帧，避免黑帧或闪屏。建议维护「当前 Clip」与「下一个 Clip」双缓冲。
2. **Seek 精度与性能平衡**：精确 Seek 需要解码到目标帧，1080p 视频可能耗时 100~300ms。UI 拖动期间使用非精确 Seek（关键帧），松手后做一次精确 Seek。
3. **Surface 重建**：Android 屏幕旋转、分屏、Home 键都会导致 Surface 销毁重建。Player 必须保存当前 positionUs，重建后快速恢复，且不重复初始化解码器。
4. **OpenGL 上下文共享**：滤镜、LUT、贴纸纹理等资源在 Player 与 Exporter 之间可共享，避免重复加载。建议通过 EGLSharedContext 设计共享机制。
5. **变速与音频同步**：当片段 speed ≠ 1.0 时，Clip 在 Timeline 上的有效时长 = sourceDuration / speed。Scheduler 必须将 positionUs 正确映射回源素材时间。
6. **内存控制**：1080p 视频每帧约 8MB，解码器缓冲、FBO、纹理池必须设置上限。建议最大同时解码器数 ≤ 2，纹理池按 LRU 回收。

### 5.3 与现有代码的衔接建议

- 旧版 `android.hardware.Camera` 录制产生的视频文件可正常作为 `VideoClip` 源素材导入 Player 预览。
- 现有 OpenGL 滤镜 Shader 建议迁移到 renderer 模块的 `FilterPass` 中，Player 通过 `FrameRenderer` 接口调用，不再直接管理 Shader。
- 现有 JNI 音频混音能力保留在 audio 模块，`AudioPlayer` 实现中通过 JNI 调用混音逻辑。
- 旧版两段视频拼接逻辑迁移为 `Timeline` 模型 + editor 模块的排序/拼接命令，Player 只负责预览最终时间线，不再独立维护拼接状态。

---

## 6. 性能与质量要求

### 6.1 性能指标

| 指标 | 目标值 | 测试方法 |
|------|--------|---------|
| 预览帧率 | ≥ 24fps @ 1080p（中档设备） | 自定义 FPS 计数器 + Systrace |
| 首帧显示 | 从 setTimeline 到首帧显示 ≤ 500ms | 日志打点 |
| Seek 响应 | 非精确 Seek ≤ 50ms；精确 Seek ≤ 300ms | 日志打点 |
| 逐帧延迟 | nextFrame/previousFrame 画面刷新 ≤ 100ms | 日志打点 |
| 音视频同步 | 连续播放 1 分钟误差 ≤ 40ms | 耳听 + 特殊测试片源 |
| 内存占用 | 预览 1080p 时间线 ≤ 250MB | Android Studio Profiler |
| CPU 占用 | 预览期间平均 CPU ≤ 35% | Profiler |
| Surface 重建恢复 | ≤ 200ms | 旋转屏幕后测量 |

### 6.2 常见异常与处理

| 异常场景 | 原因 | 处理策略 |
|---------|------|---------|
| 解码器初始化失败 | 设备不支持该分辨率/编码格式 | 降级到软解提示或弹出「该设备不支持硬解此视频」 |
| Surface 无效/已释放 | 用户切换页面、锁屏、旋转 | 暂停播放并等待新 Surface；恢复时从原 positionUs 继续 |
| 音视频不同步 | 解码器输出时间戳异常/音频卡顿 | 以音频时钟重新校正；持续异常则暂停并提示 |
| Seek 超时 | 目标位置附近无关键帧或文件损坏 | 中断 Seek，回到上一次有效位置，上报 `DecodeError` |
| 内存不足 | 高清素材 + 多轨道导致 OOM | 降低预览分辨率、限制解码器数量、清理未使用纹理 |
| 播放中 Timeline 变更 | 用户删除/分割片段 | 立即重新调度；若当前位置不再有效，则 Seek 到最近有效帧 |
| 后台播放画面冻结 | 应用进入后台 | 可选暂停视频仅保留音频，或整体暂停；返回前台恢复 |

### 6.3 日志与监控

- 统一 Tag：`VE:Player`。
- 所有状态切换、Seek 起止、错误必须输出 INFO 或 ERROR 级别日志。
- 发布版本通过 Firebase Crashlytics 上报 `PlayerError` 类型崩溃与 ANR。

---

## 7. 依赖模块

| 模块 | 依赖内容 | 依赖方式 |
|------|---------|---------|
| timeline | `Timeline`、`Track`、`Clip` 领域模型及时间计算 | 通过 Domain 层只读依赖 |
| renderer | `FrameRenderer` 接口、OpenGL 合成管线 | 通过 Domain/Engine 接口调用 |
| audio | `AudioPlayer`、`AudioMixer`、音频解码/变速/混音 | 通过 Domain/Engine 接口调用 |
| filters | `FilterEffect` 参数定义 | 通过 Domain 模型依赖 |
| effects | `TextEffect`、`StickerEffect`、`TransitionEffect` 定义 | 通过 Domain 模型依赖 |
| project | `VideoProject`、`CanvasConfig` | 通过 UseCase 层获取当前工程上下文 |
| media | 素材元数据、缩略图、媒体文件 URI 解析 | 通过 Repository 接口依赖 |
| uiux | 预览区组件尺寸、比例、手势交互规范 | 规范层面约束，代码通过 UI 层解耦 |

---

## 8. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | 2025-06-28 | 初始 player 模块规范，覆盖 MVP 实时预览、Seek、逐帧、Surface 管理与性能指标 |

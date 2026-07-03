# audio 模块规范

> 本文件定义 VideoEditor-For-Android 项目中音频处理模块的职责边界、数据模型、接口协议、交互流程、实现要点与性能质量要求。所有涉及音频相关的需求、设计与实现必须以本文档为首要依据。

---

## 1. 模块概述

`audio` 模块负责视频剪辑过程中全部音频相关的采集、解码、处理、合成与编码工作。它在整体架构中位于 **Engine Layer**，通过 `AudioProcessor` 接口被 `UseCase` 与 `Exporter` 调用，同时依赖 `timeline` 模块提供的多轨道时间线模型以确定音频片段的排列、裁剪与叠加关系。该模块解决的核心问题包括：如何在移动端高效处理多源音频（视频原声、本地 BGM、录音、音效）的时序对齐、音量包络、淡入淡出、变速变调与最终混音输出，确保预览与导出音质一致、 lipsync 准确、资源占用可控。

---

## 2. 功能清单

### P0 — 首个 MVP 必须实现

| 名称 | 描述 | 验收标准 |
|------|------|----------|
| **本地 BGM 添加** | 支持从本地相册/文件选择 MP3/AAC/WAV 作为背景音，拖入音频轨并调整在时间线上的起止位置 | 导入后可在时间线音频轨显示波形缩略图；播放时与视频同步；导出后 BGM 正常播放 |
| **音频提取** | 从已导入的视频片段中提取原声音轨，生成独立音频 Clip 放入音频轨 | 提取过程在后台完成，1 分钟 1080p 视频提取 ≤ 3s；提取后的音频可单独调节音量与裁剪 |
| **音量调节** | 对单个音频 Clip 设置整体音量增益，范围 0% ~ 200%，默认 100% | 实时预览与导出的音量变化一致；静音时无爆音 |
| **基础混音** | 将多条音频轨（视频原声、BGM）按时间线叠加，输出混合后的单/双声道 PCM | 两条音频轨同时存在时不出现 clipping 爆音；混音结果与商业播放器听感一致 |
| **淡入淡出** | 对音频 Clip 设置头部淡入与尾部淡出，支持线性/指数曲线，时长 0~2s 可调 | 淡入淡出起始/结束点无咔哒声；预览与导出效果一致 |

### P1 — MVP 后 1~2 个迭代

| 名称 | 描述 | 验收标准 |
|------|------|----------|
| **录音旁白** | 使用 `AudioRecord` 实时录制人声，生成录音 Clip 插入音频轨指定位置 | 录音与预览画面同步；支持回听、重录、删除；录音文件保存为 WAV/AAC |
| **音效库** | 内置分类音效（转场、搞笑、环境等），支持本地音效导入，作为短音频 Clip 叠加 | 音效可设置入点、持续时长、音量；音效文件体积 ≤ 500KB/个 |
| **音频裁剪** | 在音频 Clip 上设置源素材入点/出点，支持双指缩放精修 | 裁剪后波形图同步更新；裁剪精度达到 ±40ms |
| **音频分割** | 在时间线指定位置将音频 Clip 切分为两段 | 分割后两段 Clip 可独立移动、删除、调节音量；无损分割不重新编码 |
| **静音/独奏** | 对音频轨或单个 Clip 设置静音/独奏状态 | UI 状态实时反馈；导出时仅输出非静音且未被独奏屏蔽的轨道 |

### P2 — 长期规划

| 名称 | 描述 | 验收标准 |
|------|------|----------|
| **AI 配音/字幕配音** | 接入 TTS 能力，将字幕文本转换为语音轨道 | 支持多音色、语速调节；输出与字幕时间轴对齐 |
| **音频降噪** | 对录音或视频原声进行环境噪声抑制 | 信噪比提升 ≥ 6dB；处理过程不引入明显失真 |
| **多音轨输出** | 导出时支持分离音轨（如原声轨、BGM 轨独立） | 专业格式可选；默认输出仍保持 AAC 混音 |
| **关键帧音量包络** | 在音频 Clip 上按关键帧绘制音量曲线，实现局部起伏 | 关键帧插值平滑；预览时实时响应关键帧变化 |

---

## 3. 数据模型与接口

### 3.1 核心领域模型

```kotlin
/**
 * 音频片段，位于 Timeline 的 AUDIO 类型轨道上。
 */
data class AudioClip(
    override val id: String,
    override val trackId: String,
    override val startTimeUs: Long,      // 在时间线上的起始位置（微秒）
    override val endTimeUs: Long,        // 在时间线上的结束位置（微秒）
    override val sourceStartUs: Long,    // 源音频素材的起始位置
    override val sourceEndUs: Long,      // 源音频素材的结束位置
    val uri: Uri,                        // 源文件 URI
    val volume: Float = 1.0f,            // 音量增益，0.0 ~ 2.0
    val fadeInMs: Int = 0,               // 淡入时长（毫秒）
    val fadeOutMs: Int = 0,              // 淡出时长（毫秒）
    val fadeCurve: FadeCurve = FadeCurve.LINEAR,
    val isMuted: Boolean = false,
    val speed: Float = 1.0f              // 播放速度，预留变速接口
) : Clip(id, trackId, startTimeUs, endTimeUs, sourceStartUs, sourceEndUs)

enum class FadeCurve { LINEAR, EXPONENTIAL, S_CURVE }

/**
 * 录音配置与结果。
 */
data class AudioRecordConfig(
    val sampleRate: Int = 44100,
    val channelCount: Int = 1,           // 旁白通常单声道
    val encoding: Int = AudioFormat.ENCODING_PCM_16BIT,
    val outputUri: Uri
)

data class AudioRecordClip(
    val config: AudioRecordConfig,
    val durationUs: Long,
    val peakAmplitude: Float
)

/**
 * 混音输出参数。
 */
data class AudioMixConfig(
    val sampleRate: Int = 44100,
    val channelCount: Int = 2,
    val bitDepth: Int = 16,
    val outputFormat: AudioOutputFormat = AudioOutputFormat.AAC
)

enum class AudioOutputFormat { AAC, WAV, PCM }
```

### 3.2 引擎层接口

```kotlin
/**
 * 音频处理器接口，由 Engine Layer 实现。
 */
interface AudioProcessor {

    /**
     * 解码指定 URI 的音频，返回 PCM 数据或文件路径。
     */
    suspend fun decodeToPcm(
        uri: Uri,
        targetSampleRate: Int = 44100,
        targetChannelCount: Int = 2
    ): Result<PcmAudioBuffer>

    /**
     * 从视频文件中提取音轨并解码为 PCM。
     */
    suspend fun extractAudioFromVideo(
        videoUri: Uri,
        outputUri: Uri
    ): Result<AudioClip>

    /**
     * 将多条音频轨按时间线混音为单一 PCM 流。
     */
    suspend fun mixTracks(
        tracks: List<AudioTrackSnapshot>,
        rangeUs: LongRange,
        config: AudioMixConfig
    ): Result<PcmAudioBuffer>

    /**
     * 对 PCM 数据应用音量、淡入淡出、变速变调。
     */
    suspend fun applyEffects(
        input: PcmAudioBuffer,
        effects: List<AudioEffect>
    ): Result<PcmAudioBuffer>

    /**
     * 将 PCM 编码为 AAC 并写入 Muxer。
     */
    suspend fun encodeToAac(
        pcmBuffer: PcmAudioBuffer,
        muxer: MediaMuxer,
        format: MediaFormat
    ): Result<Unit>
}

/**
 * 某一时刻的音频轨道快照，供混音器使用。
 */
data class AudioTrackSnapshot(
    val trackId: String,
    val clips: List<AudioClip>,
    val isMuted: Boolean = false,
    val solo: Boolean = false
)

/**
 * 音频效果基类。
 */
sealed class AudioEffect(
    open val startTimeUs: Long,
    open val endTimeUs: Long
)

data class VolumeEnvelopeEffect(
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    val gain: Float
) : AudioEffect(startTimeUs, endTimeUs)

data class FadeEffect(
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    val fadeInDurationUs: Long,
    val fadeOutDurationUs: Long,
    val curve: FadeCurve
) : AudioEffect(startTimeUs, endTimeUs)
```

### 3.3 Repository 与 UseCase 接口

```kotlin
/**
 * 音频资源仓库，负责音频元数据持久化与缓存。
 */
interface AudioRepository {
    suspend fun saveAudioClip(clip: AudioClip): Result<Unit>
    suspend fun deleteAudioClip(clipId: String): Result<Unit>
    suspend fun getWaveformData(clipId: String): Result<FloatArray>
    suspend fun cachePcm(uri: Uri, pcm: PcmAudioBuffer): Result<Uri>
}

/**
 * 音频编辑 UseCase，供 ViewModel 调用。
 */
interface AudioEditUseCase {
    suspend fun addBgm(uri: Uri, insertAtUs: Long): Result<AudioClip>
    suspend fun extractAudioFromVideo(videoClipId: String): Result<AudioClip>
    suspend fun adjustVolume(clipId: String, volume: Float): Result<Unit>
    suspend fun setFade(clipId: String, fadeInMs: Int, fadeOutMs: Int): Result<Unit>
    suspend fun splitAudioClip(clipId: String, atUs: Long): Result<Pair<AudioClip, AudioClip>>
    suspend fun startRecording(config: AudioRecordConfig, startAtUs: Long): Result<AudioRecordClip>
    suspend fun stopRecording(): Result<AudioClip>
}
```

### 3.4 状态管理

- **AudioEditState**：当前音频面板的状态，包含选中的音频轨、选中的 Clip、录音状态、波形生成进度。
- **AudioPlaybackState**：预览播放器中的音频状态，包括当前播放位置、激活的音频 Clip 列表、是否正在录音。
- 所有状态通过 `StateFlow` 向 UI 层暴露，状态变更由 `UseCase` 驱动。

---

## 4. 交互与流程

### 4.1 添加 BGM 操作流程

1. 用户在素材选择页选择本地音频文件。
2. `MediaScanner` 解析音频时长、采样率、声道数，生成缩略波形数据。
3. `AudioEditUseCase.addBgm()` 创建 `AudioClip`，默认放置于主音频轨当前播放头位置。
4. 更新 `Timeline` 领域模型，触发 `TimelineRepository` 自动保存。
5. `Player` 刷新渲染图，音频处理器重新计算当前时间段的混音结果。
6. UI 时间轴展示波形与 Clip 边界。

### 4.2 音频提取流程

```
用户选中视频片段 → 点击「提取音频」
    ↓
AudioEditUseCase.extractAudioFromVideo(videoClipId)
    ↓
AudioProcessor.extractAudioFromVideo(uri, outputUri)
    ↓
MediaExtractor 分离音轨 → MediaCodec 解码 → PCM 缓存文件
    ↓
生成 AudioClip 并插入 AUDIO 轨道
    ↓
通知 TimelineRepository 保存，Player 刷新
```

### 4.3 实时预览音频数据流

```
Player 当前 positionUs
    ↓
AudioMixer 查询当前激活的 AudioClip 集合
    ↓
对每个 Clip 计算：源读取位置 = sourceStartUs + (positionUs - startTimeUs) * speed
    ↓
应用音量增益、淡入淡出曲线、轨道静音/独奏状态
    ↓
多 Clip PCM 样本按帧叠加（防止 clipping）
    ↓
AudioTrack 播放混合后的 PCM
```

### 4.4 导出时音频渲染流程

```
Exporter 按时间线逐帧推进
    ↓
AudioProcessor.mixTracks() 生成当前区间完整 PCM
    ↓
应用变速/变调（若 Clip 配置 speed ≠ 1.0）
    ↓
MediaCodec AAC Encoder 编码
    ↓
MediaMuxer 写入音频轨
    ↓
与视频轨同步封装为 MP4
```

---

## 5. 实现要点

### 5.1 技术方案选择

| 能力 | 方案 | 说明 |
|------|------|------|
| 音频解码 | `MediaExtractor` + `MediaCodec` | 统一解码为 44.1kHz/16bit PCM，便于后续处理 |
| 录音采集 | `AudioRecord` + 独立 `HandlerThread` | 避免录音线程阻塞 UI；录制文件先写 WAV，必要时转 AAC |
| 混音 | 复用现有 JNI 混音 + Kotlin 层调度 | 现有 JNI 混音能力接入 `AudioProcessor`；新增防止 clipping 的限幅逻辑 |
| 变速变调 | Sonic 库 / 自研 JNI | 预留接口，P1 阶段实现；MVP 不强制支持 |
| 淡入淡出 | Kotlin/NDK 浮点乘法 | 按曲线生成增益系数，与 PCM 样本相乘 |
| 编码输出 | `MediaCodec` AAC + `MediaMuxer` | 与视频导出共用 Muxer，确保音画同步 |

### 5.2 需要特别注意的难点

1. **音画同步（AV Sync）**：导出时必须保证音频 PTS 与视频帧严格对齐。音频采样数与时间换算公式为 `timeUs = sampleCount * 1_000_000 / sampleRate`，Muxer 写入时需使用该值生成 PTS。
2. **多轨道混音爆音**：多段音频叠加时可能出现样本溢出。混音器必须先将样本转换为浮点数求和，再做限幅（hard limit 或 soft limit），最后转回 16bit PCM。
3. **淡入淡出边界处理**：淡入/淡出区间不能超出 Clip 实际有效时长；曲线切换时需在零交叉点附近处理以避免咔哒声。
4. **录音与预览并发**：录音时播放器必须继续播放视频画面与参考音，需使用独立的 `AudioRecord` 与 `AudioTrack` 实例，避免共享音频焦点导致冲突。
5. **波形图生成性能**：长音频 waveform 应在后台线程分块生成并缓存，避免一次性加载大文件导致 OOM。缓存文件存放于应用私有缓存目录。
6. **Scoped Storage 适配**：Android 10+ 读取本地音频优先使用 `MediaStore` 或 SAF；写入缓存/导出文件使用应用私有目录或 `MediaStore`。

### 5.3 与现有代码的衔接建议

- 将现有 JNI 混音代码封装为 `NativeAudioMixer`，实现 `AudioProcessor.mixTracks()` 的底层调用。
- 现有 `MediaCodec` 编码逻辑从导出模块迁移到 `AudioEncoder` 类，供 `AudioProcessor.encodeToAac()` 调用。
- 旧版音频提取工具类（如有）保留作为兼容层，逐步替换为 `AudioProcessor.extractAudioFromVideo()`。
- 新增 `AudioEditViewModel` 与 `AudioEditPanel` Compose 组件，遵循 `uiux` 模块的设计系统规范。

---

## 6. 性能与质量要求

### 6.1 性能指标

| 场景 | 目标 | 测试方法 |
|------|------|----------|
| 音频解码延迟 | 1 分钟音频解码 ≤ 2s | 使用 44.1kHz stereo AAC 测试 |
| 波形图生成 | 1 分钟音频 ≤ 3s | 后台线程生成并缓存 |
| 混音预览 CPU 占用 | 1080p 双音轨预览时音频 CPU ≤ 15% | Profiler 采样 |
| 导出混音速度 | 音频混音不成为导出瓶颈，速度 ≥ 2x 实时 | 对比仅视频导出与完整导出耗时 |
| 内存占用 | 导出时音频缓冲区 ≤ 50MB | 控制单次读取 PCM 长度 ≤ 2s |
| 录音延迟 | 录音开始到写入文件 ≤ 200ms | 录音按钮点击到波形出现 |

### 6.2 常见异常与处理

| 异常场景 | 处理策略 |
|----------|----------|
| 音频文件格式不支持 | 抛出 `DecodeException`，UI 提示「该音频格式暂不支持」 |
| 音频轨道缺失/损坏 | 提取时返回失败，视频片段仍可正常编辑但无原声 |
| 混音样本溢出 | 在混音器内做 soft limit，输出峰值 ≤ 0dBFS |
| 录音权限被拒绝 | 停止录音并弹出权限引导弹窗 |
| 音频焦点被占用 | 预览时降低音量或暂停播放，录音时申请永久音频焦点 |
| 导出时编码器失败 | 回退到安全配置（AAC-LC, 44.1kHz, 128kbps）重试一次，仍失败则终止导出并上报 |
| 缓存文件损坏 | 检测到 CRC 或长度异常时删除缓存并重新生成 |

---

## 7. 依赖模块

本模块依赖以下 `spec/modules` 下的模块：

- **timeline**：依赖 `Track`、`Clip` 抽象、时间计算与撤销重做命令栈。所有音频 Clip 均放置在 `AUDIO` 类型轨道上，编辑操作需封装为 `Command`。
- **media**：依赖素材导入、MediaStore 扫描、缩略图与格式支持能力，用于获取本地音频文件元数据与生成波形缩略图。
- **project**：依赖工程模型与自动保存机制，音频 Clip 的状态变更需要持久化到工程文件中。
- **player**：依赖实时预览播放器，音频混合后的 PCM 需按播放头位置同步输出到 `AudioTrack`。
- **export**：依赖渲染管线与编码参数，混音后的 AAC 数据需与视频轨一起封装为 MP4。
- **uiux**：依赖设计系统与组件规范，音频面板、波形显示、音量滑块、录音按钮等 UI 需遵循统一交互标准。

---

## 8. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | 2025-06-28 | 初始 audio 模块规范，覆盖 MVP P0 能力与 P1/P2 规划 |

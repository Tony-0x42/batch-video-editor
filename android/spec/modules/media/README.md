# Media 模块规范

> 本文件定义 VideoEditor-For-Android 中 **Media（媒体素材管理）** 模块的职责、功能、数据模型、交互流程、实现要点与性能质量要求。
> 本模块为整个剪辑链路提供「素材发现 → 元数据解析 → 缩略图生成 → 可用性校验 → 缓存管理」能力，是时间线（timeline）、编辑器（editor）、导出（export）等模块的底层素材依赖。

---

## 1. 模块概述

Media 模块负责应用内所有外部媒体素材的**发现、导入、解析、索引与缓存**。它向上层提供统一的媒体资源抽象 `MediaAsset`，屏蔽本地文件、MediaStore、SAF（Storage Access Framework）等不同来源的差异；同时承担视频缩略图、音频波形预览图、图片降采样等计算密集型任务的异步调度与磁盘缓存。

在整体架构中，Media 模块位于 **Data Layer**，通过 `MediaRepository` 接口向 Domain / UseCase 层暴露能力。它与 Recorder 模块（录制文件落盘）、Project 模块（工程私有素材）、Draft 模块（草稿恢复）紧密协作，但本身不处理剪辑命令、渲染合成与导出编码。

本模块需要解决的核心问题包括：
- Android 10+ Scoped Storage 与分区存储限制下的安全文件访问；
- 多来源素材（相册、应用私有目录、录制缓存、第三方分享）的统一抽象；
- 大尺寸视频/图片的异步解析与缩略图缓存，避免主线程阻塞与 OOM；
- 素材格式、编码、分辨率、时长等元数据的完整采集，为时间线编排与导出参数提供决策依据。

---

## 2. 功能清单

### P0 — 首个 MVP 必须实现

| 名称 | 描述 | 验收标准 |
|------|------|----------|
| **本地视频导入** | 从系统相册/媒体库多选导入视频，支持 MP4/MOV 容器、H.264/H.265 编码。 | 1. 支持一次性多选 ≥ 10 段视频；2. 导入后返回完整 `MediaAsset` 列表；3. 对不支持的编码格式给出明确提示。 |
| **本地图片导入** | 从系统相册导入静态图片作为视频片段或画布背景。 | 1. 支持 JPG/PNG/WEBP；2. 读取 EXIF 旋转信息并校正；3. 支持单图持续时长设置（默认 3s）。 |
| **本地音频导入** | 从系统媒体库导入 BGM 或音效，支持 MP3/AAC/WAV。 | 1. 支持多选音频；2. 返回时长、采样率、声道数；3. 失败时返回可读错误码。 |
| **MediaStore 扫描** | 通过 `MediaStore` API 扫描设备公开媒体库，按时间倒序列出视频/图片/音频。 | 1. 列表加载 ≤ 1s（1000 条以内）；2. 支持按媒体类型过滤；3. Android 10+ 不依赖 `READ_EXTERNAL_STORAGE` 全权限。 |
| **素材元数据解析** | 解析素材的时长、分辨率、帧率、旋转角、比特率、采样率、声道数等关键字段。 | 1. 视频解析成功率 ≥ 98%；2. 解析过程在后台线程执行；3. 异常素材标记为 `UNSUPPORTED` 或 `CORRUPTED`。 |
| **视频缩略图生成** | 为每段视频生成均匀分布的关键帧缩略图，用于时间线轨道与素材选择页预览。 | 1. 缩略图尺寸默认 160×90（可配置）；2. 1 分钟 1080p 视频缩略图生成 ≤ 500ms；3. 缩略图磁盘缓存，二次进入秒开。 |
| **素材可用性校验** | 在加入时间线前检查素材是否存在、是否被删除、权限是否仍然有效。 | 1. 提供同步/异步两种校验接口；2. 缺失素材返回 `NOT_FOUND`；3. 授权失效素材返回 `PERMISSION_DENIED`。 |

### P1 — 重要增值

| 名称 | 描述 | 验收标准 |
|------|------|----------|
| **音频波形图生成** | 为音频素材生成缩略波形图，用于音频轨可视化编辑。 | 1. 波形点密度可配置（默认每秒 10 点）；2. 支持本地缓存；3. 生成过程不阻塞播放。 |
| **图片智能适配** | 根据目标画布比例对导入图片进行裁剪/填充建议，并生成预览。 | 1. 提供 FIT/COVER/CENTER 三种适配模式；2. 预览渲染帧率 ≥ 24fps；3. 用户可手动调整显示区域。 |
| **最近使用素材** | 维护一个应用级「最近使用」素材列表，加速重复选取。 | 1. 最多保留 50 条；2. 去重并按最近使用时间排序；3. 持久化到 DataStore。 |
| **素材去重与合并** | 同一 URI 多次导入时返回同一 `MediaAsset` 实例或相同 stable id，避免冗余缓存。 | 1. 基于 URI + 修改时间生成 stable id；2. 缓存命中时不再重复解析元数据。 |
| **批量导入进度** | 多素材导入时提供实时进度与可取消能力。 | 1. 通过 Kotlin Flow 输出 `MediaImportState`；2. 支持中途取消；3. 取消后已生成缓存可被清理。 |

### P2 — 长期规划

| 名称 | 描述 | 验收标准 |
|------|------|----------|
| **云端/网络素材导入** | 预留从网络 URL 下载素材并导入的扩展点。 | 1. 接口层支持 `SourceType.REMOTE`；2. 下载任务可暂停/恢复；3. 不阻塞 MVP 实现。 |
| **素材标签与搜索** | 为素材打标签、按时间/地点/文件名搜索。 | 1. 支持本地索引；2. 搜索结果 ≤ 300ms；3. 仅作为扩展接口预留。 |
| **RAW/DNG 图片支持** | 导入专业摄影 RAW 格式，导出时统一转码。 | 1. 提供解码接口抽象；2. MVP 阶段返回降级提示。 |
| **HDR 视频元数据识别** | 识别 HDR10/HLG 视频并提示转码或保留。 | 1. 读取 PQ/HLG color transfer；2. 导出模块可据此决策 tone mapping。 |

---

## 3. 数据模型与接口

### 3.1 核心领域模型

```kotlin
/**
 * 媒体素材统一抽象。
 * 屏蔽本地文件、MediaStore、SAF、远程 URL 等来源差异。
 */
data class MediaAsset(
    val id: String,                    // stable id，由 SourceType + URI + 修改时间哈希生成
    val sourceType: MediaSourceType,
    val uri: Uri,                      // 实际可访问 URI
    val displayUri: Uri,               // 用于 UI 展示的缩略图/预览 URI
    val mediaType: MediaType,
    val displayName: String,
    val addedTime: Long,
    val modifiedTime: Long,
    val metadata: MediaMetadata,
    val status: MediaStatus = MediaStatus.READY,
    val extra: Map<String, String> = emptyMap()
)

enum class MediaSourceType { MEDIA_STORE, SAF, APP_PRIVATE, RECORDING_CACHE, REMOTE }
enum class MediaType { VIDEO, IMAGE, AUDIO }
enum class MediaStatus { READY, NOT_FOUND, PERMISSION_DENIED, UNSUPPORTED, CORRUPTED, PENDING }

/**
 * 媒体元数据，根据 mediaType 部分字段可能为空。
 */
data class MediaMetadata(
    val durationUs: Long = 0L,         // 视频/音频时长，图片为 0
    val width: Int = 0,                // 视频/图片宽度
    val height: Int = 0,               // 视频/图片高度
    val rotation: Int = 0,             // 0/90/180/270
    val frameRate: Float = 0f,         // 视频帧率
    val videoBitrate: Int = 0,         // 视频码率 bps
    val audioBitrate: Int = 0,         // 音频码率 bps
    val sampleRate: Int = 0,           // 音频采样率
    val channelCount: Int = 0,         // 音频声道数
    val mimeType: String,              // 如 video/mp4
    val codecName: String = ""         // 如 avc/hevc，用于判断硬解支持
)

/**
 * 缩略图/预览资源描述。
 */
data class MediaThumbnail(
    val assetId: String,
    val thumbnailUri: Uri,             // 本地缓存文件 URI
    val width: Int,
    val height: Int,
    val timeUs: Long = 0L              // 视频帧对应时间，图片为 0
)

/**
 * 音频波形采样数据。
 */
data class AudioWaveform(
    val assetId: String,
    val sampleCount: Int,
    val amplitudes: List<Float>,       // 0.0 ~ 1.0
    val durationUs: Long
)
```

### 3.2 Repository 接口

```kotlin
/**
 * Media 模块对外暴露的核心仓库接口。
 * UI / UseCase 层仅依赖此接口。
 */
interface MediaRepository {

    /**
     * 扫描系统媒体库，按类型过滤。
     * @param type 过滤类型，null 表示全部
     * @return Flow 形式的实时列表，MediaStore 变更时自动刷新
     */
    fun observeMediaStore(type: MediaType? = null): Flow<List<MediaAsset>>

    /**
     * 同步获取指定 URI 的 MediaAsset，适用于单次导入。
     */
    suspend fun resolve(uri: Uri, type: MediaType): Result<MediaAsset>

    /**
     * 批量解析并导入多个 URI，返回带进度的状态流。
     */
    fun importBatch(uris: List<Uri>, type: MediaType): Flow<MediaImportState>

    /**
     * 校验素材当前是否可用。
     */
    suspend fun verify(asset: MediaAsset): MediaStatus

    /**
     * 获取视频缩略图。若缓存不存在则异步生成。
     * @param timeUs 视频帧时间戳，-1 表示取首帧
     */
    suspend fun getVideoThumbnail(asset: MediaAsset, timeUs: Long = -1L): Result<MediaThumbnail>

    /**
     * 获取视频缩略图序列，用于时间线轨道。
     */
    suspend fun getVideoThumbnailSequence(
        asset: MediaAsset,
        count: Int,
        startUs: Long = 0L,
        endUs: Long = asset.metadata.durationUs
    ): Result<List<MediaThumbnail>>

    /**
     * 获取图片降采样预览。
     */
    suspend fun getImagePreview(asset: MediaAsset, maxSide: Int): Result<MediaThumbnail>

    /**
     * 获取音频波形数据。
     */
    suspend fun getAudioWaveform(asset: MediaAsset, samplesPerSecond: Int = 10): Result<AudioWaveform>

    /**
     * 清除指定素材的所有本地缓存。
     */
    suspend fun clearCache(asset: MediaAsset)
}

sealed class MediaImportState {
    data class Progress(val current: Int, val total: Int, val asset: MediaAsset? = null) : MediaImportState()
    data class Success(val assets: List<MediaAsset>) : MediaImportState()
    data class Error(val failed: List<Pair<Uri, MediaImportError>>) : MediaImportState()
}

enum class MediaImportError {
    UNSUPPORTED_FORMAT,
    FILE_NOT_FOUND,
    PERMISSION_DENIED,
    METADATA_PARSE_FAILED,
    THUMBNAIL_GENERATION_FAILED,
    UNKNOWN
}
```

### 3.3 内部接口与缓存

```kotlin
/**
 * 元数据解析器，封装 MediaMetadataRetriever / MediaExtractor 等实现。
 */
interface MetadataExtractor {
    suspend fun extract(uri: Uri, type: MediaType): Result<MediaMetadata>
}

/**
 * 缩略图生成器，封装 MediaMetadataRetriever.getFrameAtTime / Glide 等实现。
 */
interface ThumbnailGenerator {
    suspend fun generateVideoFrame(asset: MediaAsset, timeUs: Long): Result<MediaThumbnail>
    suspend fun generateImagePreview(asset: MediaAsset, maxSide: Int): Result<MediaThumbnail>
}

/**
 * 磁盘缓存管理，按 assetId 与参数组织缓存文件。
 */
interface MediaCacheManager {
    fun getCachedThumbnail(assetId: String, timeUs: Long): File?
    fun putCachedThumbnail(assetId: String, timeUs: Long, file: File)
    fun getCacheSize(): Long
    fun evictIfNeeded(maxSize: Long)
    fun clear()
}
```

### 3.4 与其他模块的依赖接口

Media 模块**不直接依赖**上层模块，但会向上层暴露以下边界能力：

| 消费方 | 使用接口 | 说明 |
|--------|----------|------|
| timeline / editor | `MediaRepository.resolve()` / `importBatch()` | 将素材转换为 `Clip` |
| player | `getVideoThumbnailSequence()` | 时间线轨道缩略图 |
| audio | `getAudioWaveform()` | 音频轨道波形可视化 |
| export | `MediaAsset.metadata` | 判断是否需要转码、目标分辨率适配 |
| draft | `MediaRepository.verify()` | 草稿恢复时校验素材是否存在 |
| recorder | 通过文件 URI 注册为 `MediaSourceType.RECORDING_CACHE` | 录制片段自动进入媒体索引 |

---

## 4. 交互与流程

### 4.1 用户操作流程

#### 4.1.1 从相册导入视频

1. 用户在素材选择页点击「导入视频」；
2. 系统相册/SAF 返回一个或多个 `Uri`；
3. UI 调用 `MediaRepository.importBatch(uris, VIDEO)`；
4. Media 模块后台解析元数据、生成首帧缩略图；
5. 解析完成的 `MediaAsset` 通过 Flow 返回给 UI；
6. UI 将 `MediaAsset` 提交给 Editor / Timeline 模块，创建 `VideoClip`。

#### 4.1.2 时间线缩略图加载

1. 时间线渲染轨道时，根据 `Clip.sourceStartUs / sourceEndUs` 决定需要展示的缩略图范围；
2. 调用 `getVideoThumbnailSequence()` 获取缩略图序列；
3. 优先读取磁盘缓存，未命中则异步解码视频帧并缓存；
4. 缩略图按轨道时序排列显示，支持动态刷新。

### 4.2 模块内部数据流

```
外部 URI（相册/SAF/录制缓存）
        ↓
[MediaRepository.importBatch]
        ↓
[MetadataExtractor] 解析时长/分辨率/旋转/帧率/编码
        ↓
[CapabilityChecker] 检查格式与编码支持
        ↓
[ThumbnailGenerator] 生成首帧/序列缩略图
        ↓
[MediaCacheManager] 写入磁盘缓存
        ↓
返回 MediaAsset 列表 ←→ 上层 Timeline/Editor 创建 Clip
        ↓
[MediaStore Observer] 监听系统媒体库变更并刷新索引
```

### 4.3 缩略图生成时序（伪时序）

```
TimelineViewModel          MediaRepository          ThumbnailGenerator          CacheManager
      |                           |                         |                         |
      |--- requestThumbnails --->|                         |                         |
      |                         |--- generateSequence --->|                         |
      |                         |                         |--- cache hit? ---→       |
      |                         |                         |←-- return cached --------|
      |                         |                         |--- miss: decode frame ---|
      |                         |                         |--- save to cache ------->|
      |                         |←-- List<Thumbnail> -----|                         |
      |←-- update UI ------------|                         |                         |
```

---

## 5. 实现要点

### 5.1 技术方案选择

| 能力 | 选型 | 说明 |
|------|------|------|
| 系统媒体库扫描 | `MediaStore` + `ContentObserver` | Android 官方推荐，适配 Scoped Storage；10+ 使用 `READ_MEDIA_*` 细分权限。 |
| 元数据解析 | `MediaMetadataRetriever` + `MediaExtractor` | 覆盖时长、分辨率、旋转、帧率等；对特殊文件可fallback到 `MediaExtractor`。 |
| 缩略图生成 | `MediaMetadataRetriever.getFrameAtTime()` / `Glide` | 视频帧优先使用系统 API；图片预览复用 Glide 的降采样与缓存能力。 |
| 音频波形 | 自研 JNI 或 `MediaCodec` 解码后降采样 | 现有 JNI 混音模块可复用部分音频解码能力；MVP 阶段可用简化算法。 |
| 磁盘缓存 | `LruCache` + 自定义文件目录 | 缓存目录位于 `context.cacheDir/media_thumbnails`，按 `assetId` 子目录组织。 |
| 异步调度 | Kotlin Coroutines + `Dispatchers.IO` | 解析、解码、文件 IO 全部在 IO 线程执行；缩略图加载使用 `Flow` 回传。 |

### 5.2 需要特别注意的难点

1. **旋转角处理**
   - 视频和图片均可能携带旋转元数据（如手机竖屏拍摄的视频 `rotation=90`）。
   - Media 模块返回的 `MediaMetadata` 必须同时包含**容器旋转角**与**校正后的显示宽高**，避免上层渲染时出现方向错误。

2. **Scoped Storage 适配**
   - Android 10+ 禁止直接访问外部存储路径，必须通过 `Uri` 和 `ContentResolver` 操作。
   - 对 `READ_MEDIA_VIDEO/IMAGES/AUDIO` 权限做动态申请与降级：无权限时仍可浏览应用私有目录与录制缓存。

3. **大文件与内存管理**
   - 生成缩略图时禁止一次性加载原始分辨率到内存。
   - 视频缩略图统一降采样到目标尺寸；图片预览使用 `inSampleSize` 或 Glide 等效方案。

4. **格式兼容性**
   - 部分设备拍摄的视频为 H.265、AV1 或特殊封装，需通过 `codecName` 标记并在上层提示转码或不支持。
   - 音频采样率、声道数不一致时，由 audio 模块处理重采样，media 模块只负责上报原始元数据。

5. **缓存清理策略**
   - 缩略图缓存需设置上限（默认 200MB），超过时按 LRU 清理。
   - 卸载草稿或工程删除时，应调用 `clearCache()` 避免磁盘膨胀。

### 5.3 与现有代码的衔接建议

- 旧版 Demo 中若存在直接读取文件路径的代码，应逐步改为通过 `MediaRepository` 获取 `MediaAsset`。
- 现有 `MediaMetadataRetriever` 工具类可抽取为 `DefaultMetadataExtractor` 的实现。
- 录制模块落盘后的文件建议通过 `MediaRepository.registerRecording(uri)` 注册到媒体索引，避免重复扫描。
- 工程私有目录中的临时素材统一使用 `MediaSourceType.APP_PRIVATE`，不混入系统 MediaStore 列表。

---

## 6. 性能与质量要求

### 6.1 性能指标

| 场景 | 目标 | 测试方法 |
|------|------|----------|
| MediaStore 列表加载 | 1000 条素材 ≤ 1s | 真机相册 1000 条记录，测量从查询到 UI 渲染完成 |
| 单素材元数据解析 | 1080p 视频 ≤ 200ms | 连续解析 20 段素材取平均 |
| 视频首帧缩略图 | 1080p 视频 ≤ 300ms | 冷缓存首次生成 |
| 缩略图序列生成 | 1 分钟视频 8 张缩略图 ≤ 500ms | 冷缓存，中档设备 |
| 批量导入 10 段视频 | 总耗时 ≤ 3s | 每段 6s 1080p，含元数据解析与首帧缩略图 |
| 内存峰值 | 批量导入 20 段素材 ≤ 150MB | Android Studio Profiler 监测 |
| 磁盘缓存上限 | 默认 200MB，可配置 | 超过上限后 LRU 自动清理 |

### 6.2 常见异常与处理

| 异常场景 | 处理方式 | 用户感知 |
|----------|----------|----------|
| 文件被删除或移动 | `verify()` 返回 `NOT_FOUND` | 时间线中显示「素材缺失」占位图，提示重新导入 |
| URI 权限失效（SAF） | `verify()` 返回 `PERMISSION_DENIED` | 提示用户重新授权或重新选择 |
| 不支持的编码格式 | 解析时标记 `UNSUPPORTED` | 导入页显示「暂不支持该格式」并跳过 |
| 文件损坏或无法解码 | 解析时标记 `CORRUPTED` | 显示「文件损坏」提示 |
| 缩略图生成失败 | 返回占位缩略图，记录日志 | UI 显示默认占位图，不影响导入流程 |
| 缓存目录不可用 | 降级到内存缓存，记录警告 | 功能可用，重启后需重新生成 |
| 存储空间不足 | 清理旧缓存，必要时终止生成 | Toast 提示「存储空间不足」 |

---

## 7. 依赖模块

Media 模块在 `spec/modules` 下的依赖关系如下：

| 依赖模块 | 依赖方式 | 说明 |
|----------|----------|------|
| project | 间接依赖 | 工程的私有素材目录由 project 模块定义，Media 模块通过 `APP_PRIVATE` 源类型访问。 |
| draft | 间接依赖 | 草稿恢复时调用 `MediaRepository.verify()` 校验素材可用性。 |
| recorder | 被依赖 | Recorder 录制的缓存文件注册为 `RECORDING_CACHE` 来源，供 Media 模块索引。 |

Media 模块**不直接依赖** timeline、editor、audio、filters、effects、export、player、uiux 等上层模块；这些模块通过 `MediaRepository` 接口消费 Media 能力。

---

## 8. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | 2025-06-28 | 初始版本，定义 Media 模块职责、功能、模型、流程与性能指标 |

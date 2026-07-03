# 技术规范

> 本文档定义 VideoEditor-For-Android 商用演进版本的技术栈、架构分层、核心数据流与性能指标。

---

## 1. 技术栈

### 1.1 基础平台

| 层级 | 选型 | 说明 |
|------|------|------|
| 语言 | Kotlin + Java | 新模块优先使用 Kotlin；与现有 Java 模块通过接口交互 |
| 最低 SDK | API 24 (Android 7.0) | 确保 MediaCodec 异步模式、Camera2 可用 |
| 目标 SDK | API 35 | 适配最新 Android 版本与商店政策 |
| 构建工具 | Gradle 8.9 + AGP 8.7.0 | 已升级，保持同步更新 |
| JDK | 17 | 必须，AGP 8.7 要求 |

### 1.2 视频图像

| 能力 | 技术选型 | 说明 |
|------|----------|------|
| 相机采集 | Camera2 / CameraX | 逐步替换旧 `android.hardware.Camera` |
| GPU 渲染 | OpenGL ES 3.0 / 2.0 | 滤镜链、水印、美颜、画中画均基于 GPU |
| 视频编解码 | MediaCodec 硬编码 | H.264/H.265 编码；优先设备硬解 |
| 封装格式 | MediaMuxer | 输出 MP4（MPEG-4 + AAC） |
| 图像处理 | OpenGL Shader | 所有滤镜、调色、转场通过 Fragment Shader 实现 |

### 1.3 音频

| 能力 | 技术选型 | 说明 |
|------|----------|------|
| 音频解码 | MediaCodec / MediaExtractor | 提取与解码音轨 |
| 音频录制 | AudioRecord | 旁白/录音 |
| 音频处理 | Sonic / 自研 JNI | 变速、变调、混音；现有 JNI 混音可复用 |
| 音频编码 | MediaCodec AAC | 输出 AAC |

### 1.4 数据与存储

| 能力 | 技术选型 | 说明 |
|------|----------|------|
| 工程数据 | Room + JSON | 时间线、片段信息、Effect 参数持久化 |
| 媒体索引 | MediaStore + 本地缓存 | 扫描相册与项目私有目录 |
| 缩略图 | Glide + 本地缓存 | 视频帧缩略图、滤镜预览 |
| 配置 | DataStore | 用户设置、主题、导出默认参数 |

### 1.5 UI

| 能力 | 技术选型 | 说明 |
|------|----------|------|
| UI 框架 | Jetpack Compose + View 混合 | 新页面/复杂交互优先 Compose；遗留页面保留 View |
| 架构模式 | MVVM + Repository | ViewModel + Flow/LiveData |
| 依赖注入 | Hilt | 统一依赖管理 |
| 异步 | Kotlin Coroutines + Flow | 替代 RxJava/Handler 线程切换 |
| 导航 | Jetpack Navigation | 页面路由统一 |

---

## 2. 架构分层

```
┌─────────────────────────────────────────────┐
│  UI Layer (Compose / View + ViewModel)        │
│  - 页面、组件、手势、主题、动画               │
├─────────────────────────────────────────────┤
│  Presentation / UseCase Layer                 │
│  - 剪辑命令、撤销重做、导出流程编排           │
├─────────────────────────────────────────────┤
│  Domain Layer                                   │
│  - Timeline、Clip、Track、Effect 领域模型     │
│  - 接口定义（Renderer、Decoder、Encoder）     │
├─────────────────────────────────────────────┤
│  Data Layer                                     │
│  - 工程持久化、媒体索引、素材缓存             │
├─────────────────────────────────────────────┤
│  Engine Layer                                   │
│  - Player（OpenGL 预览）                      │
│  - Renderer（滤镜/特效/合成）                 │
│  - Recorder（相机录制）                       │
│  - Exporter（硬编码导出）                     │
│  - AudioProcessor（混音/变速）                │
└─────────────────────────────────────────────┘
```

### 2.1 层间依赖规则

- **UI Layer** 只能依赖 ViewModel / UseCase
- **UseCase** 只能依赖 Domain Repository 接口
- **Domain** 不依赖 Android Framework，可单元测试
- **Data Layer** 实现 Domain Repository 接口
- **Engine Layer** 被 Domain / Data / UseCase 通过接口调用

---

## 3. 核心数据模型

### 3.1 Project（工程）

```kotlin
data class VideoProject(
    val id: String,
    val name: String,
    val createTime: Long,
    val updateTime: Long,
    val canvas: CanvasConfig,
    val timeline: Timeline,
    val exportSettings: ExportSettings
)
```

### 3.2 Timeline（时间线）

```kotlin
data class Timeline(
    val durationUs: Long,
    val tracks: List<Track>
)

sealed class Track(
    open val id: String,
    open val type: TrackType,
    open val clips: List<Clip>
)

enum class TrackType { VIDEO, AUDIO, TEXT, STICKER, EFFECT }
```

### 3.3 Clip（片段）

```kotlin
sealed class Clip(
    open val id: String,
    open val trackId: String,
    open val startTimeUs: Long,      // 在时间线上的起始位置
    open val endTimeUs: Long,        // 在时间线上的结束位置
    open val sourceStartUs: Long,    // 源素材起始位置
    open val sourceEndUs: Long       // 源素材结束位置
)

data class VideoClip(
    override val id: String,
    val uri: Uri,
    val speed: Float = 1.0f,
    val filters: List<FilterEffect> = emptyList(),
    val transformations: VideoTransform = VideoTransform(),
    // ...
) : Clip(...)
```

### 3.4 Effect（效果）

```kotlin
sealed class Effect(
    open val id: String,
    open val startTimeUs: Long,
    open val endTimeUs: Long
)

data class FilterEffect(
    val filterId: String,
    val intensity: Float = 1.0f,
    // ...
) : Effect(...)
```

---

## 4. 核心数据流

### 4.1 导入素材

```
用户选择素材
    ↓
MediaScanner 解析（时长、分辨率、旋转、帧率）
    ↓
生成缩略图与波形图（异步缓存）
    ↓
创建 Clip 对象并加入 Timeline
    ↓
触发 Player 刷新预览
```

### 4.2 剪辑操作

```
用户操作（分割/删除/裁剪）
    ↓
Command 模式封装为可撤销命令
    ↓
修改 Timeline 领域模型
    ↓
通知 Player 更新渲染图
    ↓
自动保存 Project 到 Room
```

### 4.3 实时预览

```
Timeline 当前时间 positionUs
    ↓
Player 计算当前激活的 Clip 与 Effect
    ↓
OpenGL Renderer 按轨道顺序合成帧
    ↓
输出到 SurfaceView/TextureView
```

### 4.4 导出渲染

```
Exporter 按时间线逐帧读取
    ↓
Decoder 解码原始帧
    ↓
OpenGL Renderer 应用滤镜/特效/合成
    ↓
Encoder 编码为 H.264/H.265
    ↓
MediaMuxer 混流为 MP4
```

---

## 5. 性能指标

| 场景 | 目标 | 测试方法 |
|------|------|----------|
| 冷启动 | ≤ 2s | 从 Launcher 点击到 MainActivity 渲染完成 |
| 素材导入 | 1min 1080p 素材索引 ≤ 3s | 导入 10 段 6s 1080p 视频 |
| 时间线操作反馈 | ≤ 100ms | 分割/删除/拖拽后 UI 刷新完成 |
| 预览帧率 | ≥ 24fps @ 1080p | Systrace / 自定义 FPS 计数 |
| 导出速度 | ≤ 0.7x 视频时长 | 1min 1080p 30fps 视频在中档设备导出 |
| 内存占用 | 导出 1080p ≤ 400MB | Android Studio Profiler |
| 崩溃率 | < 0.5% | 发布版本 Firebase Crashlytics |

---

## 6. 安全与兼容性

- 所有文件操作必须通过 SAF / MediaStore / 应用私有目录，适配 Android 10+ Scoped Storage
- 相机、麦克风、存储权限遵循最小权限原则，动态申请
- 导出任务在后台 Service 中执行，避免 ANR
- 对不支持硬解码的设备降级处理或友好提示

---

## 7. 测试策略

| 层级 | 方式 |
|------|------|
| Domain | JUnit + MockK 单元测试 |
| UseCase | Coroutines Test |
| UI | Compose UI Test + Espresso |
| 性能 | Macrobenchmark |
| 兼容性 | Firebase Test Lab 多机型测试 |

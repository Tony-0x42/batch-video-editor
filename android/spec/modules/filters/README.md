# 滤镜调色模块规范（filters）

> 本文档定义 VideoEditor-For-Android 项目中「滤镜调色」模块的职责边界、功能清单、数据模型、交互流程、实现要点与性能质量要求。
> 实现本模块前，必须先阅读 `spec/README.md`、`spec/technical-spec.md`、`spec/project-boundary.md`、`spec/project-guidelines.md`。

---

## 1. 模块概述

滤镜调色模块负责为视频剪辑工程提供**实时预览与导出渲染阶段的画面风格化与美化能力**，覆盖预设滤镜、滤镜强度调节、基础美颜（磨皮、美白）三大核心能力。该模块位于 Engine 层的 Renderer 子系统内部，通过 Domain 层定义的 `FilterEffect` 模型与 Timeline 模块联动，接收 Player 与 Exporter 在每一帧传入的原始纹理，按时间线作用区间叠加对应 Shader 效果后输出处理后的纹理。

本模块解决的核心问题是：在保证实时预览帧率的前提下，让非专业用户通过点选与滑杆即可快速获得统一、美观的画面风格，同时为后续 LUT 高级调色预留可扩展的 Shader 管线。模块不直接处理时间计算、素材解码、音频混音或导出封装，只专注于「输入纹理 → 滤镜链处理 → 输出纹理」的纯图像处理职责。

---

## 2. 功能清单

### P0 - 首个 MVP 必须实现

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 预设滤镜列表 | 提供 20 款以上内置预设滤镜（如清新、胶片、黑白、复古、日系、暖黄、冷蓝、美食、风景、人像等），以缩略图形式在滤镜面板展示 | 滤镜资源随安装包下发；列表可横向滑动；选中后实时预览在 100ms 内生效 |
| 滤镜实时预览 | 用户在滤镜面板点选任一预设后，顶部播放器当前帧立即呈现效果，且滑动时间线或播放时持续生效 | 1080p 素材预览帧率 ≥ 24fps；切换滤镜无明显闪屏或黑帧 |
| 滤镜强度调节 | 为每个生效滤镜提供 0% ~ 100% 的强度滑杆，0% 等价于原图，100% 为完整效果 | 滑杆拖动时预览实时更新；强度数值精确到 1%；导出结果与预览一致 |
| 滤镜作用于片段 | 滤镜以 Effect 形式绑定到具体 VideoClip，支持为不同片段设置不同滤镜，也支持同一片段叠加多个滤镜 | 修改片段滤镜不影响其他片段；复制片段时携带滤镜配置；删除片段时同步移除 |
| 基础美颜 | 提供磨皮与美白两个独立开关/滑杆，作用于整个工程画面 | 磨皮、美白可单独开启/关闭；强度范围 0~100；预览与导出效果一致 |
| 滤镜配置持久化 | 滤镜选择、强度、美颜参数随工程自动保存，下次打开草稿可恢复 | 使用 Room/JSON 持久化；参数恢复后预览效果与关闭前一致 |

### P1 - MVP 后 1~2 个迭代

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 全局调色层 | 在 Timeline 上新增一个全局调色轨道/调色层，可作用于整段视频，不依赖具体 Clip | 调整全局调色后影响所有片段；与片段滤镜以合理方式混合（建议片段滤镜优先） |
| 基础参数调色 | 提供亮度、对比度、饱和度、色温、色调、锐化、暗角等独立参数调节 | 每个参数提供 -100 ~ +100 或 0 ~ 100 滑杆；参数变化实时预览；导出一致 |
| 滤镜收藏与最近使用 | 用户可收藏常用滤镜，面板顶部展示「最近使用」与「收藏」分组 | 收藏状态持久化；最近使用按时间倒序排列，最多 10 个 |
| 滤镜预览缩略图 | 滤镜面板每个缩略图使用当前视频首帧或关键帧生成带滤镜效果的小图 | 缩略图异步生成，不阻塞主线程；失败时降级显示默认色块 |
| 美颜参数细分 | 将美颜扩展为磨皮、美白、瘦脸、大眼、红润等（在移动端 Shader 可实现范围内） | 仅实现 GPU 可实时完成的项；复杂形变美颜不在本模块 P1 范围 |

### P2 - 长期规划

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| LUT 导入与高级调色 | 支持用户导入 .cube / .3dl LUT 文件，并提供 LUT 强度调节 | 兼容常见 33x33x33 LUT；导入失败给出明确提示；渲染性能不显著下降 |
| HSL 分色调色 | 针对红、橙、黄、绿、青、蓝、紫分别调整色相、饱和度、明度 | 每个颜色通道独立三参数；实时预览；导出一致 |
| RGB 曲线与色轮 | 提供专业级 RGB 曲线、色温/色调色轮、高光/阴影分离调色 | 界面与算法可落地；性能满足实时预览 |
| 在线滤镜商店接口 | 为后续云端素材商店预留滤镜下载、校验、缓存、版本管理接口 | 接口设计向后兼容；本地离线时可继续使用已下载滤镜 |
| AI 智能调色建议 | 基于画面内容推荐滤镜（云端或端侧模型，不在本阶段实现） | 仅预留扩展点，不实现具体 AI 逻辑 |

---

## 3. 数据模型与接口

### 3.1 核心领域模型

```kotlin
/**
 * 滤镜效果领域模型，继承自通用 Effect。
 * 作用在时间线上的 [startTimeUs, endTimeUs) 区间，通常与 Clip 区间对齐。
 */
data class FilterEffect(
    override val id: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    val filterId: String,              // 滤镜唯一标识，如 "filter_fresh", "filter_film"
    val intensity: Float = 1.0f,       // 0.0 ~ 1.0
    val beautyParams: BeautyParams = BeautyParams()
) : Effect(id, startTimeUs, endTimeUs)

/**
 * 美颜参数。
 * 所有字段取值范围 0.0 ~ 1.0，0 表示不生效。
 */
data class BeautyParams(
    val skinSmooth: Float = 0.0f,      // 磨皮
    val whitening: Float = 0.0f,       // 美白
    val cheekThinning: Float = 0.0f,   // 瘦脸（P1 预留）
    val eyeEnlarging: Float = 0.0f     // 大眼（P1 预留）
)

/**
 * 调色参数（P1）。
 */
data class ColorAdjustParams(
    val brightness: Float = 0.0f,      // -1.0 ~ 1.0
    val contrast: Float = 0.0f,        // -1.0 ~ 1.0
    val saturation: Float = 0.0f,      // -1.0 ~ 1.0
    val warmth: Float = 0.0f,          // -1.0 ~ 1.0
    val tint: Float = 0.0f,            // -1.0 ~ 1.0
    val sharpness: Float = 0.0f,       // 0.0 ~ 1.0
    val vignette: Float = 0.0f         // 0.0 ~ 1.0
)

/**
 * 滤镜元数据，用于 UI 展示与资源定位。
 */
data class FilterMeta(
    val id: String,
    val displayName: String,
    val category: String,              // 分类：全部 / 人像 / 风景 / 美食 / 黑白 …
    val thumbnailPath: String?,        // 内置资源路径或本地缓存路径
    val shaderName: String,            // 对应 Shader 程序名
    val supportsIntensity: Boolean = true,
    val isBuiltIn: Boolean = true
)
```

### 3.2 引擎层接口

```kotlin
/**
 * 滤镜渲染器接口，由 Engine 层实现。
 * 对 Player 与 Exporter 暴露统一的帧处理能力。
 */
interface FilterRenderer {
    /**
     * 初始化 OpenGL 资源，必须在 GL 线程调用。
     */
    fun init(surfaceSize: Size)

    /**
     * 处理一帧画面。
     * @param inputTextureId 输入纹理 ID（OES 或 2D）
     * @param inputTextureType 输入纹理类型
     * @param positionUs 当前帧在时间线上的位置
     * @param activeFilters 当前时间点生效的滤镜列表，按作用顺序排列
     * @return 处理后的输出纹理 ID
     */
    fun renderFrame(
        inputTextureId: Int,
        inputTextureType: TextureType,
        positionUs: Long,
        activeFilters: List<FilterEffect>
    ): Int

    /**
     * 释放 OpenGL 资源。
     */
    fun release()
}

enum class TextureType { OES, TEXTURE_2D }

/**
 * 单个滤镜的 Shader 实现接口。
 */
interface FilterShader {
    val filterId: String
    fun createProgram(): Int
    fun apply(inputTexture: Int, outputFrameBuffer: Int, intensity: Float, extraParams: Bundle?)
    fun release()
}

/**
 * 滤镜资源管理接口，负责内置滤镜加载与 LUT/在线滤镜缓存。
 */
interface FilterRepository {
    suspend fun getBuiltInFilters(): List<FilterMeta>
    suspend fun getFilterMeta(filterId: String): FilterMeta?
    suspend fun loadShader(filterId: String): Result<FilterShader>
    suspend fun importLut(uri: Uri): Result<FilterMeta>
    fun getRecentFilters(): Flow<List<String>>
    fun getFavoriteFilters(): Flow<List<String>>
    suspend fun toggleFavorite(filterId: String)
}
```

### 3.3 UseCase / ViewModel 接口

```kotlin
/**
 * 应用或更新片段滤镜。
 */
class ApplyFilterUseCase(
    private val timelineRepository: TimelineRepository,
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(
        clipId: String,
        filterId: String? = null,
        intensity: Float? = null,
        beautyParams: BeautyParams? = null
    ): Result<Unit>
}

/**
 * 移除片段上的指定滤镜。
 */
class RemoveFilterUseCase(private val timelineRepository: TimelineRepository) {
    suspend operator fun invoke(clipId: String, effectId: String): Result<Unit>
}

/**
 * 滤镜面板 ViewModel 状态。
 */
data class FilterPanelUiState(
    val filters: List<FilterMeta> = emptyList(),
    val selectedFilterId: String? = null,
    val intensity: Float = 1.0f,
    val beautyParams: BeautyParams = BeautyParams(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

### 3.4 与其他模块的依赖接口

| 依赖模块 | 依赖接口/模型 | 说明 |
|---------|-------------|------|
| timeline | `Timeline`、`VideoClip`、`Effect` | 滤镜以 Effect 形式挂载到 Clip 或全局调色层 |
| player | `FilterRenderer` 由 Player 调用 | Player 传入当前 positionUs 与激活滤镜列表 |
| export | `FilterRenderer` 由 Exporter 调用 | Exporter 在逐帧编码前应用滤镜，保证导出与预览一致 |
| project | `VideoProject` 持久化 | 滤镜参数随工程保存与恢复 |
| media | 素材缩略图 | 滤镜面板缩略图可基于视频首帧生成 |
| uiux | 设计系统、组件规范 | 滤镜面板、滑杆、按钮需遵循统一设计系统 |

---

## 4. 交互与流程

### 4.1 用户操作流程

**场景 A：为某一片段添加预设滤镜**

1. 用户在时间轴选中一个 VideoClip。
2. 点击底部工具栏「滤镜」按钮，进入滤镜面板。
3. 面板横向展示内置滤镜缩略图，首项为「原图」。
4. 用户点选目标滤镜，播放器立即实时显示效果。
5. 用户拖动强度滑杆调整效果强弱，预览同步变化。
6. 点击「完成」或返回，系统将 `FilterEffect` 写入该 Clip 的 `filters` 列表，触发 Project 自动保存。

**场景 B：调整全局美颜**

1. 用户进入「美颜」面板。
2. 开启磨皮、美白开关，并拖动强度滑杆。
3. 系统实时将美颜参数写入工程级配置，播放器每帧应用。
4. 美颜效果不绑定具体 Clip，作用于整个画面输出。

**场景 C：导出时确保滤镜生效**

1. 用户点击导出，Exporter 初始化渲染管线。
2. Exporter 按时间线逐帧读取，解码原始帧得到纹理。
3. Exporter 调用 `FilterRenderer.renderFrame(positionUs, activeFilters)`。
4. 处理后的纹理传入 Encoder 编码，最终封装为 MP4。

### 4.2 模块内部数据流

```
用户选择滤镜 / 调整强度 / 开关美颜
    ↓
FilterPanelViewModel 校验参数范围
    ↓
ApplyFilterUseCase 修改 Timeline 领域模型
    ↓
TimelineRepository 通知 Player 渲染图刷新
    ↓
Player 在 GL 线程计算当前 positionUs 对应的 activeFilters
    ↓
FilterRenderer 按顺序调用 FilterShader 处理纹理
    ↓
处理后的帧输出到 SurfaceView / TextureView

自动保存：
ApplyFilterUseCase 修改模型后
    ↓
ProjectRepository 将 FilterEffect 持久化到 Room/JSON
```

### 4.3 关键时序（伪代码）

```
每一帧：
  Player.onDrawFrame()
    positionUs = player.getCurrentPositionUs()
    activeFilters = timeline.getActiveFiltersAt(positionUs)
    outputTex = filterRenderer.renderFrame(inputTex, positionUs, activeFilters)
    screenRenderer.drawToScreen(outputTex)
```

---

## 5. 实现要点

### 5.1 技术方案选择

- **渲染管线**：基于 OpenGL ES 2.0/3.0，采用「多 Pass 滤镜链」结构。每个 FilterShader 负责将输入纹理渲染到帧缓冲对象（FBO），下一个 Shader 以上一个 FBO 的纹理作为输入，最终输出到屏幕或编码器。
- **纹理类型适配**：解码器输出通常为 OES 外部纹理（SurfaceTexture），需使用 `GL_TEXTURE_EXTERNAL_OES` 采样；滤镜处理后的中间纹理使用普通 2D 纹理。Shader 需根据 `TextureType` 切换 sampler 宏。
- **预设滤镜实现**：每个内置滤镜对应一个独立的 Fragment Shader，通过 uniforms 注入强度、颜色矩阵、LUT 纹理等参数。避免一个巨型 Shader 导致编译慢、维护难。
- **美颜实现**：磨皮采用双边滤波 + 高斯模糊 + 肤色检测混合；美白采用提亮 + 饱和度微调。肤色检测参数需根据常见亚洲肤色调优，避免过度漂白背景。
- **资源管理**：内置滤镜 Shader 源码放置于 `res/raw/` 或 assets 目录，运行期编译缓存；LUT 文件放置于应用私有缓存目录，按 MD5 校验防止篡改。

### 5.2 需要特别注意的难点

- **实时性能**：多滤镜叠加时，每增加一次 FBO 切换都会带来带宽与耗时开销。必须限制同时生效滤镜数量（建议 P0 不超过 3 个），并复用 FBO 对象避免重复分配。
- **预览与导出一致性**：Player 与 Exporter 必须共用同一套 `FilterRenderer` 与 Shader 参数，禁止维护两份逻辑。导出分辨率可能与预览不同，Shader 中的像素相关参数（如磨皮半径、暗角）需按输出尺寸动态换算。
- **GL 上下文共享**：Player 预览与 Exporter 编码可能运行在不同 GL 上下文，纹理不能直接共享。方案是 Domain 层只传递 FilterEffect 参数，Engine 层各自创建独立的 Shader 与纹理资源。
- **强度插值**：部分滤镜在 0% 时应完全等效原图，实现上可通过 mix(originalColor, filteredColor, intensity) 保证数值正确。
- **OES 纹理限制**：OES 纹理不能作为 FBO 颜色附件，必须先通过「OES → 2D」的第一次 Pass 转换。
- **色调映射**：滤镜叠加后可能出现色偏或溢出，建议在 Shader 末尾统一做 clamp 到 [0, 1]。

### 5.3 与现有代码的衔接建议

- 项目已有基于 OpenGL ES 2.0 的滤镜链与美颜实现，应优先复用其 Shader 源码与 GL 工具类，避免推倒重来。
- 将旧代码中耦合在 Activity/Fragment 的滤镜逻辑抽离为独立的 `FilterRenderer` 与 `FilterShader`，符合 Domain/Engine 分层。
- 旧版 `android.hardware.Camera` 录制时的滤镜应用属于 recorder 模块，本模块只提供 Shader 能力，由 recorder 按需调用。
- 滤镜参数持久化建议新增 Room Entity 或在现有 Project JSON 中扩展字段，不要破坏已有工程格式。

---

## 6. 性能与质量要求

### 6.1 性能指标

| 场景 | 目标 | 测试方法 |
|------|------|---------|
| 滤镜切换延迟 | 点选滤镜后预览生效 ≤ 100ms | 高速摄像或日志打点 |
| 单滤镜渲染耗时 | 1080p 单滤镜单帧 GPU 耗时 ≤ 5ms | GPU 计时器 / Systrace |
| 三滤镜叠加 | 1080p 三滤镜叠加单帧 GPU 耗时 ≤ 12ms | GPU 计时器 |
| 预览帧率 | 开启单滤镜 + 美颜后 ≥ 24fps | 自定义 FPS 计数 |
| 导出速度影响 | 开启滤镜后导出耗时增加 ≤ 15% | 同一段视频开关滤镜对比 |
| 内存占用 | 滤镜 FBO 与纹理复用，导出 1080p 增量 ≤ 30MB | Android Studio Profiler |

### 6.2 常见异常与处理

| 异常场景 | 原因 | 处理策略 |
|---------|------|---------|
| Shader 编译失败 | GPU 不支持某些 GLSL 语法或扩展 | 降级到简化版 Shader；记录设备型号；上报 Crashlytics |
| 滤镜资源加载失败 | 资源文件损坏或被清理 | 使用默认「原图」滤镜；提示用户重新下载/恢复；不崩溃 |
| LUT 导入失败 | 格式不兼容或文件过大 | 明确提示「不支持的 LUT 格式」；提供格式说明 |
| 强度参数越界 | UI 滑杆异常或工程数据损坏 | Domain 层强制 clamp 到 [0, 1]；修复持久化数据 |
| GL 上下文丢失 | 后台切换、Surface 重建 | 释放并重建 GL 资源；恢复时重新加载 Shader 与 LUT |
| 实时预览卡顿 | 滤镜链过长或设备性能不足 | 自动降级为单滤镜；提示用户关闭部分效果 |
| 美颜肤色误判 | 复杂光线或深色背景 | 提供肤色范围微调参数；默认参数保守，避免过度磨皮 |

### 6.3 质量要求

- 所有公共 API 必须带 KDoc。
- `FilterEffect` 参数变更必须通过 Command 模式封装，支持撤销/重做。
- Shader 中禁止出现硬编码的魔法数字，所有可调参数通过 uniform 注入。
- 滤镜缩略图生成必须异步，失败时不阻塞主线程。
- 导出结果与预览效果差异需控制在人眼不可察觉范围（PSNR ≥ 40dB 或视觉比对通过）。

---

## 7. 依赖模块

本模块实现与运行依赖以下 `spec/modules` 下的模块：

| 模块 | 路径 | 依赖说明 |
|------|------|---------|
| timeline | `modules/timeline` | FilterEffect 继承自 Effect，挂载到 Clip 或全局调色层；依赖 Timeline 的时间计算 |
| player | `modules/player` | Player 调用 FilterRenderer 完成实时预览渲染 |
| export | `modules/export` | Exporter 调用 FilterRenderer 完成导出前的帧处理 |
| project | `modules/project` | 工程模型保存滤镜参数，依赖 Project 持久化机制 |
| media | `modules/media` | 滤镜缩略图可能基于视频首帧生成，依赖缩略图加载能力 |
| uiux | `modules/uiux` | 滤镜面板、滑杆、缩略图网格遵循 uiux 设计系统与组件规范 |

---

## 8. 边界变更记录

| 版本 | 日期 | 变更说明 |
|------|------|---------|
| v0.1 | 2025-06-28 | 初始版本，明确 P0 为预设滤镜、强度调节、片段挂载、基础美颜；P1 为全局调色层与基础参数调色；P2 为 LUT/HSL/曲线/在线商店 |

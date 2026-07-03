# 特效字幕模块规范（effects）

> 本文件定义 VideoEditor-For-Android 项目中「特效字幕」模块的职责、数据模型、接口、交互流程、实现要点与性能要求。
> 模块覆盖范围：文字/字幕、贴纸、转场、关键帧动画、画中画（PiP）。

---

## 1. 模块概述

特效字幕（effects）模块负责在视频时间线上叠加非源视频本身的视觉元素，包括文字字幕、静态/动态贴纸、片段间转场、基于关键帧的属性动画以及画中画视频层。该模块位于 Engine 层与 Domain 层之间：Domain 层定义 `Effect` 领域模型与轨道归属，Engine 层的 `Renderer` 根据当前播放/导出时间采样并合成这些元素到最终画面。

本模块解决的核心问题是：让用户能够在不修改原始素材的前提下，以轨道化、可撤销、可预览的方式为视频添加表达性视觉元素，并保证实时预览与最终导出结果一致。所有效果参数必须可序列化到工程文件，支持草稿自动保存与恢复。

---

## 2. 功能清单

### P0 — 首个 MVP 必须实现

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 文字字幕添加 | 用户可在时间轴任意位置添加文字片段，文字以独立轨道形式存在 | 点击「文字」入口后可在播放器双击或拖拽添加；文字片段出现在文字轨，可拉伸调整时长 |
| 文字样式编辑 | 支持字体、字号、颜色、描边、阴影、背景、对齐方式 | 至少提供 10 款内置字体；描边宽度 0~10px；阴影可开关并调节偏移与模糊；样式修改后预览实时更新 |
| 文字位置与变换 | 支持拖拽改变位置、双指缩放/旋转；提供居中对齐等辅助吸附 | 位置误差在 2% 画布尺寸内；缩放范围 0.1x~5.0x；旋转角度 -180°~180° |
| 文字出入场动画 | 提供淡入淡出、缩放进入、位移进入/退出等基础动画 | 每种动画可独立设置入场/持续/出场三段时长；动画与文字片段时间范围绑定 |
| 文字轨道管理 | 文字轨与视频轨分离，支持多段文字按时间叠加 | 同一时间轴可存在多个不重叠或重叠文字片段；上层文字覆盖下层；轨道层级关系由 Timeline 模块统一维护 |
| 基础字幕安全区 | 提供 9:16/16:9/1:1/4:3 比例下的安全区域提示，防止文字被裁切 | 切换画布比例时，超出安全区的文字给出视觉提示；导出时按实际画布裁剪 |

### P1 — MVP 后 1~2 个迭代

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 转场效果 | 在两个相邻视频片段之间应用过渡动画，如淡入淡出、闪白、闪黑、叠化、左右推开、模糊过渡 | 提供不少于 8 种基础转场；转场时长 0.2s~2.0s 可调；实时预览与导出结果一致；转场不引起音视频错位 |
| 关键帧动画 | 对文字、贴纸、画中画等视觉元素的属性（位置、缩放、旋转、透明度、锚点）设置关键帧，自动插值生成动画 | 支持线性/缓入/缓出三种插值；关键帧时间点精确到 1ms；删除关键帧后动画平滑回退 |
| 贴纸系统 | 用户可从本地贴纸包选择静态贴纸并添加到画面；支持缩放、旋转、位置、时长调节 | 支持 PNG/WEBP 贴纸；贴纸解码异步完成；贴纸渲染不阻塞预览线程；提供 50+ 内置贴纸 |
| 画中画增强 | 在主视频之上叠加额外的视频/图片层，支持圆角、边框、阴影、蒙版、不透明度 | 画中画轨可独立存在；支持视频与图片混合；圆角半径 0~50% 宽度；不透明度 0~100% |
| 简单特效预设 | 提供抖动、故障（Glitch）、径向模糊、光晕、老电影等可作用于片段的特效 | 特效以 Shader 实现；可作用于单个视频片段或整个图层；强度 0~100% 可调 |
| 文字模板 | 提供标题、副标题、片尾署名等预设文字模板，一键应用样式与动画 | 不少于 10 套模板；模板参数（文字内容、颜色）可二次编辑 |

### P2 — 长期规划

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| AI 语音识别字幕 | 根据视频音轨自动生成字幕片段并挂载到文字轨 | 准确率目标 ≥ 85%（普通话场景）；生成后可逐条校对编辑；架构上预留接口，不阻塞 MVP |
| 3D 文字与高级字幕模板 | 支持带透视、描边发光、金属质感的高级文字效果 | 渲染帧率不低于 24fps；模板支持参数化替换 |
| 动态贴纸与 Lottie | 支持 GIF、WebP 动画、Lottie 等动态贴纸格式 | 动态贴纸循环播放与片段时长同步；内存占用可控 |
| 在线素材库 | 从云端下载贴纸、转场、字体、特效包 | 提供素材缓存与离线使用；下载失败可重试；接入时遵循项目边界变更流程 |
| 粒子特效系统 | 提供火花、雪花、光斑等粒子效果层 | 粒子渲染基于 GPU；粒子数量 1000+ 时仍保持流畅预览 |

---

## 3. 数据模型与接口

### 3.1 核心领域模型

```kotlin
/**
 * 所有视觉效果的基类，定义在时间轴上的生效区间。
 */
sealed class Effect(
    open val id: String,
    open val trackId: String,
    open val startTimeUs: Long,
    open val endTimeUs: Long,
    open val zIndex: Int
)

/**
 * 文字/字幕效果。
 */
data class TextEffect(
    override val id: String,
    override val trackId: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    override val zIndex: Int = 0,
    val text: String,
    val style: TextStyle,
    val transform: Transform2D,
    val animation: TextAnimation = TextAnimation(),
    val keyframes: List<Keyframe<Transform2D>> = emptyList()
) : Effect(id, trackId, startTimeUs, endTimeUs, zIndex)

/**
 * 贴纸效果。
 */
data class StickerEffect(
    override val id: String,
    override val trackId: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    override val zIndex: Int = 0,
    val assetUri: Uri,
    val transform: Transform2D,
    val keyframes: List<Keyframe<Transform2D>> = emptyList()
) : Effect(id, trackId, startTimeUs, endTimeUs, zIndex)

/**
 * 转场效果，依附于相邻两个视频片段之间。
 */
data class TransitionEffect(
    override val id: String,
    override val trackId: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    override val zIndex: Int = 0,
    val type: TransitionType,
    val durationUs: Long,
    val intensity: Float = 1.0f,
    val leftClipId: String,
    val rightClipId: String
) : Effect(id, trackId, startTimeUs, endTimeUs, zIndex)

/**
 * 画中画效果，本质是一个可变换的视频/图片片段层。
 */
data class PipEffect(
    override val id: String,
    override val trackId: String,
    override val startTimeUs: Long,
    override val endTimeUs: Long,
    override val zIndex: Int = 0,
    val sourceUri: Uri,
    val sourceStartUs: Long,
    val sourceEndUs: Long,
    val transform: Transform2D,
    val cornerRadius: Float = 0f,
    val border: BorderStyle? = null,
    val mask: MaskType = MaskType.NONE,
    val keyframes: List<Keyframe<PipState>> = emptyList()
) : Effect(id, trackId, startTimeUs, endTimeUs, zIndex)

/**
 * 2D 变换参数，使用归一化画布坐标。
 */
data class Transform2D(
    val x: Float = 0.5f,      // 中心点 X，0~1
    val y: Float = 0.5f,      // 中心点 Y，0~1
    val scaleX: Float = 1.0f,
    val scaleY: Float = 1.0f,
    val rotation: Float = 0f, // 角度，顺时针
    val alpha: Float = 1.0f   // 0~1
)

/**
 * 文字样式。
 */
data class TextStyle(
    val fontName: String,
    val fontSizeSp: Float = 24f,
    val color: Int = 0xFFFFFFFF.toInt(),
    val strokeColor: Int = 0xFF000000.toInt(),
    val strokeWidthPx: Float = 0f,
    val shadowColor: Int = 0x88000000.toInt(),
    val shadowOffsetX: Float = 0f,
    val shadowOffsetY: Float = 0f,
    val shadowRadius: Float = 0f,
    val backgroundColor: Int = 0x00000000,
    val alignment: TextAlign = TextAlign.CENTER,
    val letterSpacing: Float = 0f,
    val lineSpacing: Float = 1.0f
)

/**
 * 关键帧定义，支持对任意可插值状态做时间采样。
 */
data class Keyframe<T>(
    val timeUs: Long,
    val value: T,
    val interpolation: InterpolationType = InterpolationType.LINEAR,
    val easing: EasingCurve = EasingCurve.EASE_IN_OUT
)

enum class TransitionType { FADE, FLASH_WHITE, FLASH_BLACK, DISSOLVE, PUSH_LEFT, PUSH_RIGHT, BLUR, ZOOM_IN }
enum class InterpolationType { LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT, BEZIER }
enum class MaskType { NONE, CIRCLE, HEART, ROUNDED_RECT }
```

### 3.2 输入输出与状态管理

**输入**
- 用户通过 UI 面板发起的添加/删除/修改命令
- Timeline 模块传入的当前播放时间 `positionUs`
- Player/Exporter 传入的当前帧纹理与渲染上下文
- Project 模块反序列化后的 Effect 列表

**输出**
- 更新后的 Timeline 领域模型
- 纹理合成结果（到 Player Surface 或 Exporter Encoder Surface）
- 预览状态 Flow（当前选中的 Effect、关键帧编辑状态、转场应用状态）

**状态管理**
- Domain 层仅保存不可变 Effect 对象；任何修改通过 UseCase 生成新对象并替换
- 当前选中的 Effect ID 与编辑面板状态由 ViewModel 管理
- 关键帧编辑态（选中关键帧、预览 scrubbing）单独封装为 `EffectEditState`
- 所有 Effect 变更必须触发 Project 自动保存

### 3.3 依赖接口定义

```kotlin
/**
 * 特效渲染器接口，由 Engine 层实现。
 */
interface EffectsRenderer {
    /**
     * 初始化 OpenGL 资源，必须在 GL 线程调用。
     */
    fun prepare(outputWidth: Int, outputHeight: Int)

    /**
     * 渲染一帧，将当前激活的 Effect 合成到输出纹理。
     *
     * @param inputTexture 当前视频帧纹理
     * @param positionUs 当前时间戳
     * @param effects 当前需要渲染的效果列表
     * @return 合成后的纹理 ID
     */
    fun renderFrame(
        inputTexture: Int,
        positionUs: Long,
        effects: List<Effect>
    ): Int

    /**
     * 释放资源。
     */
    fun release()
}

/**
 * 文字纹理生成器接口。
 */
interface TextTextureProvider {
    /**
     * 根据 TextEffect 生成或复用 OpenGL 纹理，返回纹理 ID 与宽高。
     */
    fun getTexture(effect: TextEffect): TextureInfo
    fun invalidate(effectId: String)
}

/**
 * 转场 Shader 管理器。
 */
interface TransitionShaderProvider {
    fun getShader(type: TransitionType): ShaderProgram
    fun preload(types: Set<TransitionType>)
}

/**
 * 关键帧插值器接口。
 */
interface KeyframeInterpolator<T> {
    fun evaluate(positionUs: Long, keyframes: List<Keyframe<T>>): T
}
```

---

## 4. 交互与流程

### 4.1 添加文字效果

```
用户在工具栏点击「文字」
    ↓
ViewModel 调用 AddTextEffectUseCase，默认位置居中、时长 3s
    ↓
UseCase 生成 TextEffect 并插入 Timeline 的文字轨道
    ↓
Command 入撤销重做栈；自动保存 Project
    ↓
Player 收到 Timeline 变更，刷新当前帧
    ↓
UI 进入文字编辑面板，高亮新增的文字片段
```

### 4.2 应用转场效果

```
用户选中两个相邻视频片段的交界处
    ↓
UI 显示可应用转场的区域（高亮交界）
    ↓
用户选择转场类型与时长
    ↓
ApplyTransitionUseCase 校验左右片段是否相邻且同轨
    ↓
生成 TransitionEffect，startTimeUs = 左片段结束时间 - 转场时长/2
                    endTimeUs = 右片段开始时间 + 转场时长/2
    ↓
Timeline 更新；Player 在交界区间使用转场 Shader 混合两路输入
    ↓
导出时 Exporter 在相同时间区间复用同一 Shader
```

### 4.3 关键帧动画编辑

```
用户选中一个文字/贴纸/PiP 元素
    ↓
进入关键帧面板，显示当前时间点属性
    ↓
用户移动元素并点击「添加关键帧」
    ↓
KeyframeUseCase 在 effect.keyframes 中插入新的 Keyframe<Transform2D>
    ↓
Player 在播放时按时间插值计算 Transform2D
    ↓
关键帧时间点冲突时，后者覆盖前者，并提示用户
```

### 4.4 模块内部数据流

```
┌─────────────────────────────────────────────────────────────┐
│ UI Layer                                                     │
│  文字面板/贴纸面板/转场选择器/关键帧编辑器                    │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ UseCase Layer                                                │
│  AddTextEffectUseCase                                        │
│  UpdateTextStyleUseCase                                      │
│  ApplyTransitionUseCase                                      │
│  AddKeyframeUseCase                                          │
│  DeleteEffectUseCase                                         │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Domain Layer                                                 │
│  TextEffect / StickerEffect / TransitionEffect / PipEffect   │
│  Timeline (轨道与层级)                                       │
│  EffectRepository (接口)                                     │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Engine Layer                                                 │
│  EffectsRenderer → OpenGL 合成                               │
│  TextTextureProvider → Canvas 纹理缓存                       │
│  TransitionShaderProvider → Shader 管理                      │
│  KeyframeInterpolator → 插值计算                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. 实现要点

### 5.1 技术方案选择

- **文字渲染**：采用 Android `Canvas` 离屏绘制到 `Bitmap`，再上传为 OpenGL 纹理。对于静态文字，按 `TextEffect` 内容+样式做纹理缓存；内容或样式变更时失效缓存并重新生成。
- **贴纸渲染**： stickers 统一解码为 OpenGL 纹理，使用 Glide 加载并做尺寸限制（最大 2048x2048），大图按比例缩放。
- **转场渲染**：每个 TransitionType 对应一个 Fragment Shader，编译后缓存。转场需要同时输入左右两个片段的帧纹理，因此 Player/Exporter 在转场区间内需要同时准备两路解码器输出。
- **关键帧插值**：对 Transform2D 中各标量分量分别插值；角度插值需处理 -180°/180° 跳变问题。推荐实现线性、缓入、缓出三种基础曲线，贝塞尔曲线作为 P1 增强。
- **画中画**：PiP 作为独立视频/图片源，由 Player 模块在播放时单独解码并输出到纹理，再由 EffectsRenderer 按 Transform2D 变换后叠加。

### 5.2 需要特别注意的难点

1. **坐标系统一**：所有 2D 变换在 Domain 层使用归一化画布坐标（0~1），在 Engine 层根据输出分辨率转换为像素坐标。必须保证实时预览与导出使用同一套坐标映射，避免「预览正常、导出偏移」。
2. **转场与音频同步**：转场仅作用于画面，不得影响音频播放；导出时视频帧与音频帧的时间戳必须严格对齐。
3. **文字纹理缓存失效策略**：文字内容、样式、画布比例任一变化都可能导致纹理变化。需要建立以「effectId + 内容哈希 + 样式哈希 + 输出分辨率」为键的缓存，并设置最大缓存容量（建议 32MB）。
4. **多轨道层级合成**：文字轨、贴纸轨、PiP 轨之间可能存在重叠，必须按 zIndex 与轨道类型顺序合成。推荐合成顺序：主视频 → PiP → 贴纸 → 文字 → 特效层。
5. **关键帧编辑的实时反馈**：用户拖动关键帧时间点时，需要逐帧回退到关键帧位置展示效果；应避免在 UI 线程执行大量插值计算。
6. **OpenGL 上下文线程安全**：所有纹理生成、Shader 编译、帧合成必须在同一线程（GL 线程）执行；Domain 模型变更通过线程安全队列通知 Renderer。

### 5.3 与现有代码的衔接建议

- 项目已有基于 OpenGL ES 2.0 的滤镜链，EffectsRenderer 应复用现有 `GLUtils`、`TextureHelper` 等工具类，并统一使用 FBO（Framebuffer Object）进行离屏合成。
- 现有 JNI 混音能力主要服务于 audio 模块，effects 模块不直接调用 JNI，但在画中画包含独立音轨时，需通过 audio 模块的混音接口合并。
- 现有工程尚无多轨道模型，effects 模块需等待 timeline 模块建立 Track/Clip 抽象后再接入；接入前可先用单轨 `List<Effect>` 做快速验证，但不得破坏后续多轨道扩展。
- 文字/贴纸编辑面板应遵循 `spec/modules/uiux/README.md` 中的设计系统，使用 Jetpack Compose 实现。

### 5.4 推荐代码目录结构

effects 模块的代码应落在如下包结构中，严格遵循项目规范的分层要求：

```
app/src/main/java/com/example/cj/videoeditor/
├── domain/effects/
│   ├── model/
│   │   ├── Effect.kt
│   │   ├── TextEffect.kt
│   │   ├── StickerEffect.kt
│   │   ├── TransitionEffect.kt
│   │   ├── PipEffect.kt
│   │   ├── Transform2D.kt
│   │   ├── TextStyle.kt
│   │   ├── Keyframe.kt
│   │   └── Animation.kt
│   ├── repository/
│   │   └── EffectRepository.kt
│   └── engine/
│       ├── EffectsRenderer.kt
│       ├── TextTextureProvider.kt
│       ├── TransitionShaderProvider.kt
│       └── KeyframeInterpolator.kt
├── presentation/effects/
│   ├── EffectsViewModel.kt
│   ├── AddTextEffectUseCase.kt
│   ├── UpdateTextStyleUseCase.kt
│   ├── ApplyTransitionUseCase.kt
│   ├── AddKeyframeUseCase.kt
│   └── DeleteEffectUseCase.kt
├── ui/effects/
│   ├── TextEditPanel.kt
│   ├── StickerPanel.kt
│   ├── TransitionPicker.kt
│   ├── KeyframeEditor.kt
│   └── PipEditPanel.kt
└── engine/effects/
    ├── renderer/
    │   ├── EffectsRendererImpl.kt
    │   ├── TextRenderer.kt
    │   ├── StickerRenderer.kt
    │   ├── TransitionRenderer.kt
    │   └── PipRenderer.kt
    ├── shader/
    │   ├── TransitionShaders.kt
    │   └── EffectShaders.kt
    └── texture/
        ├── TextTextureCache.kt
        └── StickerTextureCache.kt
```

---

## 6. 性能与质量要求

### 6.1 性能指标

| 场景 | 目标 | 测试方法 |
|------|------|---------|
| 文字样式修改实时预览 | 修改后 100ms 内刷新 | 修改颜色/描边后计时到画面更新 |
| 贴纸添加 | 从点击到显示 ≤ 300ms | 使用 1080p 贴纸测试 |
| 转场预览帧率 | 转场区间 ≥ 24fps | 两段 1080p 视频 + 转场，统计 FPS |
| 关键帧动画预览 | 含 5 个关键帧的 PiP 元素 ≥ 24fps | Systrace / 自定义 FPS 计数 |
| 文字纹理缓存 | 峰值 ≤ 32MB | Android Studio Profiler 监控 GPU 内存 |
| 画中画内存 | 单路 PiP 解码额外内存 ≤ 80MB | 导出 1080p PiP 时监控 |
| 导出一致性 | 预览与导出像素差异 PSNR ≥ 45dB | 同时间点截图对比 |

### 6.2 常见异常与处理

| 异常场景 | 原因 | 处理策略 |
|---------|------|---------|
| 文字字体加载失败 | 用户选择自定义字体未找到或损坏 | 回退到系统默认字体；记录日志；UI 提示「字体加载失败，已使用默认字体」 |
| 贴纸图片解码失败 | 文件损坏或格式不支持 | 使用默认兜底图像替代；不阻塞时间线；提示用户替换素材 |
| 转场时长超过片段长度 | 用户设置转场过长 | UseCase 校验并自动截断到最大可用时长；UI 显示实际应用时长 |
| OpenGL 纹理创建失败 | GPU 内存不足或尺寸超限 | 降级使用 720p 纹理；仍失败则跳过该 Effect 并上报 |
| 关键帧时间点越界 | 用户拖动关键帧超出片段范围 | 自动吸附到片段边界；不允许越界关键帧入库 |
| Shader 编译失败 | 设备不支持特定 GLSL 语法 | 对该转场/特效回退到基础淡入淡出；记录设备型号与错误日志 |
| 画中画源素材旋转/镜像异常 | 视频元数据带有旋转角 | 在 PiP 解码时读取 MediaMetadataRetriever 旋转角，并在 Shader 中校正 |

### 6.3 可测试性要求

- **Domain 层单元测试**：对 `TextEffect`、`Transform2D`、`Keyframe` 的不可变拷贝、时间区间计算、关键帧插值逻辑编写 JUnit + MockK 测试。
- **UseCase 测试**：使用 Coroutines Test 验证添加/删除/修改 Effect 后 Timeline 状态正确，并触发 Project 自动保存。
- **Shader 兼容性测试**：至少覆盖高通、联发科、麒麟、三星 Exynos 四类 GPU 的代表机型，验证转场 Shader 编译与渲染结果。
- **预览与导出一致性测试**：在相同时间点分别截取预览画面与导出视频帧，计算 PSNR，目标 ≥ 45dB。
- **性能回归测试**：使用 Macrobenchmark 对含 5 段文字 + 3 张贴纸 + 2 个转场的工程进行预览帧率采样，任何提交导致帧率下降超过 10% 必须打回。

---

## 7. 依赖模块

本模块依赖以下 `spec/modules` 下的模块：

| 依赖模块 | 说明 |
|---------|------|
| [timeline](../timeline/README.md) | 多轨道时间轴模型、Clip 抽象、Track 层级、时间计算、撤销重做命令栈。Effect 必须挂载到 Track 上。 |
| [player](../player/README.md) | 实时预览播放器、Surface 管理、帧回调。EffectsRenderer 被 Player 调用以合成当前帧。 |
| [export](../export/README.md) | 渲染管线、编码参数、导出进度。导出时需要复用预览阶段的 Shader 与合成逻辑。 |
| [project](../project/README.md) | 工程模型、元数据、版本管理。Effect 参数需要序列化到工程文件。 |
| [media](../media/README.md) | 素材导入、MediaStore 扫描、缩略图。贴纸、PiP 素材依赖 media 模块解析与加载。 |
| [filters](../filters/README.md) | 滤镜链执行顺序与特效层衔接；部分混合模式（如光晕叠加）可能需要复用 filters 的 Shader 工具。 |
| [uiux](../uiux/README.md) | 页面结构、设计系统、颜色字体、手势交互、组件规范。文字/贴纸/转场/关键帧编辑面板必须遵循。 |

---

## 8. 附录：核心接口伪时序

### 8.1 实时预览一帧

```
Player.onDrawFrame(positionUs)
    ↓
timeline.getActiveEffects(positionUs) → List<Effect>
    ↓
for each effect in sorted(zIndex):
    when (effect):
        TextEffect   → textTextureProvider.getTexture(effect)
        StickerEffect → stickerTextureProvider.getTexture(effect)
        PipEffect     → player.getPipFrameTexture(effect)
        TransitionEffect → shaderProvider.getShader(type)
    ↓
effectsRenderer.renderFrame(inputTexture, positionUs, effects)
    ↓
输出到 SurfaceView
```

### 8.2 导出时一帧

```
Exporter.readNextVideoFrame(positionUs)
    ↓
decoder.getFrame(positionUs)
    ↓
timeline.getActiveEffects(positionUs)
    ↓
effectsRenderer.renderFrame(decodedTexture, positionUs, effects)
    ↓
encoder.writeSampleData(renderedTexture)
```

---

*本规范与项目总体规范保持一致，后续若涉及范围变更，需同步更新 `spec/project-boundary.md` 与本文件。*

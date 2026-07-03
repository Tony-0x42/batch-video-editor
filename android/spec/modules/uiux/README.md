# UI/UX 模块规范

> 定义 VideoEditor-For-Android 商用演进版本的界面层结构、设计系统、交互手势、组件规范与页面流转。

---

## 1. 模块概述

UI/UX 模块位于整体架构的最上层（UI Layer），负责把用户操作转译为 ViewModel / UseCase 调用，并把底层引擎与领域模型的状态以可视化、可交互的形式呈现给用户。本模块不处理剪辑算法、编解码、媒体解析等业务逻辑，而是专注于页面结构、设计系统、通用组件、手势交互、主题与无障碍体验。

该模块的核心目标是：建立统一的视觉语言与组件规范，降低用户学习成本，保证「导入 → 剪辑 → 导出」主流程在常见 Android 设备上拥有流畅的触控响应与一致的交互反馈，解决当前 Demo 界面风格老旧、组件散落、手势缺失、状态展示不一致等问题。

---

## 2. 功能清单

### P0 — 首个 MVP 必须实现

| 功能名称 | 功能描述 | 验收标准 |
| --- | --- | --- |
| 首页与草稿入口 | 展示「开始创作」、最近草稿列表与快捷操作，支持继续编辑、重命名、删除 | 首页冷启动渲染完成 ≤ 2s；草稿列表滑动 60fps；点击后 300ms 内进入编辑器 |
| 素材选择页 | 从相册多选视频/图片/音频，支持按文件夹/时间筛选与缩略图展示 | 支持多选 20+ 素材；缩略图加载不阻塞主线程；选中状态实时反馈 |
| 剪辑主页面布局 | 顶部预览区、中部工具栏、底部多轨道时间轴的「三段式」布局 | 各区域比例可配置；适配 9:16/16:9/1:1/4:3 画布；背景填充可见 |
| 顶部播放器容器 | 承载 OpenGL 预览 Surface，支持缩放、平移、点击播放/暂停 | Surface 生命周期与页面同步；手势不冲突；播放状态图标实时更新 |
| 底部工具栏 | 分割、删除、复制、撤销/重做、比例、背景、导出入口 | 常驻工具不超过 7 个；点击反馈 ≤ 100ms；禁用状态明确 |
| 功能面板（BottomSheet） | 音频、文字、滤镜、美颜四类编辑面板 | 面板展开/收起动画 ≤ 200ms；与预览区不遮挡关键内容；面板内滚动流畅 |
| 设计令牌系统 | 颜色、字体、圆角、阴影、间距、图标风格统一 | 提供 Theme 对象；全局可切换深色/浅色；Compose 与遗留 View 共享 token |
| 通用组件库 | 按钮、图标按钮、滑块、进度条、对话框、Snackbar、加载动画、空状态 | 组件覆盖 MVP 全部页面；支持禁用/加载状态；无障碍标签完整 |
| 手势交互框架 | 时间轴拖拽、缩放、滑动 Seek；播放器双指缩放/平移；面板下滑关闭 | 手势冲突由统一分发器管理；时间轴缩放帧率 ≥ 55fps |
| 权限请求界面 | 相机、麦克风、存储、通知权限的动态申请与拒绝引导 | 按最小权限原则申请；拒绝后提供二次引导；不崩溃 |

### P1 — 重要增值

| 功能名称 | 功能描述 | 验收标准 |
| --- | --- | --- |
| 页面转场动画 | 首页→选择页→编辑器→导出页之间的共享元素与滑动转场 | 转场时长 200-300ms；无闪白/黑屏；支持返回手势 |
| 时间轴高级手势 | 惯性滑动、吸附边缘、长按震动反馈、scrubbing 音效 | 惯性停止位置准确；吸附误差 ≤ 20ms；震动即时 |
| 组件库扩展 | 分段选择器、颜色选择器、字体选择器、波形预览组件、关键帧曲线编辑器 | 可复用；支持配置化；与 ViewModel 状态绑定 |
| 新手引导与空状态 | 首次进入提示、空时间轴引导、操作成功反馈 | 引导不强制阻断；可关闭；记录用户是否已阅 |
| 无障碍支持 | TalkBack 焦点顺序、语义标签、图标按钮描述 | 核心流程可通过 TalkBack 完成；通过 Accessibility Scanner |
| 动态取色/Material You | 基于系统壁纸生成主题色 | Android 12+ 可用；不影响低版本 |

### P2 — 长期规划

| 功能名称 | 功能描述 | 验收标准 |
| --- | --- | --- |
| 平板/折叠屏适配 | 双栏布局、拖放、键盘/手写笔支持 | 大屏利用率 ≥ 70%；不拉伸变形 |
| 多窗口与分屏 | 支持 Android 自由窗口与分屏模式 | 生命周期正确；Surface 不重建 |
| 高级交互动画 | Lottie/Rive 预览、贴纸拖拽旋转缩放关键帧可视化 | 动画帧率 ≥ 30fps；内存可控 |
| 云端账号 UI | 登录、云草稿、素材商店入口预留 | 仅 UI 与路由；业务接口预留 |
| 自定义转场预览 | 在播放器内实时预览转场效果 | 与 effects 模块接口对接 |

---

## 3. 数据模型与接口

### 3.1 设计令牌

```kotlin
/**
 * 应用级设计令牌，所有 UI 组件必须引用此对象而非硬编码值。
 */
data class AppDesignTokens(
    val colors: ColorScheme,
    val typography: AppTypography,
    val shapes: AppShapes,
    val spacing: SpacingScale,
    val durations: MotionDurations,
    val elevation: ElevationScale
)

data class ColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val background: Color,
    val error: Color,
    val divider: Color,
    val playerBackground: Color
)

data class AppTypography(
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val body: TextStyle,
    val caption: TextStyle,
    val button: TextStyle
)
```

### 3.2 页面 UI 状态

```kotlin
/**
 * 剪辑主页面完整 UI 状态，由 EditorViewModel 聚合后下发。
 */
data class EditorUiState(
    val playerState: PlayerViewportState,
    val timelineState: TimelineUiState,
    val toolbarState: ToolbarState,
    val panelState: PanelState,
    val isLoading: Boolean,
    val errorMessage: String? = null
)

data class PlayerViewportState(
    val isPlaying: Boolean,
    val currentPositionUs: Long,
    val totalDurationUs: Long,
    val canvasAspectRatio: Float,
    val fillMode: CanvasFillMode,
    val isSurfaceReady: Boolean
)

data class TimelineUiState(
    val visibleStartUs: Long,
    val visibleEndUs: Long,
    val selectedClipId: String?,
    val tracks: List<TrackUiModel>,
    val cursorPositionUs: Long,
    val scaleFactor: Float
)

sealed class PanelState {
    object Closed : PanelState()
    data class AudioPanel(val selectedTrackId: String?) : PanelState()
    data class TextPanel(val selectedEffectId: String?) : PanelState()
    data class FilterPanel(val selectedClipId: String?) : PanelState()
    data class BeautyPanel(val selectedClipId: String?) : PanelState()
    data class ExportPanel(val settings: ExportSettings) : PanelState()
}
```

### 3.3 核心接口

```kotlin
/**
 * 主题提供者，供 Activity / Compose 在入口处获取当前设计令牌。
 */
interface UiThemeProvider {
    val designTokens: StateFlow<AppDesignTokens>
    fun setDarkMode(enabled: Boolean)
    fun setAccentColor(color: Color)
}

/**
 * 播放器视口控制器，由 UI 层持有并转交用户手势给 player 模块。
 */
interface PlayerViewportController {
    fun attachSurface(surface: Surface)
    fun detachSurface()
    fun onScale(scaleFactor: Float, focusX: Float, focusY: Float)
    fun onTranslate(deltaX: Float, deltaY: Float)
    fun playOrPause()
}

/**
 * 面板调度器，负责控制底部功能面板的展开与关闭。
 */
interface PanelController {
    val currentPanel: StateFlow<PanelState>
    fun open(panel: PanelState)
    fun close()
    fun toggle(panel: PanelState)
}

/**
 * 页面导航路由，封装 Jetpack Navigation 以便单元测试与模块解耦。
 */
interface EditorRouter {
    fun navigateToMediaPicker()
    fun navigateToExport(settings: ExportSettings)
    fun navigateBack()
}
```

### 3.4 输入输出

- **输入**：ViewModel / UseCase 提供的 `StateFlow` / `LiveData`；用户触摸、按键、系统配置变化；权限申请结果；引擎事件（Surface 创建/销毁、播放进度）。
- **输出**：用户意图事件（`UiEvent` / `UserAction`）；页面导航指令；权限请求；Toast / Snackbar / Dialog 等反馈。
- **状态管理**：UI 层仅做「状态呈现」与「事件转发」，禁止在 Activity / Fragment / Compose 中直接修改领域模型。所有状态变更通过调用 ViewModel 方法完成。

---

## 4. 交互与流程

### 4.1 关键用户操作流程

**核心创作流程**

1. 打开 App → 首页显示「开始创作」与草稿列表。
2. 点击「开始创作」→ 跳转素材选择页，用户多选视频/图片/音频。
3. 选择完成 → 进入剪辑主页，预览区加载首帧，时间轴初始化主视频轨。
4. 用户在时间轴上选中片段 → 工具栏高亮可用操作。
5. 点击「分割」→ 时间轴即时刷新；点击「删除」→ 片段移除并自动闭合空隙。
6. 打开音频/文字/滤镜面板 → 调整参数 → 预览区实时反馈。
7. 点击「导出」→ 打开导出设置面板 → 选择分辨率/帧率/码率/水印 → 确认后跳转导出进度页。
8. 导出完成 → 提示保存成功并返回首页或草稿。

### 4.2 模块内部数据流

```text
用户手势（触摸/按键）
    ↓
UI 组件（Compose / View）封装为 UiEvent
    ↓
ViewModel / UseCase 校验并生成 Command 或调用 UseCase
    ↓
Domain 模型更新（timeline / project / audio 等）
    ↓
Engine（player / renderer）刷新渲染
    ↓
StateFlow 推送新状态
    ↓
UI 组件重组 / invalidate 呈现最新界面
```

### 4.3 伪时序：时间轴双指缩放

```text
用户双指捏合
  → TimelineGestureDetector.onScale()
  → 计算新的 scaleFactor 并限制在 [MIN_SCALE, MAX_SCALE]
  → EditorViewModel.onTimelineScaleChanged(scaleFactor)
  → TimelineUseCase 更新可见时间窗口 visibleStartUs / visibleEndUs
  → 通知 Player 保持当前 cursor 居中（可选）
  → StateFlow<TimelineUiState> 更新
  → Compose Timeline 重组重绘轨道与 Clip
```

---

## 5. 实现要点

### 5.1 技术方案选择

- **UI 框架**：新页面与复杂交互统一使用 Jetpack Compose；OpenGL 预览、Camera 预览等需要 `Surface` 的页面可保留 `SurfaceView` / `TextureView`，并通过 `AndroidView` 嵌入 Compose。
- **导航**：Jetpack Navigation + Compose Navigation，统一路由表，避免深层 Activity 跳转。
- **状态传递**：ViewModel 使用 `StateFlow` 聚合 UI 状态，Compose 通过 `collectAsStateWithLifecycle()` 订阅，自动处理生命周期。
- **主题系统**：基于 Material Design 3 构建 `MaterialTheme`，将颜色、字体、形状抽取到 `AppDesignTokens`，并通过 `CompositionLocalProvider` 下发。
- **手势框架**：对时间轴使用自定义 `GestureDetector` / `PointerInput` 处理单指拖拽 Seek、双指缩放、长按选择；对播放器使用 `Transformable` / 自定义 `onTouchEvent` 处理双指缩放/平移。
- **依赖注入**：Hilt 注入 ViewModel、Router、PanelController、PermissionRequester。

### 5.2 需要特别注意的难点

- **Surface 生命周期与 Compose 重组**：GL 预览 `SurfaceView` 的创建/销毁必须精确对应页面 `RESUMED` / `PAUSED`，避免在配置变更时重建导致闪黑。建议使用 `DisposableEffect` 或 `LifecycleEventEffect` 管理。
- **高频手势不阻塞渲染线程**：时间轴缩放、Seek 操作会产生大量事件，必须在 UI 层做节流（throttle）或只在手势结束时提交最终值，避免频繁触发 `Player.seekTo()` 导致卡顿。
- **底部面板与预览区布局冲突**：面板展开时不可遮挡播放/暂停按钮与当前时间码；需使用 `WindowInsets` 计算安全区域，并保证输入法弹起时输入框可见。
- **深色模式与主题切换**：切换主题后不应重启 Activity，所有组件必须基于动态 `ColorScheme` 重组。
- **遗留 View 与 Compose 混用**：旧 Demo 中大量基于 Activity + View 的页面需通过 `ComposeView` 或 `AndroidView` 做隔离，禁止在旧 View 中直接操作新 Domain 模型。

### 5.3 与现有代码的衔接建议

- 在 `app/src/main/java/com/example/cj/videoeditor/ui/` 下新建 `home/`、`editor/`、`export/`、`common/` 包，将旧 Activity 逐步迁移为对应的 `Screen()` 可组合函数 + `ViewModel`。
- 旧滤镜选择、美颜设置等 UI 逻辑抽取为 `FilterPanel()`、`BeautyPanel()` 等可组合函数，通过 `PanelController` 统一调度。
- 将原本写在 Activity 中的权限与媒体选择回调，迁移到 `ActivityResultContracts` + `PermissionRequester` 接口实现中。
- 保留 `Engine` 层的 `Surface` 回调接口，UI 层仅做 Surface 容器，不处理 OpenGL 上下文。

---

## 6. 性能与质量要求

### 6.1 性能指标

| 场景 | 目标 | 测试方法 |
| --- | --- | --- |
| 首页冷启动 | ≤ 2s | 从 Launcher 点击到首页可交互 |
| 页面跳转 | ≤ 150ms | 点击到目标页面首帧渲染 |
| 时间轴手势 | ≥ 55fps | 滑动/缩放时 Systrace 检查掉帧 |
| 面板展开/收起 | ≤ 200ms | 动画时长，无跳帧 |
| Compose 重组 | 无过度重组 | Layout Inspector / Recomposition Counter |
| 内存 | 编辑器页面 ≤ 150MB（不含引擎） | Profiler |
| 崩溃率 | 发布版本 UI 崩溃 < 0.3% | Crashlytics |

### 6.2 常见异常与处理

| 异常场景 | 处理策略 |
| --- | --- |
| 权限被拒绝 | 显示引导弹窗说明用途，提供「去设置」入口；核心功能不可用时禁用对应按钮 |
| Surface 尚未就绪 | Player 操作排队或忽略，待 `surfaceCreated` 后再执行；UI 显示加载指示器 |
| 配置变更（旋转/分屏） | 使用 `ViewModel` + `rememberSaveable` 保存 UI 状态；Surface 按需重建 |
| 低内存 / OOM 警告 | 降低缩略图分辨率、回收不可见面板资源、提示用户关闭后台应用 |
| 手势冲突 | 统一手势分发器，按区域（预览区/时间轴/面板）设定优先级，避免误触发 |
| 主题切换失败 | 兜底使用默认浅色主题，记录日志，不崩溃 |

---

## 7. 依赖模块

本模块处于 UI Layer，依赖其他模块提供的 UseCase / Repository / 引擎接口和领域模型，但不对这些模块的实现做假设。

| 依赖模块 | 依赖说明 |
| --- | --- |
| [project](../project/README.md) | 工程元数据、画布配置、导出参数；首页草稿列表与导出设置 UI 依赖其模型 |
| [draft](../draft/README.md) | 草稿箱列表、自动保存状态；首页「继续编辑」与草稿管理 UI 依赖其状态 |
| [media](../media/README.md) | 相册扫描、缩略图、媒体格式信息；素材选择页与媒体项 UI 组件依赖其模型 |
| [timeline](../timeline/README.md) | 多轨道时间轴模型、Clip 抽象、轨道层级；时间轴编辑器 UI 依赖其数据结构 |
| [editor](../editor/README.md) | 分割、删除、复制、排序、撤销重做命令；工具栏与长按菜单依赖其 UseCase |
| [audio](../audio/README.md) | BGM、音量、淡入淡出、录音；音频面板 UI 依赖其状态与参数 |
| [filters](../filters/README.md) | 预设滤镜、滤镜强度、美颜参数；滤镜/美颜面板依赖其模型 |
| [effects](../effects/README.md) | 文字、字幕、贴纸、转场、关键帧；文字/特效面板依赖其模型 |
| [player](../player/README.md) | 实时预览、Seek、Surface 管理；播放器容器与播放控制 UI 依赖其接口 |
| [export](../export/README.md) | 导出设置、导出进度、输出格式；导出页与进度 UI 依赖其状态 |
| [recorder](../recorder/README.md) | 相机预览、分段录制；拍摄/录制入口 UI 依赖其启动与状态回调 |

---

> 本规范由 UI/UX 模块负责人维护。后续新增页面、组件或主题变量时，必须同步更新本文件与 [../../project-guidelines.md](../../project-guidelines.md) 中的相关约定。

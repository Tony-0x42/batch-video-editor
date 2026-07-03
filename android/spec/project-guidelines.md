# 项目规范

> 编码规范、目录结构、命名约定、代码审查标准与开发流程。所有贡献者必须遵守。

---

## 1. 代码风格

### 1.1 语言选择

- **新模块优先使用 Kotlin**
- 与现有 Java 模块交互时，通过接口和 Repository 解耦
- 禁止在 Kotlin 中混用 Java 式 null 检查，优先使用 `?`、`?:`、`let`、`run` 等安全调用

### 1.2 代码格式

- 使用 Android Studio 默认代码格式化（Ctrl+Alt+L / Cmd+Option+L）
- 行宽 120 字符
- 缩进 4 个空格
- 类/方法前保留适当空行

### 1.3 命名约定

| 类型 | 命名方式 | 示例 |
|------|----------|------|
| 类 | PascalCase | `TimelineViewModel`, `VideoClip` |
| 接口 | PascalCase，可加 I 前缀 | `ITimelineRenderer` / `TimelineRenderer` |
| 方法/变量 | camelCase | `splitClipAt`, `currentPositionUs` |
| 常量 | SCREAMING_SNAKE_CASE | `MAX_EXPORT_RESOLUTION` |
| XML 资源 | snake_case | `activity_editor.xml`, `ic_split` |
| Compose 函数 | PascalCase | `TimelineTrack()` |
| 包名 | 全小写 | `com.example.cj.videoeditor.timeline` |

### 1.4 注释规范

- 公共 API 必须写 KDoc/JavaDoc
- 复杂算法必须附注释说明核心逻辑
- 禁止使用无意义注释（如 `// 初始化`）
- TODO 必须带作者和日期：`// TODO(username): 2025-06-28 说明原因`

---

## 2. 目录结构

### 2.1 应用模块结构

```
app/src/main/java/com/example/cj/videoeditor/
├── App.kt                          # Application 类
├── di/                             # Hilt 依赖注入模块
├── ui/                             # UI 页面与组件（Compose + View）
│   ├── main/
│   ├── editor/
│   ├── export/
│   └── common/
├── presentation/                   # ViewModel + UseCase
├── domain/                         # 领域模型与接口
│   ├── model/
│   ├── repository/
│   └── engine/
├── data/                           # 数据实现
│   ├── local/
│   │   ├── db/                     # Room
│   │   └── prefs/                  # DataStore
│   ├── media/
│   └── repository/
├── engine/                         # 渲染/录制/导出引擎
│   ├── player/
│   ├── renderer/
│   ├── recorder/
│   ├── exporter/
│   └── audio/
└── utils/                          # 通用工具
```

### 2.2 资源目录结构

```
app/src/main/res/
├── layout/                         # 遗留 View 布局
├── drawable/                       # 矢量图与形状
├── mipmap-xxxhdpi/                 # 应用图标
├── values/
│   ├── colors.xml
│   ├── strings.xml
│   ├── themes.xml
│   └── styles.xml
├── raw/                            # 本地音效、LUT 等
└── font/                           # 字体资源
```

### 2.3 模块规范目录

```
spec/
├── README.md
├── technical-spec.md
├── project-boundary.md
├── project-guidelines.md
└── modules/
    └── {module}/
        └── README.md              # 模块规范
```

---

## 3. 架构原则

### 3.1 必须遵循

1. **单一职责**：一个类只负责一件事
2. **依赖倒置**：Domain 层定义接口，Data/Engine 层实现
3. **接口隔离**：引擎能力通过细粒度接口暴露
4. **MVVM**：所有页面必须有 ViewModel，禁止在 Activity/Fragment 中直接操作数据
5. **响应式**：使用 Kotlin Flow 传递状态与事件

### 3.2 禁止行为

- 禁止在 UI 层直接调用 Engine/Native 方法
- 禁止在 Activity/Fragment 中写业务逻辑
- 禁止内存泄漏：所有订阅必须在生命周期结束时清理
- 禁止在主线程执行耗时操作（解码、编码、文件 IO）

---

## 4. 异步与线程

| 场景 | 推荐方式 |
|------|----------|
| 轻量后台计算 | `viewModelScope.launch { ... }` |
| 数据库操作 | Room 配合 Flow |
| 文件 IO | `Dispatchers.IO` |
| 渲染线程 | OpenGL 专用 GL 线程 |
| 编码线程 | MediaCodec 回调线程或专用 HandlerThread |
| 定时任务 | `LifecycleScope` + `delay` |

---

## 5. 错误处理

### 5.1 通用原则

- 所有可能失败的操作必须返回 `Result<T>` 或抛出自定义异常
- 用户可见错误必须转化为 UI 状态（Snackbar/Toast/Dialog）
- 日志使用统一 Tag：`VE:${ClassName}`

### 5.2 自定义异常

```kotlin
sealed class VideoEditorException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class DecodeException(message: String, cause: Throwable? = null) : VideoEditorException(message, cause)
    class EncodeException(message: String, cause: Throwable? = null) : VideoEditorException(message, cause)
    class RenderException(message: String, cause: Throwable? = null) : VideoEditorException(message, cause)
    class StorageException(message: String, cause: Throwable? = null) : VideoEditorException(message, cause)
}
```

---

## 6. 代码审查标准

### 6.1 提交前自检

- [ ] 代码能编译通过
- [ ] 无新引入的 Lint 警告
- [ ] 相关单元测试通过
- [ ] 不泄露敏感信息（API Key、路径等）
- [ ] 资源文件已压缩（图片、音频）

### 6.2 Review 关注点

- 是否符合架构分层
- 是否存在内存泄漏风险
- 是否处理了空值与异常
- 是否遵循命名规范
- 是否有重复代码可抽取

---

## 7. 版本管理

### 7.1 分支策略

- `main`：稳定发布分支
- `develop`：日常开发分支
- `feature/{module-name}`：功能分支
- `bugfix/{issue-id}`：修复分支

### 7.2 提交信息

```
<type>(<scope>): <subject>

<body>

<footer>
```

示例：
```
feat(timeline): 实现视频片段分割命令

- 添加 SplitClipCommand
- 集成撤销重做栈
- 更新 TimelineRepository

Closes #123
```

---

## 8. 性能与质量

### 8.1 必须监控

- 内存泄漏：使用 LeakCanary（debug）
- 过度绘制：使用 GPU 过度绘制调试
- 帧率：关键页面开启 FPS 监控
- ANR：避免主线程阻塞

### 8.2 包体积

- 图片优先使用 WebP/Vector
- 音频资源使用 OGG/MP3 压缩
- 避免引入不必要的第三方库
- Native 库仅保留必要 ABI（arm64-v8a、armeabi-v7a）

---

## 9. 安全

- 所有网络请求必须 HTTPS
- 本地密钥不得硬编码，使用 NDK 或 Keystore
- 导出的媒体文件保存到应用外部私有目录或 MediaStore
- 权限申请遵循最小权限原则

---

## 10. 文档要求

- 每个公共类/方法必须有 KDoc
- 每个模块必须有 README 说明职责和接口
- 复杂业务流程必须补充时序图或流程图
- 修改本规范时，必须同步更新相关模块规范

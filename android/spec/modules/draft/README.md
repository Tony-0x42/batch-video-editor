# Draft 模块规范

> 本文档定义 VideoEditor-For-Android 草稿箱（Draft）模块的产品、技术、数据与交互规范。草稿模块负责工程在非导出状态下的生命周期管理：自动保存、草稿列表、恢复续编、命名与清理。

---

## 1. 模块概述

Draft（草稿箱）模块是连接用户创作意图与 Project 持久化层的中间枢纽。它的核心职责是：在用户剪辑过程中持续、可靠地保存工程状态；在 App 被系统回收、进程崩溃或用户主动退出后，能够无损恢复至上一次编辑位置；同时提供草稿列表页，支持用户浏览、继续编辑、重命名、删除及批量清理草稿。

在整体架构中，Draft 模块位于 **Data Layer 与 Presentation Layer 之间**，向上为首页/草稿箱页、剪辑页 ViewModel 提供「自动保存进度」「草稿元数据」「恢复状态」等状态流；向下依赖 Project 模块的领域模型与 Repository 接口完成工程序列化，依赖 Media 模块生成封面缩略图。Draft 模块不直接参与时间线计算、渲染、编码等业务，仅负责工程对象的**保存调度、版本快照与生命周期管理**。

该模块需要解决的关键问题包括：剪辑过程中高频修改如何避免频繁全量写入造成卡顿；崩溃后如何恢复到最近一次一致状态；多草稿列表如何快速加载缩略图而不阻塞主线程；以及本地存储空间不足、文件损坏等异常场景下的降级与提示。

---

## 2. 功能清单

### P0 — 首个 MVP 必须实现

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 自动保存 | 用户在剪辑页进行操作（分割、删除、拖拽、添加滤镜/字幕/BGM 等）后，系统在后台自动将当前工程写入草稿持久化存储。 | 1. 每次有效操作后 2s 内触发自动保存；2. 切换到后台、退出剪辑页、按 Home 键前必须完成一次保存；3. 保存过程不阻塞主线程，UI 帧率不低于 24fps。 |
| 草稿创建 | 用户点击「开始创作」并导入素材后，生成新的草稿工程；用户从录制页进入编辑时，同样生成新草稿。 | 1. 草稿 ID 全局唯一且稳定；2. 默认名称为「草稿 1」「草稿 2」递增，或按日期生成；3. 创建后立即写入数据库并返回。 |
| 草稿列表 | 首页展示所有未导出草稿的卡片列表，包含封面缩略图、名称、时长、最后编辑时间、分辨率比例。 | 1. 首屏加载 ≤ 800ms；2. 列表支持下拉刷新；3. 空态展示「开始创作」引导。 |
| 继续编辑 | 用户点击草稿卡片后，恢复工程到剪辑主页面，时间线、播放器、已添加的素材与效果均与上次保存一致。 | 1. 恢复后播放头位置与保存时一致（误差 ≤ 100ms）；2. 撤销/重做栈可清空或保留最近一次快照；3. 缺失素材提示用户重新链接。 |
| 删除草稿 | 用户在草稿列表或设置中删除草稿，释放工程文件与缩略图缓存。 | 1. 删除前弹出二次确认；2. 删除后数据库记录、工程目录、封面缩略图一并清理；3. 支持撤销删除（Snackbar 3s 内撤销）。 |
| 重命名草稿 | 用户可修改草稿名称，避免默认名称难以辨识。 | 1. 名称长度 1-40 字符；2. 支持中文、英文、数字、空格；3. 重命名即时同步到列表与数据库。 |

### P1 — 重要增值

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 手动保存与快照 | 提供「保存草稿」按钮或下拉手势，允许用户手动触发保存并生成显式快照。 | 1. 手动保存完成后给出轻提示；2. 快照可记录备注（可选）；3. 崩溃恢复时优先使用最近快照。 |
| 草稿封面自定义 | 用户可从时间线中任选一帧作为草稿封面，替代默认首帧缩略图。 | 1. 选帧后 1s 内生成封面并更新列表；2. 封面持久化到私有目录；3. 不同草稿封面互不覆盖。 |
| 草稿排序与筛选 | 列表支持按「最近编辑」「创建时间」「名称」排序，支持搜索草稿名称。 | 1. 排序切换响应 ≤ 200ms；2. 搜索支持实时过滤；3. 结果为空时给出对应提示。 |
| 草稿占用空间显示 | 在草稿列表或设置中显示每个草稿及其缓存占用的磁盘空间。 | 1. 空间计算在 IO 线程异步完成；2. 显示精度到 0.1MB；3. 总占用超过阈值时提示清理。 |
| 崩溃恢复提示 | App 启动时检测到上一次异常退出，提示用户「是否恢复未保存的草稿」。 | 1. 检测准确，不误报正常退出；2. 用户选择「恢复」后进入剪辑页；3. 用户选择「放弃」后清理临时文件。 |
| 草稿导出联动 | 草稿导出成功后，可选择「保留草稿」或「自动删除草稿」；导出失败时保留草稿以便重新导出。 | 1. 导出成功弹窗提供二选一；2. 默认策略可在设置中配置；3. 删除草稿前确认是否已保存到相册。 |

### P2 — 长期规划

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 草稿版本历史 | 为同一草稿保留多个历史版本，允许用户回滚到任意历史版本。 | 1. 每次显式手动保存生成一个版本；2. 版本列表展示时间与占用空间；3. 回滚后撤销栈重置。 |
| 草稿云端备份预留 | 本地草稿元数据中预留云同步字段，后续可扩展为账号绑定后上传工程文件。 | 1. 数据模型包含 `syncState` / `cloudId` 字段；2. 未登录时不触发同步逻辑；3. 接口设计向后兼容。 |
| 草稿模板化 | 用户可将当前草稿保存为「模板」，后续基于模板快速创建新草稿。 | 1. 模板仅保存轨道结构、滤镜、字幕样式，不绑定具体素材 URI；2. 创建新草稿时提示替换素材；3. 模板单独管理列表。 |
| 智能清理策略 | 当磁盘空间不足或草稿长期未编辑时，自动归档或提示清理旧草稿。 | 1. 30 天未编辑草稿置顶提示；2. 空间不足时优先提示大体积草稿；3. 不自动删除用户未确认的草稿。 |

---

## 3. 数据模型与接口

### 3.1 核心领域模型

```kotlin
/**
 * 草稿领域模型，面向 UI 与 UseCase 层暴露。
 */
data class Draft(
    val id: String,
    val projectId: String,              // 关联的工程 ID
    val name: String,
    val coverPath: String?,             // 封面缩略图本地路径
    val canvasRatio: CanvasRatio,       // 9:16 / 16:9 / 1:1 / 4:3
    val durationUs: Long,               // 草稿时长（微秒）
    val createTime: Long,
    val updateTime: Long,
    val autoSaveState: AutoSaveState,   // 自动保存状态
    val isRecovered: Boolean = false,   // 是否来自崩溃恢复
    val storageSizeBytes: Long = 0L     // 占用字节数
)

enum class AutoSaveState {
    SAVED,      // 已保存
    SAVING,     // 保存中
    PENDING,    // 有待保存的变更
    FAILED      // 上次保存失败
}

enum class CanvasRatio { RATIO_9_16, RATIO_16_9, RATIO_1_1, RATIO_4_3 }

/**
 * 草稿持久化实体，用于 Room 存储。
 */
@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val coverPath: String?,
    val canvasRatio: String,
    val durationUs: Long,
    val createTime: Long,
    val updateTime: Long,
    val autoSaveState: String,
    val isRecovered: Boolean,
    val storageSizeBytes: Long,
    val projectSnapshotPath: String   // 工程 JSON / 二进制快照路径
)
```

### 3.2 Repository 接口

```kotlin
/**
 * 草稿仓库接口，由 Data Layer 实现。
 */
interface DraftRepository {
    /**
     * 观察草稿列表变化。
     */
    fun observeDrafts(sortBy: DraftSortBy = DraftSortBy.UPDATE_TIME): Flow<List<Draft>>

    /**
     * 创建新草稿，返回草稿 ID。
     */
    suspend fun createDraft(project: VideoProject, name: String? = null): Result<String>

    /**
     * 保存当前工程到草稿。调用方负责传入完整的 Project 领域对象。
     */
    suspend fun saveDraft(draftId: String, project: VideoProject): Result<Unit>

    /**
     * 根据草稿 ID 恢复工程。
     */
    suspend fun loadProject(draftId: String): Result<VideoProject>

    /**
     * 重命名草稿。
     */
    suspend fun renameDraft(draftId: String, newName: String): Result<Unit>

    /**
     * 删除草稿并清理资源。
     * @param permanent 是否立即永久删除；false 时放入回收区，保留 3 天可撤销。
     */
    suspend fun deleteDraft(draftId: String, permanent: Boolean = true): Result<Unit>

    /**
     * 撤销最近一次删除（仅对非永久删除有效）。
     */
    suspend fun undoDeleteDraft(draftId: String): Result<Unit>

    /**
     * 更新草稿封面。
     */
    suspend fun updateCover(draftId: String, frameTimeUs: Long): Result<Unit>

    /**
     * 检查是否存在可恢复的崩溃草稿。
     */
    suspend fun checkCrashRecovery(): Result<Draft?>

    /**
     * 放弃崩溃恢复草稿并清理。
     */
    suspend fun discardCrashRecovery(draftId: String): Result<Unit>
}

enum class DraftSortBy { UPDATE_TIME, CREATE_TIME, NAME }
```

### 3.3 UseCase 层接口

```kotlin
/**
 * 自动保存调度器，运行于剪辑页 ViewModel 生命周期内。
 */
interface AutoSaveCoordinator {
    /**
     * 标记当前工程有变更，触发防抖保存。
     */
    fun markDirty()

    /**
     * 立即执行保存，通常用于切后台、退出页面前。
     */
    suspend fun saveNow(): Result<Unit>

    /**
     * 释放资源，停止保存调度。
     */
    fun release()
}

/**
 * 草稿箱首页业务用例。
 */
class DraftListUseCase(private val repository: DraftRepository) {
    fun observeDrafts(sortBy: DraftSortBy): Flow<List<Draft>> = repository.observeDrafts(sortBy)
    suspend fun deleteDraft(draftId: String) = repository.deleteDraft(draftId, permanent = false)
    suspend fun renameDraft(draftId: String, newName: String) = repository.renameDraft(draftId, newName)
}
```

### 3.4 输入输出

| 输入 | 来源 | 说明 |
|------|------|------|
| `VideoProject` | Project 模块 | 完整工程领域对象，含时间线、画布、导出设置 |
| 用户操作事件 | UI 层 | 创建、重命名、删除、选封面、排序、搜索 |
| 生命周期事件 | Activity/Fragment | `onPause`、`onStop`、`onDestroy` 触发强制保存 |
| 崩溃标记 | Application/Activity | 进程启动时检查异常退出标记 |

| 输出 | 去向 | 说明 |
|------|------|------|
| `Flow<List<Draft>>` | UI 层 | 草稿列表状态流 |
| `Result<Unit>` | UI 层 | 操作结果，成功/失败及异常类型 |
| 工程快照文件 | 应用私有目录 | JSON / protobuf 序列化工程数据 |
| 封面缩略图 | 私有缓存目录 | JPEG/WebP 格式，宽度 480px |

---

## 4. 交互与流程

### 4.1 关键用户操作流程

#### 流程 A：创建草稿并进入编辑

```
用户点击「开始创作」
    ↓
选择素材并完成导入
    ↓
Project 模块生成 VideoProject
    ↓
DraftRepository.createDraft(project)
    ↓
生成唯一 draftId / projectId
    ↓
写入 DraftEntity 与工程快照
    ↓
生成默认封面缩略图
    ↓
跳转 EditorActivity，携带 draftId
    ↓
加载工程并初始化 AutoSaveCoordinator
```

#### 流程 B：自动保存

```
用户执行剪辑操作
    ↓
EditorViewModel 收到 Timeline/Effect 变更
    ↓
调用 AutoSaveCoordinator.markDirty()
    ↓
防抖计时器（默认 2s）启动
    ↓
计时器触发 → 状态置为 SAVING
    ↓
协程在 Dispatchers.IO 中序列化 VideoProject
    ↓
写入工程快照文件 + 更新 DraftEntity.updateTime
    ↓
生成/更新封面缩略图（仅在必要时，如首帧变化）
    ↓
状态置为 SAVED，UI 显示「已保存」
```

#### 流程 C：继续编辑草稿

```
用户在首页点击草稿卡片
    ↓
DraftRepository.loadProject(draftId)
    ↓
校验工程快照完整性
    ↓
缺失素材检测（对比 MediaStore / 原始 URI 是否存在）
    ↓
若存在缺失 → 跳转素材修复页，提示重新选择
    ↓
若完整 → 恢复 VideoProject 并跳转 EditorActivity
    ↓
播放头恢复到上次位置
```

#### 流程 D：崩溃恢复

```
App 冷启动
    ↓
Application 读取崩溃标记 isLastSessionCrashed
    ↓
DraftRepository.checkCrashRecovery()
    ↓
若存在 recovery draft → 首页弹窗提示「是否恢复未保存的草稿？」
    ↓
用户选择「恢复」→ 进入 EditorActivity 并加载 recovery draft
    ↓
用户选择「放弃」→ DraftRepository.discardCrashRecovery(draftId)
```

### 4.2 模块内部数据流

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                            │
│   DraftListScreen / EditorActivity / CrashRecoveryDialog      │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                  Presentation / UseCase Layer               │
│   DraftListViewModel   AutoSaveUseCase   RecoveryUseCase      │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                       Domain Layer                          │
│   DraftRepository (interface)   Draft   AutoSaveCoordinator   │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                         Data Layer                          │
│   DraftRepositoryImpl   Room (drafts)   File Snapshot Store   │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                       Dependency Modules                    │
│   Project Module (VideoProject)   Media Module (thumbnail)    │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. 实现要点

### 5.1 技术方案选择

1. **持久化格式**：工程快照优先采用 **JSON + Zip** 或 **Protocol Buffers** 序列化。JSON 便于调试与版本迁移；Protobuf 在草稿数量多、工程体积大时读取更快。首个 MVP 建议使用 JSON + Zip，后续可无缝替换序列化器。
2. **数据库选型**：草稿元数据使用 **Room** 存储，字段覆盖列表展示所需全部信息，避免加载列表时反序列化完整工程文件。
3. **自动保存策略**：采用**防抖写入**（debounce）+ **强制保存点**（app 切后台、退出页面前）。防抖间隔默认 2s，可通过 RemoteConfig 或设置调整；保存操作在 `Dispatchers.IO` 执行，使用 Mutex 串行化避免并发写入导致文件损坏。
4. **封面生成**：默认使用工程首帧，通过 Media 模块的缩略图能力异步生成 480×720 WebP；用户自定义封面时，从 Player 当前帧截图并裁剪为与画布比例一致的尺寸。
5. **崩溃检测**：在 `Application.onCreate` 中读取一个持久化标记 `app_exit_normally`，若上次未正常置为 true，则认为上次异常退出。进入首页后调用 `checkCrashRecovery()`。

### 5.2 需要特别注意的难点

1. **高频写入与性能平衡**：剪辑过程中每秒可能产生多次操作（如拖拽 scrubbing）。必须在 ViewModel 层对 `markDirty()` 做防抖，避免 1s 内多次全量序列化。同时可引入增量快照策略（P1），但 MVP 阶段建议先保证全量快照正确性。
2. **工程快照一致性**：写入新快照时应先写入临时文件，完成后再原子重命名为正式文件，防止写入过程中断导致工程损坏。
3. **素材缺失恢复**：用户删除或移动了相册中的源素材后，工程快照中的 URI 会失效。恢复时必须逐段检测 Clip.uri 可访问性，对缺失素材给出「替换/删除/取消」选项，不能静默跳过导致崩溃。
4. **存储空间管理**：草稿目录应限定在应用私有目录内，便于清理；需要定期扫描草稿占用空间，对超过 2GB 总占用给出清理提示。
5. **多入口创建草稿一致性**：用户可能从「开始创作」「相机录制」「模板创建」三个入口进入编辑，必须统一调用 `DraftRepository.createDraft()`，确保 draftId、projectId、默认名称、封面生成逻辑一致。

### 5.3 与现有代码的衔接建议

- 旧 Demo 中若存在直接以文件路径保存工程或缓存的代码，应迁移到 `DraftRepository` 接口下统一管理，禁止在 Activity 中直接操作工程文件。
- 现有 JNI 音频混音、滤镜链等业务不依赖 Draft 模块；Draft 模块只保存这些效果的参数配置（如 filterId、intensity），不保存运行时状态。
- 若旧代码中存在 `Project` 类，需评估是否与新的 `VideoProject` 领域模型兼容。建议在新 `VideoProject` 中提供旧工程迁移适配器，将旧 JSON 迁移到新的 Draft + Project 结构。
- 临时导出文件（未最终保存到相册）不应放入草稿目录，避免 Draft 模块误判为草稿。

---

## 6. 性能与质量要求

### 6.1 本模块相关性能指标

| 场景 | 目标 | 测试方法 |
|------|------|---------|
| 草稿列表首屏加载 | ≤ 800ms | 50 个草稿样本，从数据库读取 + 封面缩略图加载 |
| 自动保存完成时间 | ≤ 1.5s（工程 5min 1080p） | 模拟包含 20 个片段的工程，测量序列化+写入时间 |
| 自动保存期间 UI 帧率 | ≥ 24fps | Systrace 检测主线程阻塞 |
| 草稿恢复时间 | ≤ 1.5s | 从点击卡片到 EditorActivity 完成加载 |
| 删除草稿 | ≤ 500ms | 包含 1GB 媒体缓存的草稿删除 |
| 崩溃恢复检测 | ≤ 300ms | Application 启动时同步检测 |

### 6.2 常见异常与处理

| 异常场景 | 原因 | 处理策略 |
|---------|------|---------|
| 保存失败（磁盘不足） | 设备存储空间不足 | 状态置为 FAILED，UI 提示「存储空间不足，请及时清理」；不再尝试自动保存，等待用户释放空间后手动保存。 |
| 工程快照文件损坏 | 写入中断、JSON 不完整 | 尝试读取上一次备份快照；若均损坏，提示用户「草稿已损坏，无法恢复」并允许删除。 |
| 源素材 URI 失效 | 用户在系统相册中删除/移动了原视频 | 恢复时进入素材修复流程，支持批量替换或删除失效片段。 |
| 自动保存并发冲突 | 用户快速连续操作导致多次保存触发 | 使用 Kotlin Mutex 保证同一 draftId 串行写入；新保存请求等待旧保存完成后执行最新一次。 |
| 进程被杀时保存未完成 | 系统 Low Memory Killer 触发 | 在 `onStop`/`onTrimMemory` 中触发一次同步保存（超时 500ms），同时保留未完成的临时文件用于恢复。 |
| 封面生成失败 | 首帧解码异常或文件不存在 | 使用占位图（纯色 + 首字母）替代，不阻塞列表加载。 |

---

## 7. 依赖模块

本模块依赖以下 `spec/modules` 下的模块：

| 依赖模块 | 路径 | 依赖内容 |
|---------|------|---------|
| project | [modules/project](../project) | `VideoProject` 领域模型、工程版本管理、工程序列化接口 |
| media | [modules/media](../media) | 封面缩略图生成、素材 URI 可访问性检测、MediaStore 查询 |
| timeline | [modules/timeline](../timeline) | 时间线结构变更监听、撤销重做状态（仅作为工程数据一部分保存） |
| uiux | [modules/uiux](../uiux) | 草稿列表 UI 组件、空态/加载态/错误态视觉规范 |

---

## 8. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | 2025-06-28 | 初始 Draft 模块规范，覆盖 MVP 自动保存、草稿列表、恢复续编、命名删除等核心能力 |

# project 模块规范

> 本文件定义 VideoEditor-For-Android 中 **project（工程管理）** 模块的职责边界、数据模型、接口契约、交互流程、实现要点与性能质量要求。
>
> 模块定位：负责「一个剪辑工程」的完整生命周期管理，包括领域模型定义、元数据与版本控制、工程文件目录结构、持久化策略、当前工程状态管理，以及向草稿箱（draft）、导出（export）、时间线（timeline）等模块暴露标准化接口。

---

## 1. 模块概述

`project` 模块是剪辑应用的数据中枢之一，位于 **Domain 层（模型与接口）** 与 **Data 层（持久化实现）** 之间。它解决的核心问题是：如何以稳定、可迁移、可自动保存的方式，定义并存储一个剪辑工程的全部静态信息。

具体而言，本模块承担以下职责：

- 定义统一的工程领域模型 `VideoProject`，包含画布配置、时间线引用、导出预设、创建/更新时间、版本号等元数据。
- 管理工程在磁盘上的物理结构：工程元数据数据库记录、工程私有目录、缩略图/封面、时间线 JSON 描述文件等。
- 提供工程版本号与迁移策略，保证后续迭代中新增字段、轨道类型、效果参数能够平滑兼容旧工程。
- 维护「当前打开工程」的运行时状态，并通过响应式流暴露给 UI 层与 UseCase 层。
- 定义工程级生命周期接口：新建、打开、关闭、保存、删除、复制。

与 `draft` 模块的关系：`draft` 负责草稿箱列表、自动保存调度、最近工程恢复等用户-facing 能力；`project` 提供单工程持久化与模型能力，是 `draft` 的底层依赖。与 `timeline`、`media`、`export`、`audio` 等模块的关系：`project` 持有对这些模块数据的引用或快照，但不直接参与其内部算法实现。

---

## 2. 功能清单

### P0 — 首个 MVP 必须实现

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 工程领域模型 | 定义 `VideoProject` 及附属数据类，作为所有剪辑操作的统一上下文 | 模型覆盖 id、名称、创建/更新时间、画布、时间线引用、导出预设、版本号；可在单元测试中独立实例化与序列化 |
| 工程新建 | 根据用户选择的素材或空模板创建新工程 | 调用 `ProjectRepository.createProject(...)` 后返回有效工程 id；磁盘目录与数据库记录同时创建；耗时操作在 IO 线程完成 |
| 工程打开 | 通过工程 id 加载完整工程到内存 | 打开后 `ProjectStateManager.currentProject` 更新；总耗时 ≤ 300ms（不含媒体索引） |
| 工程关闭 | 安全关闭当前工程，释放资源并触发最终保存 | 关闭后当前工程状态为 null；所有订阅者收到 `ProjectState.Closed` 事件 |
| 工程保存 | 将当前工程模型持久化到本地 | 保存成功后数据库 updateTime 刷新；JSON 描述文件与数据库记录一致；保存失败返回 `Result.failure` |
| 工程删除 | 删除工程元数据、私有目录及所有衍生文件 | 删除后数据库记录与磁盘目录均不存在；删除前二次确认由 UI 层处理 |
| 工程元数据管理 | 维护工程名称、封面、画布比例、导出默认参数等元信息 | 元数据修改后 500ms 内触发自动保存；UI 可观察到实时变更 |
| 工程文件目录结构 | 定义每个工程在私有目录下的标准化子目录 | 目录包括 `project.json`、`thumbnails/`、`covers/`、`exports/`、`temp/`；目录隔离避免工程间污染 |
| 工程版本管理 | 在 `VideoProject` 中声明 `schemaVersion`，支持字段迁移 | 新增字段必须提供默认值；读取旧版本工程时自动升级，不丢数据；无法兼容时抛出 `ProjectIncompatibleException` |
| 当前工程状态流 | 通过 `StateFlow<ProjectState>` 向全应用广播当前工程状态 | 状态包括 Idle、Loading、Active、Saving、Error、Closed；UI 与 UseCase 仅通过该流读取当前工程 |

### P1 — MVP 后 1~2 个迭代

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 工程模板 | 提供「空项目」「照片电影」「Vlog 模板」等快速创建入口 | 模板仅生成初始时间线与素材占位；不引入额外引擎能力 |
| 工程复制 | 基于现有工程创建独立副本 | 复制后新工程拥有独立目录与 id；源工程数据不受影响；耗时 ≤ 1s |
| 工程导入/导出（本地备份） | 将工程打包为 `.veproject` 文件，支持从文件恢复 | 打包包含 project.json、缩略图、封面及引用素材清单；素材缺失时给出友好提示 |
| 工程封面管理 | 允许从时间线某一帧生成或用户自选图片作为工程封面 | 封面缩略图按 16:9 与 1:1 两种比例缓存；生成不阻塞主线程 |
| 工程快照 | 手动保存关键节点，支持回退到历史快照 | 快照序列化保存于工程 `snapshots/` 目录；每个快照含版本号与时间戳 |
| 工程搜索与排序 | 在草稿箱内按名称、修改时间、时长排序与搜索 | 排序与搜索逻辑封装于 Repository；返回 `Flow<List<ProjectInfo>>` |

### P2 — 长期规划

| 功能名称 | 功能描述 | 验收标准 |
|---------|---------|---------|
| 云同步预留 | 工程模型预留 `syncId`、`remoteRevision` 等字段 | 本地逻辑不依赖云端；云端能力接入时无需修改领域模型 |
| 工程标签/收藏 | 为工程添加分类标签、收藏状态 | 标签存储于工程元数据库；支持按标签过滤 |
| 工程分享 | 生成可分享的工程文件或导出预览视频 | 分享入口由 UI 层提供，project 模块负责打包数据 |
| 工程回收站 | 删除后进入回收站，保留 30 天可恢复 | 回收站目录隔离；到期后由 WorkManager 清理 |

---

## 3. 数据模型与接口

### 3.1 核心领域模型

```kotlin
/**
 * 剪辑工程领域模型。
 * 该模型为只读不可变对象，所有修改通过 copy + 保存完成，
 * 便于 diff 比较、Undo 快照与线程安全。
 */
data class VideoProject(
    val id: String,
    val name: String,
    val createTime: Long,
    val updateTime: Long,
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val canvas: CanvasConfig,
    val timeline: Timeline,
    val exportSettings: ExportSettings,
    val coverUri: Uri? = null,
    val tags: List<String> = emptyList(),
    val syncMeta: SyncMeta? = null
)

/**
 * 画布配置，决定导出分辨率与预览比例。
 */
data class CanvasConfig(
    val aspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
    val resolution: Resolution = Resolution.P1080,
    val background: BackgroundConfig = BackgroundConfig.Blur,
    val frameRate: Int = 30
)

enum class AspectRatio { RATIO_9_16, RATIO_16_9, RATIO_1_1, RATIO_4_3 }
enum class Resolution { P480, P720, P1080 }

sealed class BackgroundConfig {
    object Blur : BackgroundConfig()
    data class SolidColor(val color: Int) : BackgroundConfig()
    data class Image(val uri: Uri) : BackgroundConfig()
}

/**
 * 导出默认参数，可被 export 模块在导出前覆盖。
 */
data class ExportSettings(
    val videoBitrate: Int = 8_000_000,
    val audioBitrate: Int = 128_000,
    val frameRate: Int = 30,
    val includeWatermark: Boolean = true,
    val outputFormat: OutputFormat = OutputFormat.MP4_H264_AAC
)

enum class OutputFormat { MP4_H264_AAC, MP4_HEVC_AAC }

/**
 * 云同步预留元数据，P2 阶段使用。
 */
data class SyncMeta(
    val syncId: String?,
    val remoteRevision: Long?,
    val lastSyncTime: Long?
)
```

### 3.2 数据库实体

```kotlin
/**
 * Room 实体，仅存储工程元数据与 JSON 文件路径。
 * 时间线详情以 JSON 文件形式保存，避免数据库表结构频繁变更。
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createTime: Long,
    val updateTime: Long,
    val schemaVersion: Int,
    val projectDir: String,
    val projectJsonPath: String,
    val coverUri: String?,
    val durationUs: Long,
    val tagsJson: String,
    val syncId: String?,
    val remoteRevision: Long?,
    val lastSyncTime: Long?
)
```

### 3.3 Repository 接口

```kotlin
interface ProjectRepository {
    /**
     * 创建新工程。
     * @param name 工程名称
     * @param canvas 初始画布配置
     * @param initialTimeline 初始时间线，可为空
     * @return 创建后的工程模型
     */
    suspend fun createProject(
        name: String,
        canvas: CanvasConfig,
        initialTimeline: Timeline = Timeline.empty()
    ): Result<VideoProject>

    /**
     * 通过 id 打开工程，加载 project.json 并反序列化。
     */
    suspend fun openProject(projectId: String): Result<VideoProject>

    /**
     * 保存当前工程，先写临时 JSON 文件，再原子替换。
     */
    suspend fun saveProject(project: VideoProject): Result<Unit>

    /**
     * 关闭当前工程，触发最终保存并释放引用。
     */
    suspend fun closeProject(): Result<Unit>

    /**
     * 删除工程及其私有目录。
     */
    suspend fun deleteProject(projectId: String): Result<Unit>

    /**
     * 复制工程。
     */
    suspend fun duplicateProject(projectId: String, newName: String): Result<VideoProject>

    /**
     * 获取所有工程信息列表（用于草稿箱）。
     */
    fun observeAllProjects(): Flow<List<ProjectInfo>>

    /**
     * 获取指定工程信息。
     */
    suspend fun getProjectInfo(projectId: String): Result<ProjectInfo>
}

/**
 * 工程列表项，仅含展示所需轻量字段。
 */
data class ProjectInfo(
    val id: String,
    val name: String,
    val updateTime: Long,
    val durationUs: Long,
    val coverUri: Uri?,
    val aspectRatio: AspectRatio
)
```

### 3.4 当前工程状态管理

```kotlin
/**
 * 当前工程运行时状态。
 */
sealed class ProjectState {
    object Idle : ProjectState()
    data class Loading(val projectId: String) : ProjectState()
    data class Active(val project: VideoProject) : ProjectState()
    data class Saving(val projectId: String) : ProjectState()
    data class Error(val projectId: String?, val cause: Throwable) : ProjectState()
    object Closed : ProjectState()
}

interface ProjectStateManager {
    val currentState: StateFlow<ProjectState>
    val currentProject: VideoProject?

    suspend fun open(projectId: String): Result<VideoProject>
    suspend fun close(): Result<Unit>
    fun updateProject(transform: (VideoProject) -> VideoProject)
    fun notifyError(cause: Throwable)
}
```

### 3.5 版本迁移接口

```kotlin
interface ProjectMigration {
    val fromVersion: Int
    val toVersion: Int

    /**
     * 将旧版本 JSON 对象迁移到当前版本。
     */
    fun migrate(json: JSONObject): JSONObject
}

interface ProjectMigrationProvider {
    fun getMigrations(fromVersion: Int, toVersion: Int): List<ProjectMigration>
}
```

---

## 4. 交互与流程

### 4.1 新建工程流程

```
用户点击「开始创作」/选择素材
    ↓
UI ViewModel 调用 CreateProjectUseCase
    ↓
ProjectRepository.createProject(name, canvas, timeline)
    ↓
创建工程私有目录与 project.json 占位
    ↓
写入 Room ProjectEntity
    ↓
ProjectStateManager 切换至 Active(project)
    ↓
自动保存通道开始监听
```

### 4.2 打开工程流程

```
草稿箱点击「继续编辑」
    ↓
ProjectStateManager.open(projectId)
    ↓
状态变为 Loading(projectId)
    ↓
ProjectRepository 读取 Room 元数据
    ↓
按 projectJsonPath 读取 project.json
    ↓
检查 schemaVersion，执行必要迁移
    ↓
反序列化为 VideoProject
    ↓
状态变为 Active(project)
    ↓
通知 Timeline/Player/Export 模块刷新
```

### 4.3 自动保存流程

```
ProjectStateManager.currentState 为 Active
    ↓
监听 Timeline/Canvas/ExportSettings 变更（由 UseCase 聚合）
    ↓
变更发生后防抖 500ms
    ↓
状态变为 Saving(projectId)
    ↓
ProjectRepository.saveProject(project)
    ↓
写 temp.project.json → fsync → 重命名为 project.json
    ↓
更新 Room updateTime
    ↓
状态恢复 Active(project)
```

### 4.4 工程删除流程

```
草稿箱触发删除
    ↓
若当前工程为被删除工程，先 closeProject()
    ↓
ProjectRepository.deleteProject(projectId)
    ↓
删除 Room 记录
    ↓
递归删除工程私有目录
    ↓
通知草稿箱刷新列表
```

---

## 5. 实现要点

### 5.1 工程文件目录结构

每个工程在应用私有目录下拥有独立目录，建议结构如下：

```
/data/data/com.example.cj.videoeditor/files/projects/{projectId}/
├── project.json              # 工程主描述文件（时间线、画布、导出参数）
├── metadata.db               # （可选）工程级缓存数据库
├── thumbnails/               # 工程内素材缩略图缓存
├── covers/                   # 工程封面（16:9 / 1:1）
├── exports/                  # 已导出视频文件
├── snapshots/                # 手动快照（P1）
└── temp/                     # 临时文件，关闭/删除时清理
```

### 5.2 持久化策略

- **元数据**：使用 Room 存储，便于草稿箱快速列表与搜索。
- **时间线与效果参数**：使用 JSON 文件存储，原因有二：
  1. 时间线结构在快速迭代中变化频繁，JSON 可避免数据库 schema 频繁迁移。
  2. JSON 文件易于版本迁移、快照备份与云同步。
- **写安全**：保存时先写入 `project.json.tmp`，调用 `FileDescriptor.sync()` 后再原子重命名，避免写一半崩溃导致工程损坏。

### 5.3 版本迁移策略

- `schemaVersion` 为整型，从 1 开始递增。
- 读取 `project.json` 时，若版本低于 `CURRENT_SCHEMA_VERSION`，按区间链式执行迁移器。
- 每个迁移器只负责一个版本跃迁，遵循单一职责。
- 新增字段必须提供默认值；删除字段保留到下一个大版本再做清理。
- 若遇到无法识别的 `schemaVersion`，抛出 `ProjectIncompatibleException`，UI 层提示用户「工程文件版本不兼容」。

### 5.4 与现有代码的衔接建议

- 旧 Demo 代码中若存在直接操作文件路径或全局工程变量的逻辑，应迁移到 `ProjectRepository` 与 `ProjectStateManager`。
- 现有 Camera 录制模块产生的分段视频文件，应统一保存到当前工程的 `temp/` 或 `media/` 子目录下，并由 media 模块登记 URI，project 模块仅保存引用。
- 导出模块输出文件应优先保存到工程 `exports/` 目录，再由 MediaStore 保存到相册；project 模块记录导出历史但不负责具体编码。

### 5.5 需要特别注意的难点

- **大工程 JSON 性能**：时间线片段与关键帧数量增多后，project.json 可能达到数 MB。应使用流式 JSON 解析器（如 Kotlinx Serialization 的 `Json.decodeFromStream`），避免一次性加载大字符串。
- **工程损坏恢复**：启动时检测到 project.json 损坏，可尝试读取 `snapshots/` 中最新快照恢复；均无则标记为损坏并提示用户。
- **多进程/多实例**：应用不应同时打开两个工程；`ProjectStateManager` 需通过单一实例与锁机制保证这一点。
- **存储空间不足**：保存前检查磁盘剩余空间，至少预留 200MB；不足时返回 `StorageException`。

---

## 6. 性能与质量要求

### 6.1 性能指标

| 场景 | 目标 | 测试方法 |
|------|------|---------|
| 新建工程 | ≤ 200ms | 从调用 createProject 到返回 Active 状态 |
| 打开工程 | ≤ 300ms | 元数据 + project.json 反序列化完成 |
| 保存工程 | ≤ 300ms（工程大小 < 5MB） | 从触发保存到数据库与 JSON 写入完成 |
| 草稿箱列表加载 | ≤ 200ms / 100 条 | Room 分页查询 |
| 自动保存防抖 | 500ms | 用户停止操作后 500ms 触发，避免频繁写磁盘 |
| 删除工程 | ≤ 500ms | 目录递归删除在 IO 线程完成 |

### 6.2 常见异常与处理

| 异常 | 原因 | 处理策略 |
|------|------|---------|
| `ProjectNotFoundException` | 工程 id 不存在或记录已被删除 | 返回 Result.failure，UI 提示「工程不存在或已被删除」 |
| `ProjectIncompatibleException` | project.json 版本号高于当前 App 支持 | UI 提示「请升级应用后打开」 |
| `ProjectCorruptedException` | JSON 解析失败或文件缺失 | 尝试快照恢复；无法恢复则标记损坏 |
| `StorageException` | 磁盘空间不足或权限受限 | 中止保存，提示用户清理空间或检查权限 |
| `ConcurrentModificationException` | 保存过程中工程被修改 | 使用不可变模型 + 版本号乐观锁，冲突时丢弃旧保存 |

### 6.3 质量要求

- 所有公共 API 必须标注 `@Throws` 或返回 `Result<T>`。
- 工程模型不可变，禁止在业务层直接修改 `VideoProject` 字段。
- 文件 IO 必须在 `Dispatchers.IO` 上执行，禁止在主线程调用 Repository 保存方法。
- 必须编写单元测试覆盖：新建/打开/保存/删除/迁移五个核心路径。
- 崩溃率目标：因 project 模块导致的崩溃 < 0.1%。

---

## 7. 依赖模块

| 模块 | 依赖方式 | 说明 |
|------|---------|------|
| `timeline` | Domain 层引用 | `VideoProject` 持有 `Timeline` 对象；project 模块负责序列化与反序列化 |
| `draft` | Data 层被依赖 | draft 模块调用 `ProjectRepository` 实现草稿箱列表与自动保存调度 |
| `media` | Domain 层引用 URI | `VideoClip` 中媒体 URI 由 media 模块解析与扫描，project 仅保存引用 |
| `export` | Domain 层引用配置 | `ExportSettings` 作为导出模块的输入参数之一 |
| `uiux` | 无直接依赖 | project 模块通过 `ProjectState` 与 `ProjectInfo` 向 UI 层暴露数据，不依赖具体 UI 实现 |

---

## 8. 附录

### 8.1 核心接口调用示例

```kotlin
class EditorViewModel @Inject constructor(
    private val projectStateManager: ProjectStateManager,
    private val saveProjectUseCase: SaveProjectUseCase
) : ViewModel() {

    val projectState: StateFlow<ProjectState> = projectStateManager.currentState

    fun renameProject(newName: String) {
        projectStateManager.updateProject { project ->
            project.copy(name = newName, updateTime = System.currentTimeMillis())
        }
        viewModelScope.launch {
            val current = projectStateManager.currentProject ?: return@launch
            saveProjectUseCase(current)
        }
    }
}
```

### 8.2 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | 2025-06-28 | 初始版本，定义 project 模块职责、模型、接口与流程 |

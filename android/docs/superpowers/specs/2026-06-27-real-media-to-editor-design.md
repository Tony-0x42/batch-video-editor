# 把媒体选择页的真实视频传入编辑器

## 背景

当前编辑页（`EditorScreen`）加载的是 `EditorViewModel.createTestTimeline()` 写死的测试数据：

- 视频轨道固定 2 条，URI 为 `content://test/video1`、`content://test/video2`
- 音频轨道固定 1 条，URI 为 `content://test/audio1`
- 文字轨道固定 1 条

这导致无论用户在媒体选择页选中多少视频，进入编辑器后看到的都是同样的假数据。任务目标是把用户真实选中的视频按顺序转换成 `Timeline`，并让编辑器显示真实的时间线。

## 目标

1. 媒体选择页点击“下一步”时，根据用户选中的视频 URI 构建真实 `Timeline`。
2. 把真实 `Timeline` 写入 `TimelineRepository`。
3. 导航到编辑器时携带一个真实的 `projectId`。
4. `EditorViewModel` 不再使用测试数据，而是从 `TimelineRepository` 读取当前 `Timeline`。
5. 每次进入新工程时清空命令栈，避免上一次编辑的撤销/重做状态残留。

## 方案选择

### 方案 A：生成真实 projectId + 内存 TimelineRepository（推荐）

- 在 `VideoEditorNavHost` 中，当 `MediaPickerScreen` 确认选择后，生成一个新的 UUID 作为 `projectId`。
- `MediaPickerViewModel` 提供 `buildTimelineFromSelection()`，把选中的视频按选择顺序首尾相接生成 `VideoClip` 和 `AudioClip`，并写入 `TimelineRepository`。
- `EditorViewModel` 的 `init` 中订阅 `TimelineRepository.getTimeline()`，不再调用 `createTestTimeline()`。
- `EditorViewModel` 在加载新工程时调用 `editorUseCase.clearCommandStack()`（或新增方法），清空撤销/重做栈。

**优点：**

- 与现有 `editor/{projectId}` 路由保持一致。
- 为后续工程持久化、草稿恢复、导出页传递 projectId 留下接口。
- 改动集中，不破坏现有命令/撤销重做逻辑。

**缺点：**

- MVP 阶段 Timeline 仍只存在内存，退出应用后丢失；但这是已知现状，不是本次改造要解决的问题。

### 方案 B：通过导航参数直接传递 URI 列表

- 把选中的 URI 列表编码进 `editor/{projectId}?uris=...` 路由参数。
- `EditorViewModel` 在 `init` 中解析 URI 列表并构建 Timeline。

**优点：**

- 不依赖全局内存状态，返回后再进入会得到新的 Timeline。

**缺点：**

- URI 列表可能很长，超出导航参数长度限制。
- `projectId` 仍为占位或需要在目标页再次生成。
- 与当前 `TimelineRepository` 设计重复。

**决策：** 采用方案 A。

## 详细设计

### 数据流

```
MediaPickerScreen
    └─ 用户选中视频
    └─ 点击“下一步”
        └─ onMediaSelected(selectedUris) 回调到 VideoEditorNavHost
            └─ NavHost 生成 projectId = UUID.randomUUID().toString()
            └─ viewModel.buildTimelineFromSelection() // 挂起函数
            └─ 成功后 navigate("editor/$projectId")

EditorScreen(projectId)
    └─ EditorViewModel
        └─ init 订阅 TimelineRepository.getTimeline()
        └─ 已有测试数据不再写入
        └─ 命令栈清空
```

### Timeline 构建规则

1. 仅处理选中的视频（当前媒体页默认 VIDEO 标签）。
2. 每个视频生成一条 `VideoClip`，放置到 `track_video_1`。
3. 每个视频同时生成一条对应的 `AudioClip`，放置到 `track_audio_1`，URI 与原视频相同（后续解码器负责分离音轨）。
4. 片段按用户选择顺序首尾相接：
   - 第 0 个视频：`startTimeUs = 0`，`endTimeUs = durationUs`
   - 第 n 个视频：`startTimeUs = 前 n 个视频总时长`，`endTimeUs = startTimeUs + durationUs`
5. `sourceStartUs = 0`，`sourceEndUs = durationUs`。
6. 如果无法获取视频时长，durationUs 为 0（后续渲染会显示为空片段，但不会崩溃）。

### 关键改动点

#### 1. `MediaPickerViewModel`

- 已新增 `buildTimelineFromSelection()` 和 `createTimelineFromUris()`（需要补全 import 和编译修复）。
- 注入 `TimelineRepository` 与 `@ApplicationContext`（已注入）。

#### 2. `VideoEditorNavHost`

- `MediaPickerScreen` 的 `onMediaSelected` 改为接收 URI 列表后：
  1. 生成 UUID `projectId`。
  2. 调用 `viewModel.buildTimelineFromSelection()`（需要在 Composable 中通过 `hiltViewModel()` 获取 `MediaPickerViewModel`）。
  3. 成功后 `navController.navigate(Screen.Editor.createRoute(projectId))`。

#### 3. `EditorViewModel`

- 移除 `init` 中的 `timelineRepository.setTimeline(createTestTimeline())`。
- 保留对 `timelineRepository.getTimeline()` 的订阅。
- 在 `init` 中调用 `editorUseCase.clearCommandStack()`（需要在 `EditorUseCase` 中暴露该方法）。
- 可保留 `createTestTimeline()` 作为私有备用方法，但不再调用。

#### 4. `EditorUseCase`

- 新增 `clearCommandStack()` 方法，调用 `commandStack.clear()`。

### 边界情况

- **未选中任何视频**：底部“下一步”按钮已禁用，不会出现。
- **选中视频无音频**：当前仍为其创建 `AudioClip`，播放器/导出模块后续读取不到音频轨道时可自行处理为空音频；时间轴仍显示音频条。
- **获取不到时长**：durationUs = 0，片段在时间轴上宽度为 0，UI 需要能处理这种情况（目前时间轴按 `endTimeUs - startTimeUs` 计算宽度，0 时长片段可能不可见；本次不强制处理，但会在真机测试中观察）。
- **返回媒体页重新选择**：由于 `TimelineRepository` 是单例，新选择会覆盖旧 Timeline；`EditorViewModel` 重新进入时会订阅到新的 Timeline。

## 测试与验证

1. 编译通过。
2. 在真机上：
   - 授权后媒体页显示视频缩略图。
   - 选择 1 个视频 → 进入编辑器 → 视频轨道显示 1 个片段，音频轨道显示 1 个片段，总时长与视频时长一致。
   - 选择 3 个视频 → 进入编辑器 → 视频轨道显示 3 个片段，音频轨道显示 3 个片段，总时长为三者之和。
   - 选中视频片段 → 点击“分割” → 片段被分成两段。
   - 选中片段 → 点击“删除” → 片段消失，后续片段前移。
   - 选中片段 → 点击“复制” → 出现同名副本并后移。
   - 操作后“撤销/重做”可用且行为正确。

## 后续可扩展

- 把 `Timeline` 持久化到 Room 或 JSON 文件，按 `projectId` 存储。
- 在媒体选择页支持选择照片、纯音频，分别生成 `ImageClip` 和 `AudioClip`。
- 在编辑器内支持调整片段顺序、裁剪入点出点。

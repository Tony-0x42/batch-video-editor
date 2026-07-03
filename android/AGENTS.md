# AGENTS.md

> 本文件为 AI 编码助手提供项目级上下文与规范指引。

## 项目概述

VideoEditor-For-Android 是一个正在从「技术 Demo」向「商用级手机视频剪辑 App」演进的 Android 项目。

- **定位**：Android 端短视频剪辑工具
- **目标**：提供对标剪映/CapCut/VN 的基础剪辑体验
- **核心技术**：Camera2/CameraX + OpenGL ES + MediaCodec 硬编码 + Kotlin Coroutines

## 规范入口

所有涉及产品、技术、模块实现的问题，必须首先参考 `spec/` 目录下的规范：

```
spec/
├── README.md              # 总览与导航
├── technical-spec.md      # 技术栈、架构、数据流、性能指标
├── project-boundary.md    # 项目边界与 MVP 范围
├── project-guidelines.md  # 编码规范、目录结构、命名约定
└── modules/               # 各模块详细规范
    ├── timeline/
    ├── recorder/
    ├── editor/
    ├── audio/
    ├── filters/
    ├── effects/
    ├── export/
    ├── player/
    ├── media/
    ├── project/
    ├── draft/
    └── uiux/
```

## 助手行为准则

1. **实现前必读规范**
   - 修改或新增功能前，先阅读对应模块的 `spec/modules/{module}/README.md`
   - 若规范缺失或不明确，先补充规范再实现

2. **遵循架构分层**
   - UI → ViewModel → UseCase → Domain Repository → Data/Engine
   - 禁止跨层直接调用

3. **保持代码质量**
   - 遵循 `spec/project-guidelines.md` 中的命名、格式、注释规范
   - 新模块优先使用 Kotlin
   - 所有公共 API 必须有 KDoc

4. **优先满足 MVP**
   - 参考 `spec/project-boundary.md` 明确当前范围
   - 不在首个 MVP 范围内的功能，仅预留接口与扩展点

5. **美观与体验**
   - 商用级 UI/UX 要求：现代、简洁、手势友好
   - 参考 `spec/modules/uiux/README.md` 中的设计系统

6. **性能意识**
   - 视频处理必须考虑内存、帧率、导出速度
   - 禁止在主线程执行耗时操作

## 当前已知问题

- 旧版 `android.hardware.Camera` 需要迁移到 Camera2/CameraX
- 缺少时间轴编辑器与多轨道模型
- UI 为早期 Demo 风格，需要全面重构为商用界面
- 导出参数固定，需要支持自定义

## 联系与决策

- 技术决策：参考 `spec/technical-spec.md`
- 范围决策：参考 `spec/project-boundary.md`
- 代码风格：参考 `spec/project-guidelines.md`

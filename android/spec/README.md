# VideoEditor-For-Android 商用视频剪辑 SPEC

> 本文档是 VideoEditor-For-Android 项目从「技术 Demo」向「商用级手机视频剪辑 App」演进的产品与技术规范总览。
> 所有模块实现前，必须先阅读本目录下的总体规范，再阅读对应模块规范。

---

## 1. 项目定位

### 1.1 目标
打造一个可在 Android 手机上流畅运行的**商用级短视频剪辑应用**，对标剪映/CapCut、快影、VN 等主流产品的基础剪辑体验。

### 1.2 核心原则
- **先能剪，再炫酷**：首个闭环是「导入 → 剪辑 → 导出」
- **性能优先**：移动设备资源有限，必须基于硬解码/硬编码 + OpenGL GPU 渲染
- **可扩展架构**：模块间通过清晰接口解耦，便于后续叠加 AI、云端、素材库
- **商用 UI/UX**：专业剪辑界面、手势交互、实时预览、撤销重做

### 1.3 当前状态
项目已有基础链路：
- Camera + OpenGL ES 2.0 录制
- MediaCodec 硬编码
- 11+ 预设滤镜 + 美颜
- 音视频分离、PCM 编码、音频混音（JNI）
- 两段视频拼接

但缺失：时间轴编辑器、精确剪辑、多轨道、BGM、转场、字幕、变速倒放、现代相机 API 等商用必备能力。

---

## 2. 规范文件导航

| 文件 | 说明 |
|------|------|
| [README.md](./README.md) | 本文件：总览与导航 |
| [technical-spec.md](./technical-spec.md) | 技术栈、架构分层、数据流、性能指标 |
| [project-boundary.md](./project-boundary.md) | 项目边界：做什么、不做什么、MVP 范围 |
| [project-guidelines.md](./project-guidelines.md) | 编码规范、目录结构、命名约定、代码审查标准 |

### 2.1 模块规范目录

| 模块 | 规范路径 | 负责范围 |
|------|----------|----------|
| 时间线 | [modules/timeline](./modules/timeline) | 多轨道时间轴、片段模型、剪辑操作、撤销重做 |
| 录制 | [modules/recorder](./modules/recorder) | 相机预览、录制、分段录制、CameraX 迁移 |
| 剪辑核心 | [modules/editor](./modules/editor) | 分割、裁剪、删除、复制、排序、变速、倒放 |
| 音频 | [modules/audio](./modules/audio) | BGM、音效、录音、音频提取、混音、音量包络 |
| 滤镜调色 | [modules/filters](./modules/filters) | 预设滤镜、LUT、基础调色、美颜、磨皮 |
| 特效字幕 | [modules/effects](./modules/effects) | 文字、字幕、贴纸、转场、关键帧、画中画 |
| 导出 | [modules/export](./modules/export) | 渲染管线、编码参数、导出进度、格式选择 |
| 播放器 | [modules/player](./modules/player) | 实时预览播放器、Seek、逐帧、Surface 管理 |
| 媒体管理 | [modules/media](./modules/media) | 导入、素材扫描、缩略图、媒体格式支持 |
| 项目管理 | [modules/project](./modules/project) | 草稿、工程文件、自动保存、元数据 |
| UI/UX | [modules/uiux](./modules/uiux) | 页面结构、设计系统、交互手势、主题 |
| 草稿 | [modules/draft](./modules/draft) | 草稿箱、自动保存、工程恢复 |

---

## 3. 功能优先级总览

### P0 - 首个 MVP 必须实现
1. 本地素材导入（视频/图片/音频）
2. 多轨道时间轴（主视频轨 + 画中画轨 + 音频轨 + 文字轨）
3. 基础剪辑：分割、裁剪、删除、复制、拖拽排序
4. 画布与比例：9:16 / 16:9 / 1:1 / 4:3 + 背景填充
5. 音频基础：本地 BGM、音频提取、音量调节、基础混音
6. 文字与滤镜：手动字幕、字体样式、基础滤镜、美颜
7. 导出：720p/1080p、30fps、MP4、保存相册
8. 草稿自动保存 + 水印开关

### P1 - MVP 后 1~2 个迭代
- 变速、倒放、定格
- 转场效果
- 录音/音效库
- 画中画增强
- 关键帧动画
- 贴纸与特效库
- 撤销/重做多步栈

### P2 - 长期规划
- AI 字幕/配音
- 智能抠像
- 云同步
- LUT 高级调色
- 会员体系与在线素材商店

---

## 4. 关键非功能需求

| 指标 | 目标 |
|------|------|
| 启动时间 | 冷启动 ≤ 2s |
| 导入时间 | 1 分钟 1080p 素材 ≤ 3s 完成索引与缩略图生成 |
| 时间线操作延迟 | 分割/删除/拖拽后 UI 反馈 ≤ 100ms |
| 预览帧率 | 编辑预览 ≥ 24fps（1080p 设备） |
| 导出速度 | 1 分钟 1080p 视频导出 ≤ 40s（中档设备） |
| 崩溃率 | 发布版本崩溃率 < 0.5% |
| 包体积 | 安装包 ≤ 80MB（不含在线素材） |

---

## 5. 如何阅读本规范

1. 先读 [project-boundary.md](./project-boundary.md) 明确范围
2. 再读 [technical-spec.md](./technical-spec.md) 理解架构
3. 最后按需阅读 [modules/](./modules/) 下对应模块规范
4. 编码前必读 [project-guidelines.md](./project-guidelines.md)

---

## 6. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | 2025-06-28 | 初始 SPEC，基于商用视频剪辑软件调研与现有项目评估 |

# AGENTS.md

> 本文件为 AI 编码助手提供项目级上下文与规范指引。

## 项目定位

本工程是「批量剪辑」的 **Android APP 用户端**：面向品牌方、经销商（分公司/服务商）及个人用户的 **批量视频生成工具**。核心价值不是单视频剪辑，而是「AI 云创」——上传素材 → AI 分割分镜 → 按随机化策略**批量生成多条低重复率视频**，另含 AI 去水印、学习专区、个人中心等功能。

- 后端：RuoYi 管理后台 + 业务接口，工程在 `../server`（见 `../server/doc/部署说明.md`）
- 产品需求：`../spec/`（页面级需求在 `../spec/modules/`）
- 后端接口文档：**`api-doc.md`（本目录，开发前必读）**
- 项目级产品约定（组织树、算力规则、视觉规范等）：`../AGENTS.md`

## 代码结构（`app/src/main/java/com/example/cj/videoeditor/`）

> 包名 `com.example.cj.videoeditor` 是底包遗留，业务代码就在其中，勿按包名判断归属。

### 批量剪辑业务代码（日常开发主战场）

| 目录 | 内容 |
|------|------|
| `activity/` | 各页面 Activity：登录/注册（Login/Register）、AI 云创编辑（AiCreationEdit）、分割进度（SplitProgress）、去水印（Watermark*）、学习/品牌/文档/公告/客服/资料编辑等 |
| `ui/` | Compose 页面：`home/`（首页）、`aicreation/`（AI 云创）、`watermark/`（AI 去水印）、`profile/`（我的） |
| `network/` | Retrofit 网络层：`ApiService.java`（接口定义）、`RetrofitClient.kt`、`ApiHelper.java`、`dto/`（与后端一一对应的 DTO）、`interceptor/`（Token 注入） |
| `data/`、`di/`、`bean/`、`utils/` | 本地存储、Hilt 依赖注入、通用实体与工具 |

对接后端时的顺序：先查 `api-doc.md` 确认接口 → 在 `network/dto/` 建/改 DTO → 在 `ApiService.java` 加方法 → 页面层调用。

### 底包遗留代码（仅供参考，**勿当成需求实现**）

本工程基于开源项目 VideoEditor-For-Android 二次开发，以下目录是底包（剪映式**单视频编辑器**）的遗留代码：

- `camera/`、`record/`、`gpufilter/`、`filter/`、`mediacodec/`、`media/`、`drawer/`、`widget/`、`jni/`
- `domain/`（含 `engine/`、`usecase/`、`repository/`）、`engine/`（exporter/player/recorder/renderer/audio）
- 本目录下的 `spec/`（timeline/recorder/editor/filters/export 等底包模块规范）

这些代码与批量剪辑业务基本无关：批量剪辑的视频分割/合成在**服务端用 FFmpeg 完成**（见 `../server` 的 `batch/aivideo`），APP 只负责上传素材、提交任务、轮询结果。**不要**依据底包 spec 新增剪辑/录制/滤镜功能，除非需求文档（`../spec/`）明确要求。

## 业务约束（必须遵守）

- **底部导航**：首页 / AI 云创 / AI 去水印 / 我的，4 项固定；主色调蓝色。
- **登录体系**：APP 使用 `POST /batch/app/login`、`/batch/app/register`（手机号+密码，匿名放行），Token 权限标识 `app:user`；不要用后台的 `/login`、`/register`。
- **算力前置检查**：AI 生成、去水印下载前必须检查/扣减算力（`POST /batch/computing/log/consume` 或由生成接口内部扣减），余额不足统一提示「当前算力已耗尽，请联系管理员增加算力额度」。
- **上传限制**：AI 云创单次选择视频上限 10 个；切片时长 0.5~10s、步长 0.1s，阈值由后台全局参数（`/batch/config/global`）下发，前端不写死。
- **素材只读**：展示类文案/图片/数字均由后台下发，禁止客户端写死。
- **高危操作**（注销、删除）：必须二次确认弹窗。
- **Base URL**：`BuildConfig.API_BASE_URL` 由 `app/build.gradle` 注入，可在 `gradle.properties` 用 `API_BASE_URL` / `API_BASE_URL_DEBUG` / `API_BASE_URL_RELEASE` 覆盖；模拟器访问本机后端用 `http://10.0.2.2:8080/`。

## 构建与运行

- **JDK 17**（`gradle.properties` 已配置 `org.gradle.java.home=D:/tools/jdk17`）；compileSdk 35、minSdk 24。
- 常用命令（Windows）：
  ```bash
  gradlew.bat assembleDebug       # 调试包
  gradlew.bat assembleRelease     # 发布包（release 当前未开启混淆）
  gradlew.bat test                # 单元测试
  ```
- 本目录下大量 `build*.log` / `compile*.log` 为历史构建日志，忽略即可。
- 上线前：`API_BASE_URL_RELEASE` 指向生产 HTTPS 地址，并将 `AndroidManifest.xml` 的 `usesCleartextTraffic` 改为 `false`（详见 `../server/doc/部署说明.md`）。

## 助手行为准则

1. 改页面前先读 `../spec/modules/app/<页面>/README.md` 与 `api-doc.md` 对应章节。
2. 接口路径、参数、权限以 `../server` 后端代码为准，禁止臆造；发现文档与代码不一致时以代码为准并更新 `api-doc.md`。
3. 新页面/组件沿用现有 Activity + Compose 混合架构与网络层封装，不要引入新网络框架。
4. 最小改动：不重构底包遗留代码，不在同一任务中改动无关模块。

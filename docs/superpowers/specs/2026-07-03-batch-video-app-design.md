# 批量剪辑 AI APP 端设计方案

> **设计日期**：2026-07-03  
> **对应需求**：`spec/modules/app/` 下全部 16 个页面 README.md  
> **参考架构**：`D:/project/VideoReEditor-For-Android`（Kotlin + Jetpack Compose + Hilt + MVVM + Clean Architecture）

---

## 1. 项目背景与目标

`android/android/` 目录当前完全空白，需要从零构建一个完整可用的 Android APP。APP 面向品牌方、经销商及个人用户，核心功能包括：手机号密码登录/注册、首页品牌与内容展示、AI 短视频创作、AI 去水印、个人中心。

**成功标准**：
- APP 能在已连接的 Android 真机上安装、启动、运行；
- 所有 16 个页面可正常进入、返回、交互；
- 所有表单、弹窗、加载、空状态、二次确认可用；
- 视频选择、预览、保存等本地链路可用；
- AI/云端强依赖功能以 Mock/模拟方式保证流程完整，并预留切换真实服务的接口。

---

## 2. 方案选型

| 方案 | 描述 | 优点 | 缺点 | 结论 |
|---|---|---|---|---|
| A：纯原生 Android + 本地简化算法 | Kotlin + Jetpack Compose + Hilt + MVVM，本地实现基础视频能力，AI 能力走 Mock API | 性能最佳、与参考架构一致、可充分调用 Android 媒体 API、便于后续替换真实后端 | 开发量较大，部分 AI 功能只能模拟 | **推荐** |
| B：跨平台（Flutter/RN） | 一套代码多端，视频走原生插件或云端 | 开发速度可能快 | 与参考架构不符、视频桥接复杂 | 不采用 |
| C：WebView 壳 + H5 | 原生壳加载 H5 | 开发最快 | 体验差、离线弱、不符合完整 APP 预期 | 不采用 |

---

## 3. 技术栈

| 层级 | 技术 |
|---|---|
| 语言 | Kotlin 1.9+ |
| UI | Jetpack Compose + Material3 |
| 导航 | Jetpack Navigation Compose |
| 依赖注入 | Hilt 2.51+ |
| 响应式 | Kotlin Coroutines + Flow |
| 架构 | MVVM + Repository + UseCase |
| 本地存储 | DataStore（Token/用户/设置）+ Room（内容缓存/视频组元数据） |
| 网络 | Retrofit2 + OkHttp + Kotlin Serialization |
| 图片加载 | Coil |
| 本地视频处理 | MediaExtractor + MediaCodec + MediaMuxer + MediaStore |
| 测试 | JUnit 4 + Coroutines Test + 真机冒烟测试 |

---

## 4. 工程结构

```
android/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/batchvideo/app/
│   │   │   ├── App.kt
│   │   │   ├── di/
│   │   │   │   ├── NetworkModule.kt
│   │   │   │   ├── DatabaseModule.kt
│   │   │   │   ├── DataStoreModule.kt
│   │   │   │   └── RepositoryModule.kt
│   │   │   ├── ui/
│   │   │   │   ├── navigation/
│   │   │   │   │   ├── BatchVideoNavHost.kt
│   │   │   │   │   ├── BottomNavBar.kt
│   │   │   │   │   └── Screen.kt
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Type.kt
│   │   │   │   │   └── Theme.kt
│   │   │   │   ├── components/
│   │   │   │   │   ├── LoadingScreen.kt
│   │   │   │   │   ├── EmptyScreen.kt
│   │   │   │   │   ├── ErrorRetry.kt
│   │   │   │   │   ├── ConfirmDialog.kt
│   │   │   │   │   └── PowerCheckBanner.kt
│   │   │   │   ├── auth/
│   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   ├── RegisterScreen.kt
│   │   │   │   │   └── AgreementScreen.kt
│   │   │   │   ├── home/
│   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   ├── LearningScreen.kt
│   │   │   │   │   ├── ContactScreen.kt
│   │   │   │   │   ├── BrandScreen.kt
│   │   │   │   │   └── DocumentScreen.kt
│   │   │   │   ├── aicreation/
│   │   │   │   │   ├── AiCreationListScreen.kt
│   │   │   │   │   ├── AiCreationEditScreen.kt
│   │   │   │   │   └── SplitProgressScreen.kt
│   │   │   │   ├── watermark/
│   │   │   │   │   └── WatermarkScreen.kt
│   │   │   │   └── profile/
│   │   │   │       ├── ProfileScreen.kt
│   │   │   │       ├── EditProfileScreen.kt
│   │   │   │       └── CustomerServiceScreen.kt
│   │   │   ├── presentation/
│   │   │   │   ├── auth/AuthViewModel.kt
│   │   │   │   ├── home/HomeViewModel.kt
│   │   │   │   ├── aicreation/AiCreationViewModel.kt
│   │   │   │   ├── watermark/WatermarkViewModel.kt
│   │   │   │   └── profile/ProfileViewModel.kt
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.kt
│   │   │   │   │   ├── ComputePower.kt
│   │   │   │   │   ├── Banner.kt
│   │   │   │   │   ├── VideoGroup.kt
│   │   │   │   │   ├── Clip.kt
│   │   │   │   │   ├── Material.kt
│   │   │   │   │   ├── Contact.kt
│   │   │   │   │   ├── Brand.kt
│   │   │   │   │   └── Document.kt
│   │   │   │   ├── repository/
│   │   │   │   │   ├── AuthRepository.kt
│   │   │   │   │   ├── HomeRepository.kt
│   │   │   │   │   ├── AiCreationRepository.kt
│   │   │   │   │   ├── WatermarkRepository.kt
│   │   │   │   │   └── ProfileRepository.kt
│   │   │   │   └── usecase/
│   │   │   │       ├── ValidatePhoneUseCase.kt
│   │   │   │       ├── CheckComputePowerUseCase.kt
│   │   │   │       └── SaveToGalleryUseCase.kt
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── UserDao.kt
│   │   │   │   │   ├── VideoGroupDao.kt
│   │   │   │   │   └── CacheDataStore.kt
│   │   │   │   ├── remote/
│   │   │   │   │   ├── BatchVideoApiService.kt
│   │   │   │   │   ├── MockDataSource.kt
│   │   │   │   │   └── NetworkResult.kt
│   │   │   │   └── repository/
│   │   │   │       ├── AuthRepositoryImpl.kt
│   │   │   │       ├── HomeRepositoryImpl.kt
│   │   │   │       ├── AiCreationRepositoryImpl.kt
│   │   │   │       ├── WatermarkRepositoryImpl.kt
│   │   │   │       └── ProfileRepositoryImpl.kt
│   │   │   └── utils/
│   │   │       ├── Extensions.kt
│   │   │       ├── PermissionUtils.kt
│   │   │       ├── FileUtils.kt
│   │   │       ├── VideoUtils.kt
│   │   │       └── ToastUtils.kt
│   │   └── res/values/
│   │       ├── colors.xml
│   │       ├── strings.xml
│   │       └── themes.xml
│   └── src/test/...
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradle/wrapper/gradle-wrapper.properties
```

---

## 5. 全局约束落实

| 约束来源 | 落实方式 |
|---|---|
| 蓝色主色、蓝色主按钮 | `Color.kt` 中定义 `PrimaryBlue = #2196F3`，主题应用；所有主操作按钮使用 `Button` + `containerColor = PrimaryBlue` |
| 底部 4 项固定导航 | `BottomNavBar` 固定顺序：首页 / AI云创 / AI去水印 / 我的 |
| 二级页面返回按钮 | 每个二级 Screen 顶部使用 `CenterAlignedTopAppBar` + `navigationIcon` 返回箭头 |
| 加载与空状态 | 全局 `LoadingScreen`、`EmptyScreen`、`ErrorRetry` 组件，Repository 返回 Loading/Error/Empty |
| 高危操作二次确认 | 全局 `ConfirmDialog`，删除视频组、注销、退出登录前弹出 |
| 后台下发素材 | 所有展示文本/图片/数字/联系方式/文档/视频均走 Repository，Mock 数据模拟后台下发 |
| 算力全局规则 | `CheckComputePowerUseCase` + `PowerCheckBanner`，AI 生成/去水印保存前校验，不足弹窗 |
| 上传限制 | 视频选择器限制单次最多 10 个；分割时长 Slider 固定 0.5~10s、步长 0.1s |
| 协议合规 | 登录/注册页面必须勾选协议，未勾选时登录按钮置灰并提示 |

---

## 6. 模块详细设计

### 6.1 登录/注册模块

- **LoginScreen**：Logo + Slogan（后台下发）、手机号输入、密码输入（显隐切换）、协议勾选、登录按钮、注册入口。
- **RegisterScreen**：上级手机号只读展示（通过 Deep Link/Intent 模拟扫码读取）、手机号/密码/确认密码、协议勾选、注册按钮。
- **AgreementScreen / PrivacyScreen**：富文本/Html 展示，图片点击放大。
- **状态**：`AuthUiState` 包含手机号、密码、协议勾选、加载、错误提示；`AuthViewModel` 处理校验与登录/注册。
- **持久化**：登录成功后将 Token 与 `User` 写入 DataStore。

### 6.2 首页模块

- **HomeScreen**：
  - 顶部 Banner（自动轮播 + 指示器，点击进入链接或详情）。
  - 喜报区（业绩标题 + 销售冠军 + 销售金额）。
  - 4 宫格功能导航（学习专区、信息咨询、品牌专区、其他服务）。
  - 教程文档入口。
- **LearningScreen**：分类 Tab + 资料列表 + 搜索，点击进入详情或播放。
- **ContactScreen**：在线客服入口、总台电话、区域负责人列表，电话唤起拨号。
- **BrandScreen**：品牌列表 + 品牌详情页。
- **DocumentScreen**：文档分类 + 列表 + 富文本详情。

### 6.3 AI 云创模块

- **AiCreationListScreen**：算力统计卡片、视频组列表、搜索、长按拖拽排序、删除二次确认、新增按钮。
- **AiCreationEditScreen**：
  - 标题编辑；
  - 视频预览区（当前选中分镜头）；
  - 分镜头总进度；
  - 保留原声开关；
  - AI 视频分割入口；
  - 分镜头管理：编号、拖拽排序、镜像、替换、删除；
  - AI 口播配音（文字转语音/本地录音）；
  - 背景音乐（本地上传/提取原声，音量调节）；
  - 底部 AI 生成按钮。
- **SplitProgressScreen**：进度条、当前视频名称、总视频数、切片时长、完成后自动返回。
- **本地实现**：视频选择器（≤10 个）、MediaMetadataRetriever 获取时长/缩略图、简单裁剪预览。
- **Mock 实现**：AI 分割、配音、生成通过 Mock API 模拟异步任务与结果返回。

### 6.4 AI 去水印模块

- **WatermarkScreen**：链接输入框、清空、解析按钮、结果 Tab（视频/图片/文本）、保存按钮。
- **本地实现**：保存视频/图片到相册（MediaStore）。
- **Mock 实现**：链接解析返回模拟的去水印视频 URL、视频帧图片、文案。

### 6.5 我的模块

- **ProfileScreen**：头像、昵称、脱敏手机号、VIP 标识与有效期、服务列表、系统设置、退出登录。
- **EditProfileScreen**：头像拍照/相册、昵称修改、手机号更换。
- **CustomerServiceScreen**：客服电话、服务时段、在线客服入口。
- **设置/协议**：复用全局组件。
- **退出/注销**：二次确认后清除本地数据并返回登录页。

---

## 7. 数据流

```
UI Screen
   ↓ (collect StateFlow, dispatch UiEvent)
ViewModel
   ↓ (call UseCase / Repository)
Repository
   ↓ (switchable: Mock / Retrofit / Local DB)
Data Source
```

- **状态管理**：每个 Screen 对应一个 `UiState` data class 与一个 `UiEvent` sealed class。
- **Repository 双模式**：所有 Repository 接口提供 `MockXxxRepositoryImpl` 和 `NetworkXxxRepositoryImpl`，通过 Hilt 绑定默认使用 Mock，便于无后台时真机测试。
- **错误处理**：Repository 返回 `Result<T>` 或 `NetworkResult<T>`；ViewModel 转换为 `UiState.Error(message)`，UI 展示 `ErrorRetry`。

---

## 8. 本地视频处理策略

| 功能 | 实现方式 |
|---|---|
| 视频选择 | `ActivityResultContracts.GetMultipleContents("video/*")`，限制 10 个 |
| 视频信息 | `MediaMetadataRetriever` 获取时长、宽高、缩略图 |
| 视频预览 | `ExoPlayer` 或 `VideoView` Compose 封装 |
| 保存相册 | `MediaStore.Video.Media` / `MediaStore.Images.Media` 插入 |
| 简单裁剪 | `MediaExtractor` + `MediaCodec` + `MediaMuxer`（轻量实现） |
| AI 分割/配音/生成 | 本地仅做 UI 与进度模拟，真实算法通过 `AiCreationApi` 接口后续接入 |

---

## 9. 测试计划

1. **构建测试**：`./gradlew assembleDebug` 成功生成 APK。
2. **单元测试**：ViewModel 状态流转、UseCase 校验。
3. **真机冒烟测试**：
   - 启动 → 登录 → 首页；
   - 底部 4 个 Tab 切换；
   - 首页各入口进入与返回；
   - AI 云创新增/编辑/删除/生成流程；
   - AI 去水印解析与保存；
   - 我的页编辑/清理缓存/退出登录。

---

## 10. 风险与应对

| 风险 | 应对 |
|---|---|
| AI 功能无法真正本地运行 | 使用 Mock API 模拟完整流程，接口与数据结构对齐需求文档，后续可替换 |
| 真机视频保存权限问题 | 适配 Android 10+ Scoped Storage，使用 MediaStore |
| Gradle/NDK 环境差异 | 采用与参考项目接近的 Gradle 8.9 + AGP 8.7 + JDK17 配置 |
| 开发量巨大 | 分模块按 Task 实现，优先保证 UI 与流程可用，再细化动画与性能 |

---

## 11. 待后续迭代

- 接入真实 server 后端 API；
- 替换 Mock 的 AI 分割、配音、去水印解析服务；
- 实现完整时间线渲染与导出引擎；
- 相机录制与高级滤镜；
- 推送、统计、埋点。

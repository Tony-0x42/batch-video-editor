# 批量剪辑 AI APP 端实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `batch-video-editor/batch-video-editor/` 目录从零构建一个完整可用的批量剪辑 AI Android APP，覆盖登录/注册、首页、AI 云创、AI 去水印、我的五大模块，支持真机安装与冒烟测试。

**Architecture:** 采用 Kotlin + Jetpack Compose + Hilt + MVVM + Repository + UseCase 分层；数据层通过 Repository 接口隔离，提供 Mock 实现保证无后台时可运行；本地存储使用 DataStore + Room；AI/云端能力通过接口预留，默认 Mock 模拟完整流程。

**Tech Stack:** Kotlin 1.9, Jetpack Compose Material3, Navigation Compose, Hilt, Coroutines/Flow, Room, DataStore, Retrofit2, OkHttp, Kotlin Serialization, Coil, MediaExtractor/MediaCodec/MediaMuxer.

## Global Constraints

- 主色调蓝色 `#2196F3`，所有主操作按钮蓝色填充。
- 一级页面底部固定 4 项导航：首页 / AI云创 / AI去水印 / 我的。
- 二级页面顶部标配返回按钮。
- 所有展示类素材由后台下发，前端只读；默认使用 Mock 数据。
- 算力为账号维度独立数据，耗尽时禁用视频生成/下载，统一弹窗「当前算力已耗尽，请联系管理员增加算力额度」。
- AI 云创相册单次选择视频上限 10 个。
- 视频分割切片时长区间 0.5~10s，步长 0.1s，前端不可修改。
- 高危操作（删除、注销、退出登录）必须二次确认。
- minSdk 24, targetSdk 35, compileSdk 35, JDK 17, Gradle 8.9, AGP 8.7.

---

## File Structure

```
batch-video-editor/batch-video-editor/
├── app/src/main/java/com/batchvideo/app/
│   ├── App.kt
│   ├── di/
│   │   ├── NetworkModule.kt
│   │   ├── DatabaseModule.kt
│   │   ├── DataStoreModule.kt
│   │   └── RepositoryModule.kt
│   ├── ui/
│   │   ├── navigation/
│   │   │   ├── Screen.kt
│   │   │   ├── BottomNavBar.kt
│   │   │   └── BatchVideoNavHost.kt
│   │   ├── theme/
│   │   │   ├── Color.kt
│   │   │   ├── Type.kt
│   │   │   └── Theme.kt
│   │   ├── components/
│   │   │   ├── LoadingScreen.kt
│   │   │   ├── EmptyScreen.kt
│   │   │   ├── ErrorRetry.kt
│   │   │   ├── ConfirmDialog.kt
│   │   │   ├── PowerCheckBanner.kt
│   │   │   └── VideoPlayer.kt
│   │   ├── auth/
│   │   │   ├── LoginScreen.kt
│   │   │   ├── RegisterScreen.kt
│   │   │   └── AgreementScreen.kt
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── LearningScreen.kt
│   │   │   ├── ContactScreen.kt
│   │   │   ├── BrandScreen.kt
│   │   │   └── DocumentScreen.kt
│   │   ├── aicreation/
│   │   │   ├── AiCreationListScreen.kt
│   │   │   ├── AiCreationEditScreen.kt
│   │   │   └── SplitProgressScreen.kt
│   │   ├── watermark/
│   │   │   └── WatermarkScreen.kt
│   │   └── profile/
│   │       ├── ProfileScreen.kt
│   │       ├── EditProfileScreen.kt
│   │       └── CustomerServiceScreen.kt
│   ├── presentation/
│   │   ├── auth/AuthViewModel.kt
│   │   ├── home/HomeViewModel.kt
│   │   ├── aicreation/AiCreationViewModel.kt
│   │   ├── watermark/WatermarkViewModel.kt
│   │   └── profile/ProfileViewModel.kt
│   ├── domain/
│   │   ├── model/
│   │   │   ├── User.kt
│   │   │   ├── ComputePower.kt
│   │   │   ├── Banner.kt
│   │   │   ├── Announcement.kt
│   │   │   ├── NavItem.kt
│   │   │   ├── Material.kt
│   │   │   ├── Contact.kt
│   │   │   ├── Brand.kt
│   │   │   ├── Document.kt
│   │   │   ├── VideoGroup.kt
│   │   │   ├── Clip.kt
│   │   │   ├── Dubbing.kt
│   │   │   ├── Bgm.kt
│   │   │   └── WatermarkResult.kt
│   │   ├── repository/
│   │   │   ├── AuthRepository.kt
│   │   │   ├── HomeRepository.kt
│   │   │   ├── AiCreationRepository.kt
│   │   │   ├── WatermarkRepository.kt
│   │   │   └── ProfileRepository.kt
│   │   └── usecase/
│   │       ├── ValidatePhoneUseCase.kt
│   │       ├── CheckComputePowerUseCase.kt
│   │       └── SaveToGalleryUseCase.kt
│   └── data/
│       ├── local/
│       │   ├── AppDatabase.kt
│       │   ├── UserDao.kt
│       │   ├── VideoGroupDao.kt
│       │   └── CacheDataStore.kt
│       ├── remote/
│       │   ├── BatchVideoApiService.kt
│       │   ├── MockDataSource.kt
│       │   └── NetworkResult.kt
│       └── repository/
│           ├── AuthRepositoryImpl.kt
│           ├── HomeRepositoryImpl.kt
│           ├── AiCreationRepositoryImpl.kt
│           ├── WatermarkRepositoryImpl.kt
│           └── ProfileRepositoryImpl.kt
└── app/src/main/res/values/strings.xml
```

---

### Task 1: 初始化 Android 工程与 Gradle 配置

**Files:**
- Create: `batch-video-editor/build.gradle.kts`
- Create: `batch-video-editor/settings.gradle.kts`
- Create: `batch-video-editor/gradle.properties`
- Create: `batch-video-editor/gradle/wrapper/gradle-wrapper.properties`
- Create: `batch-video-editor/app/build.gradle.kts`
- Create: `batch-video-editor/app/src/main/AndroidManifest.xml`
- Create: `batch-video-editor/app/proguard-rules.pro`
- Modify: `D:/project/batch-video-editor/.gitignore`（添加 Android 忽略项）

**Interfaces:**
- Produces: 可运行的空 Android 工程，`./gradlew assembleDebug` 成功。

- [ ] **Step 1: 创建项目级 Gradle 文件**

`batch-video-editor/build.gradle.kts`:
```kotlin
plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24" apply false
}
```

`batch-video-editor/settings.gradle.kts`:
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "batch-video-editor"
include(":app")
```

`batch-video-editor/gradle/wrapper/gradle-wrapper.properties`:
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

- [ ] **Step 2: 创建 app 模块 build.gradle.kts**

`batch-video-editor/app/build.gradle.kts`:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.batchvideo.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.batchvideo.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

- [ ] **Step 3: 创建 AndroidManifest 与 Application 入口**

`batch-video-editor/app/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CALL_PHONE" />

    <application
        android:name=".App"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.batch-video-editor"
        android:usesCleartextTraffic="true">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.batch-video-editor">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 4: 验证构建**

Run: `cd D:/project/batch-video-editor/batch-video-editor && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
cd D:/project/batch-video-editor
git add batch-video-editor/
git commit -m "chore: init batch-video-editor Android project"
```

---

### Task 2: 创建主题、导航与基础组件

**Files:**
- Create: `batch-video-editor/app/src/main/java/com/batchvideo/app/ui/theme/Color.kt`
- Create: `batch-video-editor/app/src/main/java/com/batchvideo/app/ui/theme/Type.kt`
- Create: `batch-video-editor/app/src/main/java/com/batchvideo/app/ui/theme/Theme.kt`
- Create: `batch-video-editor/app/src/main/java/com/batchvideo/app/ui/navigation/Screen.kt`
- Create: `batch-video-editor/app/src/main/java/com/batchvideo/app/ui/navigation/BottomNavBar.kt`
- Create: `batch-video-editor/app/src/main/java/com/batchvideo/app/ui/navigation/BatchVideoNavHost.kt`
- Create: `batch-video-editor/app/src/main/java/com/batchvideo/app/ui/components/LoadingScreen.kt`
- Create: `batch-video-editor/app/src/main/java/com/batchvideo/app/ui/components/EmptyScreen.kt`
- Create: `batch-video-editor/app/src/main/java/com/batchvideo/app/ui/components/ErrorRetry.kt`
- Create: `batch-video-editor/app/src/main/java/com/batchvideo/app/ui/components/ConfirmDialog.kt`
- Create: `batch-video-editor/app/src/main/java/com/batchvideo/app/MainActivity.kt`
- Create: `batch-video-editor/app/src/main/java/com/batchvideo/app/App.kt`
- Create: `batch-video-editor/app/src/main/res/values/strings.xml`
- Modify: `batch-video-editor/app/src/main/res/values/themes.xml`

**Interfaces:**
- Consumes: Gradle 配置完成。
- Produces: 可运行的带主题、底部导航与占位页面的应用；`MainActivity` 使用 `BatchVideoNavHost`。

- [ ] **Step 1: 定义颜色与主题**

`Color.kt`:
```kotlin
package com.batchvideo.app.ui.theme

import androidx.compose.ui.graphics.Color

val PrimaryBlue = Color(0xFF2196F3)
val PrimaryBlueDark = Color(0xFF1976D2)
val ErrorRed = Color(0xFFE53935)
val BackgroundLight = Color(0xFFF5F5F5)
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF212121)
val TextSecondary = Color(0xFF757575)
```

`Theme.kt`:
```kotlin
@Composable
fun batch-video-editorTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = PrimaryBlue,
        onPrimary = Color.White,
        primaryContainer = PrimaryBlue.copy(alpha = 0.12f),
        error = ErrorRed,
        background = BackgroundLight,
        surface = SurfaceLight
    )
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
```

- [ ] **Step 2: 定义导航路由与底部导航栏**

`Screen.kt`:
```kotlin
sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Login : Screen("login", "登录")
    data object Register : Screen("register", "注册")
    data object Agreement : Screen("agreement", "用户协议")
    data object Privacy : Screen("privacy", "隐私政策")

    data object Home : Screen("home", "首页", Icons.Default.Home)
    data object Learning : Screen("learning", "学习专区")
    data object Contact : Screen("contact", "信息咨询")
    data object Brand : Screen("brand", "品牌专区")
    data object Document : Screen("document", "新手文档")

    data object AiCreation : Screen("aicreation", "AI云创", Icons.Default.AutoFixHigh)
    data object AiCreationEdit : Screen("aicreation_edit/{groupId}", "视频组编辑")
    data object SplitProgress : Screen("split_progress/{groupId}", "分割进度")

    data object Watermark : Screen("watermark", "AI去水印", Icons.Default.WaterDrop)

    data object Profile : Screen("profile", "我的", Icons.Default.Person)
    data object EditProfile : Screen("edit_profile", "编辑资料")
    data object CustomerService : Screen("customer_service", "联系客服")

    fun withArgs(vararg args: String): String = buildString {
        val segments = route.split("/")
        append(segments[0])
        args.forEach { append("/$it") }
    }
}

val bottomNavItems = listOf(Screen.Home, Screen.AiCreation, Screen.Watermark, Screen.Profile)
```

`BottomNavBar.kt`:
```kotlin
@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon ?: Icons.Default.Help, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
```

- [ ] **Step 3: 创建基础 UI 组件**

每个组件为独立文件，包含 `LoadingScreen`、`EmptyScreen`、`ErrorRetry(message, onRetry)`、`ConfirmDialog(title, text, onConfirm, onDismiss)`。

- [ ] **Step 4: 创建 NavHost 与 MainActivity**

`BatchVideoNavHost.kt` 初始化 NavHost，暂时为每个 Screen 挂一个占位 `Text`；`MainActivity` setContent 中设置 `batch-video-editorTheme { BatchVideoNavHost() }`。

- [ ] **Step 5: 验证运行**

Run: `./gradlew installDebug`
Expected: APK 安装到已连接设备，底部 4 个 Tab 可切换。

- [ ] **Step 6: Commit**

```bash
git add batch-video-editor/app/src/main/java/com/batchvideo/app/ui/ batch-video-editor/app/src/main/java/com/batchvideo/app/MainActivity.kt batch-video-editor/app/src/main/java/com/batchvideo/app/App.kt batch-video-editor/app/src/main/res/
git commit -m "feat: add theme, navigation and base components"
```

---

### Task 3: 数据层 - 模型、Repository 接口与 Mock 实现

**Files:**
- Create: `domain/model/*.kt`（User, ComputePower, Banner, Announcement, NavItem, Material, Contact, Brand, Document, VideoGroup, Clip, Dubbing, Bgm, WatermarkResult）
- Create: `domain/repository/AuthRepository.kt`
- Create: `domain/repository/HomeRepository.kt`
- Create: `domain/repository/AiCreationRepository.kt`
- Create: `domain/repository/WatermarkRepository.kt`
- Create: `domain/repository/ProfileRepository.kt`
- Create: `data/remote/BatchVideoApiService.kt`
- Create: `data/remote/MockDataSource.kt`
- Create: `data/remote/NetworkResult.kt`
- Create: `data/repository/AuthRepositoryImpl.kt`
- Create: `data/repository/HomeRepositoryImpl.kt`
- Create: `data/repository/AiCreationRepositoryImpl.kt`
- Create: `data/repository/WatermarkRepositoryImpl.kt`
- Create: `data/repository/ProfileRepositoryImpl.kt`

**Interfaces:**
- Consumes: 主题与导航已完成。
- Produces: 所有 Repository 接口可用，默认返回 Mock 数据；ViewModel 可开始消费。

- [ ] **Step 1: 定义领域模型**

所有模型使用 `@Serializable` data class，例如：
```kotlin
@Serializable
data class User(
    val id: String,
    val phone: String,
    val nickname: String,
    val avatarUrl: String?,
    val level: String, // branch / service / personal
    val parentPhone: String?,
    val vipExpireAt: String?,
    val token: String
)

@Serializable
data class ComputePower(val total: Int, val used: Int) {
    val percent: Int get() = if (total == 0) 0 else (used * 100 / total)
    val remaining: Int get() = total - used
}
```

- [ ] **Step 2: 定义 Repository 接口**

```kotlin
interface AuthRepository {
    suspend fun login(phone: String, password: String): Result<User>
    suspend fun register(parentPhone: String, phone: String, password: String): Result<User>
    suspend fun getAgreement(): Result<String>
    suspend fun getPrivacy(): Result<String>
}

interface HomeRepository {
    suspend fun getBanners(): Result<List<Banner>>
    suspend fun getAnnouncement(): Result<Announcement>
    suspend fun getNavItems(): Result<List<NavItem>>
    suspend fun getMaterials(category: String? = null, keyword: String? = null): Result<List<Material>>
    suspend fun getContacts(): Result<List<Contact>>
    suspend fun getBrands(): Result<List<Brand>>
    suspend fun getDocuments(category: String? = null): Result<List<Document>>
}
```

- [ ] **Step 3: 创建 Mock 数据源**

`MockDataSource.kt` 提供所有 Mock 数据，例如 banners、announcement、navItems、videoGroups、watermarkResult 等。

- [ ] **Step 4: 实现 Repository**

每个 Repository 实现注入 `BatchVideoApiService` 和 `MockDataSource`，默认使用 Mock 数据。示例：
```kotlin
class AuthRepositoryImpl @Inject constructor(
    private val mockDataSource: MockDataSource
) : AuthRepository {
    override suspend fun login(phone: String, password: String): Result<User> =
        mockDataSource.login(phone, password)
    // ...
}
```

- [ ] **Step 5: Commit**

```bash
git add batch-video-editor/app/src/main/java/com/batchvideo/app/domain/ batch-video-editor/app/src/main/java/com/batchvideo/app/data/
git commit -m "feat: add domain models and mock repositories"
```

---

### Task 4: 依赖注入与本地存储

**Files:**
- Create: `di/NetworkModule.kt`
- Create: `di/DatabaseModule.kt`
- Create: `di/DataStoreModule.kt`
- Create: `di/RepositoryModule.kt`
- Create: `data/local/AppDatabase.kt`
- Create: `data/local/UserDao.kt`
- Create: `data/local/VideoGroupDao.kt`
- Create: `data/local/CacheDataStore.kt`

**Interfaces:**
- Consumes: Repository 实现。
- Produces: Hilt 可注入所有依赖；DataStore 与 Room 可用。

- [ ] **Step 1: 创建 DataStore 与 Room**

`CacheDataStore` 提供 `userFlow`、`tokenFlow`、`saveUser()`、`clearUser()`。
`AppDatabase` 包含 `UserEntity`、`VideoGroupEntity` 及对应 DAO。

- [ ] **Step 2: 创建 Hilt Modules**

`RepositoryModule`:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    // ...
}
```

- [ ] **Step 3: 验证编译**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add batch-video-editor/app/src/main/java/com/batchvideo/app/di/ batch-video-editor/app/src/main/java/com/batchvideo/app/data/local/
git commit -m "feat: add DI modules and local storage"
```

---

### Task 5: UseCase 与工具类

**Files:**
- Create: `domain/usecase/ValidatePhoneUseCase.kt`
- Create: `domain/usecase/CheckComputePowerUseCase.kt`
- Create: `domain/usecase/SaveToGalleryUseCase.kt`
- Create: `utils/Extensions.kt`
- Create: `utils/PermissionUtils.kt`
- Create: `utils/FileUtils.kt`
- Create: `utils/VideoUtils.kt`
- Create: `utils/ToastUtils.kt`

**Interfaces:**
- Consumes: Repository、Context。
- Produces: 全局可用的业务校验与工具方法。

- [ ] **Step 1: 实现 UseCase**

```kotlin
class ValidatePhoneUseCase @Inject constructor() {
    operator fun invoke(phone: String): Boolean =
        phone.length == 11 && phone.all { it.isDigit() }
}

class CheckComputePowerUseCase @Inject constructor() {
    operator fun invoke(power: ComputePower): Boolean = power.remaining > 0
}
```

- [ ] **Step 2: 实现工具类**

`PermissionUtils` 封装 Android 13/10/低版本权限请求；`FileUtils` 提供缓存目录、URI 转路径；`VideoUtils` 使用 MediaMetadataRetriever 获取视频信息；`ToastUtils` 提供 Compose 侧 Toast/Snackbar 入口。

- [ ] **Step 3: Commit**

```bash
git add batch-video-editor/app/src/main/java/com/batchvideo/app/domain/usecase/ batch-video-editor/app/src/main/java/com/batchvideo/app/utils/
git commit -m "feat: add usecases and utils"
```

---

### Task 6: 登录/注册模块 UI 与 ViewModel

**Files:**
- Create: `presentation/auth/AuthViewModel.kt`
- Create: `ui/auth/LoginScreen.kt`
- Create: `ui/auth/RegisterScreen.kt`
- Create: `ui/auth/AgreementScreen.kt`
- Modify: `ui/navigation/BatchVideoNavHost.kt` 注册路由

**Interfaces:**
- Consumes: `AuthRepository`, `ValidatePhoneUseCase`, `CacheDataStore`。
- Produces: 完整登录/注册/协议页面；登录成功后跳转首页。

- [ ] **Step 1: 实现 AuthViewModel**

包含 `AuthUiState`、`AuthUiEvent`、`login()`、`register()`、`onPhoneChange()`、`onPasswordChange()`、`toggleAgreement()`。

- [ ] **Step 2: 实现 LoginScreen**

手机号输入框（数字键盘、11 位限制）、密码输入框（显隐切换）、协议勾选 + 蓝色链接、蓝色主按钮、错误提示。

- [ ] **Step 3: 实现 RegisterScreen**

上级手机号只读展示、手机号/密码/确认密码输入、协议勾选、注册按钮。

- [ ] **Step 4: 实现 AgreementScreen/PrivacyScreen**

使用 `Html` 或 `Text` 展示富文本，底部显示更新时间。

- [ ] **Step 5: 验证登录流程**

Run: `./gradlew installDebug`
Expected: 启动进入登录页，输入任意 11 位手机号 + 任意 6-20 位密码 + 勾选协议，点击登录进入首页。

- [ ] **Step 6: Commit**

```bash
git add batch-video-editor/app/src/main/java/com/batchvideo/app/ui/auth/ batch-video-editor/app/src/main/java/com/batchvideo/app/presentation/auth/ batch-video-editor/app/src/main/java/com/batchvideo/app/ui/navigation/BatchVideoNavHost.kt
git commit -m "feat: implement auth module"
```

---

### Task 7: 首页模块

**Files:**
- Create: `presentation/home/HomeViewModel.kt`
- Create: `ui/home/HomeScreen.kt`
- Create: `ui/home/LearningScreen.kt`
- Create: `ui/home/ContactScreen.kt`
- Create: `ui/home/BrandScreen.kt`
- Create: `ui/home/DocumentScreen.kt`
- Modify: `ui/navigation/BatchVideoNavHost.kt`

**Interfaces:**
- Consumes: `HomeRepository`。
- Produces: 首页 Banner、喜报、导航入口、学习/联系/品牌/文档子页面。

- [ ] **Step 1: 实现 HomeViewModel**

`HomeUiState` 包含 banners、announcement、navItems、loading、error；`HomeUiEvent` 处理刷新、搜索、点击进入。

- [ ] **Step 2: 实现 HomeScreen**

- 顶部 Banner 自动轮播（使用 HorizontalPager 或自定义）。
- 喜报卡片。
- 4 宫格功能入口。
- 教程文档入口列表。
- 下拉刷新（PullRefresh）。

- [ ] **Step 3: 实现子页面**

`LearningScreen`：Tab + 列表 + 搜索。
`ContactScreen`：电话列表 + 拨号 Intent。
`BrandScreen`：品牌列表 + 详情。
`DocumentScreen`：文档分类 + 列表 + 富文本详情。

- [ ] **Step 4: 验证首页**

Run: `./gradlew installDebug`
Expected: 首页加载 Mock 数据，各入口可进入，返回正常。

- [ ] **Step 5: Commit**

```bash
git add batch-video-editor/app/src/main/java/com/batchvideo/app/ui/home/ batch-video-editor/app/src/main/java/com/batchvideo/app/presentation/home/ batch-video-editor/app/src/main/java/com/batchvideo/app/ui/navigation/BatchVideoNavHost.kt
git commit -m "feat: implement home module"
```

---

### Task 8: AI 云创模块

**Files:**
- Create: `presentation/aicreation/AiCreationViewModel.kt`
- Create: `ui/aicreation/AiCreationListScreen.kt`
- Create: `ui/aicreation/AiCreationEditScreen.kt`
- Create: `ui/aicreation/SplitProgressScreen.kt`
- Create: `ui/components/PowerCheckBanner.kt`
- Create: `ui/components/VideoPlayer.kt`
- Modify: `ui/navigation/BatchVideoNavHost.kt`

**Interfaces:**
- Consumes: `AiCreationRepository`, `CheckComputePowerUseCase`, `SaveToGalleryUseCase`, `VideoUtils`。
- Produces: 视频组列表、编辑、分割进度、生成功能可用。

- [ ] **Step 1: 实现 AiCreationViewModel**

包含 `AiCreationUiState`（videoGroups, computePower, selectedGroup, clips, splitProgress, generating）、`loadGroups()`、`createGroup()`、`deleteGroup()`、`selectVideos()`、`split()`、`generate()`。

- [ ] **Step 2: 实现 AiCreationListScreen**

算力卡片、视频组列表（LazyColumn）、搜索框、长按排序（自定义）、删除二次确认、新增按钮。

- [ ] **Step 3: 实现 AiCreationEditScreen**

标题编辑、视频预览（`VideoPlayer`）、分镜头列表（编号、拖拽、镜像、替换、删除二次确认）、保留原声 Switch、AI 分割入口、配音/背景音乐入口、底部 AI 生成按钮。

- [ ] **Step 4: 实现 SplitProgressScreen**

进度条、当前视频名、总视频数、切片时长、完成后自动返回。

- [ ] **Step 5: 验证 AI 云创**

Run: `./gradlew installDebug`
Expected: 可新增视频组、进入编辑、模拟分割、模拟生成；算力耗尽时弹窗。

- [ ] **Step 6: Commit**

```bash
git add batch-video-editor/app/src/main/java/com/batchvideo/app/ui/aicreation/ batch-video-editor/app/src/main/java/com/batchvideo/app/presentation/aicreation/ batch-video-editor/app/src/main/java/com/batchvideo/app/ui/components/PowerCheckBanner.kt batch-video-editor/app/src/main/java/com/batchvideo/app/ui/components/VideoPlayer.kt batch-video-editor/app/src/main/java/com/batchvideo/app/ui/navigation/BatchVideoNavHost.kt
git commit -m "feat: implement AI creation module"
```

---

### Task 9: AI 去水印模块

**Files:**
- Create: `presentation/watermark/WatermarkViewModel.kt`
- Create: `ui/watermark/WatermarkScreen.kt`
- Modify: `ui/navigation/BatchVideoNavHost.kt`

**Interfaces:**
- Consumes: `WatermarkRepository`, `CheckComputePowerUseCase`, `SaveToGalleryUseCase`。
- Produces: 链接解析、结果展示、保存功能可用。

- [ ] **Step 1: 实现 WatermarkViewModel**

`WatermarkUiState` 包含 link、loading、result、selectedTab、error；`parse()`、`saveVideo()`、`saveImages()`。

- [ ] **Step 2: 实现 WatermarkScreen**

链接输入框、清空按钮、解析按钮、Tab（视频/图片/文本）、视频预览、图片网格、保存按钮。

- [ ] **Step 3: 验证去水印**

Run: `./gradlew installDebug`
Expected: 输入链接后解析出 Mock 结果，可切换 Tab，点击保存提示成功（使用本地占位文件或模拟）。

- [ ] **Step 4: Commit**

```bash
git add batch-video-editor/app/src/main/java/com/batchvideo/app/ui/watermark/ batch-video-editor/app/src/main/java/com/batchvideo/app/presentation/watermark/ batch-video-editor/app/src/main/java/com/batchvideo/app/ui/navigation/BatchVideoNavHost.kt
git commit -m "feat: implement watermark removal module"
```

---

### Task 10: 我的模块

**Files:**
- Create: `presentation/profile/ProfileViewModel.kt`
- Create: `ui/profile/ProfileScreen.kt`
- Create: `ui/profile/EditProfileScreen.kt`
- Create: `ui/profile/CustomerServiceScreen.kt`
- Modify: `ui/navigation/BatchVideoNavHost.kt`

**Interfaces:**
- Consumes: `ProfileRepository`, `CacheDataStore`, `AuthRepository`（退出/注销）。
- Produces: 个人中心、资料编辑、客服、退出/注销功能可用。

- [ ] **Step 1: 实现 ProfileViewModel**

`ProfileUiState` 包含 user、cacheSize、version；`logout()`、`clearCache()`、`checkUpdate()`。

- [ ] **Step 2: 实现 ProfileScreen**

头像、昵称、脱敏手机号、VIP 标识/有效期、服务列表、系统设置列表、退出登录按钮（蓝色主按钮）。

- [ ] **Step 3: 实现 EditProfileScreen**

头像选择（相册/相机）、昵称输入、手机号展示。

- [ ] **Step 4: 实现 CustomerServiceScreen**

客服电话、服务时段、在线客服入口。

- [ ] **Step 5: 验证我的模块**

Run: `./gradlew installDebug`
Expected: 个人中心显示 Mock 用户信息，退出登录二次确认后返回登录页。

- [ ] **Step 6: Commit**

```bash
git add batch-video-editor/app/src/main/java/com/batchvideo/app/ui/profile/ batch-video-editor/app/src/main/java/com/batchvideo/app/presentation/profile/ batch-video-editor/app/src/main/java/com/batchvideo/app/ui/navigation/BatchVideoNavHost.kt
git commit -m "feat: implement profile module"
```

---

### Task 11: 本地视频处理与权限完善

**Files:**
- Create: `utils/VideoProcessor.kt`
- Create: `utils/PermissionHandler.kt`
- Modify: 各 Screen 中接入权限请求

**Interfaces:**
- Consumes: Context, MediaExtractor, MediaCodec, MediaMuxer。
- Produces: 视频选择、信息读取、简单裁剪、保存相册可用。

- [ ] **Step 1: 实现视频信息读取**

使用 `MediaMetadataRetriever` 获取时长、宽高、旋转、缩略图。

- [ ] **Step 2: 实现保存到相册**

`SaveToGalleryUseCase` 使用 `MediaStore` 插入视频/图片，适配 Android 10+ Scoped Storage。

- [ ] **Step 3: 接入权限处理**

在 AI 云创、去水印、编辑资料页面使用 `rememberLauncherForActivityResult` 请求权限。

- [ ] **Step 4: Commit**

```bash
git add batch-video-editor/app/src/main/java/com/batchvideo/app/utils/VideoProcessor.kt batch-video-editor/app/src/main/java/com/batchvideo/app/utils/PermissionHandler.kt
git commit -m "feat: add video processing and permission handling"
```

---

### Task 12: 真机构建与冒烟测试

**Files:**
- Create: `batch-video-editor/app/src/test/...`（可选 ViewModel 单元测试）
- Modify: 修复测试中发现的问题

**Interfaces:**
- Consumes: 完整 APP 代码。
- Produces: 可在真机运行的 APK；问题清单与修复。

- [ ] **Step 1: 运行单元测试**

Run: `./gradlew test`
Expected: 至少 ExampleUnitTest 通过；如有 ViewModel 测试，全部通过。

- [ ] **Step 2: 构建 Debug APK**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL，APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

- [ ] **Step 3: 安装到真机**

Run: `adb devices` 确认手机已连接。
Run: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
Expected: Success。

- [ ] **Step 4: 冒烟测试**

逐项验证：
1. 启动 Logo/登录页显示正常；
2. 登录（任意 11 位手机号 + 密码 + 勾选协议）进入首页；
3. 底部 4 个 Tab 切换正常；
4. 首页 Banner 自动轮播、喜报、功能入口点击进入与返回；
5. AI 云创新增/编辑/删除/生成流程；
6. AI 去水印解析与保存；
7. 我的页资料、客服、退出登录。

- [ ] **Step 5: 修复问题并提交**

每修复一个 bug 提交一次：
```bash
git add .
git commit -m "fix: [问题描述]"
```

- [ ] **Step 6: 最终 Commit**

```bash
git commit -m "chore: finish app smoke test on device"
```

---

## Self-Review

1. **Spec coverage**: 登录/注册、首页及子页面、AI 云创、AI 去水印、我的模块均有对应 Task；全局约束在每个 Task 中通过组件/UseCase 落实。
2. **Placeholder scan**: 无 TBD/TODO；所有步骤包含具体文件、命令与预期结果。
3. **Type consistency**: 所有 Repository 接口与实现命名一致；Screen 路由命名统一使用驼峰 data object；UiState/UiEvent 模式贯穿所有 ViewModel。

---

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-07-03-batchvideo-app-implementation.md`.**

**Execution approach:** Subagent-Driven (recommended) — dispatch fresh subagents per Task/module, review between tasks, fast iteration. REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`.

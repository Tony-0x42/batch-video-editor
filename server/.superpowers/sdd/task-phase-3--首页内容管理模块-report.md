# Phase 3: 首页内容管理模块 实施报告

## 状态

- **状态**：已完成 ✅
- **commit SHA**：`c972decfdec0f92393e1ca7eddba6937d9c580ce`
- **分支**：`main`
- **提交信息**：`feat(Phase 3: 首页内容管理模块): 实现轮播图/喜报/功能入口/教程入口的后端CRUD与前端管理页`
- **已推送**：是（`git push` 成功）

## 完成内容

### 后端（ruoyi-system）

在 `com.ruoyi.batch.home` 包下完成了 4 个子模块的完整后端实现：

| 子模块 | 实体 | Mapper | Service | ServiceImpl | Controller | Mapper XML |
|---|---|---|---|---|---|---|
| 轮播图 | `BatchHomeBanner` | `BatchHomeBannerMapper` | `IBatchHomeBannerService` | `BatchHomeBannerServiceImpl` | `BatchHomeBannerController` | `BatchHomeBannerMapper.xml` |
| 喜报数据 | `BatchHomeNews` | `BatchHomeNewsMapper` | `IBatchHomeNewsService` | `BatchHomeNewsServiceImpl` | `BatchHomeNewsController` | `BatchHomeNewsMapper.xml` |
| 功能入口 | `BatchHomeEntry` | `BatchHomeEntryMapper` | `IBatchHomeEntryService` | `BatchHomeEntryServiceImpl` | `BatchHomeEntryController` | `BatchHomeEntryMapper.xml` |
| 教程入口 | `BatchHomeTutorialEntry` | `BatchHomeTutorialEntryMapper` | `IBatchHomeTutorialEntryService` | `BatchHomeTutorialEntryServiceImpl` | `BatchHomeTutorialEntryController` | `BatchHomeTutorialEntryMapper.xml` |

额外新增：

- `BatchHomeDocumentOption`：教程入口弹窗中关联文档下拉选项的 VO。
- 教程入口 Mapper 中通过 `LEFT JOIN batch_document` 回显关联文档标题。

### 接口清单

统一前缀 `/batch/home/...`，权限字符串 `batch:home:...`：

- `GET /batch/home/banner/list`、`/batch/home/news/list`、`/batch/home/entry/list`、`/batch/home/tutorialEntry/list`
- `POST /batch/home/banner/export`、`/batch/home/news/export`、`/batch/home/entry/export`、`/batch/home/tutorialEntry/export`
- `GET /batch/home/banner/{id}` 等详情接口
- `POST /batch/home/banner` 等新增接口
- `PUT /batch/home/banner` 等修改接口
- `PUT /batch/home/banner/changeStatus` 等状态切换接口
- `DELETE /batch/home/banner/{ids}` 等删除接口
- `GET /batch/home/tutorialEntry/documentList`：获取可关联的文档列表

### 前端（ruoyi-ui）

- 新增 API：`ruoyi-ui/src/api/batch/home.js`
- 新增页面：`ruoyi-ui/src/views/batch/home/index.vue`
  - 顶部 Tab 切换：轮播图 / 喜报数据 / 功能入口 / 教程入口
  - 每个 Tab 支持：搜索、新增、编辑、删除二次确认、状态开关、导出、分页
  - 轮播图 / 功能入口 / 教程入口支持上下移动排序
  - 复用全局组件：`ImageUpload`、`Pagination`、`RightToolbar`
  - 教程入口弹窗通过 `listDocumentOption` 拉取文档列表
  - 按钮权限使用 `v-hasPermi="['batch:home:add']"` 等

## 修改文件清单

本次提交共 27 个文件：

```
ruoyi-system/src/main/java/com/ruoyi/batch/home/controller/BatchHomeBannerController.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/controller/BatchHomeEntryController.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/controller/BatchHomeNewsController.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/controller/BatchHomeTutorialEntryController.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/domain/BatchHomeBanner.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/domain/BatchHomeDocumentOption.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/domain/BatchHomeEntry.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/domain/BatchHomeNews.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/domain/BatchHomeTutorialEntry.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/mapper/BatchHomeBannerMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/mapper/BatchHomeEntryMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/mapper/BatchHomeNewsMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/mapper/BatchHomeTutorialEntryMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/service/IBatchHomeBannerService.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/service/IBatchHomeEntryService.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/service/IBatchHomeNewsService.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/service/IBatchHomeTutorialEntryService.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/service/impl/BatchHomeBannerServiceImpl.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/service/impl/BatchHomeEntryServiceImpl.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/service/impl/BatchHomeNewsServiceImpl.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/service/impl/BatchHomeTutorialEntryServiceImpl.java
ruoyi-system/src/main/resources/mapper/batch/home/BatchHomeBannerMapper.xml
ruoyi-system/src/main/resources/mapper/batch/home/BatchHomeEntryMapper.xml
ruoyi-system/src/main/resources/mapper/batch/home/BatchHomeNewsMapper.xml
ruoyi-system/src/main/resources/mapper/batch/home/BatchHomeTutorialEntryMapper.xml
ruoyi-ui/src/api/batch/home.js
ruoyi-ui/src/views/batch/home/index.vue
```

## 测试 / 检查结果

- **Java 编译**：未执行。当前环境 JDK 为 1.8，项目要求 Java 17（`pom.xml` 中 `<java.version>17</java.version>`），本地无法编译验证。
- **后端静态检查**：
  - 代码沿用项目现有 RuoYi 结构：`@RestController`、`@PreAuthorize("@ss.hasPermi('batch:home:xxx')")`、`@Log` 等用法与现有 `SysJobController` 一致。
  - 使用 `jakarta.servlet.http.HttpServletResponse` 与 Spring Boot 3 对齐。
  - `del_flag` 删除约定统一为 `2`；状态 `0=启用/上架`，`1=禁用/下架`。
  - MyBatis XML 已配置 `useGeneratedKeys="true" keyProperty="xxxId"`。
- **前端静态检查**：
  - `node --input-type=module --check < src/api/batch/home.js` 通过。
  - `git diff --check` 无空白错误。
  - 页面复用 `ImageUpload`、`Pagination`、`RightToolbar` 等全局组件；权限指令使用 `v-hasPermi`。
- **运行测试**：因无 MySQL / 前端运行环境，未进行实际运行测试。

## 遇到的问题

1. **JDK 版本不匹配**：本地 JDK 1.8 无法编译 Java 17 项目，因此未执行 `mvn clean package`。需要 Java 17 环境才能做编译与后端接口验证。
2. **无运行时环境**：没有可用的 MySQL 与前端 dev server，无法实际联调接口与页面。
3. **并发写入风险**：实施过程中发现 `BatchHomeBanner*` 占位文件曾被还原为初始占位内容（可能是其他 Phase 子代理并发操作导致），已重新写入并立即提交，避免再次冲突。

## 备注

- 所有新增接口 URL 均按规范以 `/batch/home/...` 开头。
- 权限字符串严格使用 `batch:home:list/add/edit/remove/export/query`。
- 菜单与按钮权限已在 `sql/batch_menu.sql` 中预先配置，本模块未重复创建菜单。

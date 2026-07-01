# Phase 5: 教程管理模块 实施报告

## 状态

- [x] 已完成
- commit SHA: `86ccf6e`
- 推送分支: `main`

## 实施范围

按照《批量剪辑 管理后台全模块实施计划》Phase 5 要求，完成「教程管理模块」的后端接口与前端页面，覆盖：

1. 教程管理：列表查询、新增、编辑、删除（逻辑删除）、状态切换（上架/下架）、导出。
2. 教程分类管理：列表查询、新增、编辑、删除，删除前校验是否有关联教程。
3. 视频/图文双类型支持：视频教程上传视频文件，图文教程使用富文本编辑器。
4. 复用 RuoYi 现有组件：`ImageUpload`、`FileUpload`、`Editor`、`Pagination`、`RightToolbar` 等。

## 新增/修改文件清单

### 后端 Java（ruoyi-system）

| 路径 | 说明 |
| --- | --- |
| `ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/domain/BatchTutorial.java` | 教程实体（覆盖占位类） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/domain/BatchTutorialCategory.java` | 教程分类实体（新增） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/mapper/BatchTutorialMapper.java` | 教程 Mapper 接口（覆盖占位类） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/mapper/BatchTutorialCategoryMapper.java` | 教程分类 Mapper 接口（新增） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/service/IBatchTutorialService.java` | 教程 Service 接口（覆盖占位类） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/service/IBatchTutorialCategoryService.java` | 教程分类 Service 接口（新增） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/service/impl/BatchTutorialServiceImpl.java` | 教程 Service 实现（覆盖占位类） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/service/impl/BatchTutorialCategoryServiceImpl.java` | 教程分类 Service 实现（新增） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/controller/BatchTutorialController.java` | 教程管理 Controller（覆盖占位类） |
| `ruoyi-system/src/main/resources/mapper/batch/tutorial/BatchTutorialMapper.xml` | 教程 SQL（覆盖占位文件） |
| `ruoyi-system/src/main/resources/mapper/batch/tutorial/BatchTutorialCategoryMapper.xml` | 教程分类 SQL（新增） |

### 前端 Vue（ruoyi-ui）

| 路径 | 说明 |
| --- | --- |
| `ruoyi-ui/src/api/batch/tutorial.js` | 教程与分类 API 封装（新增） |
| `ruoyi-ui/src/views/batch/tutorial/index.vue` | 教程管理页面（新增）：列表、搜索、新增/编辑弹窗、详情预览、分类管理抽屉 |

## 接口清单

统一前缀 `/batch/tutorial`，权限字符串遵循 `batch:tutorial:操作`。

| 方法 | URL | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/batch/tutorial/list` | `batch:tutorial:list` | 分页查询教程列表 |
| POST | `/batch/tutorial/export` | `batch:tutorial:export` | 导出教程列表 |
| GET | `/batch/tutorial/{tutorialId}` | `batch:tutorial:query` | 查询教程详情 |
| POST | `/batch/tutorial` | `batch:tutorial:add` | 新增教程 |
| PUT | `/batch/tutorial` | `batch:tutorial:edit` | 修改教程 |
| PUT | `/batch/tutorial/changeStatus` | `batch:tutorial:edit` | 修改教程状态（上架/下架） |
| DELETE | `/batch/tutorial/{tutorialIds}` | `batch:tutorial:remove` | 删除教程（逻辑删除） |
| GET | `/batch/tutorial/category/list` | `batch:tutorial:list` | 分页查询分类列表 |
| GET | `/batch/tutorial/category/all` | `batch:tutorial:list` | 查询所有启用分类 |
| GET | `/batch/tutorial/category/{categoryId}` | `batch:tutorial:query` | 查询分类详情 |
| POST | `/batch/tutorial/category` | `batch:tutorial:add` | 新增分类 |
| PUT | `/batch/tutorial/category` | `batch:tutorial:edit` | 修改分类 |
| DELETE | `/batch/tutorial/category/{categoryIds}` | `batch:tutorial:remove` | 删除分类（删除前校验无关联教程） |

> 封面图与视频文件上传复用系统通用上传接口 `/common/upload`，由 `ImageUpload` / `FileUpload` 组件调用，无需单独实现上传接口。

## 关键实现说明

1. **逻辑删除**：教程与分类表均包含 `del_flag` 字段，删除操作更新 `del_flag = 2`，列表查询均过滤 `del_flag = 0`，与现有文档管理模块保持一致。
2. **分类删除校验**：`BatchTutorialCategoryServiceImpl.deleteCategoryById/ByIds` 调用 `BatchTutorialMapper.countByCategoryId` 统计该分类下未删除的教程数量，大于 0 时抛出 `ServiceException`。
3. **动态表单**：前端新增/编辑弹窗根据 `tutorialType`（1 视频 / 2 图文）动态显示「视频上传」或「图文富文本」区域，切换类型时自动清空另一类型字段，避免脏数据。
4. **状态切换二次确认**：表格内 `el-switch` 切换上架/下架状态时，弹出确认框，取消则回滚状态。
5. **删除二次确认**：教程删除与分类删除均调用 `this.$modal.confirm` 进行二次确认，符合全局约束。

## 测试/检查结果

### 静态检查

- 已对照 `SysNoticeController` / `SysConfigController` 校验 Controller 接口风格、权限注解、`BaseController` 方法调用。
- 已对照 `BatchDocumentMapper.xml` 校验 XML 中 `del_flag` 过滤、逻辑删除、`<trim>` 语法。
- 已确认 `jakarta.validation`、`jakarta.servlet` 等包与项目现有版本一致。
- 已确认 `ImageUpload`、`FileUpload`、`Editor` 为全局注册组件，可直接在页面使用。
- 已确认菜单权限 `batch:tutorial:list/add/edit/remove/export/query` 已在 `sql/batch_menu.sql` 中配置。

### 未能执行的检查

- **后端编译**：当前环境 JDK 为 1.8，项目使用 `jakarta.*` 包需 Java 17，执行 `mvn clean package` 会因 JDK 版本不匹配而失败，未进行本地编译验证。
- **前端运行**：未安装 Node / 未启动前端服务，未进行页面实际访问验证。
- **接口联调**：无 MySQL/Redis 运行环境，未进行接口调用测试。

## 遇到的问题

1. **环境 JDK 与项目要求不一致**：本地 JDK 1.8 无法编译使用 Jakarta EE 命名空间的代码。已尽量保证语法与现有模块一致，但无法做编译级验证。建议切换到 Java 17 环境后执行 `mvn clean package -DskipTests` 进行验证。
2. **工作区存在其他模块未提交的变更**：`git status` 显示 customer/home/statistics 等模块也有大量改动。为避免提交他人未完成的工作，本次仅 `git add` 了 tutorial 模块相关文件，commit 范围已做隔离。
3. **报告文件命名模板歧义**：`.superpowers/sdd/` 下已有 `task-phase-2--...`、`task-phase6-report.md`、`task-phase8-system-configuration-module-report.md` 等多种命名。本报告按与 Phase 2/4 相近的风格命名为 `task-phase-5--教程管理模块-report.md`。

## 下一步建议

1. 在 Java 17 环境下执行 `mvn clean package -DskipTests`，修复可能的编译错误。
2. 启动 MySQL/Redis，执行 `sql/batch_business.sql` 与 `sql/batch_menu.sql`，确认表结构与菜单权限。
3. 启动前后端，admin 登录后访问「批量剪辑管理 → 教程管理」，验证列表、新增、编辑、删除、状态切换、分类管理抽屉、详情预览等功能。

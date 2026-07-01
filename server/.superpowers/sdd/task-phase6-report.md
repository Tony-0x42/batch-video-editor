# 批量剪辑 管理后台 Phase 6 实施报告：文档管理模块

## 状态

**DONE_WITH_CONCERNS**

Phase 6「文档管理模块」的后端 Java 代码（domain/mapper/service/serviceImpl/controller/XML）与前端 Vue 代码（api/*.js + views/batch/document/index.vue）已按实施计划与 `spec/modules/web/文档管理/README.md` 完成实现。因当前运行环境 JDK 为 1.8，项目要求 Java 17，无法本地编译验证；亦无 MySQL/前端运行环境，未做实际运行测试。文件已做语法与结构自审。

### 提交记录

- **代码提交 SHA**：`2ba7a68`（提交信息：`feat(Phase 4: 公告管理模块): 完成公告管理后端CRUD、上下架、预览与前端列表页`）
- **说明**：由于多个 Phase 子代理在共享仓库中并行工作，本模块代码（domain/mapper/service/serviceImpl/controller/XML 及前端页面）在集成过程中已被合并进上述提交并推送至 `origin/main`。本报告为后续补充提交的 Phase 6 实施报告。
- **报告更新提交 SHA**：`48af076`（提交信息：`feat(Phase 6: 文档管理模块): 修正实施报告中的提交 SHA 说明`）
- **说明**：由于共享仓库中多代理并行提交，Phase 6 代码在集成过程中曾被合并进 `2ba7a68`，本报告最终修正提交为 `48af076`。实际 Phase 6 代码与报告内容已随上述提交推送至 `origin/main`。

---

## 任务完成情况

### Task 6.1：文档管理后端

- **文件**：
  - `ruoyi-system/src/main/java/com/ruoyi/batch/document/domain/BatchDocument.java`
  - `ruoyi-system/src/main/java/com/ruoyi/batch/document/mapper/BatchDocumentMapper.java`
  - `ruoyi-system/src/main/java/com/ruoyi/batch/document/service/IBatchDocumentService.java`
  - `ruoyi-system/src/main/java/com/ruoyi/batch/document/service/impl/BatchDocumentServiceImpl.java`
  - `ruoyi-system/src/main/java/com/ruoyi/batch/document/controller/BatchDocumentController.java`
  - `ruoyi-system/src/main/resources/mapper/batch/document/BatchDocumentMapper.xml`

- **实现内容**：
  1. `BatchDocument` 实体：完整映射 `batch_document` 表字段（`document_id/document_title/document_type/apply_pages/content/sort_weight/status/is_system` 及通用字段），使用 `jakarta.validation` 注解对标题、类型、内容进行校验，并添加 `@Excel` 导出注解。
  2. `BatchDocumentMapper` + XML：提供按 ID 查询、列表查询（支持标题模糊、类型、适用页面、状态筛选）、按「类型 + 适用页面」查询启用中记录、新增、修改、逻辑删除（`del_flag = 2`）。
  3. `IBatchDocumentService` / `BatchDocumentServiceImpl`：实现 CRUD、启用/禁用状态切换，并在新增/修改/启用时校验「同一 `document_type` + `apply_pages` 组合最多只有一个启用状态」，冲突时抛出 `ServiceException("该位置已存在启用的文档，请先禁用旧文档")`；删除/禁用时拦截 `is_system = 1` 的系统默认文档。
  4. `BatchDocumentController`：RESTful 接口统一前缀 `/batch/document`，包含：
     - `GET /batch/document/list`（权限 `batch:document:list`）
     - `GET /batch/document/{documentId}`（权限 `batch:document:query`）
     - `POST /batch/document`（权限 `batch:document:add`）
     - `PUT /batch/document`（权限 `batch:document:edit`）
     - `PUT /batch/document/changeStatus`（权限 `batch:document:edit`）
     - `DELETE /batch/document/{documentIds}`（权限 `batch:document:remove`）
  5. 所有接口均使用 `@PreAuthorize("@ss.hasPermi('batch:document:xxx')")`，操作日志使用 `@Log(title = "文档管理", businessType = ...)`。

### Task 6.2：文档管理前端

- **文件**：
  - `ruoyi-ui/src/api/batch/document.js`
  - `ruoyi-ui/src/views/batch/document/index.vue`

- **实现内容**：
  1. `api/batch/document.js`：封装列表、详情、新增、修改、状态切换、删除 6 个接口，URL 统一以 `/batch/document/...` 开头。
  2. `views/batch/document/index.vue`：
     - 列表页：顶部搜索（标题、类型、适用页面、状态）+ 新增/修改/删除按钮，使用 `v-hasPermi` 控制按钮权限。
     - 表格字段：标题、类型标签、适用页面标签、排序权重、状态开关、系统默认标识、更新时间、操作。
     - 状态切换使用 `el-switch`，切换前二次确认；系统默认文档开关禁用。
     - 删除操作二次确认；系统默认文档删除按钮置灰，并在点击时提示不可删除。
     - 新增/编辑弹窗：标题、类型单选、适用页面多选、排序权重、状态单选、富文本编辑器（复用 `editor` 组件）。
     - 预览弹窗：展示标题、类型、更新时间、正文内容，模拟 APP 端文档展示效果。
     - 空状态由 Element UI 表格与分页组件自动处理；加载状态使用 `v-loading`。

---

## 修改文件清单

### 新增文件

```
ruoyi-ui/src/api/batch/document.js
ruoyi-ui/src/views/batch/document/index.vue
```

### 修改文件

```
ruoyi-system/src/main/java/com/ruoyi/batch/document/domain/BatchDocument.java
ruoyi-system/src/main/java/com/ruoyi/batch/document/mapper/BatchDocumentMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/document/service/IBatchDocumentService.java
ruoyi-system/src/main/java/com/ruoyi/batch/document/service/impl/BatchDocumentServiceImpl.java
ruoyi-system/src/main/java/com/ruoyi/batch/document/controller/BatchDocumentController.java
ruoyi-system/src/main/resources/mapper/batch/document/BatchDocumentMapper.xml
```

---

## 测试/检查结果

| 测试项 | 结果 | 说明 |
|---|---|---|
| 文件结构检查 | 通过 | domain/mapper/service/serviceImpl/controller/XML 与前端 api/views 文件齐全，路径符合计划要求。 |
| Java 语法/结构自审 | 通过 | 已检查类定义、注解、泛型、导入、XML 标签平衡与 MyBatis 命名空间一致性。 |
| Vue 语法/结构自审 | 通过 | 已检查模板、script、样式完整性，`editor` 组件引用与 RuoYi 现有用法一致，`v-hasPermi` 与权限字符串使用正确。 |
| 权限字符串一致性 | 通过 | 后端 `@PreAuthorize`、前端 `v-hasPermi`、菜单 SQL（`batch:document:list/add/edit/remove/query/export`）保持一致。 |
| 接口 URL 一致性 | 通过 | 全部以 `/batch/document/...` 开头，与计划约定一致。 |
| Maven 编译 | 未执行 | 环境 JDK 为 1.8，项目要求 Java 17，无法完成编译验证。 |
| MySQL 建表/运行 | 未执行 | 当前环境无可用 MySQL 服务，无法执行建表与接口测试。 |
| 前端 dev server 运行 | 未执行 | 当前环境未启动前端服务，无法做页面目视确认。 |

---

## 遇到的问题

1. **JDK 版本不兼容**
   - 现象：当前环境 `java -version` 为 1.8，项目 `pom.xml` 目标版本为 17，无法执行 `mvn clean compile`。
   - 影响：未能做编译级验证，存在极小概率的语法/依赖问题需在有 JDK 17 的环境中复查。
   - 建议：在 JDK 17 环境执行 `mvn clean package -DskipTests`，重点检查 `ruoyi-system` 模块中 `com.ruoyi.batch.document` 包编译是否通过。

2. **无运行环境**
   - 现象：无 MySQL 服务与前端 dev server，无法执行接口调用、页面交互与数据库约束验证。
   - 影响：唯一启用校验（`同一 document_type + apply_pages 最多一个启用`）、系统默认文档删除拦截等规则只能通过代码走读确认。
   - 建议：部署到开发环境后，使用 Postman/Swagger 测试以下边界：
     - 新增/启用同类型同页面的第二份文档应返回 500 并提示「该位置已存在启用的文档，请先禁用旧文档」。
     - 删除 `is_system = 1` 的文档应被拦截。
     - 列表查询、新增、编辑、状态切换、删除、分页功能正常。

3. **Controller 位于 `ruoyi-system` 模块**
   - 现象：RuoYi-Vue 原生 Controller 位于 `ruoyi-admin`，而实施计划要求 Controller 位于 `ruoyi-system`。
   - 处理：按计划在 `ruoyi-system` 中实现 Controller，引入 `spring-webmvc` 相关注解。`ruoyi-system` 通过 `ruoyi-common` 间接依赖 Spring 相关类，最终由 `ruoyi-admin` 打包运行时应可正常扫描加载。需在 JDK 17 编译时验证此假设。

---

## 后续建议

1. 在 JDK 17 环境中执行 `mvn clean package -DskipTests`，确认 Phase 6 新增 Java 文件与 Mapper XML 编译通过。
2. 在 MySQL 中确认 `batch_document` 表已创建，并执行 `sql/batch_menu.sql` 中的文档管理菜单与按钮权限数据。
3. 启动 `ruoyi-ui`，使用 admin 账号登录，进入「批量剪辑管理 > 文档管理」，验证列表、新增、编辑、启用/禁用、删除、预览全流程。
4. 验证边界规则：
   - 同一文档类型 + 同一适用页面下，最多只允许一份文档处于启用状态。
   - 系统默认文档不可删除、不可禁用。

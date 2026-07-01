# Phase 8: 系统配置模块 实施报告

## 状态

- [x] 已完成
-  commit SHA: `2c04191`
-  推送分支: `main`

## 实施范围

按照《批量剪辑 管理后台全模块实施计划》Phase 8 要求，完成「系统配置模块」的后端接口与前端页面，覆盖：

1. 品牌配置（APP Logo、后台 Logo、产品名称、Slogan、主色调、登录页背景图）
2. 全局参数（AI 云创视频上限、切片时长区间/步长、算力不足提示、链接解析失败提示、空状态占位图、客服服务时段）
3. APP 版本管理（版本号、平台、更新类型、更新内容、下载链接、发布时间、状态）

## 新增/修改文件清单

### 后端 Java（ruoyi-system）

| 路径 | 说明 |
| --- | --- |
| `ruoyi-system/src/main/java/com/ruoyi/batch/config/domain/BatchSystemConfig.java` | 扩展全局参数实体（覆盖占位类） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/config/domain/BatchAppVersion.java` | APP 版本管理实体 |
| `ruoyi-system/src/main/java/com/ruoyi/batch/config/mapper/BatchSystemConfigMapper.java` | 扩展全局参数 Mapper 接口（覆盖占位类） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/config/mapper/BatchAppVersionMapper.java` | APP 版本管理 Mapper 接口 |
| `ruoyi-system/src/main/java/com/ruoyi/batch/config/service/IBatchSystemConfigService.java` | 扩展全局参数 Service 接口（覆盖占位类） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/config/service/IBatchAppVersionService.java` | APP 版本管理 Service 接口 |
| `ruoyi-system/src/main/java/com/ruoyi/batch/config/service/impl/BatchSystemConfigServiceImpl.java` | 扩展全局参数 Service 实现（覆盖占位类） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/config/service/impl/BatchAppVersionServiceImpl.java` | APP 版本管理 Service 实现 |
| `ruoyi-system/src/main/java/com/ruoyi/batch/config/controller/BatchConfigController.java` | 系统配置 Controller（覆盖占位类） |
| `ruoyi-system/src/main/resources/mapper/batch/config/BatchSystemConfigMapper.xml` | 扩展全局参数 SQL（覆盖占位文件） |
| `ruoyi-system/src/main/resources/mapper/batch/config/BatchAppVersionMapper.xml` | APP 版本管理 SQL |

### 前端 Vue（ruoyi-ui）

| 路径 | 说明 |
| --- | --- |
| `ruoyi-ui/src/api/batch/config.js` | 系统配置 API 封装 |
| `ruoyi-ui/src/views/batch/config/index.vue` | 系统配置页面（Tab：品牌配置/版本管理/全局参数） |

## 接口清单

统一前缀 `/batch/config`，权限字符串遵循 `batch:config:操作`。

| 方法 | URL | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/batch/config/brand` | `batch:config:list` | 查询品牌配置 |
| POST | `/batch/config/brand` | `batch:config:edit` | 保存品牌配置 |
| GET | `/batch/config/global` | `batch:config:list` | 查询全局参数 |
| POST | `/batch/config/global` | `batch:config:edit` | 保存全局参数 |
| POST | `/batch/config/initGlobal` | `batch:config:add` | 恢复全局参数默认值 |
| GET | `/batch/config/version/list` | `batch:config:list` | 版本列表（分页） |
| GET | `/batch/config/version/export` | `batch:config:export` | 导出版本列表 |
| GET | `/batch/config/version/{versionId}` | `batch:config:query` | 版本详情 |
| POST | `/batch/config/version` | `batch:config:add` | 新增版本 |
| PUT | `/batch/config/version` | `batch:config:edit` | 修改版本 |
| DELETE | `/batch/config/version/{versionIds}` | `batch:config:remove` | 删除版本（逻辑删除） |
| PUT | `/batch/config/version/changeStatus` | `batch:config:edit` | 版本状态切换 |
| GET | `/batch/config/list` | `batch:config:list` | 扩展全局参数列表（分页） |
| GET | `/batch/config/{configId}` | `batch:config:query` | 扩展全局参数详情 |
| POST | `/batch/config` | `batch:config:add` | 新增扩展全局参数 |
| PUT | `/batch/config` | `batch:config:edit` | 修改扩展全局参数 |
| DELETE | `/batch/config/{configIds}` | `batch:config:remove` | 删除扩展全局参数 |

## 关键设计说明

1. **配置存储模型**
   - 品牌配置与全局参数均通过 `batch_system_config` 表的 `config_key` / `config_value` / `config_group` 存储。
   - 品牌配置键前缀：`batch.brand.*`，分组 `brand`。
   - 全局参数键前缀：`batch.ai.*`、`batch.computing.*`、`batch.link.*`、`batch.global.*`，分组 `global`。
   - 前端通过统一的 Map 表单提交，后端按分组批量保存（存在则更新，不存在则插入）。

2. **版本管理**
   - 使用独立表 `batch_app_version`。
   - 删除为逻辑删除（`del_flag = 2`），符合全局约束。
   - 新增/编辑时校验同一平台下版本号唯一。

3. **全局参数默认值**
   - `batch.ai.maxVideos = 10`
   - `batch.ai.sliceMin = 0.5`
   - `batch.ai.sliceMax = 10`
   - `batch.ai.sliceStep = 0.1`
   - `batch.computing.emptyTip = 当前算力已耗尽，请联系管理员增加算力额度`
   - `batch.link.parseFailTip = 链接解析失败，请检查链接是否有效`

4. **前端页面**
   - 顶部 Tab 切换：品牌配置 / 版本管理 / 全局参数。
   - 复用 `ImageUpload` 组件上传 Logo / 背景图 / 空状态占位图。
   - 版本管理使用 Element UI 表格 + 新增/编辑弹窗，支持状态开关二次确认、删除二次确认。
   - 全局参数使用 `el-input-number` 控制数值，`el-color-picker` 选择主色调。

## 测试/检查结果

- [x] 代码自审：接口 URL、权限字符串、字段命名与计划一致。
- [x] 代码自审：后端实体字段与 `sql/batch_business.sql` 中 `batch_system_config`、`batch_app_version` 表结构一致。
- [x] 代码自审：XML 中 namespace、resultMap、statement id 与 Mapper 接口完全对应。
- [x] 代码自审：前端路由组件 `batch/config/index` 与菜单 SQL 配置一致。
- [ ] 本地 Maven 编译：失败，原因见下。
- [ ] 本地前端运行：未执行（无 Node 环境）。
- [ ] 接口联调：未执行（无运行环境）。

## 遇到的问题

1. **JDK 版本不匹配导致无法本地编译**
   - 当前环境 JDK 为 1.8，项目 `pom.xml` 中 `maven-compiler-plugin` 目标版本为 17。
   - 执行 `mvn clean compile -pl ruoyi-system -am -DskipTests` 时报错：`Fatal error compiling: 无效的目标发行版: 17`。
   - 已进行静态代码审查，语法遵循现有 RuoYi 3.9.2 风格，但仍建议在 Java 17 环境中补全编译与单元测试。

2. **git commit 时误包含其他 Phase 文件**
   - 首次 `git commit` 因其他 Phase 的已暂存文件被一并提交。
   - 已通过 `git reset --soft HEAD~1` 撤销后重新提交仅 Phase 8 相关文件，最终 commit SHA 为 `2c04191`。

## 待后续验证

- 在 Java 17 + MySQL 环境中执行 `mvn clean package -DskipTests` 验证编译。
- 启动后端后，使用 Swagger/Postman 测试 `/batch/config/**` 接口。
- 启动前端 `npm run dev`，确认 admin 登录后「系统配置」菜单可正常访问，Tab 切换、表单保存、版本增删改查、状态切换均正常。
- 确认全局参数默认值可通过「恢复默认值」按钮正确初始化。

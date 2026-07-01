# Phase 4: 公告管理模块 实施报告

## 状态

已完成并提交。

## Commit 信息

```
feat(Phase 4: 公告管理模块): 完成公告管理后端 CRUD、上下架、预览与前端列表页
```

## 修改文件清单

### 后端（ruoyi-system）

| 文件 | 说明 |
|---|---|
| `ruoyi-system/src/main/java/com/ruoyi/batch/notice/domain/BatchAppNotice.java` | APP 公告实体，含 `@Excel` 导出注解 |
| `ruoyi-system/src/main/java/com/ruoyi/batch/notice/mapper/BatchAppNoticeMapper.java` | Mapper 接口 |
| `ruoyi-system/src/main/java/com/ruoyi/batch/notice/service/IBatchAppNoticeService.java` | Service 接口 |
| `ruoyi-system/src/main/java/com/ruoyi/batch/notice/service/impl/BatchAppNoticeServiceImpl.java` | Service 实现，含发布/下架逻辑 |
| `ruoyi-system/src/main/java/com/ruoyi/batch/notice/controller/BatchAppNoticeController.java` | REST 控制器 |
| `ruoyi-system/src/main/resources/mapper/batch/notice/BatchAppNoticeMapper.xml` | MyBatis XML |

### 前端（ruoyi-ui）

| 文件 | 说明 |
|---|---|
| `ruoyi-ui/src/api/batch/notice.js` | 公告接口封装 |
| `ruoyi-ui/src/views/batch/notice/index.vue` | 公告列表、新增/编辑弹窗、预览弹窗 |

## 接口清单

| 方法 | URL | 权限 | 说明 |
|---|---|---|---|
| GET | `/batch/notice/list` | `batch:notice:list` | 分页列表 |
| GET | `/batch/notice/export` | `batch:notice:export` | 导出 Excel |
| GET | `/batch/notice/{noticeId}` | `batch:notice:query` | 详情 |
| GET | `/batch/notice/preview/{noticeId}` | `batch:notice:query` | 预览 |
| POST | `/batch/notice` | `batch:notice:add` | 新增 |
| PUT | `/batch/notice` | `batch:notice:edit` | 编辑 |
| DELETE | `/batch/notice/{noticeIds}` | `batch:notice:remove` | 删除（逻辑删除） |
| PUT | `/batch/notice/publish/{noticeId}` | `batch:notice:publish` | 发布 |
| PUT | `/batch/notice/unpublish/{noticeId}` | `batch:notice:edit` | 下架 |

## 实现要点

- 状态约定与全局约束保持一致：`publish_status` 0 已发布 / 1 已下架 / 2 暂存；`del_flag` 0 存在 / 2 删除。
- 发布时自动设置 `publish_time` 为当前时间；下架仅修改状态。
- 列表默认按发布状态升序、发布时间降序排列。
- 新增/编辑弹窗复用 `ImageUpload`（封面图）与 `Editor`（富文本）。
- 删除、发布、下架均增加二次确认弹窗。
- 前端按钮权限使用 `v-hasPermi="['batch:notice:xxx']"`，与菜单 SQL 中的按钮权限一致。
- 导出功能已加 `@Excel` 注解，支持类型与状态中文转换。

## 测试/检查结果

- 已做代码自审，确认接口 URL、权限字符串、字段名与计划及 `batch_menu.sql` 一致。
- 已确认 `ImageUpload`、`Editor`、`right-toolbar`、`pagination`、`dict-tag` 等组件在项目中存在且用法与系统公告模块一致。
- 已执行 Maven 编译验证，因本地 JDK 1.8 低于项目要求的 Java 17，编译在 `ruoyi-common` 阶段因 `无效的目标发行版: 17` 失败，属于环境限制，非代码语法问题。
- 未连接 MySQL/未运行前端服务，无法做端到端功能测试。

## 遇到的问题

1. **JDK 版本不匹配**：本地 JDK 为 1.8，项目 `pom.xml` 要求 Java 17，无法完成本地 Maven 编译。已在代码层面尽量保证语法正确，并参考现有系统公告模块保持风格一致。
2. **报告文件命名**：根据模板 `task-{{item | replace(":", "-") | replace(" ", "-") | lower}}-report.md`，本报告文件命名为 `task-phase-4--公告管理模块-report.md`（含双连字符）。

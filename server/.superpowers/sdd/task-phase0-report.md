# 批量剪辑 管理后台 Phase 0 实施报告

## 状态

**DONE_WITH_CONCERNS**

Phase 0 全部 4 项任务已按实施计划完成文件创建与代码修改。因当前运行环境 JDK 为 1.8，无法执行项目要求的 Java 17 Maven 编译，故未能在本地完成后端编译验证；其余文件已做语法/结构检查。

---

## 任务完成情况

### Task 0.1：创建数据库表

- **文件**：`server/sql/batch_business.sql`
- **内容**：包含 1.1~1.4 全部 15 张 `batch_*` 业务表的 CREATE TABLE 语句：
  1. `batch_customer`（客户/APP 账号主表）
  2. `batch_customer_relation`（上下级关系快照表）
  3. `batch_home_banner`（首页轮播图）
  4. `batch_home_news`（首页喜报数据）
  5. `batch_home_entry`（首页功能入口）
  6. `batch_home_tutorial_entry`（首页教程入口）
  7. `batch_app_notice`（APP 公告）
  8. `batch_tutorial`（教程）
  9. `batch_tutorial_category`（教程分类）
  10. `batch_document`（文档管理）
  11. `batch_computing_power_log`（算力消耗日志）
  12. `batch_video_generate_log`（视频生成记录）
  13. `batch_qr_code_stat`（二维码推广统计）
  14. `batch_system_config`（扩展全局参数）
  15. `batch_app_version`（APP 版本管理）
- **规范**：每张表均含注释、主键、常用索引、通用字段 `create_by/create_time/update_by/update_time/remark`、`del_flag`（0 存在 / 2 删除），并遵循 `status` 0 启用/1 禁用约定。
- **验证**：通过脚本检查，15 张表齐全，所有 CREATE TABLE 语句括号平衡，单引号成对。

### Task 0.2：后端业务包脚手架

- **位置**：`server/ruoyi-system/src/main/java/com/ruoyi/batch`
- **子包**：`customer`、`vip`、`home`、`notice`、`tutorial`、`document`、`computing`、`statistics`、`config`
- **每个子包包含**：
  - `domain/*.java`
  - `mapper/*.java`
  - `service/I*Service.java`
  - `service/impl/*ServiceImpl.java`
  - `controller/*Controller.java`
- **Mapper XML**：`ruoyi-system/src/main/resources/mapper/batch/<子包>/*Mapper.xml`
- **说明**：
  - domain 实体继承 `BaseEntity`，含 `id` 与通用字段，保持 RuoYi 风格。
  - Service 实现类已加 `@Service` 注解，注入对应 Mapper。
  - Controller 占位类暂为普通 Java 类（未引入 `@RestController` 等 spring-webmvc 注解），避免 `ruoyi-system` 模块因缺少 `spring-webmvc` 直接依赖导致占位阶段编译失败；后续填充接口时可按需引入注解或迁移至 `ruoyi-admin`。
  - Mapper XML 的 `namespace`、`resultMap`、`selectList` 已指向实际设计表名，处于占位状态。

### Task 0.3：扩展后台登录支持手机号

- **修改文件 1**：`server/ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml`
  - `selectUserByUserName` 查询条件由 `u.user_name = #{userName}` 改为 `(u.user_name = #{userName} or u.phonenumber = #{userName})`，支持管理员使用账号或手机号登录。
- **修改文件 2**：`server/ruoyi-ui/src/views/login.vue`
  - 账号输入框 `placeholder` 由 "账号" 改为 "请输入管理员账号/手机号"。
  - 表单校验提示信息同步改为 "请输入管理员账号/手机号"。
- **说明**：`SysUserServiceImpl.selectUserByUserName` 本身仅透传 Mapper，无需额外修改。

### Task 0.4：配置后台动态菜单

- **文件**：`server/sql/batch_menu.sql`
- **内容**：
  - 1 个目录：批量剪辑管理（`menu_id=3000`，icon `el-icon-s-management`）
  - 8 个菜单：客户管理、会员VIP管理、首页内容管理、公告管理、教程管理、文档管理、数据统计、系统配置
  - 43 个按钮权限记录：每个菜单配置 `query/add/edit/remove/export` 等，额外为客户管理补充 `upgrade/migrate/resetQr`，为公告管理补充 `publish`。
- **权限字符串**：统一使用 `batch:模块:操作` 格式，如 `batch:customer:list`、`batch:customer:add`。
- **验证**：48 条 INSERT 语句括号平衡，单引号成对。

---

## 修改文件清单

### 新增文件

```
sql/batch_business.sql
sql/batch_menu.sql
ruoyi-system/src/main/java/com/ruoyi/batch/customer/domain/BatchCustomer.java
ruoyi-system/src/main/java/com/ruoyi/batch/customer/mapper/BatchCustomerMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/customer/service/IBatchCustomerService.java
ruoyi-system/src/main/java/com/ruoyi/batch/customer/service/impl/BatchCustomerServiceImpl.java
ruoyi-system/src/main/java/com/ruoyi/batch/customer/controller/BatchCustomerController.java
ruoyi-system/src/main/java/com/ruoyi/batch/vip/domain/BatchVip.java
ruoyi-system/src/main/java/com/ruoyi/batch/vip/mapper/BatchVipMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/vip/service/IBatchVipService.java
ruoyi-system/src/main/java/com/ruoyi/batch/vip/service/impl/BatchVipServiceImpl.java
ruoyi-system/src/main/java/com/ruoyi/batch/vip/controller/BatchVipController.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/domain/BatchHomeBanner.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/mapper/BatchHomeBannerMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/service/IBatchHomeBannerService.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/service/impl/BatchHomeBannerServiceImpl.java
ruoyi-system/src/main/java/com/ruoyi/batch/home/controller/BatchHomeBannerController.java
ruoyi-system/src/main/java/com/ruoyi/batch/notice/domain/BatchAppNotice.java
ruoyi-system/src/main/java/com/ruoyi/batch/notice/mapper/BatchAppNoticeMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/notice/service/IBatchAppNoticeService.java
ruoyi-system/src/main/java/com/ruoyi/batch/notice/service/impl/BatchAppNoticeServiceImpl.java
ruoyi-system/src/main/java/com/ruoyi/batch/notice/controller/BatchAppNoticeController.java
ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/domain/BatchTutorial.java
ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/mapper/BatchTutorialMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/service/IBatchTutorialService.java
ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/service/impl/BatchTutorialServiceImpl.java
ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/controller/BatchTutorialController.java
ruoyi-system/src/main/java/com/ruoyi/batch/document/domain/BatchDocument.java
ruoyi-system/src/main/java/com/ruoyi/batch/document/mapper/BatchDocumentMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/document/service/IBatchDocumentService.java
ruoyi-system/src/main/java/com/ruoyi/batch/document/service/impl/BatchDocumentServiceImpl.java
ruoyi-system/src/main/java/com/ruoyi/batch/document/controller/BatchDocumentController.java
ruoyi-system/src/main/java/com/ruoyi/batch/computing/domain/BatchComputingPowerLog.java
ruoyi-system/src/main/java/com/ruoyi/batch/computing/mapper/BatchComputingPowerLogMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/computing/service/IBatchComputingPowerLogService.java
ruoyi-system/src/main/java/com/ruoyi/batch/computing/service/impl/BatchComputingPowerLogServiceImpl.java
ruoyi-system/src/main/java/com/ruoyi/batch/computing/controller/BatchComputingPowerLogController.java
ruoyi-system/src/main/java/com/ruoyi/batch/statistics/domain/BatchQrCodeStat.java
ruoyi-system/src/main/java/com/ruoyi/batch/statistics/mapper/BatchQrCodeStatMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/statistics/service/IBatchQrCodeStatService.java
ruoyi-system/src/main/java/com/ruoyi/batch/statistics/service/impl/BatchQrCodeStatServiceImpl.java
ruoyi-system/src/main/java/com/ruoyi/batch/statistics/controller/BatchQrCodeStatController.java
ruoyi-system/src/main/java/com/ruoyi/batch/config/domain/BatchSystemConfig.java
ruoyi-system/src/main/java/com/ruoyi/batch/config/mapper/BatchSystemConfigMapper.java
ruoyi-system/src/main/java/com/ruoyi/batch/config/service/IBatchSystemConfigService.java
ruoyi-system/src/main/java/com/ruoyi/batch/config/service/impl/BatchSystemConfigServiceImpl.java
ruoyi-system/src/main/java/com/ruoyi/batch/config/controller/BatchSystemConfigController.java
ruoyi-system/src/main/resources/mapper/batch/customer/BatchCustomerMapper.xml
ruoyi-system/src/main/resources/mapper/batch/vip/BatchVipMapper.xml
ruoyi-system/src/main/resources/mapper/batch/home/BatchHomeBannerMapper.xml
ruoyi-system/src/main/resources/mapper/batch/notice/BatchAppNoticeMapper.xml
ruoyi-system/src/main/resources/mapper/batch/tutorial/BatchTutorialMapper.xml
ruoyi-system/src/main/resources/mapper/batch/document/BatchDocumentMapper.xml
ruoyi-system/src/main/resources/mapper/batch/computing/BatchComputingPowerLogMapper.xml
ruoyi-system/src/main/resources/mapper/batch/statistics/BatchQrCodeStatMapper.xml
ruoyi-system/src/main/resources/mapper/batch/config/BatchSystemConfigMapper.xml
```

### 修改文件

```
ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml
ruoyi-ui/src/views/login.vue
```

---

## 测试情况

| 测试项 | 结果 | 说明 |
|---|---|---|
| SQL 文件结构检查 | 通过 | `batch_business.sql` 15 张表完整，`batch_menu.sql` 48 条插入完整，括号/引号平衡。 |
| Java 占位类文件结构 | 通过 | 9 个子包 × 5 类 + 9 个 Mapper XML 已按预期生成。 |
| Maven 编译 | 未执行 | 环境 JDK 为 1.8，项目要求 Java 17，`mvn compile` 报 "无效的目标发行版: 17"。 |
| MySQL 建表执行 | 未执行 | 当前环境未提供可连接的 MySQL 实例。 |
| 前端登录页文案 | 已修改 | 占位文案与校验提示已更新为 "请输入管理员账号/手机号"。 |
| 后端登录手机号支持 | 已修改 | `SysUserMapper.xml` 已增加 `OR u.phonenumber = #{userName}`。 |

---

## 遇到的问题

1. **JDK 版本不兼容**
   - 现象：`mvn compile -pl ruoyi-system -am -q` 失败，报错 "无效的目标发行版: 17"。
   - 原因：当前环境 `java -version` 为 `1.8.0_202`，未安装 JDK 17。
   - 影响：无法完成 Java 编译级验证，但占位类结构简单，已做人工检查。
   - 建议：在具备 JDK 17 的环境中执行 `mvn clean compile` 与 `mvn test` 进行验证。

2. **Controller 占位类位置**
   - 现象：实施计划要求 Controller 位于 `ruoyi-system/src/main/java/com/ruoyi/batch/*/controller/`，而 RuoYi-Vue 原生 Controller 位于 `ruoyi-admin`。
   - 处理：按计划在 `ruoyi-system` 下创建普通 Java 占位类，未引入 `@RestController` 等 `spring-webmvc` 注解，避免 `ruoyi-system` 模块因缺少 `spring-webmvc` 直接依赖而编译失败。后续实现接口时可根据团队约定决定是在 `ruoyi-system` 引入依赖还是迁移至 `ruoyi-admin`。

3. **未执行 MySQL 与前端运行验证**
   - 当前环境未提供 MySQL 服务，无法执行 `batch_business.sql` 与 `batch_menu.sql`；前端 dev server 亦未启动，无法目视确认菜单出现。
   - 建议：在开发/测试数据库中执行两条 SQL 后，使用 admin 账号登录管理后台确认左侧菜单与登录功能。

---

## 后续建议

1. 在 JDK 17 环境中运行 `mvn clean compile` 验证新增 Java 文件与 Mapper XML。
2. 在 MySQL 中执行 `sql/batch_business.sql` 与 `sql/batch_menu.sql`，确认 15 张表创建成功且菜单数据插入正确。
3. 启动 `ruoyi-ui`，使用 admin/admin123 测试登录，确认原有账号登录流程未受影响；若管理员手机号已录入，可测试手机号登录。
4. Phase 1 开始填充 `customer` 模块实体、Mapper、Service、Controller 与前端页面。

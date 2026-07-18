# Phase 1: 客户管理模块实施报告

## 1. 实施状态

- **状态**：已完成
- **模块**：客户管理（Phase 1）
- **实施范围**：后端 Java CRUD + 业务校验 + 二维码生成、前端 Vue 列表/新增/编辑/详情/二维码/升级/迁移

## 2. 修改文件清单

### 后端（ruoyi-system）

| 路径 | 说明 |
|------|------|
| `ruoyi-system/pom.xml` | 新增 zxing 二维码生成依赖（core / javase 3.5.2） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/customer/domain/BatchCustomer.java` | 客户实体（含校验注解、Excel 导出注解） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/customer/mapper/BatchCustomerMapper.java` | Mapper 接口 |
| `ruoyi-system/src/main/resources/mapper/batch/customer/BatchCustomerMapper.xml` | Mapper XML（CRUD、统计、唯一校验、二维码更新） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/customer/service/IBatchCustomerService.java` | Service 接口 |
| `ruoyi-system/src/main/java/com/ruoyi/batch/customer/service/impl/BatchCustomerServiceImpl.java` | Service 实现（含全部业务校验与级联更新） |
| `ruoyi-system/src/main/java/com/ruoyi/batch/customer/controller/BatchCustomerController.java` | REST 接口 Controller |
| `ruoyi-system/src/main/java/com/ruoyi/batch/customer/utils/QrCodeUtil.java` | 二维码生成与上传工具 |

### 前端（ruoyi-ui）

| 路径 | 说明 |
|------|------|
| `ruoyi-ui/src/api/batch/customer.js` | 客户管理 API 封装 |
| `ruoyi-ui/src/router/index.js` | 新增账号详情、二维码详情动态路由 |
| `ruoyi-ui/src/views/batch/customer/index.vue` | 客户列表页（搜索/表格/操作/分页） |
| `ruoyi-ui/src/views/batch/customer/form.vue` | 新增/编辑账号弹窗 |
| `ruoyi-ui/src/views/batch/customer/detail.vue` | 账号详情页（基础信息/配额/下级/二维码 Tab） |
| `ruoyi-ui/src/views/batch/customer/qrcode.vue` | 注册二维码查看/下载/复制/重置页 |
| `ruoyi-ui/src/views/batch/customer/upgrade.vue` | 账号升级弹窗 |
| `ruoyi-ui/src/views/batch/customer/migrate.vue` | 账号迁移弹窗 |

### 报告

| 路径 | 说明 |
|------|------|
| `.superpowers/sdd/task-phase-1--客户管理模块-report.md` | 本报告 |

## 3. 实现要点

### 3.1 后端

- **实体 `BatchCustomer`**：完整映射 `batch_customer` 表字段，新增 `subordinateCount`、`usedSubordinateCount` 作为展示/计算字段；使用 `jakarta.validation` 注解校验；使用 `@Excel` 支持导出。
- **Mapper**：支持按手机号/名称/联系人/类型/状态/上级/分公司等多条件分页查询；提供按手机号查询、按上级统计、按分公司统计、手机号唯一校验、二维码信息更新。
- **Service 业务校验**：
  - 新增时校验手机号全局唯一、上级存在且类型匹配、上级名额充足。
  - 编辑时不允许修改 `customerType` 和 `phone`；配额不能小于已创建数量；算力总配额变更时同步剩余算力。
  - 删除前校验无下级账号。
  - 自动计算并维护 `branch_phone` 链：分公司为空，服务商/个人继承上级链。
- **升级**：个人 → 服务商需指定分公司上级并配置 `maxIndividual`；服务商 → 分公司需配置分公司配额，升级后无上级。
- **迁移**：修改上级手机号，校验新上级存在/类型匹配/名额充足，同步更新所有下级的 `branch_phone`。
- **二维码**：新增账号成功后自动生成带 `invitePhone` 参数的二维码图片，上传到文件服务器并保存 URL；支持重置二维码并更新 `qr_code_key`。
- **数据权限**：列表/导出接口中，非 admin 用户自动按当前登录管理员手机号过滤 `branch_phone`，实现分公司管理员仅查看本分公司下属数据。

### 3.2 前端

- **列表页**：支持手机号/名称/联系人模糊搜索、账号类型/状态筛选、分公司手机号筛选（总后台）；表格展示所有关键字段；操作列包含查看、编辑、二维码、升级、迁移、删除；使用 `v-hasPermi` 控制按钮权限。
- **新增/编辑弹窗**：根据账号类型动态显示上级手机号、配额字段；提交成功后提示查看二维码或返回列表。
- **详情页**：卡片展示基础信息、配额信息；Tab 切换下级账号/算力记录/视频记录/注册二维码；支持下级查看、迁移、删除。
- **二维码页**：大图展示、账号信息、扫码/下载/注册统计卡片、下载/复制链接/重置按钮。
- **升级/迁移弹窗**：二次确认后提交。

### 3.3 接口列表

| 方法 | URL | 权限 |
|------|-----|------|
| GET | `/batch/customer/list` | batch:customer:list |
| POST | `/batch/customer/export` | batch:customer:export |
| GET | `/batch/customer/{customerId}` | batch:customer:query |
| GET | `/batch/customer/phone/{phone}` | batch:customer:query |
| POST | `/batch/customer` | batch:customer:add |
| PUT | `/batch/customer` | batch:customer:edit |
| DELETE | `/batch/customer/{customerIds}` | batch:customer:remove |
| PUT | `/batch/customer/changeStatus` | batch:customer:edit |
| PUT | `/batch/customer/qrCode/{customerId}` | batch:customer:resetQr |
| PUT | `/batch/customer/upgrade/{customerId}` | batch:customer:upgrade |
| PUT | `/batch/customer/migrate/{customerId}` | batch:customer:migrate |

## 4. 测试与检查

- 由于当前环境 JDK 为 1.8，项目要求 Java 17，未执行本地 Maven 编译。
- 已进行代码静态自审，检查点包括：
  - 接口 URL 统一以 `/batch/customer/...` 开头。
  - 权限字符串使用 `batch:customer:xxx`。
  - 状态约定 `status 0=启用 1=禁用`，`del_flag 0=存在 2=删除`。
  - 手机号唯一、上级类型匹配、名额充足、删除前无下级等边界规则已覆盖。
  - 二维码工具依赖 zxing 已加入 `ruoyi-system/pom.xml`。
- 未能启动前端服务进行实际页面验证；已检查组件引用、API 路径、路由配置与权限指令。

## 5. 遇到的问题与说明

1. **并行修改冲突**：工作区中存在其他 Phase 的并行改动，本提交仅包含 Phase 1（客户管理）相关文件。
2. **编译环境受限**：本地 JDK 1.8 无法编译 Java 17 项目，建议在具备 Java 17 环境后执行 `mvn clean package -DskipTests` 验证。
3. **二维码下载页 URL**：当前使用配置项 `batch.app.downloadUrl`（默认 `https://batch-video-editor.example.com/download`），后续可由 Phase 8 系统配置模块统一管理。
4. **算力/视频记录 Tab**：详情页中算力消耗记录、视频生成记录 Tab 当前为空状态占位，待 Phase 7 数据统计/算力模块完成后接入真实数据。
5. **分公司管理员数据权限**：当前以 `SecurityUtils.getLoginUser().getUser().getPhonenumber()` 作为分公司手机号过滤。若后台管理员手机号与分公司手机号不一致，需在后续迭代中补充管理员-分公司绑定字段。

## 6. Commit 信息

- **SHA**: `5bfcd49983ce04061dd95c59d3397d235f1cd784`
- **Message**: `feat(Phase 1: 客户管理模块): 实现客户列表/新增/编辑/删除/详情/二维码/升级/迁移后端与前端`
- **Push**: 已推送至 `https://github.com/Tony-0x42/batch-video-editor.git` main 分支

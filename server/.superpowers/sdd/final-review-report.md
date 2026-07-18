# 批量剪辑 管理后台最终集成审查报告

**审查日期**：2026-07-01  
**审查范围**：
1. 后端 Java 代码：`ruoyi-system/src/main/java/com/ruoyi/batch/**`
2. MyBatis XML：`ruoyi-system/src/main/resources/mapper/batch/**`
3. 前端 Vue 代码：`ruoyi-ui/src/views/batch/**`、`ruoyi-ui/src/api/batch/**`
4. SQL 文件：`sql/batch_business.sql`、`sql/batch_menu.sql`
5. 被修改的系统文件：`ruoyi-ui/src/router/index.js`、`ruoyi-ui/src/views/login.vue`、`ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml`

**审查依据**：
- `spec/modules/web/` 设计文档
- `C:/Users/beimi/.kimi-code/sessions/wd_batchvideoapp_7ea9f3255ee0/session_a2e16752-498b-4e33-b4ec-ac901788c66b/agents/main/plans/sam-alexander-colossus-shang-chi.md`
- `AGENTS.md` 全局约束

**审查结论**：**需修复（不通过）**。项目整体结构完整，9 大模块的页面、接口、菜单已基本落地，但存在若干影响功能正确性的 bug、跨模块不一致及未完成的占位功能，需在上线前修复。

---

## 一、关键问题总览（按严重程度）

| 序号 | 严重程度 | 问题简述 | 影响模块 |
|------|----------|----------|----------|
| 1 | Critical | 后端多处日期过滤使用 `%y%m%d`（2 位年份），导致跨世纪/跨年数据过滤错误 | 客户管理、公告管理 |
| 2 | Critical | VIP 管理「查看详情」跳转使用 query 参数，但路由配置为 path 参数，页面无法打开 | VIP 管理 |
| 3 | Critical | 公告导出后端返回 AjaxResult，前端按 blob 下载，功能不匹配 | 公告管理 |
| 4 | Critical | 数据统计导出把 axios response 对象直接包成 Blob，下载文件内容错误 | 数据统计 |
| 5 | Important | 客户列表 `subordinateCount` 字段后端未填充，列表始终为空 | 客户管理 |
| 6 | Important | 客户详情页「算力消耗记录/视频生成记录」Tab 为硬编码空状态，无数据加载 | 客户管理 |
| 7 | Important | 账号升级弹窗校验规则未按类型动态切换，服务商→分公司升级会被校验拦截 | 客户管理 |
| 8 | Important | 客户详情配额卡片使用当前页长度计算下级数量，分页后数据失真 | 客户管理 |
| 9 | Important | 系统配置页允许修改 AI 切片区间/视频上限，违反「前端不可修改区间阈值」的全局约束 | 系统配置 |
| 10 | Important | 教程分类删除未校验关联教程，存在误删风险 | 教程管理 |
| 11 | Minor | 客户二维码详情页统计卡片为 TODO 占位，未接入真实数据 | 客户管理 |
| 12 | Minor | `BatchComputingPowerLogController`、`BatchQrCodeStatController`、`BatchSystemConfigController` 仍为占位类 | 算力/统计/配置 |
| 13 | Minor | 首页管理相关 domain 使用 String 表示状态/类型，与其他模块 Integer 不一致 | 首页内容管理 |
| 14 | Minor | `BatchVipMapper.xml` / `BatchVip` / `BatchComputingPowerLog` 为占位实现，且 XML 引用了不存在的字段 | VIP/算力 |

---

## 二、详细问题与修复建议

### 2.1 Critical

#### CR-1：日期格式字符串使用 `%y%m%d`（2 位年份）

**位置**：
- `ruoyi-system/src/main/resources/mapper/batch/customer/BatchCustomerMapper.xml` 第 76、78 行
- `ruoyi-system/src/main/resources/mapper/batch/notice/BatchAppNoticeMapper.xml` 第 47、49 行

**问题描述**：
MyBatis 中使用 `date_format(..., '%y%m%d')` 进行日期范围过滤。`%y` 只取年份后两位，在 2000 年以后当前场景下仍可比较，但存在以下风险：
1. 语义错误，应使用 4 位年份 `%Y%m%d`；
2. 跨年/跨世纪场景或历史数据导入时会出现过滤异常；
3. 与 RuoYi 既有系统表（如 `SysUserMapper.xml`）的写法 `%Y%m%d` 不一致。

**修复建议**：
将 `%y%m%d` 统一替换为 `%Y%m%d`。

```xml
<!-- 修改前 -->
AND date_format(c.create_time,'%y%m%d') &gt;= date_format(#{params.beginTime},'%y%m%d')
<!-- 修改后 -->
AND date_format(c.create_time,'%Y%m%d') &gt;= date_format(#{params.beginTime},'%Y%m%d')
```

---

#### CR-2：VIP 管理「查看详情」路由不匹配

**位置**：
- `ruoyi-ui/src/views/batch/vip/index.vue` 第 318 行
- `ruoyi-ui/src/router/index.js` 第 172-189 行

**问题描述**：
VIP 列表页点击「查看详情」执行：
```js
this.$router.push({ path: "/batch/customer/detail", query: { customerId: row.customerId }})
```
而路由配置为：
```js
path: 'detail/:customerId(\d+)'
```
Vue Router 会尝试将 `/batch/customer/detail` 匹配到带 path 参数的路由，因缺少 `customerId` 导致匹配失败，页面空白或 404。

**修复建议**：
统一使用 path 参数跳转：
```js
this.$router.push("/batch/customer/detail/" + row.customerId)
```

---

#### CR-3：公告导出前后端协议不匹配

**位置**：
- 后端：`ruoyi-system/src/main/java/com/ruoyi/batch/notice/controller/BatchAppNoticeController.java` 第 50-58 行
- 前端：`ruoyi-ui/src/views/batch/notice/index.vue` 第 448-452 行

**问题描述**：
后端 `export` 方法返回 `AjaxResult`（ExcelUtil 无 HttpServletResponse 重载返回的下载 URL），而前端使用 `this.download('/batch/notice/export', ...)` 期望服务端直接写出二进制 Excel。结果前端无法触发文件下载。

**修复建议**（二选一）：
- **方案 A（推荐，与其他模块一致）**：后端改为 `void export(HttpServletResponse response, BatchAppNotice batchAppNotice)` 并调用 `util.exportExcel(response, list, "APP公告数据")`。
- **方案 B**：前端改为接收 AjaxResult，拿到 `data.url` 后通过 `<a>` 标签或 `window.open` 下载。

---

#### CR-4：数据统计导出错误处理 Blob

**位置**：
- `ruoyi-ui/src/views/batch/statistics/index.vue` 第 571-583 行
- 后端：`BatchStatisticsController` 各 `/xxx/export` 接口

**问题描述**：
前端 export 函数设置了 `responseType: 'blob'`，回调中却把整个 axios response 对象传给：
```js
const blob = new Blob([response], { type: '...' })
saveAs(blob, fileName)
```
`response` 是 axios 响应对象，会被 `toString()` 为 `"[object Object]"`，导致下载的 Excel 文件内容损坏。

**修复建议**：
```js
exportFunc(this.queryParams).then(response => {
  this.downloadFile(response.data, fileName) // 取 response.data
})
```

---

### 2.2 Important

#### IM-1：客户列表 `subordinateCount` 未填充

**位置**：
- `ruoyi-system/src/main/resources/mapper/batch/customer/BatchCustomerMapper.xml` 第 33-40 行
- `ruoyi-system/src/main/java/com/ruoyi/batch/customer/service/impl/BatchCustomerServiceImpl.java` 第 46-50 行
- `ruoyi-ui/src/views/batch/customer/index.vue` 第 62 行

**问题描述**：
列表查询 SQL 未选择 `subordinateCount`，`selectBatchCustomerList` 服务方法也未调用 `fillSubordinateCount`。前端表格列虽然绑定了 `subordinateCount`，但值始终为空。

**修复建议**：
在 `BatchCustomerServiceImpl.selectBatchCustomerList` 中对每个客户调用 `countByParentPhone` 填充，或在 XML 中增加子查询：
```xml
(select count(*) from batch_customer sub where sub.parent_phone = c.phone and sub.del_flag = 0) as subordinate_count
```

---

#### IM-2：客户详情「算力/视频」Tab 为占位空状态

**位置**：
- `ruoyi-ui/src/views/batch/customer/detail.vue` 第 85-91 行

**问题描述**：
详情页四个 Tab 中，「下级账号」已接入，但「算力消耗记录」和「视频生成记录」直接写死 `<el-empty description="暂无..." />`，未调用任何接口。计划明确要求详情页展示算力消耗记录和视频生成记录。

**修复建议**：
新增后端接口（如 `/batch/statistics/computing?phone=xxx`、`/batch/statistics/video?phone=xxx`）或在现有 statistics 接口中增加 `phone` 查询参数，前端在 Tab 激活时调用并渲染表格。

---

#### IM-3：账号升级弹窗校验规则未动态切换

**位置**：
- `ruoyi-ui/src/views/batch/customer/upgrade.vue` 第 45-59 行

**问题描述**：
`rules` 中 `parentPhone`、`maxServiceProvider`、`totalIndividualCapacity`、`maxIndividual` 均为必填。当服务商升级为分公司时，`parentPhone` 和 `maxIndividual` 字段被隐藏，但校验仍要求填写，导致升级提交失败。

**修复建议**：
将 `rules` 改为计算属性，根据 `row.customerType` 动态生成：
- 个人→服务商：必填 `parentPhone`、`maxIndividual`
- 服务商→分公司：必填 `maxServiceProvider`、`totalIndividualCapacity`

---

#### IM-4：客户详情配额卡片使用当前页长度

**位置**：
- `ruoyi-ui/src/views/batch/customer/detail.vue` 第 141-144、163-177 行

**问题描述**：
`subordinateCount` 使用 `this.subordinateList.length`，而 `subordinateList` 是分页后的当前页数据。当下级账号超过 10 条时，配额卡片显示的数量不准确，进而导致「剩余名额」计算错误。

**修复建议**：
使用 `subordinateTotal`（后端返回的总数）替代 `subordinateList.length`，或在后端增加 `countByParentPhone` 接口直接获取总数。

---

#### IM-5：系统配置页允许修改固定 AI 参数

**位置**：
- `ruoyi-ui/src/views/batch/config/index.vue` 第 117-129 行

**问题描述**：
全局约束要求「AI 分割切片时长区间固定 0.5~10s，步长 0.1s，前端不可修改区间阈值」。但当前全局参数页面对 `sliceMin`、`sliceMax`、`sliceStep`、`maxVideos` 均开放了编辑，且 `sliceMax` 上限放宽到 60。

**修复建议**：
将切片区间和步长字段设为只读展示（或隐藏），仅保留提示文案、空状态占位图、客服服务时段等可配置项。

---

#### IM-6：教程分类删除未校验关联教程

**位置**：
- `ruoyi-system/src/main/java/com/ruoyi/batch/tutorial/service/impl/BatchTutorialCategoryServiceImpl.java`
- `ruoyi-ui/src/views/batch/tutorial/index.vue` 第 643-651 行

**问题描述**：
计划要求「删除分类前校验无关联教程」。当前 `BatchTutorialCategoryServiceImpl` 未实现该校验，前端删除分类时也没有二次提示关联关系。

**修复建议**：
在 `deleteCategoryByIds` 中调用 `BatchTutorialMapper.countByCategoryId` 检查关联教程数量，若大于 0 则抛出 `ServiceException("该分类下存在教程，无法删除")`。

---

### 2.3 Minor

#### MN-1：客户二维码详情页统计为 TODO 占位

**位置**：
- `ruoyi-ui/src/views/batch/customer/qrcode.vue` 第 74 行

**问题描述**：
二维码详情页展示了扫码次数、下载量、注册数三个指标卡，但 `stat` 对象初始化为 0 且注释为 `TODO: 后续接入二维码推广统计接口`。

**修复建议**：
后端 `BatchStatisticsController` 增加按手机号聚合二维码统计的接口（或在 `BatchQrCodeStatController` 中实现），前端在 `getDetail` 后调用并填充 `stat`。

---

#### MN-2：多处占位类/占位 XML 未清理

**位置**：
- `ruoyi-system/src/main/java/com/ruoyi/batch/computing/controller/BatchComputingPowerLogController.java`
- `ruoyi-system/src/main/java/com/ruoyi/batch/statistics/controller/BatchQrCodeStatController.java`
- `ruoyi-system/src/main/java/com/ruoyi/batch/config/controller/BatchSystemConfigController.java`
- `ruoyi-system/src/main/java/com/ruoyi/batch/vip/domain/BatchVip.java`、`BatchVipMapper.java`
- `ruoyi-system/src/main/java/com/ruoyi/batch/computing/domain/BatchComputingPowerLog.java` 等
- `ruoyi-system/src/main/resources/mapper/batch/vip/BatchVipMapper.xml`
- `ruoyi-system/src/main/resources/mapper/batch/computing/BatchComputingPowerLogMapper.xml`

**问题描述**：
这些占位类/Mapper 未参与实际业务（VIP 实际使用 `BatchCustomerMapper`，配置实际使用 `BatchConfigController`），但会随代码库一起编译和扫描。`BatchVipMapper.xml` 中甚至引用了 `batch_customer` 表不存在的 `id`、`create_by` 等字段映射（实际业务表为 `batch_customer` 但字段映射错误）。

**修复建议**：
删除未使用的占位文件，或至少将 `BatchVipMapper.xml` / `BatchComputingPowerLogMapper.xml` 修正为与真实表结构一致，避免 MyBatis 启动扫描时报错或产生误导。

---

#### MN-3：首页管理 domain 状态类型不一致

**位置**：
- `ruoyi-system/src/main/java/com/ruoyi/batch/home/domain/BatchHomeBanner.java` 等

**问题描述**：
`BatchHomeBanner`、`BatchHomeNews`、`BatchHomeEntry`、`BatchHomeTutorialEntry`、`BatchTutorial`、`BatchTutorialCategory` 的 `status`、`targetType`、`tutorialType` 使用 `String`，而数据库为 `tinyint(1)`，客户/文档/公告/版本等模块使用 `Integer`。虽然 MySQL 会自动做类型转换，但跨模块风格不统一，易出现前端传 0/1 数字时校验失败的问题。

**修复建议**：
将首页、教程相关 domain 的状态/枚举字段统一改为 `Integer`，XML 中条件判断同步调整。

---

#### MN-4：文档管理前端类型与后端不一致

**位置**：
- `ruoyi-ui/src/views/batch/document/index.vue` 第 320-322、374-375 行

**问题描述**：
前端将 `status`、`documentType` 转为字符串处理，后端 `BatchDocument` 使用 `Integer`。当前 MySQL 隐式转换可运行，但不够健壮。

**修复建议**：
前后端统一使用数字 0/1/2/3/4 进行交互。

---

#### MN-5：客户编辑表单携带原始 status 字段

**位置**：
- `ruoyi-ui/src/views/batch/customer/form.vue` 第 191 行

**问题描述**：
编辑时 `this.form = response.data` 会把 `status` 等后端完整对象带入表单。虽然 XML 动态更新不会误改状态，但存在潜在风险。

**修复建议**：
编辑时只复制需要修改的字段，避免携带 `status`、`delFlag`、`createTime` 等只读字段。

---

## 三、接口 URL 与权限一致性检查

### 3.1 URL 规范

所有新增 Controller 均以 `/batch/模块名/...` 开头，符合计划要求：

| 模块 | Controller | 前缀 |
|------|------------|------|
| 客户管理 | `BatchCustomerController` | `/batch/customer` |
| VIP 管理 | `BatchVipController` | `/batch/vip` |
| 首页内容 | `BatchHomeBannerController` 等 | `/batch/home/banner`、`/batch/home/news`、`/batch/home/entry`、`/batch/home/tutorialEntry` |
| 公告管理 | `BatchAppNoticeController` | `/batch/notice` |
| 教程管理 | `BatchTutorialController` | `/batch/tutorial` |
| 文档管理 | `BatchDocumentController` | `/batch/document` |
| 数据统计 | `BatchStatisticsController` | `/batch/statistics` |
| 系统配置 | `BatchConfigController` | `/batch/config` |

> 注：`/batch/home/tutorialEntry` 使用了驼峰命名，建议统一为全小写 `/batch/home/tutorial-entry` 或 `/batch/home/tutorial_entry`，但当前前后端已保持一致，可保留。

### 3.2 权限字符串

后端 `@PreAuthorize` 与 `sql/batch_menu.sql` 中配置的权限基本一致：

- 客户管理：`batch:customer:list/query/add/edit/remove/export/upgrade/migrate/resetQr` ✅
- VIP 管理：`batch:vip:list/query/edit/export` ✅
- 首页内容：`batch:home:list/query/add/edit/remove/export` ✅
- 公告管理：`batch:notice:list/query/add/edit/remove/export/publish` ✅
- 教程管理：`batch:tutorial:list/query/add/edit/remove/export` ✅
- 文档管理：`batch:document:list/query/add/edit/remove/export` ✅
- 数据统计：`batch:statistics:list/query/export` ✅
- 系统配置：`batch:config:list/query/add/edit/remove/export` ✅

前端 `v-hasPermi` 与 SQL 配置基本对应。

---

## 四、数据库/实体/XML 字段一致性

### 4.1 主要表字段核对

| 表 | Java 实体 | XML 映射 | 一致性 |
|----|-----------|----------|--------|
| `batch_customer` | `BatchCustomer` | `BatchCustomerMapper.xml` | 基本一致，但 `subordinateCount` 未在列表 SQL 中填充 |
| `batch_home_banner` | `BatchHomeBanner` | `BatchHomeBannerMapper.xml` | ✅ |
| `batch_home_news` | `BatchHomeNews` | `BatchHomeNewsMapper.xml` | ✅ |
| `batch_home_entry` | `BatchHomeEntry` | `BatchHomeEntryMapper.xml` | ✅ |
| `batch_home_tutorial_entry` | `BatchHomeTutorialEntry` | `BatchHomeTutorialEntryMapper.xml` | ✅ |
| `batch_app_notice` | `BatchAppNotice` | `BatchAppNoticeMapper.xml` | ✅ |
| `batch_tutorial` | `BatchTutorial` | `BatchTutorialMapper.xml` | ✅ |
| `batch_tutorial_category` | `BatchTutorialCategory` | `BatchTutorialCategoryMapper.xml` | ✅ |
| `batch_document` | `BatchDocument` | `BatchDocumentMapper.xml` | ✅ |
| `batch_system_config` | `BatchSystemConfig` | `BatchSystemConfigMapper.xml` | ✅ |
| `batch_app_version` | `BatchAppVersion` | `BatchAppVersionMapper.xml` | ✅ |
| `batch_computing_power_log` | `BatchComputingPowerLog`（占位） | `BatchComputingPowerLogMapper.xml`（占位） | ❌ 未实现 |
| `batch_video_generate_log` | 无独立实体 | 无 | ❌ 未实现 |
| `batch_qr_code_stat` | `BatchQrCodeStat`（占位） | `BatchQrCodeStatMapper.xml`（占位） | ❌ 未实现 |

### 4.2 全局字段约定

- `status`：0 启用/上架，1 禁用/下架 ✅
- `del_flag`：0 存在，2 删除 ✅
- `customer_type`：1 分公司 / 2 服务商 / 3 个人 ✅

---

## 五、前端 API 与后端路径一致性

| API 文件 | 接口 | 后端路径 | 状态 |
|----------|------|----------|------|
| `api/batch/customer.js` | 全部 | `/batch/customer/...` | ✅ |
| `api/batch/vip.js` | 全部 | `/batch/vip/...` | ✅ |
| `api/batch/home.js` | 全部 | `/batch/home/...` | ✅ |
| `api/batch/notice.js` | 导出 | `/batch/notice/export` | ❌ 协议不匹配 |
| `api/batch/tutorial.js` | 全部 | `/batch/tutorial/...` | ✅ |
| `api/batch/document.js` | 全部 | `/batch/document/...` | ✅ |
| `api/batch/statistics.js` | 导出 | `/batch/statistics/.../export` | ❌ Blob 处理错误 |
| `api/batch/config.js` | 全部 | `/batch/config/...` | ✅ |

---

## 六、Vue 路由检查

`ruoyi-ui/src/router/index.js` 已为详情页和二维码页配置动态路由：

```js
{
  path: '/batch/customer',
  component: Layout,
  hidden: true,
  permissions: ['batch:customer:list'],
  children: [
    { path: 'detail/:customerId(\d+)', ... },
    { path: 'qrcode/:customerId(\d+)', ... }
  ]
}
```

路由配置正确，但 VIP 列表页未按此格式跳转（见 CR-2）。

---

## 七、全局约束符合度

| 约束项 | 符合情况 | 说明 |
|--------|----------|------|
| 蓝色主色调 | ✅ | 主要操作按钮均使用 `type="primary"` |
| 二次确认弹窗 | ✅ | 删除、重置二维码、升级、迁移、状态切换均有确认 |
| 手机号唯一 | ✅ | 后端 `checkPhoneUnique` 已实现 |
| 三级组织树 | ✅ | 分公司/服务商/个人层级及上下级校验完整 |
| 删除前校验下级 | ✅ | `BatchCustomerServiceImpl.deleteBatchCustomerByIds` 已校验 |
| 数据权限（分公司） | ✅ | 客户列表、统计概览按 `branch_phone` 过滤 |
| AI 切片区间不可修改 | ❌ | 系统配置页仍允许修改 sliceMin/sliceMax/sliceStep |
| 素材只读 | ✅ | 前端未写死展示素材 |

---

## 八、建议修复优先级

### 第一优先级（上线前必须修复）
1. 修复 `%y%m%d` 为 `%Y%m%d`（CR-1）
2. 修复 VIP 详情页跳转路由（CR-2）
3. 修复公告导出协议（CR-3）
4. 修复数据统计导出 Blob 处理（CR-4）
5. 修复客户列表 `subordinateCount` 未填充（IM-1）
6. 修复账号升级弹窗校验规则（IM-3）

### 第二优先级（建议上线前修复）
7. 客户详情页接入算力/视频记录（IM-2）
8. 修复客户详情配额卡片使用当前页长度（IM-4）
9. 系统配置页禁用 AI 固定阈值修改（IM-5）
10. 教程分类删除前校验关联教程（IM-6）

### 第三优先级（技术债务）
11. 清理/修正占位类与占位 XML（MN-2）
12. 统一首页/教程模块状态字段类型（MN-3）
13. 客户二维码详情页接入真实统计（MN-1）

---

## 九、总体评价

本次集成审查发现项目已完成 9 大模块的主体开发，菜单、权限、路由、前后端接口路径总体对齐，客户管理的核心业务规则（手机号唯一、上下级校验、删除前无下级、数据权限）实现较为完整。但存在 **4 个 Critical 级别的功能缺陷** 和 **6 个 Important 级别的影响用户体验或违反全局约束的问题**，需在上线前集中修复。建议优先处理 Critical 问题，随后完成 Important 问题，最后清理占位代码和类型不一致等技术债务。

**审查人**：Kimi Code CLI  
**输出文件**：`D:/project/batch-video-editor/server/.superpowers/sdd/final-review-report.md`

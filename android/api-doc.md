# 批量剪辑 APP - server 后端 API 对接文档

> 后端工程：`D:/project/batch-video-editor/server`（RuoYi 3.9.2）  
> 后端端口：`8080`  
> 应用上下文：`/`（无前缀）  
> 基础 Base URL（本地）：`http://localhost:8080/`  
> Android Emulator 访问本地后端请使用：`http://10.0.2.2:8080/`

---

## 1. 通用约定

### 1.1 认证方式

- 后端使用 RuoYi JWT Token 认证。
- 登录成功后返回 `token`，APP 需在后续请求中携带请求头：
  ```http
  Authorization: Bearer <token>
  ```
- Token 配置（`application.yml`）：
  - `token.header = Authorization`
  - `token.prefix = Bearer `
  - `token.expireTime = 30`（分钟）

### 1.2 匿名放行接口

Spring Security（`SecurityConfig`）中明确放行：

```
/login /register /captchaImage
POST /batch/app/login              （APP 手机号+密码登录）
POST /batch/app/register           （APP 扫码注册）
GET  /batch/qrcode/scan            （注册二维码扫码统计，@Anonymous 注解放行）
GET  /batch/home/banner/list、/batch/home/entry/list、/batch/home/news/list、/batch/home/tutorialEntry/list
GET  /batch/tutorial/list、/batch/tutorial/category/all、/batch/tutorial/*
GET  /batch/document/list、/batch/document/*
GET  /batch/notice/list、/batch/notice/*
GET  /batch/config/brand、/batch/config/global
GET  /batch/brand/list、/batch/contact/list
/ 、/*.html、/**.html、/**.css、/**.js、/profile/**
/swagger-ui.html、/v3/api-docs/**、/swagger-ui/**、/druid/**
```

- 标注 `@Anonymous` 注解的接口由 `PermitAllUrlProperties` 自动扫描放行（如 `/batch/qrcode/scan`）。
- **除上述外，其余接口（含全部写操作与 `/batch/ai/video/*`、`/batch/watermark/*`、`/batch/computing/*`）均需 JWT 认证（`anyRequest().authenticated()`）。**
- APP 端登录/注册请使用 `/batch/app/login`、`/batch/app/register`（见第 13 节），返回的 JWT 与 RuoYi 后台 Token 体系一致，权限标识为 `app:user`。

### 1.3 统一响应结构

#### 1.3.1 普通接口：AjaxResult

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

- `code = 200` 表示业务成功；`500` 表示业务失败；`401` 未授权/Token 过期；`403` 无权限；`601` 警告消息。
- 登录接口特殊字段：返回 `token`（与 `data` 同级）：
  ```json
  {
    "code": 200,
    "msg": "操作成功",
    "token": "eyJhbGciOiJIUzUxMiJ9..."
  }
  ```

#### 1.3.2 列表/分页接口：TableDataInfo

```json
{
  "code": 200,
  "msg": "查询成功",
  "total": 100,
  "rows": [
    { ... },
    { ... }
  ]
}
```

- `total`：总记录数。
- `rows`：当前页数据数组。
- 后端使用 PageHelper 分页，请求参数可传 `pageNum`（页码，默认 1）与 `pageSize`（每页条数，默认 10）。

### 1.4 图片/资源 URL

- 后端上传文件保存在 `D:/ruoyi/uploadPath`。
- 访问地址：`http://<host>:8080/profile/<relativePath>`。
- 业务表中图片字段（如 `imageUrl`、`coverUrl`、`qrCodeUrl`）返回的是完整 URL，APP 可直接加载。

---

## 2. 认证相关接口

### 2.1 登录

```http
POST /login
Content-Type: application/json
```

**请求体（LoginBody）：**

| 字段     | 类型   | 必填 | 说明                   |
|----------|--------|------|------------------------|
| username | string | 是   | 手机号/用户名          |
| password | string | 是   | 密码（BCrypt）         |
| code     | string | 否   | 验证码（math 类型）    |
| uuid     | string | 否   | 验证码唯一标识         |

**响应：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**注意：** `/login` 校验 `sys_user` 表，用于**管理后台**登录。APP 端手机号+密码登录请使用 `POST /batch/app/login`（校验 `batch_customer` 表，见第 13 节）。

### 2.2 获取当前登录用户信息

```http
GET /getInfo
Authorization: Bearer <token>
```

**响应：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "user": { "userId": 1, "userName": "admin", "phonenumber": "13800138000", ... },
  "roles": ["admin"],
  "permissions": ["*:*:*"]
}
```

### 2.3 获取路由（后台菜单）

```http
GET /getRouters
Authorization: Bearer <token>
```

### 2.4 注册

```http
POST /register
Content-Type: application/json
```

**请求体（RegisterBody）：**

| 字段         | 类型   | 必填 | 说明               |
|--------------|--------|------|--------------------|
| username     | string | 是   | 用户名             |
| password     | string | 是   | 密码               |
| confirmPassword | string | 是 | 确认密码           |
| code         | string | 否   | 验证码             |
| uuid         | string | 否   | 验证码唯一标识     |

**说明：** `/register` 写入 `sys_user`（管理后台账号体系），受 `sys.account.registerUser` 配置开关控制，**不用于 APP**。APP 扫码注册请使用 `POST /batch/app/register`（见第 13 节）。

### 2.5 验证码

```http
GET /captchaImage
```

返回图片流；同时响应头或 Cookie 不携带 uuid，需配合后端默认实现获取。

---

## 3. 客户/APP 账号接口（`/batch/customer`）

权限前缀：`batch:customer`

| 方法 | URL | 权限 | 说明 |
|------|-----|------|------|
| GET | `/batch/customer/list` | `batch:customer:list` | 客户列表（分公司管理员仅看本分公司数据） |
| POST | `/batch/customer/export` | `batch:customer:export` | 导出 Excel |
| POST | `/batch/customer/importTemplate` | `batch:customer:add` | 下载导入模板 |
| POST | `/batch/customer/importData` | `batch:customer:add` | 导入客户（Multipart） |
| GET | `/batch/customer/{customerId}` | `batch:customer:query` | 根据 ID 查询详情 |
| GET | `/batch/customer/phone/{phone}` | `batch:customer:query` | 根据手机号查询客户 |
| POST | `/batch/customer` | `batch:customer:add` | 新增客户 |
| PUT | `/batch/customer` | `batch:customer:edit` | 修改客户 |
| PUT | `/batch/customer/changeStatus` | `batch:customer:edit` | 修改状态（启用/禁用） |
| PUT | `/batch/customer/resetPassword` | `batch:customer:edit` | 重置客户登录密码（BCrypt 入库，密码长度 6-20 位） |
| DELETE | `/batch/customer/{customerIds}` | `batch:customer:remove` | 删除客户 |
| PUT | `/batch/customer/qrCode/{customerId}` | `batch:customer:resetQr` | 生成/重置注册二维码 |
| GET | `/batch/customer/qrCode/download/{customerId}` | `batch:customer:query` | 下载注册二维码（下载次数+1 后 302 重定向到二维码图片地址） |
| GET | `/batch/customer/qrCode/stat/{customerId}` | `batch:customer:query` | 查询该客户二维码推广累计统计 |
| PUT | `/batch/customer/upgrade/{customerId}` | `batch:customer:upgrade` | 账号升级 |
| PUT | `/batch/customer/migrate/{customerId}` | `batch:customer:migrate` | 账号迁移 |

**重置密码请求示例：**

```json
{
  "customerId": 100,
  "password": "newpass123"
}
```

**二维码统计响应示例（`qrCode/stat`）：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { "scanCount": 12, "downloadCount": 3, "registerCount": 5 }
}
```

> 统计来源为 `batch_qr_code_stat` 表（按 phone + stat_date 累计扫码/下载/注册次数），明细报表见 `/batch/statistics/qrcode`。

**BatchCustomer 主要字段：**

| 字段 | 类型 | 说明 |
|------|------|------|
| customerId | long | 客户 ID |
| customerType | int | 1 分公司 / 2 服务商 / 3 个人 |
| customerName | string | 账号名称 |
| contactName | string | 联系人 |
| phone | string | 手机号（全局唯一） |
| password | string | 登录密码（BCrypt 加密存储，仅写不读） |
| avatarUrl | string | 头像 URL（APP「账号资料编辑」上传后回写） |
| parentPhone | string | 上级手机号 |
| branchPhone | string | 所属分公司手机号 |
| maxServiceProvider | int | 分公司最大服务商数量 |
| totalIndividualCapacity | int | 分公司个人账号总容量 |
| maxIndividual | int | 服务商可创建个人上限 |
| computingPowerTotal | decimal | 算力总配额 |
| computingPowerUsed | decimal | 已消耗算力 |
| computingPowerRemain | decimal | 剩余算力 |
| vipExpireDate | date | VIP 有效期（yyyy-MM-dd） |
| qrCodeUrl | string | 注册二维码 URL |
| qrCodeKey | string | 二维码唯一 key |
| status | int | 0 启用 / 1 禁用 |
| subordinateCount | int | 下级数量（非表字段） |

**新增客户请求示例：**

```json
{
  "customerType": 3,
  "customerName": "测试个人",
  "contactName": "张三",
  "phone": "13800138001",
  "parentPhone": "13800138000",
  "computingPowerTotal": 1000,
  "vipExpireDate": "2026-12-31"
}
```

**账号升级请求示例：**

```json
{
  "parentPhone": "13800138000",
  "maxIndividual": 10
}
```

---

## 4. 首页接口（`/batch/home/*`）

权限前缀：`batch:home`

### 4.1 轮播图（`/batch/home/banner`）

| 方法 | URL | 权限 | 说明 |
|------|-----|------|------|
| GET | `/batch/home/banner/list` | `batch:home:list` | 轮播图列表（TableDataInfo） |
| GET | `/batch/home/banner/{bannerId}` | `batch:home:query` | 详情 |
| POST | `/batch/home/banner` | `batch:home:add` | 新增 |
| PUT | `/batch/home/banner` | `batch:home:edit` | 修改 |
| PUT | `/batch/home/banner/changeStatus` | `batch:home:edit` | 修改状态 |
| DELETE | `/batch/home/banner/{bannerIds}` | `batch:home:remove` | 删除 |

**BatchHomeBanner 字段：** `bannerId`, `title`, `imageUrl`, `linkUrl`, `sortWeight`, `status`, `delFlag`

### 4.2 功能入口（`/batch/home/entry`）

| 方法 | URL | 权限 | 说明 |
|------|-----|------|------|
| GET | `/batch/home/entry/list` | `batch:home:list` | 功能入口列表 |
| GET | `/batch/home/entry/{entryId}` | `batch:home:query` | 详情 |
| POST | `/batch/home/entry` | `batch:home:add` | 新增 |
| PUT | `/batch/home/entry` | `batch:home:edit` | 修改 |
| PUT | `/batch/home/entry/changeStatus` | `batch:home:edit` | 修改状态 |
| DELETE | `/batch/home/entry/{entryIds}` | `batch:home:remove` | 删除 |

**BatchHomeEntry 字段：** `entryId`, `entryName`, `iconUrl`, `targetType`(1 页面 / 2 URL / 3 功能码), `targetValue`, `sortWeight`, `status`

### 4.3 业绩喜报（`/batch/home/news`）

| 方法 | URL | 权限 | 说明 |
|------|-----|------|------|
| GET | `/batch/home/news/list` | `batch:home:list` | 喜报列表 |
| GET | `/batch/home/news/{newsId}` | `batch:home:query` | 详情 |
| POST | `/batch/home/news` | `batch:home:add` | 新增 |
| PUT | `/batch/home/news` | `batch:home:edit` | 修改 |
| PUT | `/batch/home/news/changeStatus` | `batch:home:edit` | 修改状态 |
| DELETE | `/batch/home/news/{newsIds}` | `batch:home:remove` | 删除 |

**BatchHomeNews 字段：** `newsId`, `newsTitle`, `championName`, `salesAmount`, `status`

### 4.4 教程入口（`/batch/home/tutorialEntry`）

| 方法 | URL | 权限 | 说明 |
|------|-----|------|------|
| GET | `/batch/home/tutorialEntry/list` | `batch:home:list` | 教程入口列表 |
| GET | `/batch/home/tutorialEntry/{entryId}` | `batch:home:query` | 详情 |
| POST | `/batch/home/tutorialEntry` | `batch:home:add` | 新增 |
| PUT | `/batch/home/tutorialEntry` | `batch:home:edit` | 修改 |
| PUT | `/batch/home/tutorialEntry/changeStatus` | `batch:home:edit` | 修改状态 |
| DELETE | `/batch/home/tutorialEntry/{entryIds}` | `batch:home:remove` | 删除 |
| GET | `/batch/home/tutorialEntry/documentList` | `batch:home:query` | 关联文档下拉列表 |

**BatchHomeTutorialEntry 字段：** `entryId`, `title`, `coverUrl`, `documentId`, `documentTitle`, `sortWeight`, `status`

---

## 5. 教程接口（`/batch/tutorial`）

权限前缀：`batch:tutorial`

| 方法 | URL | 权限 | 说明 |
|------|-----|------|------|
| GET | `/batch/tutorial/list` | `batch:tutorial:list` | 教程列表 |
| GET | `/batch/tutorial/{tutorialId}` | `batch:tutorial:query` | 教程详情 |
| POST | `/batch/tutorial` | `batch:tutorial:add` | 新增教程 |
| PUT | `/batch/tutorial` | `batch:tutorial:edit` | 修改教程 |
| PUT | `/batch/tutorial/changeStatus` | `batch:tutorial:edit` | 修改状态 |
| DELETE | `/batch/tutorial/{tutorialIds}` | `batch:tutorial:remove` | 删除教程 |
| GET | `/batch/tutorial/category/list` | `batch:tutorial:list` | 分类列表 |
| GET | `/batch/tutorial/category/all` | `batch:tutorial:list` | 所有有效分类 |
| GET | `/batch/tutorial/category/{categoryId}` | `batch:tutorial:query` | 分类详情 |
| POST | `/batch/tutorial/category` | `batch:tutorial:add` | 新增分类 |
| PUT | `/batch/tutorial/category` | `batch:tutorial:edit` | 修改分类 |
| DELETE | `/batch/tutorial/category/{categoryIds}` | `batch:tutorial:remove` | 删除分类 |

**BatchTutorial 字段：** `tutorialId`, `tutorialTitle`, `tutorialType`(1 视频 / 2 图文), `categoryId`, `categoryName`, `coverUrl`, `videoUrl`, `documentContent`, `intro`, `sortWeight`, `viewCount`, `status`

**BatchTutorialCategory 字段：** `categoryId`, `categoryName`, `sortWeight`, `status`

---

## 6. 文档接口（`/batch/document`）

权限前缀：`batch:document`

| 方法 | URL | 权限 | 说明 |
|------|-----|------|------|
| GET | `/batch/document/list` | `batch:document:list` | 文档列表 |
| GET | `/batch/document/{documentId}` | `batch:document:query` | 文档详情 |
| POST | `/batch/document` | `batch:document:add` | 新增文档 |
| PUT | `/batch/document` | `batch:document:edit` | 修改文档 |
| PUT | `/batch/document/changeStatus` | `batch:document:edit` | 修改状态 |
| DELETE | `/batch/document/{documentIds}` | `batch:document:remove` | 删除文档 |

**BatchDocument 字段：** `documentId`, `documentTitle`, `documentType`(1 用户协议 / 2 隐私政策 / 3 新手文档 / 4 帮助文档), `applyPages`, `content`, `sortWeight`, `status`, `isSystem`

---

## 7. 公告接口（`/batch/notice`）

权限前缀：`batch:notice`

| 方法 | URL | 权限 | 说明 |
|------|-----|------|------|
| GET | `/batch/notice/list` | `batch:notice:list` | 公告列表 |
| GET | `/batch/notice/{noticeId}` | `batch:notice:query` | 公告详情 |
| GET | `/batch/notice/preview/{noticeId}` | `batch:notice:query` | 预览公告 |
| POST | `/batch/notice` | `batch:notice:add` | 新增公告 |
| PUT | `/batch/notice` | `batch:notice:edit` | 修改公告 |
| DELETE | `/batch/notice/{noticeIds}` | `batch:notice:remove` | 删除公告 |
| PUT | `/batch/notice/publish/{noticeId}` | `batch:notice:publish` | 发布公告 |
| PUT | `/batch/notice/unpublish/{noticeId}` | `batch:notice:edit` | 下架公告 |

**BatchAppNotice 字段：** `noticeId`, `noticeTitle`, `noticeType`(1 通知 / 2 活动 / 3 重要更新), `coverUrl`, `content`, `publishStatus`(0 已发布 / 1 已下架 / 2 暂存), `publishTime`, `readCount`

---

## 8. 算力消耗日志接口（`/batch/computing/log`）

| 方法 | URL | 权限 | 说明 |
|------|-----|------|------|
| GET | `/batch/computing/log/list` | `batch:customer:query` | 算力消耗日志列表（后台，TableDataInfo） |
| GET | `/batch/computing/log/my` | `app:user` | APP 查询当前登录账号的算力消耗日志（强制按当前手机号过滤，支持分页） |
| POST | `/batch/computing/log/consume` | `app:user` | 消耗算力（APP 端下载/生成前调用），余额不足返回错误 |

**consume 请求体（ComputingConsumeBody）：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| operationType | int | 是 | 操作类型：1 生成 / 2 下载 |
| consumeValue | decimal | 是 | 消耗算力值 |
| bizNo | string | 否 | 业务单号/说明 |

**consume 响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "remain": 985.00
}
```

**BatchComputingPowerLog 字段：** `id`, `phone`, `operationType`(1 生成 / 2 下载), `consumeValue`, `remainValue`, `videoGroupName`, `createTime`

---

## 9. VIP 管理接口（`/batch/vip`）

权限前缀：`batch:vip`

| 方法 | URL | 权限 | 说明 |
|------|-----|------|------|
| GET | `/batch/vip/list` | `batch:vip:list` | VIP 客户列表 |
| PUT | `/batch/vip/{customerId}` | `batch:vip:edit` | 修改单个客户 VIP 有效期 |
| PUT | `/batch/vip/batch` | `batch:vip:edit` | 批量修改 VIP 有效期 |

**BatchVipQuery 字段：** 继承 BatchCustomer，并新增 `customerIds`（批量 ID 数组）与 `vipExpireDate`。

---

## 10. 系统配置接口（`/batch/config`）

权限前缀：`batch:config`

| 方法 | URL | 权限 | 说明 |
|------|-----|------|------|
| GET | `/batch/config/brand` | `batch:config:list` | 查询品牌配置 |
| POST | `/batch/config/brand` | `batch:config:edit` | 保存品牌配置 |
| GET | `/batch/config/global` | `batch:config:list` | 查询全局参数 |
| POST | `/batch/config/global` | `batch:config:edit` | 保存全局参数 |
| POST | `/batch/config/initGlobal` | `batch:config:add` | 初始化全局参数默认值 |
| GET | `/batch/config/version/list` | `batch:config:list` | APP 版本列表 |
| GET | `/batch/config/version/{versionId}` | `batch:config:query` | 版本详情 |
| POST | `/batch/config/version` | `batch:config:add` | 新增版本 |
| PUT | `/batch/config/version` | `batch:config:edit` | 修改版本 |
| DELETE | `/batch/config/version/{versionIds}` | `batch:config:remove` | 删除版本 |
| PUT | `/batch/config/version/changeStatus` | `batch:config:edit` | 修改版本状态 |
| GET | `/batch/config/list` | `batch:config:list` | 扩展全局参数列表 |
| GET | `/batch/config/{configId}` | `batch:config:query` | 扩展参数详情 |
| POST | `/batch/config` | `batch:config:add` | 新增扩展参数 |
| PUT | `/batch/config` | `batch:config:edit` | 修改扩展参数 |
| DELETE | `/batch/config/{configIds}` | `batch:config:remove` | 删除扩展参数 |

**品牌配置返回字段：** `appLogo`, `adminLogo`, `productName`, `slogan`, `primaryColor`, `loginBg`

**全局参数返回字段：** `maxVideos`, `sliceMin`, `sliceMax`, `sliceStep`, `emptyTip`, `parseFailTip`, `emptyPlaceholder`, `customerServiceHours`

默认值：

- `maxVideos = 10`
- `sliceMin = 0.5`
- `sliceMax = 10`
- `sliceStep = 0.1`
- `emptyTip = "当前算力已耗尽，请联系管理员增加算力额度"`
- `parseFailTip = "链接解析失败，请检查链接是否有效"`

---

## 11. 数据统计接口（`/batch/statistics`）

权限前缀：`batch:statistics`

| 方法 | URL | 权限 | 说明 |
|------|-----|------|------|
| GET | `/batch/statistics/overview` | `batch:statistics:query` | 今日概览指标（AjaxResult） |
| GET | `/batch/statistics/account` | `batch:statistics:query` | 账号数据明细列表 |
| GET | `/batch/statistics/computing` | `batch:statistics:query` | 算力消耗明细列表 |
| GET | `/batch/statistics/video` | `batch:statistics:query` | 视频生成明细列表 |
| GET | `/batch/statistics/qrcode` | `batch:statistics:query` | 二维码推广明细列表 |
| GET | `/batch/statistics/news` | `batch:statistics:query` | 业绩喜报明细列表 |
| GET | `/batch/statistics/trend` | `batch:statistics:query` | 趋势数据（AjaxResult） |

**查询参数（BatchStatisticsQuery）：** `startDate`, `endDate`, `customerType`, `branchPhone`, `phone`, `days`，以及分页参数 `pageNum`/`pageSize`。

---

## 12. 通用文件接口

| 方法 | URL | 说明 |
|------|-----|------|
| POST | `/common/upload` | 单文件上传（Multipart：file） |
| POST | `/common/uploads` | 多文件上传（Multipart：files） |
| GET | `/common/download?fileName=xxx&delete=false` | 通用下载 |
| GET | `/common/download/resource?resource=xxx` | 本地资源下载 |

上传成功响应示例：

```json
{
  "code": 200,
  "msg": "操作成功",
  "url": "http://localhost:8080/profile/upload/2026/07/03/xxx.jpg",
  "fileName": "...",
  "newFileName": "...",
  "originalFilename": "..."
}
```

---

## 13. APP 端认证与账号接口（`/batch/app`）

Controller：`BatchAppAuthController`。除登录/注册外均需 JWT；登录成功后权限标识为 `app:user`，Token 的 `username` 即手机号、`userId` 即 `customerId`。

| 方法 | URL | 匿名 | 说明 |
|------|-----|------|------|
| POST | `/batch/app/login` | 是 | APP 手机号+密码登录（校验 `batch_customer`） |
| POST | `/batch/app/register` | 是 | APP 注册（个人账号，可带上级手机号） |
| GET | `/batch/app/customer/phone/{phone}` | 否 | 查询当前账号信息（仅可查本人手机号） |
| PUT | `/batch/app/customer` | 否 | 更新资料（仅 `customerName`/`contactName`/`avatarUrl`，敏感字段服务端强制忽略） |
| POST | `/batch/app/changePassword` | 否 | 修改当前账号密码 |
| POST | `/batch/app/logout` | 否 | 退出登录（清除 Token 缓存） |
| POST | `/batch/app/upload` | 否 | 文件上传（头像等，Multipart：`file`），返回完整 URL |
| DELETE | `/batch/app/customer` | 否 | 自助注销当前账号（存在下级账号时拦截） |

### 13.1 APP 登录

```http
POST /batch/app/login
Content-Type: application/json
```

**请求体（AppLoginBody）：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 是 | 手机号（`^1[3-9]\d{9}$`） |
| password | string | 是 | 密码 |

**响应：** `token` 与 `data`（BatchCustomer）同级返回。账号被禁用返回「账号已被禁用，请联系管理员」。

### 13.2 APP 注册

```http
POST /batch/app/register
Content-Type: application/json
```

**请求体（AppRegisterBody）：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 是 | 手机号（全局唯一，重复返回「该手机号已被注册」） |
| password | string | 是 | 密码（BCrypt 入库） |
| parentPhone | string | 否 | 上级手机号（扫码注册时由二维码解析带入） |

注册成功后直接返回 `token` 与 `data`（BatchCustomer），默认创建为个人账号（customerType=3），同时累计上级账号的二维码注册次数。

### 13.3 APP 修改密码

```http
POST /batch/app/changePassword
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体（ChangePasswordBody）：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | string | 是 | 原密码（错误返回「原密码错误」） |
| newPassword | string | 是 | 新密码（长度 6-20 位） |

---

## 14. AI 云创视频接口（`/batch/ai/video`）

Controller：`BatchAiVideoController`。APP 端接口权限均为 `app:user`，且服务端校验视频组归属（仅可操作本账号的组）；生成记录后台列表权限为 `batch:aivideo:list`。

### 14.1 视频组管理

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/batch/ai/video/group/list` | 当前账号视频组列表（TableDataInfo 分页） |
| GET | `/batch/ai/video/group/{groupId}` | 视频组详情（含分镜头 `clips`） |
| POST | `/batch/ai/video/group` | 新增视频组（返回 `groupId`） |
| PUT | `/batch/ai/video/group` | 修改视频组（含分镜头覆盖保存） |
| DELETE | `/batch/ai/video/group/{groupId}` | 删除视频组 |

**BatchAiVideoGroup 字段：** `groupId`, `phone`, `groupName`, `generatedCount`, `maxLimit`, `status`(0 启用 / 1 禁用), `sortWeight`, `clips`（非持久化）

**BatchAiVideoClip 字段：** `clipId`, `groupId`, `videoUrl`, `textContent`（口播文案）, `duration`（秒）, `sortOrder`

### 14.2 视频素材上传

```http
POST /batch/ai/video/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

- 表单字段：`file`。
- 允许格式：`mp4 / mov / avi / flv / mkv / webm`。
- 保存至 `profile/video/upload`，`data` 返回可访问的完整 URL（供 split/generate 使用）。

### 14.3 AI 分割

```http
POST /batch/ai/video/split
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体（BatchAiVideoSplitBody）：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| groupId | long | 是 | 视频组 ID |
| videoUrl | string | 是 | 已上传视频 URL（`/profile/` 开头或完整 URL） |
| sliceDuration | double | 是 | 切片时长（秒），区间 0.5~10 |

**响应：** `data` 为切片结果 `BatchAiVideoClip[]`（服务端调用 FFmpeg 切段并保存为该组分镜头）。

### 14.4 提交批量生成

```http
POST /batch/ai/video/generate
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体（BatchAiVideoGenerateBody）：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| groupId | long | 是 | 视频组 ID |
| count | int | 否 | 本次生成数量，默认 1，上限 10（超出返回「单次最多生成 10 个视频」） |
| consumeValue | int | 否 | 本次生成单条消耗算力（缺省按全局参数计算） |
| clips | array | 否 | 分镜头列表（提交时以当前分镜为准） |

- 总消耗算力 = `count × 单条成本`，提交时前置校验并扣减算力，余额不足直接失败。
- 异步合成（FFmpeg），需通过任务接口轮询进度。

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "logId": 1024,
  "logIds": [1024, 1025, 1026]
}
```

### 14.5 生成任务查询（APP 轮询）

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/batch/ai/video/task/list?groupId={groupId}` | 该组生成任务列表（按时间倒序，校验组归属） |
| GET | `/batch/ai/video/task/{logId}` | 单条生成任务详情（仅本人或 admin 可查） |

**BatchAiVideoGenerateLog 字段：** `logId`, `phone`, `groupId`, `videoGroupName`, `generateCount`(每条记录固定为 1), `consumeValue`, `status`(0 处理中 / 1 成功 / 2 失败), `progress`(0-100), `resultUrl`（产出视频 URL）, `errorMsg`, `clipSeed`（去重随机种子）, `createTime`

### 14.6 生成记录列表（管理后台）

```http
GET /batch/ai/video/log/list
```

- 权限：`batch:aivideo:list`；TableDataInfo 分页，支持按 `BatchAiVideoGenerateLog` 字段（如 `phone`、`groupId`、`status`）筛选。

---

## 15. 注册二维码接口

### 15.1 扫码统计入口（匿名）

```http
GET /batch/qrcode/scan?phone={phone}
```

- `@Anonymous` 注解放行，无需登录。
- 注册二维码内容即指向本接口：扫码后对应账号的扫码次数 +1（`batch_qr_code_stat`），然后 **302 重定向** 到 APP 下载地址（配置项 `batch.app.download-url`）。

### 15.2 后台二维码管理

见第 3 节客户接口表：`PUT /batch/customer/qrCode/{customerId}`（生成/重置）、`GET /batch/customer/qrCode/download/{customerId}`（下载并计数）、`GET /batch/customer/qrCode/stat/{customerId}`（累计统计）。

---

## 16. APP 端对接现状与剩余缺口

### 16.1 已落地的接口（原缺口清单回顾）

| 需求 | 状态 | 说明 |
|------|------|------|
| APP 手机号+密码登录 | ✅ 已实现 | `POST /batch/app/login`（第 13 节），不再依赖 `sys_user` |
| APP 扫码注册 | ✅ 已实现 | `POST /batch/app/register`，支持 `parentPhone` 上级绑定 |
| APP 修改密码 | ✅ 已实现 | `POST /batch/app/changePassword`；后台重置用 `PUT /batch/customer/resetPassword` |
| 算力检查/扣减 | ✅ 已实现 | `POST /batch/computing/log/consume` 前置扣减；AI 生成接口内部亦直接校验扣减；余额查询见 `/batch/computing/log/my` |
| 视频解析/去水印 | ✅ 已实现 | `/batch/watermark/parse`（POST 解析、GET list/{id} 查询、DELETE 删除），服务端调用 `scripts/video_parse.py`（Python + Playwright）解析抖音/小红书 |
| AI 视频分割/生成 | ✅ 已实现 | `/batch/ai/video/split`、`/generate`、`/task/list`、`/task/{logId}`（第 14 节），FFmpeg 异步合成 |
| 二维码推广统计 | ✅ 已实现 | `GET /batch/qrcode/scan` 匿名计数 + `/batch/customer/qrCode/*` 后台管理 |
| 头像上传 | ✅ 已实现 | `POST /batch/app/upload` + `PUT /batch/app/customer`（`avatarUrl` 字段） |

### 16.2 目前真正剩余的缺口

| 需求 | 当前后端状态 | 建议 |
|------|--------------|------|
| 忘记密码/短信验证码找回 | 无接口（也无短信通道） | 需新增验证码下发与校验接口，或暂由后台 `PUT /batch/customer/resetPassword` 人工重置 |
| 抖音/微信等第三方账号绑定 | 无接口，`batch_customer` 无对应字段 | 需新增绑定关系表与绑定/解绑接口（高危操作需二次确认） |
| APP 版本更新检查 | 后台已有 `/batch/config/version/*` 管理接口 | APP 端尚无匿名/免鉴权的「检查更新」查询接口，可按需补充 |

---

## 17. 附录：HttpStatus 状态码对照

| 状态码 | 含义 |
|--------|------|
| 200 | 操作成功 |
| 201 | 对象创建成功 |
| 400 | 参数列表错误 |
| 401 | 未授权 / Token 无效或过期 |
| 403 | 访问受限 / 无权限 |
| 404 | 资源未找到 |
| 500 | 系统内部错误 / 业务失败 |
| 601 | 系统警告消息 |

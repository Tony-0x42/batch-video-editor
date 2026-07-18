# 内容页 API 真实化改造 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 APP 内容页（学习专区/新手指南/品牌专区/联系我们/用户协议）及登录注册的数据源从 `MockDataProvider` 切换到 `ApiService` 真实后端接口，并补齐缺失的客服联系接口。

**Architecture:** 5 个内容页按后端现成只读接口拉取数据并映射到现有 Bean；登录注册使用 `/batch/app/login` 与 `/batch/app/register` 返回的 token 与客户信息；客服联系由于后端无现成接口，新增最小 `batch_contact` 表 + Controller/Service/Mapper，APP 通过 `/batch/contact/list` 读取。

**Tech Stack:** Android (Java + Hilt + Retrofit), RuoYi (Spring Boot + MyBatis)

## Global Constraints

- 不要删除 `MockDataProvider`，但目标页面不要再调用它。
- 登录/注册成功后必须调用 `TokenManager.saveToken(token)`，并通过 `UserSession.saveLogin(context, BatchCustomerDto)` 保存客户信息。
- 需要认证的接口使用 `apiService.xxx().enqueue(new ApiCallback<T>() { onSuccess / onError })`。
- 登录/注册接口使用 `apiService.appLogin/appRegister().enqueue(new AuthApiCallback<BatchCustomerDto>() { onSuccess(String token, BatchCustomerDto data) })`。
- 保持现有 UI 和交互不变，只改数据源；添加 ProgressBar 加载与 Toast 错误提示。
- 后端 MyBatis XML 中 `>` / `<` 等按规范转义。
- 编译验证：`./gradlew :app:compileDebugJavaWithJavac :app:compileDebugKotlin`

---

## Task 1: LearningActivity 对接 `/batch/tutorial/list`

**Files:**
- Modify: `android/app/src/main/java/com/example/cj/videoeditor/activity/LearningActivity.java`
- Modify: `android/app/src/main/res/layout/activity_learning.xml`

**Interfaces:**
- Consumes: `ApiService.getTutorialList()` → `PageResponse<BatchTutorialDto>`
- Produces: `List<Material>` with `Material.Type.VIDEO` (tutorialType=1) or `DOCUMENT` (tutorialType=2)

- [ ] **Step 1: Add ProgressBar to layout**

In `activity_learning.xml`, wrap the `FrameLayout` content with another `FrameLayout` and add an indeterminate `ProgressBar` centered:

```xml
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout ...existing TabLayout + FrameLayout... />

    <ProgressBar
        android:id="@+id/progress_bar"
        style="?android:attr/progressBarStyleLarge"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />
</FrameLayout>
```

- [ ] **Step 2: Inject ApiService and call tutorial list**

Replace `initData()` mock loading with Retrofit call:

```java
@Inject
ApiService apiService;

private ProgressBar progressBar;

@Override
protected void initViews() {
    ...
    progressBar = findViewById(R.id.progress_bar);
}

@Override
protected void initData() {
    progressBar.setVisibility(View.VISIBLE);
    apiService.getTutorialList().enqueue(new ApiCallback<List<BatchTutorialDto>>() {
        @Override
        public void onSuccess(List<BatchTutorialDto> data) {
            progressBar.setVisibility(View.GONE);
            allMaterials = mapTutorials(data);
            filterMaterials(0);
        }

        @Override
        public void onError(String msg) {
            progressBar.setVisibility(View.GONE);
            ToastUtil.show(LearningActivity.this, msg);
        }
    });
}

private List<Material> mapTutorials(List<BatchTutorialDto> list) {
    List<Material> result = new ArrayList<>();
    if (list == null) return result;
    for (BatchTutorialDto dto : list) {
        Material.Type type = "1".equals(dto.getTutorialType()) ? Material.Type.VIDEO : Material.Type.DOCUMENT;
        result.add(new Material(
                String.valueOf(dto.getTutorialId()),
                dto.getTutorialTitle(),
                type,
                dto.getCoverUrl(),
                "1".equals(dto.getTutorialType()) ? dto.getVideoUrl() : dto.getDocumentContent(),
                dto.getCreateTime(),
                dto.getViewCount() != null ? dto.getViewCount() : 0
        ));
    }
    return result;
}
```

- [ ] **Step 3: Remove MockDataProvider import**

Delete `import com.example.cj.videoeditor.utils.MockDataProvider;`.

- [ ] **Step 4: Compile check**

Run: `./gradlew :app:compileDebugJavaWithJavac :app:compileDebugKotlin`

---

## Task 2: DocumentActivity 对接 `/batch/document/list`

**Files:**
- Modify: `android/app/src/main/java/com/example/cj/videoeditor/activity/DocumentActivity.java`
- Modify: `android/app/src/main/res/layout/activity_document.xml`

**Interfaces:**
- Consumes: `ApiService.getDocumentList()` → `PageResponse<BatchDocumentDto>`
- Produces: `List<Document>`; category 取自 `applyPages`，空则默认“常见问题”

- [ ] **Step 1: Add ProgressBar to layout**

Same pattern as Task 1; add `R.id.progress_bar` to `activity_document.xml`.

- [ ] **Step 2: Replace mock loading with API call**

```java
@Inject
ApiService apiService;
private ProgressBar progressBar;

@Override
protected void initViews() {
    ...
    progressBar = findViewById(R.id.progress_bar);
}

@Override
protected void initData() {
    progressBar.setVisibility(View.VISIBLE);
    apiService.getDocumentList().enqueue(new ApiCallback<List<BatchDocumentDto>>() {
        @Override
        public void onSuccess(List<BatchDocumentDto> data) {
            progressBar.setVisibility(View.GONE);
            allDocuments = mapDocuments(data);
            filterDocuments(0);
        }

        @Override
        public void onError(String msg) {
            progressBar.setVisibility(View.GONE);
            ToastUtil.show(DocumentActivity.this, msg);
        }
    });
}

private List<Document> mapDocuments(List<BatchDocumentDto> list) {
    List<Document> result = new ArrayList<>();
    if (list == null) return result;
    for (BatchDocumentDto dto : list) {
        Integer type = dto.getDocumentType();
        if (type == null || (type != 3 && type != 4)) continue; // 只展示新手文档/帮助文档
        String category = normalizeCategory(dto.getApplyPages());
        result.add(new Document(
                String.valueOf(dto.getDocumentId()),
                dto.getDocumentTitle(),
                category,
                dto.getUpdateTime(),
                dto.getContent()
        ));
    }
    return result;
}

private String normalizeCategory(String applyPages) {
    if (applyPages == null || applyPages.trim().isEmpty()) return "常见问题";
    String first = applyPages.split(",")[0].trim();
    if ("快速上手".equals(first) || "操作指南".equals(first)) return first;
    return "常见问题";
}
```

- [ ] **Step 3: Remove MockDataProvider import**

- [ ] **Step 4: Compile check**

---

## Task 3: BrandActivity 对接 `/batch/config/brand`

**Files:**
- Modify: `android/app/src/main/java/com/example/cj/videoeditor/activity/BrandActivity.java`
- Modify: `android/app/src/main/res/layout/activity_brand.xml`

**Interfaces:**
- Consumes: `ApiService.getBrandConfig()` → `BaseResponse<Map<String, Object>>`
- Produces: `List<Brand>` with one item mapped from brand config

- [ ] **Step 1: Add ProgressBar to layout**

Add `R.id.progress_bar` centered in `activity_brand.xml`.

- [ ] **Step 2: Replace mock loading with API call**

```java
@Inject
ApiService apiService;
private ProgressBar progressBar;
private RecyclerView rvBrands;
private TextView tvEmpty;

@Override
protected void initViews() {
    setTitle(getString(R.string.brand));
    rvBrands = findViewById(R.id.rv_brands);
    tvEmpty = findViewById(R.id.tv_empty);
    progressBar = findViewById(R.id.progress_bar);
    rvBrands.setLayoutManager(new GridLayoutManager(this, 2));
    loadBrands();
}

private void loadBrands() {
    progressBar.setVisibility(View.VISIBLE);
    apiService.getBrandConfig().enqueue(new ApiCallback<Map<String, Object>>() {
        @Override
        public void onSuccess(Map<String, Object> data) {
            progressBar.setVisibility(View.GONE);
            List<Brand> brands = mapBrandConfig(data);
            if (brands.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvBrands.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvBrands.setVisibility(View.VISIBLE);
                rvBrands.setAdapter(new BrandAdapter(brands, brand -> {
                    ToastUtil.show(BrandActivity.this, brand.getName() + "\n" + brand.getDetail());
                }));
            }
        }

        @Override
        public void onError(String msg) {
            progressBar.setVisibility(View.GONE);
            ToastUtil.show(BrandActivity.this, msg);
        }
    });
}

private List<Brand> mapBrandConfig(Map<String, Object> data) {
    List<Brand> list = new ArrayList<>();
    if (data == null) return list;
    String name = toString(data.get("productName"));
    if (name.isEmpty()) return list;
    String slogan = toString(data.get("slogan"));
    String detail = toString(data.get("productName")) + (slogan.isEmpty() ? "" : "\n" + slogan);
    List<String> media = new ArrayList<>();
    String loginBg = toString(data.get("loginBg"));
    if (!loginBg.isEmpty()) media.add(loginBg);
    list.add(new Brand("1", name, toString(data.get("appLogo")), slogan, detail, media));
    return list;
}

private String toString(Object value) {
    return value == null ? "" : String.valueOf(value);
}
```

- [ ] **Step 3: Remove MockDataProvider import**

- [ ] **Step 4: Compile check**

---

## Task 4: 后端新增客服联系模块并对接 ContactActivity

**Files (Backend):**
- Create: `server/ruoyi-system/src/main/java/com/ruoyi/batch/contact/domain/BatchContact.java`
- Create: `server/ruoyi-system/src/main/java/com/ruoyi/batch/contact/mapper/BatchContactMapper.java`
- Create: `server/ruoyi-system/src/main/resources/mapper/batch/contact/BatchContactMapper.xml`
- Create: `server/ruoyi-system/src/main/java/com/ruoyi/batch/contact/service/IBatchContactService.java`
- Create: `server/ruoyi-system/src/main/java/com/ruoyi/batch/contact/service/impl/BatchContactServiceImpl.java`
- Create: `server/ruoyi-system/src/main/java/com/ruoyi/batch/contact/controller/BatchContactController.java`
- Modify: `server/sql/batch_business.sql`

**Files (APP):**
- Modify: `android/app/src/main/java/com/example/cj/videoeditor/network/ApiService.java`
- Create: `android/app/src/main/java/com/example/cj/videoeditor/network/dto/BatchContactDto.java`
- Modify: `android/app/src/main/java/com/example/cj/videoeditor/activity/ContactActivity.java`
- Modify: `android/app/src/main/res/layout/activity_contact.xml`

**Interfaces:**
- Backend produces: `GET /batch/contact/list` → `AjaxResult` with `{ onlinePhone, headquarterPhone, contacts: [...] }`
- APP consumes: `ApiService.getContactList()` → `BaseResponse<BatchContactDto>`

- [ ] **Step 1: Add SQL table**

Append to `server/sql/batch_business.sql`:

```sql
-- APP 客服联系表
drop table if exists batch_contact;
create table batch_contact (
  contact_id      bigint(20)      not null auto_increment    comment '联系ID',
  contact_name    varchar(100)    not null default ''        comment '联系人/名称',
  region          varchar(100)    default ''                 comment '区域/说明',
  phone           varchar(20)     not null default ''        comment '电话',
  contact_type    tinyint(1)      not null default 1         comment '类型：1 在线客服 / 2 总部热线 / 3 区域联系',
  sort_weight     int(11)         default 0                  comment '排序权重',
  status          tinyint(1)      not null default 0         comment '状态：0 启用 / 1 禁用',
  del_flag        tinyint(1)      not null default 0         comment '删除标志：0 存在 / 2 删除',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime        default current_timestamp  comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime        default current_timestamp  on update current_timestamp comment '更新时间',
  remark          varchar(500)    default ''                 comment '备注',
  primary key (contact_id),
  key idx_batch_contact_type (contact_type),
  key idx_batch_contact_status (status),
  key idx_batch_contact_del_flag (del_flag),
  key idx_batch_contact_sort_weight (sort_weight)
) engine=innodb auto_increment=1 comment = 'APP 客服联系表';
```

- [ ] **Step 2: Create backend domain**

`BatchContact.java` (standard BaseEntity fields + getters/setters).

- [ ] **Step 3: Create Mapper + XML**

`BatchContactMapper.java` with `selectBatchContactList(BatchContact contact)` and basic CRUD.

`BatchContactMapper.xml`:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.batch.contact.mapper.BatchContactMapper">
    <resultMap type="com.ruoyi.batch.contact.domain.BatchContact" id="BatchContactResult">
        <id property="contactId" column="contact_id" />
        <result property="contactName" column="contact_name" />
        <result property="region" column="region" />
        <result property="phone" column="phone" />
        <result property="contactType" column="contact_type" />
        <result property="sortWeight" column="sort_weight" />
        <result property="status" column="status" />
        <result property="delFlag" column="del_flag" />
        <result property="createBy" column="create_by" />
        <result property="createTime" column="create_time" />
        <result property="updateBy" column="update_by" />
        <result property="updateTime" column="update_time" />
        <result property="remark" column="remark" />
    </resultMap>

    <sql id="selectBatchContactVo">
        select contact_id, contact_name, region, phone, contact_type, sort_weight, status, del_flag,
               create_by, create_time, update_by, update_time, remark
        from batch_contact
    </sql>

    <select id="selectBatchContactList" parameterType="com.ruoyi.batch.contact.domain.BatchContact" resultMap="BatchContactResult">
        <include refid="selectBatchContactVo"/>
        <where>
            del_flag = 0
            <if test="contactType != null">AND contact_type = #{contactType}</if>
            <if test="status != null">AND status = #{status}</if>
        </where>
        order by contact_type, sort_weight, contact_id
    </select>

    <!-- insert/update/delete/selectById 按标准 RuoYi 模式补齐 -->
</mapper>
```

- [ ] **Step 4: Create Service + Impl**

`IBatchContactService` defines `selectBatchContactList(BatchContact contact)` and CRUD.
`BatchContactServiceImpl` implements.

- [ ] **Step 5: Create Controller**

```java
@RestController
@RequestMapping("/batch/contact")
public class BatchContactController extends BaseController {
    @Autowired
    private IBatchContactService contactService;

    @PreAuthorize("isAnonymous() or @ss.hasPermi('batch:config:list') or @ss.hasPermi('app:user')")
    @GetMapping("/list")
    public AjaxResult list(BatchContact contact) {
        contact.setStatus(0);
        contact.setDelFlag(0);
        List<BatchContact> list = contactService.selectBatchContactList(contact);
        String onlinePhone = "";
        String headquarterPhone = "";
        List<Map<String, Object>> contacts = new ArrayList<>();
        for (BatchContact c : list) {
            if (c.getContactType() == 1) onlinePhone = c.getPhone();
            else if (c.getContactType() == 2) headquarterPhone = c.getPhone();
            else {
                Map<String, Object> m = new HashMap<>();
                m.put("name", c.getContactName());
                m.put("region", c.getRegion());
                m.put("phone", c.getPhone());
                contacts.add(m);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("onlinePhone", onlinePhone);
        result.put("headquarterPhone", headquarterPhone);
        result.put("contacts", contacts);
        return AjaxResult.success(result);
    }

    // CRUD endpoints with @PreAuthorize("@ss.hasPermi('batch:config:xxx')") if needed
}
```

- [ ] **Step 6: Add APP DTO**

`BatchContactDto.java`:
```java
public class BatchContactDto {
    @SerializedName("onlinePhone") private String onlinePhone;
    @SerializedName("headquarterPhone") private String headquarterPhone;
    @SerializedName("contacts") private List<ContactItem> contacts;

    public static class ContactItem {
        @SerializedName("name") private String name;
        @SerializedName("region") private String region;
        @SerializedName("phone") private String phone;
        // getters/setters
    }
    // getters/setters
}
```

- [ ] **Step 7: Add ApiService method**

```java
@GET("batch/contact/list")
Call<BaseResponse<BatchContactDto>> getContactList();
```

- [ ] **Step 8: Update ContactActivity**

Add `ProgressBar` to `activity_contact.xml`, inject `ApiService`, and call `getContactList()`.

```java
@Inject
ApiService apiService;
private ProgressBar progressBar;
private TextView tvOnlinePhone, tvHeadquarterPhone;
private RecyclerView rvContacts;

@Override
protected void initViews() {
    setTitle(getString(R.string.contact));
    tvOnlinePhone = findViewById(R.id.tv_online_phone);
    tvHeadquarterPhone = findViewById(R.id.tv_headquarter_phone);
    rvContacts = findViewById(R.id.rv_contacts);
    progressBar = findViewById(R.id.progress_bar);
    rvContacts.setLayoutManager(new LinearLayoutManager(this));
    loadContacts();
}

private void loadContacts() {
    progressBar.setVisibility(View.VISIBLE);
    apiService.getContactList().enqueue(new ApiCallback<BatchContactDto>() {
        @Override
        public void onSuccess(BatchContactDto data) {
            progressBar.setVisibility(View.GONE);
            if (data == null) return;
            String online = data.getOnlinePhone();
            String hq = data.getHeadquarterPhone();
            tvOnlinePhone.setText(TextUtils.isEmpty(online) ? "暂无" : online);
            tvHeadquarterPhone.setText(TextUtils.isEmpty(hq) ? "暂无" : hq);
            findViewById(R.id.tv_online_phone).setOnClickListener(v -> dial(online));
            findViewById(R.id.tv_headquarter_phone).setOnClickListener(v -> dial(hq));
            List<Contact> contacts = new ArrayList<>();
            if (data.getContacts() != null) {
                for (BatchContactDto.ContactItem item : data.getContacts()) {
                    contacts.add(new Contact(item.getName(), item.getRegion(), item.getPhone()));
                }
            }
            rvContacts.setAdapter(new ContactAdapter(contacts));
        }

        @Override
        public void onError(String msg) {
            progressBar.setVisibility(View.GONE);
            ToastUtil.show(ContactActivity.this, msg);
        }
    });
}
```

- [ ] **Step 9: Remove MockDataProvider import and compile**

---

## Task 5: AgreementActivity 对接 `/batch/document/list`

**Files:**
- Modify: `android/app/src/main/java/com/example/cj/videoeditor/activity/AgreementActivity.java`

**Interfaces:**
- Consumes: `ApiService.getDocumentList()` → `PageResponse<BatchDocumentDto>`
- Produces: HTML body from `documentType == 1` (用户协议)

- [ ] **Step 1: Inject ApiService and load user agreement**

```java
@Inject
ApiService apiService;

@Override
protected void initViews() {
    setTitle(getString(R.string.user_agreement));
    webView = findViewById(R.id.web_view);
    progressBar = findViewById(R.id.progress_bar);
    webView.getSettings().setJavaScriptEnabled(false);
    webView.setWebChromeClient(...);
    loadAgreement();
}

private void loadAgreement() {
    progressBar.setVisibility(View.VISIBLE);
    apiService.getDocumentList().enqueue(new ApiCallback<List<BatchDocumentDto>>() {
        @Override
        public void onSuccess(List<BatchDocumentDto> data) {
            progressBar.setVisibility(View.GONE);
            String content = findAgreementContent(data);
            if (content == null || content.isEmpty()) {
                ToastUtil.show(AgreementActivity.this, "未配置用户协议");
            }
            webView.loadDataWithBaseURL(null, wrapHtml(content != null ? content : ""), "text/html", "utf-8", null);
        }

        @Override
        public void onError(String msg) {
            progressBar.setVisibility(View.GONE);
            ToastUtil.show(AgreementActivity.this, msg);
        }
    });
}

private String findAgreementContent(List<BatchDocumentDto> list) {
    if (list == null) return null;
    for (BatchDocumentDto dto : list) {
        if (dto.getDocumentType() != null && dto.getDocumentType() == 1) {
            return dto.getContent();
        }
    }
    return null;
}
```

- [ ] **Step 2: Remove MockDataProvider import and compile**

---

## Task 6: LoginActivity 对接 `/batch/app/login`

**Files:**
- Modify: `android/app/src/main/java/com/example/cj/videoeditor/activity/LoginActivity.java`

**Interfaces:**
- Consumes: `ApiService.appLogin(AppLoginBody)` → `BaseResponse<BatchCustomerDto>`
- Produces: token saved via `TokenManager`; customer info via `UserSession.saveLogin(context, customer)`

- [ ] **Step 1: Enable Hilt injection and add fields**

```java
@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    @Inject
    ApiService apiService;
    @Inject
    TokenManager tokenManager;
    ...
}
```

- [ ] **Step 2: Replace simulated login with real login**

```java
private void attemptLogin() {
    ...validation unchanged...
    login(phone, password);
}

private void login(String phone, String password) {
    btnLogin.setEnabled(false);
    apiService.appLogin(new AppLoginBody(phone, password)).enqueue(new AuthApiCallback<BatchCustomerDto>() {
        @Override
        public void onSuccess(String token, BatchCustomerDto data) {
            btnLogin.setEnabled(true);
            tokenManager.saveToken(token);
            UserSession.saveLogin(LoginActivity.this, data);
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        @Override
        public void onError(String msg) {
            btnLogin.setEnabled(true);
            ToastUtil.show(LoginActivity.this, msg);
        }
    });
}
```

- [ ] **Step 3: Remove simulateLogin and MockDataProvider import**

- [ ] **Step 4: Compile check**

---

## Task 7: RegisterActivity 对接 `/batch/app/register`

**Files:**
- Modify: `android/app/src/main/java/com/example/cj/videoeditor/activity/RegisterActivity.java`

**Interfaces:**
- Consumes: `ApiService.appRegister(AppRegisterBody)` → `BaseResponse<BatchCustomerDto>`
- Produces: token + customer info saved

- [ ] **Step 1: Inject ApiService and TokenManager**

`@Inject ApiService apiService; @Inject TokenManager tokenManager;`

- [ ] **Step 2: Replace delayed mock register with real register**

```java
private void attemptRegister() {
    ...validation unchanged...
    register(phone, password);
}

private void register(String phone, String password) {
    btnRegister.setEnabled(false);
    btnRegister.setText(R.string.loading);
    String parentPhone = getIntent().getStringExtra("parent_phone");
    apiService.appRegister(new AppRegisterBody(phone, password, parentPhone))
        .enqueue(new AuthApiCallback<BatchCustomerDto>() {
            @Override
            public void onSuccess(String token, BatchCustomerDto data) {
                tokenManager.saveToken(token);
                UserSession.saveLogin(RegisterActivity.this, data);
                ToastUtil.show(RegisterActivity.this, "注册成功");
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String msg) {
                btnRegister.setEnabled(true);
                btnRegister.setText(R.string.register);
                ToastUtil.show(RegisterActivity.this, msg);
            }
        });
}
```

- [ ] **Step 3: Remove MockDataProvider import**

- [ ] **Step 4: Compile check**

---

## Task 8: 最终全量编译与回归检查

- [ ] **Step 1: 全局 MockDataProvider 调用扫描**

Run: `grep -R "MockDataProvider" android/app/src/main/java/com/example/cj/videoeditor/activity/`

Expected: only `CustomerServiceActivity.java`, `EditProfileActivity.java`, `PrivacyActivity.java` (非本次目标页) 仍可保留；5 个目标页 + Login/Register 不再引用。

- [ ] **Step 2: 后端编译**

Run: `D:/tools/bin/mvn.cmd -f server/pom.xml -pl ruoyi-system -am clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: APP 编译**

Run: `./gradlew :app:compileDebugJavaWithJavac :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

---

## Self-Review

**Spec coverage:**
- LearningActivity / DocumentActivity / BrandActivity / ContactActivity / AgreementActivity 全部替换为真实接口：✓
- 登录注册 token 保存 + 客户信息保存：✓
- 缺失接口补齐（客服联系）：✓
- 加载状态与错误提示：✓
- 不删除 MockDataProvider：✓

**Placeholder scan:** 无 TBD/TODO。

**Type consistency:** `BatchContactDto` 与 `ApiService.getContactList()` 返回类型一致；`AuthApiCallback<BatchCustomerDto>` 与 `ApiService.appLogin/appRegister` 一致。

<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
      <!-- 品牌配置 -->
      <el-tab-pane label="品牌配置" name="brand">
        <el-form ref="brandForm" :model="brandForm" :rules="brandRules" label-width="120px" style="max-width: 700px;">
          <el-form-item label="APP Logo" prop="appLogo">
            <image-upload v-model="brandForm.appLogo" :limit="1" />
            <div class="form-tip">建议尺寸 200 x 200 像素，用于 APP 启动页及我的页</div>
          </el-form-item>
          <el-form-item label="后台 Logo" prop="adminLogo">
            <image-upload v-model="brandForm.adminLogo" :limit="1" />
            <div class="form-tip">建议尺寸 200 x 60 像素，用于管理后台顶部</div>
          </el-form-item>
          <el-form-item label="产品名称" prop="productName">
            <el-input v-model="brandForm.productName" placeholder="请输入产品名称" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="Slogan" prop="slogan">
            <el-input v-model="brandForm.slogan" placeholder="请输入 Slogan" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="主色调" prop="primaryColor">
            <el-color-picker v-model="brandForm.primaryColor" show-alpha />
            <span style="margin-left: 10px; color: #909399;">{{ brandForm.primaryColor || '#409EFF' }}</span>
          </el-form-item>
          <el-form-item label="登录页背景图" prop="loginBg">
            <image-upload v-model="brandForm.loginBg" :limit="1" />
            <div class="form-tip">建议尺寸 1920 x 1080 像素，不上传则使用默认背景</div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitBrand" v-hasPermi="['batch:config:edit']">保存品牌配置</el-button>
            <el-button @click="resetBrand">重置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 版本管理 -->
      <el-tab-pane label="版本管理" name="version">
        <el-form :model="versionQuery" ref="versionQueryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="版本号" prop="versionNo">
            <el-input v-model="versionQuery.versionNo" placeholder="请输入版本号" clearable @keyup.enter.native="getVersionList" />
          </el-form-item>
          <el-form-item label="平台" prop="platform">
            <el-select v-model="versionQuery.platform" placeholder="请选择平台" clearable>
              <el-option v-for="dict in platformOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="versionQuery.status" placeholder="请选择状态" clearable>
              <el-option v-for="dict in statusOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="getVersionList">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetVersionQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAddVersion" v-hasPermi="['batch:config:add']">新增版本</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getVersionList"></right-toolbar>
        </el-row>

        <el-table v-loading="versionLoading" :data="versionList">
          <el-table-column label="版本号" align="center" prop="versionNo" />
          <el-table-column label="平台" align="center" prop="platform">
            <template slot-scope="scope">
              <span>{{ scope.row.platform === 1 ? 'Android' : 'iOS' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="更新类型" align="center" prop="updateType">
            <template slot-scope="scope">
              <span>{{ scope.row.updateType === 1 ? '强制' : (scope.row.updateType === 2 ? '提示' : '静默') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="下载链接" align="center" prop="downloadUrl" :show-overflow-tooltip="true" />
          <el-table-column label="发布时间" align="center" prop="publishTime" width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.publishTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" prop="status">
            <template slot-scope="scope">
              <el-switch
                v-model="scope.row.status"
                :active-value="0"
                :inactive-value="1"
                active-text="启用"
                inactive-text="禁用"
                @change="handleVersionStatusChange(scope.row)"
                v-hasPermi="['batch:config:edit']"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdateVersion(scope.row)" v-hasPermi="['batch:config:edit']">修改</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDeleteVersion(scope.row)" v-hasPermi="['batch:config:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="versionTotal > 0"
          :total="versionTotal"
          :page.sync="versionQuery.pageNum"
          :limit.sync="versionQuery.pageSize"
          @pagination="getVersionList"
        />
      </el-tab-pane>

      <!-- 全局参数 -->
      <el-tab-pane label="全局参数" name="global">
        <el-form ref="globalForm" :model="globalForm" :rules="globalRules" label-width="160px" style="max-width: 700px;">
          <h4 class="form-section">AI 云创配置</h4>
          <el-form-item label="单次选择视频上限" prop="maxVideos">
            <el-input-number v-model="globalForm.maxVideos" :disabled="true" :min="1" :max="50" :step="1" step-strictly />
            <span style="margin-left: 10px; color: #909399;">个（固定 10，不可修改）</span>
          </el-form-item>
          <el-form-item label="切片时长区间" prop="sliceMin">
            <el-input-number v-model="globalForm.sliceMin" :disabled="true" :min="0.1" :max="10" :step="0.1" :precision="1" />
            <span style="margin: 0 10px;">~</span>
            <el-input-number v-model="globalForm.sliceMax" :disabled="true" :min="0.1" :max="60" :step="0.1" :precision="1" />
            <span style="margin-left: 10px; color: #909399;">秒（固定 0.5~10s，不可修改）</span>
          </el-form-item>
          <el-form-item label="切片时长步长" prop="sliceStep">
            <el-input-number v-model="globalForm.sliceStep" :disabled="true" :min="0.1" :max="1" :step="0.1" :precision="1" />
            <span style="margin-left: 10px; color: #909399;">秒（固定 0.1s，不可修改）</span>
          </el-form-item>

          <h4 class="form-section">提示文案配置</h4>
          <el-form-item label="算力不足提示文案" prop="emptyTip">
            <el-input v-model="globalForm.emptyTip" type="textarea" :rows="2" placeholder="请输入算力不足提示文案" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="链接解析失败提示" prop="parseFailTip">
            <el-input v-model="globalForm.parseFailTip" type="textarea" :rows="2" placeholder="请输入链接解析失败提示文案" maxlength="200" show-word-limit />
          </el-form-item>

          <h4 class="form-section">其他配置</h4>
          <el-form-item label="空状态占位图" prop="emptyPlaceholder">
            <image-upload v-model="globalForm.emptyPlaceholder" :limit="1" />
            <div class="form-tip">建议尺寸 300 x 300 像素，用于 APP 各页面空状态展示</div>
          </el-form-item>
          <el-form-item label="客服服务时段" prop="customerServiceHours">
            <el-input v-model="globalForm.customerServiceHours" placeholder="如 09:00-18:00" maxlength="50" show-word-limit />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitGlobal" v-hasPermi="['batch:config:edit']">保存全局参数</el-button>
            <el-button @click="resetGlobal">重置</el-button>
            <el-button type="warning" plain @click="handleInitGlobal" v-hasPermi="['batch:config:add']">恢复默认值</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增/编辑版本弹窗 -->
    <el-dialog :title="versionTitle" :visible.sync="versionOpen" width="600px" append-to-body>
      <el-form ref="versionForm" :model="versionForm" :rules="versionRules" label-width="100px">
        <el-form-item label="版本号" prop="versionNo">
          <el-input v-model="versionForm.versionNo" placeholder="请输入版本号，如 1.2.0" maxlength="50" />
        </el-form-item>
        <el-form-item label="平台" prop="platform">
          <el-radio-group v-model="versionForm.platform">
            <el-radio :label="1">Android</el-radio>
            <el-radio :label="2">iOS</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="更新类型" prop="updateType">
          <el-radio-group v-model="versionForm.updateType">
            <el-radio :label="1">强制更新</el-radio>
            <el-radio :label="2">提示更新</el-radio>
            <el-radio :label="3">静默更新</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="下载链接" prop="downloadUrl">
          <el-input v-model="versionForm.downloadUrl" placeholder="请输入下载链接" maxlength="500" />
        </el-form-item>
        <el-form-item label="发布时间" prop="publishTime">
          <el-date-picker v-model="versionForm.publishTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="选择发布时间" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="更新内容" prop="updateContent">
          <el-input v-model="versionForm.updateContent" type="textarea" :rows="4" placeholder="请输入更新内容" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="versionForm.status">
            <el-radio :label="0">启用</el-radio>
            <el-radio :label="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitVersionForm">确 定</el-button>
        <el-button @click="cancelVersion">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getBrandConfig,
  saveBrandConfig,
  getGlobalConfig,
  saveGlobalConfig,
  initGlobalConfig,
  listVersion,
  getVersion,
  addVersion,
  updateVersion,
  delVersion,
  changeVersionStatus
} from "@/api/batch/config"

export default {
  name: "BatchConfig",
  data() {
    return {
      activeTab: "brand",
      loading: false,
      showSearch: true,
      // 品牌配置
      brandForm: {
        appLogo: "",
        adminLogo: "",
        productName: "",
        slogan: "",
        primaryColor: "#409EFF",
        loginBg: ""
      },
      brandRules: {
        productName: [{ required: true, message: "产品名称不能为空", trigger: "blur" }]
      },
      // 全局参数
      globalForm: {
        maxVideos: 10,
        sliceMin: 0.5,
        sliceMax: 10,
        sliceStep: 0.1,
        emptyTip: "当前算力已耗尽，请联系管理员增加算力额度",
        parseFailTip: "链接解析失败，请检查链接是否有效",
        emptyPlaceholder: "",
        customerServiceHours: ""
      },
      globalRules: {
        maxVideos: [{ required: true, message: "视频上限不能为空", trigger: "blur" }],
        emptyTip: [{ required: true, message: "算力不足提示文案不能为空", trigger: "blur" }],
        parseFailTip: [{ required: true, message: "链接解析失败提示不能为空", trigger: "blur" }]
      },
      // 版本管理
      platformOptions: [
        { value: 1, label: "Android" },
        { value: 2, label: "iOS" }
      ],
      statusOptions: [
        { value: 0, label: "启用" },
        { value: 1, label: "禁用" }
      ],
      versionLoading: false,
      versionList: [],
      versionTotal: 0,
      versionQuery: {
        pageNum: 1,
        pageSize: 10,
        versionNo: undefined,
        platform: undefined,
        status: undefined
      },
      versionOpen: false,
      versionTitle: "",
      versionForm: {
        versionId: undefined,
        versionNo: "",
        platform: 1,
        updateType: 2,
        updateContent: "",
        downloadUrl: "",
        publishTime: undefined,
        status: 0
      },
      versionRules: {
        versionNo: [{ required: true, message: "版本号不能为空", trigger: "blur" }],
        platform: [{ required: true, message: "平台不能为空", trigger: "change" }],
        updateType: [{ required: true, message: "更新类型不能为空", trigger: "change" }],
        downloadUrl: [{ required: true, message: "下载链接不能为空", trigger: "blur" }],
        publishTime: [{ required: true, message: "发布时间不能为空", trigger: "change" }],
        status: [{ required: true, message: "状态不能为空", trigger: "change" }]
      }
    }
  },
  created() {
    this.getBrandConfig()
    this.getGlobalConfig()
  },
  methods: {
    /** Tab 切换 */
    handleTabClick(tab) {
      if (tab.name === "version") {
        this.getVersionList()
      }
    },
    /** 获取品牌配置 */
    getBrandConfig() {
      getBrandConfig().then(response => {
        const data = response.data || {}
        this.brandForm = {
          appLogo: data.appLogo || "",
          adminLogo: data.adminLogo || "",
          productName: data.productName || "",
          slogan: data.slogan || "",
          primaryColor: data.primaryColor || "#409EFF",
          loginBg: data.loginBg || ""
        }
      })
    },
    /** 提交品牌配置 */
    submitBrand() {
      this.$refs["brandForm"].validate(valid => {
        if (valid) {
          saveBrandConfig(this.brandForm).then(() => {
            this.$modal.msgSuccess("保存成功")
          })
        }
      })
    },
    /** 重置品牌配置 */
    resetBrand() {
      this.getBrandConfig()
    },
    /** 获取全局参数 */
    getGlobalConfig() {
      getGlobalConfig().then(response => {
        const data = response.data || {}
        this.globalForm = {
          maxVideos: data.maxVideos !== undefined ? data.maxVideos : 10,
          sliceMin: data.sliceMin !== undefined ? data.sliceMin : 0.5,
          sliceMax: data.sliceMax !== undefined ? data.sliceMax : 10,
          sliceStep: data.sliceStep !== undefined ? data.sliceStep : 0.1,
          emptyTip: data.emptyTip || "当前算力已耗尽，请联系管理员增加算力额度",
          parseFailTip: data.parseFailTip || "链接解析失败，请检查链接是否有效",
          emptyPlaceholder: data.emptyPlaceholder || "",
          customerServiceHours: data.customerServiceHours || ""
        }
      })
    },
    /** 提交全局参数 */
    submitGlobal() {
      this.$refs["globalForm"].validate(valid => {
        if (valid) {
          if (this.globalForm.sliceMin >= this.globalForm.sliceMax) {
            this.$modal.msgError("切片时长最小值必须小于最大值")
            return
          }
          saveGlobalConfig(this.globalForm).then(() => {
            this.$modal.msgSuccess("保存成功")
          })
        }
      })
    },
    /** 重置全局参数 */
    resetGlobal() {
      this.getGlobalConfig()
    },
    /** 恢复全局参数默认值 */
    handleInitGlobal() {
      this.$modal.confirm('是否确认恢复全局参数为默认值？').then(() => {
        initGlobalConfig().then(() => {
          this.getGlobalConfig()
          this.$modal.msgSuccess("恢复成功")
        })
      }).catch(() => {})
    },
    /** 查询版本列表 */
    getVersionList() {
      this.versionLoading = true
      listVersion(this.versionQuery).then(response => {
        this.versionList = response.rows
        this.versionTotal = response.total
        this.versionLoading = false
      }).catch(() => {
        this.versionLoading = false
      })
    },
    /** 重置版本查询 */
    resetVersionQuery() {
      this.versionQuery = {
        pageNum: 1,
        pageSize: 10,
        versionNo: undefined,
        platform: undefined,
        status: undefined
      }
      this.getVersionList()
    },
    /** 新增版本 */
    handleAddVersion() {
      this.resetVersionForm()
      this.versionOpen = true
      this.versionTitle = "新增版本"
    },
    /** 修改版本 */
    handleUpdateVersion(row) {
      this.resetVersionForm()
      getVersion(row.versionId).then(response => {
        this.versionForm = response.data
        this.versionOpen = true
        this.versionTitle = "修改版本"
      })
    },
    /** 删除版本 */
    handleDeleteVersion(row) {
      this.$modal.confirm('是否确认删除版本号为"' + row.versionNo + '"的数据项？').then(() => {
        return delVersion(row.versionId)
      }).then(() => {
        this.getVersionList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 版本状态切换 */
    handleVersionStatusChange(row) {
      const text = row.status === 0 ? "启用" : "禁用"
      this.$modal.confirm('确认"' + text + '""' + row.versionNo + '"版本吗？').then(() => {
        return changeVersionStatus(row.versionId, row.status)
      }).then(() => {
        this.$modal.msgSuccess(text + "成功")
      }).catch(() => {
        row.status = row.status === 0 ? 1 : 0
      })
    },
    /** 提交版本表单 */
    submitVersionForm() {
      this.$refs["versionForm"].validate(valid => {
        if (valid) {
          const api = this.versionForm.versionId ? updateVersion : addVersion
          api(this.versionForm).then(() => {
            this.$modal.msgSuccess(this.versionForm.versionId ? "修改成功" : "新增成功")
            this.versionOpen = false
            this.getVersionList()
          })
        }
      })
    },
    /** 取消版本弹窗 */
    cancelVersion() {
      this.versionOpen = false
      this.resetVersionForm()
    },
    /** 重置版本表单 */
    resetVersionForm() {
      this.versionForm = {
        versionId: undefined,
        versionNo: "",
        platform: 1,
        updateType: 2,
        updateContent: "",
        downloadUrl: "",
        publishTime: undefined,
        status: 0
      }
      this.resetForm("versionForm")
    }
  }
}
</script>

<style scoped>
.form-section {
  margin: 20px 0 15px 0;
  padding-left: 10px;
  border-left: 4px solid #409EFF;
  font-size: 16px;
  color: #303133;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 5px;
}
</style>

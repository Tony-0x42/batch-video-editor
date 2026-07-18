<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 220px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="账号名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入账号名称" clearable style="width: 220px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="联系人" prop="contactName">
        <el-input v-model="queryParams.contactName" placeholder="请输入联系人" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="账号类型" prop="customerType">
        <el-select v-model="queryParams.customerType" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="dict in customerTypeOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属分公司" prop="branchPhone" v-hasPermi="['batch:customer:add']">
        <el-input v-model="queryParams.branchPhone" placeholder="分公司手机号" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="启用" value="0" />
          <el-option label="禁用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['batch:customer:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['batch:customer:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="handleImport" v-hasPermi="['batch:customer:add']">导入</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="customerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="账号ID" align="center" key="customerId" prop="customerId" v-if="columns.customerId.visible" width="80" />
      <el-table-column label="账号类型" align="center" key="customerType" prop="customerType" v-if="columns.customerType.visible" width="100">
        <template slot-scope="scope">
          <span>{{ formatType(scope.row.customerType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="账号名称" align="center" key="customerName" prop="customerName" v-if="columns.customerName.visible" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <a class="link-type" style="cursor:pointer" @click="handleView(scope.row)">{{ scope.row.customerName }}</a>
        </template>
      </el-table-column>
      <el-table-column label="联系人" align="center" key="contactName" prop="contactName" v-if="columns.contactName.visible" />
      <el-table-column label="手机号" align="center" key="phone" prop="phone" v-if="columns.phone.visible" width="120" />
      <el-table-column label="上级手机号" align="center" key="parentPhone" prop="parentPhone" v-if="columns.parentPhone.visible" width="120" />
      <el-table-column label="所属分公司" align="center" key="branchPhone" prop="branchPhone" v-if="columns.branchPhone.visible" width="120" />
      <el-table-column label="算力总配额" align="center" key="computingPowerTotal" prop="computingPowerTotal" v-if="columns.computingPowerTotal.visible" width="110" />
      <el-table-column label="VIP有效期" align="center" key="vipExpireDate" prop="vipExpireDate" v-if="columns.vipExpireDate.visible" width="110" />
      <el-table-column label="下级数量" align="center" key="subordinateCount" prop="subordinateCount" v-if="columns.subordinateCount.visible" width="90" />
      <el-table-column label="状态" align="center" key="status" v-if="columns.status.visible" width="90">
        <template slot-scope="scope">
          <el-switch v-model="scope.row.status" :active-value="0" :inactive-value="1" @change="handleStatusChange(scope.row)" :disabled="!checkPermi(['batch:customer:edit'])"></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" v-if="columns.createTime.visible" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['batch:customer:query']">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['batch:customer:edit']">编辑</el-button>
          <el-button size="mini" type="text" icon="el-icon-picture-outline" @click="handleQrCode(scope.row)" v-hasPermi="['batch:customer:resetQr']">二维码</el-button>
          <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)" v-hasPermi="['batch:customer:upgrade', 'batch:customer:migrate', 'batch:customer:edit', 'batch:customer:remove']">
            <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="handleResetPassword" icon="el-icon-key" v-hasPermi="['batch:customer:edit']">重置密码</el-dropdown-item>
              <el-dropdown-item command="handleUpgrade" icon="el-icon-top" v-hasPermi="['batch:customer:upgrade']" v-if="scope.row.customerType !== 1">升级</el-dropdown-item>
              <el-dropdown-item command="handleMigrate" icon="el-icon-rank" v-hasPermi="['batch:customer:migrate']" v-if="scope.row.customerType !== 1">迁移</el-dropdown-item>
              <el-dropdown-item command="handleDelete" icon="el-icon-delete" v-hasPermi="['batch:customer:remove']">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 升级弹窗 -->
    <customer-upgrade-dialog ref="upgradeDialogRef" @success="getList" />
    <!-- 迁移弹窗 -->
    <customer-migrate-dialog ref="migrateDialogRef" @success="getList" />

    <!-- 重置密码弹窗 -->
    <el-dialog title="重置密码" :visible.sync="resetPwdOpen" width="450px" append-to-body :close-on-click-modal="false">
      <el-form ref="resetPwdForm" :model="resetPwdForm" :rules="resetPwdRules" label-width="90px">
        <el-form-item label="客户">
          <span>{{ resetPwdForm.customerName }}（{{ resetPwdForm.phone }}）</span>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetPwdForm.password" type="password" show-password placeholder="请输入新密码（6-20位）" maxlength="20" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" maxlength="20" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="resetPwdLoading" @click="submitResetPassword">确 定</el-button>
        <el-button @click="resetPwdOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 导入弹窗 -->
    <el-dialog title="导入客户" :visible.sync="importOpen" width="450px" append-to-body :close-on-click-modal="false">
      <el-upload ref="uploadRef" drag action="" :auto-upload="false" :limit="1" accept=".xlsx, .xls" :on-change="handleFileChange" :on-remove="handleFileRemove">
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip" slot="tip">
          <span>仅允许导入 xls、xlsx 格式文件。</span>
          <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline" @click.prevent="handleDownloadTemplate">下载模板</el-link>
        </div>
      </el-upload>
      <div v-if="importResultVisible" style="margin-top: 15px; padding: 10px; background: #f5f7fa; border-radius: 4px;">
        <div>成功：<span style="color: #67c23a; font-weight: bold;">{{ importResult.successCount }}</span> 条</div>
        <div>失败：<span style="color: #f56c6c; font-weight: bold;">{{ importResult.failCount }}</span> 条</div>
        <el-link v-if="importResult.failCount > 0" type="primary" :underline="false" style="font-size: 12px" @click="handleViewFailList">查看失败条目</el-link>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="importLoading" @click="submitImport">确 定</el-button>
        <el-button @click="importOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 失败列表弹窗 -->
    <el-dialog title="导入失败明细" :visible.sync="failListOpen" width="700px" append-to-body :close-on-click-modal="false">
      <el-table :data="importResult.failList" max-height="400">
        <el-table-column label="行号" align="center" prop="rowNum" width="80" />
        <el-table-column label="账号名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
        <el-table-column label="手机号" align="center" prop="phone" width="120" />
        <el-table-column label="失败原因" align="center" prop="reason" :show-overflow-tooltip="true" />
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="failListOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCustomer, delCustomer, changeCustomerStatus, exportCustomer, importData, resetCustomerPassword } from "@/api/batch/customer"
import CustomerUpgradeDialog from "./upgrade"
import CustomerMigrateDialog from "./migrate"
import { checkPermi } from "@/utils/permission"

export default {
  name: "BatchCustomer",
  components: { CustomerUpgradeDialog, CustomerMigrateDialog },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      customerList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        phone: undefined,
        customerName: undefined,
        contactName: undefined,
        customerType: undefined,
        branchPhone: undefined,
        status: undefined
      },
      customerTypeOptions: [
        { value: 1, label: "分公司" },
        { value: 2, label: "服务商" },
        { value: 3, label: "个人" }
      ],
      columns: {
        customerId: { label: '账号ID', visible: true },
        customerType: { label: '账号类型', visible: true },
        customerName: { label: '账号名称', visible: true },
        contactName: { label: '联系人', visible: true },
        phone: { label: '手机号', visible: true },
        parentPhone: { label: '上级手机号', visible: true },
        branchPhone: { label: '所属分公司', visible: true },
        computingPowerTotal: { label: '算力总配额', visible: true },
        vipExpireDate: { label: 'VIP有效期', visible: true },
        subordinateCount: { label: '下级数量', visible: true },
        status: { label: '状态', visible: true },
        createTime: { label: '创建时间', visible: true }
      },
      // 导入相关
      importOpen: false,
      importLoading: false,
      importResultVisible: false,
      importResult: {
        successCount: 0,
        failCount: 0,
        failList: []
      },
      importFile: null,
      failListOpen: false,
      // 重置密码相关
      resetPwdOpen: false,
      resetPwdLoading: false,
      resetPwdForm: {
        customerId: undefined,
        customerName: "",
        phone: "",
        password: "",
        confirmPassword: ""
      },
      resetPwdRules: {
        password: [
          { required: true, message: "新密码不能为空", trigger: "blur" },
          { min: 6, max: 20, message: "密码长度必须在 6-20 位之间", trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, message: "请再次输入新密码", trigger: "blur" },
          {
            validator: (rule, value, callback) => {
              if (value !== this.resetPwdForm.password) {
                callback(new Error("两次输入的密码不一致"))
              } else {
                callback()
              }
            },
            trigger: "blur"
          }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    checkPermi,
    /** 查询客户列表 */
    getList() {
      this.loading = true
      listCustomer(this.queryParams).then(response => {
        this.customerList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.customerId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    // 更多操作触发
    handleCommand(command, row) {
      switch (command) {
        case "handleResetPassword":
          this.handleResetPassword(row)
          break
        case "handleUpgrade":
          this.handleUpgrade(row)
          break
        case "handleMigrate":
          this.handleMigrate(row)
          break
        case "handleDelete":
          this.handleDelete(row)
          break
        default:
          break
      }
    },
    /** 状态修改 */
    handleStatusChange(row) {
      let text = row.status === 0 ? "启用" : "禁用"
      this.$modal.confirm('确认要"' + text + '""' + row.customerName + '"账号吗？').then(function() {
        return changeCustomerStatus(row.customerId, row.status)
      }).then(() => {
        this.$modal.msgSuccess(text + "成功")
      }).catch(function() {
        row.status = row.status === 0 ? 1 : 0
      })
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.$tab.openPage('新增账号', '/batch/customer/add')
    },
    /** 编辑按钮操作 */
    handleUpdate(row) {
      this.$tab.openPage('编辑账号', '/batch/customer/edit/' + row.customerId)
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.$router.push("/batch/customer/detail/" + row.customerId)
    },
    /** 二维码按钮操作 */
    handleQrCode(row) {
      this.$router.push("/batch/customer/qrcode/" + row.customerId)
    },
    /** 升级按钮操作 */
    handleUpgrade(row) {
      this.$refs.upgradeDialogRef.open(row)
    },
    /** 迁移按钮操作 */
    handleMigrate(row) {
      this.$refs.migrateDialogRef.open(row)
    },
    /** 重置密码按钮操作 */
    handleResetPassword(row) {
      this.resetPwdForm = {
        customerId: row.customerId,
        customerName: row.customerName,
        phone: row.phone,
        password: "",
        confirmPassword: ""
      }
      this.resetPwdOpen = true
      this.$nextTick(() => {
        this.$refs.resetPwdForm && this.$refs.resetPwdForm.clearValidate()
      })
    },
    /** 提交重置密码 */
    submitResetPassword() {
      this.$refs.resetPwdForm.validate(valid => {
        if (!valid) return
        this.resetPwdLoading = true
        resetCustomerPassword(this.resetPwdForm.customerId, this.resetPwdForm.password).then(() => {
          this.$modal.msgSuccess("重置密码成功")
          this.resetPwdOpen = false
        }).finally(() => {
          this.resetPwdLoading = false
        })
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const customerIds = row.customerId || this.ids
      this.$modal.confirm('是否确认删除账号编号为"' + customerIds + '"的数据项？').then(function() {
        return delCustomer(customerIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('/batch/customer/export', {
        ...this.queryParams
      }, `customer_${new Date().getTime()}.xlsx`)
    },
    /** 导入按钮操作 */
    handleImport() {
      this.importOpen = true
      this.importResultVisible = false
      this.importResult = { successCount: 0, failCount: 0, failList: [] }
      this.importFile = null
      this.$nextTick(() => {
        this.$refs.uploadRef && this.$refs.uploadRef.clearFiles()
      })
    },
    /** 下载导入模板 */
    handleDownloadTemplate() {
      this.download('/batch/customer/importTemplate', {}, `customer_import_template_${new Date().getTime()}.xlsx`)
    },
    /** 文件选择变化 */
    handleFileChange(file, fileList) {
      this.importFile = file.raw
      this.importResultVisible = false
    },
    /** 文件移除 */
    handleFileRemove() {
      this.importFile = null
      this.importResultVisible = false
    },
    /** 提交导入 */
    submitImport() {
      if (!this.importFile) {
        this.$modal.msgError("请选择要上传的文件")
        return
      }
      const name = this.importFile.name.toLowerCase()
      if (!name.endsWith('.xls') && !name.endsWith('.xlsx')) {
        this.$modal.msgError("请选择后缀为 xls 或 xlsx 的文件")
        return
      }
      this.importLoading = true
      const formData = new FormData()
      formData.append('file', this.importFile)
      importData(formData).then(response => {
        this.importResult.successCount = response.data.successCount || 0
        this.importResult.failCount = response.data.failCount || 0
        this.importResult.failList = response.data.failList || []
        this.importResultVisible = true
        this.$modal.msgSuccess(`导入完成：成功 ${this.importResult.successCount} 条，失败 ${this.importResult.failCount} 条`)
        this.getList()
      }).finally(() => {
        this.importLoading = false
      })
    },
    /** 查看失败列表 */
    handleViewFailList() {
      this.failListOpen = true
    }
  }
}
</script>

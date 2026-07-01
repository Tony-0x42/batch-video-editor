<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入手机号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="账号名称" prop="customerName">
        <el-input
          v-model="queryParams.customerName"
          placeholder="请输入账号名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="账号类型" prop="customerType">
        <el-select v-model="queryParams.customerType" placeholder="全部" clearable>
          <el-option
            v-for="item in customerTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="VIP状态" prop="vipStatus">
        <el-select v-model="queryParams.vipStatus" placeholder="全部" clearable>
          <el-option
            v-for="item in vipStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="所属分公司" prop="branchPhone">
        <el-input
          v-model="queryParams.branchPhone"
          placeholder="请输入分公司手机号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-date"
          size="mini"
          :disabled="multiple"
          @click="handleBatchEdit"
          v-hasPermi="['batch:vip:edit']"
        >批量调整VIP</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="vipList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="账号类型" align="center" prop="customerType" width="100">
        <template slot-scope="scope">
          <el-tag :type="customerTypeTag(scope.row.customerType)">{{ customerTypeText(scope.row.customerType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="账号名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
      <el-table-column label="联系人" align="center" prop="contactName" :show-overflow-tooltip="true" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="VIP标识" align="center" prop="vipFlag" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.vipFlag ? 'success' : 'danger'">{{ scope.row.vipFlag ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="VIP有效期" align="center" prop="vipExpireDate" width="120">
        <template slot-scope="scope">
          <span :class="{ 'text-danger': !scope.row.vipFlag }">{{ parseDate(scope.row.vipExpireDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="剩余天数" align="center" prop="remainDays" width="100">
        <template slot-scope="scope">
          <span :class="{ 'text-danger': scope.row.remainDays < 0, 'text-warning': scope.row.remainDays >= 0 && scope.row.remainDays <= 7 }">
            {{ formatRemainDays(scope.row.remainDays) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">{{ scope.row.status === 0 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleEdit(scope.row)"
            v-hasPermi="['batch:vip:edit']"
          >编辑</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
            v-hasPermi="['batch:vip:query']"
          >查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 单个编辑VIP弹窗 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="账号名称">
          <el-input v-model="form.customerName" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" disabled />
        </el-form-item>
        <el-form-item label="VIP有效期" prop="vipExpireDate">
          <el-date-picker
            v-model="form.vipExpireDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择VIP有效期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">保 存</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 批量编辑VIP弹窗 -->
    <el-dialog title="批量调整VIP有效期" :visible.sync="batchOpen" width="500px" append-to-body>
      <el-form ref="batchForm" :model="batchForm" :rules="batchRules" label-width="120px">
        <el-form-item label="已选中账号数">
          <el-input v-model="selectedCount" disabled />
        </el-form-item>
        <el-form-item label="VIP有效期" prop="vipExpireDate">
          <el-date-picker
            v-model="batchForm.vipExpireDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择VIP有效期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitBatchForm">保 存</el-button>
        <el-button @click="cancelBatch">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listVip, updateVip, batchUpdateVip } from "@/api/batch/vip"

export default {
  name: "Vip",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      vipList: [],
      title: "",
      open: false,
      batchOpen: false,
      customerTypeOptions: [
        { value: 1, label: "分公司" },
        { value: 2, label: "服务商" },
        { value: 3, label: "个人" }
      ],
      vipStatusOptions: [
        { value: 0, label: "有效" },
        { value: 1, label: "已过期" }
      ],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        phone: undefined,
        customerName: undefined,
        customerType: undefined,
        vipStatus: undefined,
        branchPhone: undefined
      },
      form: {},
      rules: {
        vipExpireDate: [
          { required: true, message: "请选择VIP有效期", trigger: "change" }
        ]
      },
      batchForm: {},
      batchRules: {
        vipExpireDate: [
          { required: true, message: "请选择VIP有效期", trigger: "change" }
        ]
      }
    }
  },
  computed: {
    selectedCount() {
      return this.ids ? this.ids.length : 0
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询VIP列表 */
    getList() {
      this.loading = true
      listVip(this.queryParams).then(response => {
        this.vipList = (response.rows || []).map(item => this.enrichVipInfo(item))
        this.total = response.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /** 补充VIP标识与剩余天数 */
    enrichVipInfo(item) {
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      let vipFlag = false
      let remainDays = null
      if (item.vipExpireDate) {
        const expire = new Date(item.vipExpireDate)
        expire.setHours(0, 0, 0, 0)
        const diff = expire.getTime() - today.getTime()
        remainDays = Math.floor(diff / (1000 * 60 * 60 * 24))
        vipFlag = remainDays >= 0
      }
      return { ...item, vipFlag, remainDays }
    },
    customerTypeText(type) {
      const map = { 1: "分公司", 2: "服务商", 3: "个人" }
      return map[type] || "未知"
    },
    customerTypeTag(type) {
      const map = { 1: "primary", 2: "success", 3: "info" }
      return map[type] || ""
    },
    parseDate(date) {
      if (!date) {
        return "-"
      }
      return this.parseTime(date, '{y}-{m}-{d}')
    },
    formatRemainDays(days) {
      if (days === null || days === undefined) {
        return "-"
      }
      return days >= 0 ? days + " 天" : "已过期 " + Math.abs(days) + " 天"
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.queryParams.customerType = undefined
      this.queryParams.vipStatus = undefined
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.customerId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 单个编辑按钮 */
    handleEdit(row) {
      this.reset()
      this.form = {
        customerId: row.customerId,
        customerName: row.customerName,
        phone: row.phone,
        vipExpireDate: row.vipExpireDate
      }
      this.open = true
      this.title = "编辑VIP有效期"
    },
    /** 批量编辑按钮 */
    handleBatchEdit() {
      this.batchForm = { vipExpireDate: undefined }
      this.batchOpen = true
    },
    /** 查看详情 */
    handleDetail(row) {
      this.$router.push("/batch/customer/detail/" + row.customerId)
    },
    /** 提交单个编辑 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          updateVip(this.form.customerId, this.form.vipExpireDate).then(() => {
            this.$modal.msgSuccess("修改成功")
            this.open = false
            this.getList()
          })
        }
      })
    },
    /** 提交批量编辑 */
    submitBatchForm() {
      this.$refs["batchForm"].validate(valid => {
        if (valid) {
          batchUpdateVip(this.ids, this.batchForm.vipExpireDate).then(() => {
            this.$modal.msgSuccess("批量修改成功")
            this.batchOpen = false
            this.getList()
          })
        }
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    cancelBatch() {
      this.batchOpen = false
      this.resetBatch()
    },
    // 表单重置
    reset() {
      this.form = {
        customerId: undefined,
        customerName: undefined,
        phone: undefined,
        vipExpireDate: undefined
      }
      this.resetForm("form")
    },
    resetBatch() {
      this.batchForm = {
        vipExpireDate: undefined
      }
      this.resetForm("batchForm")
    }
  }
}
</script>

<style scoped>
.text-danger {
  color: #F56C6C;
}
.text-warning {
  color: #E6A23C;
}
</style>

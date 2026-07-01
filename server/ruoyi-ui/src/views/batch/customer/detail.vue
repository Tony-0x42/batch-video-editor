<template>
  <div class="app-container">
    <el-page-header @back="goBack" content="账号详情" />

    <el-card class="box-card" style="margin-top: 20px">
      <div slot="header" class="clearfix">
        <span>基础信息</span>
        <el-button style="float: right; padding: 3px 0" type="text" icon="el-icon-edit" @click="handleEdit" v-hasPermi="['batch:customer:edit']">编辑</el-button>
      </div>
      <el-row :gutter="20">
        <el-col :span="8"><span class="detail-label">账号类型：</span>{{ formatType(customer.customerType) }}</el-col>
        <el-col :span="8"><span class="detail-label">账号名称：</span>{{ customer.customerName }}</el-col>
        <el-col :span="8"><span class="detail-label">联系人：</span>{{ customer.contactName }}</el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 15px">
        <el-col :span="8"><span class="detail-label">手机号：</span>{{ customer.phone }}</el-col>
        <el-col :span="8"><span class="detail-label">上级手机号：</span>{{ customer.parentPhone || '无' }}</el-col>
        <el-col :span="8"><span class="detail-label">所属分公司：</span>{{ customer.branchPhone || '无' }}</el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 15px">
        <el-col :span="8"><span class="detail-label">状态：</span>{{ customer.status === 0 ? '启用' : '禁用' }}</el-col>
        <el-col :span="8"><span class="detail-label">创建时间：</span>{{ parseTime(customer.createTime) }}</el-col>
        <el-col :span="8"><span class="detail-label">VIP有效期：</span>{{ customer.vipExpireDate }}</el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 15px">
        <el-col :span="8"><span class="detail-label">算力总配额：</span>{{ customer.computingPowerTotal }} GF</el-col>
        <el-col :span="8"><span class="detail-label">已消耗算力：</span>{{ customer.computingPowerUsed }} GF</el-col>
        <el-col :span="8"><span class="detail-label" style="color: #409EFF">剩余算力：</span>{{ customer.computingPowerRemain }} GF</el-col>
      </el-row>
    </el-card>

    <el-card class="box-card" style="margin-top: 20px">
      <div slot="header" class="clearfix">
        <span>配额信息</span>
      </div>
      <el-row :gutter="20" v-if="customer.customerType === 1">
        <el-col :span="8"><span class="detail-label">最大可创建服务商数量：</span>{{ customer.maxServiceProvider }}</el-col>
        <el-col :span="8"><span class="detail-label">已创建服务商数量：</span>{{ subordinateCount }}</el-col>
        <el-col :span="8"><span class="detail-label" style="color: #409EFF">剩余可创建名额：</span>{{ customer.maxServiceProvider - subordinateCount }}</el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 15px" v-if="customer.customerType === 1">
        <el-col :span="8"><span class="detail-label">个人账号总容量：</span>{{ customer.totalIndividualCapacity }}</el-col>
        <el-col :span="8"><span class="detail-label">已分配个人账号数量：</span>{{ individualCount }}</el-col>
        <el-col :span="8"><span class="detail-label" style="color: #409EFF">剩余可分配名额：</span>{{ customer.totalIndividualCapacity - individualCount }}</el-col>
      </el-row>
      <el-row :gutter="20" v-if="customer.customerType === 2">
        <el-col :span="8"><span class="detail-label">可拆分创建个人账号上限：</span>{{ customer.maxIndividual }}</el-col>
        <el-col :span="8"><span class="detail-label">已创建个人账号数量：</span>{{ subordinateCount }}</el-col>
        <el-col :span="8"><span class="detail-label" style="color: #409EFF">剩余可创建名额：</span>{{ customer.maxIndividual - subordinateCount }}</el-col>
      </el-row>
      <el-row :gutter="20" v-if="customer.customerType === 3">
        <el-col :span="24"><span class="detail-label">个人账号无下级配额配置</span></el-col>
      </el-row>
    </el-card>

    <el-card class="box-card" style="margin-top: 20px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="下级账号" name="subordinate">
          <el-table v-loading="loading" :data="subordinateList">
            <el-table-column label="账号类型" align="center" prop="customerType" width="100">
              <template slot-scope="scope">{{ formatType(scope.row.customerType) }}</template>
            </el-table-column>
            <el-table-column label="账号名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
            <el-table-column label="联系人" align="center" prop="contactName" />
            <el-table-column label="手机号" align="center" prop="phone" width="120" />
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" align="center" prop="status" width="80">
              <template slot-scope="scope">{{ scope.row.status === 0 ? '启用' : '禁用' }}</template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="180">
              <template slot-scope="scope">
                <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)">查看</el-button>
                <el-button size="mini" type="text" icon="el-icon-rank" @click="handleMigrate(scope.row)" v-hasPermi="['batch:customer:migrate']">迁移</el-button>
                <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['batch:customer:remove']">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-show="subordinateTotal > 0" :total="subordinateTotal" :page.sync="subQuery.pageNum" :limit.sync="subQuery.pageSize" @pagination="getSubordinateList" />
        </el-tab-pane>

        <el-tab-pane label="算力消耗记录" name="computing">
          <el-table v-loading="loadingComputing" :data="computingList">
            <el-table-column label="操作类型" align="center" prop="operationType" width="100">
              <template slot-scope="scope">{{ formatOperationType(scope.row.operationType) }}</template>
            </el-table-column>
            <el-table-column label="消耗算力(GF)" align="center" prop="consumeValue" />
            <el-table-column label="剩余算力(GF)" align="center" prop="remainValue" />
            <el-table-column label="关联视频组" align="center" prop="videoGroupName" :show-overflow-tooltip="true" />
            <el-table-column label="操作时间" align="center" prop="createTime" width="160">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-show="computingTotal > 0" :total="computingTotal" :page.sync="computingQuery.pageNum" :limit.sync="computingQuery.pageSize" @pagination="getComputingList" />
        </el-tab-pane>

        <el-tab-pane label="视频生成记录" name="video">
          <el-table v-loading="loadingVideo" :data="videoList">
            <el-table-column label="视频组名称" align="center" prop="videoGroupName" :show-overflow-tooltip="true" />
            <el-table-column label="生成数量" align="center" prop="generateCount" width="100" />
            <el-table-column label="状态" align="center" prop="status" width="100">
              <template slot-scope="scope">{{ formatVideoStatus(scope.row.status) }}</template>
            </el-table-column>
            <el-table-column label="生成时间" align="center" prop="createTime" width="160">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-show="videoTotal > 0" :total="videoTotal" :page.sync="videoQuery.pageNum" :limit.sync="videoQuery.pageSize" @pagination="getVideoList" />
        </el-tab-pane>

        <el-tab-pane label="注册二维码" name="qrcode">
          <div style="text-align: center; padding: 20px">
            <el-image v-if="customer.qrCodeUrl" :src="customer.qrCodeUrl" style="width: 300px; height: 300px" fit="contain" />
            <el-empty v-else description="暂无二维码" />
            <div style="margin-top: 20px">
              <el-button type="primary" icon="el-icon-view" @click="handleViewQrCode">查看二维码详情</el-button>
              <el-button type="warning" icon="el-icon-refresh" @click="handleResetQrCode" v-hasPermi="['batch:customer:resetQr']">重置二维码</el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <customer-migrate-dialog ref="migrateDialogRef" @success="getSubordinateList" />
    <customer-form-dialog ref="formDialogRef" title="编辑账号" @success="getDetail" />
  </div>
</template>

<script>
import { getCustomer, delCustomer, resetCustomerQrCode } from "@/api/batch/customer"
import { listCustomer } from "@/api/batch/customer"
import { listStatisticsComputing, listStatisticsVideo } from "@/api/batch/statistics"
import CustomerMigrateDialog from "./migrate"
import CustomerFormDialog from "./form"

export default {
  name: "BatchCustomerDetail",
  components: { CustomerMigrateDialog, CustomerFormDialog },
  data() {
    return {
      loading: false,
      customerId: undefined,
      customer: {},
      activeTab: "subordinate",
      subordinateList: [],
      subordinateTotal: 0,
      subQuery: {
        pageNum: 1,
        pageSize: 10,
        parentPhone: undefined
      },
      individualCount: 0,
      loadingComputing: false,
      computingList: [],
      computingTotal: 0,
      computingQuery: {
        pageNum: 1,
        pageSize: 10,
        phone: undefined
      },
      loadingVideo: false,
      videoList: [],
      videoTotal: 0,
      videoQuery: {
        pageNum: 1,
        pageSize: 10,
        phone: undefined
      }
    }
  },
  created() {
    this.customerId = this.$route.params.customerId
    this.getDetail()
  },
  computed: {
    subordinateCount() {
      return this.subordinateTotal
    }
  },
  methods: {
    getDetail() {
      getCustomer(this.customerId).then(response => {
        this.customer = response.data || {}
        this.getSubordinateList()
        this.getIndividualCount()
        this.getComputingList()
        this.getVideoList()
      })
    },
    getSubordinateList() {
      if (!this.customer.phone) return
      this.loading = true
      this.subQuery.parentPhone = this.customer.phone
      listCustomer(this.subQuery).then(response => {
        this.subordinateList = response.rows || []
        this.subordinateTotal = response.total || 0
        this.loading = false
      })
    },
    getIndividualCount() {
      if (this.customer.customerType !== 1 || !this.customer.phone) {
        this.individualCount = 0
        return
      }
      const query = {
        branchPhone: this.customer.phone,
        customerType: 3,
        pageNum: 1,
        pageSize: 10000
      }
      listCustomer(query).then(response => {
        this.individualCount = (response.rows || []).length
      })
    },
    getComputingList() {
      if (!this.customer.phone) return
      this.loadingComputing = true
      this.computingQuery.phone = this.customer.phone
      listStatisticsComputing(this.computingQuery).then(response => {
        this.computingList = response.rows || []
        this.computingTotal = response.total || 0
        this.loadingComputing = false
      })
    },
    getVideoList() {
      if (!this.customer.phone) return
      this.loadingVideo = true
      this.videoQuery.phone = this.customer.phone
      listStatisticsVideo(this.videoQuery).then(response => {
        this.videoList = response.rows || []
        this.videoTotal = response.total || 0
        this.loadingVideo = false
      })
    },
    goBack() {
      this.$router.go(-1)
    },
    handleEdit() {
      this.$refs.formDialogRef.open(this.customerId)
    },
    handleView(row) {
      this.$router.push("/batch/customer/detail/" + row.customerId)
    },
    handleMigrate(row) {
      this.$refs.migrateDialogRef.open(row)
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除账号"' + row.customerName + '"？').then(function() {
        return delCustomer(row.customerId)
      }).then(() => {
        this.getSubordinateList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleViewQrCode() {
      this.$router.push("/batch/customer/qrcode/" + this.customerId)
    },
    handleResetQrCode() {
      this.$modal.confirm('重置二维码后旧二维码将立即失效，是否继续？').then(() => {
        return resetCustomerQrCode(this.customerId)
      }).then(response => {
        this.customer.qrCodeUrl = response.qrCodeUrl
        this.$modal.msgSuccess("重置成功")
      }).catch(() => {})
    },
    formatType(type) {
      const map = { 1: "分公司", 2: "服务商", 3: "个人" }
      return map[type] || type
    },
    formatOperationType(type) {
      const map = { 1: "生成", 2: "下载" }
      return map[type] || type
    },
    formatVideoStatus(status) {
      const map = { 0: "成功", 1: "失败" }
      return map[status] || status
    }
  }
}
</script>

<style scoped>
.detail-label {
  color: #606266;
  font-weight: 500;
}
</style>

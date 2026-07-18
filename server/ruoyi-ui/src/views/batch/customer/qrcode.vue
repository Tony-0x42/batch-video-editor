<template>
  <div class="app-container">
    <el-page-header @back="goBack" content="注册二维码" />

    <el-card class="box-card" style="margin-top: 20px; text-align: center; padding: 30px">
      <div v-if="customer.qrCodeUrl">
        <el-image :src="customer.qrCodeUrl" style="width: 300px; height: 300px" fit="contain" />
      </div>
      <el-empty v-else description="暂无二维码" />

      <div style="margin-top: 24px">
        <p><span class="detail-label">账号名称：</span>{{ customer.customerName }}</p>
        <p><span class="detail-label">账号类型：</span>{{ formatType(customer.customerType) }}</p>
        <p><span class="detail-label">上级手机号：</span>{{ customer.parentPhone || '无' }}</p>
        <p><span class="detail-label">创建时间：</span>{{ parseTime(customer.createTime) }}</p>
      </div>

      <div style="margin-top: 24px">
        <el-button type="primary" icon="el-icon-download" @click="handleDownload">下载二维码</el-button>
        <el-button type="success" icon="el-icon-link" @click="handleCopyLink">复制推广链接</el-button>
        <el-button type="warning" icon="el-icon-refresh" @click="handleReset" v-hasPermi="['batch:customer:resetQr']">重置二维码</el-button>
      </div>

      <el-divider />

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="8">
          <el-card shadow="hover">
            <div style="font-size: 24px; color: #409EFF; font-weight: bold">{{ stat.scanCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px">扫码次数</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div style="font-size: 24px; color: #409EFF; font-weight: bold">{{ stat.downloadCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px">APP 下载量</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div style="font-size: 24px; color: #409EFF; font-weight: bold">{{ stat.registerCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px">成功注册用户数</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { getCustomer, resetCustomerQrCode, getCustomerQrCodeStat, downloadCustomerQrCode } from "@/api/batch/customer"

export default {
  name: "BatchCustomerQrCode",
  data() {
    return {
      customerId: undefined,
      customer: {},
      stat: {
        scanCount: 0,
        downloadCount: 0,
        registerCount: 0
      }
    }
  },
  created() {
    this.customerId = this.$route.params.customerId
    this.getDetail()
  },
  methods: {
    getDetail() {
      getCustomer(this.customerId).then(response => {
        this.customer = response.data || {}
      })
      getCustomerQrCodeStat(this.customerId).then(response => {
        this.stat = response.data || { scanCount: 0, downloadCount: 0, registerCount: 0 }
      })
    },
    goBack() {
      this.$router.go(-1)
    },
    formatType(type) {
      const map = { 1: "分公司", 2: "服务商", 3: "个人" }
      return map[type] || type
    },
    handleDownload() {
      if (!this.customer.qrCodeUrl) {
        this.$modal.msgError("二维码不存在")
        return
      }
      downloadCustomerQrCode(this.customerId).then(res => {
        const blob = new Blob([res], { type: 'image/png' })
        const a = document.createElement('a')
        a.href = URL.createObjectURL(blob)
        a.download = 'qrcode_' + this.customer.phone + '.png'
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        URL.revokeObjectURL(a.href)
        this.stat.downloadCount = (this.stat.downloadCount || 0) + 1
        this.$modal.msgSuccess("已开始下载")
      }).catch(() => {})
    },
    handleCopyLink() {
      if (!this.customer.qrCodeUrl) {
        this.$modal.msgError("二维码不存在")
        return
      }
      // 推广链接取二维码内容，即 APP 下载页 + invitePhone
      const link = this.buildPromoteLink()
      if (navigator.clipboard) {
        navigator.clipboard.writeText(link).then(() => this.$modal.msgSuccess("复制成功"))
      } else {
        const ta = document.createElement('textarea')
        ta.value = link
        document.body.appendChild(ta)
        ta.select()
        document.execCommand('copy')
        document.body.removeChild(ta)
        this.$modal.msgSuccess("复制成功")
      }
    },
    buildPromoteLink() {
      // 与后端 QrCodeUtil 二维码内容保持一致：扫码统计接口，扫码后 302 跳 APP 下载页
      // 注意：生产环境 VUE_APP_BASE_API 需与后端 batch.app.server-url 指向同一服务地址
      return process.env.VUE_APP_BASE_API + '/batch/qrcode/scan?phone=' + this.customer.phone
    },
    handleReset() {
      this.$modal.confirm('重置二维码后旧二维码将立即失效，是否继续？').then(() => {
        return resetCustomerQrCode(this.customerId)
      }).then(response => {
        this.customer.qrCodeUrl = response.qrCodeUrl
        this.$modal.msgSuccess("重置成功")
      }).catch(() => {})
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

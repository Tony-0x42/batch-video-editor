<template>
  <div class="app-container">
    <!-- 顶部筛选区 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          align="right"
          unlink-panels
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          :picker-options="pickerOptions"
          @change="handleDateChange"
        />
      </el-form-item>
      <el-form-item label="账号类型" prop="customerType">
        <el-select v-model="queryParams.customerType" placeholder="全部类型" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="分公司" value="1" />
          <el-option label="服务商" value="2" />
          <el-option label="个人" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属分公司" prop="branchPhone" v-if="isAdmin">
        <el-input
          v-model="queryParams.branchPhone"
          placeholder="请输入分公司手机号"
          clearable
          style="width: 180px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 概览卡片 -->
    <el-row :gutter="16" class="overview-row" v-loading="overviewLoading">
      <!-- 总后台指标 -->
      <template v-if="isAdmin">
        <el-col :span="4" :xs="12">
          <div class="overview-card">
            <div class="overview-label">账号总数</div>
            <div class="overview-value">{{ overview.accountTotal }}</div>
            <div class="overview-sub">分公司 {{ overview.branchTotal }} / 服务商 {{ overview.providerTotal }} / 个人 {{ overview.individualTotal }}</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="overview-card">
            <div class="overview-label">今日新增账号</div>
            <div class="overview-value">{{ overview.accountTodayNew }}</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="overview-card">
            <div class="overview-label">今日算力消耗</div>
            <div class="overview-value">{{ overview.computingTodayConsume }}</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="overview-card">
            <div class="overview-label">今日视频生成</div>
            <div class="overview-value">{{ overview.videoTodayGenerate }}</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="overview-card">
            <div class="overview-label">今日二维码</div>
            <div class="overview-value">{{ overview.qrScanToday }}</div>
            <div class="overview-sub">下载 {{ overview.qrDownloadToday }} / 注册 {{ overview.qrRegisterToday }}</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="overview-card">
            <div class="overview-label">喜报业绩金额</div>
            <div class="overview-value">{{ overview.newsSalesAmount }}</div>
          </div>
        </el-col>
      </template>
      <!-- 分公司后台指标 -->
      <template v-else>
        <el-col :span="4" :xs="12">
          <div class="overview-card">
            <div class="overview-label">服务商总数</div>
            <div class="overview-value">{{ overview.branchServiceProviderCount }}</div>
            <div class="overview-sub">剩余名额 {{ overview.branchMaxServiceProviderRemain }}</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="overview-card">
            <div class="overview-label">个人账号总数</div>
            <div class="overview-value">{{ overview.branchIndividualCount }}</div>
            <div class="overview-sub">剩余名额 {{ overview.branchServiceProviderRemain }}</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="overview-card">
            <div class="overview-label">今日新增账号</div>
            <div class="overview-value">{{ overview.accountTodayNew }}</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="overview-card">
            <div class="overview-label">今日算力消耗</div>
            <div class="overview-value">{{ overview.computingTodayConsume }}</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="overview-card">
            <div class="overview-label">今日视频生成</div>
            <div class="overview-value">{{ overview.videoTodayGenerate }}</div>
          </div>
        </el-col>
        <el-col :span="4" :xs="12">
          <div class="overview-card">
            <div class="overview-label">今日二维码</div>
            <div class="overview-value">{{ overview.qrScanToday }}</div>
            <div class="overview-sub">下载 {{ overview.qrDownloadToday }} / 注册 {{ overview.qrRegisterToday }}</div>
          </div>
        </el-col>
      </template>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="18" :xs="24">
        <div class="chart-card">
          <div class="chart-title">近 {{ queryParams.days || 7 }} 天趋势</div>
          <div ref="trendChart" class="chart-body"></div>
          <el-empty v-if="!trendData.dates || trendData.dates.length === 0" description="暂无趋势数据" class="chart-empty"></el-empty>
        </div>
      </el-col>
      <el-col :span="6" :xs="24">
        <div class="chart-card">
          <div class="chart-title">账号类型分布</div>
          <div ref="pieChart" class="chart-body"></div>
          <el-empty v-if="!trendData.accountTypePie || trendData.accountTypePie.length === 0" description="暂无分布数据" class="chart-empty"></el-empty>
        </div>
      </el-col>
    </el-row>

    <!-- 数据维度 Tab -->
    <el-tabs v-model="activeTab" type="border-card" @tab-click="handleTabClick" class="stat-tabs">
      <el-tab-pane label="账号数据" name="account">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="el-icon-download"
              size="mini"
              @click="handleExport"
              v-hasPermi="['batch:statistics:export']"
            >导出</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
        <el-table v-loading="loading" :data="list" @sort-change="handleSortChange">
          <el-table-column label="账号类型" align="center" prop="customerType" width="100">
            <template slot-scope="scope">
              <span>{{ formatCustomerType(scope.row.customerType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="账号名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
          <el-table-column label="联系人" align="center" prop="contactName" width="120" />
          <el-table-column label="手机号" align="center" prop="phone" width="140" />
          <el-table-column label="上级手机号" align="center" prop="parentPhone" width="140" />
          <el-table-column label="所属分公司手机号" align="center" prop="branchPhone" width="150" />
          <el-table-column label="状态" align="center" prop="status" width="80">
            <template slot-scope="scope">
              <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createTime" width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="算力数据" name="computing">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="el-icon-download"
              size="mini"
              @click="handleExport"
              v-hasPermi="['batch:statistics:export']"
            >导出</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
        <el-table v-loading="loading" :data="list">
          <el-table-column label="账号名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
          <el-table-column label="手机号" align="center" prop="phone" width="140" />
          <el-table-column label="操作类型" align="center" prop="operationType" width="100">
            <template slot-scope="scope">
              <span>{{ scope.row.operationType === 2 ? '下载' : '生成' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="消耗算力" align="center" prop="consumeValue" width="120" />
          <el-table-column label="剩余算力" align="center" prop="remainValue" width="120" />
          <el-table-column label="关联视频组" align="center" prop="videoGroupName" :show-overflow-tooltip="true" />
          <el-table-column label="操作时间" align="center" prop="createTime" width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="视频生成" name="video">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="el-icon-download"
              size="mini"
              @click="handleExport"
              v-hasPermi="['batch:statistics:export']"
            >导出</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
        <el-table v-loading="loading" :data="list">
          <el-table-column label="账号名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
          <el-table-column label="视频组名称" align="center" prop="videoGroupName" :show-overflow-tooltip="true" />
          <el-table-column label="生成数量" align="center" prop="generateCount" width="100" />
          <el-table-column label="状态" align="center" prop="status" width="100">
            <template slot-scope="scope">
              <span>{{ scope.row.status === 1 ? '失败' : '成功' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="生成时间" align="center" prop="createTime" width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="二维码推广" name="qrcode">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="el-icon-download"
              size="mini"
              @click="handleExport"
              v-hasPermi="['batch:statistics:export']"
            >导出</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
        <el-table v-loading="loading" :data="list">
          <el-table-column label="二维码所属账号" align="center" prop="customerName" :show-overflow-tooltip="true" />
          <el-table-column label="账号类型" align="center" prop="customerType" width="100">
            <template slot-scope="scope">
              <span>{{ formatCustomerType(scope.row.customerType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="手机号" align="center" prop="phone" width="140" />
          <el-table-column label="扫码次数" align="center" prop="scanCount" width="100" />
          <el-table-column label="下载量" align="center" prop="downloadCount" width="100" />
          <el-table-column label="注册数" align="center" prop="registerCount" width="100" />
          <el-table-column label="统计日期" align="center" prop="statDate" width="120">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.statDate, '{y}-{m}-{d}') }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="业绩喜报" name="news">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="el-icon-download"
              size="mini"
              @click="handleExport"
              v-hasPermi="['batch:statistics:export']"
            >导出</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
        <el-table v-loading="loading" :data="list">
          <el-table-column label="业绩标题" align="center" prop="newsTitle" :show-overflow-tooltip="true" />
          <el-table-column label="销售冠军" align="center" prop="championName" width="120" />
          <el-table-column label="销售金额" align="center" prop="salesAmount" width="120" />
          <el-table-column label="状态" align="center" prop="status" width="100">
            <template slot-scope="scope">
              <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
            </template>
          </el-table-column>
          <el-table-column label="更新时间" align="center" prop="updateTime" width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.updateTime) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  getStatisticsOverview,
  getStatisticsTrend,
  listStatisticsAccount,
  listStatisticsComputing,
  listStatisticsVideo,
  listStatisticsQrcode,
  listStatisticsNews,
  exportStatisticsAccount,
  exportStatisticsComputing,
  exportStatisticsVideo,
  exportStatisticsQrcode,
  exportStatisticsNews
} from "@/api/batch/statistics"
import { saveAs } from 'file-saver'

export default {
  name: "BatchStatistics",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      showSearch: true,
      loading: false,
      overviewLoading: false,
      activeTab: 'account',
      isAdmin: false,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        startDate: undefined,
        endDate: undefined,
        customerType: undefined,
        branchPhone: undefined,
        days: 7
      },
      overview: {},
      trendData: {
        dates: [],
        accountTrend: [],
        computingTrend: [],
        videoTrend: [],
        qrScanTrend: [],
        accountTypePie: []
      },
      list: [],
      total: 0,
      trendChart: null,
      pieChart: null,
      pickerOptions: {
        shortcuts: [
          {
            text: '今天',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              picker.$emit('pick', [start, end])
            }
          },
          {
            text: '最近7天',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 6)
              picker.$emit('pick', [start, end])
            }
          },
          {
            text: '最近30天',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 29)
              picker.$emit('pick', [start, end])
            }
          }
        ]
      }
    }
  },
  created() {
    this.isAdmin = this.$store.getters.roles && this.$store.getters.roles.includes('admin')
    this.initDefaultDateRange()
    this.getOverview()
    this.getTrend()
    this.getList()
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts()
      window.addEventListener('resize', this.handleResize)
    })
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.trendChart) {
      this.trendChart.dispose()
      this.trendChart = null
    }
    if (this.pieChart) {
      this.pieChart.dispose()
      this.pieChart = null
    }
  },
  methods: {
    initDefaultDateRange() {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 6)
      this.dateRange = [
        this.parseDate(start),
        this.parseDate(end)
      ]
      this.queryParams.startDate = this.dateRange[0]
      this.queryParams.endDate = this.dateRange[1]
    },
    parseDate(date) {
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    },
    formatCustomerType(type) {
      if (type === 1) return '分公司'
      if (type === 2) return '服务商'
      if (type === 3) return '个人'
      return '-'
    },
    handleDateChange(val) {
      if (val && val.length === 2) {
        this.queryParams.startDate = val[0]
        this.queryParams.endDate = val[1]
      } else {
        this.queryParams.startDate = undefined
        this.queryParams.endDate = undefined
      }
    },
    getOverview() {
      this.overviewLoading = true
      getStatisticsOverview(this.queryParams).then(response => {
        this.overview = response.data || {}
        this.overviewLoading = false
      }).catch(() => {
        this.overviewLoading = false
        this.$modal.msgError('数据加载失败，请刷新')
      })
    },
    getTrend() {
      getStatisticsTrend(this.queryParams).then(response => {
        this.trendData = response.data || {
          dates: [],
          accountTrend: [],
          computingTrend: [],
          videoTrend: [],
          qrScanTrend: [],
          accountTypePie: []
        }
        this.updateCharts()
      }).catch(() => {
        this.$modal.msgError('趋势数据加载失败')
      })
    },
    getList() {
      this.loading = true
      let request
      switch (this.activeTab) {
        case 'account':
          request = listStatisticsAccount(this.queryParams)
          break
        case 'computing':
          request = listStatisticsComputing(this.queryParams)
          break
        case 'video':
          request = listStatisticsVideo(this.queryParams)
          break
        case 'qrcode':
          request = listStatisticsQrcode(this.queryParams)
          break
        case 'news':
          request = listStatisticsNews(this.queryParams)
          break
        default:
          request = listStatisticsAccount(this.queryParams)
      }
      request.then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => {
        this.loading = false
        this.$modal.msgError('数据加载失败，请刷新')
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getOverview()
      this.getTrend()
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.initDefaultDateRange()
      this.queryParams.customerType = undefined
      this.queryParams.branchPhone = undefined
      this.queryParams.days = 7
      this.handleQuery()
    },
    handleTabClick() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleSortChange() {
      this.getList()
    },
    handleExport() {
      let exportFunc
      let fileName
      switch (this.activeTab) {
        case 'account':
          exportFunc = exportStatisticsAccount
          fileName = '账号数据.xlsx'
          break
        case 'computing':
          exportFunc = exportStatisticsComputing
          fileName = '算力消耗数据.xlsx'
          break
        case 'video':
          exportFunc = exportStatisticsVideo
          fileName = '视频生成数据.xlsx'
          break
        case 'qrcode':
          exportFunc = exportStatisticsQrcode
          fileName = '二维码推广数据.xlsx'
          break
        case 'news':
          exportFunc = exportStatisticsNews
          fileName = '业绩喜报数据.xlsx'
          break
        default:
          exportFunc = exportStatisticsAccount
          fileName = '账号数据.xlsx'
      }
      this.$modal.confirm('是否确认导出当前筛选条件下的数据？').then(() => {
        exportFunc(this.queryParams).then(response => {
          this.downloadFile(response.data, fileName)
          this.$modal.msgSuccess('导出成功')
        }).catch(() => {
          this.$modal.msgError('导出失败，请稍后重试')
        })
      }).catch(() => {})
    },
    downloadFile(response, fileName) {
      const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      saveAs(blob, fileName)
    },
    initCharts() {
      this.trendChart = echarts.init(this.$refs.trendChart)
      this.pieChart = echarts.init(this.$refs.pieChart)
      this.updateCharts()
    },
    updateCharts() {
      this.updateTrendChart()
      this.updatePieChart()
    },
    updateTrendChart() {
      if (!this.trendChart) return
      const dates = this.trendData.dates || []
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['新增账号', '算力消耗', '视频生成', '二维码扫码'],
          bottom: 0
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: dates
        },
        yAxis: [
          {
            type: 'value',
            name: '数量'
          },
          {
            type: 'value',
            name: '算力',
            position: 'right'
          }
        ],
        series: [
          {
            name: '新增账号',
            type: 'line',
            data: this.trendData.accountTrend || []
          },
          {
            name: '算力消耗',
            type: 'line',
            yAxisIndex: 1,
            data: this.trendData.computingTrend || []
          },
          {
            name: '视频生成',
            type: 'line',
            data: this.trendData.videoTrend || []
          },
          {
            name: '二维码扫码',
            type: 'line',
            data: this.trendData.qrScanTrend || []
          }
        ]
      }
      this.trendChart.setOption(option, true)
    },
    updatePieChart() {
      if (!this.pieChart) return
      const data = this.trendData.accountTypePie || []
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 5,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: true,
              formatter: '{b}: {c}'
            },
            data: data
          }
        ]
      }
      this.pieChart.setOption(option, true)
    },
    handleResize() {
      if (this.trendChart) this.trendChart.resize()
      if (this.pieChart) this.pieChart.resize()
    }
  }
}
</script>

<style scoped>
.overview-row {
  margin-bottom: 16px;
}
.overview-card {
  background: #fff;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}
.overview-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}
.overview-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 4px;
}
.overview-sub {
  font-size: 12px;
  color: #909399;
}
.chart-row {
  margin-bottom: 16px;
}
.chart-card {
  background: #fff;
  border-radius: 4px;
  padding: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  position: relative;
  min-height: 320px;
}
.chart-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 12px;
  color: #303133;
}
.chart-body {
  width: 100%;
  height: 260px;
}
.chart-empty {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.stat-tabs {
  background: #fff;
}
</style>

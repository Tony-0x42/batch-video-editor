<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入客户手机号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="生成时间">
        <el-date-picker
          v-model="dateRange"
          size="small"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList">
      <el-table-column label="ID" align="center" prop="logId" width="80" />
      <el-table-column label="客户手机号" align="center" prop="phone" width="120" />
      <el-table-column label="视频组" align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ scope.row.groupName || scope.row.groupId || "-" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <el-tag :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="进度" align="center" prop="progress" width="90">
        <template slot-scope="scope">
          <span>{{ progressText(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="消耗算力" align="center" prop="consumeValue" width="90">
        <template slot-scope="scope">
          <span>{{ scope.row.consumeValue != null ? scope.row.consumeValue : "-" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="失败原因" align="center" prop="errorMsg" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ scope.row.errorMsg || "-" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="生成时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="100">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.resultUrl"
            size="mini"
            type="text"
            icon="el-icon-video-play"
            @click="handleViewResult(scope.row)"
          >查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listAiVideoLog } from "@/api/batch/aiVideoLog"

export default {
  name: "BatchAiVideoLog",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 生成记录表格数据
      logList: [],
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        phone: undefined,
        status: undefined
      },
      // 状态选项（0=处理中 1=成功 2=失败）
      statusOptions: [
        { value: 0, label: "处理中" },
        { value: 1, label: "成功" },
        { value: 2, label: "失败" }
      ],
      baseUrl: process.env.VUE_APP_BASE_API
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询生成记录列表 */
    getList() {
      this.loading = true
      const params = {
        ...this.queryParams,
        params: this.dateRange && this.dateRange.length === 2
          ? { beginTime: this.dateRange[0], endTime: this.dateRange[1] }
          : {}
      }
      listAiVideoLog(params).then(response => {
        this.logList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => {
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
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    statusText(status) {
      const item = this.statusOptions.find(i => i.value === status)
      return item ? item.label : "未知"
    },
    statusType(status) {
      switch (status) {
        case 0: return "warning"
        case 1: return "success"
        case 2: return "danger"
        default: return "info"
      }
    },
    progressText(row) {
      if (row.progress == null) return "-"
      if (row.status === 1) return "100%"
      return row.progress + "%"
    },
    /** 工具：补全资源 URL */
    fullUrl(url) {
      if (!url) return ""
      return url.startsWith("http") ? url : this.baseUrl + url
    },
    /** 查看生成结果 */
    handleViewResult(row) {
      window.open(this.fullUrl(row.resultUrl), "_blank")
    }
  }
}
</script>

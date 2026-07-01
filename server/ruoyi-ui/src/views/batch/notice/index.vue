<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="公告标题" prop="noticeTitle">
        <el-input
          v-model="queryParams.noticeTitle"
          placeholder="请输入公告标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="公告类型" prop="noticeType">
        <el-select v-model="queryParams.noticeType" placeholder="请选择公告类型" clearable>
          <el-option
            v-for="item in noticeTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发布状态" prop="publishStatus">
        <el-select v-model="queryParams.publishStatus" placeholder="请选择发布状态" clearable>
          <el-option
            v-for="item in publishStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发布时间">
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
      <el-col :span="1.5">
        <el-button
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['batch:notice:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['batch:notice:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['batch:notice:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['batch:notice:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="noticeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" prop="noticeId" width="80" />
      <el-table-column label="公告标题" align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-link type="primary" :underline="false" @click="handleView(scope.row)">{{ scope.row.noticeTitle }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="公告类型" align="center" prop="noticeType" width="100">
        <template slot-scope="scope">
          <el-tag :type="noticeTypeType(scope.row.noticeType)">{{ noticeTypeText(scope.row.noticeType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布人" align="center" prop="createBy" width="100" />
      <el-table-column label="发布时间" align="center" prop="publishTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.publishTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="publishStatus" width="90">
        <template slot-scope="scope">
          <el-tag :type="publishStatusType(scope.row.publishStatus)">{{ publishStatusText(scope.row.publishStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="阅读量" align="center" prop="readCount" width="80" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="280">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['batch:notice:query']"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['batch:notice:edit']"
          >编辑</el-button>
          <el-button
            v-if="scope.row.publishStatus !== 0"
            size="mini"
            type="text"
            icon="el-icon-s-promotion"
            @click="handlePublish(scope.row)"
            v-hasPermi="['batch:notice:publish']"
          >发布</el-button>
          <el-button
            v-if="scope.row.publishStatus === 0"
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="handleUnpublish(scope.row)"
            v-hasPermi="['batch:notice:edit']"
          >下架</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['batch:notice:remove']"
          >删除</el-button>
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

    <!-- 添加或修改公告对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="850px" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="公告标题" prop="noticeTitle">
              <el-input v-model="form.noticeTitle" placeholder="请输入公告标题" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公告类型" prop="noticeType">
              <el-select v-model="form.noticeType" placeholder="请选择公告类型" style="width: 100%">
                <el-option
                  v-for="item in noticeTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发布状态" prop="publishStatus">
              <el-select v-model="form.publishStatus" placeholder="请选择发布状态" style="width: 100%">
                <el-option
                  v-for="item in publishStatusFormOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="封面图">
              <image-upload v-model="form.coverUrl" :limit="1" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="公告内容" prop="content">
              <editor v-model="form.content" :min-height="320" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 公告预览对话框 -->
    <el-dialog title="公告预览" :visible.sync="previewOpen" width="460px" append-to-body :close-on-click-modal="false">
      <div class="notice-preview">
        <h3 class="preview-title">{{ previewData.noticeTitle }}</h3>
        <div class="preview-meta">
          <span>{{ parseTime(previewData.publishTime) || previewData.createTime }}</span>
          <span class="ml10">{{ noticeTypeText(previewData.noticeType) }}</span>
        </div>
        <el-image v-if="previewData.coverUrl" :src="previewData.coverUrl" fit="cover" class="preview-cover" />
        <div class="preview-content" v-html="previewData.content"></div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="previewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listNotice, getNotice, addNotice, updateNotice, delNotice, publishNotice, unpublishNotice } from "@/api/batch/notice"

export default {
  name: "BatchNotice",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 公告表格数据
      noticeList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 预览弹窗
      previewOpen: false,
      previewData: {},
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        noticeTitle: undefined,
        noticeType: undefined,
        publishStatus: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        noticeTitle: [
          { required: true, message: "公告标题不能为空", trigger: "blur" }
        ],
        noticeType: [
          { required: true, message: "公告类型不能为空", trigger: "change" }
        ],
        publishStatus: [
          { required: true, message: "发布状态不能为空", trigger: "change" }
        ],
        content: [
          { required: true, message: "公告内容不能为空", trigger: "blur" }
        ]
      },
      noticeTypeOptions: [
        { value: 1, label: "通知" },
        { value: 2, label: "活动" },
        { value: 3, label: "重要更新" }
      ],
      publishStatusOptions: [
        { value: 0, label: "已发布" },
        { value: 1, label: "已下架" },
        { value: 2, label: "暂存" }
      ],
      publishStatusFormOptions: [
        { value: 0, label: "立即发布" },
        { value: 2, label: "暂存" }
      ]
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询公告列表 */
    getList() {
      this.loading = true
      const params = {
        ...this.queryParams,
        params: this.dateRange && this.dateRange.length === 2
          ? { beginTime: this.dateRange[0], endTime: this.dateRange[1] }
          : {}
      }
      listNotice(params).then(response => {
        this.noticeList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        noticeId: undefined,
        noticeTitle: undefined,
        noticeType: 1,
        coverUrl: undefined,
        content: undefined,
        publishStatus: 0,
        readCount: 0
      }
      this.resetForm("form")
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.noticeId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增公告"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const noticeId = row.noticeId || this.ids
      getNotice(noticeId).then(response => {
        this.form = response.data
        // 已下架时编辑也保持下架状态，其余状态可继续编辑
        if (this.form.publishStatus === undefined || this.form.publishStatus === null) {
          this.form.publishStatus = 0
        }
        this.open = true
        this.title = "编辑公告"
      })
    },
    /** 查看公告 */
    handleView(row) {
      getNotice(row.noticeId).then(response => {
        this.previewData = response.data
        this.previewOpen = true
      })
    },
    /** 发布公告 */
    handlePublish(row) {
      this.$modal.confirm('是否确认发布标题为"' + row.noticeTitle + '"的公告？').then(() => {
        return publishNotice(row.noticeId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("发布成功")
      }).catch(() => {})
    },
    /** 下架公告 */
    handleUnpublish(row) {
      this.$modal.confirm('是否确认下架标题为"' + row.noticeTitle + '"的公告？').then(() => {
        return unpublishNotice(row.noticeId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("下架成功")
      }).catch(() => {})
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          const data = { ...this.form }
          // 下架状态编辑时不允许直接改回已发布，需通过发布按钮操作
          if (this.form.noticeId && data.publishStatus === 0) {
            const old = this.noticeList.find(item => item.noticeId === data.noticeId)
            if (old && old.publishStatus === 1) {
              data.publishStatus = 1
            }
          }
          if (this.form.noticeId != undefined) {
            updateNotice(data).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addNotice(data).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const noticeIds = row.noticeId || this.ids
      this.$modal.confirm('是否确认删除公告编号为"' + noticeIds + '"的数据项？').then(() => {
        return delNotice(noticeIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('/batch/notice/export', {
        ...this.queryParams
      }, `notice_${new Date().getTime()}.xlsx`)
    },
    noticeTypeText(type) {
      const item = this.noticeTypeOptions.find(i => i.value === type)
      return item ? item.label : "未知"
    },
    noticeTypeType(type) {
      switch (type) {
        case 1: return ""
        case 2: return "success"
        case 3: return "danger"
        default: return "info"
      }
    },
    publishStatusText(status) {
      const item = this.publishStatusOptions.find(i => i.value === status)
      return item ? item.label : "未知"
    },
    publishStatusType(status) {
      switch (status) {
        case 0: return "success"
        case 1: return "info"
        case 2: return "warning"
        default: return "info"
      }
    }
  }
}
</script>

<style scoped>
.notice-preview {
  max-height: 600px;
  overflow-y: auto;
  padding: 0 10px;
}
.preview-title {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.4;
}
.preview-meta {
  color: #909399;
  font-size: 13px;
  margin-bottom: 15px;
}
.ml10 {
  margin-left: 10px;
}
.preview-cover {
  width: 100%;
  height: 180px;
  border-radius: 4px;
  margin-bottom: 15px;
}
.preview-content {
  font-size: 14px;
  line-height: 1.8;
  word-break: break-word;
}
.preview-content >>> img {
  max-width: 100%;
  height: auto;
  display: block;
}
</style>

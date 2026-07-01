<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="文档标题" prop="documentTitle">
        <el-input
          v-model="queryParams.documentTitle"
          placeholder="请输入文档标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="文档类型" prop="documentType">
        <el-select v-model="queryParams.documentType" placeholder="全部" clearable style="width: 120px">
          <el-option
            v-for="item in documentTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="适用页面" prop="applyPages">
        <el-select v-model="queryParams.applyPages" placeholder="全部" clearable style="width: 140px">
          <el-option
            v-for="item in applyPageOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 100px">
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
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['batch:document:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['batch:document:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['batch:document:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="documentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" align="center" width="60" />
      <el-table-column label="文档标题" align="center" prop="documentTitle" :show-overflow-tooltip="true" />
      <el-table-column label="文档类型" align="center" prop="documentType" width="110">
        <template slot-scope="scope">
          <el-tag :type="typeStyle(scope.row.documentType)" size="small">{{ typeText(scope.row.documentType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="适用页面" align="center" prop="applyPages" width="180">
        <template slot-scope="scope">
          <el-tag v-for="(page, index) in formatApplyPages(scope.row.applyPages)" :key="index" size="small" style="margin: 2px">{{ page }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序权重" align="center" prop="sortWeight" width="90" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            :disabled="scope.row.isSystem == 1"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="['batch:document:edit']"
          />
          <span style="margin-left: 5px">{{ scope.row.status == '0' ? '启用' : '禁用' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="系统默认" align="center" prop="isSystem" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isSystem == 1" type="danger" size="small">是</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="240">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handlePreview(scope.row)"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['batch:document:edit']"
          >编辑</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            :disabled="scope.row.isSystem == 1"
            @click="handleDelete(scope.row)"
            v-hasPermi="['batch:document:remove']"
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

    <!-- 新增/编辑文档弹窗 -->
    <el-dialog :title="title" :visible.sync="open" width="850px" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="文档标题" prop="documentTitle">
              <el-input v-model="form.documentTitle" placeholder="请输入文档标题" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="文档类型" prop="documentType">
              <el-select v-model="form.documentType" placeholder="请选择文档类型" style="width: 100%">
                <el-option
                  v-for="item in documentTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="适用页面" prop="applyPagesArray">
              <el-select v-model="form.applyPagesArray" multiple collapse-tags placeholder="请选择适用页面" style="width: 100%">
                <el-option
                  v-for="item in applyPageOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序权重" prop="sortWeight">
              <el-input-number v-model="form.sortWeight" :min="0" :max="9999" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="0">启用</el-radio>
                <el-radio label="1">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="文档内容" prop="content">
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

    <!-- 预览弹窗 -->
    <el-dialog title="文档预览" :visible.sync="previewOpen" width="700px" append-to-body>
      <div class="document-preview">
        <h2 class="preview-title">{{ previewData.documentTitle }}</h2>
        <div class="preview-meta">
          <span>类型：{{ typeText(previewData.documentType) }}</span>
          <span style="margin-left: 20px">更新时间：{{ parseTime(previewData.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </div>
        <div class="preview-content" v-html="previewData.content"></div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="previewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDocument, getDocument, delDocument, addDocument, updateDocument, changeDocumentStatus } from "@/api/batch/document"

export default {
  name: "BatchDocument",
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
      // 文档表格数据
      documentList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示预览弹窗
      previewOpen: false,
      // 预览数据
      previewData: {},
      // 文档类型选项
      documentTypeOptions: [
        { value: "1", label: "用户协议" },
        { value: "2", label: "隐私政策" },
        { value: "3", label: "新手文档" },
        { value: "4", label: "帮助文档" }
      ],
      // 适用页面选项
      applyPageOptions: [
        { value: "login", label: "登录页" },
        { value: "register", label: "注册页" },
        { value: "mine", label: "我的页" },
        { value: "guide", label: "新手文档页" },
        { value: "help", label: "帮助中心" },
        { value: "setting", label: "系统设置页" }
      ],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        documentTitle: undefined,
        documentType: undefined,
        applyPages: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        documentTitle: [
          { required: true, message: "文档标题不能为空", trigger: "blur" }
        ],
        documentType: [
          { required: true, message: "文档类型不能为空", trigger: "change" }
        ],
        applyPagesArray: [
          { type: "array", required: true, message: "适用页面不能为空", trigger: "change" }
        ],
        content: [
          { required: true, message: "文档内容不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询文档列表 */
    getList() {
      this.loading = true
      listDocument(this.queryParams).then(response => {
        this.documentList = (response.rows || []).map(row => {
          return {
            ...row,
            status: String(row.status),
            documentType: String(row.documentType)
          }
        })
        this.total = response.total
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
        documentId: undefined,
        documentTitle: undefined,
        documentType: undefined,
        applyPagesArray: [],
        content: undefined,
        sortWeight: 0,
        status: "0"
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
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.documentId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增文档"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const documentId = row.documentId || this.ids
      getDocument(documentId).then(response => {
        this.form = response.data
        this.form.status = String(this.form.status)
        this.form.documentType = String(this.form.documentType)
        this.form.applyPagesArray = this.form.applyPages ? this.form.applyPages.split(",") : []
        this.open = true
        this.title = "修改文档"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (!this.form.applyPagesArray || this.form.applyPagesArray.length === 0) {
            this.$modal.msgError("请选择适用页面")
            return
          }
          this.form.applyPages = this.form.applyPagesArray.join(",")
          if (this.form.documentId != undefined) {
            updateDocument(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addDocument(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 状态切换 */
    handleStatusChange(row) {
      const text = row.status == "0" ? "启用" : "禁用"
      this.$modal.confirm('确认要"' + text + '""' + row.documentTitle + '"文档吗？').then(function() {
        return changeDocumentStatus({ documentId: row.documentId, status: row.status })
      }).then(() => {
        this.$modal.msgSuccess(text + "成功")
        this.getList()
      }).catch(() => {
        row.status = row.status == "0" ? "1" : "0"
      })
    },
    /** 查看文档 */
    handlePreview(row) {
      this.previewData = row
      this.previewOpen = true
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const documentIds = row.documentId || this.ids
      if (row.isSystem == 1 || (Array.isArray(documentIds) && documentIds.some(id => this.documentList.find(item => item.documentId === id && item.isSystem == 1)))) {
        this.$modal.msgError("系统默认文档不可删除")
        return
      }
      this.$modal.confirm('是否确认删除文档编号为"' + documentIds + '"的数据项？').then(function() {
        return delDocument(documentIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 文档类型文本 */
    typeText(type) {
      const item = this.documentTypeOptions.find(item => item.value === String(type))
      return item ? item.label : type
    },
    /** 文档类型样式 */
    typeStyle(type) {
      switch (String(type)) {
        case "1": return "primary"
        case "2": return "warning"
        case "3": return "success"
        case "4": return "info"
        default: return ""
      }
    },
    /** 格式化适用页面 */
    formatApplyPages(pages) {
      if (!pages) return []
      return pages.split(",").map(page => {
        const item = this.applyPageOptions.find(item => item.value === page.trim())
        return item ? item.label : page
      })
    }
  }
}
</script>

<style scoped>
.document-preview {
  max-height: 600px;
  overflow-y: auto;
  padding: 0 10px;
}
.preview-title {
  text-align: center;
  margin-bottom: 10px;
  font-size: 20px;
  font-weight: bold;
}
.preview-meta {
  text-align: center;
  color: #999;
  font-size: 13px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}
.preview-content {
  line-height: 1.8;
  font-size: 14px;
  color: #333;
}
.preview-content img {
  max-width: 100%;
}
</style>

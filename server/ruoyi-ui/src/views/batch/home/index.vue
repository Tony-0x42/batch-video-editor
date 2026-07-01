<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
      <!-- 轮播图管理 -->
      <el-tab-pane label="轮播图管理" name="banner">
        <el-form :model="bannerQuery" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="标题" prop="title">
            <el-input v-model="bannerQuery.title" placeholder="请输入标题" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="bannerQuery.status" placeholder="请选择状态" clearable>
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['batch:home:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['batch:home:export']">导出</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="bannerList">
          <el-table-column label="排序权重" align="center" prop="sortWeight" width="80" />
          <el-table-column label="图片" align="center" width="120">
            <template slot-scope="scope">
              <el-image style="width: 80px; height: 40px" :src="fullUrl(scope.row.imageUrl)" :preview-src-list="[fullUrl(scope.row.imageUrl)]" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
          <el-table-column label="跳转链接" align="center" prop="linkUrl" :show-overflow-tooltip="true" />
          <el-table-column label="状态" align="center" width="100">
            <template slot-scope="scope">
              <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" active-color="#409EFF" @change="handleStatusChange(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-arrow-up" @click="handleMove(scope.row, scope.$index, 'up')">上移</el-button>
              <el-button size="mini" type="text" icon="el-icon-arrow-down" @click="handleMove(scope.row, scope.$index, 'down')">下移</el-button>
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['batch:home:edit']">修改</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['batch:home:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="bannerTotal > 0" :total="bannerTotal" :page.sync="bannerQuery.pageNum" :limit.sync="bannerQuery.pageSize" @pagination="getList" />
      </el-tab-pane>

      <!-- 喜报数据管理 -->
      <el-tab-pane label="喜报数据管理" name="news">
        <el-form :model="newsQuery" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
          <el-form-item label="业绩标题" prop="newsTitle">
            <el-input v-model="newsQuery.newsTitle" placeholder="请输入业绩标题" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="销售冠军" prop="championName">
            <el-input v-model="newsQuery.championName" placeholder="请输入销售冠军" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="newsQuery.status" placeholder="请选择状态" clearable>
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['batch:home:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['batch:home:export']">导出</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="newsList">
          <el-table-column label="业绩标题" align="center" prop="newsTitle" :show-overflow-tooltip="true" />
          <el-table-column label="销售冠军" align="center" prop="championName" width="120" />
          <el-table-column label="销售金额" align="center" prop="salesAmount" width="120" />
          <el-table-column label="状态" align="center" width="100">
            <template slot-scope="scope">
              <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" active-color="#409EFF" @change="handleStatusChange(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="更新时间" align="center" prop="updateTime" width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.updateTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['batch:home:edit']">修改</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['batch:home:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="newsTotal > 0" :total="newsTotal" :page.sync="newsQuery.pageNum" :limit.sync="newsQuery.pageSize" @pagination="getList" />
      </el-tab-pane>

      <!-- 功能入口配置 -->
      <el-tab-pane label="功能入口配置" name="entry">
        <el-form :model="entryQuery" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
          <el-form-item label="入口名称" prop="entryName">
            <el-input v-model="entryQuery.entryName" placeholder="请输入入口名称" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="跳转类型" prop="targetType">
            <el-select v-model="entryQuery.targetType" placeholder="请选择跳转类型" clearable>
              <el-option v-for="item in targetTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="entryQuery.status" placeholder="请选择状态" clearable>
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['batch:home:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['batch:home:export']">导出</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="entryList">
          <el-table-column label="入口名称" align="center" prop="entryName" :show-overflow-tooltip="true" />
          <el-table-column label="图标" align="center" width="80">
            <template slot-scope="scope">
              <el-image style="width: 40px; height: 40px" :src="fullUrl(scope.row.iconUrl)" :preview-src-list="[fullUrl(scope.row.iconUrl)]" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column label="跳转类型" align="center" prop="targetType" width="100">
            <template slot-scope="scope">
              <span>{{ targetTypeFormat(scope.row.targetType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="跳转目标" align="center" prop="targetValue" :show-overflow-tooltip="true" />
          <el-table-column label="排序" align="center" prop="sortWeight" width="80" />
          <el-table-column label="状态" align="center" width="100">
            <template slot-scope="scope">
              <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" active-color="#409EFF" @change="handleStatusChange(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-arrow-up" @click="handleMove(scope.row, scope.$index, 'up')">上移</el-button>
              <el-button size="mini" type="text" icon="el-icon-arrow-down" @click="handleMove(scope.row, scope.$index, 'down')">下移</el-button>
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['batch:home:edit']">修改</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['batch:home:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="entryTotal > 0" :total="entryTotal" :page.sync="entryQuery.pageNum" :limit.sync="entryQuery.pageSize" @pagination="getList" />
      </el-tab-pane>

      <!-- 教程入口配置 -->
      <el-tab-pane label="教程入口配置" name="tutorial">
        <el-form :model="tutorialQuery" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
          <el-form-item label="入口标题" prop="title">
            <el-input v-model="tutorialQuery.title" placeholder="请输入入口标题" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="关联文档" prop="documentId">
            <el-select v-model="tutorialQuery.documentId" placeholder="请选择关联文档" clearable>
              <el-option v-for="item in documentOptions" :key="item.documentId" :label="item.documentTitle" :value="item.documentId" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="tutorialQuery.status" placeholder="请选择状态" clearable>
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['batch:home:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['batch:home:export']">导出</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="tutorialList">
          <el-table-column label="入口标题" align="center" prop="title" :show-overflow-tooltip="true" />
          <el-table-column label="封面图" align="center" width="100">
            <template slot-scope="scope">
              <el-image style="width: 60px; height: 40px" :src="fullUrl(scope.row.coverUrl)" :preview-src-list="[fullUrl(scope.row.coverUrl)]" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column label="关联文档" align="center" prop="documentTitle" :show-overflow-tooltip="true" />
          <el-table-column label="排序" align="center" prop="sortWeight" width="80" />
          <el-table-column label="状态" align="center" width="100">
            <template slot-scope="scope">
              <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" active-color="#409EFF" @change="handleStatusChange(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-arrow-up" @click="handleMove(scope.row, scope.$index, 'up')">上移</el-button>
              <el-button size="mini" type="text" icon="el-icon-arrow-down" @click="handleMove(scope.row, scope.$index, 'down')">下移</el-button>
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['batch:home:edit']">修改</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['batch:home:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="tutorialTotal > 0" :total="tutorialTotal" :page.sync="tutorialQuery.pageNum" :limit.sync="tutorialQuery.pageSize" @pagination="getList" />
      </el-tab-pane>
    </el-tabs>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <!-- 轮播图表单 -->
        <template v-if="activeTab === 'banner'">
          <el-form-item label="图片" prop="imageUrl">
            <image-upload v-model="form.imageUrl" :limit="1" :fileSize="5" :fileType="['png', 'jpg', 'jpeg']" />
          </el-form-item>
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入标题" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="跳转链接">
            <el-input v-model="form.linkUrl" placeholder="请输入跳转链接" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="排序权重">
            <el-input-number v-model="form.sortWeight" :min="0" :step="1" step-strictly controls-position="right" />
          </el-form-item>
        </template>

        <!-- 喜报数据表单 -->
        <template v-if="activeTab === 'news'">
          <el-form-item label="业绩标题" prop="newsTitle">
            <el-input v-model="form.newsTitle" placeholder="请输入业绩标题" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="销售冠军" prop="championName">
            <el-input v-model="form.championName" placeholder="请输入销售冠军姓名" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="销售金额" prop="salesAmount">
            <el-input-number v-model="form.salesAmount" :min="0" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
          </el-form-item>
        </template>

        <!-- 功能入口表单 -->
        <template v-if="activeTab === 'entry'">
          <el-form-item label="入口名称" prop="entryName">
            <el-input v-model="form.entryName" placeholder="请输入入口名称" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="图标" prop="iconUrl">
            <image-upload v-model="form.iconUrl" :limit="1" :fileSize="5" :fileType="['png', 'jpg', 'jpeg']" />
          </el-form-item>
          <el-form-item label="跳转类型" prop="targetType">
            <el-select v-model="form.targetType" placeholder="请选择跳转类型" style="width: 100%">
              <el-option v-for="item in targetTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="跳转目标" prop="targetValue">
            <el-input v-model="form.targetValue" placeholder="请输入跳转目标值" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="排序权重">
            <el-input-number v-model="form.sortWeight" :min="0" :step="1" step-strictly controls-position="right" />
          </el-form-item>
        </template>

        <!-- 教程入口表单 -->
        <template v-if="activeTab === 'tutorial'">
          <el-form-item label="入口标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入入口标题" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="封面图" prop="coverUrl">
            <image-upload v-model="form.coverUrl" :limit="1" :fileSize="5" :fileType="['png', 'jpg', 'jpeg']" />
          </el-form-item>
          <el-form-item label="关联文档" prop="documentId">
            <el-select v-model="form.documentId" placeholder="请选择关联文档" filterable clearable style="width: 100%">
              <el-option v-for="item in documentOptions" :key="item.documentId" :label="item.documentTitle" :value="item.documentId" />
            </el-select>
          </el-form-item>
          <el-form-item label="排序权重">
            <el-input-number v-model="form.sortWeight" :min="0" :step="1" step-strictly controls-position="right" />
          </el-form-item>
        </template>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="item in statusOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listBanner, getBanner, addBanner, updateBanner, delBanner, changeBannerStatus,
  listNews, getNews, addNews, updateNews, delNews, changeNewsStatus,
  listEntry, getEntry, addEntry, updateEntry, delEntry, changeEntryStatus,
  listTutorialEntry, getTutorialEntry, addTutorialEntry, updateTutorialEntry, delTutorialEntry, changeTutorialEntryStatus,
  listDocumentOption
} from "@/api/batch/home"

export default {
  name: "Home",
  data() {
    return {
      baseUrl: process.env.VUE_APP_BASE_API,
      activeTab: "banner",
      loading: true,
      open: false,
      title: "",
      showSearch: true,
      statusOptions: [
        { label: "启用", value: "0" },
        { label: "禁用", value: "1" }
      ],
      targetTypeOptions: [
        { label: "页面", value: "1" },
        { label: "URL", value: "2" },
        { label: "功能码", value: "3" }
      ],
      documentOptions: [],

      // 轮播图
      bannerList: [],
      bannerTotal: 0,
      bannerQuery: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        status: undefined
      },

      // 喜报
      newsList: [],
      newsTotal: 0,
      newsQuery: {
        pageNum: 1,
        pageSize: 10,
        newsTitle: undefined,
        championName: undefined,
        status: undefined
      },

      // 功能入口
      entryList: [],
      entryTotal: 0,
      entryQuery: {
        pageNum: 1,
        pageSize: 10,
        entryName: undefined,
        targetType: undefined,
        status: undefined
      },

      // 教程入口
      tutorialList: [],
      tutorialTotal: 0,
      tutorialQuery: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        documentId: undefined,
        status: undefined
      },

      form: {},
      rules: {
        title: [{ required: true, message: "标题不能为空", trigger: "blur" }],
        imageUrl: [{ required: true, message: "请上传图片", trigger: "change" }],
        newsTitle: [{ required: true, message: "业绩标题不能为空", trigger: "blur" }],
        championName: [{ required: true, message: "销售冠军姓名不能为空", trigger: "blur" }],
        salesAmount: [{ required: true, message: "销售金额不能为空", trigger: "blur" }],
        entryName: [{ required: true, message: "入口名称不能为空", trigger: "blur" }],
        iconUrl: [{ required: true, message: "请上传图标", trigger: "change" }],
        targetType: [{ required: true, message: "请选择跳转类型", trigger: "change" }],
        targetValue: [{ required: true, message: "请输入跳转目标", trigger: "blur" }],
        coverUrl: [{ required: true, message: "请上传封面图", trigger: "change" }],
        documentId: [{ required: true, message: "请选择关联文档", trigger: "change" }]
      }
    }
  },
  created() {
    this.getList()
    this.loadDocumentOptions()
  },
  methods: {
    /** 加载关联文档下拉 */
    loadDocumentOptions() {
      listDocumentOption().then(response => {
        this.documentOptions = response.data || []
      })
    },
    /** Tab 切换 */
    handleTabClick() {
      this.handleQuery()
    },
    /** 查询列表 */
    getList() {
      this.loading = true
      const tab = this.activeTab
      if (tab === "banner") {
        listBanner(this.bannerQuery).then(response => {
          this.bannerList = response.rows
          this.bannerTotal = response.total
          this.loading = false
        })
      } else if (tab === "news") {
        listNews(this.newsQuery).then(response => {
          this.newsList = response.rows
          this.newsTotal = response.total
          this.loading = false
        })
      } else if (tab === "entry") {
        listEntry(this.entryQuery).then(response => {
          this.entryList = response.rows
          this.entryTotal = response.total
          this.loading = false
        })
      } else if (tab === "tutorial") {
        listTutorialEntry(this.tutorialQuery).then(response => {
          this.tutorialList = response.rows
          this.tutorialTotal = response.total
          this.loading = false
        })
      }
    },
    /** 搜索 */
    handleQuery() {
      this.setPageNum(1)
      this.getList()
    },
    /** 重置 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    setPageNum(num) {
      const tab = this.activeTab
      if (tab === "banner") this.bannerQuery.pageNum = num
      else if (tab === "news") this.newsQuery.pageNum = num
      else if (tab === "entry") this.entryQuery.pageNum = num
      else if (tab === "tutorial") this.tutorialQuery.pageNum = num
    },
    /** 取消 */
    cancel() {
      this.open = false
      this.reset()
    },
    /** 表单重置 */
    reset() {
      const tab = this.activeTab
      let defaults = { status: "0", sortWeight: 0 }
      if (tab === "banner") {
        defaults = { bannerId: undefined, title: undefined, imageUrl: undefined, linkUrl: undefined, sortWeight: 0, status: "0" }
      } else if (tab === "news") {
        defaults = { newsId: undefined, newsTitle: undefined, championName: undefined, salesAmount: 0, status: "0" }
      } else if (tab === "entry") {
        defaults = { entryId: undefined, entryName: undefined, iconUrl: undefined, targetType: undefined, targetValue: undefined, sortWeight: 0, status: "0" }
      } else if (tab === "tutorial") {
        defaults = { entryId: undefined, title: undefined, coverUrl: undefined, documentId: undefined, sortWeight: 0, status: "0" }
      }
      this.form = defaults
      this.resetForm("form")
    },
    /** 新增 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = this.getTitlePrefix() + "新增"
    },
    /** 修改 */
    handleUpdate(row) {
      this.reset()
      const tab = this.activeTab
      const id = this.getId(row)
      const getApi = {
        banner: getBanner,
        news: getNews,
        entry: getEntry,
        tutorial: getTutorialEntry
      }
      getApi[tab](id).then(response => {
        this.form = response.data
        this.open = true
        this.title = this.getTitlePrefix() + "修改"
      })
    },
    /** 提交 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) return
        const tab = this.activeTab
        const idField = this.getIdField()
        const addApi = {
          banner: addBanner,
          news: addNews,
          entry: addEntry,
          tutorial: addTutorialEntry
        }
        const updateApi = {
          banner: updateBanner,
          news: updateNews,
          entry: updateEntry,
          tutorial: updateTutorialEntry
        }
        const api = this.form[idField] !== undefined ? updateApi[tab] : addApi[tab]
        api(this.form).then(() => {
          this.$modal.msgSuccess(this.form[idField] !== undefined ? "修改成功" : "新增成功")
          this.open = false
          this.getList()
        })
      })
    },
    /** 删除 */
    handleDelete(row) {
      const tab = this.activeTab
      const id = this.getId(row)
      const delApi = {
        banner: delBanner,
        news: delNews,
        entry: delEntry,
        tutorial: delTutorialEntry
      }
      this.$modal.confirm('是否确认删除该数据项？').then(() => {
        return delApi[tab](id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 状态变更 */
    handleStatusChange(row) {
      const tab = this.activeTab
      const idField = this.getIdField()
      const statusApi = {
        banner: changeBannerStatus,
        news: changeNewsStatus,
        entry: changeEntryStatus,
        tutorial: changeTutorialEntryStatus
      }
      const text = row.status === "0" ? "启用" : "禁用"
      this.$modal.confirm('确认要"' + text + '"该数据项吗？').then(() => {
        const data = { status: row.status }
        data[idField] = row[idField]
        return statusApi[tab](data)
      }).then(() => {
        this.$modal.msgSuccess(text + "成功")
      }).catch(() => {
        this.getList()
      })
    },
    /** 上下移动 */
    handleMove(row, index, direction) {
      const tab = this.activeTab
      if (tab === "news") return
      const listName = tab + "List"
      const list = this[listName]
      const targetIndex = direction === "up" ? index - 1 : index + 1
      if (targetIndex < 0 || targetIndex >= list.length) {
        this.$modal.msgWarning(direction === "up" ? "已经是第一条" : "已经是最后一条")
        return
      }
      const target = list[targetIndex]
      const temp = row.sortWeight
      row.sortWeight = target.sortWeight
      target.sortWeight = temp
      const updateApi = {
        banner: updateBanner,
        entry: updateEntry,
        tutorial: updateTutorialEntry
      }
      Promise.all([updateApi[tab](row), updateApi[tab](target)]).then(() => {
        this.$modal.msgSuccess("排序调整成功")
        this.getList()
      })
    },
    /** 导出 */
    handleExport() {
      const tab = this.activeTab
      const exportMap = {
        banner: { url: "/batch/home/banner/export", query: this.bannerQuery, name: "banner" },
        news: { url: "/batch/home/news/export", query: this.newsQuery, name: "news" },
        entry: { url: "/batch/home/entry/export", query: this.entryQuery, name: "entry" },
        tutorial: { url: "/batch/home/tutorialEntry/export", query: this.tutorialQuery, name: "tutorial" }
      }
      const item = exportMap[tab]
      this.download(item.url, { ...item.query }, item.name + "_" + new Date().getTime() + ".xlsx")
    },
    /** 工具：获取当前 Tab 标题前缀 */
    getTitlePrefix() {
      const map = { banner: "轮播图", news: "喜报数据", entry: "功能入口", tutorial: "教程入口" }
      return map[this.activeTab]
    },
    /** 工具：ID 字段名 */
    getIdField() {
      const map = { banner: "bannerId", news: "newsId", entry: "entryId", tutorial: "entryId" }
      return map[this.activeTab]
    },
    /** 工具：获取行 ID */
    getId(row) {
      return row[this.getIdField()]
    },
    /** 工具：跳转类型格式化 */
    targetTypeFormat(value) {
      const item = this.targetTypeOptions.find(i => i.value === value)
      return item ? item.label : value
    },
    /** 工具：补全资源 URL */
    fullUrl(url) {
      if (!url) return ""
      return url.startsWith("http") ? url : this.baseUrl + url
    }
  }
}
</script>

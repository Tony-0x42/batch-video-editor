<template>
  <el-dialog title="账号升级" :visible.sync="open" width="500px" append-to-body :close-on-click-modal="false">
    <el-form ref="form" :model="form" :rules="rules" label-width="150px">
      <el-form-item label="当前账号">
        <span>{{ row.customerName }}（{{ formatType(row.customerType) }}）</span>
      </el-form-item>
      <el-form-item label="升级后类型">
        <span>{{ formatType(row.customerType === 3 ? 2 : 1) }}</span>
      </el-form-item>
      <el-form-item label="新上级手机号" prop="parentPhone" v-if="row.customerType === 3">
        <el-input v-model="form.parentPhone" placeholder="请输入分公司手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="最大服务商数量" prop="maxServiceProvider" v-if="row.customerType === 2">
        <el-input-number v-model="form.maxServiceProvider" :min="0" :precision="0" :step="1" controls-position="right" style="width: 100%" />
      </el-form-item>
      <el-form-item label="个人账号总容量" prop="totalIndividualCapacity" v-if="row.customerType === 2">
        <el-input-number v-model="form.totalIndividualCapacity" :min="0" :precision="0" :step="1" controls-position="right" style="width: 100%" />
      </el-form-item>
      <el-form-item label="个人账号上限" prop="maxIndividual" v-if="row.customerType === 3">
        <el-input-number v-model="form.maxIndividual" :min="0" :precision="0" :step="1" controls-position="right" style="width: 100%" />
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="cancel">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { upgradeCustomer } from "@/api/batch/customer"

export default {
  name: "CustomerUpgradeDialog",
  data() {
    return {
      open: false,
      row: {},
      form: {
        parentPhone: undefined,
        maxServiceProvider: undefined,
        totalIndividualCapacity: undefined,
        maxIndividual: undefined
      },

    }
  },
  computed: {
    rules() {
      const baseRules = {}
      if (this.row.customerType === 3) {
        baseRules.parentPhone = [
          { required: true, message: "新上级手机号不能为空", trigger: "blur" },
          { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
        ]
        baseRules.maxIndividual = [
          { required: true, message: "个人账号上限不能为空", trigger: "change" }
        ]
      }
      if (this.row.customerType === 2) {
        baseRules.maxServiceProvider = [
          { required: true, message: "最大服务商数量不能为空", trigger: "change" }
        ]
        baseRules.totalIndividualCapacity = [
          { required: true, message: "个人账号总容量不能为空", trigger: "change" }
        ]
      }
      return baseRules
    }
  },
  methods: {
    open(row) {
      this.row = row
      this.reset()
      this.open = true
    },
    reset() {
      this.form = {
        parentPhone: undefined,
        maxServiceProvider: undefined,
        totalIndividualCapacity: undefined,
        maxIndividual: undefined
      }
      this.resetForm("form")
    },
    cancel() {
      this.open = false
      this.reset()
    },
    formatType(type) {
      const map = { 1: "分公司", 2: "服务商", 3: "个人" }
      return map[type] || type
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.$modal.confirm('确认要升级账号"' + this.row.customerName + '"吗？').then(() => {
            return upgradeCustomer(this.row.customerId, this.form)
          }).then(() => {
            this.$modal.msgSuccess("升级成功")
            this.open = false
            this.$emit("success")
          }).catch(() => {})
        }
      })
    }
  }
}
</script>

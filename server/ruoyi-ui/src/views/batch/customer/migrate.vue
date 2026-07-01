<template>
  <el-dialog title="账号迁移" :visible.sync="open" width="500px" append-to-body :close-on-click-modal="false">
    <el-form ref="form" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="当前账号">
        <span>{{ row.customerName }}（{{ formatType(row.customerType) }}）</span>
      </el-form-item>
      <el-form-item label="当前上级">
        <span>{{ row.parentPhone || '无' }}</span>
      </el-form-item>
      <el-form-item label="新上级手机号" prop="parentPhone">
        <el-input v-model="form.parentPhone" placeholder="请输入新上级手机号" maxlength="11" />
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="cancel">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { migrateCustomer } from "@/api/batch/customer"

export default {
  name: "CustomerMigrateDialog",
  data() {
    return {
      open: false,
      row: {},
      form: {
        parentPhone: undefined
      },
      rules: {
        parentPhone: [
          { required: true, message: "新上级手机号不能为空", trigger: "blur" },
          { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
        ]
      }
    }
  },
  methods: {
    open(row) {
      this.row = row
      this.reset()
      this.form.parentPhone = row.parentPhone
      this.open = true
    },
    reset() {
      this.form = { parentPhone: undefined }
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
          this.$modal.confirm('确认要迁移账号"' + this.row.customerName + '"的上级吗？').then(() => {
            return migrateCustomer(this.row.customerId, this.form)
          }).then(() => {
            this.$modal.msgSuccess("迁移成功")
            this.open = false
            this.$emit("success")
          }).catch(() => {})
        }
      })
    }
  }
}
</script>

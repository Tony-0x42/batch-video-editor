<template>
  <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body :close-on-click-modal="false">
    <el-form ref="form" :model="form" :rules="rules" label-width="150px">
      <el-row>
        <el-col :span="24">
          <el-form-item label="账号类型" prop="customerType">
            <el-select v-model="form.customerType" placeholder="请选择账号类型" style="width: 100%" :disabled="isEdit">
              <el-option label="分公司" :value="1" />
              <el-option label="服务商" :value="2" />
              <el-option label="个人" :value="3" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="账号名称" prop="customerName">
            <el-input v-model="form.customerName" placeholder="请输入账号名称" maxlength="100" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系人" prop="contactName">
            <el-input v-model="form.contactName" placeholder="请输入联系人" maxlength="50" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" :disabled="isEdit" />
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="form.customerType !== 1">
          <el-form-item label="上级手机号" prop="parentPhone">
            <el-input v-model="form.parentPhone" placeholder="请输入上级手机号" maxlength="11" :disabled="isEdit" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-if="form.customerType === 1">
        <el-col :span="12">
          <el-form-item label="最大服务商数量" prop="maxServiceProvider">
            <el-input-number v-model="form.maxServiceProvider" :min="0" :precision="0" :step="1" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="个人账号总容量" prop="totalIndividualCapacity">
            <el-input-number v-model="form.totalIndividualCapacity" :min="0" :precision="0" :step="1" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-if="form.customerType === 2">
        <el-col :span="12">
          <el-form-item label="个人账号上限" prop="maxIndividual">
            <el-input-number v-model="form.maxIndividual" :min="0" :precision="0" :step="1" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="算力总配额(GF)" prop="computingPowerTotal">
            <el-input-number v-model="form.computingPowerTotal" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="VIP有效期" prop="vipExpireDate">
            <el-date-picker v-model="form.vipExpireDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" maxlength="500" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="submitForm">保 存</el-button>
      <el-button @click="cancel">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { addCustomer, updateCustomer, getCustomer } from "@/api/batch/customer"

export default {
  name: "CustomerFormDialog",
  props: {
    title: {
      type: String,
      default: "新增账号"
    }
  },
  data() {
    return {
      open: false,
      isEdit: false,
      form: {
        customerId: undefined,
        customerType: 1,
        customerName: undefined,
        contactName: undefined,
        phone: undefined,
        parentPhone: undefined,
        branchPhone: undefined,
        maxServiceProvider: 0,
        totalIndividualCapacity: 0,
        maxIndividual: 0,
        computingPowerTotal: undefined,
        vipExpireDate: undefined,
        remark: undefined,
        status: 0
      },
      baseRules: {
        customerType: [
          { required: true, message: "账号类型不能为空", trigger: "change" }
        ],
        customerName: [
          { required: true, message: "账号名称不能为空", trigger: "blur" },
          { min: 2, max: 100, message: "账号名称长度必须介于 2 和 100 之间", trigger: "blur" }
        ],
        contactName: [
          { required: true, message: "联系人不能为空", trigger: "blur" },
          { min: 2, max: 50, message: "联系人长度必须介于 2 和 50 之间", trigger: "blur" }
        ],
        phone: [
          { required: true, message: "手机号不能为空", trigger: "blur" },
          { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
        ],
        computingPowerTotal: [
          { required: true, message: "算力总配额不能为空", trigger: "change" }
        ],
        vipExpireDate: [
          { required: true, message: "VIP有效期不能为空", trigger: "change" }
        ]
      }
    }
  },
  computed: {
    rules() {
      const rules = { ...this.baseRules }
      if (this.form.customerType !== 1) {
        rules.parentPhone = [
          { required: true, message: "上级手机号不能为空", trigger: "blur" },
          { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
        ]
      }
      if (this.form.customerType === 1) {
        rules.maxServiceProvider = [
          { required: true, message: "最大服务商数量不能为空", trigger: "change" }
        ]
        rules.totalIndividualCapacity = [
          { required: true, message: "个人账号总容量不能为空", trigger: "change" }
        ]
      }
      if (this.form.customerType === 2) {
        rules.maxIndividual = [
          { required: true, message: "个人账号上限不能为空", trigger: "change" }
        ]
      }
      return rules
    }
  },
  watch: {
    'form.customerType'(val) {
      if (!this.isEdit) {
        if (val === 1) {
          this.form.parentPhone = undefined
        } else if (val === 2) {
          this.form.parentPhone = undefined
          this.form.maxServiceProvider = undefined
          this.form.totalIndividualCapacity = undefined
        } else if (val === 3) {
          this.form.parentPhone = undefined
          this.form.maxServiceProvider = undefined
          this.form.totalIndividualCapacity = undefined
          this.form.maxIndividual = undefined
        }
      }
    }
  },
  methods: {
    open(customerId) {
      this.reset()
      this.open = true
      if (customerId) {
        this.isEdit = true
        getCustomer(customerId).then(response => {
          this.form = response.data
        })
      } else {
        this.isEdit = false
      }
    },
    reset() {
      this.form = {
        customerId: undefined,
        customerType: 1,
        customerName: undefined,
        contactName: undefined,
        phone: undefined,
        parentPhone: undefined,
        branchPhone: undefined,
        maxServiceProvider: 0,
        totalIndividualCapacity: 0,
        maxIndividual: 0,
        computingPowerTotal: undefined,
        vipExpireDate: undefined,
        remark: undefined,
        status: 0
      }
      this.resetForm("form")
    },
    cancel() {
      this.open = false
      this.reset()
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.isEdit) {
            updateCustomer(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.$emit("success")
            })
          } else {
            addCustomer(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.$emit("success")
              const qrCodeUrl = response.qrCodeUrl || ''
              const customerId = response.customerId
              if (qrCodeUrl && customerId) {
                this.$modal.confirm("账号创建成功，是否立即查看二维码？", "提示", {
                  confirmButtonText: "查看二维码",
                  cancelButtonText: "返回列表",
                  type: "success"
                }).then(() => {
                  this.$router.push("/batch/customer/qrcode/" + customerId)
                }).catch(() => {})
              }
            })
          }
        }
      })
    }
  }
}
</script>

import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi"

// 查询客户列表
export function listCustomer(query) {
  return request({
    url: '/batch/customer/list',
    method: 'get',
    params: query
  })
}

// 查询客户详细
export function getCustomer(customerId) {
  return request({
    url: '/batch/customer/' + parseStrEmpty(customerId),
    method: 'get'
  })
}

// 根据手机号查询客户
export function getCustomerByPhone(phone) {
  return request({
    url: '/batch/customer/phone/' + parseStrEmpty(phone),
    method: 'get'
  })
}

// 新增客户
export function addCustomer(data) {
  return request({
    url: '/batch/customer',
    method: 'post',
    data: data
  })
}

// 修改客户
export function updateCustomer(data) {
  return request({
    url: '/batch/customer',
    method: 'put',
    data: data
  })
}

// 删除客户
export function delCustomer(customerIds) {
  return request({
    url: '/batch/customer/' + customerIds,
    method: 'delete'
  })
}

// 修改客户状态
export function changeCustomerStatus(customerId, status) {
  const data = {
    customerId,
    status
  }
  return request({
    url: '/batch/customer/changeStatus',
    method: 'put',
    data: data
  })
}

// 生成/重置二维码
export function resetCustomerQrCode(customerId) {
  return request({
    url: '/batch/customer/qrCode/' + customerId,
    method: 'put'
  })
}

// 查询二维码推广累计统计
export function getCustomerQrCodeStat(customerId) {
  return request({
    url: '/batch/customer/qrCode/stat/' + customerId,
    method: 'get'
  })
}

// 下载注册二维码（后端记录下载次数后重定向到图片，axios 自动跟随 302）
export function downloadCustomerQrCode(customerId) {
  return request({
    url: '/batch/customer/qrCode/download/' + customerId,
    method: 'get',
    responseType: 'blob'
  })
}

// 账号升级
export function upgradeCustomer(customerId, data) {
  return request({
    url: '/batch/customer/upgrade/' + customerId,
    method: 'put',
    data: data
  })
}

// 重置客户密码
export function resetCustomerPassword(customerId, password) {
  const data = {
    customerId,
    password
  }
  return request({
    url: '/batch/customer/resetPassword',
    method: 'put',
    data: data
  })
}

// 账号迁移
export function migrateCustomer(customerId, data) {
  return request({
    url: '/batch/customer/migrate/' + customerId,
    method: 'put',
    data: data
  })
}

// 导出客户
export function exportCustomer(query) {
  return request({
    url: '/batch/customer/export',
    method: 'post',
    data: query,
    responseType: 'blob'
  })
}

// 下载导入模板
export function importTemplate() {
  return request({
    url: '/batch/customer/importTemplate',
    method: 'get',
    responseType: 'blob'
  })
}

// 导入客户
export function importData(data) {
  return request({
    url: '/batch/customer/importData',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

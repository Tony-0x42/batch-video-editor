import request from '@/utils/request'

// 查询VIP客户列表
export function listVip(query) {
  return request({
    url: '/batch/vip/list',
    method: 'get',
    params: query
  })
}

// 修改单个客户VIP有效期
export function updateVip(customerId, vipExpireDate) {
  return request({
    url: '/batch/vip/' + customerId,
    method: 'put',
    data: { vipExpireDate: vipExpireDate }
  })
}

// 批量修改客户VIP有效期
export function batchUpdateVip(customerIds, vipExpireDate) {
  return request({
    url: '/batch/vip/batch',
    method: 'put',
    data: { customerIds: customerIds, vipExpireDate: vipExpireDate }
  })
}

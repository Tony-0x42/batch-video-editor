import request from '@/utils/request'

// 查询品牌配置
export function getBrandConfig() {
  return request({
    url: '/batch/config/brand',
    method: 'get'
  })
}

// 保存品牌配置
export function saveBrandConfig(data) {
  return request({
    url: '/batch/config/brand',
    method: 'post',
    data: data
  })
}

// 查询全局参数
export function getGlobalConfig() {
  return request({
    url: '/batch/config/global',
    method: 'get'
  })
}

// 保存全局参数
export function saveGlobalConfig(data) {
  return request({
    url: '/batch/config/global',
    method: 'post',
    data: data
  })
}

// 初始化全局参数默认值
export function initGlobalConfig() {
  return request({
    url: '/batch/config/initGlobal',
    method: 'post'
  })
}

// 查询版本列表
export function listVersion(query) {
  return request({
    url: '/batch/config/version/list',
    method: 'get',
    params: query
  })
}

// 查询版本详细
export function getVersion(versionId) {
  return request({
    url: '/batch/config/version/' + versionId,
    method: 'get'
  })
}

// 新增版本
export function addVersion(data) {
  return request({
    url: '/batch/config/version',
    method: 'post',
    data: data
  })
}

// 修改版本
export function updateVersion(data) {
  return request({
    url: '/batch/config/version',
    method: 'put',
    data: data
  })
}

// 删除版本
export function delVersion(versionId) {
  return request({
    url: '/batch/config/version/' + versionId,
    method: 'delete'
  })
}

// 版本状态修改
export function changeVersionStatus(versionId, status) {
  return request({
    url: '/batch/config/version/changeStatus',
    method: 'put',
    data: {
      versionId,
      status
    }
  })
}

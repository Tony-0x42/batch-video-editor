import request from '@/utils/request'

// 查询今日概览指标
export function getStatisticsOverview(query) {
  return request({
    url: '/batch/statistics/overview',
    method: 'get',
    params: query
  })
}

// 查询账号数据明细列表
export function listStatisticsAccount(query) {
  return request({
    url: '/batch/statistics/account',
    method: 'get',
    params: query
  })
}

// 导出账号数据明细
export function exportStatisticsAccount(query) {
  return request({
    url: '/batch/statistics/account/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 查询算力消耗明细列表
export function listStatisticsComputing(query) {
  return request({
    url: '/batch/statistics/computing',
    method: 'get',
    params: query
  })
}

// 导出算力消耗明细
export function exportStatisticsComputing(query) {
  return request({
    url: '/batch/statistics/computing/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 查询视频生成明细列表
export function listStatisticsVideo(query) {
  return request({
    url: '/batch/statistics/video',
    method: 'get',
    params: query
  })
}

// 导出视频生成明细
export function exportStatisticsVideo(query) {
  return request({
    url: '/batch/statistics/video/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 查询二维码推广明细列表
export function listStatisticsQrcode(query) {
  return request({
    url: '/batch/statistics/qrcode',
    method: 'get',
    params: query
  })
}

// 导出二维码推广明细
export function exportStatisticsQrcode(query) {
  return request({
    url: '/batch/statistics/qrcode/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 查询业绩喜报明细列表
export function listStatisticsNews(query) {
  return request({
    url: '/batch/statistics/news',
    method: 'get',
    params: query
  })
}

// 导出业绩喜报明细
export function exportStatisticsNews(query) {
  return request({
    url: '/batch/statistics/news/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 查询趋势数据
export function getStatisticsTrend(query) {
  return request({
    url: '/batch/statistics/trend',
    method: 'get',
    params: query
  })
}

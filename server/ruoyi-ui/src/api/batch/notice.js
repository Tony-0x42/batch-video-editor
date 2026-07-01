import request from '@/utils/request'

// 查询APP公告列表
export function listNotice(query) {
  return request({
    url: '/batch/notice/list',
    method: 'get',
    params: query
  })
}

// 查询APP公告详细
export function getNotice(noticeId) {
  return request({
    url: '/batch/notice/' + noticeId,
    method: 'get'
  })
}

// 预览APP公告
export function previewNotice(noticeId) {
  return request({
    url: '/batch/notice/preview/' + noticeId,
    method: 'get'
  })
}

// 新增APP公告
export function addNotice(data) {
  return request({
    url: '/batch/notice',
    method: 'post',
    data: data
  })
}

// 修改APP公告
export function updateNotice(data) {
  return request({
    url: '/batch/notice',
    method: 'put',
    data: data
  })
}

// 删除APP公告
export function delNotice(noticeId) {
  return request({
    url: '/batch/notice/' + noticeId,
    method: 'delete'
  })
}

// 发布APP公告
export function publishNotice(noticeId) {
  return request({
    url: '/batch/notice/publish/' + noticeId,
    method: 'put'
  })
}

// 下架APP公告
export function unpublishNotice(noticeId) {
  return request({
    url: '/batch/notice/unpublish/' + noticeId,
    method: 'put'
  })
}

// 导出APP公告
export function exportNotice(query) {
  return request({
    url: '/batch/notice/export',
    method: 'get',
    params: query
  })
}

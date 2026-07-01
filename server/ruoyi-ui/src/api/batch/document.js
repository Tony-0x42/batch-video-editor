import request from '@/utils/request'

// 查询文档列表
export function listDocument(query) {
  return request({
    url: '/batch/document/list',
    method: 'get',
    params: query
  })
}

// 查询文档详细
export function getDocument(documentId) {
  return request({
    url: '/batch/document/' + documentId,
    method: 'get'
  })
}

// 新增文档
export function addDocument(data) {
  return request({
    url: '/batch/document',
    method: 'post',
    data: data
  })
}

// 修改文档
export function updateDocument(data) {
  return request({
    url: '/batch/document',
    method: 'put',
    data: data
  })
}

// 修改文档状态
export function changeDocumentStatus(data) {
  return request({
    url: '/batch/document/changeStatus',
    method: 'put',
    data: data
  })
}

// 删除文档
export function delDocument(documentId) {
  return request({
    url: '/batch/document/' + documentId,
    method: 'delete'
  })
}

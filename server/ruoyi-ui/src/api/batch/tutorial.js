import request from '@/utils/request'

// 查询教程列表
export function listTutorial(query) {
  return request({
    url: '/batch/tutorial/list',
    method: 'get',
    params: query
  })
}

// 查询教程详细
export function getTutorial(tutorialId) {
  return request({
    url: '/batch/tutorial/' + tutorialId,
    method: 'get'
  })
}

// 新增教程
export function addTutorial(data) {
  return request({
    url: '/batch/tutorial',
    method: 'post',
    data: data
  })
}

// 修改教程
export function updateTutorial(data) {
  return request({
    url: '/batch/tutorial',
    method: 'put',
    data: data
  })
}

// 修改教程状态
export function changeTutorialStatus(data) {
  return request({
    url: '/batch/tutorial/changeStatus',
    method: 'put',
    data: data
  })
}

// 删除教程
export function delTutorial(tutorialId) {
  return request({
    url: '/batch/tutorial/' + tutorialId,
    method: 'delete'
  })
}

// 导出教程
export function exportTutorial(query) {
  return request({
    url: '/batch/tutorial/export',
    method: 'post',
    data: query,
    responseType: 'blob'
  })
}

// 查询分类列表
export function listCategory(query) {
  return request({
    url: '/batch/tutorial/category/list',
    method: 'get',
    params: query
  })
}

// 查询所有有效分类
export function listCategoryAll() {
  return request({
    url: '/batch/tutorial/category/all',
    method: 'get'
  })
}

// 查询分类详细
export function getCategory(categoryId) {
  return request({
    url: '/batch/tutorial/category/' + categoryId,
    method: 'get'
  })
}

// 新增分类
export function addCategory(data) {
  return request({
    url: '/batch/tutorial/category',
    method: 'post',
    data: data
  })
}

// 修改分类
export function updateCategory(data) {
  return request({
    url: '/batch/tutorial/category',
    method: 'put',
    data: data
  })
}

// 删除分类
export function delCategory(categoryId) {
  return request({
    url: '/batch/tutorial/category/' + categoryId,
    method: 'delete'
  })
}

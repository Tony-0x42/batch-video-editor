import request from '@/utils/request'

// ---------------- 首页轮播图 ----------------
export function listBanner(query) {
  return request({
    url: '/batch/home/banner/list',
    method: 'get',
    params: query
  })
}

export function getBanner(bannerId) {
  return request({
    url: '/batch/home/banner/' + bannerId,
    method: 'get'
  })
}

export function addBanner(data) {
  return request({
    url: '/batch/home/banner',
    method: 'post',
    data: data
  })
}

export function updateBanner(data) {
  return request({
    url: '/batch/home/banner',
    method: 'put',
    data: data
  })
}

export function delBanner(bannerId) {
  return request({
    url: '/batch/home/banner/' + bannerId,
    method: 'delete'
  })
}

export function changeBannerStatus(data) {
  return request({
    url: '/batch/home/banner/changeStatus',
    method: 'put',
    data: data
  })
}

// ---------------- 首页喜报数据 ----------------
export function listNews(query) {
  return request({
    url: '/batch/home/news/list',
    method: 'get',
    params: query
  })
}

export function getNews(newsId) {
  return request({
    url: '/batch/home/news/' + newsId,
    method: 'get'
  })
}

export function addNews(data) {
  return request({
    url: '/batch/home/news',
    method: 'post',
    data: data
  })
}

export function updateNews(data) {
  return request({
    url: '/batch/home/news',
    method: 'put',
    data: data
  })
}

export function delNews(newsId) {
  return request({
    url: '/batch/home/news/' + newsId,
    method: 'delete'
  })
}

export function changeNewsStatus(data) {
  return request({
    url: '/batch/home/news/changeStatus',
    method: 'put',
    data: data
  })
}

// ---------------- 首页功能入口 ----------------
export function listEntry(query) {
  return request({
    url: '/batch/home/entry/list',
    method: 'get',
    params: query
  })
}

export function getEntry(entryId) {
  return request({
    url: '/batch/home/entry/' + entryId,
    method: 'get'
  })
}

export function addEntry(data) {
  return request({
    url: '/batch/home/entry',
    method: 'post',
    data: data
  })
}

export function updateEntry(data) {
  return request({
    url: '/batch/home/entry',
    method: 'put',
    data: data
  })
}

export function delEntry(entryId) {
  return request({
    url: '/batch/home/entry/' + entryId,
    method: 'delete'
  })
}

export function changeEntryStatus(data) {
  return request({
    url: '/batch/home/entry/changeStatus',
    method: 'put',
    data: data
  })
}

// ---------------- 首页教程入口 ----------------
export function listTutorialEntry(query) {
  return request({
    url: '/batch/home/tutorialEntry/list',
    method: 'get',
    params: query
  })
}

export function getTutorialEntry(entryId) {
  return request({
    url: '/batch/home/tutorialEntry/' + entryId,
    method: 'get'
  })
}

export function addTutorialEntry(data) {
  return request({
    url: '/batch/home/tutorialEntry',
    method: 'post',
    data: data
  })
}

export function updateTutorialEntry(data) {
  return request({
    url: '/batch/home/tutorialEntry',
    method: 'put',
    data: data
  })
}

export function delTutorialEntry(entryId) {
  return request({
    url: '/batch/home/tutorialEntry/' + entryId,
    method: 'delete'
  })
}

export function changeTutorialEntryStatus(data) {
  return request({
    url: '/batch/home/tutorialEntry/changeStatus',
    method: 'put',
    data: data
  })
}

export function listDocumentOption() {
  return request({
    url: '/batch/home/tutorialEntry/documentList',
    method: 'get'
  })
}

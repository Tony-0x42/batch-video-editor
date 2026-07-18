import request from '@/utils/request'

// 查询AI视频生成记录列表
export function listAiVideoLog(query) {
  return request({
    url: '/batch/ai/video/log/list',
    method: 'get',
    params: query
  })
}

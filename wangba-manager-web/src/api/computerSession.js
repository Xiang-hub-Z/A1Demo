import request from '@/utils/request'

// 开始上机
export function startSession(data) {
  return request({
    url: '/computer-session/start',
    method: 'post',
    data
  })
}

// 结束上机
export function endSession(data) {
  return request({
    url: '/computer-session/end',
    method: 'post',
    data
  })
}

// 获取用户当前上机状态
export function getCurrentSession(userId) {
  return request({
    url: `/computer-session/user/${userId}/current`,
    method: 'get'
  })
}

// 获取会话列表 - 更新为支持参数
export function getSessionList(params) {
  return request({
    url: '/computer-session/list',
    method: 'get',
    params
  })
}

// 根据ID获取会话详情
export function getSessionById(id) {
  return request({
    url: `/computer-session/${id}`,
    method: 'get'
  })
}
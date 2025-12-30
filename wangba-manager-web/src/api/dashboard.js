import request from '@/utils/request'

// 获取仪表盘统计数据
export function getDashboardStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

// 获取电脑状态统计
export function getComputerStatus() {
  return request({
    url: '/dashboard/computer-status',
    method: 'get'
  })
}

// 获取最近会话
export function getRecentSessions() {
  return request({
    url: '/dashboard/recent-sessions',
    method: 'get'
  })
}
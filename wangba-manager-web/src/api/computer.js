import request from '@/utils/request'

// 获取所有计算机
export function getComputerList() {
  return request({
    url: '/computer/list',
    method: 'get'
  })
}

// 根据区域获取计算机
export function getComputersByArea(areaId) {
  return request({
    url: `/computer/list/by-area/${areaId}`,
    method: 'get'
  })
}

// 添加计算机
export function addComputer(data) {
  return request({
    url: '/computer/add',
    method: 'post',
    data
  })
}

// 更新计算机信息
export function updateComputer(data) {
  return request({
    url: '/computer/update',
    method: 'put',
    data
  })
}

// 删除计算机
export function deleteComputer(id) {
  return request({
    url: `/computer/delete/${id}`,
    method: 'delete'
  })
}

// 获取单个计算机信息
export function getComputerById(id) {
  return request({
    url: `/computer/${id}`,
    method: 'get'
  })
}
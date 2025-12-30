import request from '@/utils/request'

// 获取所有区域
export function getAreaList() {
  return request({
    url: '/area/list',
    method: 'get'
  })
}

// 添加区域
export function addArea(data) {
  return request({
    url: '/area/add',
    method: 'post',
    data
  })
}

// 更新区域信息
export function updateArea(data) {
  return request({
    url: '/area/update',
    method: 'put',
    data
  })
}

// 删除区域
export function deleteArea(id) {
  return request({
    url: `/area/delete/${id}`,
    method: 'delete'
  })
}
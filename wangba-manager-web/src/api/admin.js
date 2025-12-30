import request from '@/utils/request'

// 管理员登录
export function adminLogin(data) {
  return request({
    url: '/admin/login',
    method: 'post',
    data
  })
}
// 获取用户列表
export function getAdminList() {
  return request({
    url: '/admin/list',
    method: 'get'
  })
}
// 添加用户
export function addAdmin(data) {
  return request({
    url: '/admin/add',
    method: 'post',
    data
  })
}
// 更新用户信息
export function updateAdminInfo(data) {
  return request({
    url: '/admin/update',
    method: 'put',
    data
  })
}
// 删除用户
export function deleteAdmin(id) {
  return request({
    url: `/admin/delete/${id}`,
    method: 'delete'
  })
}
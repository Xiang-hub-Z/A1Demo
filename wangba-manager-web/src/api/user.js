import request from '@/utils/request'

// 用户登录
export function userLogin(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 用户注册
export function userRegister(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}
// 获取用户列表
export function getUserList() {
  return request({
    url: '/user/list',
    method: 'get'
  })
}

// 添加用户
export function addUser(data) {
  return request({
    url: '/user/add',
    method: 'post',
    data
  })
}
// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}
// 删除用户
export function deleteUser(id) {
  return request({
    url: `/user/delete/${id}`,
    method: 'delete'
  })
}
// 用户充值
export function userRecharge(data) {
  return request({
    url: '/user/recharge',
    method: 'post',
    data
  })
}
// 检查用户余额
export function checkUserBalance(userId) {
  return request({
    url: `/user/${userId}`,
    method: 'get'
  })
}

// 获取用户消费记录（可选）
export function getUserConsumptionRecords(userId) {
  return request({
    url: `/user/consumption/records/${userId}`,
    method: 'get'
  })
}
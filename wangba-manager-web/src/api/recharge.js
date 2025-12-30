// src/api/recharge.js
import request from '@/utils/request'

// 获取所有充值记录
export function getRechargeRecords() {
  return request({
    url: '/recharge-record/list',
    method: 'get'
  })
}

// 根据ID查询充值记录
export function getRechargeRecordById(id) {
  return request({
    url: `/recharge-record/${id}`,
    method: 'get'
  })
}

// 添加充值记录
export function addRechargeRecord(data) {
  return request({
    url: '/recharge-record/add',
    method: 'post',
    data
  })
}

// 更新充值记录信息
export function updateRechargeRecord(data) {
  return request({
    url: '/recharge-record/update',
    method: 'put',
    data
  })
}

// 删除充值记录
export function deleteRechargeRecord(id) {
  return request({
    url: `/recharge-record/delete/${id}`,
    method: 'delete'
  })
}
<template>
  <div class="order-list">
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多订单了"
      @load="onLoad"
    >
      <van-cell
        v-for="order in orders"
        :key="order.id"
        :title="`订单 #${order.id}`"
        :value="`¥${order.amount}`"
        :label="order.createTime"
      >
        <template #extra>
          <van-tag :type="getStatusType(order.status)">
            {{ getStatusText(order.status) }}
          </van-tag>
        </template>
      </van-cell>
    </van-list>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  status: String
})

const loading = ref(false)
const finished = ref(false)
const orders = ref([])

const mockOrders = [
  { id: '20241103001', amount: '50.00', status: 'completed', createTime: '2024-11-03 10:00:00' },
  { id: '20241103002', amount: '30.00', status: 'processing', createTime: '2024-11-03 09:30:00' },
  { id: '20241102001', amount: '100.00', status: 'completed', createTime: '2024-11-02 15:20:00' }
]

const getStatusType = (status) => {
  const types = {
    'processing': 'warning',
    'completed': 'success',
    'cancelled': 'danger'
  }
  return types[status] || 'default'
}

const getStatusText = (status) => {
  const texts = {
    'processing': '进行中',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return texts[status] || '未知'
}

const onLoad = () => {
  setTimeout(() => {
    if (props.status) {
      orders.value = mockOrders.filter(order => order.status === props.status)
    } else {
      orders.value = mockOrders
    }
    loading.value = false
    finished.value = true
  }, 1000)
}

onMounted(() => {
  onLoad()
})
</script>

<style scoped>
.order-list {
  padding: 10px;
}
</style>
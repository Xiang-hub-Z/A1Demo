<template>
  <div class="recharge-page">
    <van-cell-group title="选择充值金额">
      <van-radio-group v-model="selectedAmount">
        <van-cell
          v-for="amount in amountOptions"
          :key="amount"
          :title="`¥${amount}`"
          clickable
          @click="selectedAmount = amount"
        >
          <template #right-icon>
            <van-radio :name="amount" />
          </template>
        </van-cell>
      </van-radio-group>
    </van-cell-group>

    <van-cell-group title="支付方式" style="margin-top: 20px;">
      <van-radio-group v-model="selectedPayment">
        <van-cell
          v-for="payment in paymentOptions"
          :key="payment.value"
          :title="payment.label"
          clickable
          @click="selectedPayment = payment.value"
        >
          <template #right-icon>
            <van-radio :name="payment.value" />
          </template>
        </van-cell>
      </van-radio-group>
    </van-cell-group>

    <!-- 模拟支付弹窗 -->
    <van-dialog
      v-model:show="showPayDialog"
      title="模拟支付"
      show-cancel-button
      :show-confirm-button="false"
    >
      <div class="pay-dialog-content">
        <div class="pay-info">
          <p>充值金额: <span class="amount">¥{{ selectedAmount }}</span></p>
          <p>支付方式: {{ getPaymentLabel(selectedPayment) }}</p>
          <p class="order-id">订单号: {{ currentOrderId }}</p>
        </div>
        
        <div class="simulate-pay-buttons">
          <van-button 
            type="success" 
            size="large" 
            @click="handleSimulatePaySuccess"
            :loading="payLoading"
          >
            模拟支付成功
          </van-button>
          <van-button 
            type="danger" 
            size="large" 
            @click="handleSimulatePayFail"
            style="margin-top: 10px;"
          >
            模拟支付失败
          </van-button>
        </div>
      </div>
    </van-dialog>

    <div class="action-bar">
      <van-button
        type="primary"
        block
        size="large"
        @click="handleRecharge"
        :loading="loading"
      >
        立即充值 ¥{{ selectedAmount }}
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast, showDialog, showNotify } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const payLoading = ref(false)
const showPayDialog = ref(false)
const selectedAmount = ref(50)
const selectedPayment = ref('alipay')
const currentOrderId = ref('')

const amountOptions = [10, 20, 50, 100, 200, 500]
const paymentOptions = [
  { label: '支付宝', value: 'alipay' },
  { label: '微信支付', value: 'wechat' },
  { label: '银行卡', value: 'bank' }
]

// 生成随机订单号
const generateOrderId = () => {
  const timestamp = new Date().getTime()
  const random = Math.floor(Math.random() * 10000)
  return `RC${timestamp}${random}`
}

const getPaymentLabel = (paymentValue) => {
  const payment = paymentOptions.find(p => p.value === paymentValue)
  return payment ? payment.label : ''
}

const handleRecharge = async () => {
  if (!userStore.token) {
    showToast('请先登录')
    router.push('/login')
    return
  }

  try {
    loading.value = true
    
    // 生成订单号
    currentOrderId.value = generateOrderId()
    
    // 显示模拟支付弹窗
    showPayDialog.value = true
    
  } catch (error) {
    console.error('创建订单失败:', error)
    showToast('创建订单失败，请重试')
  } finally {
    loading.value = false
  }
}

// 模拟支付成功
const handleSimulatePaySuccess = async () => {
  payLoading.value = true
  
  try {
    // 模拟支付处理时间
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 调用后端API更新用户余额 - 添加 paymentMethod 参数
    const response = await updateUserBalance({
      userId: userStore.userInfo.id,
      amount: selectedAmount.value,
      orderId: currentOrderId.value,
      paymentMethod: selectedPayment.value  // 添加这一行
    })
    
    if (response.code === 200) {
      // 更新本地用户信息中的余额
      userStore.userInfo.balance = (parseFloat(userStore.userInfo.balance) + parseFloat(selectedAmount.value)).toFixed(2)
      
      showPayDialog.value = false
      showNotify({
        type: 'success',
        message: `充值成功！已到账 ¥${selectedAmount.value}`,
        duration: 3000
      })
      
      // 可以在这里添加跳转到其他页面或刷新数据的逻辑
    } else {
      showToast(response.message || '充值失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    showToast('支付失败，请重试')
  } finally {
    payLoading.value = false
  }
}

// 模拟支付失败
const handleSimulatePayFail = () => {
  showPayDialog.value = false
  showToast('支付失败，请重试')
}

// 模拟充值
const updateUserBalance = async (data) => {
  try {
    const response = await fetch('/api/user/recharge', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userStore.token}` // 如果有token的话
      },
      body: JSON.stringify(data)
    });
    
    return await response.json();
  } catch (error) {
    console.error('充值请求失败:', error);
    return {
      code: 500,
      message: '网络请求失败'
    };
  }
}
</script>

<style scoped lang="scss">
.recharge-page {
  padding-bottom: 80px;
}

.action-bar {
  position: fixed;
  bottom: 40px;
  left: 0;
  right: 0;
  padding: 16px;
  background: white;
  border-top: 1px solid #ebedf0;
}

.pay-dialog-content {
  padding: 20px;
  text-align: center;
  
  .pay-info {
    margin-bottom: 20px;
    
    p {
      margin: 8px 0;
      color: #333;
      
      .amount {
        color: #ee0a24;
        font-weight: bold;
        font-size: 18px;
      }
      
      &.order-id {
        font-size: 12px;
        color: #666;
      }
    }
  }
  
  .simulate-pay-buttons {
    .van-button {
      width: 100%;
    }
  }
}
</style>
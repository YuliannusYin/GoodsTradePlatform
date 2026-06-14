/**
 * @file CheckoutView.vue
 * @description 结算页面主视图，使用步骤条管理三步结算流程（商品清单 -> 地址与配送 -> 确认下单）
 * @input 无
 * @output 无
 */
<template>
  <div class="max-w-4xl mx-auto px-4 py-6">
    <!-- 下单成功页面 -->
    <OrderSuccess
      v-if="orderPlaced"
      :order-id="placedOrderId"
      :total-price="placedTotalPrice"
      :remaining-balance="placedRemainingBalance"
    />

    <!-- 结算流程 -->
    <template v-else>
      <!-- 步骤指示器 -->
      <CheckoutStepper
        :steps="stepLabels"
        :current-step="currentStep"
        @update:current-step="handleStepChange"
      />

      <!-- 加载中状态 -->
      <div v-if="isLoading" class="flex items-center justify-center py-20">
        <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-500"></div>
        <span class="ml-3 text-gray-500">加载中...</span>
      </div>

      <!-- 错误提示 -->
      <div v-else-if="loadError" class="text-center py-20">
        <p class="text-red-500 mb-4">{{ loadError }}</p>
        <button
          class="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors"
          @click="loadOngoingOrder"
        >
          重新加载
        </button>
      </div>

      <!-- 空购物车提示 -->
      <div v-else-if="orderItems.length === 0" class="text-center py-20">
        <p class="text-gray-500 mb-4">购物车为空，请先添加商品</p>
        <router-link
          to="/shop"
          class="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors inline-block"
        >
          去购物
        </router-link>
      </div>

      <!-- 步骤内容 -->
      <template v-else>
        <!-- 步骤1：商品清单 -->
        <div v-show="currentStep === 0">
          <CheckoutItems :items="orderItems" />
          <div class="flex justify-end mt-6">
            <button
              class="px-6 py-2.5 bg-blue-500 hover:bg-blue-600 text-white rounded-md text-sm font-medium transition-colors duration-200"
              @click="nextStep"
            >
              下一步：填写地址
            </button>
          </div>
        </div>

        <!-- 步骤2：地址 + 配送 + 余额 -->
        <div v-show="currentStep === 1" class="space-y-6">
          <CheckoutAddress ref="addressRef" v-model="address" />
          <CheckoutDelivery v-model="deliveryMethod" />
          <CheckoutBalance :total-price="totalPrice" />
          <div class="flex justify-between mt-6">
            <button
              class="px-6 py-2.5 border border-gray-300 hover:border-gray-400 text-gray-700 rounded-md text-sm font-medium transition-colors duration-200"
              @click="prevStep"
            >
              上一步
            </button>
            <button
              class="px-6 py-2.5 bg-blue-500 hover:bg-blue-600 text-white rounded-md text-sm font-medium transition-colors duration-200"
              @click="goToConfirm"
            >
              下一步：确认订单
            </button>
          </div>
        </div>

        <!-- 步骤3：确认订单 -->
        <div v-show="currentStep === 2">
          <CheckoutConfirm
            :items="orderItems"
            :address="address"
            :delivery-method="deliveryMethod"
            :total-price="totalPrice"
            :balance="currentBalance"
            @confirm="handleConfirmOrder"
          />
          <div class="flex justify-start mt-6">
            <button
              class="px-6 py-2.5 border border-gray-300 hover:border-gray-400 text-gray-700 rounded-md text-sm font-medium transition-colors duration-200"
              @click="prevStep"
            >
              上一步
            </button>
          </div>
        </div>
      </template>

      <!-- 确认下单弹窗 -->
      <ConfirmDialogue
        v-if="showConfirmDialog"
        header="确认下单"
        :text="`确认使用账户余额支付 ¥${totalPrice.toFixed(2)} 下单吗？`"
        :on-confirm="executePlaceOrder"
        :on-cancel="closeConfirmDialog"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 结算页面主视图
 * 职责：管理三步结算流程的状态流转，协调各子组件交互
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/network/orderStore'
import { useAccountStore } from '@/stores/network/accountStore'
import { useShoppingCartStore } from '@/stores/shoppingCartStore'
import type { OrderItem } from '@/types/order'
import type { AddressInfo } from '@/types/order'
import { DELIVERY_METHODS } from '@/types/order'

import CheckoutStepper from '@/components/checkout/CheckoutStepper.vue'
import CheckoutItems from '@/components/checkout/CheckoutItems.vue'
import CheckoutAddress from '@/components/checkout/CheckoutAddress.vue'
import CheckoutDelivery from '@/components/checkout/CheckoutDelivery.vue'
import CheckoutBalance from '@/components/checkout/CheckoutBalance.vue'
import CheckoutConfirm from '@/components/checkout/CheckoutConfirm.vue'
import OrderSuccess from '@/components/checkout/OrderSuccess.vue'
import ConfirmDialogue from '@/components/ConfirmDialogue.vue'

const router = useRouter()
const orderStore = useOrderStore()
const accountStore = useAccountStore()
const shoppingCartStore = useShoppingCartStore()

// === 步骤管理 ===

/** 步骤名称数组 */
const stepLabels = ['商品清单', '地址与配送', '确认订单']
/** 当前步骤索引 */
const currentStep = ref(0)

// === 数据状态 ===

/** 订单商品列表（从后端获取） */
const orderItems = ref<OrderItem[]>([])
/** 是否正在加载 */
const isLoading = ref(true)
/** 加载错误信息 */
const loadError = ref('')

/** 收货地址表单数据 */
const address = ref<AddressInfo>({
  receiverName: '',
  receiverPhone: '',
  region: '',
  detailAddress: ''
})

/** 选中的配送方式，默认为普通快递 */
const deliveryMethod = ref<string>('STANDARD_DELIVERY')

/** 是否显示确认下单弹窗 */
const showConfirmDialog = ref(false)

/** 是否正在提交订单 */
const isSubmitting = ref(false)

// === 下单成功状态 ===

/** 是否已下单成功 */
const orderPlaced = ref(false)
/** 已下单的订单ID */
const placedOrderId = ref('')
/** 已下单的支付金额 */
const placedTotalPrice = ref(0)
/** 已下单后的剩余余额 */
const placedRemainingBalance = ref(0)

// === 地址组件引用 ===

const addressRef = ref<InstanceType<typeof CheckoutAddress> | null>(null)

// === 计算属性 ===

/** 订单总价 */
const totalPrice = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.price, 0)
})

/** 当前账户余额 */
const currentBalance = computed(() => {
  return accountStore.balance ?? 0
})

// === 方法 ===

/**
 * 加载进行中的订单数据（从后端计算商品价格和可用性）
 */
async function loadOngoingOrder() {
  isLoading.value = true
  loadError.value = ''
  try {
    const response = await orderStore.getOngoingOrder() as any
    if (response) {
      orderItems.value = response.items || []
    }
  } catch (e: any) {
    loadError.value = e?.message || '加载订单信息失败，请重试'
  } finally {
    isLoading.value = false
  }
}

/**
 * 切换到下一步
 */
function nextStep() {
  if (currentStep.value < stepLabels.length - 1) {
    currentStep.value++
  }
}

/**
 * 切换到上一步
 */
function prevStep() {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

/**
 * 处理步骤指示器的步骤切换（仅允许回退到已完成步骤）
 * @param {number} step - 目标步骤索引
 */
function handleStepChange(step: number) {
  // 仅允许回退到已完成的步骤
  if (step < currentStep.value) {
    currentStep.value = step
  }
}

/**
 * 从步骤2进入步骤3前，验证地址表单
 */
function goToConfirm() {
  // 验证地址表单
  if (addressRef.value) {
    const isValid = addressRef.value.validateAll()
    if (!isValid) {
      return
    }
  }
  nextStep()
}

/**
 * 处理确认下单按钮点击，弹出确认弹窗
 */
function handleConfirmOrder() {
  showConfirmDialog.value = true
}

/**
 * 关闭确认弹窗
 */
function closeConfirmDialog() {
  showConfirmDialog.value = false
}

/**
 * 执行下单操作
 */
async function executePlaceOrder() {
  // 防止重复提交
  if (isSubmitting.value) return
  isSubmitting.value = true

  try {
    const response = await orderStore.placeOrder(
      address.value.receiverName,
      address.value.receiverPhone,
      address.value.region,
      address.value.detailAddress,
      deliveryMethod.value
    ) as any

    if (response) {
      // 下单成功，更新成功状态
      orderPlaced.value = true
      placedOrderId.value = response.id || response.orderId || ''
      placedTotalPrice.value = totalPrice.value
      // 刷新账户余额并计算剩余余额
      await accountStore.fetchUserDetails()
      placedRemainingBalance.value = accountStore.balance ?? 0
    }
  } catch (e: any) {
    // 下单失败提示
    alert(e?.message || '下单失败，请重试')
  } finally {
    isSubmitting.value = false
    showConfirmDialog.value = false
  }
}

// === 生命周期 ===

onMounted(() => {
  // 检查购物车是否为空
  if (shoppingCartStore.totalQuantity === 0) {
    isLoading.value = false
    return
  }
  // 加载进行中的订单数据
  loadOngoingOrder()
})
</script>

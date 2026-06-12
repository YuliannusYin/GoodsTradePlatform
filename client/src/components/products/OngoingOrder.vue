<template>
  <div class="flex flex-col justify-center items-center w-full">
    <div v-if="!isConfirmedOrder" class="w-full flex flex-col items-center justify-center">
      <h2 class="text-l font-bold text-center sm:text-left uppercase w-full sm:min-w-max sm:w-[70%] py-4">查看购物车</h2>
      <div class="p-4 bg-white rounded shadow w-full sm:min-w-max sm:w-[70%] mb-4">
        <ul>
          <div v-if="ongoingOrder !== null && !isConfirmedOrder">
            <div v-for="item in ongoingOrder?.items" :key="item.product.id">
              <li class="mb-1 py-2 border-b w-full flex justify-between items-center">
                <div class="flex items-center">
                  <img :src="item.product.imageUrls?.[0]" alt="商品图片"
                    class="max-w-[4rem] sm:max-w-[5rem] inline-block mr-5" />
                  <div>
                    <span class="font-bold">{{ item.product.name }}</span>
                    <br />
                    - {{ item.product.price }}:-
                  </div>
                </div>
                <div class="flex flex-col items-center sm:flex-row px-2 sm:px-4">
                  <div class="flex items-center border rounded-md">
                    <div class="px-3 py-3 text-left flex text-l">
                      <p class="text-gray-700">{{ item.amount }}</p>
                    </div>
                    <div class="flex flex-col">
                      <button @click="() => addProduct(item.product.id)" :disabled="item.amount >= item.product.quantity"
                        class="text-center border-l border-b font-bold text-2xl px-2"
                        :class="{ 'bg-gray-100 text-gray-400 cursor-not-allowed': item.amount >= item.product.quantity }">+</button>
                      <button @click="() => removeProduct(item.product.id)"
                        class="text-center border-l font-bold text-2xl px-2">-</button>
                    </div>
                  </div>
                  <div class="flex justify-reverse sm:justify-center mt-2 w-full sm:ml-9 max-w-[2rem]">
                    <p class="font-semibold max-w-[4rem]">{{ item.price.toFixed(2) }}:-</p>
                  </div>
                </div>
              </li>
            </div>
          </div>
        </ul>
        <div v-if="ongoingOrder !== null && !isConfirmedOrder"
          class="w-full font-bold flex justify-between items-center mt-4 mb-1 px-2">
          <p class="ml-[0.47rem]">总价：</p>
          <p>{{ ongoingOrder?.totalPrice }}:-</p>
        </div>
      </div>

      <div v-if="accountStore.isAuthenticated" class="sm:min-w-max w-full space-y-4 text-l font-bold sm:w-[70%] pb-4">
        <h2 class="text-l font-bold uppercase text-center sm:text-left">2. 填写配送信息</h2>
        <div class="p-4 bg-white rounded shadow pb-4 w-full flex flex-col justify-center items-center">
          <iframe id="googleMap" class="w-full h-[20rem] rounded-md mb-4"
            :src="`https://maps.google.com/maps?q=${deliveryCoordinates.latitude},${deliveryCoordinates.longitude}&hl=en&z=14&amp;output=embed`">
          </iframe>
          <button @click="getGeoLocation" class="bg-sky-500 hover:bg-sky-600 px-6 py-2 text-l text-white rounded">获取我的位置</button>
        </div>

        <div class="flex justify-between">
          <div class="p-4 bg-white rounded shadow w-full mr-4">
            <h2 class="text-l font-bold mb-6 text-center sm:text-left uppercase whitespace-nowrap">3. 选择配送方式</h2>
            <div class="mb-4">
              <label for="email" class="block text-gray-700 text-sm font-bold mb-2">配送方式</label>
              <select v-model="selectedDeliveryMethod" id="selectedDeliveryMethod"
                class="px-3 py-2 border rounded border-gray-300 focus:outline-none">
                <option v-for="deliveryMethod in deliveryMethods" :key="deliveryMethod" :value="deliveryMethod">
                  {{ deliveryMethod === 'DRONE_DELIVERY' ? '无人机配送' : deliveryMethod }}</option>
              </select>
            </div>
          </div>

          <div class="p-4 bg-white rounded shadow w-full">
            <h2 class="text-l font-bold mb-6 text-center sm:text-left uppercase whitespace-nowrap">4. 选择支付方式</h2>
            <div class="mb-4">
              <label for="email" class="block text-gray-700 text-sm font-bold mb-2">支付方式</label>
              <select v-model="selectedPaymentMethod" id="selectedDeliveryMethod"
                class="px-3 py-2 border rounded border-gray-300 focus:outline-none">
                <option v-for="paymentMethod in paymentMethods" :key="paymentMethod" :value="paymentMethod">
                  {{ paymentMethod === "PAY_ON_DELIVERY" ? "货到付款" : paymentMethod }}</option>
              </select>
            </div>
          </div>
        </div>

        <div class="p-4 bg-white rounded shadow">
          <h2 class="text-l font-bold mb-3 text-center sm:text-left uppercase">{{ accountStore.isAuthenticated ? '5' : '2' }}. 确认订单</h2>
          <div class="w-full flex justify-center items-center">
            <button @click="placeOrder"
              :disabled="deliveryCoordinates.latitude == '' || deliveryCoordinates.longitude == ''"
              class="bg-green-500 hover:bg-green-600 px-6 py-2 text-l text-white rounded disabled:bg-gray-500 disabled:cursor-not-allowed">确认下单</button>
          </div>
        </div>
      </div>
      <div v-if="!accountStore.isAuthenticated && !isConfirmedOrder">
        <h2 class="text-l font-bold my-6 text-center sm:text-left uppercase">2. 登录以完成下单</h2>
        <div class="flex flex-col justify-center">
          <router-link to="/login"
            class="w-max-min bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none transition duration-300 text-center">
            登录
          </router-link>
          <div class="flex justify-center items-center mt-4">
            <p class="mr-1 text-sm">新用户？</p>
            <router-link to="/signup" class="text-blue-700 hover:text-blue-400 text-sm">点击注册</router-link>
          </div>
        </div>
      </div>
    </div>
    <div v-if="isConfirmedOrder" class="flex flex-col items-center justify-center">
      <h2 class="text-l font-bold my-6 text-center sm:text-left uppercase">感谢您的购买！</h2>
      <RouterLink to="/shop">
        <button @click="clearConfirmedOrder"
          class="bg-blue-500 hover:bg-blue-600 px-6 py-2 text-l text-white font-semibold rounded">继续购物</button>
      </RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file OngoingOrder.vue
 * @description 购物车/结算页面组件，展示购物车商品、配送信息填写、配送方式选择和下单确认
 */
import { computed, onMounted, reactive, ref, watch, type Ref } from 'vue'
import { useOrderStore } from '@/stores/network/orderStore'
import { useShoppingCartStore } from '@/stores/shoppingCartStore'
import { useAccountStore } from '@/stores/network/accountStore'
import type { OngoingOrder } from '@/types/order'

const orderStore = useOrderStore()
const shoppingCartStore = useShoppingCartStore()
const accountStore = useAccountStore()
// 当前进行中的订单数据
const ongoingOrder = ref<OngoingOrder | null>(null)
// 可用的配送方式列表
const deliveryMethods = ref<string[]>([])
// 可用的支付方式列表
const paymentMethods = ref<string[]>([])

// 配送坐标（经纬度）
const deliveryCoordinates = reactive({
  latitude: '',
  longitude: ''
})

// 当前选中的配送方式
const selectedDeliveryMethod = ref('')
// 当前选中的支付方式
const selectedPaymentMethod = ref('')
// 订单是否已确认下单
const isConfirmedOrder = ref(false)

// 组件挂载时初始化订单和配送/支付方式
onMounted(() => {
  updateOngoingOrder()
  getDeliveryMethods()
  getPaymentMethods()
})

// 监听购物车商品数量变化，更新订单数据
watch(
  () => shoppingCartStore.productAmount,
  (newItemsState: number) => {
    if (newItemsState) {
      updateOngoingOrder()
    }
  }
)

// 获取可用的配送方式列表
async function getDeliveryMethods() {
  deliveryMethods.value = await orderStore.getAvailableDeliveryMethods()
}

// 配送方式加载后默认选中第一个
watch(deliveryMethods as Ref, (newMethods: string[]) => {
  if (newMethods.length > 0) {
    selectedDeliveryMethod.value = newMethods[0]
  }
})

// 获取可用的支付方式列表
async function getPaymentMethods() {
  paymentMethods.value = await orderStore.getAvailablePaymentMethods()
}

// 支付方式加载后默认选中第一个
watch(paymentMethods as Ref, (newMethods: string[]) => {
  if (newMethods.length > 0) {
    selectedPaymentMethod.value = newMethods[0]
  }
})

// 更新当前进行中的订单数据
async function updateOngoingOrder() {
  ongoingOrder.value = await orderStore.getOngoingOrder()
}

// 向购物车添加商品
function addProduct(productId: string) {
  shoppingCartStore.addProductId(productId)
}

// 从购物车移除商品
function removeProduct(productId: string) {
  shoppingCartStore.removeProductId(productId)
}

/**
 * 确认下单操作，提交订单信息
 */
async function placeOrder() {
  const address = `${deliveryCoordinates.latitude},${deliveryCoordinates.longitude}`
  await orderStore.placeOrder(address, selectedDeliveryMethod.value, selectedPaymentMethod.value)
  ongoingOrder.value = null
  isConfirmedOrder.value = true
}

/**
 * 获取用户地理位置坐标
 */
function getGeoLocation() {
  if (navigator.geolocation) {
    navigator.geolocation.watchPosition(showPosition)
  }
}

/**
 * 地理位置获取成功回调，更新配送坐标
 * @param {any} position - 浏览器返回的地理位置对象
 */
function showPosition(position: any) {
  deliveryCoordinates.latitude = position.coords.latitude
  deliveryCoordinates.longitude = position.coords.longitude
}

// 清除已确认订单状态，返回购物状态
function clearConfirmedOrder() {
  isConfirmedOrder.value = false
}
</script>

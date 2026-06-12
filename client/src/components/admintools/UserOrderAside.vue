<template>
  <div v-if="props.order"
    class="w-screen sm:w-max sm:min-w-[30rem] bg-white border-l fixed h-screen top-[6.45rem] sm:top-[5.78rem] right-0 p-5 shadow-md">
    <ConfirmDialogue :isPasswordRequired="false" header="确认发货"
      text="确定要发送此订单吗？"
      v-if="props.order?.status === 'PENDING' && isConfirmationVisible" :onConfirm="sendOrder"
      :onCancel="closeConfirmation" />
    <ConfirmDialogue :isPasswordRequired="false" header="确认修改预计送达时间"
      text="确定要修改预计送达时间吗？"
      v-if="props.order?.status === 'SHIPPED' && isConfirmationVisible" :onConfirm="changeExpectedDelivery"
      :onCancel="closeConfirmation" />

    <div class="flex justify-between items-center">
      <h2 class="text-lg font-bold">{{
        props.order?.status === "PENDING" ? "发货"
        : props.order?.status === "SHIPPED" ? "修改预计送达"
          : '' }}</h2>
      <i class="fas fa-close text-center font-semibold px-3 rounded-md cursor-pointer" @click="props.onClose" />
    </div>
    <div class="border border-t-1 w-full my-2"></div>

    <div>
      <div class="my-4 space-y-4">
        <div>
          <SmallViewTitle class="mb-1" text="用户邮箱" :hasUnderline='false' additionalClass="text-base" />
          <p>{{ props.order.userEmail }}</p>
        </div>
        <div>
          <SmallViewTitle class="mb-1" text="价格" :hasUnderline='false' additionalClass="text-base" />
          <p>{{ props.order.price }}</p>
        </div>
        <div>
          <SmallViewTitle class="mb-1" text="状态" :hasUnderline='false' additionalClass="text-base" />
          <p>{{ props.order.status }}</p>
        </div>
        <div>
          <SmallViewTitle class="mb-3" text="商品列表" additionalClass="text-base" />
          <ul v-for="item in props.order.items" :key="item.product.id">
            <li class="my-1">
              <img :src="item.product.imageUrls?.[0]" class="w-8 h-8 mr-2 hidden sm:inline-block" />
              <span class="font-bold text-xs">{{ item.product.name }}</span>
              <span v-if="item.amount > 1" class="font-semibold text-blue-700"> x {{ item.amount }}</span>
              <div class="border border-t-1 w-full my-2"></div>
            </li>
          </ul>
        </div>
        <div>
          <SmallViewTitle class="mb-1" text="下单时间" :hasUnderline='false' additionalClass="text-base" />
          <div class="flex items-center justify-center mt-2">
            <input type="date" disabled v-model="receivedDate" class="border p-2 w-full focus:outline-none" />
            <input type="time" disabled v-model="receivedTime" class="border p-2 w-full focus:outline-none" />
          </div>
        </div>
        <div>
          <SmallViewTitle class="mb-1" text="预计送达" :hasUnderline='false' additionalClass="text-base" />
          <div class="flex items-center justify-center mt-2">
            <input type="date" v-model="selectedDeliveryDate" class="border p-2 w-full focus:outline-none" />
            <input type="time" v-model="selectedDeliveryTime" class="border p-2 w-full focus:outline-none" />
          </div>
        </div>
      </div>
      <button v-if="props.order?.status === 'PENDING'" @click="openConfirmation"
        class="bg-blue-500 text-white px-4 py-2 rounded w-full">
        发货
      </button>
      <button v-if="props.order?.status === 'SHIPPED'" @click="openConfirmation"
        class="bg-blue-500 text-white px-4 py-2 rounded w-full">
        修改
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import type { UserOrder } from '@/types/order'
import ConfirmDialogue from '../ConfirmDialogue.vue'
import SmallViewTitle from '../SmallViewTitle.vue'

const props = defineProps<{
  order: UserOrder | null
  onSend: (orderId: string, expectedDelivery: string) => void
  onUpdate: (orderId: string, newExpectedDelivery: string) => void
  onClose: () => void
}>()

const receivedDate = ref('')
const receivedTime = ref('')
const selectedDeliveryDate = ref('')
const selectedDeliveryTime = ref('')
const isConfirmationVisible = ref(false)

function parseReceived() {
  if (props.order?.received) {
    const received = props.order.received
    receivedDate.value = received.split('T')[0]
    receivedTime.value = received.split('T')[1].slice(0, 5)
  }
}

function parseExpectedDelivery() {
  if (props.order?.expectedDelivery) {
    const expectedDelivery = props.order.expectedDelivery
    selectedDeliveryDate.value = expectedDelivery.split('T')[0]
    selectedDeliveryTime.value = expectedDelivery.split('T')[1].slice(0, 5)
  } else {
    selectedDeliveryDate.value = ''
    selectedDeliveryTime.value = ''
  }
}

onMounted(() => {
  parseReceived()
  parseExpectedDelivery()
})

watch(() => props.order, () => {
  parseReceived()
  parseExpectedDelivery()
})

function openConfirmation() {
  isConfirmationVisible.value = true
}

function closeConfirmation() {
  isConfirmationVisible.value = false
}

async function sendOrder() {
  props.onSend(props.order?.id || '', `${selectedDeliveryDate.value}T${selectedDeliveryTime.value}:00`)
}

async function changeExpectedDelivery() {
  props.onUpdate(props.order?.id || '', `${selectedDeliveryDate.value}T${selectedDeliveryTime.value}:00`)
}
</script>

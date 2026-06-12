<template>
  <div :class="['flex flex-col justify-center items-center relative', additionalClass]" @mouseover="showDropdown"
    @mouseleave="hideDropdown" @keyup.enter="handleSearch">
    <div
      class="flex justify-center items-center bg-white border border-white md:border-gray-600 rounded py-[0.5rem] md:py-0 px-6 sm:px-3 w-screen sm:w-full lg:w-[28rem]">
      <i class="fas fa-search text-black"></i>
      <input type="text" v-model="searchInput" class="px-4 py-2 w-full focus:aria-black focus:outline-none"
        :placeholder="placeholder" />
      <i v-if="hasCloseSearchEnabled" class="fas fa-close cursor-pointer" @click="handleCloseSearch" />
    </div>
    <div v-if="isOpenDropdown"
      class="bg-white w-full lg:w-[28rem] transition duration-400 rounded-sm min-h-max shadow-md border border-gray-300 absolute top-[3.73rem] md:top-[2.6rem] lg:top-[2.63rem] flex flex-col p-4 space-y-2">
      <h3 class="text-base font-semibold">排序方式</h3>
      <div class="w-full border border-t-gray-300"></div>
      <div class="flex flex-col text-base items-start justify-center space-y-1">
        <label>
          <input type="checkbox" :checked="filters.lowestPrice" @change="() => handleFilterChange('lowest_price')"
            class="form-checkbox h-4 w-4" />
          Lowest price
        </label>
        <label>
          <input type="checkbox" :checked="filters.highestPrice" @change="() => handleFilterChange('highest_price')"
            class="form-checkbox h-4 w-4" />
          价格从高到低
        </label>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file SearchBar.vue
 * @description 商品搜索栏组件，支持关键词搜索和价格排序筛选
 */
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const props = withDefaults(defineProps<{
  additionalClass?: string
  placeholder?: string
  hasCloseSearchEnabled?: boolean
  query?: string
  filter?: string
}>(), {
  placeholder: '搜索商品',
  hasCloseSearchEnabled: false
})

const emit = defineEmits<{
  search: [query: string, filter: string | null]
  onClose: [value: boolean]
}>()

// 搜索输入内容
const searchInput = ref('')
// 排序下拉菜单是否可见
const isOpenDropdown = ref(false)
// 价格排序筛选状态
const filters = reactive({
  lowestPrice: false,
  highestPrice: false
})
const router = useRouter()
const route = useRoute()

// 组件挂载时从 URL 查询参数恢复搜索状态
onMounted(() => {
  const query = route.query.query as string
  const filter = route.query.filter as string
  searchInput.value = query || ''
  // 根据 URL 中的筛选参数恢复选中状态
  if (filter === 'lowest_price') {
    filters.lowestPrice = true
  } else if (filter === 'highest_price') {
    filters.highestPrice = true
  }
})

// 鼠标悬停时显示排序下拉菜单
function showDropdown() {
  isOpenDropdown.value = true
}

// 鼠标离开时隐藏排序下拉菜单
function hideDropdown() {
  isOpenDropdown.value = false
}

/**
 * 执行搜索操作，触发搜索事件并跳转到商城页面
 */
function handleSearch() {
  emit('search',
    searchInput.value || '',
    filters.lowestPrice ? 'lowest_price' : filters.highestPrice ? 'highest_price' : null
  )
  const queryParameters = {
    query: searchInput.value,
    filter: filters.lowestPrice ? 'lowest_price' : filters.highestPrice ? 'highest_price' : null
  }
  router.push({ name: 'shop', query: queryParameters })
}

/**
 * 切换排序筛选条件，同一时间只能选中一个价格排序
 * @param {string} targetFilter - 目标筛选条件标识
 */
function handleFilterChange(targetFilter: string) {
  if (targetFilter === 'lowest_price') {
    filters.lowestPrice = !filters.lowestPrice
    // 互斥：选中最低价时取消最高价
    filters.highestPrice = false
  }
  if (targetFilter === 'highest_price') {
    filters.highestPrice = !filters.highestPrice
    // 互斥：选中最高价时取消最低价
    filters.lowestPrice = false
  }
}

// 关闭搜索框（移动端使用）
function handleCloseSearch() {
  emit('onClose', true)
}
</script>

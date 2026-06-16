/**
 * @file ProductFormSlideOver.vue
 * @description 商品添加/编辑侧滑面板，包含表单验证、分类/成色选择器和图片管理
 * @input 模式（add/edit）、编辑时的商品数据
 * @output 保存成功后触发 saved 事件
 */
<template>
  <!-- 遮罩层 -->
  <TransitionRoot :show="true" as="template">
    <Dialog class="relative z-40" @close="$emit('close')">
      <!-- 背景遮罩 -->
      <TransitionChild
        enter="ease-in-out duration-300" enter-from="opacity-0" enter-to="opacity-100"
        leave="ease-in-out duration-300" leave-from="opacity-100" leave-to="opacity-0">
        <div class="fixed inset-0 bg-gray-500/30 transition-opacity" @click="$emit('close')" />
      </TransitionChild>

      <div class="fixed inset-0 overflow-hidden">
        <div class="absolute inset-0 overflow-hidden">
          <div class="pointer-events-none fixed inset-y-0 right-0 flex max-w-full pl-10">
            <!-- 侧滑面板 -->
            <TransitionChild
              enter="transform transition ease-in-out duration-300"
              enter-from="translate-x-full" enter-to="translate-x-0"
              leave="transform transition ease-in-out duration-300"
              leave-from="translate-x-0" leave-to="translate-x-full">
              <DialogPanel class="pointer-events-auto w-screen max-w-lg">
                <div class="flex h-full flex-col overflow-y-auto bg-white shadow-xl">
                  <!-- 面板头部 -->
                  <div class="bg-gray-50 px-4 py-4 border-b">
                    <div class="flex items-center justify-between">
                      <DialogTitle class="text-lg font-semibold text-gray-800">
                        {{ mode === 'add' ? '添加商品' : '编辑商品' }}
                      </DialogTitle>
                      <button @click="$emit('close')"
                        class="text-gray-400 hover:text-gray-600 p-1 rounded hover:bg-gray-100">
                        <i class="fas fa-times"></i>
                      </button>
                    </div>
                  </div>

                  <!-- 表单内容 -->
                  <div class="flex-1 px-4 py-6 space-y-5">
                    <!-- 商品名称 -->
                    <div>
                      <label class="block text-sm font-medium text-gray-700 mb-1">
                        商品名称 <span class="text-red-500">*</span>
                      </label>
                      <input v-model="form.name" type="text"
                        :class="inputClass(errors.name)"
                        placeholder="请输入商品名称" />
                      <p v-if="errors.name" class="mt-1 text-xs text-red-500">{{ errors.name }}</p>
                    </div>

                    <!-- 商品描述 -->
                    <div>
                      <label class="block text-sm font-medium text-gray-700 mb-1">
                        商品描述 <span class="text-red-500">*</span>
                      </label>
                      <textarea v-model="form.description" rows="3"
                        :class="inputClass(errors.description)"
                        placeholder="请输入商品描述"></textarea>
                      <p v-if="errors.description" class="mt-1 text-xs text-red-500">{{ errors.description }}</p>
                    </div>

                    <!-- 分类和成色 -->
                    <div class="grid grid-cols-2 gap-4">
                      <!-- 分类选择 -->
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">
                          分类 <span class="text-red-500">*</span>
                        </label>
                        <Listbox v-model="form.category">
                          <div class="relative">
                            <ListboxButton :class="selectClass(errors.category)">
                              <span class="block truncate">{{ categoryLabel }}</span>
                              <span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                                <i class="fas fa-chevron-down text-gray-400 text-xs"></i>
                              </span>
                            </ListboxButton>
                            <transition
                              leave-active-class="transition duration-100 ease-in"
                              leave-from-class="opacity-100" leave-to-class="opacity-0">
                              <ListboxOptions class="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-sm shadow-lg ring-1 ring-black/5 focus:outline-none">
                                <ListboxOption v-for="cat in categoryOptions" :key="cat.value" :value="cat.value"
                                  v-slot="{ active, selected }">
                                  <li :class="[active ? 'bg-blue-50 text-blue-700' : 'text-gray-700', 'relative cursor-pointer select-none py-2 pl-8 pr-4']">
                                    <span v-if="selected" class="absolute inset-y-0 left-0 flex items-center pl-1.5 text-blue-600">
                                      <i class="fas fa-check text-xs"></i>
                                    </span>
                                    {{ cat.label }}
                                  </li>
                                </ListboxOption>
                              </ListboxOptions>
                            </transition>
                          </div>
                        </Listbox>
                        <p v-if="errors.category" class="mt-1 text-xs text-red-500">{{ errors.category }}</p>
                      </div>

                      <!-- 成色选择 -->
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">
                          成色 <span class="text-red-500">*</span>
                        </label>
                        <Listbox v-model="form.condition">
                          <div class="relative">
                            <ListboxButton :class="selectClass(errors.condition)">
                              <span class="block truncate">{{ conditionLabel }}</span>
                              <span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                                <i class="fas fa-chevron-down text-gray-400 text-xs"></i>
                              </span>
                            </ListboxButton>
                            <transition
                              leave-active-class="transition duration-100 ease-in"
                              leave-from-class="opacity-100" leave-to-class="opacity-0">
                              <ListboxOptions class="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-sm shadow-lg ring-1 ring-black/5 focus:outline-none">
                                <ListboxOption v-for="cond in conditionOptions" :key="cond.value" :value="cond.value"
                                  v-slot="{ active, selected }">
                                  <li :class="[active ? 'bg-blue-50 text-blue-700' : 'text-gray-700', 'relative cursor-pointer select-none py-2 pl-8 pr-4']">
                                    <span v-if="selected" class="absolute inset-y-0 left-0 flex items-center pl-1.5 text-blue-600">
                                      <i class="fas fa-check text-xs"></i>
                                    </span>
                                    {{ cond.label }}
                                  </li>
                                </ListboxOption>
                              </ListboxOptions>
                            </transition>
                          </div>
                        </Listbox>
                        <p v-if="errors.condition" class="mt-1 text-xs text-red-500">{{ errors.condition }}</p>
                      </div>
                    </div>

                    <!-- 价格和数量 -->
                    <div class="grid grid-cols-2 gap-4">
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">
                          价格 (¥) <span class="text-red-500">*</span>
                        </label>
                        <input v-model.number="form.price" type="number" step="0.01" min="0"
                          :class="inputClass(errors.price)"
                          placeholder="0.00" />
                        <p v-if="errors.price" class="mt-1 text-xs text-red-500">{{ errors.price }}</p>
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">
                          库存数量 <span class="text-red-500">*</span>
                        </label>
                        <input v-model.number="form.quantity" type="number" min="0"
                          :class="inputClass(errors.quantity)"
                          placeholder="0" />
                        <p v-if="errors.quantity" class="mt-1 text-xs text-red-500">{{ errors.quantity }}</p>
                      </div>
                    </div>

                    <!-- 图片链接管理 -->
                    <div>
                      <label class="block text-sm font-medium text-gray-700 mb-1">图片链接</label>
                      <!-- 已添加的图片标签 -->
                      <div v-if="form.imageUrls.length > 0" class="flex flex-wrap gap-2 mb-2">
                        <div v-for="(url, index) in form.imageUrls" :key="index"
                          class="group relative inline-flex items-center gap-1 bg-gray-100 rounded px-2 py-1 text-xs">
                          <img :src="url" class="w-6 h-6 object-contain" @error="handleThumbError" />
                          <span class="max-w-[120px] truncate">{{ url.split('/').pop() }}</span>
                          <button @click="removeImage(index)"
                            class="text-gray-400 hover:text-red-500 ml-1">
                            <i class="fas fa-times text-[10px]"></i>
                          </button>
                        </div>
                      </div>
                      <!-- 添加图片输入 -->
                      <div class="flex gap-2">
                        <input v-model="newImageUrl" type="url"
                          class="flex-1 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none"
                          placeholder="输入图片链接后点击添加"
                          @keyup.enter="addImage" />
                        <button @click="addImage" type="button"
                          class="px-3 py-2 bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 text-sm transition-colors">
                          添加
                        </button>
                      </div>
                    </div>
                  </div>

                  <!-- 面板底部操作栏 -->
                  <div class="border-t px-4 py-4 bg-gray-50 flex justify-end gap-3">
                    <button @click="$emit('close')"
                      class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 border border-gray-200 rounded-lg hover:bg-gray-100 transition-colors">
                      取消
                    </button>
                    <button @click="handleSubmit" :disabled="submitting"
                      class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors inline-flex items-center gap-2">
                      <i v-if="submitting" class="fas fa-spinner fa-spin"></i>
                      {{ submitting ? '保存中...' : (mode === 'add' ? '添加商品' : '保存修改') }}
                    </button>
                  </div>
                </div>
              </DialogPanel>
            </TransitionChild>
          </div>
        </div>
      </div>
    </Dialog>
  </TransitionRoot>
</template>

<script setup lang="ts">
/**
 * @file ProductFormSlideOver.vue
 * @description 商品添加/编辑侧滑面板，使用 Headless UI Dialog 实现遮罩和动画
 */
import { ref, computed, watch } from 'vue'
import {
  Dialog, DialogPanel, DialogTitle,
  TransitionRoot, TransitionChild,
  Listbox, ListboxButton, ListboxOptions, ListboxOption
} from '@headlessui/vue'
import { useAdminToolsStore } from '@/stores/network/adminToolsStore'
import { useToastStore } from '@/stores/toastStore'
import type { Product, CreateProductDto } from '@/types/product'
import { PRODUCT_CATEGORIES, PRODUCT_CONDITIONS } from '@/types/product'

const props = defineProps<{
  /** 表单模式：添加或编辑 */
  mode: 'add' | 'edit'
  /** 编辑模式下的商品数据 */
  product: Product | null
}>()

const emit = defineEmits<{
  /** 关闭面板事件 */
  (e: 'close'): void
  /** 保存成功事件 */
  (e: 'saved'): void
}>()

const adminToolsStore = useAdminToolsStore()
const toast = useToastStore()

// 分类下拉选项
const categoryOptions = Object.entries(PRODUCT_CATEGORIES).map(([value, label]) => ({ value, label }))
// 成色下拉选项
const conditionOptions = Object.entries(PRODUCT_CONDITIONS).map(([value, label]) => ({ value, label }))

// 表单数据
const form = ref<CreateProductDto>({
  name: '',
  description: '',
  imageUrls: [],
  price: 0,
  quantity: 0,
  category: '',
  condition: 'NEW',
  source: 'PLATFORM'
})

// 新图片URL输入
const newImageUrl = ref('')
// 提交中状态
const submitting = ref(false)
// 表单验证错误
const errors = ref<Record<string, string>>({})

// 当前选中分类的显示标签
const categoryLabel = computed(() => {
  return PRODUCT_CATEGORIES[form.value.category] || '请选择分类'
})

// 当前选中成色的显示标签
const conditionLabel = computed(() => {
  return PRODUCT_CONDITIONS[form.value.condition] || '请选择成色'
})

// 编辑模式下，监听商品数据变化填充表单
watch(() => props.product, (product) => {
  if (product && props.mode === 'edit') {
    form.value = {
      name: product.name,
      description: product.description,
      imageUrls: [...(product.imageUrls || [])],
      price: product.price,
      quantity: product.quantity,
      category: product.category || '',
      condition: product.condition || 'NEW',
      source: product.source || 'PLATFORM'
    }
  }
}, { immediate: true })

/**
 * 表单验证逻辑
 * @returns {boolean} 验证是否通过
 */
function validate(): boolean {
  errors.value = {}

  if (!form.value.name.trim()) {
    errors.value.name = '商品名称不能为空'
  }
  if (!form.value.description.trim()) {
    errors.value.description = '商品描述不能为空'
  }
  if (!form.value.category) {
    errors.value.category = '请选择商品分类'
  }
  if (!form.value.condition) {
    errors.value.condition = '请选择商品成色'
  }
  if (form.value.price <= 0) {
    errors.value.price = '价格必须大于0'
  }
  if (form.value.quantity < 0) {
    errors.value.quantity = '库存数量不能为负数'
  }
  if (!Number.isInteger(form.value.quantity)) {
    errors.value.quantity = '库存数量必须为整数'
  }

  return Object.keys(errors.value).length === 0
}

/**
 * 添加图片链接到列表
 */
function addImage() {
  const url = newImageUrl.value.trim()
  if (!url) return
  // 简单的 URL 格式校验
  try {
    new URL(url)
    form.value.imageUrls.push(url)
    newImageUrl.value = ''
  } catch {
    toast.addToast('请输入有效的图片链接', 'warning')
  }
}

/**
 * 移除指定索引的图片
 * @param {number} index - 图片索引
 */
function removeImage(index: number) {
  form.value.imageUrls.splice(index, 1)
}

/**
 * 缩略图加载失败时隐藏
 * @param {Event} event - 图片错误事件
 */
function handleThumbError(event: Event) {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}

/**
 * 根据错误状态返回输入框样式类
 * @param {string | undefined} error - 错误信息
 * @returns {string} Tailwind CSS 样式类
 */
function inputClass(error?: string): string {
  const base = 'w-full border rounded-lg px-3 py-2 text-sm outline-none transition-colors'
  return error
    ? `${base} border-red-300 focus:ring-2 focus:ring-red-300 focus:border-red-300`
    : `${base} border-gray-200 focus:ring-2 focus:ring-blue-300 focus:border-blue-300`
}

/**
 * 根据错误状态返回下拉选择器样式类
 * @param {string | undefined} error - 错误信息
 * @returns {string} Tailwind CSS 样式类
 */
function selectClass(error?: string): string {
  const base = 'relative w-full cursor-pointer rounded-lg bg-white py-2 pl-3 pr-10 text-left text-sm outline-none transition-colors'
  return error
    ? `${base} border border-red-300 focus:ring-2 focus:ring-red-300`
    : `${base} border border-gray-200 focus:ring-2 focus:ring-blue-300`
}

/**
 * 提交表单，执行添加或编辑操作
 */
async function handleSubmit() {
  if (!validate()) return

  submitting.value = true
  try {
    if (props.mode === 'add') {
      await adminToolsStore.addProduct({ ...form.value })
      toast.addToast('商品添加成功', 'success')
    } else if (props.product) {
      await adminToolsStore.editProduct(props.product.id, { ...form.value })
      toast.addToast('商品修改成功', 'success')
    }
    emit('saved')
  } catch (e: any) {
    toast.addToast('操作失败：' + (e?.message || '未知错误'), 'error')
  } finally {
    submitting.value = false
  }
}
</script>

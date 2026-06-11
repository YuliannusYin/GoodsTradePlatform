<template>
  <div class="flex w-full sm:space-x-5">
    <ConfirmDialogue :isPasswordRequired="false" header="确认添加商品"
      text="确定要添加此商品吗？" v-if="isConfirmationVisible" :onConfirm="addNewProduct"
      :onCancel="closeConfirmation" />

    <ProductPreview :product="product" />
    <div class="p-4 bg-white rounded shadow w-full sm:max-w-[50%] sm:min-w-[50%]">
      <SmallViewTitle text="添加商品" class="mb-2"/>
      <form @submit.prevent="openConfirmation">
        <div class="mb-4">
          <label for="productName" class="block text-gray-700 font-bold mb-2">名称</label>
          <input v-model="product.name" type="text" class="border w-full p-2 rounded" />
        </div>
        <div class="mb-4">
          <label for="productDescription" class="block text-gray-700 font-bold mb-2">描述</label>
          <textarea v-model="product.description" class="border w-full p-2 rounded"></textarea>
        </div>
        <div class="mb-4">
          <label for="productImageUrl" class="block text-gray-700 font-bold mb-2">图片链接</label>
          <input v-model="imageUrlInput" type="text" class="border w-full p-2 rounded" />
        </div>
        <div class="mb-4">
          <label for="productPrice" class="block text-gray-700 font-bold mb-2">价格</label>
          <input v-model="product.price" type="number" step="0.01" class="border w-full p-2 rounded" />
        </div>
        <div class="mb-4">
          <label for="productQuantity" class="block text-gray-700 font-bold mb-2">数量</label>
          <input v-model="product.quantity" type="number" class="border w-full p-2 rounded" />
        </div>
        <SubmitButton text="添加商品" />
      </form>
    </div>
  </div>
</template>
  
<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useAdminToolsStore } from '@/stores/network/adminToolsStore';
import type { CreateProductDto } from '@/types/product';
import ConfirmDialogue from '../ConfirmDialogue.vue';
import SmallViewTitle from '../SmallViewTitle.vue';
import ProductPreview from '../ProductPreview.vue';
import SubmitButton from '../SubmitButton.vue';

export default defineComponent({
  name: 'AddProduct',
  setup() {
    const adminToolsStore = useAdminToolsStore();
    const product = ref<CreateProductDto>({
      name: '',
      description: '',
      imageUrls: [],
      price: 0,
      quantity: 0,
      category: '',
      condition: 'NEW',
      source: 'PLATFORM'
    });
    const imageUrlInput = ref<string>('');
    const isConfirmationVisible = ref<boolean>(false);

    function openConfirmation() {
      isConfirmationVisible.value = true;
    }

    function closeConfirmation() {
      isConfirmationVisible.value = false;
    }

    async function addNewProduct() {
      product.value.imageUrls = imageUrlInput.value ? [imageUrlInput.value] : [];
      const newProduct = { ...product.value };
      await adminToolsStore.API.addProduct(newProduct);
      resetProductValues();
    }

    function resetProductValues() {
      product.value = {
        name: '',
        description: '',
        imageUrls: [],
        price: 0,
        quantity: 0,
        category: '',
        condition: 'NEW',
        source: 'PLATFORM'
      };
      imageUrlInput.value = '';
    }
    return { product, imageUrlInput, addNewProduct, isConfirmationVisible, openConfirmation, closeConfirmation };
  },
  components: { ConfirmDialogue, SmallViewTitle, ProductPreview, SubmitButton }
});
</script>
  
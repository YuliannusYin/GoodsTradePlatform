<template>
  <div class="flex w-full sm:space-x-5">
    <ConfirmDialogue :isPasswordRequired="false" header="确认编辑商品"
      text="确定要编辑此商品吗？" v-if="isConfirmationVisible" :onConfirm="editProduct"
      :onCancel="closeConfirmation" />

    <ProductPreview :product="product" />
    <div class="p-4 bg-white rounded shadow w-full sm:max-w-[50%] sm:min-w-[50%]">
      <SmallViewTitle text="编辑商品" class="mb-2" />
      <div class="mb-4">
        <label for="products" class="block text-gray-700 font-bold mb-2">商品</label>
        <select v-model="selectedProductId" class="border w-full p-2 rounded" @change="loadProduct(selectedProductId)">
          <option v-for="product in products" :key="product.id" :value="product.id">
            {{ product.name }}
          </option>
        </select>
      </div>
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
        <SubmitButton text="Edit product" />
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue';
import { useAdminToolsStore } from '@/stores/network/adminToolsStore';
import type { EditProductDto, Product } from '@/types/product';
import { useProductStore } from '@/stores/network/productStore';
import ConfirmDialogue from '../ConfirmDialogue.vue';
import ProductPreview from '../ProductPreview.vue';
import SmallViewTitle from '../SmallViewTitle.vue';
import SubmitButton from '../SubmitButton.vue';

export default defineComponent({
  name: 'EditProduct',

  setup() {
    const adminToolsStore = useAdminToolsStore();
    const productStore = useProductStore();
    const products = ref<Product[]>([]);

    onMounted(async () => {
      products.value = await productStore.API.getAllProducts();
      if (products.value.length > 0) {
        selectedProductId.value = products.value[0].id;
        loadProduct(selectedProductId.value);
      }
    });

    const selectedProductId = ref<string>('');
    const product = ref<EditProductDto>({
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

    async function loadProduct(productId: string) {
      selectedProductId.value = productId;
      const response = await productStore.API.getProduct(productId);
      selectedProductId.value = productId;
      product.value.name = response.name;
      product.value.description = response.description;
      product.value.imageUrls = response.imageUrls || [];
      imageUrlInput.value = response.imageUrls?.[0] || '';
      product.value.price = response.price;
      product.value.quantity = response.quantity;
    }

    async function editProduct() {
      product.value.imageUrls = imageUrlInput.value ? [imageUrlInput.value] : [];
      const editedProduct = { ...product.value };
      await adminToolsStore.API.editProduct(selectedProductId.value, editedProduct);
    }

    return { products, selectedProductId, product, imageUrlInput, isConfirmationVisible, openConfirmation, closeConfirmation, loadProduct, editProduct };
  },
  components: { ConfirmDialogue, ProductPreview, SmallViewTitle, SubmitButton }
});
</script>

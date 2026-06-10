<template>
  <section class="max-w-7xl mx-auto px-4 py-6">
    <div class="flex flex-col md:flex-row gap-6">
      <aside class="w-full md:w-56 shrink-0">
        <div class="bg-white rounded-xl shadow-md p-4 sticky top-4">
          <h3 class="text-lg font-bold text-gray-800 mb-3">商品分类</h3>
          <div class="space-y-1">
            <button @click="selectCategory('')"
              class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
              :class="!selectedCategory ? 'bg-primary-500 text-white font-medium' : 'text-gray-600 hover:bg-primary-50'">
              全部
            </button>
            <button v-for="(label, key) in PRODUCT_CATEGORIES" :key="key"
              @click="selectCategory(key)"
              class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
              :class="selectedCategory === key ? 'bg-primary-500 text-white font-medium' : 'text-gray-600 hover:bg-primary-50'">
              {{ label }}
            </button>
          </div>
        </div>
      </aside>
      <div class="flex-1">
        <ProductCards :placeholderAmount="10" :products="products" />
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref, watch } from 'vue';
import ProductCards from '@/components/products/ProductCards.vue';
import { useProductStore } from '@/stores/network/productStore';
import type { Product } from '@/types/product';
import { PRODUCT_CATEGORIES } from '@/types/product';
import { useRoute, useRouter } from 'vue-router';

export default defineComponent({
  name: "ShopView",
  setup() {
    const productStore = useProductStore();
    const products = ref<Product[]>([]);
    const selectedCategory = ref('');
    const route = useRoute();
    const router = useRouter();

    async function getAllProducts() {
      products.value = await productStore.API.getAllProducts();
    }

    async function getSearchedProducts(query: string, filter: any, category?: string) {
      products.value = await productStore.API.getSearchedProducts(query, filter, category);
    }

    function isEmpty(query: string): boolean {
      return query === '';
    }

    function hasNoFilter(filter: any): boolean {
      return filter === null;
    }

    async function handleSearch(query: string, filter: any, category?: string) {
      if (isEmpty(query) && hasNoFilter(filter) && !category) {
        getAllProducts();
      } else {
        getSearchedProducts(query, filter, category);
      }
    };

    function selectCategory(category: string) {
      selectedCategory.value = category;
      const query = (route.query.query as string) || '';
      const filter = (route.query.filter as string) || 'none';
      handleSearch(query, filter, category || undefined);
    }

    onMounted(async () => {
      const query = route.query.query as string;
      const filter = route.query.filter as string;
      const category = route.query.category as string;

      if (category) {
        selectedCategory.value = category;
      }

      const hasNoSearchQuery = query == undefined;
      const hasNoFilter = filter == undefined;

      if (hasNoSearchQuery || hasNoFilter) {
        if (category) {
          handleSearch('', 'none', category);
        } else {
          getAllProducts();
        }
      } else {
        handleSearch(query, filter, category || undefined);
      }
    })

    watch(
      () => ({
        query: route.query.query as string,
        filter: route.query.filter as string,
        category: route.query.category as string,
      }),
      (newQuery) => {
        const { query, filter, category } = newQuery;
        if (category) {
          selectedCategory.value = category;
        }
        handleSearch(query || '', filter || null, category || selectedCategory.value || undefined);
      },
    );

    return {
      products,
      handleSearch,
      selectedCategory,
      selectCategory,
      PRODUCT_CATEGORIES
    };
  },
  components: {
    ProductCards
  }
});
</script>

<template>
  <section class="max-w-5xl mx-auto px-4 py-6">
    <ProductCard v-if="product" :product="product" :isForProductView='true' />

    <div v-if="product" class="mt-8">
      <div class="bg-white rounded-2xl shadow-md p-6">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-gray-800">商品评价</h2>
          <div v-if="productRating" class="flex items-center gap-2">
            <span class="text-3xl font-bold text-primary-600">{{ productRating.averageRating }}</span>
            <div class="flex flex-col">
              <div class="flex">
                <svg v-for="i in 5" :key="i" xmlns="http://www.w3.org/2000/svg"
                  class="h-4 w-4" :class="i <= Math.round(productRating.averageRating) ? 'text-yellow-400' : 'text-gray-300'"
                  viewBox="0 0 20 20" fill="currentColor">
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                </svg>
              </div>
              <span class="text-xs text-gray-500">{{ productRating.reviewCount }} 条评价</span>
            </div>
          </div>
        </div>

        <div v-if="authStore.states.isAuthenticated" class="mb-6 p-4 bg-primary-50 rounded-xl">
          <h3 class="font-semibold text-gray-700 mb-3">发表评价</h3>
          <div class="flex items-center gap-1 mb-3">
            <span class="text-sm text-gray-600 mr-2">评分：</span>
            <button v-for="i in 5" :key="i" @click="newRating = i" class="focus:outline-none">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 transition-colors"
                :class="i <= newRating ? 'text-yellow-400' : 'text-gray-300'" viewBox="0 0 20 20" fill="currentColor">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
            </button>
          </div>
          <textarea v-model="newComment" rows="3"
            class="w-full p-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-300 focus:border-primary-300 outline-none resize-none"
            placeholder="分享你对这个商品的看法..."></textarea>
          <button @click="submitReview" class="btn-primary mt-3 text-sm"
            :disabled="newRating === 0 || !newComment.trim()">
            提交评价
          </button>
        </div>

        <div class="space-y-4">
          <div v-for="review in reviews" :key="review.id"
            class="p-4 border-b border-gray-100 last:border-0">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <div class="w-8 h-8 bg-primary-200 rounded-full flex items-center justify-center text-primary-700 font-bold text-sm">
                  {{ review.username.charAt(0).toUpperCase() }}
                </div>
                <span class="font-medium text-gray-800">{{ review.username }}</span>
              </div>
              <span class="text-xs text-gray-400">{{ review.createdAt }}</span>
            </div>
            <div class="flex mb-2">
              <svg v-for="i in 5" :key="i" xmlns="http://www.w3.org/2000/svg"
                class="h-4 w-4" :class="i <= review.rating ? 'text-yellow-400' : 'text-gray-300'"
                viewBox="0 0 20 20" fill="currentColor">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
            </div>
            <p class="text-gray-600 text-sm">{{ review.comment }}</p>
          </div>
          <div v-if="reviews.length === 0" class="text-center py-8 text-gray-400">
            暂无评价，快来发表第一条评价吧！
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
  
<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue';
import type { Product } from '@/types/product';
import type { Review, ProductRating } from '@/types/review';
import { useShoppingCartStore } from '@/stores/shoppingCartStore';
import { useProductStore } from '@/stores/network/productStore';
import { useReviewStore } from '@/stores/network/reviewStore';
import { useAuthenticationStore } from '@/stores/authenticationStore';
import { useRoute } from 'vue-router';
import ProductCard from '@/components/products/ProductCard.vue';

export default defineComponent({
  name: "ProductView",
  setup() {
    const route = useRoute();
    const shoppingCartStore = useShoppingCartStore();
    const productStore = useProductStore();
    const reviewStore = useReviewStore();
    const authStore = useAuthenticationStore();
    const product = ref<Product | null>(null);
    const reviews = ref<Review[]>([]);
    const productRating = ref<ProductRating | null>(null);
    const newRating = ref(0);
    const newComment = ref('');

    async function loadProductData() {
      const productId = route.params.productId as string;
      product.value = await productStore.API.getProduct(productId);
      await loadReviews(productId);
    }

    async function loadReviews(productId: string) {
      try {
        reviews.value = await reviewStore.API.getProductReviews(productId);
        productRating.value = await reviewStore.API.getProductRating(productId);
      } catch (error) {
        console.error('Failed to load reviews:', error);
      }
    }

    async function submitReview() {
      if (!product.value || newRating.value === 0 || !newComment.value.trim()) return;

      try {
        await reviewStore.API.addReview({
          rating: newRating.value,
          comment: newComment.value,
          productId: product.value.id
        });
        newRating.value = 0;
        newComment.value = '';
        await loadReviews(product.value.id);
      } catch (error) {
        console.error('Failed to submit review:', error);
      }
    }

    function addToCart(productId: string) {
      shoppingCartStore.methods.addProductId(productId)
    };

    onMounted(loadProductData)

    return {
      product, addToCart, reviews, productRating,
      newRating, newComment, submitReview, authStore
    };
  },
  components: {
    ProductCard
  }

});
</script>

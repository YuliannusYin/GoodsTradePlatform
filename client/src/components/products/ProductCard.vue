<template>
    <div v-if="isForProductView" class="flex justify-center">
        <div class="bg-white p-6 w-full flex flex-col justify-center items-center md:flex-row md:items-start gap-6 rounded-2xl shadow-lg">
            <div class="w-full md:w-1/2">
                <img v-if="product.imageUrls && product.imageUrls.length > 0"
                    :src="product.imageUrls[0]" :alt="product.name"
                    class="w-full h-[20rem] object-contain rounded-xl">
            </div>
            <div class="flex flex-col justify-start items-start space-y-3 w-full md:w-1/2">
                <div class="flex items-center gap-2">
                    <span v-if="product.category" class="text-xs bg-primary-100 text-primary-700 px-2 py-1 rounded-full font-medium">
                        {{ getCategoryLabel(product.category) }}
                    </span>
                    <span v-if="product.condition" class="text-xs bg-mint-100 text-mint-700 px-2 py-1 rounded-full font-medium">
                        {{ getConditionLabel(product.condition) }}
                    </span>
                    <span v-if="product.source === 'USER'" class="text-xs bg-accent-100 text-accent-700 px-2 py-1 rounded-full font-medium">
                        个人闲置
                    </span>
                </div>
                <h3 class="text-2xl font-bold text-gray-800">{{ product.name }}</h3>
                <p class="text-gray-600 leading-relaxed">{{ product.description }}</p>
                <div v-if="product.seller" class="text-sm text-gray-500">
                    卖家：{{ product.seller.username }}
                </div>
                <span class="text-2xl font-bold text-accent-600">¥{{ product.price.toFixed(2) }}</span>
                <div class="flex items-center gap-3">
                    <AddToCartButton :product="product" />
                    <button @click="toggleFavorite" class="p-2 rounded-full transition-colors"
                        :class="isFavorited ? 'text-accent-500' : 'text-gray-400 hover:text-accent-400'">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" :fill="isFavorited ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                        </svg>
                    </button>
                </div>
            </div>
        </div>
    </div>
    <div v-else class="bg-white p-4 flex flex-col justify-between min-w-max rounded-xl shadow-md card-hover">
        <div class="relative">
            <img v-if="product.imageUrls && product.imageUrls.length > 0"
                :src="product.imageUrls[0]" :alt="product.name"
                class="mb-3 h-[12rem] w-full object-contain cursor-pointer rounded-lg"
                @click="() => showProductView(product.id)">
            <button @click.stop="toggleFavorite" class="absolute top-2 right-2 p-1.5 rounded-full bg-white/80 backdrop-blur-sm transition-colors"
                :class="isFavorited ? 'text-accent-500' : 'text-gray-400 hover:text-accent-400'">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" :fill="isFavorited ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
            </button>
        </div>
        <div class="flex items-center gap-1.5 mb-2">
            <span v-if="product.category" class="text-[10px] bg-primary-100 text-primary-700 px-1.5 py-0.5 rounded-full">
                {{ getCategoryLabel(product.category) }}
            </span>
            <span v-if="product.condition && product.condition !== 'NEW'" class="text-[10px] bg-mint-100 text-mint-700 px-1.5 py-0.5 rounded-full">
                {{ getConditionLabel(product.condition) }}
            </span>
        </div>
        <h3 class="text-sm font-semibold mb-2 text-gray-800 line-clamp-2 cursor-pointer hover:text-primary-600"
            @click="() => showProductView(product.id)">{{ product.name }}</h3>
        <div class="flex flex-col justify-between items-center gap-2">
            <span class="text-lg font-bold text-accent-600">¥{{ product.price.toFixed(2) }}</span>
            <AddToCartButton :product="product" />
        </div>
    </div>
</template>
  
<script lang="ts">
import { defineComponent, type PropType } from 'vue';
import AddToCartButton from './AddToCartButton.vue';
import type { Product } from '@/types/product';
import { PRODUCT_CATEGORIES, PRODUCT_CONDITIONS } from '@/types/product';
import { useRouter } from 'vue-router';
import { useFavoriteStore } from '@/stores/network/favoriteStore';
import { useAuthenticationStore } from '@/stores/authenticationStore';
import { ref, onMounted } from 'vue';

export default defineComponent({
    name: "ProductCard",

    props: {
        product: {
            type: Object as PropType<Product>,
            required: true,
        },

        isForProductView: {
            type: Boolean,
            required: false,
            default: false,
        }
    },

    setup(props) {
        const router = useRouter()
        const favoriteStore = useFavoriteStore()
        const authStore = useAuthenticationStore()
        const isFavorited = ref(false)

        onMounted(async () => {
            if (authStore.states.isAuthenticated) {
                try {
                    isFavorited.value = await favoriteStore.API.isFavorite(props.product.id)
                } catch {
                    isFavorited.value = false
                }
            }
        })

        function showProductView(productId: string) {
            router.push({ name: 'productView', params: { productId: productId } });
        }

        async function toggleFavorite() {
            if (!authStore.states.isAuthenticated) {
                router.push('/login')
                return
            }
            try {
                if (isFavorited.value) {
                    await favoriteStore.API.removeFavorite(props.product.id)
                    isFavorited.value = false
                } else {
                    await favoriteStore.API.addFavorite(props.product.id)
                    isFavorited.value = true
                }
            } catch (error) {
                console.error('Failed to toggle favorite:', error)
            }
        }

        function getCategoryLabel(category: string): string {
            return PRODUCT_CATEGORIES[category] || category
        }

        function getConditionLabel(condition: string): string {
            return PRODUCT_CONDITIONS[condition] || condition
        }

        return { showProductView, toggleFavorite, isFavorited, getCategoryLabel, getConditionLabel };
    },
    components: { AddToCartButton }
})

</script>

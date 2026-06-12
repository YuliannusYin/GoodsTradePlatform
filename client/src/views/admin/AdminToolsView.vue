<template>
  <section class="px-0 py-0 lg:p-10">
    <div class="bg-white rounded-md sm:flex sm:p-4">
      <div class="flex justify-evenly sm:flex-col sm:justify-start sm:items-center sm:mr-4 sm:space-y-4">
        <button class="border p-2 w-full min-w-max sm:p-4 hover:bg-blue-50 hover:text-blue-600"
          :class="{ 'bg-blue-50 text-blue-600': isOnProductsRoute, 'bg-white': !isOnProductsRoute }">
          <i class="fas fa-cube"></i>
          <router-link :to="{ name: 'HandleProductsView' }" class="text-black font-semibold">
            商品管理
          </router-link>
        </button>
        <button class="border p-2 w-full min-w-max sm:p-4 hover:bg-blue-50 hover:text-blue-600"
          :class="{ 'bg-blue-50 text-blue-600': isOnOrdersRoute, 'bg-white': !isOnOrdersRoute }">
          <i class="fas fa-truck sm:ml-[-0.6rem]"></i>
          <router-link :to="{ name: 'HandleOrdersView' }" class="text-black ml-1 font-semibold">
            订单管理
          </router-link>
        </button>
        <button v-if="isSuperAdmin" class="border p-2 w-full min-w-max sm:p-4 hover:bg-blue-50 hover:text-blue-600"
          :class="{ 'bg-blue-50 text-blue-600': isOnRolesRoute, 'bg-white': !isOnRolesRoute }">
          <i class="fas fa-shield-alt"></i>
          <router-link :to="{ name: 'RoleManagementView' }" class="text-black font-semibold">
            角色管理
          </router-link>
        </button>
        <button class="border p-2 w-full min-w-max sm:p-4 hover:bg-blue-50 hover:text-blue-600"
          :class="{ 'bg-blue-50 text-blue-600': isOnUsersRoute, 'bg-white': !isOnUsersRoute }">
          <i class="fas fa-users"></i>
          <router-link :to="{ name: 'UserManagementView' }" class="text-black font-semibold">
            用户管理
          </router-link>
        </button>
        <button class="border p-2 w-full min-w-max sm:p-4 hover:bg-blue-50 hover:text-blue-600"
          :class="{ 'bg-blue-50 text-blue-600': isOnReviewsRoute, 'bg-white': !isOnReviewsRoute }">
          <i class="fas fa-clipboard-check"></i>
          <router-link :to="{ name: 'ProductReviewView' }" class="text-black font-semibold">
            商品审核
          </router-link>
        </button>
      </div>
      <RouterView class="w-full border" />
    </div>
  </section>
</template>

<script lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthenticationStore } from '@/stores/authenticationStore';

export default {
  name: 'AdminToolsView',

  setup() {
    const route = useRoute();
    const authStore = useAuthenticationStore();

    const isOnProductsRoute = ref<boolean>(false)
    const isOnOrdersRoute = ref<boolean>(false)
    const isOnRolesRoute = ref<boolean>(false)
    const isOnUsersRoute = ref<boolean>(false)
    const isOnReviewsRoute = ref<boolean>(false)

    const isSuperAdmin = ref<boolean>(authStore.states.isSuperAdmin)

    function assignHighlightedButton() {
      isOnProductsRoute.value = ['AddProduct', 'EditProduct', 'DeleteProduct', 'HandleProductsView'].includes(route.name as string);
      isOnOrdersRoute.value = ['PendingOrders', 'SentOrders', 'AllOrders', 'HandleOrdersView'].includes(route.name as string);
      isOnRolesRoute.value = ['RoleManagementView'].includes(route.name as string);
      isOnUsersRoute.value = ['UserManagementView'].includes(route.name as string);
      isOnReviewsRoute.value = ['ProductReviewView'].includes(route.name as string);
    }

    onMounted(() => {
      assignHighlightedButton();
    });

    watch(() => route.name, () => {
      assignHighlightedButton();
    });

    return { isOnProductsRoute, isOnOrdersRoute, isOnRolesRoute, isOnUsersRoute, isOnReviewsRoute, isSuperAdmin };
  },

  components: {},
};
</script>

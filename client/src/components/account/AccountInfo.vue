<template>
    <div class="p-4 bg-white rounded shadow">
        <h2 class="text-xl font-semibold mb-4">账户信息</h2>
        <div>
            <p><strong>用户名：</strong> {{ username }}</p>
            <p><strong>邮箱：</strong> {{ email }}</p>
            <p><strong>余额：</strong> ${{ formattedBalance }}</p>
            <p v-if="isProtected" class="mt-2 text-sm text-amber-600 font-medium">
                这是系统账户，用户名、邮箱、密码和删除操作受到限制。
            </p>
        </div>
    </div>
</template>

<script lang="ts">
import { useAccountStore } from '@/stores/network/accountStore';
import { defineComponent, ref, onMounted, watch, computed } from 'vue';

export default defineComponent({
    name: 'AccountInfo',
    setup() {
        const accountStore = useAccountStore();

        const username = ref<string | null>(null);
        const email = ref<string | null>(null);
        const balance = ref<number>(0);
        const isProtected = ref<boolean>(false);

        const formattedBalance = computed(() => {
            return balance.value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        });

        onMounted(async () => {
            username.value = accountStore.states.username;
            email.value = accountStore.states.email;
            balance.value = accountStore.states.balance;
            isProtected.value = accountStore.states.isProtected;
        });

        watch(() => accountStore.states.username, (val: any) => { if (val) username.value = val; });
        watch(() => accountStore.states.email, (val: any) => { if (val) email.value = val; });
        watch(() => accountStore.states.balance, (val: any) => { if (val !== undefined) balance.value = val; });
        watch(() => accountStore.states.isProtected, (val: any) => { if (val !== undefined) isProtected.value = val; });

        return { username, email, balance, isProtected, formattedBalance }
    },
});
</script>
  
<template>
    <RouterLink to="/">
        <div class="flex items-center justify-center min-w-max">
            <span class="text-2xl mr-2">🎮</span>
            <p class="text-primary-700 text-lg font-bold">周边交易平台</p>
        </div>
    </RouterLink>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { RouterLink } from 'vue-router';

export default defineComponent({
    name: "HeaderLogo",
    setup() {

    },
    components: {
        RouterLink
    },
})
</script>

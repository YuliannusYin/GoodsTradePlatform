<template>
    <nav class="space-x-6">
        <StyledRouterLink text="首页" path="/"
            additionalClass="text-primary-200 hover:text-white transition duration-300 ease-in-out" />
        <StyledRouterLink text="周边商城" path="/shop"
            additionalClass="text-primary-200 hover:text-white transition duration-300 ease-in-out" />
    </nav>
</template>
  
<script lang="ts">
import StyledRouterLink from '../StyledRouterLink.vue';

export default {
    name: "FooterNavItems",
    components: {
        StyledRouterLink
    },
}
</script>

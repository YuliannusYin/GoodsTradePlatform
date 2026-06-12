<template>
  <footer class="bg-primary-800 text-white py-10">
    <div class="container mx-auto px-4">
      <div class="flex justify-between items-center mb-6">
        <FooterNavItems />
        <SocialsIcons />
      </div>
      <div class="text-center text-primary-300">
        <p>&copy; 2025 周边交易平台 - 互联网应用开发期末大作业</p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import FooterNavItems from './FooterNavItems.vue'
import SocialsIcons from './SocialsIcons.vue'
</script>

<template>
  <article class="hero-background h-[28rem] flex items-center justify-center text-white relative overflow-hidden">
    <div class="absolute inset-0 bg-gradient-to-r from-primary-900/80 to-accent-900/60"></div>
    <div class="text-center relative z-10">
      <h1 class="text-4xl md:text-6xl lg:text-7xl font-black mb-4 tracking-wide">
        <span class="text-accent-300">周边</span>交易平台
      </h1>
      <p class="text-lg md:text-xl font-medium mb-8 text-primary-100">发现你心爱的动漫 · 游戏 · 偶像周边</p>
      <router-link to="/shop" class="btn-accent text-lg inline-block">探索周边</router-link>
    </div>
    <div class="absolute -bottom-1 left-0 right-0">
      <svg viewBox="0 0 1440 120" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M0 120L60 105C120 90 240 60 360 52.5C480 45 600 60 720 67.5C840 75 960 75 1080 67.5C1200 60 1320 45 1380 37.5L1440 30V120H1380C1320 120 1200 120 1080 120C960 120 840 120 720 120C600 120 480 120 360 120C240 120 120 120 60 120H0Z" fill="#f0f4ff"/>
      </svg>
    </div>
  </article>
</template>

<script setup lang="ts">
</script>

<style scoped>
.hero-background {
  background: linear-gradient(135deg, #4263eb 0%, #7c3aed 50%, #d6336c 100%);
  background-size: cover;
}
</style>

<!--
  @file App.vue
  @description 应用根组件，定义整体页面布局结构（导航栏 + 路由视图 + 页脚）
  @input 无
  @output 渲染完整的页面框架
-->
<template>
  <!-- 顶部导航栏 -->
  <NavBar />
  <!-- 路由视图区域，根据当前路由渲染对应页面 -->
  <RouterView class="min-h-max p-0 sm:p-2.5 bg-primary-50" />
  <!-- 底部信息栏 -->
  <FooterInfo />
</template>

<script setup lang="ts">
import NavBar from './components/header/NavBar.vue';
import { RouterView } from 'vue-router'
import FooterInfo from './components/footer/FooterInfo.vue'
</script>


<style>
html {
  /* 页面全局背景色 */
  background-color: #f0f4ff;
  /* 全局字体设置：优先使用 Noto Sans SC 中文字体 */
  font-family: 'Noto Sans SC', system-ui, sans-serif;
}
</style>

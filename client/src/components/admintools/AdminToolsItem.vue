<template>
  <div class="cursor-pointer transition duration-300" @mouseover="showPopup" @mouseleave="hidePopup">
    <router-link to="/admin_tools" class="text-blue-500">
      <i class="fas fa-id-card"></i>
    </router-link>
    <AdminToolsPopup v-if="isShowingPopup" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AdminToolsPopup from './AdminToolsPopup.vue'

const isShowingPopup = ref(false)

function showPopup() {
  isShowingPopup.value = true
}

function hidePopup() {
  isShowingPopup.value = false
}
</script>

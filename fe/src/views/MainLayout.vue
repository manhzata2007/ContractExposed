<script setup lang="ts">
import { RouterView } from 'vue-router'
import AppNavbar from '@/components/AppNavbar.vue'
</script>

<template>
  <div class="min-h-screen flex flex-col bg-gray-50">
    <AppNavbar />
    <main class="flex-1">
      <RouterView />
    </main>
    <footer class="border-t border-gray-200 bg-white py-4 text-center text-xs text-gray-400">
      © 2024 ContractExposed – "Đọc trước khi ký, hiểu rõ trước khi trả tiền"
    </footer>
  </div>
</template>

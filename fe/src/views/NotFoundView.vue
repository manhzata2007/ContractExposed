<script setup lang="ts">
import { useRouter } from 'vue-router'
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 px-4">
    <div class="text-center animate-fade-in">
      <p class="text-8xl font-black text-red-100">404</p>
      <h1 class="text-2xl font-bold text-gray-900 mt-2">Trang không tồn tại</h1>
      <p class="text-gray-500 mt-2">Trang bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
      <router-link
        to="/"
        class="inline-flex items-center gap-2 mt-6 px-6 py-3 bg-red-600 hover:bg-red-700 text-white rounded-xl font-medium transition-colors"
      >
        Về Trang Chủ
      </router-link>
    </div>
  </div>
</template>

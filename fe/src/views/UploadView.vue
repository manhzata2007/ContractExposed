<script setup lang="ts">
import { Shield } from 'lucide-vue-next'
import FileUploader from '@/components/FileUploader.vue'
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-10 animate-fade-in">
    <div class="text-center mb-8">
      <div class="inline-flex items-center justify-center w-14 h-14 bg-red-100 rounded-2xl mb-4">
        <Shield class="w-7 h-7 text-red-600" />
      </div>
      <h1 class="text-3xl font-bold text-gray-900">Upload Hợp Đồng</h1>
      <p class="text-gray-500 mt-2 max-w-md mx-auto">
        Tải lên hợp đồng của bạn. AI sẽ tự động phân tích và phát hiện các điều khoản bất lợi.
      </p>
    </div>

    <FileUploader />

    <!-- Privacy notice -->
    <p class="text-center text-xs text-gray-400 mt-6">
      🔒 File của bạn được xử lý an toàn và không được chia sẻ với bên thứ ba.
      Các file sẽ được tự động xóa sau 30 ngày.
    </p>
  </div>
</template>

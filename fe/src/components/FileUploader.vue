<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Upload, FileText, AlertCircle, CheckCircle } from 'lucide-vue-next'
import { useContractStore } from '@/stores/contractStore'
import { ElMessage } from 'element-plus'

const router = useRouter()
const store  = useContractStore()

const isDragging  = ref(false)
const selectedFile = ref<File | null>(null)
const fileError    = ref('')

const ALLOWED_TYPES = ['application/pdf',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/msword', 'image/png', 'image/jpeg', 'image/tiff']
const MAX_SIZE_MB = 20

const fileSizeMB = computed(() =>
  selectedFile.value ? (selectedFile.value.size / 1024 / 1024).toFixed(2) : '0'
)

function validateFile(file: File): boolean {
  if (!ALLOWED_TYPES.includes(file.type)) {
    fileError.value = 'Định dạng không hỗ trợ. Chỉ chấp nhận: PDF, DOCX, DOC, PNG, JPG, TIFF.'
    return false
  }
  if (file.size > MAX_SIZE_MB * 1024 * 1024) {
    fileError.value = `File quá lớn. Tối đa ${MAX_SIZE_MB}MB.`
    return false
  }
  fileError.value = ''
  return true
}

function onFilePicked(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files?.[0] && validateFile(input.files[0])) {
    selectedFile.value = input.files[0]
  }
}

function onDrop(e: DragEvent) {
  isDragging.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file && validateFile(file)) selectedFile.value = file
}

function clearFile() {
  selectedFile.value = null
  fileError.value    = ''
}

async function submit() {
  if (!selectedFile.value) return
  try {
    const result = await store.upload(selectedFile.value)
    router.push({ name: 'analyzing', params: { id: result.id } })
  } catch {
    ElMessage.error('Upload thất bại. Vui lòng thử lại.')
  }
}

function formatFileType(mime: string): string {
  if (mime.includes('pdf'))  return 'PDF'
  if (mime.includes('word') || mime.includes('openxmlformats')) return 'DOCX/DOC'
  if (mime.includes('image')) return 'Ảnh'
  return 'File'
}
</script>

<template>
  <div class="w-full max-w-2xl mx-auto">

    <!-- Drop zone -->
    <div
      v-if="!selectedFile"
      class="relative border-2 border-dashed rounded-2xl p-12 text-center cursor-pointer transition-all duration-200"
      :class="isDragging
        ? 'border-red-400 bg-red-50 scale-[1.01]'
        : 'border-gray-300 bg-white hover:border-red-300 hover:bg-red-50/30'"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="onDrop"
      @click="($refs.fileInput as HTMLInputElement).click()"
    >
      <input
        ref="fileInput"
        type="file"
        class="hidden"
        accept=".pdf,.docx,.doc,.png,.jpg,.jpeg,.tiff"
        @change="onFilePicked"
      />

      <div class="flex flex-col items-center gap-4">
        <div
          class="w-16 h-16 rounded-2xl flex items-center justify-center transition-colors"
          :class="isDragging ? 'bg-red-100' : 'bg-gray-100'"
        >
          <Upload class="w-8 h-8" :class="isDragging ? 'text-red-500' : 'text-gray-400'" />
        </div>
        <div>
          <p class="text-lg font-semibold text-gray-700">
            {{ isDragging ? 'Thả file vào đây...' : 'Kéo & thả hoặc nhấn để chọn file' }}
          </p>
          <p class="text-sm text-gray-400 mt-1">PDF, DOCX, DOC, PNG, JPG, TIFF – tối đa 20MB</p>
        </div>
        <button
          type="button"
          class="px-5 py-2.5 bg-red-600 hover:bg-red-700 text-white rounded-xl text-sm font-medium transition-colors shadow-sm"
        >
          Chọn File Hợp Đồng
        </button>
      </div>
    </div>

    <!-- File error -->
    <div v-if="fileError" class="mt-3 flex items-center gap-2 text-red-600 text-sm">
      <AlertCircle class="w-4 h-4 flex-shrink-0" />
      {{ fileError }}
    </div>

    <!-- File selected preview -->
    <div v-if="selectedFile" class="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm animate-slide-up">
      <div class="flex items-start gap-4">
        <div class="w-12 h-12 bg-red-100 rounded-xl flex items-center justify-center flex-shrink-0">
          <FileText class="w-6 h-6 text-red-600" />
        </div>
        <div class="flex-1 min-w-0">
          <p class="font-semibold text-gray-900 truncate">{{ selectedFile.name }}</p>
          <p class="text-sm text-gray-500 mt-0.5">
            {{ formatFileType(selectedFile.type) }} · {{ fileSizeMB }} MB
          </p>
        </div>
        <button
          @click="clearFile"
          class="p-1.5 hover:bg-gray-100 rounded-lg text-gray-400 hover:text-gray-600 transition-colors"
          aria-label="Xóa file"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>

      <!-- Upload progress -->
      <div v-if="store.isUploading" class="mt-4">
        <div class="flex justify-between text-xs text-gray-500 mb-1">
          <span>Đang upload...</span>
          <span>{{ store.uploadProgress }}%</span>
        </div>
        <div class="w-full bg-gray-100 rounded-full h-2">
          <div
            class="bg-red-500 h-2 rounded-full transition-all duration-300"
            :style="{ width: store.uploadProgress + '%' }"
          />
        </div>
      </div>

      <!-- Submit button -->
      <button
        v-if="!store.isUploading"
        @click="submit"
        class="mt-5 w-full flex items-center justify-center gap-2 bg-red-600 hover:bg-red-700 text-white py-3 rounded-xl font-semibold transition-colors shadow-sm"
      >
        <CheckCircle class="w-5 h-5" />
        Phân Tích Hợp Đồng
      </button>
    </div>
  </div>
</template>

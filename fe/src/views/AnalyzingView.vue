<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Shield, AlertCircle } from 'lucide-vue-next'
import { useContractStore } from '@/stores/contractStore'
import type { ContractStatusResponse } from '@/types'

const props  = defineProps<{ id: string }>()
const router = useRouter()
const store  = useContractStore()

const contractId = Number(props.id)

const statusMessages: Record<string, string> = {
  UPLOADED:   'Đã nhận file, chuẩn bị xử lý…',
  EXTRACTING: 'Đang trích xuất văn bản từ file…',
  EXTRACTED:  'Trích xuất hoàn tất, bắt đầu phân tích AI…',
  ANALYZING:  'AI đang đọc và phân tích các điều khoản…',
  ANALYZED:   'Phân tích hoàn tất! Đang chuyển hướng…',
  FAILED:     'Phân tích thất bại.',
}

const progressMap: Record<string, number> = {
  UPLOADED: 10, EXTRACTING: 30, EXTRACTED: 50, ANALYZING: 75, ANALYZED: 100, FAILED: 100,
}

const currentStatus = ref<string>('UPLOADED')
const errorMsg      = ref<string>('')

onMounted(() => {
  store.startPolling(contractId, (status: ContractStatusResponse) => {
    currentStatus.value = status.status
    if (status.reportReady) {
      setTimeout(() => router.push({ name: 'report', params: { id: contractId } }), 800)
    } else if (status.status === 'FAILED') {
      errorMsg.value = status.errorMessage || 'Đã xảy ra lỗi trong quá trình xử lý.'
    }
  })
})

onBeforeUnmount(() => store.stopPolling())

const steps = [
  { key: 'UPLOADED',   label: 'Upload file'          },
  { key: 'EXTRACTING', label: 'Trích xuất văn bản'   },
  { key: 'ANALYZING',  label: 'Phân tích AI'         },
  { key: 'ANALYZED',   label: 'Báo cáo sẵn sàng'     },
]

const stepOrder = ['UPLOADED','EXTRACTING','EXTRACTED','ANALYZING','ANALYZED']

function stepState(key: string): 'done' | 'active' | 'pending' {
  const cur = stepOrder.indexOf(currentStatus.value)
  const pos = stepOrder.indexOf(key)
  if (key === 'EXTRACTING' || key === 'EXTRACTED') {
    // merge into one display step
  }
  if (pos < cur) return 'done'
  if (pos === cur) return 'active'
  return 'pending'
}
</script>

<template>
  <div class="max-w-lg mx-auto px-4 py-16 text-center animate-fade-in">

    <!-- Spinner or error icon -->
    <div class="relative inline-block mb-8">
      <div v-if="currentStatus !== 'FAILED'" class="w-24 h-24 rounded-full bg-red-50 flex items-center justify-center">
        <Shield class="w-10 h-10 text-red-500" />
        <div class="absolute inset-0 rounded-full border-4 border-red-200 border-t-red-500 animate-spin" />
      </div>
      <div v-else class="w-24 h-24 rounded-full bg-red-100 flex items-center justify-center">
        <AlertCircle class="w-12 h-12 text-red-600" />
      </div>
    </div>

    <h2 class="text-2xl font-bold text-gray-900 mb-2">
      {{ currentStatus === 'FAILED' ? 'Phân tích thất bại' : 'Đang phân tích hợp đồng…' }}
    </h2>
    <p class="text-gray-500 mb-2">{{ statusMessages[currentStatus] ?? '…' }}</p>

    <!-- Error detail -->
    <div v-if="errorMsg" class="mt-3 bg-red-50 border border-red-200 rounded-xl p-4 text-sm text-red-700">
      {{ errorMsg }}
    </div>

    <!-- Progress bar -->
    <div v-if="currentStatus !== 'FAILED'" class="mt-8 w-full bg-gray-200 rounded-full h-2">
      <div
        class="bg-red-500 h-2 rounded-full transition-all duration-700"
        :style="{ width: (progressMap[currentStatus] ?? 10) + '%' }"
      />
    </div>

    <!-- Step indicators -->
    <div class="mt-8 space-y-2">
      <div
        v-for="step in steps"
        :key="step.key"
        class="flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm transition-colors"
        :class="{
          'bg-green-50 text-green-700':  stepState(step.key) === 'done',
          'bg-red-50 text-red-700 font-medium': stepState(step.key) === 'active',
          'text-gray-400':               stepState(step.key) === 'pending',
        }"
      >
        <span class="w-5 h-5 flex-shrink-0 flex items-center justify-center rounded-full text-xs font-bold"
          :class="{
            'bg-green-500 text-white':  stepState(step.key) === 'done',
            'bg-red-500 text-white':    stepState(step.key) === 'active',
            'bg-gray-200 text-gray-400':stepState(step.key) === 'pending',
          }"
        >
          <template v-if="stepState(step.key) === 'done'">✓</template>
          <template v-else-if="stepState(step.key) === 'active'">
            <span class="w-2 h-2 bg-white rounded-full animate-pulse-fast" />
          </template>
          <template v-else>·</template>
        </span>
        {{ step.label }}
      </div>
    </div>

    <!-- Back button on failure -->
    <button
      v-if="currentStatus === 'FAILED'"
      @click="$router.push('/upload')"
      class="mt-8 px-6 py-3 bg-red-600 hover:bg-red-700 text-white rounded-xl font-medium transition-colors"
    >
      Thử lại
    </button>
  </div>
</template>

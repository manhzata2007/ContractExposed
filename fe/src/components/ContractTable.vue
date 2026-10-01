<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Eye, Trash2, FileText, Loader } from 'lucide-vue-next'
import { ElMessageBox } from 'element-plus'
import type { ContractSummary, ContractStatus } from '@/types'
import { useContractStore } from '@/stores/contractStore'
import RiskScoreBadge from './RiskScoreBadge.vue'

const props = defineProps<{ contracts: ContractSummary[] }>()
const emit  = defineEmits<{ (e: 'refresh'): void }>()

const router = useRouter()
const store  = useContractStore()

function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

const statusConfig: Record<ContractStatus, { label: string; class: string }> = {
  UPLOADED:   { label: 'Đã upload',    class: 'bg-gray-100 text-gray-600'   },
  EXTRACTING: { label: 'Trích xuất…',  class: 'bg-blue-100 text-blue-600'   },
  EXTRACTED:  { label: 'Đã trích xuất',class: 'bg-blue-100 text-blue-700'   },
  ANALYZING:  { label: 'Đang phân tích',class:'bg-yellow-100 text-yellow-700'},
  ANALYZED:   { label: 'Đã phân tích', class: 'bg-green-100 text-green-700' },
  FAILED:     { label: 'Thất bại',     class: 'bg-red-100 text-red-600'     },
}

function isProcessing(status: ContractStatus) {
  return ['UPLOADED','EXTRACTING','EXTRACTED','ANALYZING'].includes(status)
}

function viewReport(c: ContractSummary) {
  if (c.status === 'ANALYZED') {
    router.push({ name: 'report', params: { id: c.id } })
  } else if (isProcessing(c.status)) {
    router.push({ name: 'analyzing', params: { id: c.id } })
  }
}

async function confirmDelete(c: ContractSummary) {
  await ElMessageBox.confirm(
    `Xóa hợp đồng "${c.originalFileName}"? Thao tác này không thể hoàn tác.`,
    'Xác nhận xóa',
    { confirmButtonText: 'Xóa', cancelButtonText: 'Hủy', type: 'warning' }
  )
  await store.removeContract(c.id)
  emit('refresh')
}
</script>

<template>
  <div class="overflow-hidden rounded-xl border border-gray-200 shadow-sm bg-white">
    <table class="w-full text-sm">
      <thead class="bg-gray-50 border-b border-gray-200">
        <tr>
          <th class="text-left px-4 py-3 font-medium text-gray-500">File</th>
          <th class="text-left px-4 py-3 font-medium text-gray-500 hidden sm:table-cell">Kích thước</th>
          <th class="text-left px-4 py-3 font-medium text-gray-500">Trạng thái</th>
          <th class="text-left px-4 py-3 font-medium text-gray-500 hidden md:table-cell">Rủi ro</th>
          <th class="text-left px-4 py-3 font-medium text-gray-500 hidden lg:table-cell">Ngày upload</th>
          <th class="text-right px-4 py-3 font-medium text-gray-500">Thao tác</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-100">
        <tr
          v-for="c in contracts"
          :key="c.id"
          class="hover:bg-gray-50 transition-colors"
        >
          <!-- File name -->
          <td class="px-4 py-3">
            <div class="flex items-center gap-2">
              <FileText class="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span class="font-medium text-gray-800 truncate max-w-[180px]">{{ c.originalFileName }}</span>
              <span class="text-xs text-gray-400 bg-gray-100 px-1.5 py-0.5 rounded">{{ c.fileType }}</span>
            </div>
          </td>
          <!-- Size -->
          <td class="px-4 py-3 text-gray-500 hidden sm:table-cell">{{ formatSize(c.fileSize) }}</td>
          <!-- Status -->
          <td class="px-4 py-3">
            <span class="inline-flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-medium" :class="statusConfig[c.status].class">
              <Loader v-if="isProcessing(c.status)" class="w-3 h-3 animate-spin" />
              {{ statusConfig[c.status].label }}
            </span>
          </td>
          <!-- Risk -->
          <td class="px-4 py-3 hidden md:table-cell">
            <RiskScoreBadge
              v-if="c.riskLevel && c.riskLevel !== 'UNKNOWN'"
              :risk-level="c.riskLevel"
              :score="c.overallRiskScore"
              size="sm"
            />
            <span v-else class="text-xs text-gray-400">–</span>
          </td>
          <!-- Date -->
          <td class="px-4 py-3 text-gray-500 hidden lg:table-cell text-xs">{{ formatDate(c.createdAt) }}</td>
          <!-- Actions -->
          <td class="px-4 py-3">
            <div class="flex items-center justify-end gap-1">
              <button
                v-if="c.status === 'ANALYZED' || isProcessing(c.status)"
                @click="viewReport(c)"
                class="p-1.5 hover:bg-blue-50 text-blue-500 hover:text-blue-700 rounded-lg transition-colors"
                :title="c.status === 'ANALYZED' ? 'Xem báo cáo' : 'Xem tiến trình'"
              >
                <Eye class="w-4 h-4" />
              </button>
              <button
                @click="confirmDelete(c)"
                class="p-1.5 hover:bg-red-50 text-gray-400 hover:text-red-500 rounded-lg transition-colors"
                title="Xóa"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Empty state -->
    <div v-if="!contracts.length" class="py-16 text-center">
      <FileText class="w-12 h-12 text-gray-200 mx-auto mb-3" />
      <p class="text-gray-500 font-medium">Chưa có hợp đồng nào</p>
      <p class="text-sm text-gray-400 mt-1">Upload hợp đồng đầu tiên của bạn</p>
    </div>
  </div>
</template>

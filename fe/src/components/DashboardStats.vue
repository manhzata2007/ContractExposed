<script setup lang="ts">
import { onMounted } from 'vue'
import { FileText, CheckCircle, Clock, XCircle, AlertTriangle } from 'lucide-vue-next'
import { useContractStore } from '@/stores/contractStore'

const store = useContractStore()
onMounted(() => store.fetchStats())

const statCards = [
  { key: 'totalContracts',    label: 'Tổng hợp đồng',    icon: FileText,     color: 'bg-blue-50 text-blue-600'   },
  { key: 'analyzedContracts', label: 'Đã phân tích',      icon: CheckCircle,  color: 'bg-green-50 text-green-600' },
  { key: 'pendingContracts',  label: 'Đang xử lý',        icon: Clock,        color: 'bg-yellow-50 text-yellow-600'},
  { key: 'failedContracts',   label: 'Thất bại',          icon: XCircle,      color: 'bg-red-50 text-red-600'     },
]

const riskCards = [
  { key: 'criticalRiskContracts', label: 'Nghiêm trọng', color: 'text-red-600',    dot: 'bg-red-500'    },
  { key: 'highRiskContracts',     label: 'Rủi ro cao',   color: 'text-orange-500', dot: 'bg-orange-400' },
  { key: 'mediumRiskContracts',   label: 'Rủi ro vừa',   color: 'text-yellow-600', dot: 'bg-yellow-400' },
  { key: 'lowRiskContracts',      label: 'Rủi ro thấp',  color: 'text-green-600',  dot: 'bg-green-400'  },
]

function val(key: string): number {
  return (store.dashboardStats as Record<string, number> | null)?.[key] ?? 0
}
</script>

<template>
  <div class="space-y-4">
    <!-- Processing stats -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
      <div
        v-for="card in statCards"
        :key="card.key"
        class="bg-white rounded-xl border border-gray-200 p-4 flex items-center gap-3 shadow-sm"
      >
        <div class="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0" :class="card.color">
          <component :is="card.icon" class="w-5 h-5" />
        </div>
        <div>
          <p class="text-2xl font-bold text-gray-900">{{ val(card.key) }}</p>
          <p class="text-xs text-gray-500">{{ card.label }}</p>
        </div>
      </div>
    </div>

    <!-- Risk level breakdown -->
    <div class="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
      <div class="flex items-center gap-2 mb-3">
        <AlertTriangle class="w-4 h-4 text-gray-400" />
        <p class="text-sm font-medium text-gray-700">Phân loại theo mức rủi ro</p>
      </div>
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div
          v-for="card in riskCards"
          :key="card.key"
          class="flex items-center gap-2"
        >
          <span class="w-2 h-2 rounded-full flex-shrink-0" :class="card.dot" />
          <span class="text-sm font-semibold" :class="card.color">{{ val(card.key) }}</span>
          <span class="text-xs text-gray-400">{{ card.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

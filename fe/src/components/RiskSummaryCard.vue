<script setup lang="ts">
import { computed } from 'vue'
import type { AnalysisReport } from '@/types'
import { RISK_LEVEL_CONFIG } from '@/types'
import RiskScoreBadge from './RiskScoreBadge.vue'
import { AlertTriangle, FileText, Clock, Cpu } from 'lucide-vue-next'

const props = defineProps<{ report: AnalysisReport }>()

const cfg = computed(() => RISK_LEVEL_CONFIG[props.report.riskLevel])

const scoreColor = computed(() => {
  const s = props.report.overallRiskScore
  if (s >= 80) return 'text-red-600'
  if (s >= 60) return 'text-orange-500'
  if (s >= 40) return 'text-yellow-500'
  return 'text-green-500'
})

const durationSec = computed(() =>
  props.report.analysisDurationMs
    ? (props.report.analysisDurationMs / 1000).toFixed(1)
    : null
)
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">

    <!-- Risk level banner -->
    <div
      class="px-5 py-4 flex items-center justify-between"
      :class="cfg.bg"
    >
      <div class="flex items-center gap-3">
        <span class="text-3xl">{{ cfg.icon }}</span>
        <div>
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Mức rủi ro tổng thể</p>
          <RiskScoreBadge :risk-level="report.riskLevel" :score="report.overallRiskScore" size="lg" />
        </div>
      </div>
      <!-- Gauge circle -->
      <div class="relative w-16 h-16">
        <svg class="w-16 h-16 -rotate-90" viewBox="0 0 36 36">
          <circle cx="18" cy="18" r="15.9" fill="none" stroke="#e5e7eb" stroke-width="3" />
          <circle
            cx="18" cy="18" r="15.9" fill="none" stroke-width="3"
            :stroke="report.overallRiskScore >= 80 ? '#ef4444' : report.overallRiskScore >= 60 ? '#f97316' : report.overallRiskScore >= 40 ? '#eab308' : '#22c55e'"
            stroke-linecap="round"
            :stroke-dasharray="`${report.overallRiskScore} 100`"
          />
        </svg>
        <span class="absolute inset-0 flex items-center justify-center text-sm font-bold" :class="scoreColor">
          {{ report.overallRiskScore }}
        </span>
      </div>
    </div>

    <!-- Finding counts -->
    <div class="grid grid-cols-4 divide-x divide-gray-100 border-t border-gray-100">
      <div class="p-3 text-center">
        <p class="text-xl font-bold text-red-600">{{ report.criticalCount }}</p>
        <p class="text-xs text-gray-500 mt-0.5">Nguy hiểm</p>
      </div>
      <div class="p-3 text-center">
        <p class="text-xl font-bold text-orange-500">{{ report.highCount }}</p>
        <p class="text-xs text-gray-500 mt-0.5">Cao</p>
      </div>
      <div class="p-3 text-center">
        <p class="text-xl font-bold text-yellow-500">{{ report.mediumCount }}</p>
        <p class="text-xs text-gray-500 mt-0.5">Vừa</p>
      </div>
      <div class="p-3 text-center">
        <p class="text-xl font-bold text-green-500">{{ report.lowCount }}</p>
        <p class="text-xs text-gray-500 mt-0.5">Thấp</p>
      </div>
    </div>

    <!-- Summary text -->
    <div class="px-5 py-4 border-t border-gray-100">
      <div class="flex items-start gap-2">
        <AlertTriangle class="w-4 h-4 text-gray-400 flex-shrink-0 mt-0.5" />
        <p class="text-sm text-gray-700 leading-relaxed">{{ report.summary }}</p>
      </div>
    </div>

    <!-- Meta info -->
    <div class="px-5 py-3 bg-gray-50 border-t border-gray-100 flex flex-wrap gap-4 text-xs text-gray-400">
      <span class="flex items-center gap-1">
        <FileText class="w-3 h-3" /> {{ report.originalFileName }}
      </span>
      <span v-if="durationSec" class="flex items-center gap-1">
        <Clock class="w-3 h-3" /> Phân tích trong {{ durationSec }}s
      </span>
      <span v-if="report.modelUsed" class="flex items-center gap-1">
        <Cpu class="w-3 h-3" /> {{ report.modelUsed }}
      </span>
    </div>
  </div>
</template>

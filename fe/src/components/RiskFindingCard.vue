<script setup lang="ts">
import { ref } from 'vue'
import { ChevronDown, ChevronUp, FileWarning, Lightbulb } from 'lucide-vue-next'
import type { RiskFinding } from '@/types'
import { RISK_TYPE_LABELS } from '@/types'
import SeverityBadge from './SeverityBadge.vue'

defineProps<{ finding: RiskFinding; index: number }>()
const expanded = ref(false)
</script>

<template>
  <div
    class="rounded-xl border transition-all duration-200 overflow-hidden"
    :class="{
      'border-red-200 shadow-sm':    finding.severity === 'CRITICAL',
      'border-orange-200':           finding.severity === 'HIGH',
      'border-yellow-200':           finding.severity === 'MEDIUM',
      'border-green-200':            finding.severity === 'LOW',
    }"
  >
    <!-- Header (always visible) -->
    <button
      class="w-full text-left p-4 flex items-start gap-3 hover:bg-gray-50/50 transition-colors"
      @click="expanded = !expanded"
    >
      <!-- Severity indicator bar -->
      <div
        class="w-1 self-stretch rounded-full flex-shrink-0"
        :class="{
          'bg-red-500':    finding.severity === 'CRITICAL',
          'bg-orange-400': finding.severity === 'HIGH',
          'bg-yellow-400': finding.severity === 'MEDIUM',
          'bg-green-400':  finding.severity === 'LOW',
        }"
      />

      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2 flex-wrap">
          <SeverityBadge :severity="finding.severity" />
          <span class="text-xs text-gray-400 bg-gray-100 px-2 py-0.5 rounded">
            {{ RISK_TYPE_LABELS[finding.riskType] }}
          </span>
          <span v-if="finding.pageNumber" class="text-xs text-gray-400">
            Trang {{ finding.pageNumber }}
          </span>
        </div>
        <p class="font-semibold text-gray-800 mt-1.5 text-sm">
          {{ index + 1 }}. {{ finding.clauseTitle }}
        </p>
        <!-- Snippet preview when collapsed -->
        <p v-if="!expanded" class="text-xs text-gray-500 mt-1 line-clamp-2 italic">
          "{{ finding.originalText }}"
        </p>
      </div>

      <component :is="expanded ? ChevronUp : ChevronDown" class="w-4 h-4 text-gray-400 flex-shrink-0 mt-0.5" />
    </button>

    <!-- Expanded details -->
    <div v-if="expanded" class="px-4 pb-4 space-y-3 border-t border-gray-100">

      <!-- Original clause text -->
      <div>
        <p class="text-xs font-semibold text-gray-500 uppercase tracking-wide mt-3 mb-1.5">Điều khoản gốc</p>
        <blockquote class="text-sm text-gray-700 bg-gray-50 border-l-4 border-gray-300 pl-3 py-2 pr-2 rounded-r-lg italic leading-relaxed">
          "{{ finding.originalText }}"
        </blockquote>
      </div>

      <!-- Explanation -->
      <div class="flex gap-2">
        <FileWarning class="w-4 h-4 text-red-500 flex-shrink-0 mt-0.5" />
        <div>
          <p class="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1">Vì sao rủi ro?</p>
          <p class="text-sm text-gray-700 leading-relaxed">{{ finding.explanation }}</p>
        </div>
      </div>

      <!-- Recommendation -->
      <div v-if="finding.recommendation" class="flex gap-2 bg-blue-50 rounded-lg p-3">
        <Lightbulb class="w-4 h-4 text-blue-500 flex-shrink-0 mt-0.5" />
        <div>
          <p class="text-xs font-semibold text-blue-600 uppercase tracking-wide mb-1">Đề xuất đàm phán</p>
          <p class="text-sm text-blue-800 leading-relaxed">{{ finding.recommendation }}</p>
        </div>
      </div>

    </div>
  </div>
</template>

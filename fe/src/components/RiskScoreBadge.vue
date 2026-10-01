<script setup lang="ts">
import { computed } from 'vue'
import type { RiskLevel } from '@/types'
import { RISK_LEVEL_CONFIG } from '@/types'

const props = defineProps<{
  riskLevel: RiskLevel
  score?: number
  size?: 'sm' | 'md' | 'lg'
}>()

const cfg  = computed(() => RISK_LEVEL_CONFIG[props.riskLevel])
const size = computed(() => props.size ?? 'md')
</script>

<template>
  <span
    class="inline-flex items-center gap-1 font-semibold rounded-full border"
    :class="[
      cfg.bg,
      cfg.color,
      size === 'sm' ? 'px-2 py-0.5 text-xs' : '',
      size === 'md' ? 'px-3 py-1 text-sm'   : '',
      size === 'lg' ? 'px-4 py-1.5 text-base': '',
    ]"
  >
    <span>{{ cfg.icon }}</span>
    <span>{{ cfg.label }}</span>
    <span v-if="score !== undefined" class="opacity-75">({{ score }})</span>
  </span>
</template>

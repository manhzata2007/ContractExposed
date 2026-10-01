<script setup lang="ts">
import { computed } from 'vue'
import type { Severity } from '@/types'
import { SEVERITY_CONFIG } from '@/types'

const props = defineProps<{ severity: Severity }>()
const cfg   = computed(() => SEVERITY_CONFIG[props.severity])
</script>

<template>
  <span
    class="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold border"
    :class="[cfg.bg, cfg.color, cfg.border]"
  >
    {{ cfg.label }}
  </span>
</template>

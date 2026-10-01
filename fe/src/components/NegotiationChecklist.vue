<script setup lang="ts">
import { computed } from 'vue'
import { CheckSquare, Square, ClipboardList } from 'lucide-vue-next'
import type { NegotiationChecklistItem } from '@/types'
import { useContractStore } from '@/stores/contractStore'

const props  = defineProps<{ items: NegotiationChecklistItem[] }>()
const store  = useContractStore()

const completedCount = computed(() => props.items.filter((i) => i.isCompleted).length)
const progressPct    = computed(() =>
  props.items.length ? Math.round((completedCount.value / props.items.length) * 100) : 0
)

const priorityConfig = {
  HIGH:   { label: 'Ưu tiên cao',   color: 'text-red-600',    dot: 'bg-red-500'    },
  MEDIUM: { label: 'Ưu tiên vừa',   color: 'text-yellow-600', dot: 'bg-yellow-400' },
  LOW:    { label: 'Ưu tiên thấp',  color: 'text-gray-500',   dot: 'bg-gray-300'   },
}

async function toggle(item: NegotiationChecklistItem) {
  await store.toggleChecklist(item.id, !item.isCompleted)
}
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">

    <!-- Header -->
    <div class="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
      <div class="flex items-center gap-2">
        <ClipboardList class="w-5 h-5 text-blue-600" />
        <h3 class="font-semibold text-gray-900">Checklist Đàm Phán</h3>
      </div>
      <span class="text-sm text-gray-500">{{ completedCount }}/{{ items.length }}</span>
    </div>

    <!-- Progress bar -->
    <div class="px-5 py-3 bg-gray-50 border-b border-gray-100">
      <div class="flex justify-between text-xs text-gray-500 mb-1.5">
        <span>Tiến độ</span>
        <span class="font-medium text-gray-700">{{ progressPct }}%</span>
      </div>
      <div class="w-full bg-gray-200 rounded-full h-1.5">
        <div
          class="h-1.5 rounded-full transition-all duration-500"
          :class="progressPct === 100 ? 'bg-green-500' : 'bg-blue-500'"
          :style="{ width: progressPct + '%' }"
        />
      </div>
    </div>

    <!-- Items -->
    <ul class="divide-y divide-gray-100">
      <li
        v-for="item in items"
        :key="item.id"
        class="flex items-start gap-3 px-5 py-3.5 hover:bg-gray-50 transition-colors cursor-pointer"
        @click="toggle(item)"
      >
        <button class="flex-shrink-0 mt-0.5 focus:outline-none" :aria-label="item.isCompleted ? 'Bỏ đánh dấu' : 'Đánh dấu hoàn thành'">
          <CheckSquare v-if="item.isCompleted" class="w-5 h-5 text-green-500" />
          <Square      v-else                  class="w-5 h-5 text-gray-300" />
        </button>

        <div class="flex-1 min-w-0">
          <p
            class="text-sm leading-snug transition-colors"
            :class="item.isCompleted ? 'line-through text-gray-400' : 'text-gray-700'"
          >
            {{ item.itemText }}
          </p>
          <div class="flex items-center gap-2 mt-1">
            <span
              v-if="item.priority"
              class="flex items-center gap-1 text-xs"
              :class="priorityConfig[item.priority].color"
            >
              <span class="w-1.5 h-1.5 rounded-full inline-block" :class="priorityConfig[item.priority].dot" />
              {{ priorityConfig[item.priority].label }}
            </span>
            <span v-if="item.category" class="text-xs text-gray-400">· {{ item.category }}</span>
          </div>
        </div>
      </li>
    </ul>

    <!-- Empty state -->
    <div v-if="!items.length" class="px-5 py-8 text-center text-gray-400 text-sm">
      Không có mục nào trong checklist.
    </div>
  </div>
</template>

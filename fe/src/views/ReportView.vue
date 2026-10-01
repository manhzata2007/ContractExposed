<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeft, Download, List, FileText, AlertTriangle, ClipboardList
} from 'lucide-vue-next'
import { useContractStore } from '@/stores/contractStore'
import RiskSummaryCard      from '@/components/RiskSummaryCard.vue'
import RiskFindingCard      from '@/components/RiskFindingCard.vue'
import NegotiationChecklist from '@/components/NegotiationChecklist.vue'
import PdfViewer            from '@/components/PdfViewer.vue'
import { getPdfHighlightedUrl, getPdfOriginalUrl } from '@/api/contracts'

const props  = defineProps<{ id: string }>()
const router = useRouter()
const store  = useContractStore()

const contractId    = Number(props.id)
const activeTab     = ref<'findings' | 'checklist' | 'pdf'>('findings')
const highlightPage = ref<number | undefined>(undefined)
const pdfUrl        = ref('')
const showHighlight = ref(true)

onMounted(async () => {
  await store.fetchReport(contractId)
  if (store.currentReport?.fileType === 'PDF') {
    pdfUrl.value = showHighlight.value
      ? getPdfHighlightedUrl(contractId)
      : getPdfOriginalUrl(contractId)
  }
})

const report = computed(() => store.currentReport)

function jumpToPdfPage(page?: number) {
  if (!page) return
  highlightPage.value = page
  activeTab.value     = 'pdf'
}

function togglePdfMode() {
  showHighlight.value = !showHighlight.value
  pdfUrl.value = showHighlight.value
    ? getPdfHighlightedUrl(contractId)
    : getPdfOriginalUrl(contractId)
}

function downloadHighlighted() {
  const url = getPdfHighlightedUrl(contractId)
  const a   = document.createElement('a')
  a.href    = url
  a.download = `highlighted_contract_${contractId}.pdf`
  a.click()
}

const filteredFindings = computed(() => {
  if (!report.value) return []
  return [...report.value.findings].sort((a, b) => b.severityScore - a.severityScore)
})
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 py-6 animate-fade-in">

    <!-- Header -->
    <div class="flex items-center gap-4 mb-6">
      <button
        @click="router.back()"
        class="p-2 hover:bg-gray-100 rounded-xl text-gray-500 transition-colors"
        aria-label="Quay lại"
      >
        <ArrowLeft class="w-5 h-5" />
      </button>
      <div class="flex-1 min-w-0">
        <h1 class="text-xl font-bold text-gray-900 truncate">
          {{ report?.originalFileName ?? 'Báo cáo phân tích' }}
        </h1>
        <p class="text-sm text-gray-500">
          {{ report?.totalFindings }} vấn đề được phát hiện
        </p>
      </div>
      <button
        v-if="report?.fileType === 'PDF'"
        @click="downloadHighlighted"
        class="hidden sm:flex items-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-xl text-sm font-medium transition-colors"
      >
        <Download class="w-4 h-4" />
        Tải PDF có highlight
      </button>
    </div>

    <!-- Loading -->
    <div v-if="store.isLoadingReport" class="flex justify-center py-20">
      <div class="w-10 h-10 border-2 border-red-300 border-t-red-600 rounded-full animate-spin" />
    </div>

    <template v-else-if="report">
      <!-- Risk summary -->
      <div class="mb-6">
        <RiskSummaryCard :report="report" />
      </div>

      <!-- Tab layout -->
      <div class="flex gap-1 p-1 bg-gray-100 rounded-xl mb-5 w-fit">
        <button
          v-for="tab in [
            { key: 'findings',  label: 'Điều khoản rủi ro', icon: AlertTriangle, count: report.totalFindings },
            { key: 'checklist', label: 'Checklist đàm phán', icon: ClipboardList, count: report.checklistItems.length },
            { key: 'pdf',       label: 'Xem PDF',           icon: FileText,      count: null },
          ]"
          :key="tab.key"
          @click="activeTab = tab.key as typeof activeTab"
          class="flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium transition-all"
          :class="activeTab === tab.key
            ? 'bg-white text-gray-900 shadow-sm'
            : 'text-gray-500 hover:text-gray-700'"
        >
          <component :is="tab.icon" class="w-4 h-4" />
          {{ tab.label }}
          <span v-if="tab.count !== null" class="bg-red-100 text-red-600 text-xs px-1.5 py-0.5 rounded-full font-semibold">
            {{ tab.count }}
          </span>
        </button>
      </div>

      <!-- Tab: Findings -->
      <div v-if="activeTab === 'findings'" class="space-y-3 animate-slide-up">
        <RiskFindingCard
          v-for="(finding, i) in filteredFindings"
          :key="finding.id"
          :finding="finding"
          :index="i"
          @click.native="jumpToPdfPage(finding.pageNumber)"
        />
        <p v-if="!filteredFindings.length" class="text-center text-gray-400 py-10">
          Không tìm thấy vấn đề nào.
        </p>
      </div>

      <!-- Tab: Checklist -->
      <div v-else-if="activeTab === 'checklist'" class="animate-slide-up">
        <NegotiationChecklist :items="report.checklistItems" />
      </div>

      <!-- Tab: PDF Viewer -->
      <div v-else-if="activeTab === 'pdf'" class="animate-slide-up">
        <div v-if="report.fileType === 'PDF'">
          <div class="flex items-center gap-2 mb-3">
            <button
              @click="togglePdfMode"
              class="px-3 py-1.5 text-xs rounded-lg border transition-colors"
              :class="showHighlight
                ? 'bg-red-50 text-red-600 border-red-200'
                : 'bg-gray-100 text-gray-600 border-gray-200'"
            >
              {{ showHighlight ? '🎨 Đang xem: có highlight' : '📄 Đang xem: bản gốc' }}
            </button>
          </div>
          <PdfViewer :pdf-url="pdfUrl" :highlight-page="highlightPage" />
        </div>
        <div v-else class="bg-white rounded-xl border border-gray-200 p-8 text-center text-gray-500">
          <FileText class="w-12 h-12 text-gray-200 mx-auto mb-3" />
          <p>Xem PDF chỉ khả dụng với file PDF.</p>
          <p class="text-sm mt-1">File của bạn là {{ report.fileType }}.</p>
        </div>
      </div>
    </template>

  </div>
</template>

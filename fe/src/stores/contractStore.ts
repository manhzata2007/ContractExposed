import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import {
  uploadContract,
  listContracts,
  getContractStatus,
  getReport,
  deleteContract,
  getDashboardStats,
  toggleChecklistItem,
} from '@/api/contracts'
import type {
  ContractSummary,
  ContractUploadResponse,
  ContractStatusResponse,
  AnalysisReport,
  DashboardStats,
  PageResponse,
} from '@/types'

// ─── Polling config ───────────────────────────────────────────
const POLL_INTERVAL_MS  = 2500
const POLL_MAX_ATTEMPTS = 120 // 5 minutes max

export const useContractStore = defineStore('contracts', () => {

  // ── State ──────────────────────────────────────────────────
  const contracts         = ref<ContractSummary[]>([])
  const currentReport     = ref<AnalysisReport | null>(null)
  const dashboardStats    = ref<DashboardStats | null>(null)
  const uploadProgress    = ref(0)
  const isUploading       = ref(false)
  const isLoadingList     = ref(false)
  const isLoadingReport   = ref(false)
  const isPolling         = ref(false)
  const pollingContractId = ref<number | null>(null)
  const pollingStatus     = ref<ContractStatusResponse | null>(null)
  const totalElements     = ref(0)
  const totalPages        = ref(0)
  const currentPage       = ref(0)
  const pageSize          = ref(10)

  // ── Computed ───────────────────────────────────────────────
  const hasContracts = computed(() => contracts.value.length > 0)

  const criticalFindings = computed(() =>
    currentReport.value?.findings.filter((f) => f.severity === 'CRITICAL') ?? []
  )

  const highFindings = computed(() =>
    currentReport.value?.findings.filter((f) => f.severity === 'HIGH') ?? []
  )

  const pendingChecklistCount = computed(() =>
    currentReport.value?.checklistItems.filter((i) => !i.isCompleted).length ?? 0
  )

  // ── Actions ────────────────────────────────────────────────

  async function upload(file: File): Promise<ContractUploadResponse> {
    isUploading.value   = true
    uploadProgress.value = 0
    try {
      const result = await uploadContract(file, (pct) => {
        uploadProgress.value = pct
      })
      ElMessage.success(`Đã upload "${file.name}" thành công. Đang bắt đầu phân tích...`)
      return result
    } finally {
      isUploading.value = false
    }
  }

  async function fetchList(page = 0, size = 10): Promise<void> {
    isLoadingList.value = true
    try {
      const result: PageResponse<ContractSummary> = await listContracts(page, size)
      contracts.value   = result.content
      totalElements.value = result.totalElements
      totalPages.value  = result.totalPages
      currentPage.value = result.number
      pageSize.value    = result.size
    } finally {
      isLoadingList.value = false
    }
  }

  async function fetchReport(contractId: number): Promise<void> {
    isLoadingReport.value = true
    currentReport.value   = null
    try {
      currentReport.value = await getReport(contractId)
    } finally {
      isLoadingReport.value = false
    }
  }

  async function fetchStats(): Promise<void> {
    dashboardStats.value = await getDashboardStats()
  }

  async function removeContract(contractId: number): Promise<void> {
    await deleteContract(contractId)
    contracts.value = contracts.value.filter((c) => c.id !== contractId)
    ElMessage.success('Đã xóa hợp đồng.')
    // Refresh stats
    await fetchStats()
  }

  async function toggleChecklist(itemId: number, isCompleted: boolean): Promise<void> {
    if (!currentReport.value) return
    const updated = await toggleChecklistItem(itemId, isCompleted)
    const idx = currentReport.value.checklistItems.findIndex((i) => i.id === itemId)
    if (idx !== -1) {
      currentReport.value.checklistItems[idx] = updated
    }
  }

  // ── Polling ────────────────────────────────────────────────

  function startPolling(contractId: number, onComplete: (status: ContractStatusResponse) => void): void {
    if (isPolling.value) stopPolling()

    isPolling.value         = true
    pollingContractId.value = contractId
    pollingStatus.value     = null
    let attempts            = 0

    const timer = setInterval(async () => {
      attempts++
      try {
        const status = await getContractStatus(contractId)
        pollingStatus.value = status

        if (status.reportReady) {
          clearInterval(timer)
          isPolling.value = false
          ElNotification({
            title: 'Phân tích hoàn tất!',
            message: 'Báo cáo rủi ro đã sẵn sàng.',
            type: 'success',
            duration: 4000,
          })
          onComplete(status)
          return
        }

        if (status.status === 'FAILED') {
          clearInterval(timer)
          isPolling.value = false
          ElNotification({
            title: 'Phân tích thất bại',
            message: status.errorMessage || 'Vui lòng thử lại.',
            type: 'error',
            duration: 6000,
          })
          onComplete(status)
          return
        }

        if (attempts >= POLL_MAX_ATTEMPTS) {
          clearInterval(timer)
          isPolling.value = false
          ElMessage.warning('Quá thời gian chờ. Vui lòng làm mới trang.')
        }
      } catch {
        // Network errors during polling — keep trying
        if (attempts >= POLL_MAX_ATTEMPTS) {
          clearInterval(timer)
          isPolling.value = false
        }
      }
    }, POLL_INTERVAL_MS)
  }

  function stopPolling(): void {
    isPolling.value         = false
    pollingContractId.value = null
  }

  function clearReport(): void {
    currentReport.value = null
  }

  return {
    // state
    contracts,
    currentReport,
    dashboardStats,
    uploadProgress,
    isUploading,
    isLoadingList,
    isLoadingReport,
    isPolling,
    pollingContractId,
    pollingStatus,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    // computed
    hasContracts,
    criticalFindings,
    highFindings,
    pendingChecklistCount,
    // actions
    upload,
    fetchList,
    fetchReport,
    fetchStats,
    removeContract,
    toggleChecklist,
    startPolling,
    stopPolling,
    clearReport,
  }
})

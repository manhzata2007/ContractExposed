import api from './axios'
import type {
  ApiResponse,
  PageResponse,
  ContractSummary,
  ContractUploadResponse,
  ContractStatusResponse,
  AnalysisReport,
  DashboardStats,
  NegotiationChecklistItem,
} from '@/types'

// ── Upload ────────────────────────────────────────────────────────────────────
export async function uploadContract(
  file: File,
  onProgress?: (pct: number) => void
): Promise<ContractUploadResponse> {
  const form = new FormData()
  form.append('file', file)

  const res = await api.post<ApiResponse<ContractUploadResponse>>(
    '/contracts/upload',
    form,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (onProgress && e.total) {
          onProgress(Math.round((e.loaded * 100) / e.total))
        }
      },
    }
  )
  return res.data.data
}

// ── List ──────────────────────────────────────────────────────────────────────
export async function listContracts(
  page = 0,
  size = 10
): Promise<PageResponse<ContractSummary>> {
  const res = await api.get<ApiResponse<PageResponse<ContractSummary>>>(
    '/contracts',
    { params: { page, size } }
  )
  return res.data.data
}

// ── Status ────────────────────────────────────────────────────────────────────
export async function getContractStatus(id: number): Promise<ContractStatusResponse> {
  const res = await api.get<ApiResponse<ContractStatusResponse>>(
    `/contracts/${id}/status`
  )
  return res.data.data
}

// ── Report ────────────────────────────────────────────────────────────────────
export async function getReport(id: number): Promise<AnalysisReport> {
  const res = await api.get<ApiResponse<AnalysisReport>>(
    `/contracts/${id}/report`
  )
  return res.data.data
}

// ── Delete ────────────────────────────────────────────────────────────────────
export async function deleteContract(id: number): Promise<void> {
  await api.delete(`/contracts/${id}`)
}

// ── Dashboard stats ───────────────────────────────────────────────────────────
export async function getDashboardStats(): Promise<DashboardStats> {
  const res = await api.get<ApiResponse<DashboardStats>>('/contracts/stats')
  return res.data.data
}

// ── Checklist toggle ──────────────────────────────────────────────────────────
export async function toggleChecklistItem(
  itemId: number,
  isCompleted: boolean
): Promise<NegotiationChecklistItem> {
  const res = await api.patch<ApiResponse<NegotiationChecklistItem>>(
    `/checklist/${itemId}`,
    { isCompleted }
  )
  return res.data.data
}

// ── PDF URLs ──────────────────────────────────────────────────────────────────
export function getPdfOriginalUrl(contractId: number): string {
  return `${api.defaults.baseURL}/pdf/${contractId}/original`
}

export function getPdfHighlightedUrl(contractId: number): string {
  return `${api.defaults.baseURL}/pdf/${contractId}/highlighted`
}

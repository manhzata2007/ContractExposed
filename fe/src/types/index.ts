// ─────────────────────────────────────────────────────────────────────────────
// API wrapper
// ─────────────────────────────────────────────────────────────────────────────
export interface ApiResponse<T> {
  success: boolean
  message?: string
  data: T
  errorCode?: string
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// ─────────────────────────────────────────────────────────────────────────────
// Enums (must match Java enums exactly)
// ─────────────────────────────────────────────────────────────────────────────
export type ContractStatus =
  | 'UPLOADED'
  | 'EXTRACTING'
  | 'EXTRACTED'
  | 'ANALYZING'
  | 'ANALYZED'
  | 'FAILED'

export type FileType = 'PDF' | 'DOCX' | 'DOC' | 'IMAGE'

export type RiskLevel = 'UNKNOWN' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export type RiskType =
  | 'FINANCIAL'
  | 'LEGAL'
  | 'TERMINATION'
  | 'PRIVACY'
  | 'PENALTY'
  | 'RENEWAL'
  | 'DISPUTE'
  | 'OTHER'

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH'

// ─────────────────────────────────────────────────────────────────────────────
// Domain models
// ─────────────────────────────────────────────────────────────────────────────
export interface ContractSummary {
  id: number
  originalFileName: string
  fileType: FileType
  fileSize: number
  status: ContractStatus
  overallRiskScore?: number
  riskLevel?: RiskLevel
  createdAt: string
  updatedAt: string
}

export interface ContractUploadResponse {
  id: number
  originalFileName: string
  fileType: FileType
  fileSize: number
  status: ContractStatus
  createdAt: string
}

export interface ContractStatusResponse {
  id: number
  status: ContractStatus
  errorMessage?: string
  reportReady: boolean
}

export interface RiskFinding {
  id: number
  clauseTitle: string
  originalText: string
  riskType: RiskType
  severity: Severity
  severityScore: number
  explanation: string
  recommendation: string
  pageNumber?: number
  highlightColor: string
  sortOrder: number
}

export interface NegotiationChecklistItem {
  id: number
  itemText: string
  priority: Priority
  category?: string
  isCompleted: boolean
  sortOrder: number
}

export interface AnalysisReport {
  contractId: number
  originalFileName: string
  fileType: FileType
  fileSize: number
  contractStatus: ContractStatus
  reportId: number
  overallRiskScore: number
  riskLevel: RiskLevel
  summary: string
  modelUsed: string
  tokensUsed: number
  analysisDurationMs: number
  analyzedAt: string
  findings: RiskFinding[]
  findingsBySeverity: Record<string, number>
  findingsByType: Record<string, number>
  checklistItems: NegotiationChecklistItem[]
  totalFindings: number
  criticalCount: number
  highCount: number
  mediumCount: number
  lowCount: number
}

export interface DashboardStats {
  totalContracts: number
  analyzedContracts: number
  pendingContracts: number
  failedContracts: number
  criticalRiskContracts: number
  highRiskContracts: number
  mediumRiskContracts: number
  lowRiskContracts: number
}

// ─────────────────────────────────────────────────────────────────────────────
// UI helpers
// ─────────────────────────────────────────────────────────────────────────────
export const RISK_LEVEL_CONFIG: Record<RiskLevel, { label: string; color: string; bg: string; icon: string }> = {
  UNKNOWN:  { label: 'Chưa rõ',       color: 'text-gray-500',  bg: 'bg-gray-100',   icon: '❓' },
  LOW:      { label: 'Thấp',          color: 'text-green-600', bg: 'bg-green-50',   icon: '✅' },
  MEDIUM:   { label: 'Trung bình',    color: 'text-yellow-600',bg: 'bg-yellow-50',  icon: '⚠️' },
  HIGH:     { label: 'Cao',           color: 'text-orange-600',bg: 'bg-orange-50',  icon: '🔥' },
  CRITICAL: { label: 'Nghiêm trọng',  color: 'text-red-600',   bg: 'bg-red-50',     icon: '🚨' },
}

export const SEVERITY_CONFIG: Record<Severity, { label: string; color: string; bg: string; border: string }> = {
  LOW:      { label: 'Thấp',       color: 'text-green-700',  bg: 'bg-green-50',   border: 'border-green-200'  },
  MEDIUM:   { label: 'Vừa',        color: 'text-yellow-700', bg: 'bg-yellow-50',  border: 'border-yellow-200' },
  HIGH:     { label: 'Cao',        color: 'text-orange-700', bg: 'bg-orange-50',  border: 'border-orange-200' },
  CRITICAL: { label: 'Nguy hiểm',  color: 'text-red-700',    bg: 'bg-red-50',     border: 'border-red-200'    },
}

export const RISK_TYPE_LABELS: Record<RiskType, string> = {
  FINANCIAL:   'Tài chính',
  LEGAL:       'Pháp lý',
  TERMINATION: 'Chấm dứt HĐ',
  PRIVACY:     'Bảo mật',
  PENALTY:     'Phạt vi phạm',
  RENEWAL:     'Tự động gia hạn',
  DISPUTE:     'Tranh chấp',
  OTHER:       'Khác',
}

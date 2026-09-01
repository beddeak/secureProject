export type DocumentType =
  | 'GENERAL_RECORD'
  | 'AUTHORIZATION_REQUEST'
  | 'INCIDENT_REPORT'
  | 'RESEARCH_REPORT'
  | 'ERROR_REPORT'

export type DocumentStatus =
  | 'DRAFT'
  | 'PENDING_REVIEW'
  | 'PUBLISHED'
  | 'REJECTED'
  | 'ARCHIVED'

export type DocumentAuthor = {
  id: number
  nickname: string
  title: string | null
}

export type DocumentDepartment = {
  id: number
  code: string
  name: string
  description: string
}

export type ArchiveDocument = {
  id: number
  documentCode: string
  title: string
  documentType: DocumentType
  author: DocumentAuthor
  department: DocumentDepartment | null
  requiredClearanceLevel: number
  status: DocumentStatus
  summary: string | null
  content: string
  publishedAt: string | null
  createdAt: string
  updatedAt: string
}

export type DocumentCreateRequest = {
  documentCode: string
  title: string
  documentType: DocumentType
  departmentId: number | null
  requiredClearanceLevel: number
  summary: string | null
  content: string
}

export type DocumentUpdateRequest = {
  title: string
  requiredClearanceLevel: number
  summary: string | null
  content: string
}

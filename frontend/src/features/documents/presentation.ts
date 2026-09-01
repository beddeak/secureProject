import type { DocumentStatus, DocumentType } from './types'

export const documentTypeLabel: Record<DocumentType, string> = {
  GENERAL_RECORD: '일반 기록',
  AUTHORIZATION_REQUEST: '승인 요청서',
  INCIDENT_REPORT: '사고 보고서',
  RESEARCH_REPORT: '연구 보고서',
  ERROR_REPORT: 'ERROR 보고서',
}

export const documentStatusLabel: Record<DocumentStatus, string> = {
  PUBLISHED: '배포',
  PENDING_REVIEW: '승인 대기',
  REJECTED: '반려',
  DRAFT: '임시 저장',
  ARCHIVED: '폐기',
}

export function formatDocumentDate(value: string | null) {
  if (!value) return '-'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'

  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(date)
}

export function isErrorDocument(
  documentType: DocumentType,
  documentCode: string,
) {
  return (
    documentType === 'ERROR_REPORT' ||
    documentCode.toUpperCase().includes('ERR')
  )
}

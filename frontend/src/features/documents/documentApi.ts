import { apiRequest } from '../../api/http'
import type {
  ArchiveDocument,
  DocumentCreateRequest,
  DocumentUpdateRequest,
} from './types'

export const documentApi = {
  getDocuments(accessToken?: string) {
    return apiRequest<ArchiveDocument[]>(
      '/api/documents',
      { method: 'GET' },
      accessToken,
    )
  },

  getDocument(documentId: number, accessToken?: string) {
    return apiRequest<ArchiveDocument>(
      `/api/documents/${documentId}`,
      { method: 'GET' },
      accessToken,
    )
  },

  createDocument(request: DocumentCreateRequest, accessToken: string) {
    return apiRequest<ArchiveDocument>(
      '/api/documents',
      {
        method: 'POST',
        body: JSON.stringify(request),
      },
      accessToken,
    )
  },

  updateDocument(
    documentId: number,
    request: DocumentUpdateRequest,
    accessToken: string,
  ) {
    return apiRequest<ArchiveDocument>(
      `/api/documents/${documentId}`,
      {
        method: 'PUT',
        body: JSON.stringify(request),
      },
      accessToken,
    )
  },

  submitForReview(documentId: number, accessToken: string) {
    return apiRequest<ArchiveDocument>(
      `/api/documents/${documentId}/submit`,
      { method: 'PATCH' },
      accessToken,
    )
  },

  approveDocument(documentId: number, accessToken: string) {
    return apiRequest<ArchiveDocument>(
      `/api/documents/${documentId}/approve`,
      { method: 'PATCH' },
      accessToken,
    )
  },

  rejectDocument(documentId: number, accessToken: string) {
    return apiRequest<ArchiveDocument>(
      `/api/documents/${documentId}/reject`,
      { method: 'PATCH' },
      accessToken,
    )
  },
}

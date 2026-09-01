import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { ApiError } from '../api/http'
import EmptyTableRow from '../components/EmptyTableRow'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../features/auth/AuthContext'
import { documentApi } from '../features/documents/documentApi'
import {
  documentStatusLabel,
  documentTypeLabel,
  formatDocumentDate,
  isErrorDocument,
} from '../features/documents/presentation'
import type {
  ArchiveDocument,
  DocumentStatus,
  DocumentType,
} from '../features/documents/types'

export default function DocumentsPage() {
  const { accessToken } = useAuth()
  const [documents, setDocuments] = useState<ArchiveDocument[]>([])
  const [searchText, setSearchText] = useState('')
  const [typeFilter, setTypeFilter] = useState<DocumentType | ''>('')
  const [statusFilter, setStatusFilter] = useState<DocumentStatus | ''>('')
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let isCurrent = true

    setIsLoading(true)
    setErrorMessage(null)

    documentApi
      .getDocuments(accessToken ?? undefined)
      .then((response) => {
        if (isCurrent) setDocuments(response)
      })
      .catch((error) => {
        if (!isCurrent) return
        setErrorMessage(
          error instanceof ApiError
            ? error.message
            : '문서 목록을 불러오지 못했습니다.',
        )
      })
      .finally(() => {
        if (isCurrent) setIsLoading(false)
      })

    return () => {
      isCurrent = false
    }
  }, [accessToken, reloadKey])

  const filteredDocuments = useMemo(() => {
    const normalizedSearch = searchText.trim().toLowerCase()

    return documents.filter((document) => {
      const matchesSearch =
        !normalizedSearch ||
        document.documentCode.toLowerCase().includes(normalizedSearch) ||
        document.title.toLowerCase().includes(normalizedSearch) ||
        document.department?.name.toLowerCase().includes(normalizedSearch)
      const matchesType =
        !typeFilter || document.documentType === typeFilter
      const matchesStatus = !statusFilter || document.status === statusFilter

      return matchesSearch && matchesType && matchesStatus
    })
  }, [documents, searchText, typeFilter, statusFilter])

  return (
    <section className="archive-page">
      <PageHeader
        eyebrow="ARCHIVE INDEX / DISTRIBUTED RECORDS"
        title="문서 보관소"
        description="인가 등급에 따라 열람 가능한 기록만 표시됩니다."
        actions={
          <Link className="primary-button" to="/documents/new">
            신규 기록 작성
          </Link>
        }
      />

      <div className="archive-summary" aria-label="보관소 상태">
        <span>조회 가능 기록 {documents.length}건</span>
        <span>열람 기준: 사용자 인가 등급</span>
      </div>

      <div className="filter-bar" aria-label="문서 필터">
        <input
          type="search"
          value={searchText}
          onChange={(event) => setSearchText(event.target.value)}
          placeholder="문서번호, 제목 또는 담당 부서 검색"
        />
        <select
          value={typeFilter}
          onChange={(event) =>
            setTypeFilter(event.target.value as DocumentType | '')
          }
        >
          <option value="">모든 문서 종류</option>
          {Object.entries(documentTypeLabel).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
        <select
          value={statusFilter}
          onChange={(event) =>
            setStatusFilter(event.target.value as DocumentStatus | '')
          }
        >
          <option value="">모든 상태</option>
          {Object.entries(documentStatusLabel).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
      </div>

      {errorMessage ? (
        <div className="inline-alert error" role="alert">
          <span>{errorMessage}</span>
          <button type="button" onClick={() => setReloadKey((key) => key + 1)}>
            다시 시도
          </button>
        </div>
      ) : null}

      <div className="table-wrap">
        <table className="archive-table">
          <thead>
            <tr>
              <th>문서번호</th>
              <th>제목</th>
              <th>문서 종류</th>
              <th>담당 부서</th>
              <th>보안 등급</th>
              <th>상태</th>
              <th>수정일</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <EmptyTableRow colSpan={7} message="문서를 불러오는 중입니다." />
            ) : null}
            {!isLoading && !errorMessage && filteredDocuments.length === 0 ? (
              <EmptyTableRow colSpan={7} message="조건에 맞는 문서가 없습니다." />
            ) : null}
            {!isLoading
              ? filteredDocuments.map((document) => {
                  const errorDocument = isErrorDocument(
                    document.documentType,
                    document.documentCode,
                  )

                  return (
                    <tr
                      key={document.id}
                      className={`document-row${errorDocument ? ' is-error' : ''}`}
                    >
                      <td>
                        <Link
                          className="document-code document-link"
                          to={`/documents/${document.id}`}
                        >
                          {document.documentCode}
                        </Link>
                      </td>
                      <td>
                        <Link
                          className="document-title document-link"
                          data-text={document.title}
                          to={`/documents/${document.id}`}
                        >
                          {document.title}
                        </Link>
                      </td>
                      <td>{documentTypeLabel[document.documentType]}</td>
                      <td>{document.department?.name ?? '독립 기록'}</td>
                      <td>
                        <span
                          className={`clearance-badge level-${document.requiredClearanceLevel}`}
                        >
                          Level-{document.requiredClearanceLevel}
                        </span>
                      </td>
                      <td>
                        <span
                          className={`status-badge status-${document.status.toLowerCase()}`}
                        >
                          {documentStatusLabel[document.status]}
                        </span>
                      </td>
                      <td className="date-cell">
                        {formatDocumentDate(document.updatedAt)}
                      </td>
                    </tr>
                  )
                })
              : null}
          </tbody>
        </table>
      </div>

      <p className="archive-footnote">
        열람 및 수정 행위는 내부 감사 기록에 자동 저장됩니다.
      </p>
    </section>
  )
}

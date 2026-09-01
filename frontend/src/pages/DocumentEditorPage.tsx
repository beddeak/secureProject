import { useEffect, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../api/http'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../features/auth/AuthContext'
import { documentApi } from '../features/documents/documentApi'
import { documentTypeLabel } from '../features/documents/presentation'
import type {
  DocumentCreateRequest,
  DocumentType,
  DocumentUpdateRequest,
} from '../features/documents/types'
import { membershipApi } from '../features/memberships/membershipApi'
import type { DepartmentMembership } from '../features/memberships/types'

type DocumentEditorPageProps = {
  mode: 'create' | 'edit'
}

type DocumentFormState = {
  documentCode: string
  documentType: DocumentType
  departmentId: string
  title: string
  requiredClearanceLevel: string
  summary: string
  content: string
}

const emptyForm: DocumentFormState = {
  documentCode: '',
  documentType: 'GENERAL_RECORD',
  departmentId: '',
  title: '',
  requiredClearanceLevel: '0',
  summary: '',
  content: '',
}

export default function DocumentEditorPage({ mode }: DocumentEditorPageProps) {
  const { documentId } = useParams()
  const navigate = useNavigate()
  const { accessToken } = useAuth()
  const isCreate = mode === 'create'
  const numericDocumentId = Number(documentId)

  const [form, setForm] = useState<DocumentFormState>(emptyForm)
  const [memberships, setMemberships] = useState<DepartmentMembership[]>([])
  const [isLoading, setIsLoading] = useState(!isCreate)
  const [isSaving, setIsSaving] = useState(false)
  const [loadErrorMessage, setLoadErrorMessage] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [membershipMessage, setMembershipMessage] = useState<string | null>(null)

  useEffect(() => {
    if (!accessToken) return

    let isCurrent = true
    setLoadErrorMessage(null)
    setMembershipMessage(null)

    membershipApi
      .getMyMemberships(accessToken)
      .then((response) => {
        if (isCurrent) setMemberships(response)
      })
      .catch(() => {
        if (isCurrent) {
          setMembershipMessage(
            '소속 부서를 불러오지 못해 독립 문서만 작성할 수 있습니다.',
          )
        }
      })

    if (isCreate) {
      setForm(emptyForm)
      setIsLoading(false)
      return () => {
        isCurrent = false
      }
    }

    if (!Number.isInteger(numericDocumentId) || numericDocumentId <= 0) {
      setLoadErrorMessage('올바르지 않은 문서 번호입니다.')
      setIsLoading(false)
      return () => {
        isCurrent = false
      }
    }

    setIsLoading(true)
    documentApi
      .getDocument(numericDocumentId, accessToken)
      .then((document) => {
        if (!isCurrent) return
        setForm({
          documentCode: document.documentCode,
          documentType: document.documentType,
          departmentId: document.department?.id.toString() ?? '',
          title: document.title,
          requiredClearanceLevel:
            document.requiredClearanceLevel.toString(),
          summary: document.summary ?? '',
          content: document.content,
        })
      })
      .catch((error) => {
        if (!isCurrent) return
        setLoadErrorMessage(
          error instanceof ApiError
            ? error.message
            : '문서를 불러오지 못했습니다.',
        )
      })
      .finally(() => {
        if (isCurrent) setIsLoading(false)
      })

    return () => {
      isCurrent = false
    }
  }, [accessToken, isCreate, numericDocumentId])

  const handleChange = (
    event: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>,
  ) => {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!accessToken || isSaving) return

    const requiredClearanceLevel = Number(form.requiredClearanceLevel)
    if (
      !Number.isInteger(requiredClearanceLevel) ||
      requiredClearanceLevel < 0 ||
      requiredClearanceLevel > 10
    ) {
      setErrorMessage('필요 인가 등급은 0부터 10 사이여야 합니다.')
      return
    }

    setIsSaving(true)
    setErrorMessage(null)

    try {
      const commonRequest: DocumentUpdateRequest = {
        title: form.title.trim(),
        requiredClearanceLevel,
        summary: form.summary.trim() || null,
        content: form.content.trim(),
      }

      const savedDocument = isCreate
        ? await documentApi.createDocument(
            {
              ...commonRequest,
              documentCode: form.documentCode.trim(),
              documentType: form.documentType,
              departmentId: form.departmentId
                ? Number(form.departmentId)
                : null,
            } satisfies DocumentCreateRequest,
            accessToken,
          )
        : await documentApi.updateDocument(
            numericDocumentId,
            commonRequest,
            accessToken,
          )

      navigate(`/documents/${savedDocument.id}`, { replace: true })
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : '문서를 저장하지 못했습니다.',
      )
    } finally {
      setIsSaving(false)
    }
  }

  const handleCancel = () => {
    navigate(
      isCreate || !Number.isInteger(numericDocumentId)
        ? '/documents'
        : `/documents/${numericDocumentId}`,
    )
  }

  if (isLoading) {
    return (
      <section>
        <PageHeader eyebrow="EDIT RECORD" title="문서 불러오는 중" />
        <div className="route-loading">문서 내용을 불러오는 중입니다.</div>
      </section>
    )
  }

  if (!isCreate && loadErrorMessage) {
    return (
      <section>
        <PageHeader eyebrow="EDIT RECORD" title="문서 수정 실패" />
        <div className="inline-alert error" role="alert">
          <span>{loadErrorMessage}</span>
          <button type="button" onClick={handleCancel}>
            문서로 돌아가기
          </button>
        </div>
      </section>
    )
  }

  return (
    <section>
      <PageHeader
        eyebrow={isCreate ? 'NEW RECORD' : 'EDIT RECORD'}
        title={isCreate ? '새 문서 작성' : `${form.documentCode} 수정`}
        description={
          isCreate
            ? '문서는 초안 상태로 저장됩니다.'
            : '배포 또는 반려 문서를 수정하면 다시 초안 상태가 됩니다.'
        }
      />

      <form className="document-form" onSubmit={handleSubmit}>
        {errorMessage ? (
          <p className="form-error" role="alert">
            {errorMessage}
          </p>
        ) : null}

        {membershipMessage ? (
          <p className="form-notice">{membershipMessage}</p>
        ) : null}

        <fieldset className="form-fieldset" disabled={isSaving}>
          {isCreate ? (
            <div className="form-row">
              <label>
                문서 코드
                <input
                  name="documentCode"
                  value={form.documentCode}
                  onChange={handleChange}
                  maxLength={50}
                  placeholder="DOC-001"
                  required
                />
              </label>
              <label>
                문서 유형
                <select
                  name="documentType"
                  value={form.documentType}
                  onChange={handleChange}
                >
                  {Object.entries(documentTypeLabel).map(([value, label]) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </select>
              </label>
            </div>
          ) : (
            <dl className="editor-record-meta">
              <div>
                <dt>문서 코드</dt>
                <dd>{form.documentCode}</dd>
              </div>
              <div>
                <dt>문서 유형</dt>
                <dd>{documentTypeLabel[form.documentType]}</dd>
              </div>
            </dl>
          )}

          <label>
            제목
            <input
              name="title"
              value={form.title}
              onChange={handleChange}
              maxLength={isCreate ? 50 : 200}
              required
            />
          </label>

          <div className="form-row">
            {isCreate ? (
              <label>
                담당 부서
                <select
                  name="departmentId"
                  value={form.departmentId}
                  onChange={handleChange}
                >
                  <option value="">독립 문서</option>
                  {memberships.map((membership) => (
                    <option
                      key={membership.department.id}
                      value={membership.department.id}
                    >
                      {membership.department.name} / {membership.rank.name}
                    </option>
                  ))}
                </select>
              </label>
            ) : null}
            <label>
              필요 인가 등급
              <input
                name="requiredClearanceLevel"
                value={form.requiredClearanceLevel}
                onChange={handleChange}
                type="number"
                min="0"
                max="10"
                required
              />
            </label>
          </div>

          <label>
            요약
            <textarea
              name="summary"
              value={form.summary}
              onChange={handleChange}
              rows={3}
            />
          </label>
          <label>
            본문
            <textarea
              name="content"
              value={form.content}
              onChange={handleChange}
              rows={16}
              required
            />
          </label>

          <div className="form-actions">
            <button
              className="secondary-button"
              type="button"
              onClick={handleCancel}
            >
              취소
            </button>
            <button className="primary-button" type="submit">
              {isSaving
                ? '저장 중...'
                : isCreate
                  ? '초안 저장'
                  : '변경 저장'}
            </button>
          </div>
        </fieldset>
      </form>
    </section>
  )
}

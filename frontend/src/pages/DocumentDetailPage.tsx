import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ApiError } from '../api/http'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../features/auth/AuthContext'
import { documentApi } from '../features/documents/documentApi'
import { getDocumentPermissions } from '../features/documents/permissions'
import {
  documentStatusLabel,
  documentTypeLabel,
  formatDocumentDate,
  isErrorDocument,
} from '../features/documents/presentation'
import type { ArchiveDocument } from '../features/documents/types'
import { membershipApi } from '../features/memberships/membershipApi'
import type { DepartmentMembership } from '../features/memberships/types'

type WorkflowAction = 'submit' | 'approve' | 'reject'

const workflowLabels: Record<WorkflowAction, string> = {
  submit: '검토 요청',
  approve: '배포 승인',
  reject: '반려',
}

const workflowConfirmations: Record<WorkflowAction, string> = {
  submit: '이 문서를 검토 대기 상태로 전환할까요?',
  approve: '검토를 완료하고 이 문서를 배포할까요?',
  reject: '이 문서를 작성자에게 반려할까요?',
}

export default function DocumentDetailPage() {
  const { documentId } = useParams()
  const { user, accessToken } = useAuth()
  const [document, setDocument] = useState<ArchiveDocument | null>(null)
  const [memberships, setMemberships] = useState<DepartmentMembership[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [permissionMessage, setPermissionMessage] = useState<string | null>(null)
  const [actionMessage, setActionMessage] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [pendingAction, setPendingAction] = useState<WorkflowAction | null>(null)
  const [isActionRunning, setIsActionRunning] = useState(false)
  const [reloadKey, setReloadKey] = useState(0)

  const numericDocumentId = Number(documentId)

  useEffect(() => {
    let isCurrent = true

    if (!Number.isInteger(numericDocumentId) || numericDocumentId <= 0) {
      setErrorMessage('올바르지 않은 문서 번호입니다.')
      setIsLoading(false)
      return
    }

    setIsLoading(true)
    setErrorMessage(null)
    setPermissionMessage(null)

    const membershipRequest = accessToken
      ? membershipApi.getMyMemberships(accessToken).catch(() => {
          if (isCurrent) {
            setPermissionMessage(
              '부서 권한을 확인하지 못해 일부 작업 버튼이 표시되지 않을 수 있습니다.',
            )
          }
          return []
        })
      : Promise.resolve([])

    Promise.all([
      documentApi.getDocument(numericDocumentId, accessToken ?? undefined),
      membershipRequest,
    ])
      .then(([documentResponse, membershipResponse]) => {
        if (!isCurrent) return
        setDocument(documentResponse)
        setMemberships(membershipResponse)
      })
      .catch((error) => {
        if (!isCurrent) return
        setErrorMessage(
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
  }, [numericDocumentId, accessToken, reloadKey])

  const permissions = useMemo(
    () =>
      document
        ? getDocumentPermissions(user, document, memberships)
        : {
            canEdit: false,
            canSubmit: false,
            canApprove: false,
            canReject: false,
          },
    [document, memberships, user],
  )

  const handleWorkflowAction = async () => {
    if (!accessToken || !pendingAction || !document || isActionRunning) return

    setIsActionRunning(true)
    setActionError(null)
    setActionMessage(null)

    try {
      const updatedDocument =
        pendingAction === 'submit'
          ? await documentApi.submitForReview(document.id, accessToken)
          : pendingAction === 'approve'
            ? await documentApi.approveDocument(document.id, accessToken)
            : await documentApi.rejectDocument(document.id, accessToken)

      setDocument(updatedDocument)
      setActionMessage(`${workflowLabels[pendingAction]} 처리가 완료되었습니다.`)
      setPendingAction(null)
    } catch (error) {
      setActionError(
        error instanceof ApiError
          ? error.message
          : '문서 상태를 변경하지 못했습니다.',
      )
    } finally {
      setIsActionRunning(false)
    }
  }

  if (isLoading) {
    return (
      <article>
        <PageHeader eyebrow="DOCUMENT RECORD" title="문서 조회" />
        <div className="route-loading">문서 내용을 불러오는 중입니다.</div>
      </article>
    )
  }

  if (errorMessage || !document) {
    return (
      <article>
        <PageHeader eyebrow="DOCUMENT RECORD" title="문서 조회 실패" />
        <div className="inline-alert error" role="alert">
          <span>{errorMessage ?? '문서를 찾을 수 없습니다.'}</span>
          <button type="button" onClick={() => setReloadKey((key) => key + 1)}>
            다시 시도
          </button>
        </div>
      </article>
    )
  }

  const hasWorkflowActions =
    permissions.canEdit ||
    permissions.canSubmit ||
    permissions.canApprove ||
    permissions.canReject
  const errorDocument = isErrorDocument(
    document.documentType,
    document.documentCode,
  )

  return (
    <article className={errorDocument ? 'error-record' : undefined}>
      <PageHeader
        eyebrow={document.documentCode}
        title={document.title}
        description={documentTypeLabel[document.documentType]}
        actions={
          hasWorkflowActions ? (
            <div className="document-actions">
              {permissions.canEdit ? (
                <Link
                  className="secondary-button"
                  to={`/documents/${document.id}/edit`}
                >
                  수정
                </Link>
              ) : null}
              {permissions.canSubmit ? (
                <button
                  className="secondary-button"
                  type="button"
                  onClick={() => setPendingAction('submit')}
                >
                  검토 요청
                </button>
              ) : null}
              {permissions.canApprove ? (
                <button
                  className="approve-button"
                  type="button"
                  onClick={() => setPendingAction('approve')}
                >
                  배포 승인
                </button>
              ) : null}
              {permissions.canReject ? (
                <button
                  className="reject-button"
                  type="button"
                  onClick={() => setPendingAction('reject')}
                >
                  반려
                </button>
              ) : null}
            </div>
          ) : undefined
        }
      />

      {permissionMessage ? (
        <div className="inline-alert warning" role="status">
          {permissionMessage}
        </div>
      ) : null}

      {actionMessage ? (
        <div className="inline-alert success" role="status">
          {actionMessage}
        </div>
      ) : null}

      {actionError ? (
        <div className="inline-alert error" role="alert">
          {actionError}
        </div>
      ) : null}

      {pendingAction ? (
        <section className="workflow-confirmation" role="alertdialog">
          <div>
            <strong>{workflowLabels[pendingAction]}</strong>
            <p>{workflowConfirmations[pendingAction]}</p>
          </div>
          <div className="workflow-confirmation-actions">
            <button
              className="secondary-button"
              type="button"
              disabled={isActionRunning}
              onClick={() => setPendingAction(null)}
            >
              취소
            </button>
            <button
              className={
                pendingAction === 'reject' ? 'reject-button' : 'primary-button'
              }
              type="button"
              disabled={isActionRunning}
              onClick={handleWorkflowAction}
            >
              {isActionRunning ? '처리 중...' : '확인'}
            </button>
          </div>
        </section>
      ) : null}

      {errorDocument ? (
        <div className="error-document-warning" role="note">
          ERROR 연관 기록입니다. 인가 등급에 따라 일부 정보가 제한될 수
          있습니다.
        </div>
      ) : null}

      <dl className="metadata-grid document-metadata">
        <div>
          <dt>문서 종류</dt>
          <dd>{documentTypeLabel[document.documentType]}</dd>
        </div>
        <div>
          <dt>처리 상태</dt>
          <dd>
            <span
              className={`status-badge status-${document.status.toLowerCase()}`}
            >
              {documentStatusLabel[document.status]}
            </span>
          </dd>
        </div>
        <div>
          <dt>담당 부서</dt>
          <dd>{document.department?.name ?? '독립 기록'}</dd>
        </div>
        <div>
          <dt>필요 인가 등급</dt>
          <dd>
            <span
              className={`clearance-badge level-${document.requiredClearanceLevel}`}
            >
              Level-{document.requiredClearanceLevel}
            </span>
          </dd>
        </div>
        <div>
          <dt>작성자</dt>
          <dd>{document.author.nickname}</dd>
        </div>
        <div>
          <dt>최종 수정일</dt>
          <dd>{formatDocumentDate(document.updatedAt)}</dd>
        </div>
      </dl>

      <section className="document-body">
        {document.summary ? (
          <div className="document-summary">
            <h2>요약</h2>
            <p>{document.summary}</p>
          </div>
        ) : null}

        <div className="document-content">
          <h2>본문</h2>
          <p>{document.content}</p>
        </div>

        <footer className="document-audit-note">
          배포일 {formatDocumentDate(document.publishedAt)} / 기록 ID {document.id}
        </footer>
      </section>
    </article>
  )
}

import type { AuthUser, UserRole } from '../auth/types'
import type { DepartmentMembership } from '../memberships/types'
import type { ArchiveDocument } from './types'

const unrestrictedGlobalRoles = new Set<UserRole>([
  'SITE_DIRECTOR',
  'VICE_ADMINISTRATOR',
  'ADMINISTRATOR',
])

function hasGlobalDocumentAuthority(
  user: AuthUser,
  memberships: DepartmentMembership[],
) {
  if (unrestrictedGlobalRoles.has(user.role)) return true

  return (
    user.role === 'AION_COUNCIL' &&
    memberships.some(
      (membership) =>
        membership.rank.code === 'SENIOR_OVERWATCH_COMMANDER',
    )
  )
}

function hasDepartmentRank(
  document: ArchiveDocument,
  memberships: DepartmentMembership[],
  minimumLevel: number,
) {
  if (!document.department) return false

  return memberships.some(
    (membership) =>
      membership.department.id === document.department?.id &&
      membership.rank.levelOrder >= minimumLevel,
  )
}

export function getDocumentPermissions(
  user: AuthUser | null,
  document: ArchiveDocument,
  memberships: DepartmentMembership[],
) {
  if (!user) {
    return {
      canEdit: false,
      canSubmit: false,
      canApprove: false,
      canReject: false,
    }
  }

  const isAuthor = user.id === document.author.id
  const hasGlobalAuthority = hasGlobalDocumentAuthority(user, memberships)
  const isEditableStatus =
    document.status !== 'PENDING_REVIEW' && document.status !== 'ARCHIVED'
  const isPendingReview = document.status === 'PENDING_REVIEW'

  return {
    canEdit: (isAuthor || hasGlobalAuthority) && isEditableStatus,
    canSubmit:
      isAuthor &&
      (document.status === 'DRAFT' || document.status === 'REJECTED'),
    canApprove:
      isPendingReview &&
      (hasGlobalAuthority || hasDepartmentRank(document, memberships, 5)),
    canReject:
      isPendingReview &&
      (hasGlobalAuthority || hasDepartmentRank(document, memberships, 4)),
  }
}

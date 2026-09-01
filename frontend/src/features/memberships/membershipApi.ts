import { apiRequest } from '../../api/http'
import type { DepartmentMembership } from './types'

export const membershipApi = {
  getMyMemberships(accessToken: string) {
    return apiRequest<DepartmentMembership[]>(
      '/api/users/me/memberships',
      { method: 'GET' },
      accessToken,
    )
  },
}

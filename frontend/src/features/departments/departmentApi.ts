import { apiRequest } from '../../api/http'
import type { Department, DepartmentRank } from './types'

export const departmentApi = {
  getDepartments() {
    return apiRequest<Department[]>('/api/departments', { method: 'GET' })
  },

  getDepartmentRanks(departmentId: number) {
    return apiRequest<DepartmentRank[]>(
      `/api/departments/${departmentId}/ranks`,
      { method: 'GET' },
    )
  },
}

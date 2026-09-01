import type {
  Department,
  DepartmentRank,
} from '../departments/types'

export type DepartmentMembership = {
  id: number
  department: Department
  rank: DepartmentRank
  joinedAt: string
}

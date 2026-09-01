export type UserRole =
  | 'USER'
  | 'STAFF'
  | 'SITE_DIRECTOR'
  | 'AION_COUNCIL'
  | 'VICE_ADMINISTRATOR'
  | 'ADMINISTRATOR'

export type AuthUser = {
  id: number
  email: string
  nickname?: string
  role: UserRole
  clearanceLevel: number
  clearanceLevelName?: string
  title?: string | null
  status?: string
}

export type LoginRequest = {
  email: string
  password: string
}

export type SignupRequest = LoginRequest & {
  nickname: string
}

export type LoginUserPayload = {
  id: number
  email: string
  nickname: string
  role: UserRole
  ClearanceLevel?: number
  ClearanceLevelName?: string
  clearanceLevel?: number
  clearanceLevelName?: string
  title?: string | null
  status?: string
}

export type LoginResponse = {
  accessToken: string
  tokenType: 'Bearer'
  user: LoginUserPayload
}

export type AuthenticatedUserResponse = {
  id: number
  email: string
  role: UserRole
  clearanceLevel: number
}

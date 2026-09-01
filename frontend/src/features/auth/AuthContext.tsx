import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react'
import type { ReactNode } from 'react'
import { authApi, normalizeLoginUser } from './authApi'
import type { AuthUser, LoginRequest, SignupRequest } from './types'

const TOKEN_STORAGE_KEY = 'secure-archive.access-token'

type AuthContextValue = {
  user: AuthUser | null
  accessToken: string | null
  isInitializing: boolean
  login: (request: LoginRequest) => Promise<AuthUser>
  signup: (request: SignupRequest) => Promise<AuthUser>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(() =>
    sessionStorage.getItem(TOKEN_STORAGE_KEY),
  )
  const [user, setUser] = useState<AuthUser | null>(null)
  const [isInitializing, setIsInitializing] = useState(Boolean(accessToken))

  const logout = useCallback(() => {
    sessionStorage.removeItem(TOKEN_STORAGE_KEY)
    setAccessToken(null)
    setUser(null)
  }, [])

  useEffect(() => {
    if (!accessToken) {
      setIsInitializing(false)
      return
    }

    let isCurrent = true

    authApi
      .me(accessToken)
      .then((response) => {
        if (!isCurrent) return
        setUser((currentUser) =>
          currentUser?.id === response.id
            ? { ...currentUser, ...response }
            : response,
        )
      })
      .catch(() => {
        if (isCurrent) logout()
      })
      .finally(() => {
        if (isCurrent) setIsInitializing(false)
      })

    return () => {
      isCurrent = false
    }
  }, [accessToken, logout])

  const login = useCallback(async (request: LoginRequest) => {
    const response = await authApi.login(request)
    const authenticatedUser = normalizeLoginUser(response)

    sessionStorage.setItem(TOKEN_STORAGE_KEY, response.accessToken)
    setAccessToken(response.accessToken)
    setUser(authenticatedUser)
    setIsInitializing(false)

    return authenticatedUser
  }, [])

  const signup = useCallback(
    async (request: SignupRequest) => {
      await authApi.signup(request)
      return login({ email: request.email, password: request.password })
    },
    [login],
  )

  const value = useMemo(
    () => ({ user, accessToken, isInitializing, login, signup, logout }),
    [user, accessToken, isInitializing, login, signup, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }

  return context
}

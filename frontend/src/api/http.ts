export type ApiErrorBody = {
  status?: number
  error?: string
  message?: string
  path?: string
}

export class ApiError extends Error {
  readonly status: number
  readonly code?: string

  constructor(status: number, message: string, code?: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL ?? ''
const apiBaseUrl = configuredBaseUrl.replace(/\/$/, '')

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
  accessToken?: string,
): Promise<T> {
  const headers = new Headers(options.headers)

  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`)
  }

  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...options,
    headers,
  })
  const responseText = await response.text()
  let responseBody: unknown = null

  if (responseText) {
    try {
      responseBody = JSON.parse(responseText)
    } catch {
      responseBody = responseText
    }
  }

  if (!response.ok) {
    const errorBody =
      typeof responseBody === 'object' && responseBody !== null
        ? (responseBody as ApiErrorBody)
        : null

    throw new ApiError(
      response.status,
      errorBody?.message ?? `요청을 처리하지 못했습니다. (${response.status})`,
      errorBody?.error,
    )
  }

  return responseBody as T
}

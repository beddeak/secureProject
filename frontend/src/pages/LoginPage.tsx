import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/http'
import { useAuth } from '../features/auth/AuthContext'

type LoginLocationState = {
  from?: string
}

export default function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (user) {
    return <Navigate to="/documents" replace />
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setErrorMessage(null)
    setIsSubmitting(true)

    const formData = new FormData(event.currentTarget)
    const email = String(formData.get('email') ?? '').trim()
    const password = String(formData.get('password') ?? '')

    try {
      await login({ email, password })
      const state = location.state as LoginLocationState | null
      navigate(state?.from ?? '/documents', { replace: true })
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : '로그인 중 서버와 통신하지 못했습니다.',
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="login-page">
      <section className="login-panel" aria-labelledby="login-title">
        <Link className="login-brand" to="/documents">
          <span className="brand-mark">SA</span>
          <span>SECURE ARCHIVE</span>
        </Link>

        <div className="login-heading">
          <p className="eyebrow">AUTHORIZED PERSONNEL ONLY</p>
          <h1 id="login-title">보안 인증</h1>
          <p>내부 문서 시스템에 등록된 계정으로 로그인합니다.</p>
        </div>

        <form className="stack-form" onSubmit={handleSubmit}>
          <label>
            이메일
            <input
              type="email"
              name="email"
              autoComplete="email"
              placeholder="name@securearchive.local"
              disabled={isSubmitting}
              required
            />
          </label>
          <label>
            비밀번호
            <input
              type="password"
              name="password"
              autoComplete="current-password"
              minLength={8}
              disabled={isSubmitting}
              required
            />
          </label>

          {errorMessage ? (
            <p className="form-error" role="alert">
              {errorMessage}
            </p>
          ) : null}

          <button className="primary-button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? '인증 중...' : '로그인'}
          </button>
        </form>

        <p className="auth-footer">
          계정이 없나요? <Link to="/signup">회원가입</Link>
        </p>
      </section>
    </main>
  )
}

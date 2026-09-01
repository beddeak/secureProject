import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/http'
import { useAuth } from '../features/auth/AuthContext'

export default function SignupPage() {
  const { user, signup } = useAuth()
  const navigate = useNavigate()
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (user) {
    return <Navigate to="/documents" replace />
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setErrorMessage(null)

    const formData = new FormData(event.currentTarget)
    const email = String(formData.get('email') ?? '').trim()
    const nickname = String(formData.get('nickname') ?? '').trim()
    const password = String(formData.get('password') ?? '')
    const passwordConfirm = String(formData.get('passwordConfirm') ?? '')

    if (password !== passwordConfirm) {
      setErrorMessage('비밀번호 확인 값이 일치하지 않습니다.')
      return
    }

    setIsSubmitting(true)

    try {
      await signup({ email, nickname, password })
      navigate('/documents', { replace: true })
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : '회원가입 중 서버와 통신하지 못했습니다.',
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="login-page">
      <section className="login-panel" aria-labelledby="signup-title">
        <Link className="login-brand" to="/documents">
          <span className="brand-mark">SA</span>
          <span>SECURE ARCHIVE</span>
        </Link>

        <div className="login-heading">
          <p className="eyebrow">ACCOUNT REGISTRATION</p>
          <h1 id="signup-title">신규 계정 등록</h1>
          <p>새 계정은 Level-0 일반 사용자로 등록됩니다.</p>
        </div>

        <form className="stack-form" onSubmit={handleSubmit}>
          <label>
            이메일
            <input
              type="email"
              name="email"
              autoComplete="email"
              disabled={isSubmitting}
              required
            />
          </label>
          <label>
            닉네임
            <input
              type="text"
              name="nickname"
              autoComplete="nickname"
              minLength={3}
              disabled={isSubmitting}
              required
            />
          </label>
          <label>
            비밀번호
            <input
              type="password"
              name="password"
              autoComplete="new-password"
              minLength={8}
              disabled={isSubmitting}
              required
            />
          </label>
          <label>
            비밀번호 확인
            <input
              type="password"
              name="passwordConfirm"
              autoComplete="new-password"
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
            {isSubmitting ? '계정 생성 중...' : '회원가입'}
          </button>
        </form>

        <p className="auth-footer">
          이미 계정이 있나요? <Link to="/login">로그인</Link>
        </p>
      </section>
    </main>
  )
}

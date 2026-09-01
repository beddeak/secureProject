import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../features/auth/AuthContext'
import type { AuthUser, UserRole } from '../features/auth/types'

const globalRoleLabels: Partial<Record<UserRole, string>> = {
  SITE_DIRECTOR: 'SITE DIRECTOR',
  AION_COUNCIL: 'AION COUNCIL',
  VICE_ADMINISTRATOR: 'VICE ADMINISTRATOR',
  ADMINISTRATOR: 'ADMINISTRATOR',
}

function getAccessLabel(user: AuthUser) {
  return (
    globalRoleLabels[user.role] ??
    user.clearanceLevelName ??
    `Level-${user.clearanceLevel}`
  )
}

const navigation = [
  { to: '/documents', label: '문서 보관소' },
  { to: '/departments', label: '부서 정보' },
  { to: '/personnel', label: '직원 관리' },
]

export default function AppLayout() {
  const { user, logout } = useAuth()

  return (
    <div className="app-frame">
      <aside className="sidebar">
        <div className="brand-block">
          <span className="brand-mark">SA</span>
          <div>
            <strong>SECURE ARCHIVE</strong>
            <span>INTERNAL CMS</span>
          </div>
        </div>

        <nav className="primary-nav" aria-label="주요 메뉴">
          {navigation.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => (isActive ? 'active' : undefined)}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-meta">
          <span>LOCAL INSTANCE</span>
          <strong>SITE-01</strong>
        </div>
      </aside>

      <div className="workspace">
        <header className="topbar">
          <span className="network-label">SECURE NETWORK / ARCHIVE</span>
          <div className="topbar-actions">
            {user ? (
              <>
                <span className="user-chip">
                  <strong>{user.nickname ?? user.email}</strong>
                  <span>{getAccessLabel(user)}</span>
                </span>
                <button className="text-button" type="button" onClick={logout}>
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <NavLink className="login-link" to="/login">
                  로그인
                </NavLink>
                <NavLink className="signup-link" to="/signup">
                  회원가입
                </NavLink>
              </>
            )}
          </div>
        </header>
        <main className="content-area">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

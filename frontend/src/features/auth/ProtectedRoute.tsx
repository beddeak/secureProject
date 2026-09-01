import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'

export default function ProtectedRoute() {
  const { user, isInitializing } = useAuth()
  const location = useLocation()

  if (isInitializing) {
    return <div className="route-loading">인증 상태를 확인하고 있습니다.</div>
  }

  if (!user) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: `${location.pathname}${location.search}` }}
      />
    )
  }

  return <Outlet />
}

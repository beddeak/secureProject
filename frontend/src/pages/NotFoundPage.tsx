import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <main className="not-found-page">
      <p className="eyebrow">404 / RECORD NOT FOUND</p>
      <h1>요청한 기록을 찾을 수 없습니다.</h1>
      <Link className="primary-button" to="/documents">
        문서 보관소로 이동
      </Link>
    </main>
  )
}

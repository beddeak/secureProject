import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './features/auth/ProtectedRoute'
import AppLayout from './layouts/AppLayout'
import {
  DepartmentsPage,
  DocumentDetailPage,
  DocumentEditorPage,
  DocumentsPage,
  LoginPage,
  NotFoundPage,
  PersonnelPage,
  SignupPage,
} from './pages'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />

      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/documents" replace />} />
        <Route path="/documents" element={<DocumentsPage />} />
        <Route
          path="/documents/:documentId"
          element={<DocumentDetailPage />}
        />
        <Route path="/departments" element={<DepartmentsPage />} />

        <Route element={<ProtectedRoute />}>
          <Route
            path="/documents/new"
            element={<DocumentEditorPage mode="create" />}
          />
          <Route
            path="/documents/:documentId/edit"
            element={<DocumentEditorPage mode="edit" />}
          />
          <Route path="/personnel" element={<PersonnelPage />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

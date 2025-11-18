import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import LoginPage from './pages/LoginPage.tsx'
import SignupPage from './pages/SignupPage.tsx'
import DashboardPage from './pages/DashboardPage.tsx'
import BookingPage from './pages/BookingPage.tsx'
import AppointmentsPage from './pages/AppointmentsPage.tsx'
import StaffSchedulePage from './pages/StaffSchedulePage.tsx'
import AdminDashboard from './pages/AdminDashboard.tsx'
import Layout from './components/Layout'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route index element={<DashboardPage />} />
          <Route path="bookings" element={<BookingPage />} />
          <Route path="appointments" element={<AppointmentsPage />} />
          <Route path="schedule" element={<StaffSchedulePage />} />
          <Route path="admin" element={<AdminDashboard />} />
        </Route>
      </Routes>
    </Router>
  )
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, loading } = useAuth()

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />
}

export default App

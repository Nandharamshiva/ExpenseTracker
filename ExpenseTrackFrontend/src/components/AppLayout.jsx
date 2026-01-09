import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../state/AuthContext'

export function AppLayout({ children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function onLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="shell">
      <header className="topbar">
        <div className="topbar-inner">
          <Link className="brand" to="/">
            Expense Tracker
          </Link>

          <nav className="nav">
            <NavLink to="/dashboard" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
              Dashboard
            </NavLink>
          </nav>

          <div className="topbar-right">
            <div className="muted small">{user?.username || user?.email || ''}</div>
            <button className="btn" type="button" onClick={onLogout}>
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="content">{children}</main>
    </div>
  )
}

import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../state/AuthContext'

function parseHashToken() {
  const hash = window.location.hash || ''
  if (!hash.startsWith('#')) return ''
  const params = new URLSearchParams(hash.slice(1))
  return params.get('token') || ''
}

export function LoginPage() {
  const { login, isAuthenticated, setSessionToken } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [usernameOrEmail, setUsernameOrEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const redirectTo = useMemo(() => {
    const from = location.state?.from
    return typeof from === 'string' && from.length > 0 ? from : '/app'
  }, [location.state])

  useEffect(() => {
    // Support OAuth2 login flow: backend redirects to http://localhost:5173/login#token=...
    const hashToken = parseHashToken()
    if (hashToken) {
      setSessionToken(hashToken)
      // Clean up fragment so token isn't kept in history.
      window.history.replaceState(null, '', window.location.pathname)
      navigate('/app', { replace: true })
    }
  }, [setSessionToken, navigate])

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/app', { replace: true })
    }
  }, [isAuthenticated, navigate])

  async function onSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)

    try {
      await login({ usernameOrEmail, password })
      navigate(redirectTo, { replace: true })
    } catch (err) {
      setError(err?.message || 'Login failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="page">
      <div className="card auth-card">
        <h1 className="h1">Welcome back</h1>
        <p className="muted">Login to manage your income, expenses, and PnL.</p>

        {error ? <div className="alert">{error}</div> : null}

        <form className="form" onSubmit={onSubmit}>
          <label className="field">
            <span className="label">Username or email</span>
            <input
              className="input"
              value={usernameOrEmail}
              onChange={(e) => setUsernameOrEmail(e.target.value)}
              autoComplete="username"
              placeholder="e.g. shanker or shanker@email.com"
              required
            />
          </label>

          <label className="field">
            <span className="label">Password</span>
            <input
              className="input"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
          </label>

          <button className="btn primary" type="submit" disabled={submitting}>
            {submitting ? 'Signing in…' : 'Login'}
          </button>
        </form>

        <div className="row space-between">
          <Link className="link" to="/signup">
            Create account
          </Link>
          <div className="muted small">Backend: /api/auth/login</div>
        </div>

        <div className="divider" />

        <div className="row gap">
          <a className="btn" href="/oauth2/authorization/google">
            Login with Google
          </a>
          <a className="btn" href="/oauth2/authorization/github">
            Login with GitHub
          </a>
        </div>
      </div>
    </div>
  )
}

import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../state/AuthContext'

export function SignupPage() {
  const { signup } = useAuth()
  const navigate = useNavigate()

  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function onSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)

    try {
      await signup({ username, email, password })
      navigate('/login', { replace: true })
    } catch (err) {
      setError(err?.message || 'Signup failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="page auth-page">
      <div className="aurora one" aria-hidden="true" />
      <div className="aurora two" aria-hidden="true" />

      <div className="auth-grid">
        <div className="card welcome-card hero-card">
          <div className="eyebrow">Create</div>
          <h1 className="h1">Open your money cockpit</h1>
          <p className="muted">Colorful analytics, quick capture flows, and OAuth in a single dashboard.</p>

          <ul className="feature-list">
            <li className="feature-item"><span className="dot" /> Unlimited ledgers with filters that stick.</li>
            <li className="feature-item"><span className="dot" /> Dual forms for expenses and income.</li>
            <li className="feature-item"><span className="dot" /> Live PnL + export-friendly tables.</li>
          </ul>
        </div>

        <div className="card auth-card glow">
          <h1 className="h1 gradient-text">Sign up</h1>
          <p className="muted">Just three fields to spin up your account.</p>

          {error ? <div className="alert">{error}</div> : null}

          <form className="form" onSubmit={onSubmit}>
            <label className="field">
              <span className="label">Username</span>
              <input
                className="input"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="min 1 char"
                required
              />
            </label>

            <label className="field">
              <span className="label">Email</span>
              <input
                className="input"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
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
                placeholder="6-30 chars"
                minLength={6}
                required
              />
            </label>

            <button className="btn primary" type="submit" disabled={submitting}>
              {submitting ? 'Creating…' : 'Signup'}
            </button>
          </form>

          <div className="row space-between">
            <Link className="link" to="/login">
              Back to login
            </Link>
            <div className="muted small">Backend: /api/auth/signup</div>
          </div>
        </div>
      </div>
    </div>
  )
}

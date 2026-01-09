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
    <div className="page">
      <div className="card auth-card">
        <h1 className="h1">Create your account</h1>
        <p className="muted">Sign up to start tracking expenses and income.</p>

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
  )
}

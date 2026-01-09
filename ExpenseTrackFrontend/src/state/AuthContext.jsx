import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { apiRequest } from '../api/http'

const TOKEN_STORAGE_KEY = 'expense_tracker_token'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_STORAGE_KEY) || '')
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  const isAuthenticated = Boolean(token)

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    setToken('')
    setUser(null)
  }, [])

  const setSessionToken = useCallback((nextToken) => {
    const t = (nextToken || '').trim()
    if (!t) {
      logout()
      return
    }
    localStorage.setItem(TOKEN_STORAGE_KEY, t)
    setToken(t)
  }, [logout])

  const refreshMe = useCallback(async () => {
    if (!token) {
      setUser(null)
      return
    }

    try {
      const res = await apiRequest('/api/auth/me', { token })
      setUser(res.data)
    } catch (e) {
      if (e && (e.status === 401 || e.status === 403)) {
        logout()
      } else {
        throw e
      }
    }
  }, [token, logout])

  const login = useCallback(async ({ usernameOrEmail, password }) => {
    const res = await apiRequest('/api/auth/login', {
      method: 'POST',
      body: { usernameOrEmail, password },
    })

    const nextToken = res.data?.token
    setSessionToken(nextToken)
    setUser(res.data?.user || null)
    return res.data
  }, [setSessionToken])

  const signup = useCallback(async ({ username, email, password }) => {
    await apiRequest('/api/auth/signup', {
      method: 'POST',
      body: { username, email, password },
    })
  }, [])

  useEffect(() => {
    let mounted = true

    async function init() {
      try {
        if (token) {
          await refreshMe()
        }
      } finally {
        if (mounted) setLoading(false)
      }
    }

    init()
    return () => {
      mounted = false
    }
  }, [token, refreshMe])

  const value = useMemo(() => ({
    token,
    user,
    loading,
    isAuthenticated,
    login,
    signup,
    logout,
    setSessionToken,
    refreshMe,
  }), [token, user, loading, isAuthenticated, login, signup, logout, setSessionToken, refreshMe])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}

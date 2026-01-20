import { useEffect, useMemo, useState } from 'react'
import { apiRequest, toQuery } from '../api/http'
import { useAuth } from '../state/AuthContext'

const EXPENSE_CATEGORIES = [
  { value: 'personal', label: 'Personal' },
  { value: 'survival', label: 'Survival / Livelihood' },
  { value: 'investment', label: 'Investment' },
]

const INCOME_SOURCES = [
  { value: 'from_investment', label: 'From investment' },
  { value: 'salary', label: 'Salary' },
  { value: 'from_trading', label: 'From trading' },
]

const ENTRY_TYPES = [
  { value: '', label: 'All' },
  { value: 'expense', label: 'Expense' },
  { value: 'income', label: 'Income' },
]

const SORT_BY = [
  { value: 'date', label: 'Date' },
  { value: 'amount', label: 'Amount' },
  { value: 'tag', label: 'Category / Source' },
]

function formatMoney(n) {
  if (n === null || n === undefined || n === '') return '—'
  const v = typeof n === 'string' ? Number(n) : n
  if (Number.isNaN(v)) return String(n)
  return v.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function kindBadge(kind) {
  if (kind === 'expense') return <span className="pill expense">Expense</span>
  if (kind === 'income') return <span className="pill income">Income</span>
  return <span className="pill">—</span>
}

export function LedgerPage() {
  const { token } = useAuth()

  // Add expense
  const [expDesc, setExpDesc] = useState('')
  const [expCategory, setExpCategory] = useState('personal')
  const [expAmount, setExpAmount] = useState('')
  const [expDate, setExpDate] = useState(() => new Date().toISOString().slice(0, 10))

  // Add income
  const [incDesc, setIncDesc] = useState('')
  const [incSource, setIncSource] = useState('salary')
  const [incAmount, setIncAmount] = useState('')
  const [incDate, setIncDate] = useState(() => new Date().toISOString().slice(0, 10))

  // Filters
  const [type, setType] = useState('')
  const [category, setCategory] = useState('')
  const [source, setSource] = useState('')
  const [dateFrom, setDateFrom] = useState('')
  const [dateTo, setDateTo] = useState('')
  const [minAmount, setMinAmount] = useState('')
  const [maxAmount, setMaxAmount] = useState('')

  // Sorting
  const [sortBy, setSortBy] = useState('date')
  const [sortDir, setSortDir] = useState('desc')

  const [entries, setEntries] = useState([])
  const [summary, setSummary] = useState({ totalIncome: null, totalExpense: null, pnl: null })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const query = useMemo(() => {
    const q = {
      type: type || undefined,
      category: category || undefined,
      source: source || undefined,
      dateFrom: dateFrom || undefined,
      dateTo: dateTo || undefined,
      minAmount: minAmount || undefined,
      maxAmount: maxAmount || undefined,
      sortBy: sortBy || undefined,
      sortDir: sortDir || undefined,
      page: 0,
      size: 50,
    }

    // Backend sorting: tag sorting only makes sense when a concrete type is selected.
    if (q.sortBy === 'tag' && !q.type) {
      q.sortBy = 'date'
    }

    return toQuery(q)
  }, [type, category, source, dateFrom, dateTo, minAmount, maxAmount, sortBy, sortDir])

  const summaryQuery = useMemo(() => {
    return toQuery({
      type: type || undefined,
      category: category || undefined,
      source: source || undefined,
      dateFrom: dateFrom || undefined,
      dateTo: dateTo || undefined,
      minAmount: minAmount || undefined,
      maxAmount: maxAmount || undefined,
    })
  }, [type, category, source, dateFrom, dateTo, minAmount, maxAmount])

  async function load() {
    setLoading(true)
    setError('')
    try {
      const [listRes, summaryRes] = await Promise.all([
        apiRequest('/api/ledger/entries', { token, query }),
        apiRequest('/api/ledger/summary', { token, query: summaryQuery }),
      ])

      setEntries(listRes.data?.content || [])
      setSummary(summaryRes.data || { totalIncome: null, totalExpense: null, pnl: null })
    } catch (e) {
      setError(e?.message || 'Failed to load ledger')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, summaryQuery])

  async function addExpense(e) {
    e.preventDefault()
    setError('')

    try {
      await apiRequest('/api/ledger/expenses', {
        method: 'POST',
        token,
        body: {
          description: expDesc,
          category: expCategory,
          amount: expAmount,
          date: expDate,
        },
      })

      setExpDesc('')
      setExpAmount('')
      await load()
    } catch (err) {
      setError(err?.message || 'Failed to add expense')
    }
  }

  async function addIncome(e) {
    e.preventDefault()
    setError('')

    try {
      await apiRequest('/api/ledger/incomes', {
        method: 'POST',
        token,
        body: {
          description: incDesc,
          source: incSource,
          amount: incAmount,
          date: incDate,
        },
      })

      setIncDesc('')
      setIncAmount('')
      await load()
    } catch (err) {
      setError(err?.message || 'Failed to add income')
    }
  }

  async function removeEntry(id) {
    setError('')
    try {
      await apiRequest(`/api/ledger/entries/${id}`, { method: 'DELETE', token })
      await load()
    } catch (err) {
      setError(err?.message || 'Failed to remove entry')
    }
  }

  function resetFilters() {
    setType('')
    setCategory('')
    setSource('')
    setDateFrom('')
    setDateTo('')
    setMinAmount('')
    setMaxAmount('')
    setSortBy('date')
    setSortDir('desc')
  }

  return (
    <div className="stack">
      <div className="card hero-card">
        <div className="row space-between wrap gap">
          <div className="stack" style={{ gap: 10 }}>
            <div className="eyebrow">Dashboard</div>
            <h1 className="h1 gradient-text">Colorful cashflow cockpit</h1>
            <div className="muted">Add expenses and income, slice by category or source, and watch PnL update live.</div>
            <div className="badges">
              <span className="pill income">Income ready</span>
              <span className="pill expense">Expense capture</span>
              <span className="pill">Live PnL</span>
            </div>
          </div>

          <div className="row gap wrap">
            <div className="chip">GET /api/ledger/entries</div>
            <div className="chip">GET /api/ledger/summary</div>
            <button className="btn primary" type="button" onClick={load} disabled={loading}>
              {loading ? 'Refreshing…' : 'Refresh data'}
            </button>
          </div>
        </div>
      </div>

      {error ? <div className="alert">{error}</div> : null}

      <div className="grid-2">
        <div className="card glow">
          <h2 className="h2">Add expense</h2>
          <p className="muted small">Categorize spending with vibrant pills.</p>
          <form className="form" onSubmit={addExpense}>
            <label className="field">
              <span className="label">Description</span>
              <input className="input" value={expDesc} onChange={(e) => setExpDesc(e.target.value)} required />
            </label>

            <div className="row gap">
              <label className="field" style={{ flex: 1 }}>
                <span className="label">Category</span>
                <select className="select" value={expCategory} onChange={(e) => setExpCategory(e.target.value)}>
                  {EXPENSE_CATEGORIES.map((c) => (
                    <option key={c.value} value={c.value}>
                      {c.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="field" style={{ flex: 1 }}>
                <span className="label">Amount</span>
                <input
                  className="input"
                  type="number"
                  inputMode="decimal"
                  min="0.01"
                  step="0.01"
                  value={expAmount}
                  onChange={(e) => setExpAmount(e.target.value)}
                  required
                />
              </label>
            </div>

            <label className="field">
              <span className="label">Date</span>
              <input className="input" type="date" value={expDate} onChange={(e) => setExpDate(e.target.value)} required />
            </label>

            <button className="btn primary" type="submit">
              Add expense
            </button>
          </form>
        </div>

        <div className="card glow">
          <h2 className="h2">Add income</h2>
          <p className="muted small">Log inflows to balance your PnL.</p>
          <form className="form" onSubmit={addIncome}>
            <label className="field">
              <span className="label">Description</span>
              <input className="input" value={incDesc} onChange={(e) => setIncDesc(e.target.value)} required />
            </label>

            <div className="row gap">
              <label className="field" style={{ flex: 1 }}>
                <span className="label">Source</span>
                <select className="select" value={incSource} onChange={(e) => setIncSource(e.target.value)}>
                  {INCOME_SOURCES.map((s) => (
                    <option key={s.value} value={s.value}>
                      {s.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="field" style={{ flex: 1 }}>
                <span className="label">Amount</span>
                <input
                  className="input"
                  type="number"
                  inputMode="decimal"
                  min="0.01"
                  step="0.01"
                  value={incAmount}
                  onChange={(e) => setIncAmount(e.target.value)}
                  required
                />
              </label>
            </div>

            <label className="field">
              <span className="label">Date</span>
              <input className="input" type="date" value={incDate} onChange={(e) => setIncDate(e.target.value)} required />
            </label>

            <button className="btn primary" type="submit">
              Add income
            </button>
          </form>
        </div>
      </div>

      <div className="card">
        <div className="row space-between wrap">
          <h2 className="h2">Filters & sorting</h2>
          <div className="row gap">
            <button className="btn ghost" type="button" onClick={resetFilters}>
              Reset
            </button>
          </div>
        </div>

        <div className="filters">
          <label className="field">
            <span className="label">Type</span>
            <select className="select" value={type} onChange={(e) => setType(e.target.value)}>
              {ENTRY_TYPES.map((t) => (
                <option key={t.value} value={t.value}>
                  {t.label}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span className="label">Expense category</span>
            <select className="select" value={category} onChange={(e) => setCategory(e.target.value)} disabled={type === 'income'}>
              <option value="">All</option>
              {EXPENSE_CATEGORIES.map((c) => (
                <option key={c.value} value={c.value}>
                  {c.label}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span className="label">Income source</span>
            <select className="select" value={source} onChange={(e) => setSource(e.target.value)} disabled={type === 'expense'}>
              <option value="">All</option>
              {INCOME_SOURCES.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span className="label">Date from</span>
            <input className="input" type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} />
          </label>

          <label className="field">
            <span className="label">Date to</span>
            <input className="input" type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} />
          </label>

          <label className="field">
            <span className="label">Min amount</span>
            <input className="input" type="number" inputMode="decimal" step="0.01" value={minAmount} onChange={(e) => setMinAmount(e.target.value)} />
          </label>

          <label className="field">
            <span className="label">Max amount</span>
            <input className="input" type="number" inputMode="decimal" step="0.01" value={maxAmount} onChange={(e) => setMaxAmount(e.target.value)} />
          </label>

          <label className="field">
            <span className="label">Sort by</span>
            <select className="select" value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              {SORT_BY.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
            {sortBy === 'tag' && !type ? <div className="hint">Select a Type to sort by Category/Source.</div> : null}
          </label>

          <label className="field">
            <span className="label">Sort dir</span>
            <select className="select" value={sortDir} onChange={(e) => setSortDir(e.target.value)}>
              <option value="desc">Desc</option>
              <option value="asc">Asc</option>
            </select>
          </label>
        </div>
      </div>

      <div className="grid-3">
        <div className="card">
          <div className="muted small">Total income</div>
          <div className="kpi">{formatMoney(summary.totalIncome)}</div>
        </div>
        <div className="card">
          <div className="muted small">Total expense</div>
          <div className="kpi">{formatMoney(summary.totalExpense)}</div>
        </div>
        <div className="card">
          <div className="muted small">Total PnL</div>
          <div className={Number(summary.pnl) >= 0 ? 'kpi good' : 'kpi bad'}>{formatMoney(summary.pnl)}</div>
        </div>
      </div>

      <div className="card">
        <div className="row space-between wrap">
          <h2 className="h2">Entries</h2>
          <div className="muted small">Tracking {entries.length} items</div>
        </div>

        <div className="table">
          <div className="tr head">
            <div>Type</div>
            <div>Description</div>
            <div>Category / Source</div>
            <div className="right">Amount</div>
            <div>Date</div>
            <div className="right">Action</div>
          </div>

          {entries.length === 0 ? (
            <div className="empty">No entries found for current filters.</div>
          ) : (
            entries.map((e) => (
              <div className="tr" key={e.id}>
                <div>{kindBadge(e.kind)}</div>
                <div className="mono">{e.description}</div>
                <div className="muted">{e.category || e.source || '—'}</div>
                <div className="right">{formatMoney(e.amount)}</div>
                <div className="mono">{e.date}</div>
                <div className="right">
                  <button className="btn danger" type="button" onClick={() => removeEntry(e.id)}>
                    Remove
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  )
}

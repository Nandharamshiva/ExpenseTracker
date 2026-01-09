# Expense Tracker Frontend

Simple React UI for the Spring Boot backend.

## Run

1) Start backend on `http://localhost:8081`

2) Install and run frontend:

- `npm install`
- `npm run dev`

Then open `http://localhost:5173`.

## API configuration

By default, Vite proxies `/api`, `/oauth2`, and `/login/oauth2` to `http://localhost:8081` (see `vite.config.js`).

If you want to run without the proxy (or for production), set:

- `VITE_API_BASE_URL=http://localhost:8081`

## Features (Dashboard)

- Login / signup (JWT Bearer)
- Add expense (category: personal/survival/investment)
- Add income (source: from_investment/salary/from_trading)
- Filter by type, category/source, date range, amount range
- Sort by date, amount, or category/source (tag)
- Total PnL (from backend summary)
- Remove expense/income (delete entry)

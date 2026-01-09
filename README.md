# Expense Tracker Application

A full-stack Expense Tracker web application that helps users manage their **expenses and income**, analyze financial data, and calculate **total Profit & Loss (PnL)**.  
The project is built using a **Spring Boot backend** and a **React frontend**, with secure authentication and user-specific data handling.
## Features

### Authentication
- User **Signup & Login**
- Secure authentication using **JWT**
- Each user can access only their own data

### Expense Management
- Add expenses with:
  - Description
  - Category: 'Personal', 'Survival/Livelihood', 'Investment'
  - Amount
  - Date
- Remove existing expenses

### Income Management
- Add income with:
  - Description
  - Source: 'Salary', 'From Investment', 'From Trading'
  - Amount
  - Date
- Remove existing income

### Sorting & Filtering
- Sort records by:
  - Category / Source
  - Amount
- Filter records by:
  - Expense or Income
  - Category / Source
  - Date range
  - Amount range

### Financial Summary
- Automatic calculation of:
  - Total Income
  - Total Expense
  - **Total Profit & Loss (PnL)**

---

## Tech Stack

### Frontend
- React.js
- Axios
- HTML5, CSS3
- JavaScript (ES6)

### Backend
- Spring Boot
- Spring Security (JWT Authentication)
- JPA & Hibernate
- REST APIs

### Database
- MySQL

## API Highlights

/auth/signup – User Registration

/auth/login – User Login

/expenses – Add / Fetch / Delete Expenses

/income – Add / Fetch / Delete Income

### Output

User-specific dashboard

Real-time financial calculations

Clean UI for managing expenses and income


package org.example.expensetracker.ledger.dto;

import java.math.BigDecimal;

public record LedgerSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal pnl
) {
}

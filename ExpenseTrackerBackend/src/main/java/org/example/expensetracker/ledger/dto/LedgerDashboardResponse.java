package org.example.expensetracker.ledger.dto;

import java.math.BigDecimal;
import java.util.List;

public record LedgerDashboardResponse(
        String monthKey,
        BigDecimal monthIncome,
        BigDecimal monthExpense,
        BigDecimal monthPnl,
        List<LedgerTrendPointResponse> trend,
        List<LedgerEntryResponse> recentExpenses
) {
}

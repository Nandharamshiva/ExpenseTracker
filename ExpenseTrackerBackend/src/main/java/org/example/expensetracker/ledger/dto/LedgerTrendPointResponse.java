package org.example.expensetracker.ledger.dto;

import java.math.BigDecimal;

public record LedgerTrendPointResponse(
        String month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {
}

package org.example.expensetracker.ledger.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.example.expensetracker.ledger.entity.ExpenseCategory;
import org.example.expensetracker.ledger.entity.IncomeSource;
import org.example.expensetracker.ledger.entity.LedgerEntryType;

public record LedgerEntryResponse(
        Long id,
        LedgerEntryType kind,
        String description,
        ExpenseCategory category,
        IncomeSource source,
        BigDecimal amount,
        LocalDate date
) {
}

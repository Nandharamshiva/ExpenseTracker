package org.example.expensetracker.ledger.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.example.expensetracker.ledger.entity.ExpenseCategory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateExpenseRequest(
        @NotBlank(message = "Expense description is required")
        String description,

        @NotNull(message = "Expense category is required")
        ExpenseCategory category,

        @NotNull(message = "Expense amount is required")
        @DecimalMin(value = "0.01", message = "Expense amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Expense date is required")
        LocalDate date
) {
}

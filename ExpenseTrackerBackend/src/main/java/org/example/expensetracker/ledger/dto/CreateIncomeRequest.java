package org.example.expensetracker.ledger.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.example.expensetracker.ledger.entity.IncomeSource;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateIncomeRequest(
        @NotBlank(message = "Income description is required")
        String description,

        @NotNull(message = "Income source is required")
        IncomeSource source,

        @NotNull(message = "Income amount is required")
        @DecimalMin(value = "0.01", message = "Income amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Income date is required")
        LocalDate date
) {
}

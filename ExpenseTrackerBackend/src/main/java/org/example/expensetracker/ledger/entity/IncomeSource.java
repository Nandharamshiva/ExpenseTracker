package org.example.expensetracker.ledger.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IncomeSource {
    FROM_INVESTMENT("from_investment"),
    SALARY("salary"),
    FROM_TRADING("from_trading");

    private final String value;

    IncomeSource(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static IncomeSource from(String raw) {
        if (raw == null) return null;
        String v = raw.trim().toLowerCase();
        // accept a few variants
        if (v.replace(" ", "").contains("investment")) return FROM_INVESTMENT;
        if (v.replace(" ", "").contains("trading")) return FROM_TRADING;
        for (IncomeSource s : values()) {
            if (s.value.equals(v)) return s;
        }
        throw new IllegalArgumentException("Invalid income source: " + raw);
    }
}

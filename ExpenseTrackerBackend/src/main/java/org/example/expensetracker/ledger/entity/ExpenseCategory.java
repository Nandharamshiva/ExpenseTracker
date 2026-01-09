package org.example.expensetracker.ledger.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ExpenseCategory {
    PERSONAL("personal"),
    SURVIVAL("survival"),
    INVESTMENT("investment");

    private final String value;

    ExpenseCategory(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static ExpenseCategory from(String raw) {
        if (raw == null) return null;
        String v = raw.trim().toLowerCase();
        // Allow some human-friendly variants
        if (v.contains("livelihood") || v.contains("survival")) return SURVIVAL;
        for (ExpenseCategory c : values()) {
            if (c.value.equals(v)) return c;
        }
        throw new IllegalArgumentException("Invalid expense category: " + raw);
    }
}

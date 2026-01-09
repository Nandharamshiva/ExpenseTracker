package org.example.expensetracker.ledger.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LedgerEntryType {
    EXPENSE("expense"),
    INCOME("income");

    private final String value;

    LedgerEntryType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static LedgerEntryType from(String raw) {
        if (raw == null) return null;
        String v = raw.trim().toLowerCase();
        for (LedgerEntryType t : values()) {
            if (t.value.equals(v)) return t;
        }
        throw new IllegalArgumentException("Invalid type: " + raw);
    }
}

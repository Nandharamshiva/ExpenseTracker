package org.example.expensetracker.ledger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.expensetracker.auth.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "LedgerEntries",
        indexes = {
                @Index(name = "idx_ledger_user_date", columnList = "user_id,entry_date"),
                @Index(name = "idx_ledger_user_type", columnList = "user_id,entry_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 20)
    private LedgerEntryType type;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "expense_category", length = 30)
    private ExpenseCategory expenseCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_source", length = 30)
    private IncomeSource incomeSource;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

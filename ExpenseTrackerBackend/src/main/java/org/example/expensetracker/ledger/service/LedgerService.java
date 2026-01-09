package org.example.expensetracker.ledger.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.expensetracker.auth.entity.User;
import org.example.expensetracker.auth.security.SecurityUtils;
import org.example.expensetracker.ledger.dto.CreateExpenseRequest;
import org.example.expensetracker.ledger.dto.CreateIncomeRequest;
import org.example.expensetracker.ledger.dto.LedgerDashboardResponse;
import org.example.expensetracker.ledger.dto.LedgerEntryResponse;
import org.example.expensetracker.ledger.dto.LedgerSummaryResponse;
import org.example.expensetracker.ledger.dto.LedgerTrendPointResponse;
import org.example.expensetracker.ledger.entity.ExpenseCategory;
import org.example.expensetracker.ledger.entity.IncomeSource;
import org.example.expensetracker.ledger.entity.LedgerEntry;
import org.example.expensetracker.ledger.entity.LedgerEntryType;
import org.example.expensetracker.ledger.repository.LedgerEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final SecurityUtils securityUtils;
    private final EntityManager entityManager;

    @Transactional
    public LedgerEntryResponse addExpense(CreateExpenseRequest req) {
        User user = securityUtils.getCurrentUser();

        LedgerEntry entry = LedgerEntry.builder()
                .user(user)
                .type(LedgerEntryType.EXPENSE)
                .description(req.description().trim())
                .expenseCategory(req.category())
                .incomeSource(null)
                .amount(req.amount())
                .entryDate(req.date())
                .build();

        validateEntry(entry);

        LedgerEntry saved = ledgerEntryRepository.save(entry);
        return toResponse(saved);
    }

    @Transactional
    public LedgerEntryResponse addIncome(CreateIncomeRequest req) {
        User user = securityUtils.getCurrentUser();

        LedgerEntry entry = LedgerEntry.builder()
                .user(user)
                .type(LedgerEntryType.INCOME)
                .description(req.description().trim())
                .incomeSource(req.source())
                .expenseCategory(null)
                .amount(req.amount())
                .entryDate(req.date())
                .build();

        validateEntry(entry);

        LedgerEntry saved = ledgerEntryRepository.save(entry);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> list(
            LedgerEntryType type,
            ExpenseCategory category,
            IncomeSource source,
            LocalDate dateFrom,
            LocalDate dateTo,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        Long userId = securityUtils.getCurrentUserId();

        int safeSize = Math.min(Math.max(size, 1), 500);
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                safeSize,
                toSort(type, sortBy, sortDir)
        );

        Specification<LedgerEntry> spec = baseSpec(userId, type, category, source, dateFrom, dateTo, minAmount, maxAmount);

        return ledgerEntryRepository.findAll(spec, pageable)
                .map(LedgerService::toResponse);
    }

    @Transactional(readOnly = true)
    public LedgerSummaryResponse summary(
            LedgerEntryType type,
            ExpenseCategory category,
            IncomeSource source,
            LocalDate dateFrom,
            LocalDate dateTo,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {
        Long userId = securityUtils.getCurrentUserId();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<LedgerEntry> root = cq.from(LedgerEntry.class);

        Predicate where = basePredicate(cb, root, userId, type, category, source, dateFrom, dateTo, minAmount, maxAmount);

        Expression<BigDecimal> amount = root.get("amount");
        Expression<BigDecimal> incomeSum = cb.sum(
                cb.<BigDecimal>selectCase()
                        .when(cb.equal(root.get("type"), LedgerEntryType.INCOME), amount)
                        .otherwise(BigDecimal.ZERO)
        );
        Expression<BigDecimal> expenseSum = cb.sum(
                cb.<BigDecimal>selectCase()
                        .when(cb.equal(root.get("type"), LedgerEntryType.EXPENSE), amount)
                        .otherwise(BigDecimal.ZERO)
        );

        cq.multiselect(
                incomeSum.alias("income"),
                expenseSum.alias("expense")
        ).where(where);

        Tuple t = entityManager.createQuery(cq).getSingleResult();
        BigDecimal income = nvl(t.get("income", BigDecimal.class));
        BigDecimal expense = nvl(t.get("expense", BigDecimal.class));
        return new LedgerSummaryResponse(income, expense, income.subtract(expense));
    }

    @Transactional(readOnly = true)
    public List<LedgerTrendPointResponse> trend(int months) {
        Long userId = securityUtils.getCurrentUserId();

        int safeMonths = Math.min(Math.max(months, 1), 36);
        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(safeMonths - 1L);
        LocalDate from = start.atDay(1);
        LocalDate to = end.atEndOfMonth();

        List<LedgerEntryRepository.MonthlyTrendRow> rows = ledgerEntryRepository.findMonthlyTrend(userId, from, to);
        Map<String, LedgerEntryRepository.MonthlyTrendRow> byKey = new HashMap<>();
        for (LedgerEntryRepository.MonthlyTrendRow r : rows) {
            String key = String.format("%04d-%02d", r.getY(), r.getM());
            byKey.put(key, r);
        }

        List<LedgerTrendPointResponse> out = new ArrayList<>(safeMonths);
        for (int i = 0; i < safeMonths; i++) {
            YearMonth ym = start.plusMonths(i);
            String key = String.format("%04d-%02d", ym.getYear(), ym.getMonthValue());
            LedgerEntryRepository.MonthlyTrendRow r = byKey.get(key);
            BigDecimal income = r == null ? BigDecimal.ZERO : nvl(r.getIncome());
            BigDecimal expense = r == null ? BigDecimal.ZERO : nvl(r.getExpense());
            out.add(new LedgerTrendPointResponse(key, income, expense, income.subtract(expense)));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public LedgerDashboardResponse dashboard(int trendMonths, int recentSize) {
        int safeTrendMonths = Math.min(Math.max(trendMonths, 1), 36);
        int safeRecentSize = Math.min(Math.max(recentSize, 1), 50);

        YearMonth now = YearMonth.now();
        String monthKey = String.format("%04d-%02d", now.getYear(), now.getMonthValue());
        LocalDate from = now.atDay(1);
        LocalDate to = now.atEndOfMonth();

        LedgerSummaryResponse monthSummary = summary(null, null, null, from, to, null, null);
        List<LedgerTrendPointResponse> points = trend(safeTrendMonths);
        List<LedgerEntryResponse> recent = list(
                LedgerEntryType.EXPENSE,
                null,
                null,
                null,
                null,
                null,
                null,
                "date",
                "desc",
                0,
                safeRecentSize
        ).getContent();

        return new LedgerDashboardResponse(
                monthKey,
                monthSummary.totalIncome(),
                monthSummary.totalExpense(),
                monthSummary.pnl(),
                points,
                recent
        );
    }

    @Transactional
    public void deleteEntry(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        LedgerEntry entry = ledgerEntryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Entry not found"));

        if (!entry.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }

        ledgerEntryRepository.delete(entry);
    }

    private static void validateEntry(LedgerEntry entry) {
        if (entry.getAmount() == null || entry.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Amount must be positive");
        }
        if (entry.getEntryDate() == null) {
            throw new IllegalStateException("Date is required");
        }
        if (entry.getDescription() == null || entry.getDescription().trim().isEmpty()) {
            throw new IllegalStateException("Description is required");
        }

        if (entry.getType() == LedgerEntryType.EXPENSE) {
            if (entry.getExpenseCategory() == null) throw new IllegalStateException("Expense category is required");
            if (entry.getIncomeSource() != null) throw new IllegalStateException("Income source must be empty for expenses");
        }

        if (entry.getType() == LedgerEntryType.INCOME) {
            if (entry.getIncomeSource() == null) throw new IllegalStateException("Income source is required");
            if (entry.getExpenseCategory() != null) throw new IllegalStateException("Expense category must be empty for incomes");
        }
    }

    private static LedgerEntryResponse toResponse(LedgerEntry e) {
        return new LedgerEntryResponse(
                e.getId(),
                e.getType(),
                e.getDescription(),
                e.getExpenseCategory(),
                e.getIncomeSource(),
                e.getAmount(),
                e.getEntryDate()
        );
    }

    private static Sort toSort(LedgerEntryType type, String sortBy, String sortDir) {
        String by = (sortBy == null ? "date" : sortBy.trim().toLowerCase());
        boolean desc = sortDir == null || sortDir.trim().equalsIgnoreCase("desc");
        Sort.Direction dir = desc ? Sort.Direction.DESC : Sort.Direction.ASC;

        return switch (by) {
            case "amount" -> Sort.by(dir, "amount");
            case "tag" -> {
                // Tag sorting only makes sense when a concrete type is selected.
                if (type == LedgerEntryType.EXPENSE) yield Sort.by(dir, "expenseCategory").and(Sort.by(Sort.Direction.DESC, "entryDate"));
                if (type == LedgerEntryType.INCOME) yield Sort.by(dir, "incomeSource").and(Sort.by(Sort.Direction.DESC, "entryDate"));
                yield Sort.by(Sort.Direction.DESC, "entryDate");
            }
            case "date" -> Sort.by(dir, "entryDate");
            default -> Sort.by(Sort.Direction.DESC, "entryDate");
        };
    }

    private static Specification<LedgerEntry> baseSpec(
            Long userId,
            LedgerEntryType type,
            ExpenseCategory category,
            IncomeSource source,
            LocalDate dateFrom,
            LocalDate dateTo,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {
        return (root, query, cb) -> basePredicate(cb, root, userId, type, category, source, dateFrom, dateTo, minAmount, maxAmount);
    }

    private static Predicate basePredicate(
            CriteriaBuilder cb,
            Root<LedgerEntry> root,
            Long userId,
            LedgerEntryType type,
            ExpenseCategory category,
            IncomeSource source,
            LocalDate dateFrom,
            LocalDate dateTo,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {
        Predicate p = cb.equal(root.get("user").get("id"), userId);

        if (type != null) {
            p = cb.and(p, cb.equal(root.get("type"), type));
        }

        if (category != null) {
            p = cb.and(p, cb.equal(root.get("expenseCategory"), category));
            // If caller provides category, ensure we only match expenses.
            p = cb.and(p, cb.equal(root.get("type"), LedgerEntryType.EXPENSE));
        }

        if (source != null) {
            p = cb.and(p, cb.equal(root.get("incomeSource"), source));
            // If caller provides source, ensure we only match income.
            p = cb.and(p, cb.equal(root.get("type"), LedgerEntryType.INCOME));
        }

        if (dateFrom != null) {
            p = cb.and(p, cb.greaterThanOrEqualTo(root.get("entryDate"), dateFrom));
        }
        if (dateTo != null) {
            p = cb.and(p, cb.lessThanOrEqualTo(root.get("entryDate"), dateTo));
        }

        if (minAmount != null) {
            p = cb.and(p, cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
        }
        if (maxAmount != null) {
            p = cb.and(p, cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
        }

        return p;
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}

package org.example.expensetracker.ledger.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.expensetracker.ledger.dto.CreateExpenseRequest;
import org.example.expensetracker.ledger.dto.CreateIncomeRequest;
import org.example.expensetracker.ledger.dto.LedgerDashboardResponse;
import org.example.expensetracker.ledger.dto.LedgerEntryResponse;
import org.example.expensetracker.ledger.dto.LedgerSummaryResponse;
import org.example.expensetracker.ledger.dto.LedgerTrendPointResponse;
import org.example.expensetracker.ledger.entity.ExpenseCategory;
import org.example.expensetracker.ledger.entity.IncomeSource;
import org.example.expensetracker.ledger.entity.LedgerEntryType;
import org.example.expensetracker.ledger.service.LedgerService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @PostMapping("/expenses")
    public ResponseEntity<LedgerEntryResponse> addExpense(@Valid @RequestBody CreateExpenseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ledgerService.addExpense(req));
    }

    @PostMapping("/incomes")
    public ResponseEntity<LedgerEntryResponse> addIncome(@Valid @RequestBody CreateIncomeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ledgerService.addIncome(req));
    }

    @GetMapping("/entries")
    public ResponseEntity<Page<LedgerEntryResponse>> list(
            @RequestParam(name = "type", required = false) LedgerEntryType type,
            @RequestParam(name = "category", required = false) ExpenseCategory category,
            @RequestParam(name = "source", required = false) IncomeSource source,
            @RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(name = "minAmount", required = false) BigDecimal minAmount,
            @RequestParam(name = "maxAmount", required = false) BigDecimal maxAmount,
            @RequestParam(name = "sortBy", required = false, defaultValue = "date") String sortBy,
            @RequestParam(name = "sortDir", required = false, defaultValue = "desc") String sortDir,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(
                ledgerService.list(type, category, source, dateFrom, dateTo, minAmount, maxAmount, sortBy, sortDir, page, size)
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<LedgerSummaryResponse> summary(
            @RequestParam(name = "type", required = false) LedgerEntryType type,
            @RequestParam(name = "category", required = false) ExpenseCategory category,
            @RequestParam(name = "source", required = false) IncomeSource source,
            @RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(name = "minAmount", required = false) BigDecimal minAmount,
            @RequestParam(name = "maxAmount", required = false) BigDecimal maxAmount
    ) {
        return ResponseEntity.ok(ledgerService.summary(type, category, source, dateFrom, dateTo, minAmount, maxAmount));
    }

    @GetMapping("/trend")
    public ResponseEntity<List<LedgerTrendPointResponse>> trend(
            @RequestParam(name = "months", required = false, defaultValue = "6") int months
    ) {
        return ResponseEntity.ok(ledgerService.trend(months));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<LedgerDashboardResponse> dashboard(
            @RequestParam(name = "trendMonths", required = false, defaultValue = "6") int trendMonths,
            @RequestParam(name = "recentSize", required = false, defaultValue = "6") int recentSize
    ) {
        return ResponseEntity.ok(ledgerService.dashboard(trendMonths, recentSize));
    }

    @DeleteMapping("/entries/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ledgerService.deleteEntry(id);
    }
}

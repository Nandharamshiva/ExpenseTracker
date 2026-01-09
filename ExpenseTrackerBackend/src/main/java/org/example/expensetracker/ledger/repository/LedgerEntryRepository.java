package org.example.expensetracker.ledger.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.expensetracker.ledger.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long>, JpaSpecificationExecutor<LedgerEntry> {

		interface MonthlyTrendRow {
				Integer getY();

				Integer getM();

				BigDecimal getIncome();

				BigDecimal getExpense();
		}

		@Query("""
						select
							year(e.entryDate) as y,
							month(e.entryDate) as m,
							sum(case when e.type = org.example.expensetracker.ledger.entity.LedgerEntryType.INCOME then e.amount else 0 end) as income,
							sum(case when e.type = org.example.expensetracker.ledger.entity.LedgerEntryType.EXPENSE then e.amount else 0 end) as expense
						from LedgerEntry e
						where e.user.id = :userId
							and e.entryDate >= :from
							and e.entryDate <= :to
						group by year(e.entryDate), month(e.entryDate)
						order by year(e.entryDate), month(e.entryDate)
						""")
		List<MonthlyTrendRow> findMonthlyTrend(
						@Param("userId") Long userId,
						@Param("from") LocalDate from,
						@Param("to") LocalDate to
		);
}

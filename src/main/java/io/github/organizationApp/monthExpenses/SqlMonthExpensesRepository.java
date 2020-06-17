package io.github.organizationApp.monthExpenses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
interface SqlMonthExpensesRepository extends MonthExpensesRepository, JpaRepository<MonthExpenses,Integer> {
}

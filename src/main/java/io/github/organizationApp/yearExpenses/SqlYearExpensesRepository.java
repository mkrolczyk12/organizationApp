package io.github.organizationApp.yearExpenses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface SqlYearExpensesRepository extends YearExpensesRepository, JpaRepository<YearExpenses,Integer> {
}

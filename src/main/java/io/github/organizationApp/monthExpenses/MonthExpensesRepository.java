package io.github.organizationApp.monthExpenses;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MonthExpensesRepository {
    MonthExpenses save(MonthExpenses entity);

    List<MonthExpenses> findAll();
    Page<MonthExpenses> findAll(Pageable page);
    Optional<MonthExpenses> findById(Integer id);
    boolean existsById(Integer id);
    boolean existsByMonth(String month);

    MonthExpenses saveAndFlush(MonthExpenses month);

    void deleteById(Integer id);
}

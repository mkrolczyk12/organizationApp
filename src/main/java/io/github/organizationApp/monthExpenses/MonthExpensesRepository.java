package io.github.organizationApp.monthExpenses;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MonthExpensesRepository {
    MonthExpenses save(MonthExpenses entity);

    List<MonthExpenses> findAll();
    Page<MonthExpenses> findAll(Pageable page);
    List<MonthExpenses> findAllByYearId(Integer yearId);
    Page<MonthExpenses> findAllByYearId(Pageable page, Integer yearId);
    Optional<MonthExpenses> findById(Integer id);
    MonthExpenses findByMonth(String month);
    Optional<MonthExpenses> findByMonthAndYearId(final String month, Integer yearId);

    MonthExpenses findByYearId(Integer yearId);
    boolean existsByYearId(Integer yearId);
    boolean existsByMonthAndYearId(String month, Integer yearId);

    boolean existsById(Integer id);
    boolean existsByMonth(String month);

    MonthExpenses saveAndFlush(MonthExpenses month);

    void deleteById(Integer id);
}

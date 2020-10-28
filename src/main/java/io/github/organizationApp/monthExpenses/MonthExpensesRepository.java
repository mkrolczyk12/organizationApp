package io.github.organizationApp.monthExpenses;

import io.github.organizationApp.yearExpenses.YearExpenses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MonthExpensesRepository {
    MonthExpenses save(MonthExpenses entity);
    MonthExpenses saveAndFlush(MonthExpenses month);

    List<MonthExpenses> findAllByYearIdAndOwnerId(Integer yearId, final String ownerId);
    Page<MonthExpenses> findAllByYearIdAndOwnerId(Pageable page, Integer yearId, final String ownerId);
    Optional<MonthExpenses> findByIdAndOwnerId(Integer id, final String ownerId);
    Optional<MonthExpenses> findByMonthAndOwnerId(String month, final String ownerId);
    Optional<MonthExpenses> findByMonthAndYearId(final String month, Integer yearId);
    MonthExpenses findByYearAndMonthAndOwnerId(final YearExpenses year, final String month, final String ownerId);

    boolean existsByMonthAndYearAndOwnerId(String month, YearExpenses year, final String ownerId);

    void deleteByIdAndOwnerId(Integer id, final String ownerId);

}

package io.github.organizationApp.yearExpenses;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface YearExpensesRepository {
    YearExpenses save(YearExpenses entity);

    List<YearExpenses> findAll();
    Page<YearExpenses> findAll(Pageable page);
    Optional<YearExpenses> findById(Integer id);
    boolean existsById(Integer id);
    boolean existsByYear(String year);

    YearExpenses saveAndFlush(YearExpenses month);

    void deleteById(Integer id);
}

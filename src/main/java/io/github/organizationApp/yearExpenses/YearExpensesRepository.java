package io.github.organizationApp.yearExpenses;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface YearExpensesRepository {
    YearExpenses save(YearExpenses entity);

    List<YearExpenses> findAllByOwnerId(final String ownerId);
    Page<YearExpenses> findAllByOwnerId(Pageable page, final String ownerId);
    Optional<YearExpenses> findByIdAndOwnerId(Integer id, final String ownerId);
    Optional<YearExpenses> findByYearAndOwnerId(short year, final String ownerId);
    boolean existsByIdAndOwnerId(Integer id, final String ownerId);
    boolean existsByYearAndOwnerId(short year, final String ownerId);

    YearExpenses saveAndFlush(YearExpenses month);

    void deleteByIdAndOwnerId(Integer id, final String ownerId);

}

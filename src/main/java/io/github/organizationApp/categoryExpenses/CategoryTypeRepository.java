package io.github.organizationApp.categoryExpenses;

import io.github.organizationApp.monthExpenses.MonthExpenses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryTypeRepository {

    CategoryType save(final CategoryType toCategory);

    List<CategoryType> findAllByMonthExpensesIdAndOwnerId(Integer id, final String ownerId);
    Page<CategoryType> findAllByMonthExpensesIdAndOwnerId(Pageable page, Integer id, final String ownerId);
    Optional<CategoryType> findByIdAndOwnerId(final Integer id, final String ownerId);

    boolean existsByTypeAndMonthExpenses_Id(String category, Integer monthId);
    boolean existsByTypeAndMonthExpensesAndOwnerId(String category, MonthExpenses month, final String ownerId);
    boolean existsById(Integer id);
    CategoryType existsByTypeAndOwnerId(String type, final String ownerId);

    CategoryType saveAndFlush(final CategoryType updatedCategoryType);

    void deleteById(final Integer id);

    boolean existsByMonthExpenses(final MonthExpenses monthExpenses);
}

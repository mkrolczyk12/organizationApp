package io.github.organizationApp.categoryExpenses;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryTypeRepository {

    CategoryType save(final CategoryType toCategory);

    List<CategoryType> findAll();
    Page<CategoryType> findAll(Pageable page);
    List<CategoryType> findAllByMonthExpensesId(Integer id);
    Page<CategoryType> findAllByMonthExpensesId(Pageable page, Integer id);
    Optional<CategoryType> findById(final Integer id);

    boolean existsByTypeAndMonthExpenses_Id(String category, Integer monthId);
    boolean existsById(Integer id);
    CategoryType existsByType(String type);

    CategoryType saveAndFlush(final CategoryType updatedCategoryType);

    void deleteById(final Integer id);
}

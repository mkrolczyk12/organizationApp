package io.github.organizationApp.expensesCategoryType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryTypeRepository {

    CategoryType save(final CategoryType toCategory);

    List<CategoryType> findAll();
    Page<CategoryType> findAll(Pageable page);
    Optional<CategoryType> findById(final Integer id);
    CategoryType existsByType(String type);
    Boolean existsAllByTypeIsIn(List<String> def_types);

    CategoryType saveAndFlush(final CategoryType updatedCategoryType);

    void deleteById(final Integer id);
}

package io.github.organizationApp.categoryExpenses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
interface SqlCategoryTypeRepository extends CategoryTypeRepository, JpaRepository<CategoryType,Integer> {
}

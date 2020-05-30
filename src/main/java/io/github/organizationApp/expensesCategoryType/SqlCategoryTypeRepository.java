package io.github.organizationApp.expensesCategoryType;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
interface SqlCategoryTypeRepository extends CategoryTypeRepository, JpaRepository<CategoryType,Integer> {
}

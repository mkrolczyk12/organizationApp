package io.github.organizationApp.expensesProcess;

import io.github.organizationApp.expensesCategoryType.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface SqlProcessRepository extends ProcessRepository, JpaRepository<Process, Long> {

//    @Override
//    @Query(nativeQuery = true, value = "SELECT * FROM EXPENSES_PROCESS p,EXPENSES_CATEGORY c WHERE p.CATEGORY_ID =:id AND p.CATEGORY_ID = c.ID;")
//    Optional<List<Process>> findAllByCategoryId(@Param("id") Integer id);
}

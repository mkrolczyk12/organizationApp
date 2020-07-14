package io.github.organizationApp.categoryExpenses;

import io.github.organizationApp.monthExpenses.MonthExpenses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
interface SqlCategoryTypeRepository extends CategoryTypeRepository, JpaRepository<CategoryType,Integer> {
    @Override
    @Query("from CategoryType c join fetch c.processes")
    List<CategoryType> findAll();
    // TODO -> trzeba porobic tak dla wsztstkich zarowno category,month jak i year

}

package io.github.organizationApp.expensesProcess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface SqlProcessRepository extends ProcessRepository, JpaRepository<Process, Long> {
    @Override
    @Query(nativeQuery = true, value = "SELECT count(*) > 0 FROM expenses_process WHERE id=:id")
    boolean existsById(@Param("id") Long id);

}

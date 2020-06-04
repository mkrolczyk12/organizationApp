package io.github.organizationApp.expensesProcess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
interface SqlProcessRepository extends ProcessRepository, JpaRepository<Process, Long> {
}

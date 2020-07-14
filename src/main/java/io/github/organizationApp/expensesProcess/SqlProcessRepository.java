package io.github.organizationApp.expensesProcess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
interface SqlProcessRepository extends ProcessRepository, JpaRepository<Process, Long> {
}

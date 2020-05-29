package io.github.organizationApp.expensesProcess;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProcessRepository {

    Process save(Process entity);
    List<Process> findAll();
    Page<Process> findAll(Pageable page);
    Optional<Process> findById(Long id);
    boolean existsById(Long id);
    Process saveAndFlush(Process process);
    void deleteById(Long id);
}

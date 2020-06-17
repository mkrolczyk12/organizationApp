package io.github.organizationApp.expensesProcess;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProcessRepository {

    Process save(Process entity);

    List<Process> findAllByOwnerId(String ownerId);
    Page<Process> findAllByOwnerId(Pageable page, String ownerId);
    Optional<List<Process>> findAllByCategory_IdAndOwnerId(Integer id, final String ownerId);
    Optional<Page<Process>> findAllByCategory_IdAndOwnerId(Pageable page, Integer id, final String ownerId);
    Optional<Process> findById(Long id);

    Process saveAndFlush(Process process);

    void deleteById(Long id);
}

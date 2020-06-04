package io.github.organizationApp.expensesProcess;

import io.github.organizationApp.expensesCategoryType.CategoryTypeRepository;
import io.github.organizationApp.monthExpenses.MonthExpensesRepository;
import io.github.organizationApp.yearExpenses.YearExpensesRepository;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class ProcessService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);
    private final YearExpensesRepository yearRepository;
    private final MonthExpensesRepository monthRepository;
    private final CategoryTypeRepository categoryRepository;
    private final ProcessRepository repository;

    ProcessService(final YearExpensesRepository yearRepository,
                   final MonthExpensesRepository monthRepository,
                   final CategoryTypeRepository categoryRepository,
                   final ProcessRepository repository) {

        this.yearRepository = yearRepository;
        this.monthRepository = monthRepository;
        this.categoryRepository = categoryRepository;
        this.repository = repository;
    }

    Process save(final Process entity) {
        return repository.save(entity);
    }

    @Async
    public CompletableFuture<List<Process>> findAllAsync() {
        return CompletableFuture.supplyAsync(() -> repository.findAll());
    }

    @Async
    CompletableFuture<Page<Process>> findAllAsync(Pageable page) {
        return CompletableFuture.supplyAsync(() -> repository.findAll(page));
    }

    public List<Process> findAll() {
        return repository.findAll();
    }

    Page<Process> findAll(Pageable page) {
        return repository.findAll(page);
    }

    Optional<Process> findById(final Long id) {
        return repository.findById(id);
    }

    Process saveAndFlush(final Process process) {
        return repository.saveAndFlush(process);
    }

    void deleteProcess(final Long id) {
        repository.deleteById(id);
    }

    /**
     *
     * @param year - String param 'year' given in URL
     * @param month - String param 'month' given in URL
     * @param category - String param 'category' given in URL
     * @return true or false
     */
    boolean processLevelValidationSuccess(final String year, final String month, final String category) {
        try {
            return monthRepository.findByMonthAndYearId(month, yearRepository.findByYear(year).get().getId())
                    .map(result -> {
                        if(categoryRepository.existsByTypeAndMonthExpenses_Id(category, result.getId())) {
                            return true;
                        } else
                            return false;
                    })
                    .orElseThrow( () -> new NotFoundException("process validation failed"));
        } catch (NotFoundException | NoSuchElementException e) {
            return false;
        }
    }

    /**
     *
     * @param processes - list of processes
     * @param PAGEABLE_PARAM_CHOSEN - Boolean param checks if any Page param is given in URL
     * @return - CollectionModel<PagedModel<PlainReadModel>> or CollectionModel<Process>
     */
    public CollectionModel<?> addEachProcessLink(final List<Process> processes, final boolean PAGEABLE_PARAM_CHOSEN) {

        for (Process each : processes) {
//            final String YEAR = each.getCategory().getMonthExpenses().getYear().getYear();
            final String YEAR = "Year_test";
//            final String MONTH = processes.get(0).getCategory().getMonthExpenses().getMonth();
            final String MONTH = "Month_test";
//            final String CATEGORY = processes.get(1).getCategory().getType();
            final String CATEGORY = "Category_test";
            each.add(linkTo(methodOn(ProcessController.class).readProcess(each.getId(), YEAR, MONTH, CATEGORY)).withRel("process"));
//            processes.forEach(process -> process.add(linkTo(methodOn(ProcessController.class).readProcess(process.getId(), YEAR, MONTH, CATEGORY)).withSelfRel()));
        }
        Link link1 = linkTo(methodOn(ProcessController.class).readProcesses("true")).withRel("all_processes");
        Link link2 = linkTo(methodOn(ProcessController.class).readProcesses("true")).withRel("all_processes?{sort,size,page}");
        if(PAGEABLE_PARAM_CHOSEN) {
            var pagedProcesses = new PageImpl<>(processes);
            return new CollectionModel(pagedProcesses, link1,link2);
        }
        return new CollectionModel<>(processes, link1,link2);
    }
}
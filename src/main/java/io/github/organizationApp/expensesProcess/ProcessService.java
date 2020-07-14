package io.github.organizationApp.expensesProcess;

import io.github.organizationApp.categoryExpenses.CategoryTypeController;
import io.github.organizationApp.categoryExpenses.CategoryTypeRepository;
import io.github.organizationApp.monthExpenses.MonthExpensesRepository;
import io.github.organizationApp.security.User;
import io.github.organizationApp.yearExpenses.YearExpensesRepository;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
class ProcessService {
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
    public CompletableFuture<List<Process>> findAllAsync(final String userId) {
        try {
            return CompletableFuture.supplyAsync(() -> repository.findAllByOwnerId(userId));
        } catch (NullPointerException | ClassCastException e) {
            throw new NoSuchElementException("no processes");
        }
    }

    @Async
    CompletableFuture<List<Process>> findAllAsync(Pageable page, String userId) {
        try {
            Page<Process> processes = CompletableFuture.supplyAsync(() -> repository.findAllByOwnerId(page, userId)).get();

            return CompletableFuture.completedFuture(processes.toList());
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Async finding failed, switched to normal finding");
            logger.info(e.getMessage());

            return CompletableFuture.completedFuture(findAll(page, userId).toList());
        } catch (NullPointerException e) {
            logger.info("no processes found");
            throw new NoSuchElementException("no processes found");
        }
    }

    List<Process> findAll(String ownerId) {
        return repository.findAllByOwnerId(ownerId);
    }

    Page<Process> findAll(Pageable page, String ownerId) {
        return repository.findAllByOwnerId(page, ownerId);
    }

    Process findById(final Long id, final String ownerId) throws NotFoundException {
        return repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException("process not found"));
    }

    boolean existsByIdAndOwnerId(final Long id, final String ownerId) {
        return repository.existsByIdAndOwnerId(id, ownerId);
    }

    Process saveAndFlush(final Process process) {
        return repository.saveAndFlush(process);
    }

    void deleteProcess(final Long id) {
        repository.deleteById(id);
    }

    /**
     *
     * @param year String param 'year' given in URL
     * @param month String param 'month' given in URL
     * @param category String param 'category' given in URL
     * @return true or false
     */
    boolean processLevelValidationSuccess(final short year, final String month, final String category, final String ownerId) {
        try {
            return monthRepository.findByMonthAndYearId(month, yearRepository.findByYearAndOwnerId(year, ownerId).get().getId())
                    .map(result -> {
                        if(categoryRepository.existsByTypeAndMonthExpenses_Id(category, result.getId())) {
                            return true;
                        } else
                            return false;
                    })
                    .orElseThrow(() -> new NotFoundException("process validation failed"));
        } catch (NotFoundException | NoSuchElementException e) {
            return false;
        }
    }

    /**
     *
     * @param processes list of processes
     * @param PAGEABLE_PARAM_CHOSEN Boolean param checks if any Page param is given in URL
     * @return CollectionModel<PagedModel<PlainReadModel>> or CollectionModel<Process>
     */
    CollectionModel<?> addEachProcessLink(final CompletableFuture<List<Process>> processes, final boolean PAGEABLE_PARAM_CHOSEN) throws NotFoundException, ExecutionException, InterruptedException {

        Link href1 = linkTo(methodOn(ProcessController.class).readProcesses()).withSelfRel();
        Link href2 = linkTo(methodOn(ProcessController.class).readProcesses()).withRel("?{sort,size,page}");

        try {
            return addEachProcessLinkHelpfulFunction(processes.get(), PAGEABLE_PARAM_CHOSEN, href1, href2);
        } catch (NullPointerException e) {

            logger.info("error while preparing processes hateoas - can't find year, month and category belonging to process (no processes)");
            return CollectionModel.empty(href1, href2);
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Async finding failed, switched to normal finding");
            logger.info(e.getMessage());
            List<Process> processesList = repository.findAllByOwnerId(User.getUserId());

            return addEachProcessLinkHelpfulFunction(processesList, PAGEABLE_PARAM_CHOSEN, href1, href2);
        }
    }

    private CollectionModel<?> addEachProcessLinkHelpfulFunction(final List<Process> processes, final boolean PAGEABLE_PARAM_CHOSEN, final Link href1, final Link href2) throws NotFoundException {
        for (Process each : processes) {
            final short YEAR = each.getCategory().getMonthExpenses().getYear().getYear();
            final String MONTH = each.getCategory().getMonthExpenses().getMonth();
            final String CATEGORY = each.getCategory().getType();

            each.add(linkTo(methodOn(ProcessController.class).readProcess(each.getId(), YEAR, MONTH, CATEGORY)).withRel("process"));
        }

        if(PAGEABLE_PARAM_CHOSEN) {
            var pagedProcesses = new PageImpl(processes);
            return CollectionModel.of(pagedProcesses, href1,href2);
        }
        return CollectionModel.of(processes, href1,href2);
    }

    /**
     *
     * @param process a single process
     * @param id Long param 'id' given in URL
     * @param year String param 'year' given in URL
     * @param month String param 'month' given in URL
     * @param category String param 'category' given in URL
     * @return EntityModel<Process>
     * @throws NotFoundException
     */
    EntityModel<Process> prepareProcessHateoas(final Process process, final Long id, final short year, final String month, final String category) throws NotFoundException {

        process.add(linkTo(methodOn(ProcessController.class).readProcess(id, year, month, category)).withRel("allowed_queries: GET,PUT,PATCH,DELETE"));
        Link href1 = linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(process.getCategory().getId(), year, month)).withRel("chosen_category_processes");
        Link href2 = linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(process.getCategory().getId(), year, month)).withRel("chosen_category_processes?{sort,size,page}");
        Link href3 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year, month)).withRel("categories");
        Link href4 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year, month)).withRel("categories?{sort,size,page}");

        EntityModel<Process> processModel = EntityModel.of(process,href1, href2, href3, href4);

        return processModel;
    }
}
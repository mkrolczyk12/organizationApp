package io.github.organizationApp.categoryExpenses;

import io.github.organizationApp.categoryExpenses.projection.CategoryFullReadModel;
import io.github.organizationApp.categoryExpenses.projection.CategoryFullWriteModel;
import io.github.organizationApp.categoryExpenses.projection.CategoryNoProcessesReadModel;
import io.github.organizationApp.categoryExpenses.projection.CategoryProcessReadModel;
import io.github.organizationApp.expensesProcess.Process;
import io.github.organizationApp.expensesProcess.ProcessController;
import io.github.organizationApp.expensesProcess.ProcessRepository;
import io.github.organizationApp.monthExpenses.MonthExpenses;
import io.github.organizationApp.monthExpenses.MonthExpensesController;
import io.github.organizationApp.monthExpenses.MonthExpensesRepository;
import io.github.organizationApp.yearExpenses.YearExpenses;
import io.github.organizationApp.yearExpenses.YearExpensesRepository;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class CategoryTypeService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryTypeService.class);
    private final YearExpensesRepository yearRepository;
    private final MonthExpensesRepository monthRepository;
    private final CategoryTypeRepository repository;
    private final ProcessRepository processesRepository;

    CategoryTypeService(final YearExpensesRepository yearRepository,
                        final MonthExpensesRepository monthRepository,
                        final CategoryTypeRepository repository,
                        final ProcessRepository processesRepository) {

        this.yearRepository = yearRepository;
        this.monthRepository = monthRepository;
        this.repository = repository;
        this.processesRepository = processesRepository;
    }

    CategoryType save(final CategoryType toCategory) {return repository.save(toCategory);}

    Process addProcess(final Process toProcess) {
        return processesRepository.save(toProcess);
    }

    void setMonthAndOwnerToNewCategory(final short year, final String month, final CategoryType toCategory, String ownerId) throws NotFoundException {
        final YearExpenses Year = yearRepository.findByYearAndOwnerId(year, ownerId)
                .orElseThrow(() -> new NotFoundException("no year founded"));
        MonthExpenses Month = monthRepository.findByMonthAndYearId(month, Year.getId())
                .orElseThrow(() -> new NotFoundException("no month founded"));
        toCategory.setOwnerId(ownerId);
        toCategory.setMonthExpenses(Month);
    }

    void setCategoryAndOwnerToNewProcess(final Integer categoryId, final Process toProcess, final String userId) throws NotFoundException {
        CategoryType category = repository.findByIdAndOwnerId(categoryId, userId)
                .orElseThrow(() -> new NotFoundException("no category matches the given query"));
        toProcess.setCategory(category);
        toProcess.setOwnerId(userId);
    }

    CategoryFullReadModel createCategoryWithProcesses(final MonthExpenses month, final String userId, final CategoryFullWriteModel source) {
        try {
            CategoryType result = repository.save(source.toCategoryType(month, userId));

            return new CategoryFullReadModel(result);
        } catch (DataAccessException e) {
            throw new RuntimeException("an error occurred while working with given data");
        }

    }

    List<?> findAllByMonthExpensesId(final short year, final String month, final String ownerId, boolean PROCESSES_FLAG_CHOSEN) throws NotFoundException {

        Integer yearId  = yearRepository.findByYearAndOwnerId(year, ownerId)
                .map(result -> result.getId())
                .orElseThrow(() -> new NotFoundException("no year for given parameter"));

        return monthRepository.findByMonthAndYearId(month, yearId)
                .map(Month -> Month.getCategories()
                    .stream()
                    .map(PROCESSES_FLAG_CHOSEN ? CategoryFullReadModel::new : CategoryNoProcessesReadModel::new)
                    .collect(Collectors.toList()))
                .orElseThrow(() -> new NotFoundException("no month for given parameter"));
    }

    Page<?> findAllByMonthExpensesId(final Pageable page, final short year, final String month, final String ownerId, boolean PROCESSES_FLAG_CHOSEN) throws NotFoundException {

        Integer yearId  = yearRepository.findByYearAndOwnerId(year, ownerId)
                .map(result -> result.getId())
                .orElseThrow(() -> new NotFoundException("no year for given parameter"));

        return monthRepository.findByMonthAndYearId(month, yearId)
                .map(Month -> {
                    var monthId = Month.getId();
                    Page<CategoryType> paged_categories = repository.findAllByMonthExpensesIdAndOwnerId(page,monthId, ownerId);
                    List<?> items = paged_categories.toList()
                            .stream()
                            .map(PROCESSES_FLAG_CHOSEN ? CategoryFullReadModel::new : CategoryNoProcessesReadModel::new)
                            .collect(Collectors.toList());
                    return new PageImpl<>(items);
                })
                .orElseThrow(() -> new NotFoundException("no month for given parameter"));
    }

    List<Process> findAllProcessesBelongToCategory(final Integer Id, final String ownerId) throws NotFoundException {
        return processesRepository.findAllByCategory_IdAndOwnerId(Id, ownerId)
                .orElseThrow(() -> new NotFoundException("no processes found for given category"));
    }

    Page<Process> findAllProcessesBelongToCategory(Pageable page, final Integer Id, final String ownerId) throws NotFoundException {
        return processesRepository.findAllByCategory_IdAndOwnerId(page, Id, ownerId)
                .orElseThrow(() -> new NotFoundException("no processes found for given category"));
    }

    CategoryType findById(final Integer id, final String ownerId) throws NotFoundException {
        return repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException("no category found"));
    }

    MonthExpenses findByMonth(final String monthName, final String ownerId) throws NotFoundException {
        return monthRepository.findByMonthAndOwnerId(monthName, ownerId)
                .orElseThrow(() -> new NotFoundException("no month found"));
    }

    MonthExpenses findByMonthAndBelongingYear(short year, String month, String ownerId) throws NotFoundException {
        return monthRepository.findByMonthAndYearId(month, yearRepository.findByYearAndOwnerId(year, ownerId).get().getId())
                .orElseThrow(() -> new NotFoundException("no month found"));
    }

    boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    CategoryType saveAndFlush(final CategoryType updatedCategoryType) {
        return repository.saveAndFlush(updatedCategoryType);
    }

    void deleteCategoryType(final Integer id) {
        repository.deleteById(id);
    }

    /**
     *
     * @param year - param 'year' given in URL
     * @param month - param 'month' given in URL
     * @return true or false
     */
    boolean categoryTypeLevelValidationSuccess(final short year, final String month, final String ownerId) {
        try {
            return yearRepository.findByYearAndOwnerId(year, ownerId)
                    .map(result -> {
                        if(monthRepository.existsByMonthAndYearAndOwnerId(month, result, ownerId)) {
                            return true;
                        } else
                            return false;
                    })
                    .orElseThrow( () -> new NotFoundException("category validation failed, no relation between given year and month"));
        } catch (NotFoundException | NullPointerException | NoSuchElementException e) {
            return false;
        }
    }

    public boolean checkIfGivenCategoryExist(final String categoryType, final MonthExpenses month, final String userId) throws NotFoundException {
        try {
            if(repository.existsByTypeAndMonthExpensesAndOwnerId(categoryType, month, userId)) {
                return true;
            } else
                return false;
        } catch (Exception e) {
            throw new NotFoundException("given category does not exist!");
        }

    }

    /**
     *
     * @param unknownCategories - List of categories
     * @param year - short param 'year' given in URL
     * @param month - String param 'month' given in URL
     * @param PAGEABLE_PARAM_CHOSEN - Boolean param checks if any Page param is given in URL
     * @param PROCESSES_FLAG_CHOSEN - Boolean param choose if categories should include processes or not
     * @return - CollectionModel<PagedModel<PlainReadModel>> or CollectionModel<PlainReadModel>
     */
    CollectionModel<?> prepareReadCategoryTypesHateoas(final List<?> unknownCategories,
                                                            final short year,
                                                            final String month,
                                                            final String ownerId,
                                                            final boolean PAGEABLE_PARAM_CHOSEN,
                                                            final boolean PROCESSES_FLAG_CHOSEN) throws NotFoundException {

        final Integer yearId = yearRepository.findByYearAndOwnerId(year, ownerId).get().getId();
        final Integer monthId = monthRepository.findByMonthAndYearId(month, yearId).get().getId();

        final Link href1 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withSelfRel();
        final Link href2 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withRel("category?{sort,size,page}");
        final Link href3 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withRel("?{processes} -> required parameter to POST category with processes");
        final Link href4 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withRel("?{processes} -> required parameter to GET categories with all processes");
        final Link href5 = linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(monthId, year)).withRel("month");

        if(PROCESSES_FLAG_CHOSEN) {
            List<CategoryFullReadModel> categories = (List<CategoryFullReadModel>) unknownCategories;

            categories.forEach(Category -> {
                List<CategoryProcessReadModel> processes = Category.getProcesses();
                final String category = Category.getType();
                processes.forEach(process -> {
                    try {
                        process.add(linkTo(methodOn(ProcessController.class).readProcess(process.getId(), year, month, category)).withRel("allowed_queries: GET,PUT,PATCH,?{DELETE}"));
                    } catch (NotFoundException ignored) {
                    }
                });
                try {
                    Category.add(linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(Category.getId(), year, month)).withRel("category allowed_queries: POST,GET,PUT,PATCH,?{DELETE}"));
                } catch (NotFoundException ignored) {
                }
            });

            if(PAGEABLE_PARAM_CHOSEN) {
                var pagedCategories = new PageImpl<>(categories);
                return CollectionModel.of(pagedCategories, href1, href2, href3, href4, href5);
            } else {
                return CollectionModel.of(categories, href1, href2, href3, href4, href5);
            }
        }
        else {
            List<CategoryNoProcessesReadModel> categories = (List<CategoryNoProcessesReadModel>) unknownCategories;

            categories.forEach(category -> {
                try {
                    category.add(linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(category.getId(), year, month)).withRel("category allowed_queries: POST,GET,PUT,PATCH,?{DELETE}"));
                } catch (NotFoundException ignored) {}
            });

            if(PAGEABLE_PARAM_CHOSEN) {
                var pagedCategories = new PageImpl<>(categories);
                return CollectionModel.of(pagedCategories, href1, href2, href3, href4, href5);
            } else {
                return CollectionModel.of(categories, href1, href2, href3, href4, href5);
            }
        }
    }

    /**
     *
     * @param processes - List of processes
     * @param year - String param 'year' given in URL
     * @param month - String param 'month' given in URL
     * @param category - String param 'category' given in URL
     * @param PAGEABLE_PARAM_CHOSEN - Boolean param checks if any Page param is given in URL
     * @return - CollectionModel<PagedModel<Process>> or CollectionModel<Process>
     */
    CollectionModel<?> prepareReadOneCategoryTypeContentHateoas(final List<Process> processes,
                                                                final short year,
                                                                final String month,
                                                                final String category,
                                                                final boolean PAGEABLE_PARAM_CHOSEN) throws NotFoundException {

        processes.forEach(process -> {
            try {
                process.add(linkTo(methodOn(ProcessController.class).readProcess(process.getId(), year, month, category)).withRel("allowed_queries: GET,PUT,PATCH,?{DELETE}"));
            } catch (NotFoundException ignored) {}
        });
        final Integer categoryId = processes.get(0).getCategory().getId();
        final Link href1 = linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(categoryId, year, month)).withSelfRel();
        final Link href2 = linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(categoryId, year, month)).withRel("POST_process");
        final Link href3 = linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(categoryId, year, month)).withRel("?{sort,size,page}");
        final Link href4 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withRel("categories");
        final Link href5 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withRel("categories?{sort,size,page}");

        if(PAGEABLE_PARAM_CHOSEN) {
            var pagedProcesses = new PageImpl<>(processes);
            return CollectionModel.of(pagedProcesses, href1, href2, href3, href4, href5);

        } else {
            return CollectionModel.of(processes, href1, href2, href3, href4, href5);
        }
    }
}

package io.github.organizationApp.categoryExpenses;

import io.github.organizationApp.expensesProcess.Process;
import io.github.organizationApp.expensesProcess.ProcessController;
import io.github.organizationApp.expensesProcess.ProcessRepository;
import io.github.organizationApp.monthExpenses.MonthExpenses;
import io.github.organizationApp.monthExpenses.MonthExpensesController;
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

    CategoryType save(final CategoryType toCategory) {
        return repository.save(toCategory);
    }

    Process addProcess(final Process toProcess) {
        return processesRepository.save(toProcess);
    }

    void setMonthToNewCategory(final String year, final String month, final CategoryType toCategory) throws NotFoundException {
        final Integer yearId = yearRepository.findByYear(year).get().getId();
        MonthExpenses Month = monthRepository.findByMonthAndYearId(month, yearId)
                .orElseThrow(() -> new NotFoundException("no month founded"));
        toCategory.setMonthExpenses(Month);
    }

    void setCategoryToNewProcess(final Integer categoryId, final Process toProcess) throws NotFoundException {
        CategoryType category = repository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("no category matches the given query"));
        toProcess.setCategory(category);
    }

    CategoryFullReadModel createCategoryWithProcesses(final MonthExpenses month, final CategoryFullWriteModel source) {
        CategoryType result = repository.save(source.toCategoryType(month));

        return new CategoryFullReadModel(result);
    }

    List<?> findAllByMonthExpensesId(final String year, final String month, boolean PROCESSES_FLAG_CHOSEN) throws NotFoundException {

        Integer yearId  = yearRepository.findByYear(year)
                .map(result -> result.getId())
                .orElseThrow(() -> new NotFoundException("no year for given parameter"));

        return monthRepository.findByMonthAndYearId(month, yearId)
                .map(Month -> Month.getCategories()
                    .stream()
                    .map(PROCESSES_FLAG_CHOSEN ? CategoryFullReadModel::new : CategoryNoProcessesReadModel::new)
                    .collect(Collectors.toList()))
                .orElseThrow(() -> new NotFoundException("no month for given parameter"));
    }

    Page<?> findAllByMonthExpensesId(final Pageable page, final String year, final String month, boolean PROCESSES_FLAG_CHOSEN) throws NotFoundException {

        Integer yearId  = yearRepository.findByYear(year)
                .map(result -> result.getId())
                .orElseThrow(() -> new NotFoundException("no year for given parameter"));

        return monthRepository.findByMonthAndYearId(month, yearId)
                .map(Month -> {
                    var monthId = Month.getId();
                    Page<CategoryType> paged_categories = repository.findAllByMonthExpensesId(page,monthId);
                    List<?> items = paged_categories.toList()
                            .stream()
                            .map(PROCESSES_FLAG_CHOSEN ? CategoryFullReadModel::new : CategoryNoProcessesReadModel::new)
                            .collect(Collectors.toList());
                    return new PageImpl<>(items);
                })
                .orElseThrow(() -> new NotFoundException("no month for given parameter"));
    }

    List<Process> findAllProcessesBelongToCategory(final Integer Id) throws NotFoundException {
        return processesRepository.findAllByCategory_Id(Id)
                .orElseThrow(() -> new NotFoundException("no processes found for given category"));
    }

    Page<Process> findAllProcessesBelongToCategory(Pageable page, final Integer Id) throws NotFoundException {
        return processesRepository.findAllByCategory_Id(page, Id)
                .orElseThrow(() -> new NotFoundException("no processes found for given category"));
    }

    CategoryType findById(final Integer id) throws NotFoundException {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("no category found"));
    }

    MonthExpenses findByMonth(final String monthName) throws NotFoundException {
        return monthRepository.findByMonth(monthName)
                .orElseThrow(() -> new NotFoundException("no month found"));
    }

    MonthExpenses findByMonthAndBelongingYear(String YEAR_PARAM, String MONTH_PARAM) throws NotFoundException {
        return monthRepository.findByMonthAndYearId(MONTH_PARAM, yearRepository.findByYear(YEAR_PARAM).get().getId())
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
    boolean categoryTypeLevelValidationSuccess(final String year, final String month) {
        try {
            return yearRepository.findByYear(year)
                    .map(result -> {
                        if(monthRepository.existsByMonthAndYear(month, result)) {
                            return true;
                        } else
                            return false;
                    })
                    .orElseThrow( () -> new NotFoundException("no year founded"));
        } catch (NotFoundException e) {
            return false;
        }
    }

    public boolean checkIfGivenCategoryExist(final String categoryType, MonthExpenses month) {
        if(repository.existsByTypeAndMonthExpenses(categoryType, month)) {
            return true;
        } else
            return false;
    }

    /**
     *
     * @param unknownCategories - List of categories
     * @param year - String param 'year' given in URL
     * @param month - String param 'month' given in URL
     * @param PAGEABLE_PARAM_CHOSEN - Boolean param checks if any Page param is given in URL
     * @param PROCESSES_FLAG_CHOSEN - Boolean param choose if categories should include processes or not
     * @return - CollectionModel<PagedModel<PlainReadModel>> or CollectionModel<PlainReadModel>
     */
    CollectionModel<?> prepareReadCategoryTypesHateoas(final List<?> unknownCategories,
                                                            final String year,
                                                            final String month,
                                                            final boolean PAGEABLE_PARAM_CHOSEN,
                                                            final boolean PROCESSES_FLAG_CHOSEN) {

        final Integer yearId = yearRepository.findByYear(year).get().getId();
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
                processes.forEach(process -> process.add(linkTo(methodOn(ProcessController.class).readProcess(process.getId(), year, month, category)).withRel("allowed_queries: GET,PUT,PATCH,?{DELETE}")));
                Category.add(linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(Category.getId(), year, month)).withRel("category allowed_queries: POST,GET,PUT,PATCH,?{DELETE}"));
            });

            if(PAGEABLE_PARAM_CHOSEN) {
                var pagedCategories = new PageImpl<>(categories);
                return new CollectionModel(pagedCategories, href1, href2, href3, href4, href5);
            } else {
                return new CollectionModel(categories, href1, href2, href3, href4, href5);
            }
        }
        else {
            List<CategoryNoProcessesReadModel> categories = (List<CategoryNoProcessesReadModel>) unknownCategories;

            categories.forEach(category -> category.add(linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(category.getId(), year, month)).withRel("category allowed_queries: POST,GET,PUT,PATCH,?{DELETE}")));

            if(PAGEABLE_PARAM_CHOSEN) {
                var pagedCategories = new PageImpl<>(categories);
                return new CollectionModel(pagedCategories, href1, href2, href3, href4, href5);
            } else {
                return new CollectionModel(categories, href1, href2, href3, href4, href5);
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
                                                                final String year,
                                                                final String month,
                                                                final String category,
                                                                final boolean PAGEABLE_PARAM_CHOSEN) {

        processes.forEach(process -> process.add(linkTo(methodOn(ProcessController.class).readProcess(process.getId(), year, month, category)).withRel("allowed_queries: GET,PUT,PATCH,?{DELETE}")));
        final Integer categoryId = processes.get(0).getCategory().getId();
        final Link href1 = linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(categoryId, year, month)).withSelfRel();
        final Link href2 = linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(categoryId, year, month)).withRel("POST_process");
        final Link href3 = linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(categoryId, year, month)).withRel("?{sort,size,page}");
        final Link href4 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withRel("categories");
        final Link href5 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withRel("categories?{sort,size,page}");

        if(PAGEABLE_PARAM_CHOSEN) {
            var pagedProcesses = new PageImpl<>(processes);
            return new CollectionModel(pagedProcesses, href1, href2, href3, href4, href5);

        } else {
            return new CollectionModel(processes, href1, href2, href3, href4, href5);
        }
    }
}

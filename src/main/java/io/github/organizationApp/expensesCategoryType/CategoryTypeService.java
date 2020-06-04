package io.github.organizationApp.expensesCategoryType;

import io.github.organizationApp.expensesProcess.Process;
import io.github.organizationApp.expensesProcess.ProcessController;
import io.github.organizationApp.expensesProcess.ProcessRepository;
import io.github.organizationApp.monthExpenses.MonthExpenses;
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
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.List;
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

    void setCategoryToNewProcess(final Integer categoryId, final Process toProcess) throws NotFoundException {
        CategoryType category = repository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("no category with given id"));
        toProcess.setCategory(category);
    }

    PlainReadModel createCategoryWithProcesses(final MonthExpenses month, final PlainWriteModel source) {
        CategoryType result = repository.save(source.toCategoryType(month));

        return new PlainReadModel(result);
    }

    List<PlainReadModel> findAll() {
            List<CategoryType> categories = repository.findAll();
            return categories
                        .stream()
                        .map(PlainReadModel::new)
                        .collect(Collectors.toList());
    }

    Page<PlainReadModel> findAll(Pageable page) {
            Page<CategoryType> paged_categories = repository.findAll(page);
            List<PlainReadModel> items = paged_categories.toList()
                .stream()
                .map(PlainReadModel::new)
                .collect(Collectors.toList());

            return new PageImpl(items);
        }

    public List<Process> findAllProcessesBelongToCategory(Integer Id) throws NotFoundException {
        return processesRepository.findAllByCategory_Id(Id)
                .orElseThrow(() -> new NotFoundException("no processes found for given category"));
    }

    public Page<Process> findAllProcessesBelongToCategory(Pageable page, Integer Id) throws NotFoundException {
        return processesRepository.findAllByCategory_Id(page, Id)
                .orElseThrow(() -> new NotFoundException("no processes found for given category"));
    }

    CategoryType findById(final Integer id) throws NotFoundException {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("no processes found for given category"));
    }

    MonthExpenses findByMonthAndBelongingYear(String YEAR_PARAM, String MONTH_PARAM) throws NotFoundException {
        return monthRepository.findByMonthAndYearId(MONTH_PARAM, yearRepository.findByYear(YEAR_PARAM).get().getId())
                .orElseThrow(() -> new NotFoundException(""));
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
    boolean categoryTypeLevelValidationSuccess(String year, String month) {
        try {
            return yearRepository.findByYear(year)
                    .map(result -> {
                        if(monthRepository.existsByMonthAndYearId(month, result.getId())) {
                            return true;
                        } else
                            return false;
                    })
                    .orElseThrow( () -> new NotFoundException("no year founded"));
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     *
     * @param categories - List of categories
     * @param year - String param 'year' given in URL
     * @param month - String param 'month' given in URL
     * @param PAGEABLE_PARAM_CHOSEN - Boolean param checks if any Page param is given in URL
     * @return - CollectionModel<PagedModel<PlainReadModel>> or CollectionModel<PlainReadModel>
     */
    CollectionModel<?> prepareReadEmptyCategoryTypesHateoas(final List<PlainReadModel> categories,
                                                            final String year,
                                                            final String month,
                                                            final boolean PAGEABLE_PARAM_CHOSEN) {

        categories.forEach(category -> category.add(linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(category.getId(), year, month)).withRel("allowed_queries: POST,GET,PUT,PATCH,?{DELETE}")));
        Link link1 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withRel("?{processes} -> req. to POST category with processes");
        Link link2 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withSelfRel();
        Link link3 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withRel("categories?{sort,size,page}");
        // TODO - > zamienic link4 oraz 5 na link do konkretnego miesiaca (tego z którego przeszedlem do danej kategorii)
        Link link4 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withRel("month_categories");
        Link link5 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withRel("month_categories?{sort,size,page}");

        if(PAGEABLE_PARAM_CHOSEN) {
            var pagedCategories = new PageImpl<>(categories);
            return new CollectionModel(pagedCategories, link1, link2, link3, link4, link5);
        } else {
            return new CollectionModel(categories, link1, link2, link3, link4, link5);
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
        Link link1 = linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(categoryId, year, month)).withSelfRel();
        Link link2 = linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(categoryId, year, month)).withRel("POST_process");
        Link link3 = linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(categoryId, year, month)).withRel("?{sort,size,page}");
        Link link4 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withRel("categories");
        Link link5 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(year,month)).withRel("categories?{sort,size,page}");

        if(PAGEABLE_PARAM_CHOSEN) {
            var pagedProcesses = new PageImpl<>(processes);
            return new CollectionModel(pagedProcesses, link1, link2, link3, link4,link5);

        } else {
            return new CollectionModel(processes, link1, link2, link3, link4,link5);
        }
    }
}

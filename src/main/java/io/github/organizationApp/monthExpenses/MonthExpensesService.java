package io.github.organizationApp.monthExpenses;

import io.github.organizationApp.categoryExpenses.*;
import io.github.organizationApp.yearExpenses.YearExpenses;
import io.github.organizationApp.yearExpenses.YearExpensesController;
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
public class MonthExpensesService {
    private static final Logger logger = LoggerFactory.getLogger(MonthExpensesService.class);
    private final YearExpensesRepository yearRepository;
    private final MonthExpensesRepository repository;
    private final CategoryTypeRepository categoryRepository;
    private final CategoryTypeService categoryService;

    MonthExpensesService(final YearExpensesRepository yearRepository,
                         final MonthExpensesRepository repository,
                         final CategoryTypeRepository categoryRepository,
                         final CategoryTypeService categoryService) {

        this.yearRepository = yearRepository;
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }


    MonthExpenses save(final MonthExpenses month) {return repository.save(month);}

    CategoryType addCategory(final CategoryType toCategoryType) {
        return categoryRepository.save(toCategoryType);
    }

    void setYearToNewMonth(final String year, final MonthExpenses toMonth) throws NotFoundException {
        final YearExpenses Year = yearRepository.findByYear(year)
                .orElseThrow(() -> new NotFoundException("no year with given id"));
        toMonth.setYear(Year);
    }

    void setMonthToNewCategory(final Integer monthId, final CategoryType toCategoryType) throws NotFoundException {
        MonthExpenses month = repository.findById(monthId)
                .orElseThrow(() -> new NotFoundException("no category with given id"));
        toCategoryType.setMonthExpenses(month);
    }

    MonthFullReadModel createMonthWithCategories(final YearExpenses belongingYear, final MonthFullWriteModel source) {
        MonthExpenses result = repository.save(source.toMonth(belongingYear));

        return new MonthFullReadModel(result);
    }

    List<MonthExpenses> findAll() {
        return repository.findAll();
    }

    Page<MonthExpenses> findAll(Pageable page) {
        return repository.findAll(page);
    }

    List<CategoryNoProcessesReadModel> findAllCategoriesBelongToMonth(final Integer monthId) {
        return categoryRepository.findAllByMonthExpensesId(monthId)
                .stream()
                .map(CategoryNoProcessesReadModel::new)
                .collect(Collectors.toList());
    }

    Page<CategoryNoProcessesReadModel> findAllCategoriesBelongToMonth(Pageable page, final Integer monthId) {
        List<CategoryNoProcessesReadModel> categories = categoryRepository.findAllByMonthExpensesId(page, monthId)
                .stream()
                .map(CategoryNoProcessesReadModel::new)
                .collect(Collectors.toList());

        return new PageImpl<>(categories);
    }

    List<?> findAllByYear(final String year, final boolean CATEGORIES_FLAG_CHOSEN) throws NotFoundException {

        Integer yearId  = yearRepository.findByYear(year)
                .map(result -> result.getId())
                .orElseThrow(() -> new NotFoundException("no year with given parameter"));

        if(CATEGORIES_FLAG_CHOSEN) {
            return repository.findAllByYearId(yearId)
                    .stream()
                    .map(MonthFullReadModel::new)
                    .collect(Collectors.toList());
        } else {
            return repository.findAllByYearId(yearId)
                    .stream()
                    .map(MonthNoCategoriesReadModel::new)
                    .collect(Collectors.toList());
        }
    }

    Page<?> findAllByYear(final Pageable page, final String year, final boolean CATEGORIES_FLAG_CHOSEN) throws NotFoundException {

        Integer yearId  = yearRepository.findByYear(year)
                .map(result -> result.getId())
                .orElseThrow(() -> new NotFoundException("no year with given parameter"));

        Page<MonthExpenses> pagedMonths = repository.findAllByYearId(page, yearId);
        List<?> items;
        if(CATEGORIES_FLAG_CHOSEN) {
            items = pagedMonths.toList()
                    .stream()
                    .map(MonthFullReadModel::new)
                    .collect(Collectors.toList());
        } else {
            items = pagedMonths.toList()
                    .stream()
                    .map(MonthNoCategoriesReadModel::new)
                    .collect(Collectors.toList());
        }
        return new PageImpl(items);
    }

    MonthExpenses findById(final Integer id) throws NotFoundException {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("no month found"));
    }

    YearExpenses findByYear(final String year) throws NotFoundException {
        return yearRepository.findByYear(year)
                .orElseThrow(() -> new NotFoundException("no year found"));
    }

    MonthExpenses findByMonth(final String month) throws NotFoundException {
        return repository.findByMonth(month)
                .orElseThrow(() -> new NotFoundException("no month found"));
    }

    public boolean existsByMonth(String month) {
        return repository.existsByMonth(month);
    }

    MonthExpenses findByYearId(Integer yearId) {
        return repository.findByYearId(yearId);
    }

    public boolean existsByYearId(Integer yearId) {
        return repository.existsByYearId(yearId);
    }

    boolean existsByMonthAndYearId(String month, Integer yearId) {
        return repository.existsByMonthAndYearId(month,yearId);
    }

    MonthExpenses saveAndFlush(final MonthExpenses updatedMonth) {
        return repository.saveAndFlush(updatedMonth);
    }

    void deleteMonth(final Integer id) {
        repository.deleteById(id);
    }

    boolean monthLevelValidationSuccess(final String year) {
            return yearRepository.existsByYear(year);
    }

    public boolean checkIfGivenMonthNameExist(final String monthName, final YearExpenses year) {
        if(repository.existsByMonthAndYear(monthName, year)) {
            return true;
        } else
            return false;
    }

    boolean checkIfCategoryExistInGivenMonth(final String categoryName, final MonthExpenses month) {
        return categoryService.checkIfGivenCategoryExist(categoryName, month);
    }


    /**
     *
     * @param unknownMonths List of months
     * @param year String param 'year' given in URL
     * @param PAGEABLE_PARAM_CHOSEN Boolean param checks if any Page param is given in URL
     * @param CATEGORIES_FLAG_CHOSEN Boolean param choose if months should include categories or not
     * @return CollectionModel<PagedModel<MonthFullReadModel>> or CollectionModel<MonthFullReadModel>
     *          or CollectionModel<PagedModel<MonthNoCategoriesReadModel>> or CollectionModel<MonthNoCategoriesReadModel>
     */
    CollectionModel<?> prepareReadMonthsHateoas(final List<?> unknownMonths,
                                                final String year,
                                                final boolean PAGEABLE_PARAM_CHOSEN,
                                                final boolean CATEGORIES_FLAG_CHOSEN) {

        final Integer yearId = yearRepository.findByYear(year).get().getId();
        final Link href1 = linkTo(methodOn(MonthExpensesController.class).readEmptyMonths(year)).withSelfRel();
        final Link href2 = linkTo(methodOn(MonthExpensesController.class).readEmptyMonths(year)).withRel("month?{sort,size,page}");
        final Link href3 = linkTo(methodOn(MonthExpensesController.class).readEmptyMonths(year)).withRel("?{categories} -> required parameter to POST month with categories");
        final Link href4 = linkTo(methodOn(MonthExpensesController.class).readEmptyMonths(year)).withRel("?{categories} -> required parameter to GET month with all categories");
        final Link href5 = linkTo(methodOn(YearExpensesController.class).readOneYearContent(yearId)).withRel("year");

        if(CATEGORIES_FLAG_CHOSEN) {
            List<MonthFullReadModel> months = (List<MonthFullReadModel>) unknownMonths;

            months.forEach(Month -> {
                final String monthType = Month.getMonth();
                List<CategoryNoProcessesReadModel> categories = Month.getCategories();
                categories.forEach(category -> category.add(linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(category.getId(), year, monthType)).withRel("category allowed_queries: POST,GET,PUT,PATCH,?{DELETE}")));
                Month.add(linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(Month.getId(), year)).withRel("month allowed_queries: POST,GET,PUT,PATCH,?{DELETE}"));
            });

            if(PAGEABLE_PARAM_CHOSEN) {
                var pagedMonths = new PageImpl<>(months);
                return new CollectionModel(pagedMonths, href1, href2, href3, href4, href5);
            } else {
                return new CollectionModel(months, href1, href2, href3, href4, href5);
            }
        }
        else {
            List<MonthNoCategoriesReadModel> months = (List<MonthNoCategoriesReadModel>) unknownMonths;

            months.forEach(Month -> Month.add(linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(Month.getId(), year)).withRel("month allowed_queries: POST,GET,PUT,PATCH,?{DELETE}")));

            if(PAGEABLE_PARAM_CHOSEN) {
                var pagedCategories = new PageImpl<>(months);
                return new CollectionModel(pagedCategories, href1, href2, href3, href4, href5);
            } else {
                return new CollectionModel(months, href1, href2, href3, href4, href5);
            }
        }
    }

    /**
     *
     * @param categories list of categories with no processes
     * @param year year String param 'year' given in URL
     * @param month month String param 'month' given in URL
     * @param PAGEABLE_PARAM_CHOSEN Boolean param checks if any Page param is given in URL
     * @return CollectionModel<PagedModel<CategoryNoProcessesReadModel>> or CollectionModel<CategoryNoProcessesReadModel>
     */
    CollectionModel<?> prepareReadOneMonthContentHateoas(final List<CategoryNoProcessesReadModel> categories,
                                                         final String year,
                                                         final String month,
                                                         final boolean PAGEABLE_PARAM_CHOSEN) {

        categories.forEach(category -> category.add(linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(category.getId(), year, month)).withRel("allowed_queries: GET,PUT,PATCH,?{DELETE}")));
        final Integer yearId = yearRepository.findByYear(year).get().getId();
        final Integer monthId = repository.findByMonthAndYearId(month, yearId).get().getId();
        final Link href1 = linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(monthId, year)).withSelfRel();
        final Link href2 = linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(monthId, year)).withRel("POST_category");
        final Link href3 = linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(monthId, year)).withRel("?{sort,size,page}");
        final Link href4 = linkTo(methodOn(MonthExpensesController.class).readEmptyMonths(year)).withRel("months");
        final Link href5 = linkTo(methodOn(MonthExpensesController.class).readEmptyMonths(year)).withRel("months?{sort,size,page}");

        if(PAGEABLE_PARAM_CHOSEN) {
            var pagedCategories = new PageImpl<>(categories);
            return new CollectionModel(pagedCategories, href1, href2, href3, href4, href5);

        } else {
            return new CollectionModel(categories, href1, href2, href3, href4, href5);
        }
    }
}

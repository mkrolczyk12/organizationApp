package io.github.organizationApp.monthExpenses;

import io.github.organizationApp.categoryExpenses.*;
import io.github.organizationApp.categoryExpenses.projection.CategoryNoProcessesReadModel;
import io.github.organizationApp.monthExpenses.projection.MonthFullReadModel;
import io.github.organizationApp.monthExpenses.projection.MonthFullWriteModel;
import io.github.organizationApp.monthExpenses.projection.MonthNoCategoriesReadModel;
import io.github.organizationApp.yearExpenses.YearExpenses;
import io.github.organizationApp.yearExpenses.YearExpensesController;
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


    MonthExpenses save(final MonthExpenses month) {
        checkIfGivenMonthParameterValueRepresentsMonth(month.getMonth());
        return repository.save(month);
    }

    CategoryType addCategory(final CategoryType toCategoryType) {
        return categoryRepository.save(toCategoryType);
    }

    void setYearAndOwnerToNewMonth(final short year, final MonthExpenses toMonth, final String ownerId) throws NotFoundException {
        final YearExpenses Year = yearRepository.findByYearAndOwnerId(year, ownerId)
                .orElseThrow(() -> new NotFoundException("no year with given id"));
        toMonth.setYear(Year);
        toMonth.setOwnerId(ownerId);
    }

    void setMonthAndOwnerToNewCategory(final Integer monthId, final CategoryType toCategoryType, final String ownerId) throws NotFoundException {
        MonthExpenses month = repository.findByIdAndOwnerId(monthId, ownerId)
                .orElseThrow(() -> new NotFoundException("no category with given id"));
        toCategoryType.setMonthExpenses(month);
        toCategoryType.setOwnerId(ownerId);
    }

    MonthFullReadModel createMonthWithCategories(final YearExpenses belongingYear,
                                                 final MonthFullWriteModel source,
                                                 final String ownerId) {

        checkIfGivenMonthParameterValueRepresentsMonth(source.getMonth());
        MonthExpenses result = repository.save(source.toMonth(belongingYear, ownerId));

        return new MonthFullReadModel(result);
    }

    List<CategoryNoProcessesReadModel> findAllCategoriesBelongToMonth(final Integer monthId, final String ownerId) {
        return categoryRepository.findAllByMonthExpensesIdAndOwnerId(monthId, ownerId)
                .stream()
                .map(CategoryNoProcessesReadModel::new)
                .collect(Collectors.toList());
    }

    Page<CategoryNoProcessesReadModel> findAllCategoriesBelongToMonth(Pageable page, final Integer monthId, final String ownerId) {
        List<CategoryNoProcessesReadModel> categories = categoryRepository.findAllByMonthExpensesIdAndOwnerId(page, monthId, ownerId)
                .stream()
                .map(CategoryNoProcessesReadModel::new)
                .collect(Collectors.toList());

        return new PageImpl<>(categories);
    }

    List<?> findAllByYear(final short year, final String ownerId, final boolean CATEGORIES_FLAG_CHOSEN) throws NotFoundException {

        Integer yearId  = yearRepository.findByYearAndOwnerId(year, ownerId)
                .map(result -> result.getId())
                .orElseThrow(() -> new NotFoundException("no year with given parameter"));
        try {
            if(CATEGORIES_FLAG_CHOSEN) {
                return repository.findAllByYearIdAndOwnerId(yearId, ownerId)
                        .stream()
                        .map(MonthFullReadModel::new)
                        .collect(Collectors.toList());
            } else {
                return repository.findAllByYearIdAndOwnerId(yearId, ownerId)
                        .stream()
                        .map(MonthNoCategoriesReadModel::new)
                        .collect(Collectors.toList());
            }
        } catch (NullPointerException | NoSuchElementException e) {
            throw new NotFoundException("no months found");
        } catch (DataAccessException e) {
            throw new RuntimeException("an error occurred while working with data");
        }
    }

    Page<?> findAllByYear(final Pageable page, final short year, final String ownerId, final boolean CATEGORIES_FLAG_CHOSEN) throws NotFoundException {

        Integer yearId  = yearRepository.findByYearAndOwnerId(year, ownerId)
                .map(result -> result.getId())
                .orElseThrow(() -> new NotFoundException("no year with given parameter"));

        try {
            Page<MonthExpenses> pagedMonths = repository.findAllByYearIdAndOwnerId(page, yearId, ownerId);
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
        } catch (NullPointerException | NoSuchElementException e) {
            throw new NotFoundException("no months found");
        } catch (DataAccessException e) {
            throw new RuntimeException("an error occurred while working with data");
        }
    }

    MonthExpenses findById(final Integer id, final String ownerId) throws NotFoundException {
        return repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException("no month found"));
    }

    YearExpenses findByYear(final short year, final String ownerId) throws NotFoundException {
        return yearRepository.findByYearAndOwnerId(year, ownerId)
                .orElseThrow(() -> new NotFoundException("no year found"));
    }

    MonthExpenses findByMonth(final String month, final String ownerId) throws NotFoundException {
        return repository.findByMonthAndOwnerId(month, ownerId)
                .orElseThrow(() -> new NotFoundException("no month found"));
    }

    MonthExpenses saveAndFlush(final MonthExpenses updatedMonth) {
        try {
            return repository.saveAndFlush(updatedMonth);
        } catch (DataAccessException e) {
            throw new RuntimeException("an error occurred while working with data");
        }
    }

    void deleteMonth(final Integer id, final String ownerId) {
        repository.deleteByIdAndOwnerId(id, ownerId);
    }

    boolean monthLevelValidationSuccess(final short year, final String ownerId) throws NotFoundException {
        try {
            return yearRepository.existsByYearAndOwnerId(year, ownerId);
        } catch (Exception e) {
            throw new NotFoundException("month level validation failed, no year founded");
        }
    }

    public boolean checkIfGivenMonthNameExist(final String monthName, final YearExpenses year, final String ownerId) throws NotFoundException {
        try {
            if(repository.existsByMonthAndYearAndOwnerId(monthName, year, ownerId)) {
                return true;
            } else
                return false;
        } catch (Exception e) {
            throw new NotFoundException("given month does not exist!");
        }

    }

    boolean checkIfCategoryExistInGivenMonth(final String categoryName, final MonthExpenses month, final String ownerId) throws NotFoundException {
        return categoryService.checkIfGivenCategoryExist(categoryName, month, ownerId);
    }

    public boolean checkIfGivenMonthParameterValueRepresentsMonth(String month) {
        String workString = month.toUpperCase();
        for(Month eachMonth : Month.values()) {
            if(eachMonth.name().equals(workString)) {
                return true;
            }
        } throw new IllegalArgumentException("the provided value '" + month + "' does not represent the month");
    }


    /**
     *
     * @param unknownMonths List of months
     * @param year short param 'year' given in URL
     * @param PAGEABLE_PARAM_CHOSEN Boolean param checks if any Page param is given in URL
     * @param CATEGORIES_FLAG_CHOSEN Boolean param choose if months should include categories or not
     * @return CollectionModel<PagedModel<MonthFullReadModel>> or CollectionModel<MonthFullReadModel>
     *          or CollectionModel<PagedModel<MonthNoCategoriesReadModel>> or CollectionModel<MonthNoCategoriesReadModel>
     */
    CollectionModel<?> prepareReadMonthsHateoas(final List<?> unknownMonths,
                                                final short year,
                                                final String ownerId,
                                                final boolean PAGEABLE_PARAM_CHOSEN,
                                                final boolean CATEGORIES_FLAG_CHOSEN) throws NotFoundException {

        final Integer yearId = yearRepository.findByYearAndOwnerId(year, ownerId).get().getId();
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
                categories.forEach(category -> {
                    try {
                        category.add(linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(category.getId(), year, monthType)).withRel("category allowed_queries: POST,GET,PUT,PATCH,?{DELETE}"));
                    } catch (NotFoundException ignored) {
                    }
                });
                try {
                    Month.add(linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(Month.getId(), year)).withRel("month allowed_queries: POST,GET,PUT,PATCH,?{DELETE}"));
                } catch (NotFoundException ignored) {
                }
            });

            if(PAGEABLE_PARAM_CHOSEN) {
                var pagedMonths = new PageImpl<>(months);
                return CollectionModel.of(pagedMonths, href1, href2, href3, href4, href5);
            } else {
                return CollectionModel.of(months, href1, href2, href3, href4, href5);
            }
        }
        else {
            List<MonthNoCategoriesReadModel> months = (List<MonthNoCategoriesReadModel>) unknownMonths;

            months.forEach(Month -> {
                try {
                    Month.add(linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(Month.getId(), year)).withRel("month allowed_queries: POST,GET,PUT,PATCH,?{DELETE}"));
                } catch (NotFoundException ignored) {
                }
            });

            if(PAGEABLE_PARAM_CHOSEN) {
                var pagedCategories = new PageImpl<>(months);
                return CollectionModel.of(pagedCategories, href1, href2, href3, href4, href5);
            } else {
                return CollectionModel.of(months, href1, href2, href3, href4, href5);
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
                                                         final short year,
                                                         final String month,
                                                         final String ownerId,
                                                         final boolean PAGEABLE_PARAM_CHOSEN) throws NotFoundException {

        categories.forEach(category -> {
            try {
                category.add(linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(category.getId(), year, month)).withRel("allowed_queries: GET,PUT,PATCH,?{DELETE}"));
            } catch (NotFoundException ignored) {
            }
        });
        final Integer yearId = yearRepository.findByYearAndOwnerId(year, ownerId).get().getId();
        final Integer monthId = repository.findByMonthAndYearId(month, yearId).get().getId();
        final Link href1 = linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(monthId, year)).withSelfRel();
        final Link href2 = linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(monthId, year)).withRel("POST_category");
        final Link href3 = linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(monthId, year)).withRel("?{sort,size,page}");
        final Link href4 = linkTo(methodOn(MonthExpensesController.class).readEmptyMonths(year)).withRel("months");
        final Link href5 = linkTo(methodOn(MonthExpensesController.class).readEmptyMonths(year)).withRel("months?{sort,size,page}");

        if(PAGEABLE_PARAM_CHOSEN) {
            var pagedCategories = new PageImpl<>(categories);
            return CollectionModel.of(pagedCategories, href1, href2, href3, href4, href5);

        } else {
            return CollectionModel.of(categories, href1, href2, href3, href4, href5);
        }
    }
}

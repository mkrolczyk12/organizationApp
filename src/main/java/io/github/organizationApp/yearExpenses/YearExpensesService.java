package io.github.organizationApp.yearExpenses;

import io.github.organizationApp.monthExpenses.*;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
class YearExpensesService {
    private static final Logger logger = LoggerFactory.getLogger(YearExpensesService.class);
    private final YearExpensesRepository repository;
    private final MonthExpensesRepository monthRepository;
    private final MonthExpensesService monthService;

    YearExpensesService(final YearExpensesRepository repository,
                        final MonthExpensesRepository monthRepository,
                        final MonthExpensesService monthService) {

        this.repository = repository;
        this.monthRepository = monthRepository;
        this.monthService = monthService;
    }

    YearExpenses save(final YearExpenses year) {return repository.save(year);}

    void setYearToNewMonth(final Integer yearId, final MonthExpenses toMonth) throws NotFoundException {
        YearExpenses Year = repository.findById(yearId)
                .orElseThrow(() -> new NotFoundException("no year with given id"));
        toMonth.setYear(Year);
    }

    MonthExpenses addMonth(final MonthExpenses toMonthExpenses) {
        return monthRepository.save(toMonthExpenses);
    }

    List<?> findAll(final boolean MONTHS_FLAG_CHOSEN) {
        if(MONTHS_FLAG_CHOSEN) {
            return repository.findAll()
                    .stream()
                    .map(YearFullReadModel::new)
                    .collect(Collectors.toList());
        } else {
            return repository.findAll()
                    .stream()
                    .map(YearNoMonthsReadModel::new)
                    .collect(Collectors.toList());
        }
    }

    Page<?> findAll(Pageable page, final boolean MONTHS_FLAG_CHOSEN) {
        Page<YearExpenses> pagedYears = repository.findAll(page);
        List<?> items;
        if(MONTHS_FLAG_CHOSEN) {
            items = pagedYears.toList()
                    .stream()
                    .map(YearFullReadModel::new)
                    .collect(Collectors.toList());
        } else {
            items = pagedYears.toList()
                    .stream()
                    .map(YearNoMonthsReadModel::new)
                    .collect(Collectors.toList());
        }
        return new PageImpl(items);
    }

    List<MonthNoCategoriesReadModel> findAllMonthsBelongToYear(final Integer id) {
        return monthRepository.findAllByYearId(id)
                .stream()
                .map(MonthNoCategoriesReadModel::new)
                .collect(Collectors.toList());
    }

    Page<MonthNoCategoriesReadModel> findAllMonthsBelongToYear(Pageable page, final Integer monthId) {
        List<MonthNoCategoriesReadModel> months = monthRepository.findAllByYearId(page, monthId)
                .stream()
                .map(MonthNoCategoriesReadModel::new)
                .collect(Collectors.toList());

        return new PageImpl<>(months);
    }

    YearExpenses findById(final Integer id) throws NotFoundException {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("no year with given parameter"));
    }

    boolean existsByYear(String year) {
        return repository.existsByYear(year);
    }

    boolean yearLevelValidationSuccess(final Integer id) {
        return repository.existsById(id);
    }

    YearExpenses saveAndFlush(final YearExpenses updatedYear) {
        return repository.saveAndFlush(updatedYear);
    }

    void deleteYear(final Integer id) {
        repository.deleteById(id);
    }

    boolean checkIfGivenYearExistAndIfRepresentsNumber(final String year) {
        if (repository.existsByYear(year) | !isInteger(year)) {
            return true;
        } else
            return false;
    }

    boolean checkIfMonthExistInGivenYear(final String monthName, final YearExpenses year) {
        return monthService.checkIfGivenMonthNameExist(monthName, year);
    }

    /**
     *
     * @param unknownYears List of years
     * @param PAGEABLE_PARAM_CHOSEN Boolean param checks if any Page param is given in URL
     * @param MONTHS_FLAG_CHOSEN Boolean param choose if years should include months or not
     * @return CollectionModel<PagedModel<YearFullReadModel>> or CollectionModel<YearFullReadModel>
     *          or CollectionModel<PagedModel<YearNoMonthsReadModel>> or CollectionModel<YearNoMonthsReadModel>
     */
    CollectionModel<?> prepareReadYearsHateoas(final List<?> unknownYears,
                                               final boolean PAGEABLE_PARAM_CHOSEN,
                                               final boolean MONTHS_FLAG_CHOSEN) {

        final Link href1 = linkTo(methodOn(YearExpensesController.class).readEmptyYears()).withSelfRel();
        final Link href2 = linkTo(methodOn(YearExpensesController.class).readEmptyYears()).withRel("year?{sort,size,page}");
        final Link href3 = linkTo(methodOn(YearExpensesController.class).readEmptyYears()).withRel("year allowed_queries: POST empty year,GET,PUT,PATCH,?{DELETE}");
        final Link href4 = linkTo(methodOn(YearExpensesController.class).readEmptyYears()).withRel("?{months} -> required parameter to GET year with all declared months");

        if(MONTHS_FLAG_CHOSEN) {
            List<YearFullReadModel> years = (List<YearFullReadModel>) unknownYears;

            years.forEach(Year -> {
                final String yearType = Year.getYear();
                List<MonthNoCategoriesReadModel> months = Year.getMonths();
                months.forEach(Month -> Month.add(linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(Month.getId(), yearType)).withRel("month allowed_queries: POST,GET,PUT,PATCH,?{DELETE}")));
                Year.add(linkTo(methodOn(YearExpensesController.class).readOneYearContent(Year.getId())).withRel("year_allowed_queries: POST month,GET,PUT,PATCH,?{DELETE}"));
            });

            if(PAGEABLE_PARAM_CHOSEN) {
                var pagedYears = new PageImpl<>(years);
                return new CollectionModel(pagedYears, href1, href2, href3, href4);
            } else {
                return new CollectionModel(years, href1, href2, href3, href4);
            }
        }
        else {
            List<YearNoMonthsReadModel> years = (List<YearNoMonthsReadModel>) unknownYears;

            years.forEach(Year -> Year.add(linkTo(methodOn(YearExpensesController.class).readOneYearContent(Year.getId())).withRel("year_allowed_queries: POST month,GET,PUT,PATCH,?{DELETE}")));

            if(PAGEABLE_PARAM_CHOSEN) {
                var pagedYears = new PageImpl<>(years);
                return new CollectionModel(pagedYears, href1, href2, href3, href4);
            } else {
                return new CollectionModel(years, href1, href2, href3, href4);
            }
        }
    }

    CollectionModel<?> prepareReadOneYearContentHateoas(final List<MonthNoCategoriesReadModel> months,
                                                        final String year,
                                                        final boolean PAGEABLE_PARAM_CHOSEN) {

        months.forEach(Month -> Month.add(linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(Month.getId(), year)).withRel("allowed_queries: GET,PUT,PATCH,?{DELETE}")));
        final Integer yearId = repository.findByYear(year).get().getId();
        final Link href1 = linkTo(methodOn(YearExpensesController.class).readOneYearContent(yearId)).withSelfRel();
        final Link href2 = linkTo(methodOn(YearExpensesController.class).readOneYearContent(yearId)).withRel("?{sort,size,page}");
        final Link href3 = linkTo(methodOn(YearExpensesController.class).readOneYearContent(yearId)).withRel("POST_month");
        final Link href4 = linkTo(methodOn(YearExpensesController.class).readEmptyYears()).withRel("years");
        final Link href5 = linkTo(methodOn(YearExpensesController.class).readEmptyYears()).withRel("years?{sort,size,page}");

        if(PAGEABLE_PARAM_CHOSEN) {
            var pagedMonths = new PageImpl<>(months);
            return new CollectionModel(pagedMonths, href1, href2, href3, href4, href5);

        } else {
            return new CollectionModel(months, href1, href2, href3, href4, href5);
        }
    }

    /**
     * @isInteger checks if given 'year' param represents a number
     * @param givenYear String param 'year' given in URL
     * @return true or false
     */
    public static boolean isInteger(String givenYear) {
        if (givenYear == null) {
            return false;
        }
        int length = givenYear.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (givenYear.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = givenYear.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}

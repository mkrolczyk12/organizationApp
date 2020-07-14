package io.github.organizationApp.yearExpenses;

import io.github.organizationApp.monthExpenses.*;
import io.github.organizationApp.monthExpenses.projection.MonthNoCategoriesReadModel;
import io.github.organizationApp.yearExpenses.projection.YearFullReadModel;
import io.github.organizationApp.yearExpenses.projection.YearNoMonthsReadModel;
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

    YearExpenses addYear(final YearExpenses year, final String ownerId) {
            year.setOwnerId(ownerId);
            return repository.save(year);
    }

    YearExpenses save(final YearExpenses year) {
        return repository.save(year);
    }

    void setYearAndOwnerToNewMonth(final Integer yearId, final MonthExpenses toMonth, final String ownerId) throws NotFoundException {
        YearExpenses Year = repository.findByIdAndOwnerId(yearId, ownerId)
                .orElseThrow(() -> new NotFoundException("no year with given id"));
        toMonth.setYear(Year);
        toMonth.setOwnerId(ownerId);
    }

    MonthExpenses addMonth(final MonthExpenses toMonthExpenses) {
        String month = toMonthExpenses.getMonth();
        if (monthService.checkIfGivenMonthParameterValueRepresentsMonth(month))
            return monthRepository.save(toMonthExpenses);
        else throw new IllegalArgumentException("the provided value '" + month + "' does not represent the month");
    }

    List<?> findAll(final boolean MONTHS_FLAG_CHOSEN, final String ownerId) {
        try {
            if(MONTHS_FLAG_CHOSEN) {
                return repository.findAllByOwnerId(ownerId)
                        .stream()
                        .map(YearFullReadModel::new)
                        .collect(Collectors.toList());
            } else {
                return repository.findAllByOwnerId(ownerId)
                        .stream()
                        .map(YearNoMonthsReadModel::new)
                        .collect(Collectors.toList());
            }
        } catch (NullPointerException e) {
            throw new NullPointerException("no years found");
        }
    }

    Page<?> findAll(Pageable page, final boolean MONTHS_FLAG_CHOSEN, final String ownerId) {
        try {
            Page<YearExpenses> pagedYears = repository.findAllByOwnerId(page, ownerId);
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
        } catch (NullPointerException e) {
            throw new NullPointerException("no years found");
        }
    }

    List<MonthNoCategoriesReadModel> findAllMonthsBelongToYear(final Integer id, final String ownerId) {
        try {
            return monthRepository.findAllByYearIdAndOwnerId(id, ownerId)
                    .stream()
                    .map(MonthNoCategoriesReadModel::new)
                    .collect(Collectors.toList());

        } catch (NullPointerException e) {
            throw new NullPointerException("no months found");
        }
    }

    Page<MonthNoCategoriesReadModel> findAllMonthsBelongToYear(Pageable page, final Integer monthId, final String ownerId) {
        try {
            List<MonthNoCategoriesReadModel> months = monthRepository.findAllByYearIdAndOwnerId(page, monthId, ownerId)
                    .stream()
                    .map(MonthNoCategoriesReadModel::new)
                    .collect(Collectors.toList());

            return new PageImpl<>(months);
        } catch (NullPointerException e) {
            throw new NullPointerException("no months found");
        }
    }

    YearExpenses findById(final Integer id, final String ownerId) throws NotFoundException {
        return repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException("no year with given parameter"));
    }

    boolean existsByYear(short year, final String ownerId) {
        return repository.existsByYearAndOwnerId(year, ownerId);
    }

    boolean yearLevelValidationSuccess(final Integer id, final String ownerId) throws NotFoundException {
        try {
            return repository.existsByIdAndOwnerId(id, ownerId);
        } catch (NullPointerException e) {
            throw new NotFoundException("given year does not exist!");
        }
    }

    YearExpenses saveAndFlush(final YearExpenses updatedYear) {
        return repository.saveAndFlush(updatedYear);
    }

    void deleteYear(final Integer id, final String ownerId) {
        repository.deleteByIdAndOwnerId(id, ownerId);
    }

    boolean checkIfGivenYearValueExist(final YearExpenses Year, final String ownerId) {
        final short year = Year.getYear();
        return repository.existsByYearAndOwnerId(year, ownerId);
    }

    boolean checkIfMonthExistInGivenYear(final String monthName, final YearExpenses year, final String ownerId) throws NotFoundException {
        return monthService.checkIfGivenMonthNameExist(monthName, year, ownerId);
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
                final short year = Year.getYear();
                List<MonthNoCategoriesReadModel> months = Year.getMonths();
                months.forEach(Month -> {
                    try {
                        Month.add(linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(Month.getId(), year)).withRel("month allowed_queries: POST,GET,PUT,PATCH,?{DELETE}"));
                    } catch (NotFoundException ignored) {
                    }
                });
                try {
                    Year.add(linkTo(methodOn(YearExpensesController.class).readOneYearContent(Year.getId())).withRel("year_allowed_queries: POST month,GET,PUT,PATCH,?{DELETE}"));
                } catch (NotFoundException ignored) {
                }
            });

            if(PAGEABLE_PARAM_CHOSEN) {
                var pagedYears = new PageImpl<>(years);
                return CollectionModel.of(pagedYears, href1, href2, href3, href4);
            } else {
                return CollectionModel.of(years, href1, href2, href3, href4);
            }
        }
        else {
            List<YearNoMonthsReadModel> years = (List<YearNoMonthsReadModel>) unknownYears;

            years.forEach(Year -> {
                try {
                    Year.add(linkTo(methodOn(YearExpensesController.class).readOneYearContent(Year.getId())).withRel("year_allowed_queries: POST month,GET,PUT,PATCH,?{DELETE}"));
                } catch (NotFoundException ignored) {
                }
            });

            if(PAGEABLE_PARAM_CHOSEN) {
                var pagedYears = new PageImpl<>(years);
                return CollectionModel.of(pagedYears, href1, href2, href3, href4);
            } else {
                return CollectionModel.of(years, href1, href2, href3, href4);
            }
        }
    }

    CollectionModel<?> prepareReadOneYearContentHateoas(final List<MonthNoCategoriesReadModel> months,
                                                        final short year,
                                                        final String ownerId,
                                                        final boolean PAGEABLE_PARAM_CHOSEN) throws NotFoundException {

        months.forEach(Month -> {
            try {
                Month.add(linkTo(methodOn(MonthExpensesController.class).readOneMonthContent(Month.getId(), year)).withRel("allowed_queries: GET,PUT,PATCH,?{DELETE}"));
            } catch (NotFoundException ignored) {
            }
        });
        final Integer yearId = repository.findByYearAndOwnerId(year, ownerId).get().getId();
        final Link href1 = linkTo(methodOn(YearExpensesController.class).readOneYearContent(yearId)).withSelfRel();
        final Link href2 = linkTo(methodOn(YearExpensesController.class).readOneYearContent(yearId)).withRel("?{sort,size,page}");
        final Link href3 = linkTo(methodOn(YearExpensesController.class).readOneYearContent(yearId)).withRel("POST_month");
        final Link href4 = linkTo(methodOn(YearExpensesController.class).readEmptyYears()).withRel("years");
        final Link href5 = linkTo(methodOn(YearExpensesController.class).readEmptyYears()).withRel("years?{sort,size,page}");

        if(PAGEABLE_PARAM_CHOSEN) {
            var pagedMonths = new PageImpl<>(months);
            return CollectionModel.of(pagedMonths, href1, href2, href3, href4, href5);

        } else {
            return CollectionModel.of(months, href1, href2, href3, href4, href5);
        }
    }
}

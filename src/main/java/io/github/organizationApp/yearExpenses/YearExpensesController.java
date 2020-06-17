package io.github.organizationApp.yearExpenses;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
import io.github.organizationApp.monthExpenses.MonthExpenses;
import io.github.organizationApp.monthExpenses.MonthNoCategoriesReadModel;
import io.github.organizationApp.security.User;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.List;


@Controller
@GeneralExceptionsProcessing
@RequestMapping("/moneyapp/years")
public class YearExpensesController {
    private static final Logger logger = LoggerFactory.getLogger(YearExpensesController.class);
    private final YearExpensesService service;
    private final ObjectMapper objectMapper;

    YearExpensesController(final YearExpensesService service, final ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    /**
     * JSON:API
     */
    // consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE
    @Transactional
    @ResponseBody
    @PostMapping
    ResponseEntity<YearExpenses> addEmptyYear(@RequestBody @Valid final YearExpenses toYear) {

        final String USER_ID = User.getUserId();
        try {
            if(service.checkIfGivenYearExistAndIfRepresentsNumber(toYear.getYear(), USER_ID)) {
                logger.info("a year '" + toYear.getYear() + "' already exists!");
                return ResponseEntity.badRequest().build();
            } else {
                YearExpenses result = service.addYear(toYear, USER_ID);
                logger.info("posted new year with id = "+result.getId());
                return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
            }
        } catch (DataAccessException e) {
            logger.warn("an error occurred while posting new year");
            return ResponseEntity.badRequest().build();
        } catch (NumberFormatException e) {
            logger.warn("an NumberFormatException occurred while validating '" + toYear.getYear() + "'");
            return ResponseEntity.badRequest().build();
        }
    }
    @Transactional
    @ResponseBody
    @PostMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MonthExpenses> addMonthToChosenYear(@PathVariable final Integer id,
                                                       @RequestBody @Valid final MonthExpenses toMonth) {

        final String USER_ID = User.getUserId();
        if(!service.yearLevelValidationSuccess(id, USER_ID)) {
            logger.info("year level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {
            YearExpenses year = service.findById(id, USER_ID);

            if(service.checkIfMonthExistInGivenYear(toMonth.getMonth(), year, USER_ID)) {
                logger.info("a month '" + toMonth.getMonth().toLowerCase() + "' in year '" + year.getYear() + "' already exists!");
                return ResponseEntity.badRequest().build();
            } else {
                service.setYearAndOwnerToNewMonth(id, toMonth, USER_ID);
                MonthExpenses result = service.addMonth(toMonth);

                logger.info("posted new month to year with id = " + id);
                return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
            }
        } catch (NotFoundException | DataAccessException e) {
            logger.warn("an error occurred while posting month to given year");
            return ResponseEntity.badRequest().build();
        }
    }
    // consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE
    @ResponseBody
    @GetMapping(params = {"!sort","!size","!page"})
    ResponseEntity<?> readEmptyYears() {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean MONTHS_FLAG = false;

        try {
            List<?> result = service.findAll(MONTHS_FLAG, USER_ID);
            CollectionModel<?> yearsCollection = service.prepareReadYearsHateoas(result, PAGEABLE_PARAM_FLAG, MONTHS_FLAG);

            logger.info("exposing all years!");
            return ResponseEntity.ok(yearsCollection);
        } catch (NullPointerException | DataAccessException e) {
            logger.warn("an error while loading years occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    //consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE
    @ResponseBody
    @GetMapping
    ResponseEntity<?> readEmptyYears(final Pageable page) {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean MONTHS_FLAG = false;

        try {
            List<?> result = service.findAll(page, MONTHS_FLAG, USER_ID).toList();
            CollectionModel<?> yearsCollection = service.prepareReadYearsHateoas(result, PAGEABLE_PARAM_FLAG, MONTHS_FLAG);

            logger.info("exposing all years!");
            return ResponseEntity.ok(yearsCollection);
        } catch (DataAccessException | NullPointerException e) {
            logger.warn("an error while loading years occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(params = {"months", "!sort", "!size", "!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readYearsWithMonths() {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean MONTHS_FLAG = true;

        try {
            List<?> result = service.findAll(MONTHS_FLAG, USER_ID);
            CollectionModel<?> yearsCollection = service.prepareReadYearsHateoas(result, PAGEABLE_PARAM_FLAG, MONTHS_FLAG);

            logger.info("exposing all years + months!");
            return ResponseEntity.ok(yearsCollection);
        } catch (DataAccessException | NullPointerException e) {
            logger.warn("an error while loading years + months occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(params = {"months"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readYearsWithMonths(final Pageable page) {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean MONTHS_FLAG = true;

        try {
            List<?> result = service.findAll(page, MONTHS_FLAG, USER_ID).toList();
            CollectionModel<?> yearsCollection = service.prepareReadYearsHateoas(result, PAGEABLE_PARAM_FLAG, MONTHS_FLAG);

            logger.info("exposing all years + months!");
            return ResponseEntity.ok(yearsCollection);
        } catch (DataAccessException | NullPointerException e) {
            logger.warn("an error while loading years + months occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(value = "/{id}", params = {"!sort", "!size", "!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readOneYearContent(@PathVariable final Integer id) {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = false;

        try {
            List<MonthNoCategoriesReadModel> result = service.findAllMonthsBelongToYear(id, USER_ID);
            short year = service.findById(id, USER_ID).getYear();
            CollectionModel<?> yearCollection = service.prepareReadOneYearContentHateoas(result, year, USER_ID, PAGEABLE_PARAM_FLAG);

            logger.info("exposing '" + year + "' year content");
            return ResponseEntity.ok(yearCollection);
        } catch (NotFoundException e) {
            logger.info("no months found for given year");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.warn("an error while loading year + months occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readOneYearContent(final Pageable page,
                                         @PathVariable final Integer id) {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = true;

        try {
            List<MonthNoCategoriesReadModel> result = service.findAllMonthsBelongToYear(page, id, USER_ID).toList();
            short year = service.findById(id, USER_ID).getYear();
            CollectionModel<?> yearCollection = service.prepareReadOneYearContentHateoas(result, year, USER_ID, PAGEABLE_PARAM_FLAG);

            logger.info("exposing '" + year + "' year content");
            return ResponseEntity.ok(yearCollection);
        } catch (NotFoundException e) {
            logger.info("no months found for given year");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.warn("an error while loading year + months occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> fullUpdateYear(@PathVariable final Integer id,
                                          @RequestBody @Valid final YearExpenses toUpdate) {

        final String USER_ID = User.getUserId();
        if(!service.yearLevelValidationSuccess(id, USER_ID)) {
            logger.info("year level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {
            if(service.checkIfGivenYearExistAndIfRepresentsNumber(toUpdate.getYear(), USER_ID)) {
                return ResponseEntity.badRequest().build();
            } else {
                YearExpenses year = service.findById(id, USER_ID);
                year.fullUpdate(toUpdate);

                service.save(year);
                logger.info("put year with id = " + id);
                return ResponseEntity.ok().build();
            }
        } catch (NullPointerException | NotFoundException | DataAccessException e) {
            logger.info("an error occurred while put year");
            return ResponseEntity.badRequest().build();
        } catch (NumberFormatException e) {
            logger.warn("an NumberFormatException occurred while validating '" + toUpdate.getYear() + "'");
            return ResponseEntity.badRequest().build();
        }
    }
    @Transactional
    @ResponseBody
    @PatchMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> partUpdateYear(@PathVariable final Integer id,
                                                 @Valid final HttpServletRequest request) {

        final String USER_ID = User.getUserId();
        if(!service.yearLevelValidationSuccess(id, USER_ID)) {
            logger.info("year level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {
            YearExpenses year = service.findById(id, USER_ID);
            short yearBeforeUpdate = year.getYear();

            YearExpenses updatedYear = objectMapper.readerForUpdating(year).readValue(request.getReader());
            short yearAfterUpdate = updatedYear.getYear();

            if(!(yearBeforeUpdate == yearAfterUpdate)) {
                if(service.checkIfGivenYearExistAndIfRepresentsNumber(yearAfterUpdate, USER_ID)) {
                    throw new IllegalStateException();
                }
            }

            service.saveAndFlush(updatedYear);
            logger.info("successfully patched year nr " + id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException | IOException | DataAccessException e) {
            logger.warn("an error occurred while patching year");
            return ResponseEntity.badRequest().build();
        } catch (NumberFormatException e) {
            logger.warn("an NumberFormatException occurred while validating year with id = '" + id + "'");
            return ResponseEntity.badRequest().build();
        }
    }
    @Transactional
    @ResponseBody
    @DeleteMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> deleteYear(@PathVariable final Integer id) {

        final String USER_ID = User.getUserId();
        if(!service.yearLevelValidationSuccess(id, USER_ID)) {
            logger.info("year level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {
            service.deleteYear(id, USER_ID);
            logger.warn("deleted year with id = " + id);
            return ResponseEntity.ok().build();
        } catch (DataAccessException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Templates
     */
}

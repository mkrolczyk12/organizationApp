package io.github.organizationApp.yearExpenses;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
import io.github.organizationApp.monthExpenses.MonthExpenses;
import io.github.organizationApp.monthExpenses.MonthNoCategoriesReadModel;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/year")
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
    @Transactional
    @ResponseBody
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<YearExpenses> addEmptyYear(@RequestBody @Valid final YearExpenses toYear) {

        try {
            if(service.checkIfGivenYearExistAndIfRepresentsNumber(toYear.getYear())) {
                logger.info("a year '" + toYear.getYear().toLowerCase() + "' already exists!");
                return ResponseEntity.badRequest().build();
            } else {
                YearExpenses result = service.save(toYear);
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

        if(!service.yearLevelValidationSuccess(id)) {
            logger.info("year level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {
            YearExpenses year = service.findById(id);

            if(service.checkIfMonthExistInGivenYear(toMonth.getMonth(), year)) {
                logger.info("a month '" + toMonth.getMonth().toLowerCase() + "' in year '" + year.getYear() + "' already exists!");
                return ResponseEntity.badRequest().build();
            } else {
                service.setYearToNewMonth(id, toMonth);
                MonthExpenses result = service.addMonth(toMonth);

                logger.info("posted new month to year with id = " + id);
                return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
            }
        } catch (NotFoundException | DataAccessException e) {
            logger.info("an error occurred while posting month to given year");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(params = {"!sort","!size","!page"},consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readEmptyYears() {

        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean MONTHS_FLAG = false;

        try {
            List<?> result = service.findAll(MONTHS_FLAG);
            CollectionModel<?> yearsCollection = service.prepareReadYearsHateoas(result, PAGEABLE_PARAM_FLAG, MONTHS_FLAG);

            logger.info("exposing all years!");
            return ResponseEntity.ok(yearsCollection);
        } catch (NullPointerException | DataAccessException e) {
            logger.info("an error while loading years occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readEmptyYears(final Pageable page) {

        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean MONTHS_FLAG = false;

        try {
            List<?> result = service.findAll(page, MONTHS_FLAG).toList();
            CollectionModel<?> yearsCollection = service.prepareReadYearsHateoas(result, PAGEABLE_PARAM_FLAG, MONTHS_FLAG);

            logger.info("exposing all years!");
            return ResponseEntity.ok(yearsCollection);
        } catch (DataAccessException | NullPointerException e) {
            logger.info("an error while loading years occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(params = {"months", "!sort", "!size", "!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readYearsWithMonths() {

        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean MONTHS_FLAG = true;

        try {
            List<?> result = service.findAll(MONTHS_FLAG);
            CollectionModel<?> yearsCollection = service.prepareReadYearsHateoas(result, PAGEABLE_PARAM_FLAG, MONTHS_FLAG);

            logger.info("exposing all years + months!");
            return ResponseEntity.ok(yearsCollection);
        } catch (DataAccessException | NullPointerException e) {
            logger.info("an error while loading years + months occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(params = {"months"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readYearsWithMonths(final Pageable page) {

        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean MONTHS_FLAG = true;

        try {
            List<?> result = service.findAll(page, MONTHS_FLAG).toList();
            CollectionModel<?> yearsCollection = service.prepareReadYearsHateoas(result, PAGEABLE_PARAM_FLAG, MONTHS_FLAG);

            logger.info("exposing all years + months!");
            return ResponseEntity.ok(yearsCollection);
        } catch (DataAccessException | NullPointerException e) {
            logger.info("an error while loading years + months occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(value = "/{id}", params = {"!sort", "!size", "!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readOneYearContent(@PathVariable final Integer id) {

        final boolean PAGEABLE_PARAM_FLAG = false;

        try {
            List<MonthNoCategoriesReadModel> result = service.findAllMonthsBelongToYear(id);
            String year = service.findById(id).getYear();
            CollectionModel<?> yearCollection = service.prepareReadOneYearContentHateoas(result, year, PAGEABLE_PARAM_FLAG);

            logger.info("exposing '" + year + "' year content");
            return ResponseEntity.ok(yearCollection);
        } catch (NotFoundException e) {
            logger.info("no months found for given year");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.info("an error while loading year + months occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readOneYearContent(final Pageable page,
                                         @PathVariable final Integer id) {

        final boolean PAGEABLE_PARAM_FLAG = true;

        try {
            List<MonthNoCategoriesReadModel> result = service.findAllMonthsBelongToYear(page, id).toList();
            String year = service.findById(id).getYear();
            CollectionModel<?> yearCollection = service.prepareReadOneYearContentHateoas(result, year, PAGEABLE_PARAM_FLAG);

            logger.info("exposing '" + year + "' year content");
            return ResponseEntity.ok(yearCollection);
        } catch (NotFoundException e) {
            logger.info("no months found for given year");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.info("an error while loading year + months occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> fullUpdateYear(@PathVariable final Integer id,
                                          @RequestBody @Valid final YearExpenses toUpdate) {

        if(!service.yearLevelValidationSuccess(id)) {
            logger.info("year level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {
            if(service.checkIfGivenYearExistAndIfRepresentsNumber(toUpdate.getYear())) {
                return ResponseEntity.badRequest().build();
            } else {
                YearExpenses year = service.findById(id);
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

        if(!service.yearLevelValidationSuccess(id)) {
            logger.info("year level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {
            YearExpenses year = service.findById(id);
            String yearBeforeUpdate = year.getYear().toLowerCase();

            YearExpenses updatedYear = objectMapper.readerForUpdating(year).readValue(request.getReader());
            String yearAfterUpdate = updatedYear.getYear();

            if(!yearBeforeUpdate.equals(yearAfterUpdate)) {
                if(service.checkIfGivenYearExistAndIfRepresentsNumber(yearAfterUpdate)) {
                    throw new IllegalStateException();
                }
            }

            service.saveAndFlush(updatedYear);
            logger.info("successfully patched year nr " + id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException | IOException | DataAccessException e) {
            logger.info("an error occurred while patching year");
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

        if(!service.yearLevelValidationSuccess(id)) {
            logger.info("year level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {

            service.deleteYear(id);
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

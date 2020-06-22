package io.github.organizationApp.yearExpenses;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.organizationApp.globalControllerAdvice.ExceptionResponse;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
import io.github.organizationApp.monthExpenses.MonthExpenses;
import io.github.organizationApp.monthExpenses.projection.MonthNoCategoriesReadModel;
import io.github.organizationApp.security.User;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Controller
@GeneralExceptionsProcessing
@ExceptionsProcessing
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
    public ResponseEntity<Object> addEmptyYear(@RequestBody @Valid final YearExpenses toYear) {

        final String USER_ID = User.getUserId();
        if(service.checkIfGivenYearExistAndIfRepresentsNumber(toYear, USER_ID)) {
            final String message = "a year '" + toYear.getYear() + "' already exists!";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else {
            YearExpenses result = service.addYear(toYear, USER_ID);
            logger.info("posted new year with id = "+result.getId());

            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        }
    }
    @Transactional
    @ResponseBody
    @PostMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addMonthToChosenYear(@PathVariable final Integer id,
                                               @RequestBody @Valid final MonthExpenses toMonth) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.yearLevelValidationSuccess(id, USER_ID)) {
            final String message = "year level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

            YearExpenses year = service.findById(id, USER_ID);
            if(service.checkIfMonthExistInGivenYear(toMonth.getMonth(), year, USER_ID)) {
                final String message = "a month '" + toMonth.getMonth().toLowerCase() + "' in year '" + year.getYear() + "' already exists!";
                logger.info(message);
                ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

                return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
            } else {
                service.setYearAndOwnerToNewMonth(id, toMonth, USER_ID);
                MonthExpenses result = service.addMonth(toMonth);

                logger.info("posted new month to year with id = " + id);
                return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
            }
    }
    // consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE
    @ResponseBody
    @GetMapping(params = {"!sort","!size","!page"})
    ResponseEntity<?> readEmptyYears() {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean MONTHS_FLAG = false;

        List<?> result = service.findAll(MONTHS_FLAG, USER_ID);
        CollectionModel<?> yearsCollection = service.prepareReadYearsHateoas(result, PAGEABLE_PARAM_FLAG, MONTHS_FLAG);

        logger.info("exposing all years!");
        return ResponseEntity.ok(yearsCollection);
    }
    //consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE
    @ResponseBody
    @GetMapping
    ResponseEntity<?> readEmptyYears(final Pageable page) {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean MONTHS_FLAG = false;

        List<?> result = service.findAll(page, MONTHS_FLAG, USER_ID).toList();
        CollectionModel<?> yearsCollection = service.prepareReadYearsHateoas(result, PAGEABLE_PARAM_FLAG, MONTHS_FLAG);

        logger.info("exposing all years!");
        return ResponseEntity.ok(yearsCollection);
    }
    @ResponseBody
    @GetMapping(params = {"months", "!sort", "!size", "!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readYearsWithMonths() {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean MONTHS_FLAG = true;

        List<?> result = service.findAll(MONTHS_FLAG, USER_ID);
        CollectionModel<?> yearsCollection = service.prepareReadYearsHateoas(result, PAGEABLE_PARAM_FLAG, MONTHS_FLAG);

        logger.info("exposing all years + months!");
        return ResponseEntity.ok(yearsCollection);
    }
    @ResponseBody
    @GetMapping(params = {"months"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readYearsWithMonths(final Pageable page) {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean MONTHS_FLAG = true;

        List<?> result = service.findAll(page, MONTHS_FLAG, USER_ID).toList();
        CollectionModel<?> yearsCollection = service.prepareReadYearsHateoas(result, PAGEABLE_PARAM_FLAG, MONTHS_FLAG);

        logger.info("exposing all years + months!");
        return ResponseEntity.ok(yearsCollection);
    }
    @ResponseBody
    @GetMapping(value = "/{id}", params = {"!sort", "!size", "!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readOneYearContent(@PathVariable final Integer id) throws NotFoundException {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = false;

        List<MonthNoCategoriesReadModel> result = service.findAllMonthsBelongToYear(id, USER_ID);
        short year = service.findById(id, USER_ID).getYear();
        CollectionModel<?> yearCollection = service.prepareReadOneYearContentHateoas(result, year, USER_ID, PAGEABLE_PARAM_FLAG);

        logger.info("exposing '" + year + "' year content");
        return ResponseEntity.ok(yearCollection);
    }
    @ResponseBody
    @GetMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readOneYearContent(final Pageable page,
                                         @PathVariable final Integer id) throws NotFoundException {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = true;

        List<MonthNoCategoriesReadModel> result = service.findAllMonthsBelongToYear(page, id, USER_ID).toList();
        short year = service.findById(id, USER_ID).getYear();
        CollectionModel<?> yearCollection = service.prepareReadOneYearContentHateoas(result, year, USER_ID, PAGEABLE_PARAM_FLAG);

        logger.info("exposing '" + year + "' year content");
        return ResponseEntity.ok(yearCollection);
    }
    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> fullUpdateYear(@PathVariable final Integer id,
                                          @RequestBody @Valid final YearExpenses toUpdate) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.yearLevelValidationSuccess(id, USER_ID)) {
            final String message = "year level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        if(service.checkIfGivenYearExistAndIfRepresentsNumber(toUpdate, USER_ID)) {
            final String message = "a year '" + toUpdate.getYear() + "' already exists!";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        } else {
            YearExpenses year = service.findById(id, USER_ID);
            year.fullUpdate(toUpdate);

            service.save(year);
            logger.info("put year with id = " + id);
            return ResponseEntity.ok().build();
        }
    }
    @Transactional
    @ResponseBody
    @PatchMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> partUpdateYear(@PathVariable final Integer id,
                                                 @Valid final HttpServletRequest request) throws NotFoundException, IOException {

        final String USER_ID = User.getUserId();
        if(!service.yearLevelValidationSuccess(id, USER_ID)) {
            final String message = "year level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        YearExpenses year = service.findById(id, USER_ID);
        short yearBeforeUpdate = year.getYear();

        YearExpenses updatedYear = objectMapper.readerForUpdating(year).readValue(request.getReader());
        short yearAfterUpdate = updatedYear.getYear();

        if(!(yearBeforeUpdate == yearAfterUpdate)) {
            if(service.checkIfGivenYearExistAndIfRepresentsNumber(updatedYear, USER_ID)) {
                throw new IllegalStateException("the given year already exists!");
            }
        }

        service.saveAndFlush(updatedYear);
        logger.info("successfully patched year nr " + id);
        return ResponseEntity.noContent().build();
    }
    @Transactional
    @ResponseBody
    @DeleteMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteYear(@PathVariable final Integer id) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.yearLevelValidationSuccess(id, USER_ID)) {
            final String message = "year level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        service.deleteYear(id, USER_ID);
        logger.warn("deleted year with id = " + id);
        return ResponseEntity.ok().build();
    }

    /**
     * Templates
     */
}

package io.github.organizationApp.monthExpenses;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.organizationApp.categoryExpenses.projection.CategoryNoProcessesReadModel;
import io.github.organizationApp.categoryExpenses.CategoryType;
import io.github.organizationApp.globalControllerAdvice.ExceptionResponse;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
import io.github.organizationApp.monthExpenses.projection.MonthFullReadModel;
import io.github.organizationApp.monthExpenses.projection.MonthFullWriteModel;
import io.github.organizationApp.security.SecurityExceptionsProcessing;
import io.github.organizationApp.security.User;
import io.github.organizationApp.yearExpenses.YearExpenses;
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
@CrossOrigin
@GeneralExceptionsProcessing
@MonthExceptionsProcessing
@SecurityExceptionsProcessing
@RequestMapping("/moneyapp/months")
public class MonthExpensesController {
    private static final Logger logger = LoggerFactory.getLogger(MonthExpensesController.class);
    private final MonthExpensesService service;
    private final ObjectMapper objectMapper;

    MonthExpensesController(final MonthExpensesService service, final ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    /**
     * JSON:API
     */
    @Transactional
    @ResponseBody
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MonthExpenses> addEmptyMonth(@RequestParam(value = "year") final short YEAR_PARAM,
                                                @RequestBody @Valid final MonthExpenses toMonth) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            final String message = "month level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

            YearExpenses year = service.findByYear(YEAR_PARAM, USER_ID);

            if(service.checkIfGivenMonthNameExist(toMonth.getMonth(), year, USER_ID)) {
                final String message = "a month '" + toMonth.getMonth().toLowerCase() + "' in year '" + YEAR_PARAM + "' already exists!";
                logger.info(message);
                ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

                return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
            }
            service.setYearAndOwnerToNewMonth(YEAR_PARAM, toMonth, USER_ID);
            MonthExpenses result = service.save(toMonth);

            logger.info("posted new empty month with id = " + result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
    }
    @Transactional
    @ResponseBody
    @PostMapping(params = {"categories"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MonthFullReadModel> addMonthWithCategories(@RequestParam(value = "year") final short YEAR_PARAM,
                                                              @RequestBody @Valid final MonthFullWriteModel toMonth) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if (!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            final String message = "month level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        YearExpenses year = service.findByYear(YEAR_PARAM, USER_ID);

        if (service.checkIfGivenMonthNameExist(toMonth.getMonth(), year, USER_ID)) {
            final String message = "a month '" + toMonth.getMonth().toLowerCase() + "' in year '" + YEAR_PARAM + "' already exists!";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        } else {
            YearExpenses belongingYear = service.findByYear(YEAR_PARAM, USER_ID);
            MonthFullReadModel result = service.createMonthWithCategories(belongingYear, toMonth, USER_ID);

            logger.info("posted new month + categories content, with id = " + result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        }
    }
    @Transactional
    @ResponseBody
    @PostMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CategoryType> addCategoryToChosenMonth(@PathVariable final Integer id,
                                                           @RequestParam(value = "year") final short YEAR_PARAM,
                                                           @RequestBody @Valid final CategoryType toCategory) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            final String message = "month level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

            MonthExpenses month = service.findById(id, USER_ID);

            if(service.checkIfCategoryExistInGivenMonth(toCategory.getType(), month, USER_ID)) {
                final String message = "a category '" + toCategory.getType().toLowerCase() + "' in year '" + YEAR_PARAM + "' and month '" + month.getMonth() + "' already exists!";
                logger.info(message);
                ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

                return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
            } else {
                service.setMonthAndOwnerToNewCategory(id, toCategory, USER_ID);
                CategoryType result = service.addCategory(toCategory);

                logger.info("posted new category to month with id = " + id);
                return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
            }
    }
    @ResponseBody
    @GetMapping(params = {"!sort", "!size", "!page"}, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readEmptyMonths(@RequestParam(value = "year") final short YEAR_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            final String message = "month level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean CATEGORIES_FLAG = false;

        List<?> result = service.findAllByYear(YEAR_PARAM, USER_ID, CATEGORIES_FLAG);
        CollectionModel<?> monthsCollection = service.prepareReadMonthsHateoas(result, YEAR_PARAM, USER_ID, PAGEABLE_PARAM_FLAG, CATEGORIES_FLAG);

        logger.info("exposing all months!");
        return ResponseEntity.ok(monthsCollection);
    }
    @ResponseBody
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readEmptyMonths(final Pageable page,
                                      @RequestParam(value = "year") final short YEAR_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            final String message = "month level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean CATEGORIES_FLAG = false;

        List<?> result = service.findAllByYear(page, YEAR_PARAM, USER_ID, CATEGORIES_FLAG).toList();
        CollectionModel<?> monthsCollection = service.prepareReadMonthsHateoas(result, YEAR_PARAM, USER_ID, PAGEABLE_PARAM_FLAG, CATEGORIES_FLAG);

        logger.info("exposing all months!");
        return ResponseEntity.ok(monthsCollection);
    }
    @ResponseBody
    @GetMapping(params = {"categories", "!sort", "!size", "!page"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readMonthsWithCategories(@RequestParam(value = "year") final short YEAR_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            final String message = "month level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean CATEGORIES_FLAG = true;

        List<?> result = service.findAllByYear(YEAR_PARAM, USER_ID, CATEGORIES_FLAG);
        CollectionModel<?> monthsCollection = service.prepareReadMonthsHateoas(result, YEAR_PARAM, USER_ID, PAGEABLE_PARAM_FLAG, CATEGORIES_FLAG);

        logger.info("exposing all months + categories!");
        return ResponseEntity.ok(monthsCollection);
    }
    @ResponseBody
    @GetMapping(params = {"categories"}, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readMonthsWithCategories(final Pageable page,
                                               @RequestParam(value = "year") final short YEAR_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            final String message = "month level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean CATEGORIES_FLAG = true;

        List<?> result = service.findAllByYear(page, YEAR_PARAM, USER_ID, CATEGORIES_FLAG).toList();
        CollectionModel<?> monthsCollection = service.prepareReadMonthsHateoas(result, YEAR_PARAM, USER_ID, PAGEABLE_PARAM_FLAG, CATEGORIES_FLAG);

        logger.info("exposing all months!");
        return ResponseEntity.ok(monthsCollection);
    }
    @ResponseBody
    @GetMapping(value = "/{id}", params = {"!sort", "!size", "!page"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readOneMonthContent(@PathVariable final Integer id,
                                                 @RequestParam(value = "year") final short YEAR_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            final String message = "month level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        final boolean PAGEABLE_PARAM_FLAG = false;

        List<CategoryNoProcessesReadModel> result = service.findAllCategoriesBelongToMonth(id, USER_ID);
        String month = service.findById(id, USER_ID).getMonth();
        CollectionModel<?> monthCollection = service.prepareReadOneMonthContentHateoas(result, YEAR_PARAM, month, USER_ID, PAGEABLE_PARAM_FLAG);

        logger.info("exposing '" + month + "' month content");
        return ResponseEntity.ok(monthCollection);
    }
    @ResponseBody
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readOneMonthContent(final Pageable page,
                                          @PathVariable final Integer id,
                                          @RequestParam(value = "year") final short YEAR_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            final String message = "month level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        final boolean PAGEABLE_PARAM_FLAG = true;

        List<CategoryNoProcessesReadModel> result = service.findAllCategoriesBelongToMonth(page, id, USER_ID).toList();
        String month = service.findById(id, USER_ID).getMonth();
        CollectionModel<?> monthCollection = service.prepareReadOneMonthContentHateoas(result, YEAR_PARAM, month, USER_ID, PAGEABLE_PARAM_FLAG);

        logger.info("exposing '" + month + "' month content");
        return ResponseEntity.ok(monthCollection);
    }
    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> fullUpdateMonth(@PathVariable final Integer id,
                                           @RequestParam(value = "year") final short YEAR_PARAM,
                                           @RequestBody @Valid final MonthExpenses toUpdate) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            final String message = "month level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        YearExpenses year = service.findByYear(YEAR_PARAM, USER_ID);

        if(service.checkIfGivenMonthNameExist(toUpdate.getMonth(), year, USER_ID)) {
            final String message = "a month '" + toUpdate.getMonth().toLowerCase() + "' in year '" + YEAR_PARAM + "' already exists!";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        } else {
            MonthExpenses month = service.findById(id, USER_ID);
            service.checkIfGivenMonthParameterValueRepresentsMonth(toUpdate.getMonth());
            month.fullUpdate(toUpdate);

            service.save(month);
            logger.info("put month with id = " + id);
            return ResponseEntity.ok().build();
        }
    }
    @Transactional
    @ResponseBody
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> partUpdateMonth(@PathVariable final Integer id,
                                                  @RequestParam(value = "year") final short YEAR_PARAM,
                                                  @Valid final HttpServletRequest request) throws NotFoundException, IOException {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            final String message = "month level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        YearExpenses year = service.findByYear(YEAR_PARAM, USER_ID);

        MonthExpenses month = service.findById(id, USER_ID);
        String monthNameBeforeUpdate = month.getMonth().toLowerCase();

        MonthExpenses updatedMonth = objectMapper.readerForUpdating(month).readValue(request.getReader());
        String monthNameAfterUpdate = updatedMonth.getMonth().toLowerCase();

        if(!monthNameBeforeUpdate.equals(monthNameAfterUpdate)) {
            service.checkIfGivenMonthParameterValueRepresentsMonth(monthNameAfterUpdate);
            if(service.checkIfGivenMonthNameExist(monthNameAfterUpdate, year, USER_ID)) {
                final String message = "a month '" + monthNameAfterUpdate.toLowerCase() + "' in year '" + YEAR_PARAM + "' already exists!";
                logger.info(message);
                throw new IllegalStateException(message);
            }
        }

        service.saveAndFlush(updatedMonth);
        logger.info("successfully patched month nr " + id);
        return ResponseEntity.noContent().build();
    }
    @Transactional
    @ResponseBody
    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> deleteMonth(@PathVariable final Integer id,
                                       @RequestParam(value = "year") final short YEAR_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            final String message = "month level validation failed, no year founded";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        String monthName = service.findById(id, USER_ID).getMonth();

        service.deleteMonth(id, USER_ID);
        logger.warn("deleted month '" + monthName + "'");
        return ResponseEntity.ok().build();
    }

    /**
     * Templates
     */
}

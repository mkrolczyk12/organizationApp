package io.github.organizationApp.monthExpenses;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.organizationApp.categoryExpenses.CategoryNoProcessesReadModel;
import io.github.organizationApp.categoryExpenses.CategoryType;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
import io.github.organizationApp.security.User;
import io.github.organizationApp.yearExpenses.YearExpenses;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
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
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MonthExpenses> addEmptyMonth(@RequestParam(value = "year") final short YEAR_PARAM,
                                                @RequestBody @Valid final MonthExpenses toMonth) {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            logger.info("month level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {
            YearExpenses year = service.findByYear(YEAR_PARAM, USER_ID);

            if(service.checkIfGivenMonthNameExist(toMonth.getMonth(), year, USER_ID)) {
                logger.info("a month '" + toMonth.getMonth().toLowerCase() + "' in year '" + YEAR_PARAM + "' already exists!");
                return ResponseEntity.badRequest().build();
            }
            service.setYearAndOwnerToNewMonth(YEAR_PARAM, toMonth, USER_ID);
            MonthExpenses result = service.save(toMonth);

            logger.info("posted new empty month with id = " + result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        } catch (NullPointerException | NotFoundException e) {
            logger.warn("an error occurred while posting new empty month");
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (ClassCastException e) {
            logger.warn("an ClassCastException occurred while validating '" + toMonth.getMonth() + "' month");
            return ResponseEntity.badRequest().build();
        } catch (DataIntegrityViolationException e) {
            logger.warn("an DataIntegrityViolationException occurred while posting new month in year '" + YEAR_PARAM + "': invalid value '" + toMonth.getMonth() + "' in field 'month'");
            return ResponseEntity.badRequest().build();
        }
    }
    @Transactional
    @ResponseBody
    @PostMapping(params = {"categories"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MonthFullReadModel> addMonthWithCategories(@RequestParam(value = "year") final short YEAR_PARAM,
                                                              @RequestBody @Valid final MonthFullWriteModel toMonth) {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            logger.info("month level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {
            YearExpenses year = service.findByYear(YEAR_PARAM, USER_ID);

            if(service.checkIfGivenMonthNameExist(toMonth.getMonth(), year, USER_ID)) {
                logger.info("a month '" + toMonth.getMonth().toLowerCase() + "' in year '" + YEAR_PARAM + "' already exists!");
                return ResponseEntity.badRequest().build();
            } else {
                YearExpenses belongingYear = service.findByYear(YEAR_PARAM, USER_ID);
                MonthFullReadModel result = service.createMonthWithCategories(belongingYear, toMonth, USER_ID);

                logger.info("posted new month + categories content, with id = " + result.getId());
                return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
            }
        } catch (NullPointerException | NotFoundException e) {
            logger.warn("an error occurred while posting month with categories");
            return ResponseEntity.badRequest().build();
        } catch (DataIntegrityViolationException e) {
            logger.warn("an DataIntegrityViolationException occurred while posting new month in year '" + YEAR_PARAM + "': invalid value '" + toMonth.getMonth() + "' in field 'month'");
            return ResponseEntity.badRequest().build();
        }
    }
    @Transactional
    @ResponseBody
    @PostMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CategoryType> addCategoryToChosenMonth(@PathVariable final Integer id,
                                                           @RequestParam(value = "year") final short YEAR_PARAM,
                                                           @RequestBody @Valid final CategoryType toCategory) {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            logger.info("month level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {
            MonthExpenses month = service.findById(id, USER_ID);

            if(service.checkIfCategoryExistInGivenMonth(toCategory.getType(), month, USER_ID)) {
                logger.info("a category '" + toCategory.getType().toLowerCase() + "' in year '" + YEAR_PARAM + "' and month '" + month.getMonth() + "' already exists!");
                return ResponseEntity.badRequest().build();
            } else {
                service.setMonthAndOwnerToNewCategory(id, toCategory, USER_ID);
                CategoryType result = service.addCategory(toCategory);

                logger.info("posted new category to month with id = " + id);
                return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
            }
        } catch (NullPointerException | NotFoundException | DataAccessException e) {
            logger.warn("an error occurred while posting category with empty processes");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(params = {"!sort", "!size", "!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readEmptyMonths(@RequestParam(value = "year") final short YEAR_PARAM) {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            logger.info("month level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean CATEGORIES_FLAG = false;

        try {
            List<?> result = service.findAllByYear(YEAR_PARAM, USER_ID, CATEGORIES_FLAG);
            CollectionModel<?> monthsCollection = service.prepareReadMonthsHateoas(result, YEAR_PARAM, USER_ID, PAGEABLE_PARAM_FLAG, CATEGORIES_FLAG);

            logger.info("exposing all months!");
            return ResponseEntity.ok(monthsCollection);
        } catch (NotFoundException e) {
            logger.info("no months");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.warn("an error while loading months occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readEmptyMonths(final Pageable page,
                                      @RequestParam(value = "year") final short YEAR_PARAM) {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            logger.info("month level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean CATEGORIES_FLAG = false;

        try {
            List<?> result = service.findAllByYear(page, YEAR_PARAM, USER_ID, CATEGORIES_FLAG).toList();
            CollectionModel<?> monthsCollection = service.prepareReadMonthsHateoas(result, YEAR_PARAM, USER_ID, PAGEABLE_PARAM_FLAG, CATEGORIES_FLAG);

            logger.info("exposing all months!");
            return ResponseEntity.ok(monthsCollection);
        } catch (NotFoundException e) {
            logger.info("no months");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.warn("an error while loading months occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(params = {"categories", "!sort", "!size", "!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readMonthsWithCategories(@RequestParam(value = "year") final short YEAR_PARAM) {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            logger.info("month level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean CATEGORIES_FLAG = true;

        try {
            List<?> result = service.findAllByYear(YEAR_PARAM, USER_ID, CATEGORIES_FLAG);
            CollectionModel<?> monthsCollection = service.prepareReadMonthsHateoas(result, YEAR_PARAM, USER_ID, PAGEABLE_PARAM_FLAG, CATEGORIES_FLAG);

            logger.info("exposing all months + categories!");
            return ResponseEntity.ok(monthsCollection);
        } catch (NotFoundException e) {
            logger.info("no months");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.warn("an error while loading months + categories occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(params = {"categories"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readMonthsWithCategories(final Pageable page,
                                               @RequestParam(value = "year") final short YEAR_PARAM) {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            logger.info("month level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean CATEGORIES_FLAG = true;

        try {
            List<?> result = service.findAllByYear(page, YEAR_PARAM, USER_ID, CATEGORIES_FLAG).toList();
            CollectionModel<?> monthsCollection = service.prepareReadMonthsHateoas(result, YEAR_PARAM, USER_ID, PAGEABLE_PARAM_FLAG, CATEGORIES_FLAG);

            logger.info("exposing all months!");
            return ResponseEntity.ok(monthsCollection);
        } catch (NotFoundException e) {
            logger.info("no months");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.warn("an error while loading months + categories occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(value = "/{id}", params = {"!sort", "!size", "!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readOneMonthContent(@PathVariable final Integer id,
                                                 @RequestParam(value = "year") final short YEAR_PARAM) {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            logger.info("month level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        final boolean PAGEABLE_PARAM_FLAG = false;

        try {
            List<CategoryNoProcessesReadModel> result = service.findAllCategoriesBelongToMonth(id, USER_ID);
            String month = service.findById(id, USER_ID).getMonth();
            CollectionModel<?> monthCollection = service.prepareReadOneMonthContentHateoas(result, YEAR_PARAM, month, USER_ID, PAGEABLE_PARAM_FLAG);

            logger.info("exposing '" + month + "' month content");
            return ResponseEntity.ok(monthCollection);
        } catch (NotFoundException e) {
            logger.info("no categories found for given month");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.warn("an error while loading month + categories occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readOneMonthContent(final Pageable page,
                                          @PathVariable final Integer id,
                                          @RequestParam(value = "year") final short YEAR_PARAM) {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            logger.info("month level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        final boolean PAGEABLE_PARAM_FLAG = true;

        try {
            List<CategoryNoProcessesReadModel> result = service.findAllCategoriesBelongToMonth(page, id, USER_ID).toList();
            String month = service.findById(id, USER_ID).getMonth();
            CollectionModel<?> monthCollection = service.prepareReadOneMonthContentHateoas(result, YEAR_PARAM, month, USER_ID, PAGEABLE_PARAM_FLAG);

            logger.info("exposing '" + month + "' month content");
            return ResponseEntity.ok(monthCollection);
        } catch (NotFoundException e) {
            logger.info("no categories found for given month");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.warn("an error while loading month + categories occurred");
            return ResponseEntity.badRequest().build();
        }
    }
    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> fullUpdateMonth(@PathVariable final Integer id,
                                           @RequestParam(value = "year") final short YEAR_PARAM,
                                           @RequestBody @Valid final MonthExpenses toUpdate) {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            logger.info("month level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {
            YearExpenses year = service.findByYear(YEAR_PARAM, USER_ID);

            if(service.checkIfGivenMonthNameExist(toUpdate.getMonth(), year, USER_ID)) {
                logger.info("a month '" + toUpdate.getMonth().toLowerCase() + "' in year '" + YEAR_PARAM + "' already exists!");
                return ResponseEntity.badRequest().build();
            } else {
                MonthExpenses month = service.findById(id, USER_ID);
                month.fullUpdate(toUpdate);

                service.save(month);
                logger.info("put month with id = " + id);
                return ResponseEntity.ok().build();
            }
        } catch (NotFoundException | DataAccessException e) {
            logger.warn("an error occurred while put month type");
            return ResponseEntity.badRequest().build();
        }
    }
    @Transactional
    @ResponseBody
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> partUpdateMonth(@PathVariable final Integer id,
                                                  @RequestParam(value = "year") final short YEAR_PARAM,
                                                  @Valid final HttpServletRequest request) {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            logger.info("month level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {
            YearExpenses year = service.findByYear(YEAR_PARAM, USER_ID);

            MonthExpenses month = service.findById(id, USER_ID);
            String monthBeforeUpdate = month.getMonth().toLowerCase();

            MonthExpenses updatedMonth = objectMapper.readerForUpdating(month).readValue(request.getReader());
            String monthAfterUpdate = updatedMonth.getMonth().toLowerCase();

            if(!monthBeforeUpdate.equals(monthAfterUpdate)) {
                if(service.checkIfGivenMonthNameExist(monthAfterUpdate, year, USER_ID)) {
                    logger.info("a month '" + monthAfterUpdate.toLowerCase() + "' in year '" + YEAR_PARAM + "' already exists!");
                    throw new IllegalStateException();
                }
            }

            service.saveAndFlush(updatedMonth);
            logger.info("successfully patched month nr " + id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException | IOException | DataAccessException e) {
            logger.warn("an error occurred while patching month");
            return ResponseEntity.badRequest().build();
        }
    }
    @Transactional
    @ResponseBody
    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> deleteMonth(@PathVariable final Integer id,
                                       @RequestParam(value = "year") final short YEAR_PARAM) {

        final String USER_ID = User.getUserId();
        if(!service.monthLevelValidationSuccess(YEAR_PARAM, USER_ID)) {
            logger.info("month level validation failed, no year founded");
            return ResponseEntity.badRequest().build();
        }

        try {
            String monthName = service.findById(id, USER_ID).getMonth();

            service.deleteMonth(id, USER_ID);
            logger.warn("deleted month '" + monthName + "'");
            return ResponseEntity.ok().build();
        } catch (NotFoundException | DataAccessException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Templates
     */
}

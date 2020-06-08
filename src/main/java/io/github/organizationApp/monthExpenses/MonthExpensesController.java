package io.github.organizationApp.monthExpenses;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.organizationApp.categoryExpenses.CategoryNoProcessesReadModel;
import io.github.organizationApp.categoryExpenses.CategoryType;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
import io.github.organizationApp.yearExpenses.YearExpenses;
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
@RequestMapping("/month")
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
    ResponseEntity<MonthExpenses> addEmptyMonth(@RequestParam(value = "year") final String YEAR_PARAM,
                                                @RequestBody @Valid final MonthExpenses toMonth) {

//        if(!service.monthLevelValidationSuccess(YEAR_PARAM)) {
//            logger.info("month level validation failed, no year founded");
//            return ResponseEntity.badRequest().build();
//        }

        // TODO -> Przed POSTEM miesiaca trzeba sprawdzic czy czasem taki juz nie istnieje

        try {
            service.setYearToNewMonth(YEAR_PARAM, toMonth);
            MonthExpenses result = service.save(toMonth);

            logger.info("posted new empty month with id = " + result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        } catch (NullPointerException | DataAccessException | NotFoundException e) {
            logger.warn("an error occurred while posting new empty month");
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @ResponseBody
    @PostMapping(params = {"categories"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MonthFullReadModel> addMonthWithCategories(@RequestParam(value = "year") final String YEAR_PARAM,
                                                              @RequestBody @Valid final MonthFullWriteModel toMonthExpenses) {

//        if(!service.monthLevelValidationSuccess(YEAR_PARAM)) {
//            logger.info("month level validation failed, no year founded");
//            return ResponseEntity.badRequest().build();
//        }

        // TODO -> Przed POSTEM miesiaca trzeba sprawdzic czy czasem taki juz nie istnieje

        try {
            YearExpenses belongingYear = service.findByYear(YEAR_PARAM);
            MonthFullReadModel result = service.createMonthWithCategories(belongingYear, toMonthExpenses);

            logger.info("posted new month + categories content, with id = " + result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);

        } catch (NotFoundException | DataAccessException e) {
            logger.info("an error occurred while posting month with categories");
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @ResponseBody
    @PostMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CategoryType> addCategoryToChosenMonth(@PathVariable final Integer id,
                                                           @RequestParam(value = "year") final String YEAR_PARAM,
                                                           @RequestBody @Valid final CategoryType toCategoryType) {

//        if(!service.monthLevelValidationSuccess(YEAR_PARAM)) {
//            logger.info("month level validation failed, no year founded");
//            return ResponseEntity.badRequest().build();
//        }

        // TODO -> sprawdzic czy nowa kategoria w danym miesiącu juz nie istnieje

        try {
            service.setMonthToNewCategory(id, toCategoryType);
            CategoryType result = service.addCategory(toCategoryType);

            logger.info("posted new category to month with id = " + id);
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        } catch (NotFoundException | DataAccessException e) {
            logger.info("an error occurred while posting month with empty categories");
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @GetMapping(params = {"!sort", "!size", "!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readEmptyMonths(@RequestParam(value = "year") final String YEAR_PARAM) {

//        if(!service.monthLevelValidationSuccess(YEAR_PARAM)) {
//            logger.info("month level validation failed, no year founded");
//            return ResponseEntity.badRequest().build();
//        }

        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean CATEGORIES_FLAG = false;

        try {
            List<?> result = service.findAllByYear(YEAR_PARAM, CATEGORIES_FLAG);
            CollectionModel<?> monthsCollection = service.prepareReadMonthsHateoas(result, YEAR_PARAM, PAGEABLE_PARAM_FLAG, CATEGORIES_FLAG);

            logger.info("exposing all months!");
            return ResponseEntity.ok(monthsCollection);
        } catch (NotFoundException e) {
            logger.info("no months");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.info("an error while loading months + categories occurred");
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readEmptyMonths(final Pageable page,
                                      @RequestParam(value = "year") final String YEAR_PARAM) {

//        if(!service.monthLevelValidationSuccess(YEAR_PARAM)) {
//            logger.info("month level validation failed, no year founded");
//            return ResponseEntity.badRequest().build();
//        }

        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean CATEGORIES_FLAG = false;

        try {
            List<?> result = service.findAllByYear(page, YEAR_PARAM, CATEGORIES_FLAG).toList();
            CollectionModel<?> monthsCollection = service.prepareReadMonthsHateoas(result, YEAR_PARAM, PAGEABLE_PARAM_FLAG, CATEGORIES_FLAG);

            logger.info("exposing all months!");
            return ResponseEntity.ok(monthsCollection);
        } catch (NotFoundException e) {
            logger.info("no months");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.info("an error while loading months + categories occurred");
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @GetMapping(params = {"categories", "!sort", "!size", "!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readMonthsWithCategories(@RequestParam(value = "year") final String YEAR_PARAM) {

//        if(!service.monthLevelValidationSuccess(YEAR_PARAM)) {
//            logger.info("month level validation failed, no year founded");
//            return ResponseEntity.badRequest().build();
//        }

        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean CATEGORIES_FLAG = true;

        try {
            List<?> result = service.findAllByYear(YEAR_PARAM, CATEGORIES_FLAG);
            CollectionModel<?> monthsCollection = service.prepareReadMonthsHateoas(result, YEAR_PARAM, PAGEABLE_PARAM_FLAG, CATEGORIES_FLAG);

            logger.info("exposing all categories!");
            return ResponseEntity.ok(monthsCollection);
        } catch (NotFoundException e) {
            logger.info("no months");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.info("an error while loading months + categories occurred");
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @GetMapping(params = {"categories"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readMonthsWithCategories(final Pageable page,
                                               @RequestParam(value = "year") final String YEAR_PARAM) {

//        if(!service.monthLevelValidationSuccess(YEAR_PARAM)) {
//            logger.info("month level validation failed, no year founded");
//            return ResponseEntity.badRequest().build();
//        }

        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean CATEGORIES_FLAG = true;

        try {
            List<?> result = service.findAllByYear(page, YEAR_PARAM, CATEGORIES_FLAG).toList();
            CollectionModel<?> monthsCollection = service.prepareReadMonthsHateoas(result, YEAR_PARAM, PAGEABLE_PARAM_FLAG, CATEGORIES_FLAG);

            logger.info("exposing all months!");
            return ResponseEntity.ok(monthsCollection);
        } catch (NotFoundException e) {
            logger.info("no months");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.info("an error while loading months + categories occurred");
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @GetMapping(value = "/{id}", params = {"!sort", "!size", "!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readOneMonthContent(@PathVariable final Integer id,
                                                 @RequestParam(value = "year") final String YEAR_PARAM) {

//        if(!service.monthLevelValidationSuccess(YEAR_PARAM)) {
//            logger.info("month level validation failed, no year founded");
//            return ResponseEntity.badRequest().build();
//        }

        final boolean PAGEABLE_PARAM_FLAG = false;

        try {
            List<CategoryNoProcessesReadModel> result = service.findAllCategoriesBelongToMonth(id);
            String month = service.findById(id).getMonth();
            CollectionModel<?> monthCollection = service.prepareReadOneMonthContentHateoas(result, YEAR_PARAM, month, PAGEABLE_PARAM_FLAG);

            logger.info("exposing '" + month + "' month content");
            return ResponseEntity.ok(monthCollection);
        } catch (NotFoundException e) {
            logger.info("no categories found for given month");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.info("an error while loading month + categories occurred");
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @GetMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readOneMonthContent(Pageable page,
                                          @PathVariable final Integer id,
                                          @RequestParam(value = "year") final String YEAR_PARAM) {

//        if(!service.monthLevelValidationSuccess(YEAR_PARAM)) {
//            logger.info("month level validation failed, no year founded");
//            return ResponseEntity.badRequest().build();
//        }

        final boolean PAGEABLE_PARAM_FLAG = true;

        try {
            List<CategoryNoProcessesReadModel> result = service.findAllCategoriesBelongToMonth(page, id).toList();
            String month = service.findById(id).getMonth();
            CollectionModel<?> monthCollection = service.prepareReadOneMonthContentHateoas(result, YEAR_PARAM, month, PAGEABLE_PARAM_FLAG);

            logger.info("exposing '" + month + "' month content");
            return ResponseEntity.ok(monthCollection);
        } catch (NotFoundException e) {
            logger.info("no categories found for given month");
            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            logger.info("an error while loading month + categories occurred");
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> fullUpdateMonth(@PathVariable final Integer id,
                                           @RequestParam(value = "year") final String YEAR_PARAM,
                                           @RequestBody @Valid final MonthExpenses toUpdate) {

//        if(!service.monthLevelValidationSuccess(YEAR_PARAM)) {
//            logger.info("month level validation failed, no year founded");
//            return ResponseEntity.badRequest().build();
//        }

        // TODO -> Przed PUTEM miesiaca trzeba sprawdzic czy czasem przy przypadku zmiany nazwy miesiaca juz taki nie istnieje

        try {
            MonthExpenses month = service.findById(id);
            month.fullUpdate(toUpdate);
            service.save(month);

            logger.info("put month type with id = " + id);
            return ResponseEntity.ok().build();
        } catch (NotFoundException | DataAccessException e) {
            logger.info("an error occurred while put month type");
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @ResponseBody
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> partUpdateMonth(@PathVariable final Integer id,
                                                  @RequestParam(value = "year") final String YEAR_PARAM,
                                                  @Valid final HttpServletRequest request) {

//        if(!service.monthLevelValidationSuccess(YEAR_PARAM)) {
//            logger.info("month level validation failed, no year founded");
//            return ResponseEntity.badRequest().build();
//        }

        // TODO -> Przed PATCHEM miesiaca trzeba sprawdzic czy czasem przy przypadku zmiany nazwy miesiaca juz taki nie istnieje

        try {
            MonthExpenses month = service.findById(id);
            MonthExpenses updatedMonth = objectMapper
                    .readerForUpdating(month)
                    .readValue(request.getReader());
            service.saveAndFlush(updatedMonth);

            logger.info("succesfully patched category type nr " + id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException | IOException | DataAccessException e) {
            logger.info("an error occurred while patching category type");
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @ResponseBody
    @DeleteMapping
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> deleteCategoryType(@PathVariable final Integer id,
                                              @RequestParam(value = "year") final String YEAR_PARAM) {

//        if(!service.monthLevelValidationSuccess(YEAR_PARAM)) {
//            logger.info("month level validation failed, no year founded");
//            return ResponseEntity.badRequest().build();
//        }

        try {
            service.deleteMonth(id);
            logger.warn("deleted month type with id = " + id);
            return ResponseEntity.ok().build();
        } catch (DataAccessException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Templates
     */
}

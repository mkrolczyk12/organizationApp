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
    ResponseEntity<YearExpenses> addEmptyYear(@RequestBody @Valid final YearExpenses toYearExpenses) {

        // TODO -> przed POST roku trzeba sprawdzić czy czasem taki juz nie istnieje + rok musi być koniecznie przedstawiony jako liczba

        try {
            YearExpenses result = service.save(toYearExpenses);

            logger.info("posted new year with id = "+result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        } catch (NullPointerException | DataAccessException e) {
            logger.warn("an error occured while posting new year");
            return ResponseEntity.notFound().build();
        }
    }

    @Transactional
    @ResponseBody
    @PostMapping(params = {"months"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<YearFullReadModel> addYearWithMonths(@RequestBody @Valid final YearFullWriteModel toYearExpenses) {

        // TODO -> przed POST roku trzeba sprawdzić czy czasem taki juz nie istnieje + rok musi być koniecznie przedstawiony jako liczba,

        try {
            YearFullReadModel result = service.createYearWithMonths(toYearExpenses);

            logger.info("posted new year + months content, with id = " + result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        } catch (DataAccessException e) {
            logger.info("an error occurred while posting year with months");
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @ResponseBody
    @PostMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MonthExpenses> addMonthToChosenYear(@PathVariable final Integer id,
                                                       @RequestBody @Valid final MonthExpenses toMonthExpenses) {

        // TODO -> sprawdzic czy miesiac w danym roku juz nie istnieje

        try {
            service.setYearToNewMonth(id, toMonthExpenses);
            MonthExpenses result = service.addMonth(toMonthExpenses);

            logger.info("posted new month to year with id = " + id);
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
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
    @GetMapping(params = {"categories"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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

        // TODO -> trzeba sprawdzic czy istnieje taki rok
        // TODO -> Przed PUTEM roku trzeba sprawdzic czy czasem przy przypadku zmiany nazwy roku juz taki nie istnieje

        try {
            YearExpenses year = service.findById(id);
            year.fullUpdate(toUpdate);
            service.save(year);

            logger.info("put year with id = " + id);
            return ResponseEntity.ok().build();
        } catch (NotFoundException | DataAccessException e) {
            logger.info("an error occurred while put year");
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @ResponseBody
    @PatchMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> partUpdateYear(@PathVariable final Integer id,
                                                 @Valid final HttpServletRequest request) {

        // TODO -> trzeba sprawdzic czy istnieje taki rok
        // TODO -> Przed PATCHEM roku trzeba sprawdzic czy czasem przy przypadku zmiany nazwy roku juz taki nie istnieje

        try {
            YearExpenses year = service.findById(id);
            YearExpenses updatedYear = objectMapper
                    .readerForUpdating(year)
                    .readValue(request.getReader());
            service.saveAndFlush(updatedYear);

            logger.info("succesfully patched year nr " + id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException | IOException | DataAccessException e) {
            logger.info("an error occurred while patching year");
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @ResponseBody
    @DeleteMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> deleteYear(@PathVariable final Integer id) {

        // TODO -> trzeba sprawdzic czy istnieje taki rok

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

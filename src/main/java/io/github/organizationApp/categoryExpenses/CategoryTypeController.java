package io.github.organizationApp.categoryExpenses;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.organizationApp.categoryExpenses.projection.CategoryFullReadModel;
import io.github.organizationApp.categoryExpenses.projection.CategoryFullWriteModel;
import io.github.organizationApp.expensesProcess.Process;
import io.github.organizationApp.globalControllerAdvice.ExceptionResponse;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
import io.github.organizationApp.monthExpenses.MonthExpenses;
import io.github.organizationApp.security.SecurityExceptionsProcessing;
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
@CategoryExceptionsProcessing
@SecurityExceptionsProcessing
@RequestMapping("/moneyapp/categories")
public class CategoryTypeController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryTypeController.class);
    private final CategoryTypeService service;
    private final ObjectMapper objectMapper;

    CategoryTypeController(final CategoryTypeService service, final ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    /**
     * JSON:API
     */
    @Transactional
    @ResponseBody
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CategoryType> addEmptyCategoryType(@RequestParam(value = "year") final short YEAR_PARAM,
                                                      @RequestParam(value = "month") final String MONTH_PARAM,
                                                      @RequestBody @Valid final CategoryType toCategory) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM, MONTH_PARAM, USER_ID)) {
            final String message = "category validation failed, no relation between given year and month";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        MonthExpenses month = service.findByMonth(MONTH_PARAM, USER_ID);

        if(service.checkIfGivenCategoryExist(toCategory.getType(), month, USER_ID)) {
            final String message = "a category '" + toCategory.getType().toLowerCase() + "' in year '" + YEAR_PARAM + "' and month '" +MONTH_PARAM + "' already exists!";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }
        else {
            service.setMonthAndOwnerToNewCategory(YEAR_PARAM, MONTH_PARAM, toCategory, USER_ID);
            CategoryType result = service.save(toCategory);
            logger.info("posted new empty category type with id = "+result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        }
    }
    @Transactional
    @ResponseBody
    @PostMapping(params = {"processes"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CategoryFullReadModel> addCategoryTypeWithProcesses(@RequestParam(value = "year") final short YEAR_PARAM,
                                                                       @RequestParam(value = "month") final String MONTH_PARAM,
                                                                       @RequestBody @Valid final CategoryFullWriteModel toCategory) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM, USER_ID)) {
            final String message = "category level validation failed, no relation between given year and month";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        MonthExpenses month = service.findByMonth(MONTH_PARAM, USER_ID);

        if(service.checkIfGivenCategoryExist(toCategory.getType(), month, USER_ID)) {
            final String message = "a category '" + toCategory.getType().toLowerCase() + "' in year '" + YEAR_PARAM + "' and month '" +MONTH_PARAM + "' already exists!";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        } else {
            MonthExpenses belongingMonth = service.findByMonthAndBelongingYear(YEAR_PARAM, MONTH_PARAM, USER_ID);
            CategoryFullReadModel result = service.createCategoryWithProcesses(belongingMonth, USER_ID, toCategory);

            logger.info("posted new category type + processes content, with id = " + result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        }
    }
    @Transactional
    @ResponseBody
    @PostMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Process> addProcessToChosenCategory(@PathVariable final Integer id,
                                                              @RequestParam(value = "year") final short YEAR_PARAM,
                                                              @RequestParam(value = "month") final String MONTH_PARAM,
                                                              @RequestBody @Valid final Process toProcess) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!(service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM, USER_ID) && service.existsById(id))) {
            final String message = "category level validation failed, no relation between given year and month";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        service.setCategoryAndOwnerToNewProcess(id, toProcess, USER_ID);
        Process result = service.addProcess(toProcess);

        logger.info("posted new process to category with id = " + id);
        return ResponseEntity.created(URI.create("/" + result.getId())).body(result);

    }
    @ResponseBody
    @GetMapping(params = {"!sort","!size","!page"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readEmptyCategoryTypes(@RequestParam(value = "year") final short YEAR_PARAM,
                                                    @RequestParam(value = "month") final String MONTH_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM, USER_ID)) {
            final String message = "category level validation failed, no relation between given year and month";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean PROCESSES_FLAG = false;

        List<?> result = service.findAllByMonthExpensesId(YEAR_PARAM, MONTH_PARAM, USER_ID, PROCESSES_FLAG);
        CollectionModel<?> categoryTypeCollection = service.prepareReadCategoryTypesHateoas(result, YEAR_PARAM, MONTH_PARAM, USER_ID, PAGEABLE_PARAM_FLAG, PROCESSES_FLAG);

        logger.info("exposing all categories!");
        return ResponseEntity.ok(categoryTypeCollection);
    }
    @ResponseBody
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readEmptyCategoryTypes(Pageable page,
                                            @RequestParam(value = "year") final short YEAR_PARAM,
                                            @RequestParam(value = "month") final String MONTH_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM, USER_ID)) {
            final String message = "category level validation failed, no relation between given year and month";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean PROCESSES_FLAG = false;

        List<?> result = service.findAllByMonthExpensesId(page, YEAR_PARAM, MONTH_PARAM, USER_ID, PROCESSES_FLAG).toList();
        CollectionModel<?> categoryTypeCollection = service.prepareReadCategoryTypesHateoas(result, YEAR_PARAM, MONTH_PARAM, USER_ID, PAGEABLE_PARAM_FLAG, PROCESSES_FLAG);

        logger.info("exposing all categories!");
        return ResponseEntity.ok(categoryTypeCollection);
    }
    @ResponseBody
    @GetMapping(params = {"processes", "!sort","!size","!page"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readCategoryTypesWithProcesses(@RequestParam(value = "year") final short YEAR_PARAM,
                                                            @RequestParam(value = "month") final String MONTH_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM, User.getUserId())) {
            final String message = "category level validation failed, no relation between given year and month";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean PROCESSES_FLAG = true;
        List<?> result = service.findAllByMonthExpensesId(YEAR_PARAM, MONTH_PARAM, USER_ID, PROCESSES_FLAG);
        CollectionModel<?> categoryTypeCollection = service.prepareReadCategoryTypesHateoas(result, YEAR_PARAM, MONTH_PARAM, USER_ID, PAGEABLE_PARAM_FLAG, PROCESSES_FLAG);

        logger.info("exposing all categories!");
        return ResponseEntity.ok(categoryTypeCollection);
    }
    @ResponseBody
    @GetMapping(params = {"processes"}, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readCategoryTypesWithProcesses(final Pageable page,
                                                     @RequestParam(value = "year") final short YEAR_PARAM,
                                                     @RequestParam(value = "month") final String MONTH_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM, USER_ID)) {
            final String message = "category level validation failed, no relation between given year and month";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean PROCESSES_FLAG = true;
        List<?> result = service.findAllByMonthExpensesId(page, YEAR_PARAM, MONTH_PARAM, USER_ID, PROCESSES_FLAG).toList();
        CollectionModel<?> categoryTypeCollection = service.prepareReadCategoryTypesHateoas(result, YEAR_PARAM, MONTH_PARAM, USER_ID, PAGEABLE_PARAM_FLAG, PROCESSES_FLAG);

        logger.info("exposing all categories!");
        return ResponseEntity.ok(categoryTypeCollection);
    }
    @ResponseBody
    @GetMapping(value = "/{id}", params = {"!sort","!size","!page"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readOneCategoryTypeContent(@PathVariable final Integer id,
                                                        @RequestParam(value = "year") final short YEAR_PARAM,
                                                        @RequestParam(value = "month") final String MONTH_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM, USER_ID)) {
            final String message = "category level validation failed, no relation between given year and month";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        final boolean PAGEABLE_PARAM_FLAG = false;

        List<Process> result = service.findAllProcessesBelongToCategory(id, USER_ID);
        String category = service.findById(id, USER_ID).getType();
        CollectionModel<?> categoryCollection = service.prepareReadOneCategoryTypeContentHateoas(result, YEAR_PARAM, MONTH_PARAM, category, PAGEABLE_PARAM_FLAG);

        logger.info("exposing '"+ category + "' category content");
        return ResponseEntity.ok(categoryCollection);
    }
    @ResponseBody
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readOneCategoryTypeContent(Pageable page,
                                                 @PathVariable final Integer id,
                                                 @RequestParam(value = "year") final short YEAR_PARAM,
                                                 @RequestParam(value = "month") final String MONTH_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM, USER_ID)) {
            final String message = "category level validation failed, no relation between given year and month";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        final boolean PAGEABLE_PARAM_FLAG = true;

        List<Process> result = service.findAllProcessesBelongToCategory(page, id, USER_ID).toList();
        String category = service.findById(id, USER_ID).getType();
        CollectionModel<?> categoryCollection = service.prepareReadOneCategoryTypeContentHateoas(result, YEAR_PARAM, MONTH_PARAM, category, PAGEABLE_PARAM_FLAG);

        logger.info("exposing '"+ category + "' category content");
        return ResponseEntity.ok(categoryCollection);
    }
    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> fullUpdateCategoryType(@PathVariable final Integer id,
                                                  @RequestParam(value = "year") final short YEAR_PARAM,
                                                  @RequestParam(value = "month") final String MONTH_PARAM,
                                                  @RequestBody @Valid final CategoryType toUpdate) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM, USER_ID)) {
            final String message = "category level validation failed, no relation between given year and month";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        MonthExpenses month = service.findByMonth(MONTH_PARAM, USER_ID);

        if(service.checkIfGivenCategoryExist(toUpdate.getType(), month, USER_ID)) {
            final String message = "a category '" + toUpdate.getType().toLowerCase() + "' in year '" + YEAR_PARAM + "' and month '" +MONTH_PARAM + "' already exists!";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        } else {
            CategoryType category = service.findById(id, USER_ID);
            category.fullUpdate(toUpdate);

            service.save(category);
            logger.info("put category type with id = "+ id);
            return ResponseEntity.ok().build();
        }
    }
    @Transactional
    @ResponseBody
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> partUpdateCategoryType(@PathVariable final Integer id,
                                                         @RequestParam(value = "year") final short YEAR_PARAM,
                                                         @RequestParam(value = "month") final String MONTH_PARAM,
                                                         @Valid final HttpServletRequest request) throws NotFoundException, IOException {

        final String USER_ID = User.getUserId();
        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM, USER_ID)) {
            final String message = "category level validation failed, no relation between given year and month";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        MonthExpenses month = service.findByMonth(MONTH_PARAM, USER_ID);

        CategoryType category = service.findById(id, USER_ID);
        String categoryBeforeUpdate = category.getType().toLowerCase();

        CategoryType updatedCategoryType = objectMapper.readerForUpdating(category).readValue(request.getReader());
        String categoryAfterUpdate = updatedCategoryType.getType();

        if(!categoryBeforeUpdate.equals(categoryAfterUpdate)) {
            if(service.checkIfGivenCategoryExist(categoryAfterUpdate, month, USER_ID)) {
                final String message = "category '" + updatedCategoryType.getType().toLowerCase() + "' in year '" + YEAR_PARAM + "' and month '" + MONTH_PARAM + "' already exists!";
                logger.info(message);

                throw new IllegalStateException(message);
            }
        }
        service.saveAndFlush(updatedCategoryType);
        logger.info("succesfully patched category type nr "+id);
        return ResponseEntity.noContent().build();
    }
    @Transactional
    @ResponseBody
    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> deleteCategoryType(@PathVariable final Integer id,
                                              @RequestParam(value = "year") final short YEAR_PARAM,
                                              @RequestParam(value = "month") final String MONTH_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!(service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM, USER_ID) && service.existsById(id))) {
            final String message = "category level validation failed, no relation between given year and month";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        String categoryName = service.findById(id, USER_ID).getType();

        service.deleteCategoryType(id);
        logger.warn("deleted category '" + categoryName + "'");
        return ResponseEntity.ok().build();
    }
    /**
     * Templates
     */
}

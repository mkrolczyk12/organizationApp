package io.github.organizationApp.categoryExpenses;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.organizationApp.expensesProcess.Process;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
import io.github.organizationApp.monthExpenses.MonthExpenses;
import io.github.organizationApp.monthExpenses.MonthExpensesRepository;
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
@RequestMapping("/category")
public class CategoryTypeController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryTypeController.class);
    private final CategoryTypeService service;
    private final ObjectMapper objectMapper;
    private final MonthExpensesRepository repository;

    CategoryTypeController(final CategoryTypeService service, final ObjectMapper objectMapper, MonthExpensesRepository repo) {
        this.service = service;
        this.objectMapper = objectMapper;
        this.repository = repo;
    }

    /**
     * JSON:API
     */
    @Transactional
    @ResponseBody
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CategoryType> addEmptyCategoryType(@RequestParam(value = "year") final String YEAR_PARAM,
                                                      @RequestParam(value = "month") final String MONTH_PARAM,
                                                      @RequestBody @Valid final CategoryType toCategory) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("category level validation failed, no relation between given year and month");
            return ResponseEntity.badRequest().build();
        }

        try {
            if(service.checkIfGivenCategoryExist(toCategory.getType(), YEAR_PARAM, MONTH_PARAM)) {
                logger.info("a category '" + toCategory.getType().toLowerCase() + "' in year '" + YEAR_PARAM + "' and month '" +MONTH_PARAM + "' already exists!");
                return ResponseEntity.badRequest().build();
            }
            else {
                service.setMonthToNewCategory(YEAR_PARAM, MONTH_PARAM, toCategory);
                CategoryType result = service.save(toCategory);
                logger.info("posted new empty category type with id = "+result.getId());
                return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
            }
        } catch (NullPointerException | DataAccessException | NotFoundException e) {
            logger.info("an error occurred while posting category type");
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @ResponseBody
    @PostMapping(params = {"processes"}, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CategoryFullReadModel> addCategoryTypeWithProcesses(@RequestParam(value = "year") final String YEAR_PARAM,
                                                                       @RequestParam(value = "month") final String MONTH_PARAM,
                                                                       @RequestBody @Valid final CategoryFullWriteModel toCategory) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("category level validation failed, no relation between given year and month");
            return ResponseEntity.badRequest().build();
        }

        try {
            if(service.checkIfGivenCategoryExist(toCategory.getType(), YEAR_PARAM, MONTH_PARAM)) {
                logger.info("a category '" + toCategory.getType().toLowerCase() + "' in year '" + YEAR_PARAM + "' and month '" +MONTH_PARAM + "' already exists!");
                return ResponseEntity.badRequest().build();
            } else {
                MonthExpenses belongingMonth = service.findByMonthAndBelongingYear(YEAR_PARAM, MONTH_PARAM);
                CategoryFullReadModel result = service.createCategoryWithProcesses(belongingMonth, toCategory);

                logger.info("posted new category type + processes content, with id = " + result.getId());
                return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
            }
        } catch (NullPointerException | NotFoundException | DataAccessException e) {
            logger.info("an error occurred while posting category type");
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @ResponseBody
    @PostMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Process> addProcessToChosenCategory(@PathVariable final Integer id,
                                                              @RequestParam(value = "year") final String YEAR_PARAM,
                                                              @RequestParam(value = "month") final String MONTH_PARAM,
                                                              @RequestBody @Valid final Process toProcess) {

        if(!(service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM) && service.existsById(id))) {
            logger.info("category level validation failed, no relation between given year, month and category id");
            return ResponseEntity.badRequest().build();
        }

        try {
            service.setCategoryToNewProcess(id, toProcess);
            Process result = service.addProcess(toProcess);

            logger.info("posted new process to category with id = " + id);
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        } catch (NullPointerException | NotFoundException | DataAccessException e) {
            return ResponseEntity.badRequest().build();
        }

    }

    @ResponseBody
    @GetMapping(params = {"!sort","!size","!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readEmptyCategoryTypes(@RequestParam(value = "year") final String YEAR_PARAM,
                                                    @RequestParam(value = "month") final String MONTH_PARAM) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("category level validation failed, no relation between given year and month");
            return ResponseEntity.badRequest().build();
        }

        final boolean PAGEABLE_PARAM_FLAG = false;
        final boolean PROCESSES_FLAG = false;

        try {
            List<?> result = service.findAllByMonthExpensesId(YEAR_PARAM, MONTH_PARAM, PROCESSES_FLAG);
            CollectionModel<?> categoryTypeCollection = service.prepareReadCategoryTypesHateoas(result, YEAR_PARAM, MONTH_PARAM, PAGEABLE_PARAM_FLAG, PROCESSES_FLAG);

            logger.info("exposing all categories!");
            return ResponseEntity.ok(categoryTypeCollection);
        } catch (NullPointerException | NotFoundException e) {
            logger.info("no categories");
            return ResponseEntity.ok().build();
        }  catch (DataAccessException e) {
            logger.info("an error while loading categories + processes occurred");
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readEmptyCategoryTypes(Pageable page,
                                            @RequestParam(value = "year") final String YEAR_PARAM,
                                            @RequestParam(value = "month") final String MONTH_PARAM) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("category level validation failed, no relation between given year and month");
            return ResponseEntity.badRequest().build();
        }

        final boolean PAGEABLE_PARAM_FLAG = true;
        final boolean PROCESSES_FLAG = false;

        try {
            List<?> result = service.findAllByMonthExpensesId(page, YEAR_PARAM, MONTH_PARAM, PROCESSES_FLAG).toList();
            CollectionModel<?> categoryTypeCollection = service.prepareReadCategoryTypesHateoas(result, YEAR_PARAM, MONTH_PARAM, PAGEABLE_PARAM_FLAG, PROCESSES_FLAG);

            logger.info("exposing all categories!");
            return ResponseEntity.ok(categoryTypeCollection);
        } catch (NullPointerException | NotFoundException e) {
            logger.info("no categories");
            return ResponseEntity.ok().build();
        }  catch (DataAccessException e) {
            logger.info("an error while loading categories + processes occurred");
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @GetMapping(params = {"processes", "!sort","!size","!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readCategoryTypesWithProcesses(@RequestParam(value = "year") final String YEAR_PARAM,
                                                            @RequestParam(value = "month") final String MONTH_PARAM) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("category level validation failed, no relation between given year and month");
            return ResponseEntity.badRequest().build();
        }

        try {
            final boolean PAGEABLE_PARAM_FLAG = false;
            final boolean PROCESSES_FLAG = true;
            List<?> result = service.findAllByMonthExpensesId(YEAR_PARAM, MONTH_PARAM, PROCESSES_FLAG);
            CollectionModel<?> categoryTypeCollection = service.prepareReadCategoryTypesHateoas(result, YEAR_PARAM, MONTH_PARAM, PAGEABLE_PARAM_FLAG, PROCESSES_FLAG);

            logger.info("exposing all categories!");
            return ResponseEntity.ok(categoryTypeCollection);
        } catch (NullPointerException | NotFoundException e) {
            logger.info("no categories");
            return ResponseEntity.ok().build();
        }  catch (DataAccessException e) {
            logger.info("an error while loading categories + processes occurred");
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @GetMapping(params = {"processes"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readCategoryTypesWithProcesses(final Pageable page,
                                                     @RequestParam(value = "year") final String YEAR_PARAM,
                                                     @RequestParam(value = "month") final String MONTH_PARAM) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("category level validation failed, no relation between given year and month");
            return ResponseEntity.badRequest().build();
        }

        try {
            final boolean PAGEABLE_PARAM_FLAG = true;
            final boolean PROCESSES_FLAG = true;
            List<?> result = service.findAllByMonthExpensesId(page, YEAR_PARAM, MONTH_PARAM, PROCESSES_FLAG).toList();
            CollectionModel<?> categoryTypeCollection = service.prepareReadCategoryTypesHateoas(result, YEAR_PARAM, MONTH_PARAM, PAGEABLE_PARAM_FLAG, PROCESSES_FLAG);

            logger.info("exposing all categories!");
            return ResponseEntity.ok(categoryTypeCollection);
        } catch (NotFoundException e) {
            logger.info("no categories");
            return ResponseEntity.ok().build();
        }  catch (DataAccessException e) {
            logger.info("an error while loading categories + processes occurred");
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @GetMapping(value = "/{id}", params = {"!sort","!size","!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readOneCategoryTypeContent(@PathVariable final Integer id,
                                                        @RequestParam(value = "year") final String YEAR_PARAM,
                                                        @RequestParam(value = "month") final String MONTH_PARAM) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("category level validation failed, no relation between given year and month");
            return ResponseEntity.badRequest().build();
        }

        final boolean PAGEABLE_PARAM_FLAG = false;

        try {
            List<Process> result = service.findAllProcessesBelongToCategory(id);
            String category = service.findById(id).getType();
            CollectionModel<?> categoryCollection = service.prepareReadOneCategoryTypeContentHateoas(result, YEAR_PARAM, MONTH_PARAM, category, PAGEABLE_PARAM_FLAG);

            logger.info("exposing '"+ category + "' category content");
            return ResponseEntity.ok(categoryCollection);
        } catch (NotFoundException e) {
            logger.info("no processes found for given category");
            return ResponseEntity.ok().build();
        }  catch (DataAccessException e) {
            logger.info("an error while loading category + processes occurred");
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @GetMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readOneCategoryTypeContent(Pageable page,
                                                 @PathVariable final Integer id,
                                                 @RequestParam(value = "year") final String YEAR_PARAM,
                                                 @RequestParam(value = "month") final String MONTH_PARAM) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("category level validation failed, no relation between given year and month");
            return ResponseEntity.badRequest().build();
        }

        final boolean PAGEABLE_PARAM_FLAG = true;

        try {
            List<Process> result = service.findAllProcessesBelongToCategory(page, id).toList();
            String category = service.findById(id).getType();
            CollectionModel<?> categoryCollection = service.prepareReadOneCategoryTypeContentHateoas(result, YEAR_PARAM, MONTH_PARAM, category, PAGEABLE_PARAM_FLAG);

            logger.info("exposing '"+ category + "' category content");
            return ResponseEntity.ok(categoryCollection);
        } catch (NotFoundException e) {
            logger.info("no processes found for given category");
            return ResponseEntity.ok().build();
        }  catch (DataAccessException e) {
            logger.info("an error while loading category + processes occurred");
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> fullUpdateCategoryType(@PathVariable final Integer id,
                                                  @RequestParam(value = "year") final String YEAR_PARAM,
                                                  @RequestParam(value = "month") final String MONTH_PARAM,
                                                  @RequestBody @Valid final CategoryType toUpdate) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("category level validation failed, no relation between given year and month");
            return ResponseEntity.badRequest().build();
        }

        try {
            if(service.checkIfGivenCategoryExist(toUpdate.getType(), YEAR_PARAM, MONTH_PARAM)) {
                logger.info("a category '" + toUpdate.getType().toLowerCase() + "' in year '" + YEAR_PARAM + "' and month '" +MONTH_PARAM + "' already exists!");
                return ResponseEntity.badRequest().build();
            } else {
                CategoryType category = service.findById(id);
                final String previousCategoryName = category.getType();
                category.fullUpdate(toUpdate);

                service.save(category);
                service.removePreviousCategoryNameInMonthAndYear(previousCategoryName, YEAR_PARAM, MONTH_PARAM);
                logger.info("put category type with id = "+ id);
                return ResponseEntity.ok().build();
            }
        } catch (NullPointerException | NotFoundException | DataAccessException e) {
            logger.info("an error occurred while put category type");
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @ResponseBody
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> partUpdateCategoryType(@PathVariable final Integer id,
                                                         @RequestParam(value = "year") final String YEAR_PARAM,
                                                         @RequestParam(value = "month") final String MONTH_PARAM,
                                                         @Valid final HttpServletRequest request) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("category level validation failed, no relation between given year and month");
            return ResponseEntity.badRequest().build();
        }

        try {
            boolean REMOVE_FLAG = false;
            CategoryType category = service.findById(id);
            String categoryBeforeUpdate = category.getType().toLowerCase();

            CategoryType updatedCategoryType = objectMapper.readerForUpdating(category).readValue(request.getReader());
            String categoryAfterUpdate = updatedCategoryType.getType();

            if(!categoryBeforeUpdate.equals(categoryAfterUpdate)) {
                REMOVE_FLAG = true;
                if(service.checkIfGivenCategoryExist(categoryAfterUpdate, YEAR_PARAM, MONTH_PARAM)) {
                    logger.info("category '" + updatedCategoryType.getType().toLowerCase() + "' in year '" + YEAR_PARAM + "' and month '" + MONTH_PARAM + "' already exists!");
                    throw new IllegalStateException();
                }
            }
            service.saveAndFlush(updatedCategoryType);
            if(REMOVE_FLAG) {
                service.removePreviousCategoryNameInMonthAndYear(categoryBeforeUpdate, YEAR_PARAM, MONTH_PARAM);
            }
            logger.info("succesfully patched category type nr "+id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException | IOException | DataAccessException e) {
            logger.info("an error occurred while patching category type");
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @ResponseBody
    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> deleteCategoryType(@PathVariable final Integer id,
                                              @RequestParam(value = "year") final String YEAR_PARAM,
                                              @RequestParam(value = "month") final String MONTH_PARAM) {

        if(!(service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM) && service.existsById(id))) {
            logger.info("category level validation failed, no relation between given year, month and category id");
            return ResponseEntity.badRequest().build();
        }
        try {
            service.deleteCategoryType(id);
            logger.warn("deleted category type with id = " +id);
            return ResponseEntity.ok().build();
        } catch (DataAccessException e ) {
            return ResponseEntity.badRequest().build();
        }
    }
    /**
     * Templates
     */
}

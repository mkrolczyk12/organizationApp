package io.github.organizationApp.expensesCategoryType;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.organizationApp.expensesProcess.Process;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
import io.github.organizationApp.monthExpenses.MonthExpenses;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.List;


@Controller
@GeneralExceptionsProcessing
@RequestMapping("/category")
class CategoryTypeController {
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
    @PostMapping(params = "empty", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CategoryType> addEmptyCategoryType(@RequestParam(value = "year") String YEAR_PARAM,
                                                      @RequestParam(value = "month") String MONTH_PARAM,
                                                      @RequestBody @Valid CategoryType toCategory) {
//        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
//            logger.info("no relation between given year and month, can't continue");
//            return ResponseEntity.badRequest().build();
//        }
        try {
            CategoryType result = service.save(toCategory);
            logger.info("posted new empty category type with id = "+result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        } catch (Exception e) {
            logger.info("an error occured while posting category type");
            return ResponseEntity.notFound().build();
        }
    }
    @Transactional
    @ResponseBody
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PlainReadModel> addCategoryTypeWithProcesses(@RequestParam(value = "year") String YEAR_PARAM,
                                                                @RequestParam(value = "month") String MONTH_PARAM,
                                                                @RequestBody @Valid PlainWriteModel toCategory) {

//        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
//            logger.info("no relation between given year and month, can't continue");
//            return ResponseEntity.badRequest().build();
//        }
        try {
            MonthExpenses belongingMonth = service.findByMonthAndBelongingYear(YEAR_PARAM, MONTH_PARAM);
            PlainReadModel result = service.createCategoryWithProcesses(belongingMonth, toCategory);
            logger.info("posted new category type + processes content, with id = "+result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        } catch (NotFoundException e) {
            logger.info("an error occured while posting category type");
            return ResponseEntity.badRequest().build();
        }
    }
    @ResponseBody
    @GetMapping(params = {"!sort","!size","!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readEmptyCategoryTypes(@RequestParam(value = "year") String YEAR_PARAM,
                                              @RequestParam(value = "month") String MONTH_PARAM) {

//        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
//            logger.info("no relation between given year and month, can't continue");
//            return ResponseEntity.badRequest().build();
//        }
        try {
            final boolean PAGEABLE_PARAM_FLAG = false;
            List<PlainReadModel> result = service.findAll();
            CollectionModel<?> categoryTypeCollection = service.prepareReadEmptyCategoryTypesHateoas(result, YEAR_PARAM, MONTH_PARAM, PAGEABLE_PARAM_FLAG);

            logger.info("exposing all categories!");
            return ResponseEntity.ok(categoryTypeCollection);
        } catch (NullPointerException e) {
            logger.info("no categories");
            return ResponseEntity.noContent().build();
        }
    }
    @ResponseBody
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readEmptyCategoryTypes(Pageable page,
                                            @RequestParam(value = "year") String YEAR_PARAM,
                                            @RequestParam(value = "month") String MONTH_PARAM) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("no relation between given year and month, can't continue");
            return ResponseEntity.badRequest().build();
        }
        try {
            final boolean PAGEABLE_PARAM_FLAG = true;
            List<PlainReadModel> result = service.findAll(page).toList();
            CollectionModel<?> categoryTypeCollection = service.prepareReadEmptyCategoryTypesHateoas(result, YEAR_PARAM, MONTH_PARAM, PAGEABLE_PARAM_FLAG);

            logger.info("exposing all categories!");
            return ResponseEntity.ok(categoryTypeCollection);
        } catch (NullPointerException e) {
            logger.info("no categories");
            return ResponseEntity.noContent().build();
        }
    }
    @ResponseBody
    @GetMapping(value = "/{id}", params = {"!sort","!size","!page"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readOneCategoryTypeContent(@PathVariable Integer id,
                                                                        @RequestParam(value = "year") String YEAR_PARAM,
                                                                        @RequestParam(value = "month") String MONTH_PARAM) {

//        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
//            logger.info("no relation between given year and month, can't continue");
//            return ResponseEntity.badRequest().build();
//        }
        try {
            final boolean PAGEABLE_PARAM_FLAG = false;
            List<Process> result = service.findAllProcessesBelongToCategory(id);
            String category = service.findById(id).getType();
            CollectionModel<?> processCollection = service.prepareReadOneCategoryTypeContentHateoas(result, YEAR_PARAM, MONTH_PARAM, category, PAGEABLE_PARAM_FLAG);

            logger.info("exposing '"+ category + "' category content");
            return ResponseEntity.ok(processCollection);
        } catch (NotFoundException e) {
            logger.info("no processes found for given category");
            return ResponseEntity.notFound().build();
        }
    }
    @ResponseBody
    @GetMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readOneCategoryTypeContent(Pageable page,
                                                 @PathVariable Integer id,
                                                 @RequestParam(value = "year") String YEAR_PARAM,
                                                 @RequestParam(value = "month") String MONTH_PARAM) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("no relation between given year and month, can't continue");
            return ResponseEntity.badRequest().build();
        }
        try {
            final boolean PAGEABLE_PARAM_FLAG = true;
            List<Process> result = service.findAllProcessesBelongToCategory(page, id).toList();
            String category = service.findById(id).getType();
            CollectionModel<?> processCollection = service.prepareReadOneCategoryTypeContentHateoas(result, YEAR_PARAM, MONTH_PARAM, category, PAGEABLE_PARAM_FLAG);

            logger.info("exposing '"+ category + "' category content");
            return ResponseEntity.ok(processCollection);
        } catch (NotFoundException e) {
            logger.info("no processes found for given category");
            return ResponseEntity.notFound().build();
        }
    }
    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> fullUpdateCategoryType(@PathVariable Integer id,
                                                  @RequestParam(value = "year") String YEAR_PARAM,
                                                  @RequestParam(value = "month") String MONTH_PARAM,
                                                  @RequestBody @Valid CategoryType toUpdate) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("no relation between given year and month, can't continue");
            return ResponseEntity.badRequest().build();
        }
        try {
            CategoryType category = service.findById(id);
            category.fullUpdate(toUpdate);
            service.save(category);
            logger.info("put category type with id = "+ id);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.info("an error occured while put category type");
            return ResponseEntity.badRequest().build();
        }
    }
    @Transactional
    @ResponseBody
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> partUpdateCategoryType(@PathVariable Integer id,
                                                         @RequestParam(value = "year") String YEAR_PARAM,
                                                         @RequestParam(value = "month") String MONTH_PARAM,
                                                         @Valid HttpServletRequest request) {

        if(!service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM)) {
            logger.info("no relation between given year and month, can't continue");
            return ResponseEntity.badRequest().build();
        }
        try {
            CategoryType category = service.findById(id);
            CategoryType updatedCategoryType = objectMapper
                    .readerForUpdating(category)
                    .readValue(request.getReader());

            service.saveAndFlush(updatedCategoryType);
            logger.info("succesfully patched category type nr "+id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.info("an error occured while patching category type");
            return ResponseEntity.badRequest().build();
        }
    }
    @Transactional
    @ResponseBody
    @DeleteMapping@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> deleteCategoryType(@PathVariable Integer id,
                                              @RequestParam(value = "year") String YEAR_PARAM,
                                              @RequestParam(value = "month") String MONTH_PARAM) {
        if(!(service.categoryTypeLevelValidationSuccess(YEAR_PARAM,MONTH_PARAM) && service.existsById(id))) {
            logger.info("no relation between given year month and category id, can't continue");
            return ResponseEntity.badRequest().build();
        }
        service.deleteCategoryType(id);
        logger.warn("deleted category type with id = " +id);
        return ResponseEntity.ok().build();
    }
    /**
     * Templates
     */
}

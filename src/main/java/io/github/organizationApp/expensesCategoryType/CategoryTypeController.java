package io.github.organizationApp.expensesCategoryType;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.organizationApp.expensesProcess.ProcessService;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@GeneralExceptionsProcessing
@RequestMapping("/category")
class CategoryTypeController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryTypeController.class);
    private final CategoryTypeService service;
    private final ProcessService processService;
    private final ObjectMapper objectMapper;

    CategoryTypeController(final CategoryTypeService service, final ProcessService processService, final ObjectMapper objectMapper) {
        this.service = service;
        this.processService = processService;
        this.objectMapper = objectMapper;
    }

    /**
     * API
     */
    @Transactional
    @ResponseBody
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CategoryType> addCategoryType(@RequestBody @Valid CategoryType toCategory) {

        try {
            CategoryType result = service.save(toCategory);
            logger.info("posted new category type with id = "+result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        } catch (Exception e) {
            logger.warn("an error occured while posting category type");
            return ResponseEntity.notFound().build();
        }
    }
    @Timed(value = "controller.categoryType.readCategoryTypes",histogram = true,percentiles = {0.5,0.95,0.99})
    @ResponseBody
    @GetMapping(params = {"!sort","!size","!page"})
    ResponseEntity<CollectionModel<CategoryType>> readCategoryTypes() {
        try {
            List<CategoryType> result = service.findAll();
            CollectionModel<CategoryType> categoryTypeCollection = service.addEachCategoryTypeLink(result);
            logger.info("exposing all categories!");
            return ResponseEntity.ok(categoryTypeCollection);
        } catch (Exception e) {
            logger.info("no categories");
            return ResponseEntity.noContent().build();
        }
    }
    @Timed(value = "controller.categoryType.readCategoryTypes(+Pageable param)",histogram = true,percentiles = {0.5,0.95,0.99})
    @ResponseBody
    @GetMapping
    ResponseEntity<CollectionModel<PagedModel<CategoryType>>> readCategoryTypes(Pageable page) {
        try {
            Page<CategoryType> result = service.findAll(page);
            CollectionModel<PagedModel<CategoryType>> categoryTypeCollection = service.addEachCategoryTypeLink(result);
            logger.info("exposing all categories!");
            return ResponseEntity.ok(categoryTypeCollection);
        } catch (NullPointerException e){
            logger.info("no categories");
            return ResponseEntity.noContent().build();
        }
    }
    @ResponseBody
    @GetMapping(value = "/{id}")
    ResponseEntity<EntityModel<CategoryType>> readCategoryType(@PathVariable Integer id) {
        return service.findById(id)
                .map(category -> {
                    Link link1 = linkTo(CategoryTypeController.class).slash(id).withSelfRel();
                    EntityModel<CategoryType> categoryTypeModel = new EntityModel(category,link1);
                    logger.info("exposing category type with id = " +id);
                    return ResponseEntity.ok(categoryTypeModel);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> fullUpdateCategoryType(@PathVariable Integer id, @RequestBody @Valid CategoryType toUpdate){
        return service.findById(id)
                .map(category -> {
                    category.fullUpdate(toUpdate);
                    service.save(category);
                    logger.info("put category type with id = "+ id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @Transactional
    @ResponseBody
    @PatchMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> partUpdateCategoryType(@PathVariable Integer id, @Valid HttpServletRequest request) {
        return service.findById(id)
                .map(category -> {
                    try {
                        CategoryType updatedCategoryType = objectMapper.readerForUpdating(category).readValue(request.getReader());
                        service.saveAndFlush(updatedCategoryType);
                        logger.info("succesfully patched category type nr "+id);
                        return ResponseEntity.noContent().build();
                    } catch (Exception e) {
                        logger.warn("bad request!");
                        return ResponseEntity.badRequest().build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @Transactional
    @ResponseBody
    @DeleteMapping(value = "/{id}")
    ResponseEntity<Object> deleteCategoryType(@PathVariable Integer id) {
        return service.findById(id)
                .map(category -> {
                    service.deleteCategoryType(id);
                    logger.warn("deleted category type with id = " +id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Templates
     */
}

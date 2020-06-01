package io.github.organizationApp.monthExpenses;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.organizationApp.expensesCategoryType.CategoryTypeService;
import io.github.organizationApp.expensesProcess.ProcessService;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
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
@RequestMapping("/month")
class MonthExpensesController {
    private static final Logger logger = LoggerFactory.getLogger(MonthExpensesController.class);
    private final MonthExpensesService service;
    private final CategoryTypeService CategoryTypeService;
    private final ProcessService processService;
    private final ObjectMapper objectMapper;

    MonthExpensesController(final MonthExpensesService service,
                            final CategoryTypeService categoryTypeService,
                            final ProcessService processService,
                            final ObjectMapper objectMapper)
    {
        this.service = service;
        this.CategoryTypeService = categoryTypeService;
        this.processService = processService;
        this.objectMapper = objectMapper;
    }

    /**
     * JSON:API
     */
    @Transactional
    @ResponseBody
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MonthExpenses> addMonth(@RequestBody @Valid MonthExpenses toMonthExpenses) {

        try {
            MonthExpenses result = service.save(toMonthExpenses);
            logger.info("posted new month with id = "+result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
        } catch (Exception e) {
            logger.warn("an error occured while posting new month");
            return ResponseEntity.notFound().build();
        }
    }
    @ResponseBody
    @GetMapping(params = {"!sort","!size","!page"},consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CollectionModel<MonthExpenses>> readMonths() {
        try {
            List<MonthExpenses> result = service.findAll();
            CollectionModel<MonthExpenses> monthsCollection = service.addEachMonthLink(result);
            logger.info("exposing all months!");
            return ResponseEntity.ok(monthsCollection);
        } catch (Exception e) {
            logger.info("an error while loading months occured");
            return ResponseEntity.noContent().build();
        }
    }
    @ResponseBody
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CollectionModel<PagedModel<MonthExpenses>>> readMonths(Pageable page) {
        try {
            Page<MonthExpenses> result = service.findAll(page);
            CollectionModel<PagedModel<MonthExpenses>> monthsCollection = service.addEachMonthLink(result);
            logger.info("exposing all months!");
            return ResponseEntity.ok(monthsCollection);
        } catch (Exception e) {
            logger.info("an error while loading months occured");
            return ResponseEntity.noContent().build();
        }
    }
    @ResponseBody
    @GetMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<MonthExpenses>> readMonth(@PathVariable Integer id) {
        return service.findById(id)
                .map(month -> {
                    Link link1 = linkTo(MonthExpensesController.class).slash(id).withSelfRel();
                    EntityModel<MonthExpenses> monthModel = new EntityModel(month,link1);
                    logger.info("exposing month with id = " +id);
                    return ResponseEntity.ok(monthModel);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> fullUpdateMonth(@PathVariable Integer id, @RequestBody @Valid MonthExpenses toUpdate){
        return service.findById(id)
                .map(month -> {
                    month.fullUpdate(toUpdate);
                    service.save(month);
                    logger.info("put month with id = "+ id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @Transactional
    @ResponseBody
    @PatchMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> partUpdateMonth(@PathVariable Integer id, @Valid HttpServletRequest request) {
        return service.findById(id)
                .map(month -> {
                    try {
                        MonthExpenses updatedMonth = objectMapper.readerForUpdating(month).readValue(request.getReader());
                        service.saveAndFlush(updatedMonth);
                        logger.info("succesfully patched month nr "+id);
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
    @DeleteMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> deleteMonth(@PathVariable Integer id) {
        return service.findById(id)
                .map(month -> {
                    service.deleteMonth(id);
                    logger.warn("deleted month with id = " +id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Templates
     */
}

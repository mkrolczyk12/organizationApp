//package io.github.organizationApp.yearExpenses;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.github.organizationApp.categoryExpenses.CategoryTypeService;
//import io.github.organizationApp.expensesProcess.ProcessService;
//import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
//import io.github.organizationApp.monthExpenses.MonthExpensesService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.hateoas.CollectionModel;
//import org.springframework.hateoas.EntityModel;
//import org.springframework.hateoas.Link;
//import org.springframework.hateoas.PagedModel;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.validation.Valid;
//import java.net.URI;
//import java.util.List;
//
//import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
//
//@Controller
//@GeneralExceptionsProcessing
//@RequestMapping("/year")
//class YearExpensesController {
//    private static final Logger logger = LoggerFactory.getLogger(YearExpensesController.class);
//    private final YearExpensesService service;
//    private final MonthExpensesService MonthExpensesService;
//    private final CategoryTypeService CategoryTypeService;
//    private final ProcessService processService;
//    private final ObjectMapper objectMapper;
//
//    YearExpensesController(final YearExpensesService service,
//                           final MonthExpensesService monthExpensesService,
//                           final CategoryTypeService categoryTypeService,
//                           final ProcessService processService,
//                           final ObjectMapper objectMapper)
//    {
//        this.service = service;
//        this.MonthExpensesService = monthExpensesService;
//        this.CategoryTypeService = categoryTypeService;
//        this.processService = processService;
//        this.objectMapper = objectMapper;
//    }
//
//    /**
//     * JSON:API
//     */
//    @Transactional
//    @ResponseBody
//    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
//    ResponseEntity<YearExpenses> addYear(@RequestBody @Valid YearExpenses toYearExpenses) {
//        try {
//            YearExpenses result = service.save(toYearExpenses);
//            logger.info("posted new year with id = "+result.getId());
//            return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
//        } catch (Exception e) {
//            logger.warn("an error occured while posting new year");
//            return ResponseEntity.notFound().build();
//        }
//    }
//    @ResponseBody
//    @GetMapping(params = {"!sort","!size","!page"},consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
//    ResponseEntity<CollectionModel<YearExpenses>> readYears() {
//        try {
//            List<YearExpenses> result = service.findAll();
//            CollectionModel<YearExpenses> yearsCollection = service.addEachYearLink(result);
//            logger.info("exposing all years!");
//            return ResponseEntity.ok(yearsCollection);
//        } catch (Exception e) {
//            logger.info("an error while loading years occured");
//            return ResponseEntity.noContent().build();
//        }
//    }
//    @ResponseBody
//    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
//    ResponseEntity<CollectionModel<PagedModel<YearExpenses>>> readYears(Pageable page) {
//        try {
//            Page<YearExpenses> result = service.findAll(page);
//            CollectionModel<PagedModel<YearExpenses>> yearsCollection = service.addEachYearLink(result);
//            logger.info("exposing all years!");
//            return ResponseEntity.ok(yearsCollection);
//        } catch (Exception e) {
//            logger.info("an error while loading years occured");
//            return ResponseEntity.noContent().build();
//        }
//    }
//    @ResponseBody
//    @GetMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
//    ResponseEntity<EntityModel<YearExpenses>> readYear(@PathVariable Integer id) {
//        return service.findById(id)
//                .map(year -> {
//                    Link link1 = linkTo(YearExpensesController.class).slash(id).withSelfRel();
//                    EntityModel<YearExpenses> yearModel = new EntityModel(year,link1);
//                    logger.info("exposing year with id = " +id);
//                    return ResponseEntity.ok(yearModel);
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
//    @Transactional
//    @ResponseBody
//    @PutMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
//    ResponseEntity<Object> fullUpdateYear(@PathVariable Integer id, @RequestBody @Valid YearExpenses toUpdate){
//        return service.findById(id)
//                .map(year -> {
//                    year.fullUpdate(toUpdate);
//                    service.save(year);
//                    logger.info("put year with id = "+ id);
//                    return ResponseEntity.ok().build();
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
//    @Transactional
//    @ResponseBody
//    @PatchMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<Object> partUpdateYear(@PathVariable Integer id, @Valid HttpServletRequest request) {
//        return service.findById(id)
//                .map(year -> {
//                    try {
//                        YearExpenses updatedYear = objectMapper.readerForUpdating(year).readValue(request.getReader());
//                        service.saveAndFlush(updatedYear);
//                        logger.info("succesfully patched year nr "+id);
//                        return ResponseEntity.noContent().build();
//                    } catch (Exception e) {
//                        logger.warn("bad request!");
//                        return ResponseEntity.badRequest().build();
//                    }
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
//    @Transactional
//    @ResponseBody
//    @DeleteMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
//    ResponseEntity<Object> deleteYear(@PathVariable Integer id) {
//        return service.findById(id)
//                .map(year -> {
//                    service.deleteYear(id);
//                    logger.warn("deleted year with id = " +id);
//                    return ResponseEntity.ok().build();
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    /**
//     * Templates
//     */
//}

package io.github.organizationApp.expensesProcess;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.organizationApp.categoryExpenses.CategoryTypeController;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Controller
@GeneralExceptionsProcessing
@RequestMapping("/process")
public class ProcessController {
    private static final Logger logger = LoggerFactory.getLogger(ProcessController.class);
    private final ProcessService service;
    private final ObjectMapper objectMapper;

    ProcessController(final ProcessService service, final ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    /**
     * JSON:API
     */
    @Timed(value = "controller.process.readProcesses", histogram = true, percentiles = {0.5,0.95,0.99})
    @ResponseBody
    @GetMapping(params = {"all","!sort","!size","!page"},consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readProcesses(@RequestParam(value = "all") final String ALL_PARAM) {

        final boolean PAGEABLE_PARAM_FLAG = false;
        try {
            logger.info("starting process async finding");
            CompletableFuture<List<Process>> processes = service.findAllAsync();
            List<Process> result = processes.get();


            CollectionModel<?> processCollection = service.addEachProcessLink(result, PAGEABLE_PARAM_FLAG);
            logger.warn("exposing all processes!");
            return ResponseEntity.ok(processCollection);
        } catch (ExecutionException | InterruptedException e) {
            logger.info("Async finding failed, switched to normal finding");
            List<Process> result = service.findAll();

            CollectionModel<?> processCollection = service.addEachProcessLink(result, PAGEABLE_PARAM_FLAG);
            return ResponseEntity.ok(processCollection);
        }
    }
    @Timed(value = "controller.process.readProcesses(+Pageable param)", histogram = true, percentiles = {0.5,0.95,0.99})
    @ResponseBody
    @GetMapping(params = "all", consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readProcesses(Pageable page,
                                    @RequestParam(value = "all") final String ALL_PARAM) {

        final boolean PAGEABLE_PARAM_FLAG = true;
        try {
            logger.info("starting process async finding");
            CompletableFuture<Page<Process>> processes = service.findAllAsync(page);
            List<Process> result = processes.get().toList();


            CollectionModel<?> processCollection = service.addEachProcessLink(result, PAGEABLE_PARAM_FLAG);
            logger.warn("exposing all processes!");
            return ResponseEntity.ok(processCollection);
        } catch (ExecutionException | InterruptedException e) {
            logger.info("Async finding failed, switched to normal finding");
            List<Process> result = service.findAll(page).toList();

            CollectionModel<?> processCollection = service.addEachProcessLink(result, PAGEABLE_PARAM_FLAG);
            return ResponseEntity.ok(processCollection);
        }
    }
    @ResponseBody
    @GetMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<Process>> readProcess(@PathVariable final Long id,
                                                            @RequestParam(value = "year") final String YEAR_PARAM,
                                                            @RequestParam(value = "month") final String MONTH_PARAM,
                                                            @RequestParam(value = "category") final String CATEGORY_PARAM) {

        if(!service.processLevelValidationSuccess(YEAR_PARAM, MONTH_PARAM, CATEGORY_PARAM)) {
            logger.info("process level validation failed, no relation between given year, month and category");
            return ResponseEntity.badRequest().build();
        }

        return service.findById(id)
                .map(process -> {
                    process.add(linkTo(methodOn(ProcessController.class).readProcess(id, YEAR_PARAM, MONTH_PARAM, CATEGORY_PARAM)).withRel("allowed_queries: GET,PUT,PATCH,DELETE"));
                    Link href1 = linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(process.getCategory().getId(), YEAR_PARAM, MONTH_PARAM)).withRel("chosen_category_processes");
                    Link href2 = linkTo(methodOn(CategoryTypeController.class).readOneCategoryTypeContent(process.getCategory().getId(), YEAR_PARAM, MONTH_PARAM)).withRel("chosen_category_processes?{sort,size,page}");
                    Link href3 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(YEAR_PARAM, MONTH_PARAM)).withRel("categories");
                    Link href4 = linkTo(methodOn(CategoryTypeController.class).readEmptyCategoryTypes(YEAR_PARAM, MONTH_PARAM)).withRel("categories?{sort,size,page}");

                    EntityModel<Process> processModel = new EntityModel(process,href1, href2, href3, href4);
                    logger.info("exposing process with id = " + id);
                    return ResponseEntity.ok(processModel);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> fullUpdateProcess(@PathVariable final Long id,
                                             @RequestParam(value = "year") final String YEAR_PARAM,
                                             @RequestParam(value = "month") final String MONTH_PARAM,
                                             @RequestParam(value = "category") final String CATEGORY_PARAM,
                                             @RequestBody @Valid final Process toUpdate){

        if(!service.processLevelValidationSuccess(YEAR_PARAM, MONTH_PARAM, CATEGORY_PARAM)) {
            logger.info("process level validation failed, no relation between given year, month and category");
            return ResponseEntity.badRequest().build();
        }
        return service.findById(id)
                .map(process -> {
                    process.fullUpdate(toUpdate);
                    service.save(process);
                    logger.info("put process with id = "+ id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @Transactional
    @ResponseBody
    @PatchMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> partUpdateProcess(@PathVariable final Long id,
                                                    @RequestParam(value = "year") final String YEAR_PARAM,
                                                    @RequestParam(value = "month") final String MONTH_PARAM,
                                                    @RequestParam(value = "category") final String CATEGORY_PARAM,
                                                    @Valid final HttpServletRequest request) {

        if(!service.processLevelValidationSuccess(YEAR_PARAM, MONTH_PARAM, CATEGORY_PARAM)) {
            logger.info("process level validation failed, no relation between given year, month and category");
            return ResponseEntity.badRequest().build();
        }
        return service.findById(id)
                .map(process -> {
                    try {
                        Process updatedProcess = objectMapper.readerForUpdating(process).readValue(request.getReader());
                        service.saveAndFlush(updatedProcess);
                        logger.info("succesfully patched process nr "+id);
                        return ResponseEntity.noContent().build();
                    } catch (IOException e) {
                        logger.warn("bad request!");
                        return ResponseEntity.badRequest().build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @Transactional
    @ResponseBody
    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> deleteProcess(@PathVariable final Long id,
                                         @RequestParam(value = "year") final String YEAR_PARAM,
                                         @RequestParam(value = "month") final String MONTH_PARAM,
                                         @RequestParam(value = "category") final String CATEGORY_PARAM) {

        if(!service.processLevelValidationSuccess(YEAR_PARAM, MONTH_PARAM, CATEGORY_PARAM)) {
            logger.info("process level validation failed, no relation between given year, month and category");
            return ResponseEntity.badRequest().build();
        }
        return service.findById(id)
                .map(process -> {
                    service.deleteProcess(id);
                    logger.warn("deleted process with id = " +id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Templates
     */
}


package io.github.organizationApp.expensesProcess;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Controller
@GeneralExceptionsProcessing
@RequestMapping("/process")
class ProcessController {
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
    @Transactional
    @ResponseBody
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Process> addProcess(@RequestBody @Valid Process toProcess) {
        Process result = service.save(toProcess);
        logger.info("posted new process with id = "+result.getId());
        return ResponseEntity.created(URI.create("/" + result.getId())).body(result);
    }
    @Timed(value = "controller.process.readProcesses",histogram = true,percentiles = {0.5,0.95,0.99})
    @ResponseBody
    @GetMapping(params = {"!sort","!size","!page"},consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CollectionModel<Process>> readProcesses() {
        try {
            logger.info("starting process async finding");
            CompletableFuture<List<Process>> processes = service.findAllAsync();
            List<Process> result = processes.get();

            CollectionModel<Process> processCollection = service.addEachProcessLink(result);
            logger.warn("exposing all processes!");
            return ResponseEntity.ok(processCollection);
        } catch (ExecutionException | InterruptedException e) {
            logger.info("Async finding failed, switched to normal finding");
            List<Process> result = service.findAll();

            CollectionModel<Process> processCollection = service.addEachProcessLink(result);
            return ResponseEntity.ok(processCollection);
        }
    }
    @Timed(value = "controller.process.readProcesses(+Pageable param)",histogram = true,percentiles = {0.5,0.95,0.99})
    @ResponseBody
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CollectionModel<PagedModel<Process>>> readProcesses(Pageable page) {
        try {
            logger.info("starting process async finding");
            CompletableFuture<Page<Process>> processes = service.findAllAsync(page);
            Page<Process> result = processes.get();

            CollectionModel<PagedModel<Process>> processCollection = service.addEachProcessLink(result);
            logger.warn("exposing all processes!");
            return ResponseEntity.ok(processCollection);
        } catch (ExecutionException | InterruptedException e) {
            logger.info("Async finding failed, switched to normal finding");
            Page<Process> result = service.findAll(page);

            CollectionModel<PagedModel<Process>> processCollection = service.addEachProcessLink(result);
            return ResponseEntity.ok(processCollection);
        }
    }
    @ResponseBody
    @GetMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<Process>> readProcess(@PathVariable Long id) {
        return service.findById(id)
                .map(process -> {
                    Link link1 = linkTo(ProcessController.class).slash(id).withSelfRel();
                    EntityModel<Process> processModel = new EntityModel(process,link1);
                    logger.info("exposing process with id = " +id);
                    return ResponseEntity.ok(processModel);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> fullUpdateProcess(@PathVariable Long id, @RequestBody @Valid Process toUpdate){
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
    public ResponseEntity<Object> partUpdateProcess(@PathVariable Long id, @Valid HttpServletRequest request) {
        return service.findById(id)
                .map(process -> {
                    try {
                        Process updatedProcess = objectMapper.readerForUpdating(process).readValue(request.getReader());
                        service.saveAndFlush(updatedProcess);
                        logger.info("succesfully patched process nr "+id);
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
    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> deleteProcess(@PathVariable Long id) {
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


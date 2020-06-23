package io.github.organizationApp.expensesProcess;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.organizationApp.globalControllerAdvice.ExceptionResponse;
import io.github.organizationApp.globalControllerAdvice.GeneralExceptionsProcessing;
import io.github.organizationApp.security.SecurityExceptionsProcessing;
import io.github.organizationApp.security.User;
import io.micrometer.core.annotation.Timed;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Controller
@GeneralExceptionsProcessing
@ProcessExceptionsProcessing
@SecurityExceptionsProcessing
@RequestMapping("/moneyapp/processes")
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
    @GetMapping(params = {"all","!sort","!size","!page"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> readProcesses() throws NotFoundException, ExecutionException, InterruptedException {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = false;

        logger.info("starting process async finding");
        CompletableFuture<List<Process>> processes = service.findAllAsync(USER_ID);

        logger.info("exposing all processes!");
        CollectionModel<?> processCollection = service.addEachProcessLink(processes, PAGEABLE_PARAM_FLAG);

        return ResponseEntity.ok(processCollection);
    }
    @Timed(value = "controller.process.readProcesses(+Pageable param)", histogram = true, percentiles = {0.5,0.95,0.99})
    @ResponseBody
    @GetMapping(params = "all", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> readProcesses(Pageable page) throws NotFoundException, ExecutionException, InterruptedException {

        final String USER_ID = User.getUserId();
        final boolean PAGEABLE_PARAM_FLAG = true;

        logger.info("starting process async finding");
        CompletableFuture<List<Process>> processes = service.findAllAsync(page, USER_ID);
        logger.info("exposing all processes!");
        CollectionModel<?> processCollection = service.addEachProcessLink(processes, PAGEABLE_PARAM_FLAG);

        return ResponseEntity.ok(processCollection);
    }
    @ResponseBody
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<Process>> readProcess(@PathVariable final Long id,
                                                            @RequestParam(value = "year") final short YEAR_PARAM,
                                                            @RequestParam(value = "month") final String MONTH_PARAM,
                                                            @RequestParam(value = "category") final String CATEGORY_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.processLevelValidationSuccess(YEAR_PARAM, MONTH_PARAM, CATEGORY_PARAM, USER_ID)) {
            final String message = "process level validation failed, no relation between given year, month and category";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        Process process = service.findById(id, USER_ID);
        EntityModel<Process> processModel = service.prepareProcessHateoas(process, id, YEAR_PARAM, MONTH_PARAM, CATEGORY_PARAM);

        logger.info("exposing process with id = " + id);
        return ResponseEntity.ok(processModel);
    }
    @Transactional
    @ResponseBody
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> fullUpdateProcess(@PathVariable final Long id,
                                             @RequestParam(value = "year") final short YEAR_PARAM,
                                             @RequestParam(value = "month") final String MONTH_PARAM,
                                             @RequestParam(value = "category") final String CATEGORY_PARAM,
                                             @RequestBody @Valid final Process toUpdate) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.processLevelValidationSuccess(YEAR_PARAM, MONTH_PARAM, CATEGORY_PARAM, USER_ID)) {
            final String message = "process level validation failed, no relation between given year, month and category";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        Process process = service.findById(id, USER_ID);
        process.fullUpdate(toUpdate);

        service.save(process);
        logger.info("put process with id = "+ id);
        return ResponseEntity.ok().build();
    }
    @Transactional
    @ResponseBody
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> partUpdateProcess(@PathVariable final Long id,
                                                    @RequestParam(value = "year") final short YEAR_PARAM,
                                                    @RequestParam(value = "month") final String MONTH_PARAM,
                                                    @RequestParam(value = "category") final String CATEGORY_PARAM,
                                                    @Valid final HttpServletRequest request) throws IOException, NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.processLevelValidationSuccess(YEAR_PARAM, MONTH_PARAM, CATEGORY_PARAM, USER_ID)) {
            final String message = "process level validation failed, no relation between given year, month and category";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        Process process = service.findById(id, USER_ID);
        Process updatedProcess = objectMapper.readerForUpdating(process).readValue(request.getReader());

        service.saveAndFlush(updatedProcess);
        logger.info("successfully patched process nr "+id);
        return ResponseEntity.noContent().build();
    }
    @Transactional
    @ResponseBody
    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> deleteProcess(@PathVariable final Long id,
                                         @RequestParam(value = "year") final short YEAR_PARAM,
                                         @RequestParam(value = "month") final String MONTH_PARAM,
                                         @RequestParam(value = "category") final String CATEGORY_PARAM) throws NotFoundException {

        final String USER_ID = User.getUserId();
        if(!service.processLevelValidationSuccess(YEAR_PARAM, MONTH_PARAM, CATEGORY_PARAM, USER_ID)) {
            final String message = "process level validation failed, no relation between given year, month and category";
            logger.info(message);
            ExceptionResponse response = new ExceptionResponse(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), message, "-");

            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        if(service.existsByIdAndOwnerId(id, USER_ID))
            service.deleteProcess(id);
        else throw new IllegalArgumentException("the given process id is incorrect");

        logger.warn("deleted process with id = " +id);
        return ResponseEntity.ok().build();
    }
    /**
     * Templates
     */
}


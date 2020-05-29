package io.github.organizationApp.expensesProcess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Service
public class ProcessService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);
    private final ProcessRepository repository;

    ProcessService(final ProcessRepository repository) {
        this.repository = repository;
    }

    Process save(Process entity) {
        return repository.save(entity);
    }

    @Async
    public CompletableFuture<List<Process>> findAllAsync() {
        return CompletableFuture.supplyAsync(() -> repository.findAll());
    }

    @Async
    CompletableFuture<Page<Process>> findAllAsync(Pageable page) {
        return CompletableFuture.supplyAsync(() -> repository.findAll(page));
    }

    public List<Process> findAll() {
        return repository.findAll();
    }

    Page<Process> findAll(Pageable page) {
        return repository.findAll(page);
    }

    Optional<Process> findById(Long id) {
        return repository.findById(id);
    }

    boolean existsById(Long id) {
        return repository.existsById(id);
    }

    Process saveAndFlush(Process process) {
        return repository.saveAndFlush(process);
    }

    void deleteProcess(Long id) {
        repository.deleteById(id);
    }

    CollectionModel<Process> addEachProcessLink(final List<Process> result) {
        result.forEach(process -> process.add(linkTo(ProcessController.class).slash(process.getId()).withSelfRel()));
        Link link1 = linkTo(ProcessController.class).withSelfRel();
        Link link2 = linkTo(ProcessController.class).withRel("?{sort,size,page}");
        CollectionModel<Process> processCollection = new CollectionModel<>(result, link1,link2);

        return processCollection;
    }

    CollectionModel<PagedModel<Process>> addEachProcessLink(final Page<Process> result) {
        result.forEach(process -> process.add(linkTo(ProcessController.class).slash(process.getId()).withSelfRel()));
        Link link1 = linkTo(ProcessController.class).withSelfRel();
        Link link2 = linkTo(ProcessController.class).withRel("?{sort,size,page}");
        CollectionModel<PagedModel<Process>> processCollection = new CollectionModel(result, link1,link2);

        return processCollection;
    }
}

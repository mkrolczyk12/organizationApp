package io.github.organizationApp.yearExpenses;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Service
public class YearExpensesService {
    private static final Logger logger = LoggerFactory.getLogger(YearExpensesService.class);
    private final YearExpensesRepository repository;

    YearExpensesService(final YearExpensesRepository repository) {
        this.repository = repository;
    }

    /**
     * Create
     */
    YearExpenses save(final YearExpenses year) {return repository.save(year);}
    /**
     * Read
     */
    List<YearExpenses> findAll() {
        return repository.findAll();
    }

    Page<YearExpenses> findAll(Pageable page) {
        return repository.findAll(page);
    }

    Optional<YearExpenses> findById(final Integer id) {return repository.findById(id);}

    boolean existsByYear(String year) {
        return repository.existsByYear(year);
    }
    /**
     * Update
     */
    YearExpenses saveAndFlush(final YearExpenses updatedYear) {
        return repository.saveAndFlush(updatedYear);
    }

    CollectionModel<YearExpenses> addEachYearLink(final List<YearExpenses> result) {
        result.forEach(year -> year.add(linkTo(YearExpensesController.class).slash(year.getId()).withSelfRel()));
        Link link1 = linkTo(YearExpensesController.class).withSelfRel();
        Link link2 = linkTo(YearExpensesController.class).withRel("?{sort,size,page}");
        CollectionModel<YearExpenses> YearCollection = new CollectionModel(result, link1,link2);

        return YearCollection;
    }

    CollectionModel<PagedModel<YearExpenses>> addEachYearLink(final Page<YearExpenses> result) {
        result.forEach(year -> year.add(linkTo(YearExpensesController.class).slash(year.getId()).withSelfRel()));
        Link link1 = linkTo(YearExpensesController.class).withSelfRel();
        Link link2 = linkTo(YearExpensesController.class).withRel("?{sort,size,page}");
        CollectionModel<PagedModel<YearExpenses>> YearCollection = new CollectionModel(result, link1,link2);

        return YearCollection;
    }
    /**
     * Delete
     */
    void deleteYear(final Integer id) {
        repository.deleteById(id);
    }
}

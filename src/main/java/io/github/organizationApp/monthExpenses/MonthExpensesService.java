package io.github.organizationApp.monthExpenses;

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
public class MonthExpensesService {
    private static final Logger logger = LoggerFactory.getLogger(MonthExpensesService.class);
    private final MonthExpensesRepository repository;

    MonthExpensesService(final MonthExpensesRepository repository) {
        this.repository = repository;
    }

    /**
     * Create
     */
    MonthExpenses save(final MonthExpenses month) {return repository.save(month);}
    /**
     * Read
     */
    List<MonthExpenses> findAll() {
        return repository.findAll();
    }

    Page<MonthExpenses> findAll(Pageable page) {
        return repository.findAll(page);
    }

    Optional<MonthExpenses> findById(final Integer id) {
        return repository.findById(id);
    }

    boolean existsByMonth(String month) {
        return repository.existsByMonth(month);
    }
    /**
     * Update
     */
    MonthExpenses saveAndFlush(final MonthExpenses updatedMonth) {
        return repository.saveAndFlush(updatedMonth);
    }

    CollectionModel<MonthExpenses> addEachMonthLink(final List<MonthExpenses> result) {
        result.forEach(month -> month.add(linkTo(MonthExpensesController.class).slash(month.getId()).withSelfRel()));
        Link link1 = linkTo(MonthExpensesController.class).withSelfRel();
        Link link2 = linkTo(MonthExpensesController.class).withRel("?{sort,size,page}");
        CollectionModel<MonthExpenses> MonthCollection = new CollectionModel(result, link1,link2);

        return MonthCollection;
    }

    CollectionModel<PagedModel<MonthExpenses>> addEachMonthLink(final Page<MonthExpenses> result) {
        result.forEach(month -> month.add(linkTo(MonthExpensesController.class).slash(month.getId()).withSelfRel()));
        Link link1 = linkTo(MonthExpensesController.class).withSelfRel();
        Link link2 = linkTo(MonthExpensesController.class).withRel("?{sort,size,page}");
        CollectionModel<PagedModel<MonthExpenses>> MonthCollection = new CollectionModel(result, link1,link2);

        return MonthCollection;
    }
    /**
     * Delete
     */
    void deleteMonth(final Integer id) {
        repository.deleteById(id);
    }
}

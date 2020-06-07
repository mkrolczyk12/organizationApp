//package io.github.organizationApp.monthExpenses;
//
//import io.github.organizationApp.categoryExpenses.CategoryTypeRepository;
//import io.github.organizationApp.expensesProcess.ProcessRepository;
//import io.github.organizationApp.yearExpenses.YearExpenses;
//import io.github.organizationApp.yearExpenses.YearExpensesRepository;
//import javassist.NotFoundException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.hateoas.CollectionModel;
//import org.springframework.hateoas.Link;
//import org.springframework.hateoas.PagedModel;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
//
//@Service
//class MonthExpensesService {
//    private static final Logger logger = LoggerFactory.getLogger(MonthExpensesService.class);
//    private final YearExpensesRepository yearRepository;
//    private final MonthExpensesRepository repository;
//    private final CategoryTypeRepository categoryRepository;
//    private final ProcessRepository processesRepository;
//
//    MonthExpensesService(final YearExpensesRepository yearRepository,
//                         final MonthExpensesRepository repository,
//                         final CategoryTypeRepository categoryRepository,
//                         final ProcessRepository processesRepository) {
//
//        this.yearRepository = yearRepository;
//        this.repository = repository;
//        this.categoryRepository = categoryRepository;
//        this.processesRepository = processesRepository;
//    }
//
//    /**
//     * Create
//     */
//    public MonthExpenses save(final MonthExpenses month) {return repository.save(month);}
//    /**
//     * Read
//     */
//    List<MonthExpenses> findAll() {
//        return repository.findAll();
//    }
//
//    Page<MonthExpenses> findAll(Pageable page) {
//        return repository.findAll(page);
//    }
//
//    Optional<MonthExpenses> findById(final Integer id) {
//        return repository.findById(id);
//    }
//
//    YearExpenses findByYear(final String year) throws NotFoundException {
//        return yearRepository.findByYear(year)
//                .orElseThrow(() -> new NotFoundException("no year found"));
//    }
//
//    public MonthExpenses findByMonth(final String month) {
//        return repository.findByMonth(month);
//    }
//
////    public MonthExpenses findByMonthAndBelongingYear(final String month, Integer yearId) {
////        return repository.findByMonthAndYearId(month,yearId);
////    }
//
//    public boolean existsByMonth(String month) {
//        return repository.existsByMonth(month);
//    }
//
//    public MonthExpenses findByYearId(Integer yearId) {
//        return repository.findByYearId(yearId);
//    }
//
//    public boolean existsByYearId(Integer yearId) {
//        return repository.existsByYearId(yearId);
//    }
//
//    public boolean existsByMonthAndYearId(String month, Integer yearId) {
//        return repository.existsByMonthAndYearId(month,yearId);
//    }
//    /**
//     * Update
//     */
//    MonthExpenses saveAndFlush(final MonthExpenses updatedMonth) {
//        return repository.saveAndFlush(updatedMonth);
//    }
//
//    CollectionModel<MonthExpenses> addEachMonthLink(final List<MonthExpenses> result) {
//        result.forEach(month -> month.add(linkTo(MonthExpensesController.class).slash(month.getId()).withSelfRel()));
//        Link link1 = linkTo(MonthExpensesController.class).withSelfRel();
//        Link link2 = linkTo(MonthExpensesController.class).withRel("?{sort,size,page}");
//        CollectionModel<MonthExpenses> MonthCollection = new CollectionModel(result, link1,link2);
//
//        return MonthCollection;
//    }
//
//    CollectionModel<PagedModel<MonthExpenses>> addEachMonthLink(final Page<MonthExpenses> result) {
//        result.forEach(month -> month.add(linkTo(MonthExpensesController.class).slash(month.getId()).withSelfRel()));
//        Link link1 = linkTo(MonthExpensesController.class).withSelfRel();
//        Link link2 = linkTo(MonthExpensesController.class).withRel("?{sort,size,page}");
//        CollectionModel<PagedModel<MonthExpenses>> MonthCollection = new CollectionModel(result, link1,link2);
//
//        return MonthCollection;
//    }
//    /**
//     * Delete
//     */
//    void deleteMonth(final Integer id) {
//        repository.deleteById(id);
//    }
//
//    boolean monthLevelValidationSuccess(final String year) {
//            return yearRepository.existsByYear(year);
//    }
//
//}

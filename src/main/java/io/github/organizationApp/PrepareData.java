package io.github.organizationApp;

import io.github.organizationApp.categoryExpenses.CategoryType;
import io.github.organizationApp.categoryExpenses.CategoryTypeRepository;
import io.github.organizationApp.expensesProcess.ProcessRepository;
import io.github.organizationApp.monthExpenses.MonthExpenses;
import io.github.organizationApp.monthExpenses.MonthExpensesRepository;
import io.github.organizationApp.yearExpenses.YearExpenses;
import io.github.organizationApp.yearExpenses.YearExpensesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class PrepareData implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(PrepareData.class);
    private final YearExpensesRepository yearRepository;
    private final MonthExpensesRepository monthRepository;
    private final CategoryTypeRepository repository;
    private final ProcessRepository processesRepository;

    PrepareData(final YearExpensesRepository yearRepository,
                final MonthExpensesRepository monthRepository,
                final CategoryTypeRepository repository,
                final ProcessRepository processesRepository) {

        this.yearRepository = yearRepository;
        this.monthRepository = monthRepository;
        this.repository = repository;
        this.processesRepository = processesRepository;
    }

    /**
     * { year1, year2, year3 ... }
     */
    private HashSet<String> collectionOfExistingYears = new HashSet<>();
    /**
     * { "year" : { month1, month2, ... }, ... }
     */
    private HashMap<String,HashSet<String>> collectionOfExistingMonthsInYears = new HashMap<>();
    /**
     * { "year" : { "month" : {category1, category2, ... }, ... }, ... }
     */
    private HashMap<String,HashMap<String,HashSet<String>>> collectionOfExistingCategoriesInMonthAndYear = new HashMap<>();


    public HashSet<String> getCollectionOfExistingYears() {return collectionOfExistingYears;}

    public void updateCollectionOfExistingYears(final HashSet<String> collectionOfExistingYears) {
        this.collectionOfExistingYears = collectionOfExistingYears;
    }

    public HashMap<String, HashSet<String>> getCollectionOfExistingMonthsInYears() {return collectionOfExistingMonthsInYears;}

    public void updateCollectionOfExistingMonthsInYears(final HashMap<String, HashSet<String>> collectionOfExistingMonthsInYears) {
        this.collectionOfExistingMonthsInYears = collectionOfExistingMonthsInYears;
    }

    public HashSet<String> getCollectionOfExistingCategoriesInMonthAndYear(String year, String month) {
        String monthValidationString = month.toLowerCase();

        HashMap<String, HashSet<String>> monthResult = collectionOfExistingCategoriesInMonthAndYear.get(year);
        HashSet<String> categoriesResult = monthResult.get(monthValidationString);

        return categoriesResult;
    }

    public void updateCollectionOfExistingCategoriesInMonthAndYear(final String year, String month, HashSet<String> updated_categories) {
        HashMap<String, HashSet<String>> updatedMonth = collectionOfExistingCategoriesInMonthAndYear.get(year);
        updatedMonth.replace(month, updated_categories);

        this.collectionOfExistingCategoriesInMonthAndYear.replace(year, updatedMonth);
    }

    /**
     * prepares the current saved collection of years, months, and categories while starting application
     */
    @Transactional
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        logger.info("Loading existing application data");

        List<YearExpenses> Years = yearRepository.findAll();

        for(YearExpenses Year : Years) {
            HashSet<String> months = new HashSet<>();
            HashMap<String,HashSet<String>> categories = new HashMap<>();

            final String yearName = Year.getYear().toLowerCase();

            for(MonthExpenses Month : Year.getMonths()) {
                HashSet<String> eachMonthCategories = new HashSet<>();

                final String monthName = Month.getMonth().toLowerCase();

                for(CategoryType Category : Month.getCategories()) {

                    final String categoryName = Category.getType().toLowerCase();
                    eachMonthCategories.add(categoryName);
                }

                months.add(monthName);
                categories.put(monthName, eachMonthCategories);
            }

            collectionOfExistingYears.add(yearName);
            collectionOfExistingMonthsInYears.put(yearName, months);
            collectionOfExistingCategoriesInMonthAndYear.put(yearName, categories);
        }
        // for test
        System.out.println(collectionOfExistingYears);
        System.out.println(collectionOfExistingMonthsInYears);
        System.out.println(collectionOfExistingCategoriesInMonthAndYear);
    }
}

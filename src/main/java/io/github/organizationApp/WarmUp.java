package io.github.organizationApp;

import io.github.organizationApp.categoryExpenses.CategoryTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
class WarmUp implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(WarmUp.class);
    final List<String> DEF_CATEGORIES = Arrays.asList("Food/Drinks","Shopping","Transport","Health/Sport","Pets","Travels");
    private final CategoryTypeRepository categoryTypeRepository;

    WarmUp(final CategoryTypeRepository categoryTypeRepository) {
        this.categoryTypeRepository = categoryTypeRepository;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        logger.info("Aplication warmup after context refreshed");
    }
}

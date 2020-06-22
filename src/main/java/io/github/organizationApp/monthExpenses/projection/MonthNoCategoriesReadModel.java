package io.github.organizationApp.monthExpenses.projection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.organizationApp.monthExpenses.MonthExpenses;

@JsonIgnoreProperties({"id","categories"})
public class MonthNoCategoriesReadModel extends MonthFullReadModel {

    public MonthNoCategoriesReadModel(final MonthExpenses source) {
        super(source.getId(), source.getMonth(), source.getDescription());
    }
}

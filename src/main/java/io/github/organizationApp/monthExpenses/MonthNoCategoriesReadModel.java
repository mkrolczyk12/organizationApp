package io.github.organizationApp.monthExpenses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"id","categories"})
public class MonthNoCategoriesReadModel extends MonthFullReadModel {

    public MonthNoCategoriesReadModel(final MonthExpenses source) {
        super(source.getId(), source.getMonth(), source.getDescription());
    }
}

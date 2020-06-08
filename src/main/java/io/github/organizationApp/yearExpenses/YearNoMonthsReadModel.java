package io.github.organizationApp.yearExpenses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"id", "months"})
public class YearNoMonthsReadModel extends YearFullReadModel {

    public YearNoMonthsReadModel(final YearExpenses source) {
        super(source);
    }
}

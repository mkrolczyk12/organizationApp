package io.github.organizationApp.yearExpenses.projection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.organizationApp.yearExpenses.YearExpenses;

@JsonIgnoreProperties({"id", "months"})
public class YearNoMonthsReadModel extends YearFullReadModel {

    public YearNoMonthsReadModel(final YearExpenses source) {
        super(source);
    }
}

package io.github.organizationApp.yearExpenses;

import io.github.organizationApp.monthExpenses.MonthNoCategoriesWriteModel;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class YearFullWriteModel {
    @NotBlank(message = "year type can't be null")
    private String year;
    private String description;
    @Valid
    private List<MonthNoCategoriesWriteModel> months = new ArrayList<>();

    public String getYear() {return year;}
    public void setYear(final String year) {this.year = year;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<MonthNoCategoriesWriteModel> getMonths() {return months;}
    public void setMonths(final List<MonthNoCategoriesWriteModel> months) {this.months = months;}

    public YearExpenses toYear() {
        var result = new YearExpenses();
        result.setYear(year);
        result.setDescription(description);
        result.setMonths(months
                .stream()
                .map(source -> source.toMonth(result))
                .collect(Collectors.toList())
        );
        return result;
    }
}

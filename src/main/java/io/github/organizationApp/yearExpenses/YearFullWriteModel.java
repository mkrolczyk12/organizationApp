package io.github.organizationApp.yearExpenses;

import io.github.organizationApp.monthExpenses.MonthNoCategoriesWriteModel;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

public class YearFullWriteModel {
    @NotNull(message = "year type can't be null")
    private short year;
    private String description;
    @Valid
    @NotEmpty
    private List<MonthNoCategoriesWriteModel> months;

    public short getYear() {return year;}
    public void setYear(final short year) {this.year = year;}

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

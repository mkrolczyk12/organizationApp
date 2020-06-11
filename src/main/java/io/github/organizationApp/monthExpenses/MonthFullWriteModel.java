package io.github.organizationApp.monthExpenses;

import io.github.organizationApp.categoryExpenses.CategoryNoProcessesWriteModel;
import io.github.organizationApp.yearExpenses.YearExpenses;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MonthFullWriteModel {
    @NotBlank(message = "month type can't be null")
    private String month;
    private String description;
    @Valid
    private List<CategoryNoProcessesWriteModel> categories = new ArrayList<>();

    public String getMonth() {return month;}
    public void setMonth(final String month) {this.month = month.toLowerCase();}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<CategoryNoProcessesWriteModel> getCategories() {return categories;}
    public void setCategories(final List<CategoryNoProcessesWriteModel> categories) {this.categories = categories;}

    public MonthExpenses toMonth(YearExpenses year) {
        var result = new MonthExpenses();
        result.setMonth(month);
        result.setDescription(description);
        result.setCategories(categories
                .stream()
                .map(source -> source.toCategoryType(result))
                .collect(Collectors.toList())
        );
        result.setYear(year);
        return result;
    }
}

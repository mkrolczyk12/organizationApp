package io.github.organizationApp.monthExpenses.projection;

import io.github.organizationApp.categoryExpenses.projection.CategoryNoProcessesWriteModel;
import io.github.organizationApp.monthExpenses.MonthExpenses;
import io.github.organizationApp.yearExpenses.YearExpenses;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class MonthNoCategoriesWriteModel {
    @NotBlank(message = "month type can't be null")
    private String month;
    private String description;
    @Valid
    private List<CategoryNoProcessesWriteModel> categories = new ArrayList<>();

    public String getMonth() {return month;}
    public void setMonth(final String month) {this.month = month;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<CategoryNoProcessesWriteModel> getCategories() {return categories;}
    public void setCategories(final List<CategoryNoProcessesWriteModel> categories) {this.categories = categories;}

    public MonthExpenses toMonth(final YearExpenses year) {
        var result = new MonthExpenses();
        result.setMonth(month);
        result.setDescription(description);
        result.setYear(year);
        return result;
    }
}

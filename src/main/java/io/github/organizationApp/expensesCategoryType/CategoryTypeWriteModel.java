package io.github.organizationApp.expensesCategoryType;

import io.github.organizationApp.monthExpenses.MonthExpenses;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryTypeWriteModel {
    @NotBlank(message = "type can't be null")
    private String type;
    private String description;
    @Valid
    private List<CategoryTypeProcessWriteModel> processes;

    public String getType() {return type;}
    public void setType(final String type) {this.type = type;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<CategoryTypeProcessWriteModel> getProcesses() {return processes;}
    public void setProcesses(final List<CategoryTypeProcessWriteModel> processes) {this.processes = processes;}

    public CategoryType toCategoryType(final MonthExpenses month) {
        var result = new CategoryType();
        result.setType(type);
        result.setDescription(description);
        result.setProcesses(processes
                .stream()
                .map(CategoryTypeProcessWriteModel::toProcess)
                .collect(Collectors.toList())
        );
        result.setMonthExpenses_id(month);
        return result;
    }
}

package io.github.organizationApp.categoryExpenses.projection;

import io.github.organizationApp.categoryExpenses.CategoryType;
import io.github.organizationApp.monthExpenses.MonthExpenses;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryFullWriteModel {
    @NotBlank(message = "type can't be null")
    private String type;
    private String description;
    @Valid
    private List<CategoryProcessWriteModel> processes = new ArrayList<>();

    public String getType() {return type;}
    public void setType(final String type) {this.type = type.toLowerCase();}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<CategoryProcessWriteModel> getProcesses() {return processes;}
    public void setProcesses(final List<CategoryProcessWriteModel> processes) {this.processes = processes;}

    public CategoryType toCategoryType(final MonthExpenses month, final String userId) {
        var result = new CategoryType();
        result.setType(type);
        result.setDescription(description);
        result.setProcesses(processes
                .stream()
                .map(source -> source.toProcess(result, userId))
                .collect(Collectors.toList())
        );
        result.setMonthExpenses(month);
        result.setOwnerId(userId);
        return result;
    }
}

package io.github.organizationApp.expensesCategoryType;

import io.github.organizationApp.monthExpenses.MonthExpenses;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlainWriteModel {
    @NotBlank(message = "type can't be null")
    private String type;
    private String description;
    @Valid
    private List<PlainProcessWriteModel> processes = new ArrayList<>();

    public String getType() {return type;}
    public void setType(final String type) {this.type = type;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<PlainProcessWriteModel> getProcesses() {return processes;}
    public void setProcesses(final List<PlainProcessWriteModel> processes) {this.processes = processes;}

    public CategoryType toCategoryType(final MonthExpenses month) {
        var result = new CategoryType();
        result.setType(type);
        result.setDescription(description);
        result.setProcesses(processes
                .stream()
                .map(source -> source.toProcess(result))
                .collect(Collectors.toList())
        );
        result.setMonthExpenses(month);
        return result;
    }
}
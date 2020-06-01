package io.github.organizationApp.expensesCategoryType;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryTypeReadModel {
    private Integer id;
    private String type;
    private String description;
    private List<CategoryTypeProcessReadModel> processes;

    public CategoryTypeReadModel(CategoryType source) {
        this.id = source.getId();
        this.type = source.getType();
        this.description = source.getDescription();
        this.processes = source.getProcesses()
                .stream()
                .map(CategoryTypeProcessReadModel::new)
                .collect(Collectors.toList());
    }

    public Integer getId() {return id;}

    public String getType() {return type;}
    public void setType(final String type) {this.type = type;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<CategoryTypeProcessReadModel> getProcesses() {return processes;}
    public void setProcesses(final List<CategoryTypeProcessReadModel> processes) {this.processes = processes;}
}

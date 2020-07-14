package io.github.organizationApp.categoryExpenses.projection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.organizationApp.categoryExpenses.CategoryType;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;
import java.util.stream.Collectors;
@JsonIgnoreProperties({"id"})
public class CategoryFullReadModel extends RepresentationModel<CategoryFullReadModel> {
    private Integer id;
    private String type;
    private String description;
    private List<CategoryProcessReadModel> processes;

    public CategoryFullReadModel(CategoryType source) {
        this.id = source.getId();
        this.type = source.getType();
        this.description = source.getDescription();
        this.processes = source.getProcesses()
                .stream()
                .map(CategoryProcessReadModel::new)
                .collect(Collectors.toList());
    }

    /**
     * for CategoryNoProcessesReadModel class
     */
    public CategoryFullReadModel(Integer categoryId, String categoryType, String categoryDescription) {
        this.id = categoryId;
        this.type = categoryType;
        this.description = categoryDescription;
    }

    public Integer getId() {return id;}

    public String getType() {return type;}
    public void setType(final String type) {this.type = type;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<CategoryProcessReadModel> getProcesses() {return processes;}
    public void setProcesses(final List<CategoryProcessReadModel> processes) {this.processes = processes;}
}

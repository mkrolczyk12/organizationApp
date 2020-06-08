package io.github.organizationApp.monthExpenses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.organizationApp.categoryExpenses.CategoryNoProcessesReadModel;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties({"id"})
public class MonthFullReadModel extends RepresentationModel<MonthFullReadModel> {
    private Integer id;
    private String month;
    private String description;
    private List<CategoryNoProcessesReadModel> categories;

    public MonthFullReadModel(MonthExpenses source) {
        this.id = source.getId();
        this.month = source.getMonth();
        this.description = source.getDescription();
        this.categories = source.getCategories()
                .stream()
                .map(CategoryNoProcessesReadModel::new)
                .collect(Collectors.toList());
    }

    public Integer getId() {return id;}

    public String getMonth() {return month;}
    public void setMonth(final String month) {this.month = month;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<CategoryNoProcessesReadModel> getCategories() {return categories;}
    public void setCategories(final List<CategoryNoProcessesReadModel> categories) {this.categories = categories;}
}

package io.github.organizationApp.yearExpenses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.organizationApp.monthExpenses.MonthNoCategoriesReadModel;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties({"id"})
public class YearFullReadModel extends RepresentationModel<YearFullReadModel> {
    private Integer id;
    @NotNull(message = "year type can't be null")
    private short year;
    private String description;
    private List<MonthNoCategoriesReadModel> months;

    public YearFullReadModel(YearExpenses source) {
        this.id = source.getId();
        this.year = source.getYear();
        this.description = source.getDescription();
        this.months = source.getMonths()
                .stream()
                .map(MonthNoCategoriesReadModel::new)
                .collect(Collectors.toList());
    }

    public Integer getId() {return id;}

    public short getYear() {return year;}
    public void setYear(final short year) {this.year = year;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<MonthNoCategoriesReadModel> getMonths() {return months;}
    public void setMonths(final List<MonthNoCategoriesReadModel> months) {this.months = months;}
}

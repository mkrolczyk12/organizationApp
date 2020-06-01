package io.github.organizationApp.yearExpenses;

import io.github.organizationApp.monthExpenses.MonthExpenses;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name = "year_expenses")
public class YearExpenses extends RepresentationModel<YearExpenses> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank(message = "year type can't be null")
    private String year;
    private String description;
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "year_id")
    List<MonthExpenses> months;

    /**
     * Hibernate use it
     */
    public YearExpenses() {
    }

    public Integer getId() {return id;}

    String getYear() {return year;}
    void setYear(final String year) {this.year = year;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<MonthExpenses> getMonths() {return months;}
    public void setMonths(final List<MonthExpenses> months) {this.months = months;}

    public void fullUpdate(YearExpenses source) {
        this.year = source.year;
        this.description = source.description;
    }
}

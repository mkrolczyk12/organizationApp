package io.github.organizationApp.monthExpenses;

import io.github.organizationApp.expensesCategoryType.CategoryType;
import io.github.organizationApp.yearExpenses.YearExpenses;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name = "month_expenses")
public class MonthExpenses extends RepresentationModel<MonthExpenses> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank(message = "month type can't be null")
    private String month;
    private String description;
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "monthExpenses")
    private List<CategoryType> categories;
    @ManyToOne
    @JoinColumn(name = "year_id")
    private YearExpenses year;
    /**
     * Hibernate use it
     */
    public MonthExpenses() {
    }
    // dla testow
    public MonthExpenses(String month) {
        this.month = month;
    }

    public Integer getId() {return id;}

    public String getMonth() {return month;}
    public void setMonth(final String month) {this.month = month;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<CategoryType> getCategories() {return categories;}
    public void setCategories(final List<CategoryType> categories) {this.categories = categories;}

    public YearExpenses getYear() {return year;}
    public void setYear(final YearExpenses year) {this.year = year;}

    public void fullUpdate(MonthExpenses source) {
        this.month = source.month;
        this.description = source.description;
    }
}
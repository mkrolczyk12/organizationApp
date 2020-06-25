package io.github.organizationApp.yearExpenses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.organizationApp.monthExpenses.MonthExpenses;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "year_expenses")
@JsonIgnoreProperties({"id", "months", "ownerId"})
public class YearExpenses extends RepresentationModel<YearExpenses> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotNull(message = "year type can't be null")
    private short year;
    private String description;
    @Column(name = "owner_id")
    private String ownerId;
    @OneToMany(mappedBy = "year")
    private List<MonthExpenses> months;

    /**
     * Hibernate use it
     */
    public YearExpenses() {
    }

    public YearExpenses(short year) {
        this.year = year;
    }

    public Integer getId() {return id;}

    public short getYear() {return year;}
    public void setYear(final short year) {this.year = year;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public String getOwnerId() {return ownerId;}
    public void setOwnerId(final String ownerId) {this.ownerId = ownerId;}

    public List<MonthExpenses> getMonths() {return months;}
    public void setMonths(final List<MonthExpenses> months) {this.months = months;}

    public void fullUpdate(YearExpenses source) {
        this.year = source.year;
        this.description = source.description;
    }
}

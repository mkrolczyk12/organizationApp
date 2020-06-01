package io.github.organizationApp.expensesCategoryType;

import io.github.organizationApp.expensesProcess.Process;
import io.github.organizationApp.monthExpenses.MonthExpenses;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name = "expenses_category")
public class CategoryType extends RepresentationModel<CategoryType> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotBlank(message = "type can't be null")
    private String type;
    private String description;

    /**
     * OneToMany relation with Process class
     */
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "category_id")
    private List<Process> processes;
    @ManyToOne
    @JoinColumn(name = "month_id")
    private MonthExpenses monthExpenses_id;

    /**
     * Hibernate use it
     */
    public CategoryType() {
    }

    public CategoryType(CategoryType source) {
        this.type = source.type;
        this.description = source.description;
    }

    public int getId() {return id;}

    public String getType() {return type;}
    public void setType(final String type) {this.type = type;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<Process> getProcesses() {return processes;}
    public void setProcesses(final List<Process> processes) {this.processes = processes;}

    public MonthExpenses getMonthExpenses_id() {return monthExpenses_id;}
    public void setMonthExpenses_id(final MonthExpenses monthExpenses_id) {this.monthExpenses_id = monthExpenses_id;}

    void fullUpdate(final CategoryType toUpdate) {
        this.type = toUpdate.type;
        this.description = toUpdate.description;
    }
}

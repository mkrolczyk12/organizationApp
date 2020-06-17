package io.github.organizationApp.categoryExpenses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.organizationApp.expensesProcess.Process;
import io.github.organizationApp.monthExpenses.MonthExpenses;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name = "category_expenses")
@JsonIgnoreProperties({"id","monthExpenses","ownerId"})
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
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "category")
    private List<Process> processes;
    @ManyToOne
    @JoinColumn(name = "month_id")
    private MonthExpenses monthExpenses;
    @Column(name = "owner_id")
    private String ownerId;

    /**
     * Hibernate use it
     */
    public CategoryType() {
    }

    public CategoryType(CategoryType source) {
        this.type = source.type.toLowerCase();
        this.description = source.description;
        this.ownerId = source.ownerId;
    }

    public int getId() {return id;}

    public String getType() {return type;}
    public void setType(final String type) {this.type = type.toLowerCase();}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<Process> getProcesses() {return processes;}
    public void setProcesses(final List<Process> processes) {this.processes = processes;}

    public MonthExpenses getMonthExpenses() {return monthExpenses;}
    public void setMonthExpenses(final MonthExpenses monthExpenses) {this.monthExpenses = monthExpenses;}

    public String getOwnerId() {return ownerId;}
    public void setOwnerId(final String ownerId) {this.ownerId = ownerId;}

    void fullUpdate(final CategoryType toUpdate) {
        this.type = toUpdate.type.toLowerCase();
        this.description = toUpdate.description;
    }
}

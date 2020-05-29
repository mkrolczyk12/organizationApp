package io.github.organizationApp.expensesProcess;

import io.github.organizationApp.expensesCategoryType.CategoryType;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses_process")
public class Process extends RepresentationModel<Process> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "price can't be null or empty")
    private BigDecimal price;
    @NotBlank(message = "select currency")
    private String currency;
//    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime buy_date;
    @NotBlank(message = "description can't be null or empty")
    private String description;
    private String transaction_type;
    private String notes;

    /**
     * ManyToOne relation with CategoryType class
     */
    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryType category_id;

    /**
     * embedded table
     */
    @Embedded
    private Audit audit = new Audit();

    /**
     * Hibernate use it
     */
    public Process() {
    }

    public Long getId() {return id;}

    public BigDecimal getPrice() {return price;}
    public void setPrice(final BigDecimal price) {this.price = price;}

    public String getCurrency() {return currency;}
    public void setCurrency(final String currency) {this.currency = currency;}

    public LocalDateTime getBuy_date() {return buy_date;}
    public void setBuy_date(final LocalDateTime buy_date) {this.buy_date = buy_date;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public String getTransaction_type() {return transaction_type;}
    public void setTransaction_type(final String transaction_type) {this.transaction_type = transaction_type;}

    public String getNotes() {return notes;}
    public void setNotes(final String notes) {this.notes = notes;}

    public CategoryType getCategory_id() {return category_id;}
    public void setCategory_id(final CategoryType category_id) {this.category_id = category_id;}

    public void fullUpdate(Process toUpdate){
        this.price = toUpdate.price;
        this.currency = toUpdate.currency;
        this.buy_date = toUpdate.buy_date;
        this.description = toUpdate.description;
        this.transaction_type = toUpdate.transaction_type;
        this.notes = toUpdate.notes;
    }
}

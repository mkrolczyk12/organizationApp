package io.github.organizationApp.expensesProcess;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.organizationApp.expensesCategoryType.CategoryType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses_process")
@JsonIgnoreProperties({"id","category"})
public class Process extends RepresentationModel<Process> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "description can't be null or empty")
    private String description;
    @NotNull(message = "price can't be null or empty")
    private BigDecimal price;
    @NotBlank(message = "select currency")
    private String currency;
    private String transaction_type;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime buy_date;
    private String notes;

    /**
     * ManyToOne relation with CategoryType class
     */
    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryType category;

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

    public Process(BigDecimal price, String currency, LocalDateTime buy_date,String description,String transaction_type,String notes, CategoryType categoryType) {
        this.price = price;
        this.currency = currency;
        this.buy_date = buy_date;
        this.description = description;
        this.transaction_type = transaction_type;
        this.notes = notes;
        this.category = categoryType;
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

    public CategoryType getCategory() {return category;}
    public void setCategory(final CategoryType category) {this.category = category;}

    public void fullUpdate(Process toUpdate){
        this.price = toUpdate.price;
        this.currency = toUpdate.currency;
        this.buy_date = toUpdate.buy_date;
        this.description = toUpdate.description;
        this.transaction_type = toUpdate.transaction_type;
        this.notes = toUpdate.notes;
    }
}


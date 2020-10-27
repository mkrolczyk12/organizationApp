package io.github.organizationApp.expensesProcess;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.organizationApp.categoryExpenses.CategoryType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses_process")
@JsonIgnoreProperties({"category","ownerId"})
public class Process extends RepresentationModel<Process> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//    @NotBlank(message = "description can't be null or empty")
    private String description;
//    @NotNull(message = "price can't be null or empty")
    private BigDecimal price;
//    @NotBlank(message = "select currency")
    private String currency;
    private String transaction_type;
//    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime buy_date;
    private String notes;
    @Column(name = "owner_id")
    private String ownerId;

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
    Process() {
    }

    private Process(final ProcessBuilder processBuilder) {
        this.price = processBuilder.price;
        this.currency = processBuilder.currency;
        this.buy_date = processBuilder.buy_date;
        this.description = processBuilder.description;
        this.transaction_type = processBuilder.transaction_type;
        this.notes = processBuilder.notes;
        this.category = processBuilder.categoryType;
        this.ownerId = processBuilder.ownerId;
    }

    public Long getId() {return id;}

    public BigDecimal getPrice() {return price;}
//    public void setPrice(final BigDecimal price) {this.price = price;}

    public String getCurrency() {return currency;}
//    public void setCurrency(final String currency) {this.currency = currency;}

    public LocalDateTime getBuy_date() {return buy_date;}
//    public void setBuy_date(final LocalDateTime buy_date) {this.buy_date = buy_date;}

    public String getDescription() {return description;}
//    public void setDescription(final String description) {this.description = description;}

    public String getTransaction_type() {return transaction_type;}
//    public void setTransaction_type(final String transaction_type) {this.transaction_type = transaction_type;}

    public String getNotes() {return notes;}
//    public void setNotes(final String notes) {this.notes = notes;}

    public String getOwnerId() {return ownerId;}
    public void setOwnerId(final String ownerId) {this.ownerId = ownerId;}

    public CategoryType getCategory() {return category;}
    public void setCategory(final CategoryType category) {this.category = category;}

    public void fullUpdate(Process toUpdate) {
        this.price = toUpdate.price;
        this.currency = toUpdate.currency;
        this.buy_date = toUpdate.buy_date;
        this.description = toUpdate.description;
        this.transaction_type = toUpdate.transaction_type;
        this.notes = toUpdate.notes;
    }

    public static class ProcessBuilder {
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
        private CategoryType categoryType;
        private String ownerId;

        public ProcessBuilder buildDescription(final String description) {
            this.description = description;
            return this;
        }
        public ProcessBuilder buildPrice(final BigDecimal price) {
            this.price = price;
            return this;
        }
        public ProcessBuilder buildCurrency(final String currency) {
            this.currency = currency;
            return this;
        }
        public ProcessBuilder buildTransactionType(final String transaction_type) {
            this.transaction_type = transaction_type;
            return this;
        }
        public ProcessBuilder buildBuyDate(final LocalDateTime buy_date) {
            this.buy_date = buy_date;
            return this;
        }
        public ProcessBuilder buildNotes(final String notes) {
            this.notes = notes;
            return this;
        }
        public ProcessBuilder buildCategoryType(final CategoryType categoryType) {
            this.categoryType = categoryType;
            return this;
        }
        public ProcessBuilder buildOwnerId(final String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Process build() {
            return new Process(this);
        }
    }
}


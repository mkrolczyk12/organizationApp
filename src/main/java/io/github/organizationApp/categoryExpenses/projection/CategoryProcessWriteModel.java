package io.github.organizationApp.categoryExpenses.projection;

import io.github.organizationApp.categoryExpenses.CategoryType;
import io.github.organizationApp.expensesProcess.Process;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CategoryProcessWriteModel {
    @NotNull(message = "price can't be null or empty")
    private BigDecimal price;
    @NotBlank(message = "select currency")
    private String currency;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime buy_date;
    @NotBlank(message = "description can't be null or empty")
    private String description;
    private String transaction_type;
    private String notes;

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

    public Process toProcess(final CategoryType categoryType, final String ownerId) {
        return new Process
            .ProcessBuilder()
            .buildPrice(price)
            .buildCurrency(currency)
            .buildBuyDate(buy_date)
            .buildDescription(description)
            .buildTransactionType(transaction_type)
            .buildNotes(notes)
            .buildCategoryType(categoryType)
            .buildOwnerId(ownerId)
            .build();
//        return new Process(price, currency, buy_date, description, transaction_type, notes, categoryType, ownerId);
    }
}

package io.github.organizationApp.expensesCategoryType;

import io.github.organizationApp.expensesProcess.Process;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PlainProcessReadModel {
    private BigDecimal price;
    private String currency;
    private LocalDateTime buy_date;
    private String description;
    private String transaction_type;
    private String notes;

    PlainProcessReadModel(Process source) {
        this.price = source.getPrice();
        this.currency = source.getCurrency();
        this.buy_date = source.getBuy_date();
        this.description = source.getDescription();
        this.transaction_type = source.getTransaction_type();
        this.notes = source.getNotes();
    }

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
}

package com.datamaveric.anomalydetector.model;

import java.time.LocalDateTime;


//public record Transaction(String transactionId, double amount, LocalDateTime timestamp, String accountId, String type){}



import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Transaction {
    @Id
    private String transactionId;
    private double amount;
    private LocalDateTime timestamp;
    private String accountId;
    private String type;

    // Constructors
    public Transaction() {}

    public Transaction(String transactionId, double amount, LocalDateTime timestamp,
                       String accountId, String type) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.accountId = accountId;
        this.type = type;
    }

    // Getters and setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
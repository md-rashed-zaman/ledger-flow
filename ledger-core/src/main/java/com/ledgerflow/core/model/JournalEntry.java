package com.ledgerflow.core.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "journal_entries")
public class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionId; // Links to the parent Transaction

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String type; // "DEBIT" or "CREDIT"

    @Column(nullable = false)
    private BigDecimal amount; // Positive for Credit, Negative for Debit

    private LocalDateTime timestamp;

    public JournalEntry(String transactionId, String accountNumber, String type, BigDecimal amount) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }
}
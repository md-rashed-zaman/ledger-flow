package com.ledgerflow.core.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String referenceId; // The Idempotency Key from Go

    private LocalDateTime timestamp;
    
    private String sourceAccount;
    private String targetAccount;
    private BigDecimal amount;
    private String status; // PENDING, COMPLETED, FAILED
}
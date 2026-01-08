package com.ledgerflow.core.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber; // e.g., "ACC_ALICE"

    @Column(nullable = false)
    private String currency;

    // We store balance for fast reads, but it must match the sum of transactions
    @Column(nullable = false)
    private BigDecimal balance; 
}
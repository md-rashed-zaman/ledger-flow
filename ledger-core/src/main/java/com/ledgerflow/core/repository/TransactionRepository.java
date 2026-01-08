package com.ledgerflow.core.repository;

import com.ledgerflow.core.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
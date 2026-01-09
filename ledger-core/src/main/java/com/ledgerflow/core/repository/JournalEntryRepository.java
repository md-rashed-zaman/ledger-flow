package com.ledgerflow.core.repository;

import com.ledgerflow.core.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
}
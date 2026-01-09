package com.ledgerflow.core.service;

import com.ledgerflow.core.model.Account;
import com.ledgerflow.core.model.JournalEntry;
import com.ledgerflow.core.model.Transaction;
import com.ledgerflow.core.model.TransferResult;
import com.ledgerflow.core.repository.AccountRepository;
import com.ledgerflow.core.repository.JournalEntryRepository;
import com.ledgerflow.core.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final JournalEntryRepository journalEntryRepository;
    
    private final KafkaTemplate<String, Object> kafkaTemplate; // Inject Kafka Producer

    @Transactional
    public void processTransaction(Transaction tx) {
        log.info("Processing transaction: {}", tx.getReferenceId());

        // 1. IDEMPOTENCY CHECK (The Guard)
        // If we already have a transaction with this ID, stop immediately.
        Optional<Transaction> existingTx = transactionRepository.findAll().stream()
                .filter(t -> t.getReferenceId().equals(tx.getReferenceId()))
                .findFirst();
        // Note: In a real high-perf app, use a custom repository method findByReferenceId()

        if (existingTx.isPresent()) {
            log.warn("Duplicate transaction detected: {}. Skipping.", tx.getReferenceId());
            return;
        }

        // 2. Fetch Accounts
        Account source = accountRepository.findByAccountNumber(tx.getSourceAccount()).orElse(null);
        Account target = accountRepository.findByAccountNumber(tx.getTargetAccount()).orElse(null);

        // 3. Validation Logic
        if (source == null || target == null) {
            failTransaction(tx, "Invalid accounts");
            return;
        }

        if (source.getBalance().compareTo(tx.getAmount()) < 0) {
            failTransaction(tx, "Insufficient funds");
            return;
        }

        // 4. EXECUTE TRANSFER (Double-Entry Logic)
        
        // A. Update Balances (The Read Cache)
        source.setBalance(source.getBalance().subtract(tx.getAmount()));
        target.setBalance(target.getBalance().add(tx.getAmount()));

        // B. Create Journal Entries (The Source of Truth)
        // Entry 1: Debit the Sender (Negative Amount)
        JournalEntry debitEntry = new JournalEntry(
                tx.getReferenceId(),
                source.getAccountNumber(),
                "DEBIT",
                tx.getAmount().negate() // -100
        );

        // Entry 2: Credit the Receiver (Positive Amount)
        JournalEntry creditEntry = new JournalEntry(
                tx.getReferenceId(),
                target.getAccountNumber(),
                "CREDIT",
                tx.getAmount() // +100
        );

        // Update Transaction Status
        tx.setStatus("COMPLETED");
        tx.setTimestamp(LocalDateTime.now());

        // 5. Save Everything
        accountRepository.save(source);
        accountRepository.save(target);
        transactionRepository.save(tx);
        
        // Save the Double Entries
        journalEntryRepository.save(debitEntry);
        journalEntryRepository.save(creditEntry);

        log.info("✅ Transaction Successful: {}", tx.getReferenceId());

        // 5. NOTIFY (Close the Loop)
        sendNotification(tx.getReferenceId(), "COMPLETED", "Transfer successful");
    }

    private void failTransaction(Transaction tx, String reason) {
        tx.setStatus("FAILED");
        tx.setTimestamp(LocalDateTime.now());
        transactionRepository.save(tx);
        log.error("Transaction failed: {}", reason);
        sendNotification(tx.getReferenceId(), "FAILED", reason);
    }

    private void sendNotification(String refId, String status, String msg) {
        TransferResult result = new TransferResult(refId, status, msg);
        // Send to 'transfer-results' topic
        kafkaTemplate.send("transfer-results", refId, result);
    }
}
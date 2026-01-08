package com.ledgerflow.core.event;

import com.ledgerflow.core.model.Transaction;
import com.ledgerflow.core.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferRequestConsumer {

    private final LedgerService ledgerService;

    @KafkaListener(topics = "transfer-requests", groupId = "ledger-group")
    public void consumeTransferRequest(Map<String, Object> payload) {
        log.info("Received Transfer Request: {}", payload);

        try {
            // 1. Map Payload to Transaction Object
            Transaction tx = new Transaction();
            tx.setReferenceId((String) payload.get("idempotency_key"));
            tx.setSourceAccount((String) payload.get("source_account"));
            tx.setTargetAccount((String) payload.get("target_account"));
            
            // Handle number conversion safely
            Double amountDouble = 0.0;
            if (payload.get("amount") instanceof Integer) {
                amountDouble = ((Integer) payload.get("amount")).doubleValue();
            } else {
                amountDouble = (Double) payload.get("amount");
            }
            tx.setAmount(BigDecimal.valueOf(amountDouble));

            // 2. Process the Business Logic
            ledgerService.processTransaction(tx);

        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }
}
package com.ledgerflow.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResult {
    private String referenceId;
    private String status; // COMPLETED, FAILED
    private String message;
}
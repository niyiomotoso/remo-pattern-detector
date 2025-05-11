package com.example.remopatterndetector.dto;

import com.example.remopatterndetector.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SuspiciousTransactionResponse {
    private Long id;
    private Double amount;
    private LocalDateTime timestamp;
    private TransactionType type;
}

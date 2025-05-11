package com.example.remopatterndetector.dto;

import com.example.remopatterndetector.model.TransactionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionRequest {
    private String userId;
    private Double amount;
    private LocalDateTime timestamp;
    private TransactionType type;
}

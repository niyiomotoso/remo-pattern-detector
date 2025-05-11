package com.example.remopatterndetector.service;

import com.example.remopatterndetector.dto.TransactionRequest;
import com.example.remopatterndetector.dto.SuspiciousTransactionResponse;

import java.util.List;

public interface TransactionService {
    void logTransaction(TransactionRequest request);
    List<SuspiciousTransactionResponse> getSuspiciousTransactionsForUser(String userId);
}

package com.example.remopatterndetector.controller;

import com.example.remopatterndetector.dto.TransactionRequest;
import com.example.remopatterndetector.dto.SuspiciousTransactionResponse;
import com.example.remopatterndetector.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transactions")
    public ResponseEntity<Void> logTransaction(@RequestBody TransactionRequest request) {
        transactionService.logTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/users/{userId}/suspicious")
    public ResponseEntity<List<SuspiciousTransactionResponse>> getSuspiciousTransactions(
            @PathVariable String userId
    ) {
        List<SuspiciousTransactionResponse> result = transactionService.getSuspiciousTransactionsForUser(userId);
        return ResponseEntity.ok(result);
    }
}

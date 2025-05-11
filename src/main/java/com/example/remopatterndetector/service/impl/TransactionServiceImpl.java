package com.example.remopatterndetector.service.impl;

import com.example.remopatterndetector.dto.TransactionRequest;
import com.example.remopatterndetector.dto.SuspiciousTransactionResponse;
import com.example.remopatterndetector.model.Transaction;
import com.example.remopatterndetector.repository.TransactionRepository;
import com.example.remopatterndetector.service.SuspiciousActivityService;
import com.example.remopatterndetector.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final SuspiciousActivityService suspiciousActivityService;

    @Override
    public void logTransaction(TransactionRequest request) {
        Transaction txn = new Transaction();
        txn.setUserId(request.getUserId());
        txn.setAmount(request.getAmount());
        txn.setTimestamp(request.getTimestamp());
        txn.setType(request.getType());

        boolean isSuspicious = suspiciousActivityService.isSuspicious(txn);
        txn.setSuspicious(isSuspicious);

        transactionRepository.save(txn);
    }

    @Override
    public List<SuspiciousTransactionResponse> getSuspiciousTransactionsForUser(String userId) {
        return transactionRepository.findByUserIdAndSuspiciousTrueOrderByTimestampAsc(userId)
                .stream()
                .map(txn -> new SuspiciousTransactionResponse(
                        txn.getId(),
                        txn.getAmount(),
                        txn.getTimestamp(),
                        txn.getType()
                ))
                .collect(Collectors.toList());
    }
}

package com.example.remopatterndetector.service.impl;

import com.example.remopatterndetector.model.Transaction;
import com.example.remopatterndetector.model.TransactionType;
import com.example.remopatterndetector.repository.TransactionRepository;
import com.example.remopatterndetector.service.SuspiciousActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleBasedSuspiciousActivityService implements SuspiciousActivityService {

    private final TransactionRepository transactionRepository;

    private static final double HIGH_VALUE_THRESHOLD = 10000.0;
    private static final int SMALL_TXN_THRESHOLD = 5;
    private static final double SMALL_TXN_AMOUNT = 100.0;
    private static final int RAPID_TRANSFER_THRESHOLD = 3;

    @Override
    public boolean isSuspicious(Transaction txn) {
        return isHighValue(txn)
                || isFrequentSmallTransactions(txn)
                || isRapidTransfers(txn);
    }

    private boolean isHighValue(Transaction txn) {
        return txn.getAmount() > HIGH_VALUE_THRESHOLD;
    }

    private boolean isFrequentSmallTransactions(Transaction txn) {
        LocalDateTime oneHourAgo = txn.getTimestamp().minusHours(1);
        List<Transaction> txns = transactionRepository.findByUserIdAndTimestampBetween(
                txn.getUserId(), oneHourAgo, txn.getTimestamp());

        long count = txns.stream()
                .filter(t -> t.getAmount() < SMALL_TXN_AMOUNT)
                .count();

        return count >= SMALL_TXN_THRESHOLD;
    }

    private boolean isRapidTransfers(Transaction txn) {
        if (txn.getType() != TransactionType.TRANSFER) {
            return false;
        }

        LocalDateTime fiveMinutesAgo = txn.getTimestamp().minusMinutes(5);
        List<Transaction> transfers = transactionRepository.findByUserIdAndTypeAndTimestampBetween(
                txn.getUserId(), TransactionType.TRANSFER, fiveMinutesAgo, txn.getTimestamp());

        return transfers.size() >= (RAPID_TRANSFER_THRESHOLD - 1);
    }
}

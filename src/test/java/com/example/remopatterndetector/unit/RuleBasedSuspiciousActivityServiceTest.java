package com.example.remopatterndetector.unit;

import com.example.remopatterndetector.model.Transaction;
import com.example.remopatterndetector.model.TransactionType;
import com.example.remopatterndetector.repository.TransactionRepository;
import com.example.remopatterndetector.service.impl.RuleBasedSuspiciousActivityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RuleBasedSuspiciousActivityServiceTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private RuleBasedSuspiciousActivityService detector;

    @Test
    void shouldFlagHighValueTransaction() {
        Transaction txn = new Transaction();
        txn.setAmount(15000.0);
        txn.setTimestamp(LocalDateTime.now());

        assertTrue(detector.isSuspicious(txn));
    }

    @Test
    void shouldFlagFrequentSmallTransactions() {
        Transaction txn = new Transaction();
        txn.setAmount(50.0);
        txn.setUserId("user1");
        txn.setTimestamp(LocalDateTime.now());

        List<Transaction> history = List.of(
                txn, txn, txn, txn, txn, txn // 6 small txns
        );

        when(repository.findByUserIdAndTimestampBetween(any(), any(), any()))
                .thenReturn(history);

        assertTrue(detector.isSuspicious(txn));
    }

    @Test
    void shouldNotFlagNormalTransaction() {
        Transaction txn = new Transaction();
        txn.setAmount(500.0);
        txn.setUserId("user1");
        txn.setTimestamp(LocalDateTime.now());
        txn.setType(TransactionType.DEPOSIT);

        when(repository.findByUserIdAndTimestampBetween(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        assertFalse(detector.isSuspicious(txn));
    }

    @Test
    void shouldFlagRapidTransfers() {
        Transaction txn = new Transaction();
        txn.setUserId("user1");
        txn.setTimestamp(LocalDateTime.now());
        txn.setType(TransactionType.TRANSFER);
        txn.setAmount(200.0);

        List<Transaction> transfers = List.of(
                txn, txn, txn // simulate 3 within 5 mins
        );

        when(repository.findByUserIdAndTypeAndTimestampBetween(
                anyString(), any(TransactionType.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transfers);

        assertTrue(detector.isSuspicious(txn));
    }

    @Test
    void shouldNotFlagTransferIfBelowThreshold() {
        Transaction txn = new Transaction();
        txn.setAmount(50.0);
        txn.setUserId("user1");
        txn.setTimestamp(LocalDateTime.now());
        txn.setType(TransactionType.TRANSFER);

        when(repository.findByUserIdAndTypeAndTimestampBetween(
                anyString(), any(TransactionType.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(txn)); // only 1 transfer

        assertFalse(detector.isSuspicious(txn));
    }
}

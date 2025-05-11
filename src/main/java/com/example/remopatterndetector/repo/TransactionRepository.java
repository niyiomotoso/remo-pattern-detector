package com.example.remopatterndetector.repository;

import com.example.remopatterndetector.model.Transaction;
import com.example.remopatterndetector.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdAndTimestampBetween(String userId, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByUserIdAndTypeAndTimestampBetween(String userId, TransactionType type, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByUserIdAndSuspiciousTrueOrderByTimestampAsc(String userId);

    List<Transaction> findTop3ByUserIdAndTypeOrderByTimestampDesc(String userId, TransactionType type);
}

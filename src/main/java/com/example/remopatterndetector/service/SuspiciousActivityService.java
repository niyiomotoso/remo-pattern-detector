package com.example.remopatterndetector.service;

import com.example.remopatterndetector.model.Transaction;

public interface SuspiciousActivityService {
    boolean isSuspicious(Transaction transaction);
}

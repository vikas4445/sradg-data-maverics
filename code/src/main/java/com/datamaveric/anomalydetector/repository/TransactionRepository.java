package com.datamaveric.anomalydetector.repository;

import com.datamaveric.anomalydetector.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
}

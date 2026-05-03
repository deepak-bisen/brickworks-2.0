package com.brickwork.finance.repository;

import com.brickwork.finance.entity.InvoiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InvoiceRecordRepository extends JpaRepository<InvoiceRecord, String> {
    Optional<InvoiceRecord> findByOrderId(String orderId);
}
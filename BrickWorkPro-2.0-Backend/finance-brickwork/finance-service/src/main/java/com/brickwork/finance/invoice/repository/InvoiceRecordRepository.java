package com.brickwork.finance.invoice.repository;

import com.brickwork.finance.invoice.entity.InvoiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InvoiceRecordRepository extends JpaRepository<InvoiceRecord, String> {
    Optional<InvoiceRecord> findByOrderId(String orderId);
}
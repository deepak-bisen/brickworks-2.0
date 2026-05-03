package com.brickwork.finance.repository;

import com.brickwork.finance.entity.InvoiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRecordRepository extends JpaRepository<InvoiceRecord, String> {
}

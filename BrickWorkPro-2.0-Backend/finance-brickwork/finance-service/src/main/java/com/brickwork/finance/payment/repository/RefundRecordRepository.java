package com.brickwork.finance.payment.repository;

import com.brickwork.finance.payment.entity.RefundRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRecordRepository extends JpaRepository<RefundRecord, String> {
}

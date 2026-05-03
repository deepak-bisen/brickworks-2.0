package com.brickwork.finance.repository;

import com.brickwork.finance.entity.RefundRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRecordRepository extends JpaRepository<RefundRecord, String> {
}

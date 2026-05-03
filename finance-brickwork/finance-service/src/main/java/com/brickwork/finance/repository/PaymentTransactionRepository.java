package com.brickwork.finance.repository;

import com.brickwork.finance.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
    Optional<PaymentTransaction> findByOrderId(String orderId);
    Optional<PaymentTransaction> findByRazorpayOrderId(String razorpayOrderId);
}
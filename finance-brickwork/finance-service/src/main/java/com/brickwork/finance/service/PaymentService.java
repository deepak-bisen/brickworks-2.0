package com.brickwork.finance.service;

import com.brickwork.finance.entity.PaymentTransaction;
import com.brickwork.finance.repository.PaymentTransactionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Autowired
    private PaymentTransactionRepository transactionRepo;

    // 1. Create Order (Triggered when user clicks "Pay")
    @Transactional
    public PaymentTransaction createRazorpayOrder(String orderId, Double amount) throws RazorpayException {

        // EXTRA PIECE: IDEMPOTENCY CHECK
        // If a transaction already exists and is SUCCESSFUL, do not create a new one!
        Optional<PaymentTransaction> existingTxOpt = transactionRepo.findByOrderId(orderId);
        if (existingTxOpt.isPresent()) {
            PaymentTransaction existingTx = existingTxOpt.get();
            if (existingTx.getStatus() == PaymentTransaction.PaymentStatus.SUCCESS) {
                throw new RuntimeException("Payment already completed for this order!");
            }
            // If it failed previously, we can allow a retry by updating it or creating a new attempt.
        }

        // Initialize SDK
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        // Razorpay expects amount in paise (multiply by 100)
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", orderId);

        // Call Razorpay API
        Order razorpayOrder = razorpay.orders.create(orderRequest);

        // Save to our database as CREATED
        PaymentTransaction transaction = existingTxOpt.orElse(new PaymentTransaction());
        transaction.setOrderId(orderId);
        transaction.setAmount(amount);
        transaction.setRazorpayOrderId(razorpayOrder.get("id"));
        transaction.setStatus(PaymentTransaction.PaymentStatus.CREATED);

        return transactionRepo.save(transaction);
    }

    // 2. Verify Signature (Triggered after successful payment on UI)
    @Transactional
    public boolean verifyPaymentSignature(String rzpOrderId, String rzpPaymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", rzpOrderId);
            options.put("razorpay_payment_id", rzpPaymentId);
            options.put("razorpay_signature", signature);

            // Cryptographic check using your secret key
            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (isValid) {
                PaymentTransaction transaction = transactionRepo.findByRazorpayOrderId(rzpOrderId)
                        .orElseThrow(() -> new RuntimeException("Transaction not found"));

                // Idempotency: Only update if it's not already success
                if (transaction.getStatus() != PaymentTransaction.PaymentStatus.SUCCESS) {
                    transaction.setRazorpayPaymentId(rzpPaymentId);
                    transaction.setStatus(PaymentTransaction.PaymentStatus.SUCCESS);
                    transactionRepo.save(transaction);

                    // TODO: In Phase 2, we will use OpenFeign here to tell the Orders service to update its status!
                }
                return true;
            }
            return false;
        } catch (RazorpayException e) {
            return false;
        }
    }
}
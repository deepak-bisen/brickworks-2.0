package com.brickwork.finance.payment.service.impl;

import com.brickwork.finance.client.OrderFeignClient;
import com.brickwork.finance.payment.dto.RazorpayOrderResponseDTO;
import com.brickwork.finance.payment.dto.UtrSubmissionDTO;
import com.brickwork.finance.payment.entity.PaymentTransaction;
import com.brickwork.finance.payment.entity.RefundRecord;
import com.brickwork.finance.payment.repository.PaymentTransactionRepository;
import com.brickwork.finance.invoice.service.InvoiceService;
import com.brickwork.finance.payment.repository.RefundRecordRepository;
import com.brickwork.finance.payment.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import com.brickwork.exception.BadRequestException;
import com.brickwork.exception.ConflictException;
import com.brickwork.exception.ForbiddenException;
import com.brickwork.exception.NotFoundException;
import com.brickwork.exception.UnauthorizedException;
import com.brickwork.security.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.brickwork.finance.payment.enums.PaymentMethod.*;
import static com.brickwork.finance.payment.enums.PaymentStatus.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.test.mode:true}") // Default set to true for now
    private boolean isTestMode;


    private final PaymentTransactionRepository  paymentTransactionRepository;
    private final OrderFeignClient orderFeignClient;
    private final InvoiceService  invoiceService;
    private final RefundRecordRepository refundRecordRepository;


    @Autowired
    public PaymentServiceImpl(PaymentTransactionRepository paymentTransactionRepository, OrderFeignClient orderFeignClient, InvoiceService invoiceService, RefundRecordRepository refundRecordRepository) {

        this.paymentTransactionRepository = paymentTransactionRepository;
        this.orderFeignClient = orderFeignClient;
        this.invoiceService = invoiceService;
        this.refundRecordRepository = refundRecordRepository;
    }

    @Override
    @Transactional
    public RazorpayOrderResponseDTO createRazorpayOrder(String orderId, Double amount) {
        // Idempotency check
        Optional<PaymentTransaction> existingTxOpt = paymentTransactionRepository.findByOrderId(orderId);
        if (existingTxOpt.isPresent() && existingTxOpt.get().getPaymentStatus() == SUCCESS) {
            throw new ConflictException("Payment already completed for this order.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        }

        try {
            RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount * 100); // Razorpay uses paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", orderId);

            Order razorpayOrder = razorpay.orders.create(orderRequest);

            PaymentTransaction transaction = existingTxOpt.orElse(new PaymentTransaction());
            transaction.setOrderId(orderId);
            transaction.setAmount(amount);
            transaction.setRazorpayOrderId(razorpayOrder.get("id"));
            transaction.setPaymentStatus(CREATED);
            transaction.setPaymentMethod(ONLINE);
            paymentTransactionRepository.save(transaction);

            log.info("Razorpay order created: orderId={}, razorpayOrderId={}, amount={}",
                    orderId, razorpayOrder.get("id"), amount);
            return new RazorpayOrderResponseDTO(razorpayOrder.get("id"), amount, "INR");
        } catch (RazorpayException e) {
            throw new BadRequestException("Payment gateway error: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean verifyPaymentSignature(String rzpOrderId, String rzpPaymentId, String signature, String orderId) {

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", rzpOrderId);
            options.put("razorpay_payment_id", rzpPaymentId);
            options.put("razorpay_signature", signature);

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);
            if (isValid) {
                processSuccessfulPayment(rzpOrderId, rzpPaymentId, orderId);
                log.info("Payment signature verified for order {}", orderId);
                return true;
            }
            log.warn("Invalid payment signature for order {}", orderId);
            return false;
        } catch (RazorpayException e) {
            return false;
        }
    }

    @Override
    @Transactional
    public void handleWebhookPaymentCaptured(String rzpOrderId, String rzpPaymentId) {
        paymentTransactionRepository.findByRazorpayOrderId(rzpOrderId).ifPresent(tx -> {
            processSuccessfulPayment(rzpOrderId, rzpPaymentId, tx.getOrderId());
        });
    }

    private void processSuccessfulPayment(String rzpOrderId, String rzpPaymentId, String orderId) {
        PaymentTransaction tx = paymentTransactionRepository.findByRazorpayOrderId(rzpOrderId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        if (tx.getPaymentStatus() != SUCCESS) {
            tx.setRazorpayPaymentId(rzpPaymentId);
            tx.setPaymentStatus(SUCCESS);
            paymentTransactionRepository.save(tx);
            log.info("Payment marked successful for order {}, razorpayPaymentId={}", orderId, rzpPaymentId);

            // IMPORTANT: Do NOT let downstream order status update failure roll back the payment
            // or return an error to the caller (UI or webhook). The payment record is the source
            // of truth for money movement. Order sync can be reconciled later.
            try {
                orderFeignClient.updateOrderStatus(orderId, "PAYMENT_RECEIVED", null, "ONLINE");
            } catch (Exception feignEx) {
                log.error("CRITICAL: Failed to update order {} to PAYMENT_RECEIVED after successful payment. " +
                          "Payment tx is committed. Manual reconciliation or webhook retry may be needed. Cause: {}",
                          orderId, feignEx.getMessage(), feignEx);
                // Do not rethrow — we still want to return success to the payment initiator.
            }

            // FIX: Generate the invoice automatically on Razorpay success!
            try {
                invoiceService.generateAndSaveInvoice(orderId);
            } catch (Exception e) {
                log.error("Invoice generation failed post-payment for order {}", orderId, e);
            }
        }
    }

    // 1. Customer Submits UTR
    @Override
    @Transactional
    public String submitUtrPayment(UtrSubmissionDTO dto) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderId(dto.getOrderId());
        transaction.setAmount(dto.getAmount());
        transaction.setPaymentMethod(BANK_TRANSFER);
        transaction.setUtrNumber(dto.getUtrNumber());
        transaction.setPaymentStatus(PENDING); // Waiting for admin

        paymentTransactionRepository.save(transaction);
        log.info("UTR submitted for order {}, amount={}", dto.getOrderId(), dto.getAmount());
        return "UTR Submitted. Pending Admin Verification.";
    }

    // 2. Admin Verifies UTR (FIXED: Ab ye Order ID se dhoondhega)
    @Override
    @Transactional
    public String verifyUtrPayment(String orderId, boolean isApproved) {
        // Changed findById to findByOrderId
        PaymentTransaction transaction = paymentTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment record not found for Order ID: " + orderId));

        if (isApproved) {
            transaction.setPaymentStatus(SUCCESS);
            paymentTransactionRepository.save(transaction);
            log.info("UTR approved for order {}", orderId);

            // Protect the cross-service update the same way as customer payments.
            try {
                orderFeignClient.updateOrderStatus(transaction.getOrderId(), "PAYMENT_RECEIVED", null, "BANK_TRANSFER");
            } catch (Exception feignEx) {
                log.error("CRITICAL: Failed to update order {} to PAYMENT_RECEIVED after UTR approval. " +
                          "Payment tx is SUCCESS. Reconciliation required. Cause: {}",
                          orderId, feignEx.getMessage(), feignEx);
            }

            try {
                invoiceService.generateAndSaveInvoice(transaction.getOrderId());
            } catch (Exception e) {
                log.error("Invoice generation failed after UTR approval for order {}", transaction.getOrderId(), e);
            }

            return "Payment Approved. Order marked as PAID and Invoice Generated.";
        } else {
            transaction.setPaymentStatus(REJECTED);
            paymentTransactionRepository.save(transaction);
            log.info("UTR rejected for order {}", orderId);

            // Optionally: Tell order service payment failed
            orderFeignClient.updateOrderStatus(transaction.getOrderId(), "PENDING_PAYMENT", null, null);
            return "Payment Rejected. Invalid UTR.";
        }
    }

    // 1. Customer selects Cash on Delivery at Checkout
    @Override
    @Transactional
    public String selectCashOnDelivery(String orderId, Double amount) {
        // Create a pending payment record
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderId(orderId);
        transaction.setAmount(amount);
        transaction.setPaymentMethod(CASH_ON_DELIVERY);
        transaction.setPaymentStatus(PENDING); // Cash is not collected yet!

        paymentTransactionRepository.save(transaction);
        log.info("COD selected for order {}, amount={}", orderId, amount);

        // IMPORTANT: Downstream failures (orders service, stock deduct, production, etc.) must not
        // cause the customer to see an error or lose the payment record. The COD payment record
        // is created; order status update can be reconciled.
        try {
            orderFeignClient.updateOrderStatus(orderId, "CONFIRMED_COD", null, "CASH_ON_DELIVERY");
        } catch (Exception feignEx) {
            log.error("CRITICAL: Failed to update order {} to CONFIRMED_COD after COD selection. " +
                      "Payment tx committed as PENDING/COD. Reconciliation needed. Cause: {}",
                      orderId, feignEx.getMessage(), feignEx);
            // Swallow — return success to UI so user is not stuck. The record exists.
        }

        // FIX: Generate the invoice immediately for Cash On Delivery!
        try {
            invoiceService.generateAndSaveInvoice(orderId);
        } catch (Exception e) {
            log.error("Invoice generation failed for COD order {}", orderId, e);
        }

        return "Cash on Delivery selected. Order Confirmed!";
    }

    // 2. Admin/Driver confirms cash was collected upon delivery
    @Override
    @Transactional
    public String confirmCodCollection(String paymentId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment record not found"));

        if (!CASH_ON_DELIVERY.equals(transaction.getPaymentMethod())) {
            throw new BadRequestException("This is not a COD transaction!");
        }

        // Mark the cash as successfully received
        transaction.setPaymentStatus(SUCCESS);
        paymentTransactionRepository.save(transaction);
        log.info("COD cash collected: paymentId={}, orderId={}", paymentId, transaction.getOrderId());

        // Protect downstream update
        try {
            orderFeignClient.updateOrderStatus(transaction.getOrderId(), "DELIVERED", null, null);
        } catch (Exception feignEx) {
            log.error("Failed to update order {} to DELIVERED after COD collection. Payment tx is SUCCESS. Cause: {}",
                      transaction.getOrderId(), feignEx.getMessage(), feignEx);
        }

        // Trigger the invoice generation now that payment is finalized!
        try {
            invoiceService.generateAndSaveInvoice(transaction.getOrderId());
        } catch (Exception e) {
            log.error("Invoice generation failed for COD collection on order {}", transaction.getOrderId(), e);
        }

        return "Cash collected successfully! Order marked as DELIVERED and Invoice Generated.";
    }

    // NAYA METHOD: Fetch payment data by Order ID
    @Override
    @Transactional(readOnly = true)
    public PaymentTransaction getPaymentDetailsByOrderId(String orderId) {
        validateOrderAccess(orderId);
        return paymentTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment record not found for Order ID: " + orderId));
    }

    private void validateOrderAccess(String orderId) {
        if (SecurityUtils.isAdmin() || SecurityUtils.hasRole("INTERNAL_SERVICE")) {
            return;
        }
        if (SecurityUtils.hasRole("CUSTOMER")) {
            String userId = SecurityUtils.getUserId()
                    .orElseThrow(() -> new UnauthorizedException("Unauthorized: user identity not available"));
            Map<String, Object> order = orderFeignClient.getOrderById(orderId);
            Object ownerId = order.get("customerId");
            if (ownerId == null || !userId.equals(ownerId.toString())) {
                throw new ForbiddenException("Access denied: you can only access your own orders");
            }
        }
    }

    @Override
    @Transactional
    public String initiateRefund(String orderId) {
        // 1. Find the payment transaction for this order
        PaymentTransaction tx = paymentTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment record not found for Order ID: " + orderId));

        // 2. Hum sirf SUCCESS payments ka hi refund karenge
        if (tx.getPaymentStatus() != SUCCESS) {
            throw new BadRequestException("Cannot process refund. Payment was not successful or already refunded.");
        }

        // 3. Create Refund Record
        RefundRecord refund = new RefundRecord();
        refund.setPaymentTransaction(tx);
        refund.setAmount(tx.getAmount());
        refund.setReason("Order cancelled by Admin");

        // 🚀 FIX: Handle Test Mode vs Live Mode
        try {
            if (tx.getPaymentMethod() == ONLINE && tx.getRazorpayPaymentId() != null) {

                if (isTestMode) {
                    // --- TEST MODE: SIMULATE REFUND ---
                    log.warn("Test mode active: simulating Razorpay refund for order {}", orderId);
                    refund.setRazorpayRefundId("test_rfnd_" + UUID.randomUUID().toString().substring(0, 8));
                    refund.setRefundStatus("PROCESSED_TEST");
                } else {
                    // --- LIVE MODE: ACTUAL API CALL ---
                    RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);
                    JSONObject refundRequest = new JSONObject();
                    refundRequest.put("amount", tx.getAmount() * 100); // Amount in paise
                    refundRequest.put("notes", new JSONObject().put("order_id", orderId));

                    com.razorpay.Refund rzpRefund = razorpay.payments.refund(tx.getRazorpayPaymentId(), refundRequest);

                    refund.setRazorpayRefundId(rzpRefund.get("id"));
                    refund.setRefundStatus("PROCESSED_ONLINE");
                }

            } else {
                // Offline Payment (COD / UTR)
                refund.setRefundStatus("PENDING_MANUAL_TRANSFER");
            }
        } catch (RazorpayException e) {
            throw new BadRequestException("Razorpay Gateway Error: Failed to process refund - " + e.getMessage());
        }

        refundRecordRepository.save(refund);

        // 4. Update transaction status
        tx.setPaymentStatus(REFUNDED);
        paymentTransactionRepository.save(tx);
        log.info("Refund initiated for order {}, amount={}, status={}", orderId, tx.getAmount(), refund.getRefundStatus());

        // Return meaningful messages based on what happened
        if ("PENDING_MANUAL_TRANSFER".equals(refund.getRefundStatus())) {
            return "Refund marked. (NOTE: Since this was COD/UTR, Admin must manually transfer ₹" + tx.getAmount() + ").";
        } else if (isTestMode) {
            return "TEST MODE: Simulated Refund of ₹" + tx.getAmount() + " processed successfully!";
        }

        return "LIVE: Real money refund of ₹" + tx.getAmount() + " initiated successfully via Razorpay!";
    }
}
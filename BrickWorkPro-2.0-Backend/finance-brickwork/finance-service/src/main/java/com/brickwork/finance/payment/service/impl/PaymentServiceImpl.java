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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.brickwork.finance.payment.enums.PaymentMethod.*;
import static com.brickwork.finance.payment.enums.PaymentStatus.*;

import java.util.Optional;
import java.util.UUID;


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
    public RazorpayOrderResponseDTO createRazorpayOrder(String orderId, Double amount) throws RazorpayException {
        // Idempotency check
        Optional<PaymentTransaction> existingTxOpt = paymentTransactionRepository.findByOrderId(orderId);
        if (existingTxOpt.isPresent() && existingTxOpt.get().getPaymentStatus() == SUCCESS) {
            throw new RuntimeException("Payment already completed for this order.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        }


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

        return new RazorpayOrderResponseDTO(razorpayOrder.get("id"), amount, "INR");
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
                return true;
            }
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
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (tx.getPaymentStatus() != SUCCESS) {
            tx.setRazorpayPaymentId(rzpPaymentId);
            tx.setPaymentStatus(SUCCESS);
            paymentTransactionRepository.save(tx);

            // Tell Orders Microservice that payment is done!
            orderFeignClient.updateOrderStatus(orderId, "PAYMENT_RECEIVED");

            // FIX: Generate the invoice automatically on Razorpay success!
            try {
                invoiceService.generateAndSaveInvoice(orderId);
            } catch (Exception e) {
                System.err.println("Invoice generation failed post-payment: " + e.getMessage());
                e.printStackTrace();
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
        return "UTR Submitted. Pending Admin Verification.";
    }

    // 2. Admin Verifies UTR (FIXED: Ab ye Order ID se dhoondhega)
    @Override
    @Transactional
    public String verifyUtrPayment(String orderId, boolean isApproved) {
        // Changed findById to findByOrderId
        PaymentTransaction transaction = paymentTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment record not found for Order ID: " + orderId));

        if (isApproved) {
            transaction.setPaymentStatus(SUCCESS);
            paymentTransactionRepository.save(transaction);

            // MAGIC HAPPENS HERE: We trigger the exact same automation as Razorpay!
            orderFeignClient.updateOrderStatus(transaction.getOrderId(), "PAYMENT_RECEIVED");

            try {
                invoiceService.generateAndSaveInvoice(transaction.getOrderId());
            } catch (Exception e) {
                System.err.println("Invoice generation failed, but payment successful: " + e.getMessage());
            }

            return "Payment Approved. Order marked as PAID and Invoice Generated.";
        } else {
            transaction.setPaymentStatus(REJECTED);
            paymentTransactionRepository.save(transaction);

            // Optionally: Tell order service payment failed
            orderFeignClient.updateOrderStatus(transaction.getOrderId(), "PENDING_PAYMENT");
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

        // Tell the Orders Service that the order is confirmed so the factory can start molding/dispatching
        orderFeignClient.updateOrderStatus(orderId, "CONFIRMED_COD");

        // FIX: Generate the invoice immediately for Cash On Delivery!
        try {
            invoiceService.generateAndSaveInvoice(orderId);
        } catch (Exception e) {
            System.err.println("Invoice generation failed for COD: " + e.getMessage() );
            e.printStackTrace();
        }

        return "Cash on Delivery selected. Order Confirmed!";
    }

    // 2. Admin/Driver confirms cash was collected upon delivery
    @Override
    @Transactional
    public String confirmCodCollection(String paymentId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        if (!CASH_ON_DELIVERY.equals(transaction.getPaymentMethod())) {
            throw new RuntimeException("This is not a COD transaction!");
        }

        // Mark the cash as successfully received
        transaction.setPaymentStatus(SUCCESS);
        paymentTransactionRepository.save(transaction);

        // Update the order status to reflect completion
        orderFeignClient.updateOrderStatus(transaction.getOrderId(), "DELIVERED");

        // Trigger the invoice generation now that payment is finalized!
        invoiceService.generateAndSaveInvoice(transaction.getOrderId());

        return "Cash collected successfully! Order marked as DELIVERED and Invoice Generated.";
    }

    // NAYA METHOD: Fetch payment data by Order ID
    @Override
    @Transactional(readOnly = true)
    public PaymentTransaction getPaymentDetailsByOrderId(String orderId) {
        return paymentTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment record not found for Order ID: " + orderId));
    }

    @Override
    @Transactional
    public String initiateRefund(String orderId) {
        // 1. Find the payment transaction for this order
        PaymentTransaction tx = paymentTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment record not found for Order ID: " + orderId));

        // 2. Hum sirf SUCCESS payments ka hi refund karenge
        if (tx.getPaymentStatus() != SUCCESS) {
            throw new RuntimeException("Cannot process refund. Payment was not successful or already refunded.");
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
                    System.out.println("⚠️ TEST MODE ACTIVE: Simulating Razorpay Refund for Order: " + orderId);
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
            throw new RuntimeException("Razorpay Gateway Error: Failed to process refund - " + e.getMessage());
        }

        refundRecordRepository.save(refund);

        // 4. Update transaction status
        tx.setPaymentStatus(REFUNDED);
        paymentTransactionRepository.save(tx);

        // Return meaningful messages based on what happened
        if ("PENDING_MANUAL_TRANSFER".equals(refund.getRefundStatus())) {
            return "Refund marked. (NOTE: Since this was COD/UTR, Admin must manually transfer ₹" + tx.getAmount() + ").";
        } else if (isTestMode) {
            return "TEST MODE: Simulated Refund of ₹" + tx.getAmount() + " processed successfully!";
        }

        return "LIVE: Real money refund of ₹" + tx.getAmount() + " initiated successfully via Razorpay!";
    }
}
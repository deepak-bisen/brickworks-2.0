package com.brickwork.finance.payment.service.impl;

import com.brickwork.finance.client.OrderFeignClient;
import com.brickwork.finance.payment.dto.RazorpayOrderResponseDTO;
import com.brickwork.finance.payment.dto.UtrSubmissionDTO;
import com.brickwork.finance.payment.entity.PaymentTransaction;
import com.brickwork.finance.payment.repository.PaymentTransactionRepository;
import com.brickwork.finance.invoice.service.InvoiceService;
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


@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Autowired
    private PaymentTransactionRepository  paymentTransactionRepository;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private InvoiceService  invoiceService;

    @Override
    @Transactional
    public RazorpayOrderResponseDTO createRazorpayOrder(String orderId, Double amount) throws RazorpayException {
        // Idempotency check
        Optional<PaymentTransaction> existingTxOpt = paymentTransactionRepository.findByOrderId(orderId);
        if (existingTxOpt.isPresent() && existingTxOpt.get().getPaymentStatus() == SUCCESS) {
            throw new RuntimeException("Payment already completed for this order.");
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
        }
    }

    // 1. Customer Submits UTR
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

    // 2. Admin Verifies UTR
    @Transactional
    public String verifyUtrPayment(String paymentId, boolean isApproved) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        if (isApproved) {
            transaction.setPaymentStatus(SUCCESS);
            paymentTransactionRepository.save(transaction);

            // MAGIC HAPPENS HERE: We trigger the exact same automation as Razorpay!
            orderFeignClient.updateOrderStatus(transaction.getOrderId(), "PAID");
            invoiceService.generateAndSaveInvoice(transaction.getOrderId());

            return "Payment Approved. Order marked as PAID and Invoice Generated.";
        } else {
            transaction.setPaymentStatus(REJECTED);
            paymentTransactionRepository.save(transaction);
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
        orderFeignClient.updateOrderStatus(orderId, "CONFIRMED");

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
}
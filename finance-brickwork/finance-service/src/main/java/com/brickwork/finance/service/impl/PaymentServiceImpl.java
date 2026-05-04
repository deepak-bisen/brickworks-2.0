package com.brickwork.finance.service.impl;

import com.brickwork.finance.client.OrderFeignClient;
import com.brickwork.finance.dto.RazorpayOrderResponseDTO;
import com.brickwork.finance.entity.PaymentTransaction;
import com.brickwork.finance.repository.PaymentTransactionRepository;
import com.brickwork.finance.service.PaymentService;
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
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Autowired
    private PaymentTransactionRepository transactionRepo;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Override
    @Transactional
    public RazorpayOrderResponseDTO createRazorpayOrder(String orderId, Double amount) throws RazorpayException {
        // Idempotency check
        Optional<PaymentTransaction> existingTxOpt = transactionRepo.findByOrderId(orderId);
        if (existingTxOpt.isPresent() && existingTxOpt.get().getStatus() == PaymentTransaction.PaymentStatus.SUCCESS) {
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
        transaction.setStatus(PaymentTransaction.PaymentStatus.CREATED);
        transactionRepo.save(transaction);

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
        transactionRepo.findByRazorpayOrderId(rzpOrderId).ifPresent(tx -> {
            processSuccessfulPayment(rzpOrderId, rzpPaymentId, tx.getOrderId());
        });
    }

    private void processSuccessfulPayment(String rzpOrderId, String rzpPaymentId, String orderId) {
        PaymentTransaction tx = transactionRepo.findByRazorpayOrderId(rzpOrderId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (tx.getStatus() != PaymentTransaction.PaymentStatus.SUCCESS) {
            tx.setRazorpayPaymentId(rzpPaymentId);
            tx.setStatus(PaymentTransaction.PaymentStatus.SUCCESS);
            transactionRepo.save(tx);

            // Tell Orders Microservice that payment is done!
            orderFeignClient.updateOrderStatus(orderId, "PAYMENT_RECEIVED");
        }
    }
}
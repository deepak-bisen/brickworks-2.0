package com.brickwork.finance.invoice.service.impl;

import com.brickwork.finance.client.OrderFeignClient;
import com.brickwork.finance.invoice.entity.InvoiceRecord;
import com.brickwork.exception.ForbiddenException;
import com.brickwork.exception.NotFoundException;
import com.brickwork.exception.UnauthorizedException;
import com.brickwork.finance.invoice.repository.InvoiceRecordRepository;
import com.brickwork.finance.invoice.service.InvoiceService;
import com.brickwork.security.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRecordRepository invoiceRepo;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private PdfInvoiceService pdfInvoiceService;

    @Override
    public String generateAndSaveInvoice(String orderId) {
        Map<String, Object> orderData = loadOrderDataForInvoice(orderId);

        InvoiceRecord invoice = invoiceRepo.findByOrderId(orderId).orElseGet(() -> {
            InvoiceRecord record = new InvoiceRecord();
            record.setOrderId(orderId);
            record.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            record.setTotalAmount(asDouble(orderData.get("totalAmount")));
            record.setTaxAmount(asDouble(orderData.get("taxAmount")));
            record.setFilePath("PENDING");
            record.setGeneratedDate(LocalDateTime.now());
            return invoiceRepo.save(record);
        });

        enrichOrderDataForTemplate(orderData, invoice);
        pdfInvoiceService.generatePdfFromHtml("invoice", orderData);
        log.info("Invoice generated: orderId={}, invoiceNumber={}", orderId, invoice.getInvoiceNumber());
        return invoice.getInvoiceNumber();
    }

    @Override
    public byte[] getInvoicePdfBytes(String orderId) {
        validateOrderAccess(orderId);
        InvoiceRecord invoice = invoiceRepo.findByOrderId(orderId).orElseThrow(
                () -> new NotFoundException("Invoice not generated yet")
        );

        Map<String, Object> orderData = loadOrderDataForInvoice(orderId);
        enrichOrderDataForTemplate(orderData, invoice);
        byte[] pdfBytes = pdfInvoiceService.generatePdfFromHtml("invoice", orderData);
        log.info("Invoice PDF downloaded: orderId={}, invoiceNumber={}", orderId, invoice.getInvoiceNumber());
        return pdfBytes;
    }

    private Map<String, Object> loadOrderDataForInvoice(String orderId) {
        validateOrderAccess(orderId);
        try {
            return normalizeOrderData(orderFeignClient.getOrderById(orderId));
        } catch (Exception ex) {
            log.error("Failed to load order {} for invoice", orderId, ex);
            throw new NotFoundException("Unable to load order details for invoice: " + ex.getMessage());
        }
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
            return;
        }

        throw new ForbiddenException("Unauthorized to access invoice for this order");
    }

    Map<String, Object> normalizeOrderData(Map<String, Object> raw) {
        Map<String, Object> orderData = new HashMap<>(raw != null ? raw : Map.of());

        Object items = orderData.get("items");
        if (items instanceof List<?> list) {
            List<Map<String, Object>> normalizedItems = new ArrayList<>();
            for (Object entry : list) {
                if (entry instanceof Map<?, ?> itemMap) {
                    Map<String, Object> item = new HashMap<>();
                    double unitPrice = asDouble(itemMap.get("unitPrice"));
                    int quantity = asInt(itemMap.get("quantity"));
                    item.put("productName", stringValue(itemMap.get("productName"), "Brick Product"));
                    item.put("quantity", quantity);
                    item.put("unitPrice", unitPrice);
                    item.put("lineTotal", unitPrice * quantity);
                    normalizedItems.add(item);
                }
            }
            orderData.put("items", normalizedItems);
        } else {
            orderData.put("items", new ArrayList<>());
        }

        orderData.putIfAbsent("orderId", "N/A");
        orderData.putIfAbsent("customerName", "Customer");
        orderData.putIfAbsent("deliveryAddress", "N/A");
        orderData.putIfAbsent("customerPhone", "N/A");
        orderData.putIfAbsent("customerEmail", "N/A");
        orderData.putIfAbsent("status", "PENDING");
        orderData.putIfAbsent("grossAmount", asDouble(orderData.get("totalAmount")));
        orderData.putIfAbsent("discountApplied", 0.0);
        orderData.putIfAbsent("taxAmount", 0.0);
        return orderData;
    }

    private void enrichOrderDataForTemplate(Map<String, Object> orderData, InvoiceRecord invoice) {
        double totalAmount = asDouble(orderData.get("totalAmount"));
        double taxableAmount = totalAmount / 1.18;
        double totalGst = totalAmount - taxableAmount;
        String status = stringValue(orderData.get("status"), "PENDING");
        String paymentMode = status.contains("COD") ? "Cash On Delivery (COD)" : "Online / Prepaid";

        orderData.put("invoiceNumber", invoice.getInvoiceNumber());
        orderData.put("invoiceDate", invoice.getGeneratedDate().toLocalDate().toString());
        orderData.put("paymentMethod", paymentMode);
        orderData.put("taxableAmount", taxableAmount);
        orderData.put("cgst", totalGst / 2);
        orderData.put("sgst", totalGst / 2);
        orderData.put("totalAmount", totalAmount);
    }

    double asDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    int asInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    String stringValue(Object value, String fallback) {
        return value == null || value.toString().isBlank() ? fallback : value.toString();
    }
}
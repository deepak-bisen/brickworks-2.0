package com.brickwork.finance.invoice.service.impl;

import com.brickwork.finance.client.OrderFeignClient;
import com.brickwork.finance.invoice.entity.InvoiceRecord;
import com.brickwork.finance.invoice.exception.InvoiceNotFoundException;
import com.brickwork.finance.invoice.repository.InvoiceRecordRepository;
import com.brickwork.finance.invoice.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRecordRepository invoiceRepo;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private PdfInvoiceService pdfInvoiceService; // Your existing Thymeleaf/Openhtmltopdf engine

    @Override
    public String generateAndSaveInvoice(String orderId) {
        // Fetch real order data via Feign!
        Map<String, Object> orderData = orderFeignClient.getOrderById(orderId);

        // 🚀 MAGIC FIX: Extract and reverse calculate 18% GST immediately
        double totalAmount = Double.parseDouble(orderData.getOrDefault("totalAmount", "0.0").toString());
        double taxableAmount = totalAmount / 1.18;
        double totalGst = totalAmount - taxableAmount;
        double cgst = totalGst / 2;
        double sgst = totalGst / 2;

        String status = (String) orderData.getOrDefault("status", "PENDING");
        String paymentMode = status.contains("COD") ? "Cash On Delivery (COD)" : "Online / Prepaid";

        // Setup Invoice Record
        InvoiceRecord invoice = new InvoiceRecord();
        invoice.setOrderId(orderId);
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invoice.setTotalAmount(Double.valueOf(orderData.getOrDefault("totalAmount", 0.0).toString()));
        invoice.setTaxAmount(Double.valueOf(orderData.getOrDefault("taxAmount", 0.0).toString()));
        invoice.setFilePath("PENDING");
        invoiceRepo.save(invoice);

        // Inject invoice details into the map for Thymeleaf
        orderData.put("invoiceNumber", invoice.getInvoiceNumber());

        //  FIX: Safely handle Date to prevent NullPointerException Crash
        String dateStr = (invoice.getGeneratedDate() != null)
                ? invoice.getGeneratedDate().toString().substring(0, 10)
                : java.time.LocalDate.now().toString();
        orderData.put("invoiceDate", dateStr);

        // 🚀 CRITICAL FIX: Inject all proper metrics for first-time Thymeleaf Template rendering
        orderData.put("paymentMethod", paymentMode);
        orderData.put("taxableAmount", taxableAmount);
        orderData.put("cgst", cgst);
        orderData.put("sgst", sgst);

        // Ensure missing fields have fallbacks
        orderData.putIfAbsent("customerPhone", "N/A");
        orderData.putIfAbsent("customerEmail", "N/A");

        // Generate the PDF Bytes
        byte[] pdfBytes = pdfInvoiceService.generatePdfFromHtml("invoice", orderData);

        // In a real enterprise app, you upload pdfBytes to AWS S3 here and save the URL.
        // For now, we rely on dynamic regeneration for downloads.

        return invoice.getInvoiceNumber();
    }
    @Override
    public byte[] getInvoicePdfBytes(String orderId) {
        Optional<InvoiceRecord> invoiceOptional = invoiceRepo.findByOrderId(orderId);
        InvoiceRecord invoice = invoiceOptional.orElseThrow(
                () -> new InvoiceNotFoundException("Invoice not generated yet")
        );

        Map<String, Object> orderData = orderFeignClient.getOrderById(orderId);

        // --- ENTERPRISE INVOICE MATH CALCULATION ---
        double totalAmount = Double.parseDouble(orderData.getOrDefault("totalAmount", "0.0").toString());
        double grossAmount = Double.parseDouble(orderData.getOrDefault("grossAmount", String.valueOf(totalAmount)).toString());
        double discountApplied = Double.parseDouble(orderData.getOrDefault("discountApplied", "0.0").toString());
        String status = (String) orderData.getOrDefault("status", "PENDING");

        // Reverse Calculate 18% GST (Taxable Base Value)
        double taxableAmount = totalAmount / 1.18;
        double totalGst = totalAmount - taxableAmount;
        double cgst = totalGst / 2;
        double sgst = totalGst / 2;

        // Determine Payment Mode safely
        String paymentMode = status.contains("COD") ? "Cash On Delivery (COD)" : "Online / Prepaid";

        // Inject all proper metrics for Thymeleaf Template
        orderData.put("invoiceNumber", invoice.getInvoiceNumber());
        orderData.put("invoiceDate", invoice.getGeneratedDate().toString().substring(0, 10)); // Format YYYY-MM-DD
        orderData.put("paymentMethod", paymentMode);
        orderData.put("taxableAmount", taxableAmount);
        orderData.put("cgst", cgst);
        orderData.put("sgst", sgst);
        // Ensure missing fields have fallbacks
        orderData.putIfAbsent("customerPhone", "N/A");
        orderData.putIfAbsent("customerEmail", "N/A");

        return pdfInvoiceService.generatePdfFromHtml("invoice", orderData);
    }
}
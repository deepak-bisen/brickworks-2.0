package com.brickwork.finance.service.impl;

import com.brickwork.finance.client.OrderFeignClient;
import com.brickwork.finance.entity.InvoiceRecord;
import com.brickwork.finance.repository.InvoiceRecordRepository;
import com.brickwork.finance.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
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

        // Setup Invoice Record
        InvoiceRecord invoice = new InvoiceRecord();
        invoice.setOrderId(orderId);
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        // Extract totals from orderData map here based on your order structure
        invoice.setTotalAmount(Double.valueOf(orderData.getOrDefault("totalAmount", 0.0).toString()));
        invoiceRepo.save(invoice);

        // Inject invoice details into the map for Thymeleaf
        orderData.put("invoiceNumber", invoice.getInvoiceNumber());
        orderData.put("invoiceDate", invoice.getGeneratedDate().toString());

        // Generate the PDF Bytes
        byte[] pdfBytes = pdfInvoiceService.generatePdfFromHtml("invoice", orderData);

        // In a real enterprise app, you upload pdfBytes to AWS S3 here and save the URL.
        // For now, we rely on dynamic regeneration for downloads.

        return invoice.getInvoiceNumber();
    }

    @Override
    public byte[] getInvoicePdfBytes(String orderId) {
        // Verify invoice exists
        invoiceRepo.findByOrderId(orderId).orElseThrow(() -> new RuntimeException("Invoice not generated yet"));

        // Fetch order data and regenerate PDF
        Map<String, Object> orderData = orderFeignClient.getOrderById(orderId);
        InvoiceRecord invoice = invoiceRepo.findByOrderId(orderId).get();

        orderData.put("invoiceNumber", invoice.getInvoiceNumber());
        orderData.put("invoiceDate", invoice.getGeneratedDate().toString());

        return pdfInvoiceService.generatePdfFromHtml("invoice", orderData);
    }
}